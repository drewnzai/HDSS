package com.andrew.hdss.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andrew.hdss.HdssApplication
import com.andrew.hdss.data.daos.AnswerDao
import com.andrew.hdss.data.daos.ChoiceDao
import com.andrew.hdss.data.daos.FormDao
import com.andrew.hdss.data.daos.FormResponseDao
import com.andrew.hdss.data.daos.HouseholdDao
import com.andrew.hdss.data.daos.IndividualDao
import com.andrew.hdss.data.daos.MembershipDao
import com.andrew.hdss.data.daos.QuestionDao
import com.andrew.hdss.data.models.Answer
import com.andrew.hdss.data.models.Choice
import com.andrew.hdss.data.models.Form
import com.andrew.hdss.data.models.FormResponse
import com.andrew.hdss.data.models.Individual
import com.andrew.hdss.data.models.Question
import com.andrew.hdss.data.models.enums.FormResponseStatus
import com.andrew.hdss.data.models.enums.MappedEntity
import com.andrew.hdss.data.models.enums.QuestionType
import com.andrew.hdss.data.models.enums.Sex
import com.andrew.hdss.util.AnswerEntityMapper
import com.andrew.hdss.util.ExprValue
import com.andrew.hdss.util.ExpressionEvaluator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.Period
import java.util.UUID

data class FormFillContext(
    val locationId: Long? = null,
    val entityClientIds: Map<MappedEntity, String> = emptyMap(),
    val foreignKeys: Map<String, String> = emptyMap(),
    // Values the app generates itself and never asks as questions, keyed
    // by entity then field, e.g. HOUSEHOLD -> {"householdCode": "EM-0000001"}.
    // These win over an answer mapped to the same field.
    val generatedFields: Map<MappedEntity, Map<String, String>> = emptyMap()
)

data class FormFillUiState(
    val form: Form? = null,
    val allQuestions: List<Question> = emptyList(),
    val choicesByListName: Map<String, List<Choice>> = emptyMap(),
    val answers: Map<Long, String> = emptyMap(),
    // Everything relevant right now, CALCULATE included — used for
    // persistence and answer->entity mapping at submit time.
    val relevantQuestions: List<Question> = emptyList(),
    // relevantQuestions minus CALCULATE — used for the stepper. A
    // CALCULATE question has no UI, so it must never be something the
    // enumerator "lands on."
    val navigableQuestions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val constraintError: String? = null,
    val isSaving: Boolean = false,
    val isComplete: Boolean = false
)

class FormFillViewModel(
    private val formId: Long,
    private val visitId: String,
    private val context: FormFillContext,
    private val formDao: FormDao,
    private val questionDao: QuestionDao,
    private val choiceDao: ChoiceDao,
    private val formResponseDao: FormResponseDao,
    private val answerDao: AnswerDao,
    private val householdDao: HouseholdDao,
    private val individualDao: IndividualDao,
    private val membershipDao: MembershipDao
) : ViewModel() {

    private val evaluator = ExpressionEvaluator()
    private val _uiState = MutableStateFlow(FormFillUiState())
    val uiState: StateFlow<FormFillUiState> = _uiState.asStateFlow()

    private val startedAt: LocalDateTime = LocalDateTime.now()

    init {
        viewModelScope.launch { loadForm() }
    }

    private suspend fun loadForm() {
        val form = formDao.getById(formId)
        val questions = questionDao.getByFormId(formId)
        val listNames = questions.mapNotNull { it.choiceListName }.distinct()
        val choicesByListName = listNames.associateWith { name ->
            choiceDao.getAllByListName(name).sortedBy { it.orderIndex }
        }

        val relevant = recomputeRelevant(questions, emptyMap())
        _uiState.update {
            it.copy(
                form = form,
                allQuestions = questions,
                choicesByListName = choicesByListName,
                relevantQuestions = relevant,
                navigableQuestions = relevant.filterNot { q -> q.type == QuestionType.CALCULATE }
            )
        }
    }

    private fun answersAsStrings(answers: Map<Long, String>, questions: List<Question>): Map<String, String?> =
        questions.associate { q -> q.name to answers[q.id] }

    private fun safeEval(
        expression: String,
        context: Map<String, String?>,
        selfValue: String? = null
    ): ExprValue? = try {
        evaluator.evaluate(expression, context, selfValue)
    } catch (e: Exception) {
        Log.w("FormFill", "Could not evaluate '$expression': ${e.message}")
        null
    }

    private fun recomputeRelevant(questions: List<Question>, answers: Map<Long, String>): List<Question> {
        val ctx = answersAsStrings(answers, questions)
        return questions.filter { q ->
            // A broken relevant expression fails open: the question is shown
            // rather than silently hidden from the enumerator.
            q.relevant.isNullOrBlank() || (safeEval(q.relevant, ctx)?.asBoolean() ?: true)
        }
    }

    private fun recomputeCalculations(questions: List<Question>, answers: Map<Long, String>): Map<Long, String> {
        val updated = answers.toMutableMap()
        questions.filter { it.type == QuestionType.CALCULATE }.forEach { q ->
            val ctx = answersAsStrings(updated, questions)
            val isRelevant = q.relevant.isNullOrBlank() || (safeEval(q.relevant, ctx)?.asBoolean() ?: true)
            val calc = q.calculation

            // No result means "nothing to record yet" (an input isn't answered,
            // or the expression is broken). Remove any stale value instead of
            // storing "".
            val result = if (isRelevant && !calc.isNullOrBlank()) safeEval(calc, ctx)?.asString() else null
            if (result.isNullOrBlank()) {
                updated.remove(q.id)
            } else {
                updated[q.id] = result
            }
        }
        return updated
    }

    fun updateAnswer(question: Question, rawValue: String) {
        val state = _uiState.value
        var updatedAnswers = state.answers + (question.id to rawValue)
        updatedAnswers = recomputeCalculations(state.allQuestions, updatedAnswers)
        val relevant = recomputeRelevant(state.allQuestions, updatedAnswers)
        _uiState.update {
            it.copy(
                answers = updatedAnswers,
                relevantQuestions = relevant,
                navigableQuestions = relevant.filterNot { q -> q.type == QuestionType.CALCULATE },
                constraintError = null
            )
        }
    }

    // For SELECT_HOUSEHOLD_MEMBER — resolves candidates from the current
    // household, filtered by sex per the motherClientId/fatherClientId
    // mappedField naming convention, restricted to adults.
    // ASSUMPTION: 15 as the adult-age cutoff — never actually specified;
    // confirm the real minimum age for this survey.
    suspend fun loadHouseholdMembers(question: Question): List<Individual> {
        val householdClientId = context.foreignKeys["householdClientId"]
            ?: context.entityClientIds[MappedEntity.HOUSEHOLD]
            ?: return emptyList()

        val memberships = membershipDao.getCurrentMembers(householdClientId).first()
        val individuals = individualDao.getByClientIds(memberships.map { it.individualClientId })

        val sexFilter = when (question.mappedField) {
            "motherClientId" -> Sex.FEMALE
            "fatherClientId" -> Sex.MALE
            else -> null
        }

        return individuals.filter { individual ->
            val sexMatches = sexFilter == null || individual.sex == sexFilter
            val isAdult = individual.dateOfBirth?.let { dob ->
                Period.between(dob, java.time.LocalDate.now()).years >= 15
            } ?: false
            sexMatches && isAdult
        }
    }

    fun goNext() {
        val state = _uiState.value
        val current = state.navigableQuestions.getOrNull(state.currentIndex) ?: return

        if (current.required && state.answers[current.id].isNullOrBlank() &&
            current.type != QuestionType.NOTE) {
            _uiState.update { it.copy(constraintError = "This question is required.") }
            return
        }

        val constraint = current.constraint
        val value = state.answers[current.id]
        if (!constraint.isNullOrBlank() && !value.isNullOrBlank()) {
            val ctx = answersAsStrings(state.answers, state.allQuestions)
            val satisfied = safeEval(constraint, ctx, selfValue = value)?.asBoolean() ?: true
            if (!satisfied) {
                _uiState.update { it.copy(constraintError = current.constraintMessage ?: "Invalid value.") }
                return
            }
        }

        val nextIndex = state.currentIndex + 1
        if (nextIndex >= state.navigableQuestions.size) {
            submit()
        } else {
            _uiState.update { it.copy(currentIndex = nextIndex, constraintError = null) }
        }
    }

    fun goBack() {
        _uiState.update { it.copy(currentIndex = (it.currentIndex - 1).coerceAtLeast(0), constraintError = null) }
    }

    private fun submit() {
        val state = _uiState.value
        val form = state.form ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val formResponseId = UUID.randomUUID().toString()
                formResponseDao.insert(
                    FormResponse(
                        id = formResponseId,
                        visitId = visitId,
                        formId = form.id,
                        formVersion = form.version,
                        status = FormResponseStatus.COMPLETED,
                        startedAt = startedAt,
                        completedAt = LocalDateTime.now(),
                        synced = false
                    )
                )

                val answerRows = state.relevantQuestions
                    .filter { state.answers.containsKey(it.id) }
                    .map { q ->
                        Answer(
                            id = UUID.randomUUID().toString(),
                            formResponseId = formResponseId,
                            questionId = q.id,
                            value = state.answers.getValue(q.id),
                            synced = false
                        )
                    }
                answerDao.insertAll(answerRows)

                val bags = AnswerEntityMapper.buildFieldBags(state.relevantQuestions, state.answers)

                val fieldsByEntity = (bags.keys + context.generatedFields.keys).associateWith { entity ->
                    (bags[entity] ?: emptyMap()) + (context.generatedFields[entity] ?: emptyMap())
                }

                fieldsByEntity[MappedEntity.HOUSEHOLD]?.let { fields ->
                    val clientId = context.entityClientIds.getValue(MappedEntity.HOUSEHOLD)
                    val locationId = context.locationId
                        ?: throw IllegalStateException("locationId is required to build a Household")
                    householdDao.insert(AnswerEntityMapper.buildHousehold(clientId, locationId, fields))
                }

                fieldsByEntity[MappedEntity.INDIVIDUAL]?.let { fields ->
                    val clientId = context.entityClientIds.getValue(MappedEntity.INDIVIDUAL)
                    individualDao.insert(AnswerEntityMapper.buildIndividual(clientId, fields))
                }

                fieldsByEntity[MappedEntity.MEMBERSHIP]?.let { fields ->
                    val clientId = context.entityClientIds.getValue(MappedEntity.MEMBERSHIP)
                    val individualClientId = context.entityClientIds[MappedEntity.INDIVIDUAL]
                        ?: context.foreignKeys.getValue("individualClientId")
                    val householdClientId = context.foreignKeys.getValue("householdClientId")
                    val membership = AnswerEntityMapper.buildMembership(clientId, individualClientId, householdClientId, fields)
                    membershipDao.insert(membership)
                    AnswerEntityMapper.applyHeadOfHouseholdRulesLocally(membership, householdDao)
                }

                _uiState.update { it.copy(isSaving = false, isComplete = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, constraintError = "Failed to save: ${e.message}") }
            }
        }
    }

    companion object {
        fun factory(formId: Long, visitId: String, context: FormFillContext) = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as HdssApplication
                val db = application.container.database
                FormFillViewModel(
                    formId = formId,
                    visitId = visitId,
                    context = context,
                    formDao = db.formDao(),
                    questionDao = db.questionDao(),
                    choiceDao = db.choiceDao(),
                    formResponseDao = db.formResponseDao(),
                    answerDao = db.answerDao(),
                    householdDao = db.householdDao(),
                    individualDao = db.individualDao(),
                    membershipDao = db.membershipDao()
                )
            }
        }
    }
}

private fun MutableStateFlow<FormFillUiState>.update(transform: (FormFillUiState) -> FormFillUiState) {
    value = transform(value)
}