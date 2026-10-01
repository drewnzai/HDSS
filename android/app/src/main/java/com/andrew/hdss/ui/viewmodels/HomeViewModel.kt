package com.andrew.hdss.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andrew.hdss.HdssApplication
import com.andrew.hdss.data.daos.HouseholdDao
import com.andrew.hdss.data.daos.LocationDao
import com.andrew.hdss.data.models.Household
import com.andrew.hdss.data.models.Location
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class HomeViewModel(
    private val locationDao: LocationDao,
    private val householdDao: HouseholdDao
) : ViewModel() {

    val roots: StateFlow<List<Location>> =
        locationDao.getChildren(null)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    private val _selectedPath = MutableStateFlow<List<Location>>(emptyList())
    val selectedPath: StateFlow<List<Location>> = _selectedPath

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentChildren: StateFlow<List<Location>> =
        _selectedPath
            .flatMapLatest { path ->
                val parentId = path.lastOrNull()?.id

                locationDao.getChildren(parentId)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val householdsAtSelection: StateFlow<List<Household>> =
        _selectedPath
            .flatMapLatest { path ->
                val leafId = path.lastOrNull()?.id
                if (leafId != null) householdDao.getByLocationId(leafId) else flowOf(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectLocation(location: Location) {
        _selectedPath.update { path ->
            path + location
        }
    }

    fun selectPathLocation(index: Int) {
        _selectedPath.update { path ->
            path.take(index + 1)
        }
    }

    fun clearSelection() {
        _selectedPath.value = emptyList()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as HdssApplication)
                val locationDao = application.container.database.locationDao()
                val householdDao = application.container.database.householdDao()
                HomeViewModel(
                    locationDao = locationDao,
                    householdDao = householdDao
                )
            }
        }
    }
}