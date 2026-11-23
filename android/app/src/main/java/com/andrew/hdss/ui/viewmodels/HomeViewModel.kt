package com.andrew.hdss.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andrew.hdss.HdssApplication
import com.andrew.hdss.data.daos.FormDao
import com.andrew.hdss.data.daos.HouseholdDao
import com.andrew.hdss.data.daos.LocationDao
import com.andrew.hdss.data.daos.MemberRow
import com.andrew.hdss.data.daos.MembershipDao
import com.andrew.hdss.data.models.Form
import com.andrew.hdss.data.models.Household
import com.andrew.hdss.data.models.Location
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// The household and person the user has picked. Lives here so the tabs and
// panes share it, and it survives rotation.
data class HomeSelection(
    val householdClientId: String? = null,
    val personClientId: String? = null
)

class HomeViewModel(
    private val locationDao: LocationDao,
    private val householdDao: HouseholdDao,
    private val membershipDao: MembershipDao,
    private val formDao: FormDao
) : ViewModel() {

    val roots: StateFlow<List<Location>> = locationDao.getChildren(null)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedPath = MutableStateFlow<List<Location>>(emptyList())
    val selectedPath: StateFlow<List<Location>> = _selectedPath.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentChildren: StateFlow<List<Location>> = _selectedPath
        .flatMapLatest { path -> locationDao.getChildren(path.lastOrNull()?.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val householdsAtSelection: StateFlow<List<Household>> = _selectedPath
        .flatMapLatest { path ->
            path.lastOrNull()?.id?.let { householdDao.getByLocationId(it) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val forms: StateFlow<List<Form>> = formDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selection = MutableStateFlow(HomeSelection())
    val selection: StateFlow<HomeSelection> = _selection.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val members: StateFlow<List<MemberRow>> = _selection
        .map { it.householdClientId }
        .distinctUntilChanged()
        .flatMapLatest { id ->
            id?.let { membershipDao.observeOpenMembers(it) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            members.collect { list ->
                _selection.update { current ->
                    val person = current.personClientId
                    if (person != null && list.none { it.clientId == person }) {
                        current.copy(personClientId = null)
                    } else {
                        current
                    }
                }
            }
        }
    }

    fun selectLocation(location: Location) {
        _selectedPath.update { it + location }
        _selection.value = HomeSelection()
    }

    fun selectPathLocation(index: Int) {
        _selectedPath.update { it.take(index + 1) }
        _selection.value = HomeSelection()
    }

    fun clearSelection() {
        _selectedPath.value = emptyList()
        _selection.value = HomeSelection()
    }

    fun selectHousehold(id: String) {
        _selection.update { current ->
            if (current.householdClientId == id) HomeSelection()
            else HomeSelection(householdClientId = id)
        }
    }

    fun selectPerson(id: String) {
        _selection.update { current ->
            current.copy(personClientId = if (current.personClientId == id) null else id)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val db = (this[APPLICATION_KEY] as HdssApplication).container.database
                HomeViewModel(
                    locationDao = db.locationDao(),
                    householdDao = db.householdDao(),
                    membershipDao = db.membershipDao(),
                    formDao = db.formDao()
                )
            }
        }
    }
}