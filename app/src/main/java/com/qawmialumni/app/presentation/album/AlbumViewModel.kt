package com.qawmialumni.app.presentation.album

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qawmialumni.app.core.datastore.UserPreferencesDataStore
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.KhedmatCategory
import com.qawmialumni.app.domain.model.UserProfile
import com.qawmialumni.app.domain.model.UserRole
import com.qawmialumni.app.domain.model.UserStatus
import com.qawmialumni.app.domain.repository.AdminRepository
import com.qawmialumni.app.domain.repository.AlumniRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlbumFilterState(
    val batch: String = "",
    val ustadId: String = "",
    val bloodGroup: String = "",
    val district: String = "",
    val query: String = "",
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val alumniRepository: AlumniRepository,
    private val adminRepository: AdminRepository,
    val dataStore: UserPreferencesDataStore,
) : ViewModel() {

    val session = dataStore.userSession.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    private val _filterState = MutableStateFlow(AlbumFilterState())
    val filterState: StateFlow<AlbumFilterState> = _filterState.asStateFlow()

    private val _syncState = MutableStateFlow<Resource<Int>>(Resource.Loading)
    val syncState: StateFlow<Resource<Int>> = _syncState.asStateFlow()

    private val _addStudentState = MutableStateFlow<Resource<Unit>?>(null)
    val addStudentState: StateFlow<Resource<Unit>?> = _addStudentState.asStateFlow()

    // Filtered alumni stream from local Room Database (Zero Firestore reads during browsing!)
    val alumniList: StateFlow<List<UserProfile>> = dataStore.userSession
        .flatMapLatest { userSession ->
            if (userSession.madrasaId.isBlank()) {
                flowOf(emptyList())
            } else {
                _filterState.flatMapLatest { filter ->
                    alumniRepository.getAlumni(
                        madrasaId = userSession.madrasaId,
                        batch = filter.batch,
                        ustadId = filter.ustadId,
                        bloodGroup = filter.bloodGroup,
                        district = filter.district,
                        query = filter.query,
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    val batches: StateFlow<List<String>> = dataStore.userSession
        .flatMapLatest { userSession ->
            if (userSession.madrasaId.isBlank()) flowOf(emptyList())
            else alumniRepository.getBatches(userSession.madrasaId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    val ustads = dataStore.userSession
        .flatMapLatest { userSession ->
            if (userSession.madrasaId.isBlank()) flowOf(Resource.Success(emptyList()))
            else adminRepository.getUstads(userSession.madrasaId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Loading,
        )

    init {
        viewModelScope.launch {
            dataStore.userSession.collect { s ->
                if (s.madrasaId.isNotBlank()) {
                    triggerSync(s.madrasaId)
                }
            }
        }
    }

    fun triggerSync(madrasaId: String) {
        viewModelScope.launch {
            _syncState.value = Resource.Loading
            _syncState.value = alumniRepository.syncAlumniFromRemote(madrasaId)
        }
    }

    fun updateQuery(query: String) {
        _filterState.value = _filterState.value.copy(query = query)
    }

    fun selectBatch(batch: String) {
        val current = _filterState.value.batch
        _filterState.value = _filterState.value.copy(batch = if (current == batch) "" else batch)
    }

    fun selectBloodGroup(bg: String) {
        val current = _filterState.value.bloodGroup
        _filterState.value = _filterState.value.copy(bloodGroup = if (current == bg) "" else bg)
    }

    fun selectUstad(ustadId: String) {
        val current = _filterState.value.ustadId
        _filterState.value = _filterState.value.copy(ustadId = if (current == ustadId) "" else ustadId)
    }

    fun resetFilters() {
        _filterState.value = AlbumFilterState()
    }

    // Flow 2: Staff direct student addition
    fun addStudentByStaff(
        name: String,
        fatherName: String,
        phone: String,
        whatsApp: String,
        dakhilaNumber: String,
        batchHijri: String,
        batchEng: String,
        supervisedUstadName: String,
        currentKhedmat: String,
        khedmatCategory: KhedmatCategory,
        bloodGroup: String,
        district: String,
        bio: String,
        photoUri: Uri?,
    ) {
        val madrasaId = session.value?.madrasaId ?: return
        viewModelScope.launch {
            _addStudentState.value = Resource.Loading
            val student = UserProfile(
                name = name.trim(),
                fatherName = fatherName.trim(),
                phone = phone.trim(),
                whatsApp = whatsApp.ifBlank { phone }.trim(),
                madrasaId = madrasaId,
                status = UserStatus.APPROVED,
                dakhilaNumber = dakhilaNumber.trim(),
                batchYearHijri = batchHijri.trim(),
                batchYearEng = batchEng.trim(),
                supervisedByUstadName = supervisedUstadName.trim(),
                currentKhedmat = currentKhedmat.trim(),
                khedmatCategory = khedmatCategory,
                bloodGroup = bloodGroup.trim(),
                district = district.trim(),
                bio = bio.trim(),
            )
            _addStudentState.value = alumniRepository.addStudentByStaff(student, photoUri)
        }
    }

    fun resetAddStudentState() {
        _addStudentState.value = null
    }
}
