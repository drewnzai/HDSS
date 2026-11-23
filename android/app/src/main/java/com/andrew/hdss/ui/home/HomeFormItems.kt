package com.andrew.hdss.ui.home

import com.andrew.hdss.data.models.Form
import com.andrew.hdss.data.models.enums.FormCategory
import com.andrew.hdss.data.models.enums.FormTarget

const val FORM_SEARCH_THRESHOLD = 8

enum class FormSection { CORE, EXTRA }

// Declared in display order, which groupForms relies on.
enum class FormScope { LOCATION, HOUSEHOLD, PERSON }

// The core flows the app has screens for. Each is matched to a form by name,
// so a CORE form the app doesn't know about (such as test_form) never appears.
enum class CoreFlow(
    val formName: String?,
    val scope: FormScope,
    val label: String,
    val destructive: Boolean = false
) {
    REGISTER_HOUSEHOLD("household-registration", FormScope.LOCATION, "Register new household"),
    ADD_INDIVIDUAL("individual-registration", FormScope.HOUSEHOLD, "Add individual"),
    TRANSFER_IN(null, FormScope.HOUSEHOLD, "Transfer in"),
    IMMIGRATION("immigration-registration", FormScope.HOUSEHOLD, "Immigration"),
    DEATH("death-registration", FormScope.PERSON, "Record death", destructive = true),
    EMIGRATION("emigration-registration", FormScope.PERSON, "Record emigration")
}

data class HomeFormItem(
    val key: String,
    val label: String,
    val section: FormSection,
    val scope: FormScope,
    val enabled: Boolean,
    val destructive: Boolean = false,
    val note: String? = null,
    val formId: Long? = null
)

data class HomeFormSelection(
    val hasLeaf: Boolean,
    val householdSelected: Boolean,
    val personSelected: Boolean
) {
    fun allows(scope: FormScope): Boolean = when (scope) {
        FormScope.LOCATION -> hasLeaf
        FormScope.HOUSEHOLD -> householdSelected
        FormScope.PERSON -> personSelected
    }
}

fun FormScope.hint(): String = when (this) {
    FormScope.LOCATION -> "Choose a location first."
    FormScope.HOUSEHOLD -> "Select a household to use these forms."
    FormScope.PERSON -> "Select a person in the household."
}

fun coreFormItems(forms: List<Form>, selection: HomeFormSelection): List<HomeFormItem> =
    CoreFlow.entries.map { flow ->
        val formId = flow.formName?.let { name -> forms.firstOrNull { it.name == name }?.id }
        val missing = flow.formName != null && formId == null

        HomeFormItem(
            key = flow.name,
            label = flow.label,
            section = FormSection.CORE,
            scope = flow.scope,
            enabled = !missing && selection.allows(flow.scope),
            destructive = flow.destructive,
            note = if (missing) "This form hasn't been downloaded yet." else null,
            formId = formId
        )
    }

fun extraFormItems(forms: List<Form>, selection: HomeFormSelection): List<HomeFormItem> =
    forms
        .filter { it.category == FormCategory.EXTRA && it.active }
        .map { form ->
            val scope = if (form.target == FormTarget.INDIVIDUAL) FormScope.PERSON else FormScope.HOUSEHOLD
            HomeFormItem(
                key = form.name,
                label = form.title.ifBlank { form.name },
                section = FormSection.EXTRA,
                scope = scope,
                enabled = selection.allows(scope),
                note = form.description?.takeIf { it.isNotBlank() },
                formId = form.id
            )
        }
        .sortedBy { it.label }

data class FormGroupUi(
    val section: FormSection,
    val scope: FormScope,
    val items: List<HomeFormItem>
)

fun groupForms(items: List<HomeFormItem>, query: String): List<FormGroupUi> {
    val matching = if (query.isBlank()) items
    else items.filter { it.label.contains(query, ignoreCase = true) }

    return matching
        .groupBy { it.section to it.scope }
        .map { (key, groupItems) -> FormGroupUi(key.first, key.second, groupItems) }
        .sortedWith(compareBy({ it.section }, { it.scope }))
}

fun FormScope.title(): String = when (this) {
    FormScope.LOCATION -> "At this location"
    FormScope.HOUSEHOLD -> "This household"
    FormScope.PERSON -> "Selected person"
}