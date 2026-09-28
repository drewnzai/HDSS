package com.andrew.hdss.ui.viewmodels

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
import kotlin.collections.associate
import kotlin.collections.filter

data class FormFillContext(
    val locationId: Long? = null,
    val entityClientIds: Map<MappedEntity, String> = emptyMap(),
    val foreignKeys: Map<String, String> = emptyMap()
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

    private fun recomputeRelevant(questions: List<Question>, answers: Map<Long, String>): List<Question> {
        val answerContext = answersAsStrings(answers, questions)
        return questions.filter { q ->
            q.relevant.isNullOrBlank() || evaluator.evaluate(q.relevant, answerContext).asBoolean()
        }
    }

    // Computes CALCULATE questions, but only assigns a value when that
    // question's OWN relevant condition currently holds — a
    // non-relevant CALCULATE question must not leave a stale value
    // sitting in `answers`, or the mapper's mutual-exclusivity guard
    // (skip unanswered/non-relevant) would be defeated by a leftover
    // value from an earlier state.
    private fun recomputeCalculations(questions: List<Question>, answers: Map<Long, String>): Map<Long, String> {
        val updated = answers.toMutableMap()
        questions.filter { it.type == QuestionType.CALCULATE }.forEach { q ->
            val answerContext = answersAsStrings(updated, questions)
            val isRelevant = q.relevant.isNullOrBlank() ||
                    evaluator.evaluate(q.relevant, answerContext).asBoolean()
            val calc = q.calculation
            if (isRelevant && !calc.isNullOrBlank()) {
                updated[q.id] = evaluator.evaluate(calc, answerContext).asString()
            } else {
                updated.remove(q.id)
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
            val answerContext = answersAsStrings(state.answers, state.allQuestions)
            val satisfied = evaluator.evaluate(constraint, answerContext, selfValue = value).asBoolean()
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

                bags[MappedEntity.HOUSEHOLD]?.let { fields ->
                    val clientId = context.entityClientIds.getValue(MappedEntity.HOUSEHOLD)
                    val locationId = context.locationId
                        ?: throw IllegalStateException("locationId is required to build a Household")
                    householdDao.insert(AnswerEntityMapper.buildHousehold(clientId, locationId, fields))
                }

                bags[MappedEntity.INDIVIDUAL]?.let { fields ->
                    val clientId = context.entityClientIds.getValue(MappedEntity.INDIVIDUAL)
                    individualDao.insert(AnswerEntityMapper.buildIndividual(clientId, fields))
                }

                bags[MappedEntity.MEMBERSHIP]?.let { fields ->
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