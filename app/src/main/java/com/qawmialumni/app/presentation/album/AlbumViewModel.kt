package com.qawmialumni.app.presentation.album

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qawmialumni.app.core.datastore.UserPreferencesDataStore
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.ChatRoom
import com.qawmialumni.app.domain.model.GraduationDepartment
import com.qawmialumni.app.domain.model.KhedmatCategory
import com.qawmialumni.app.domain.model.Madrasa
import com.qawmialumni.app.domain.model.MadrasaUpdateRequest
import com.qawmialumni.app.domain.model.UserProfile
import com.qawmialumni.app.domain.model.UserRole
import com.qawmialumni.app.domain.model.UserStatus
import com.qawmialumni.app.domain.repository.AdminRepository
import com.qawmialumni.app.domain.repository.AlumniRepository
import com.qawmialumni.app.domain.repository.AuthRepository
import com.qawmialumni.app.domain.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlbumFilterState(
    val batch: String = "",
    val ustadName: String = "",
    val bloodGroup: String = "",
    val department: String = "",
    val khedmatCategory: String = "",
    val district: String = "",
    val query: String = "",
) {
    val hasActiveFilters: Boolean
        get() = batch.isNotBlank() ||
            ustadName.isNotBlank() ||
            bloodGroup.isNotBlank() ||
            department.isNotBlank() ||
            khedmatCategory.isNotBlank() ||
            district.isNotBlank() ||
            query.isNotBlank()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val alumniRepository: AlumniRepository,
    private val adminRepository: AdminRepository,
    private val authRepository: AuthRepository,
    private val communityRepository: CommunityRepository,
    val dataStore: UserPreferencesDataStore,
    val notificationHelper: com.qawmialumni.app.core.notification.NotificationHelper,
) : ViewModel() {

    val session = dataStore.userSession.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    val currentMadrasa: StateFlow<Madrasa?> = dataStore.userSession
        .flatMapLatest { userSession ->
            if (userSession.madrasaId.isBlank()) {
                flowOf(null)
            } else {
                adminRepository.getAllMadrasas().map { res ->
                    (res as? Resource.Success)?.data?.firstOrNull { it.madrasaId == userSession.madrasaId }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    private val _filterState = MutableStateFlow(AlbumFilterState())
    val filterState: StateFlow<AlbumFilterState> = _filterState.asStateFlow()

    private val _syncState = MutableStateFlow<Resource<Int>>(Resource.Loading)
    val syncState: StateFlow<Resource<Int>> = _syncState.asStateFlow()

    private val _addStudentState = MutableStateFlow<Resource<Unit>?>(null)
    val addStudentState: StateFlow<Resource<Unit>?> = _addStudentState.asStateFlow()

    private val _selfProfileState = MutableStateFlow<Resource<UserProfile>?>(null)
    val selfProfileState: StateFlow<Resource<UserProfile>?> = _selfProfileState.asStateFlow()

    private val _currentSelfProfile = MutableStateFlow<UserProfile?>(null)
    val currentSelfProfile: StateFlow<UserProfile?> = _currentSelfProfile.asStateFlow()

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
                        ustadName = filter.ustadName,
                        bloodGroup = filter.bloodGroup,
                        department = filter.department,
                        khedmatCategory = filter.khedmatCategory,
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

    val ustadNames: StateFlow<List<String>> = dataStore.userSession
        .flatMapLatest { userSession ->
            if (userSession.madrasaId.isBlank()) flowOf(emptyList())
            else alumniRepository.getUstadNames(userSession.madrasaId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
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

    fun selectDepartment(deptWire: String) {
        val current = _filterState.value.department
        _filterState.value = _filterState.value.copy(department = if (current == deptWire) "" else deptWire)
    }

    fun selectKhedmatCategory(catWire: String) {
        val current = _filterState.value.khedmatCategory
        _filterState.value = _filterState.value.copy(khedmatCategory = if (current == catWire) "" else catWire)
    }

    fun selectUstadName(ustadName: String) {
        val current = _filterState.value.ustadName
        _filterState.value = _filterState.value.copy(ustadName = if (current == ustadName) "" else ustadName)
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
        department: GraduationDepartment,
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
        if (name.isBlank()) {
            _addStudentState.value = Resource.Error("অনুগ্রহ করে ছাত্রের নাম লিখুন")
            return
        }
        viewModelScope.launch {
            _addStudentState.value = Resource.Loading
            val madrasaId = session.value?.madrasaId.orEmpty()
            val student = UserProfile(
                name = name.trim(),
                fatherName = fatherName.trim(),
                phone = phone.trim(),
                whatsApp = whatsApp.ifBlank { phone }.trim(),
                madrasaId = madrasaId,
                status = UserStatus.APPROVED,
                dakhilaNumber = dakhilaNumber.trim(),
                department = department,
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

    // Role promotion/demotion by Madrasa Admin (Batch Representative / Staff)
    fun toggleStaffRole(alumni: UserProfile, onDone: () -> Unit) {
        val newRole = if (alumni.role == UserRole.STAFF) UserRole.ALUMNI else UserRole.STAFF
        val madrasaId = alumni.madrasaId.ifBlank { session.value?.madrasaId.orEmpty() }
        viewModelScope.launch {
            adminRepository.updateMemberRole(alumni.uid, madrasaId, newRole)
            if (madrasaId.isNotBlank()) {
                alumniRepository.syncAlumniFromRemote(madrasaId)
            }
            onDone()
        }
    }

    // Direct In-App Chat message from Alumni Detail Sheet
    fun sendDirectMessageToAlumni(
        targetAlumni: UserProfile,
        messageText: String,
        onSent: () -> Unit,
    ) {
        val currentSession = session.value ?: return
        if (currentSession.uid.isBlank() || messageText.isBlank()) return
        val roomId = ChatRoom.buildRoomId(currentSession.uid, targetAlumni.uid)
        viewModelScope.launch {
            communityRepository.sendMessage(
                roomId = roomId,
                madrasaId = currentSession.madrasaId,
                senderId = currentSession.uid,
                recipientId = targetAlumni.uid,
                senderName = currentSession.name,
                recipientName = targetAlumni.name,
                senderPhoto = currentSession.photoUrl,
                recipientPhoto = targetAlumni.photoUrl,
                text = messageText.trim(),
                messageId = java.util.UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
            )
            onSent()
        }
    }

    // Self Profile Load & Update
    fun loadSelfProfile() {
        val uid = session.value?.uid ?: return
        if (uid.isBlank()) return
        viewModelScope.launch {
            when (val res = authRepository.fetchUserProfile(uid)) {
                is Resource.Success -> _currentSelfProfile.value = res.data
                else -> Unit
            }
        }
    }

    fun updateSelfProfile(
        updatedProfile: UserProfile,
        newPhotoUri: Uri?,
    ) {
        viewModelScope.launch {
            _selfProfileState.value = Resource.Loading
            val res = alumniRepository.updateSelfProfile(updatedProfile, newPhotoUri)
            _selfProfileState.value = res
            if (res is Resource.Success) {
                _currentSelfProfile.value = res.data
            }
        }
    }

    fun resetSelfProfileState() {
        _selfProfileState.value = null
    }

    val pendingMadrasaUpdateRequest: StateFlow<MadrasaUpdateRequest?> = dataStore.userSession
        .flatMapLatest { userSession ->
            if (userSession.madrasaId.isBlank()) {
                flowOf(null)
            } else {
                adminRepository.getPendingMadrasaUpdateRequests(userSession.madrasaId).map { res ->
                    (res as? Resource.Success)?.data?.firstOrNull()
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    private val _madrasaUpdateRequestState = MutableStateFlow<Resource<MadrasaUpdateRequest>?>(null)
    val madrasaUpdateRequestState: StateFlow<Resource<MadrasaUpdateRequest>?> = _madrasaUpdateRequestState.asStateFlow()

    fun submitMadrasaUpdateRequest(
        name: String,
        address: String,
        establishedYear: String,
        contactEmail: String,
        newLogoUri: Uri?,
    ) {
        val current = currentMadrasa.value ?: return
        val currentSession = session.value ?: return
        viewModelScope.launch {
            _madrasaUpdateRequestState.value = Resource.Loading
            val request = MadrasaUpdateRequest(
                madrasaId = current.madrasaId,
                madrasaCode = current.code,
                currentName = current.name,
                requestedName = name.trim(),
                currentLogoUrl = current.logoUrl,
                requestedLogoUrl = current.logoUrl,
                currentAddress = current.address,
                requestedAddress = address.trim(),
                currentEstablishedYear = current.establishedYear,
                requestedEstablishedYear = establishedYear.trim(),
                requestedAdminEmail = contactEmail.trim().lowercase(),
                requestedByUid = currentSession.uid,
                requestedByName = currentSession.name,
            )
            val res = adminRepository.submitMadrasaUpdateRequest(request, newLogoUri)
            _madrasaUpdateRequestState.value = res
        }
    }

    fun resetMadrasaUpdateRequestState() {
        _madrasaUpdateRequestState.value = null
    }
}
