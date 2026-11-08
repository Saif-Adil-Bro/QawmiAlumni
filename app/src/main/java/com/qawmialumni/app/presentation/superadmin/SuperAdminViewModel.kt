package com.qawmialumni.app.presentation.superadmin

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qawmialumni.app.core.datastore.UserPreferencesDataStore
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.ForumCategory
import com.qawmialumni.app.domain.model.ForumPost
import com.qawmialumni.app.domain.model.Madrasa
import com.qawmialumni.app.domain.model.MadrasaUpdateRequest
import com.qawmialumni.app.domain.model.UserRole
import com.qawmialumni.app.domain.model.VerificationStatus
import com.qawmialumni.app.domain.repository.AdminRepository
import com.qawmialumni.app.domain.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CreateMadrasaUiState {
    data object Idle : CreateMadrasaUiState
    data object Loading : CreateMadrasaUiState
    data class Success(val madrasa: Madrasa) : CreateMadrasaUiState
    data class Error(val message: String) : CreateMadrasaUiState
}

@HiltViewModel
class SuperAdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val communityRepository: CommunityRepository,
    val dataStore: UserPreferencesDataStore,
    val notificationHelper: com.qawmialumni.app.core.notification.NotificationHelper,
) : ViewModel() {

    val madrasasList: StateFlow<Resource<List<Madrasa>>> = adminRepository.getAllMadrasas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Loading,
        )

    val pendingMadrasaUpdateRequests: StateFlow<Resource<List<MadrasaUpdateRequest>>> = adminRepository.getPendingMadrasaUpdateRequests()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Loading,
        )

    val globalForumPosts: StateFlow<Resource<List<ForumPost>>> = communityRepository.getAllGlobalPostsForAdmin()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Loading,
        )

    val allAuditLogs: StateFlow<Resource<List<com.qawmialumni.app.domain.model.AuditLog>>> = adminRepository.getAuditLogs("GLOBAL_ALL")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Loading,
        )

    val session = dataStore.userSession.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    private val _createState = MutableStateFlow<CreateMadrasaUiState>(CreateMadrasaUiState.Idle)
    val createState: StateFlow<CreateMadrasaUiState> = _createState.asStateFlow()

    private val _assignAdminState = MutableStateFlow<Resource<Unit>?>(null)
    val assignAdminState: StateFlow<Resource<Unit>?> = _assignAdminState.asStateFlow()

    private val _globalActionMessage = MutableStateFlow<String?>(null)
    val globalActionMessage: StateFlow<String?> = _globalActionMessage.asStateFlow()

    fun clearGlobalActionMessage() {
        _globalActionMessage.value = null
    }

    fun generateUniqueCode(name: String): String {
        val initials = name.split(" ")
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .take(4)
            .ifBlank { "MADRASA" }
        val randomSuffix = (1000..9999).random()
        return "$initials-$randomSuffix"
    }

    fun createMadrasa(
        name: String,
        code: String,
        address: String,
        establishedYear: String,
        adminEmail: String,
        logoUri: Uri?,
    ) {
        if (name.isBlank() || code.isBlank()) {
            _createState.value = CreateMadrasaUiState.Error("মাদরাসার নাম ও কোড আবশ্যক")
            return
        }

        viewModelScope.launch {
            _createState.value = CreateMadrasaUiState.Loading
            when (val res = adminRepository.createMadrasa(
                name = name,
                code = code,
                address = address,
                establishedYear = establishedYear,
                adminEmail = adminEmail,
                logoUri = logoUri,
            )) {
                is Resource.Success -> _createState.value = CreateMadrasaUiState.Success(res.data)
                is Resource.Error -> _createState.value = CreateMadrasaUiState.Error(res.message)
                Resource.Loading -> Unit
            }
        }
    }

    fun assignAdminToMadrasa(
        madrasaId: String,
        adminEmail: String,
    ) {
        assignMultipleAdminsToMadrasa(madrasaId, listOf(adminEmail))
    }

    fun assignMultipleAdminsToMadrasa(
        madrasaId: String,
        adminEmails: List<String>,
    ) {
        viewModelScope.launch {
            _assignAdminState.value = Resource.Loading
            _assignAdminState.value = adminRepository.assignMultipleMadrasaAdminEmails(madrasaId, adminEmails)
        }
    }

    fun removeAdminFromMadrasa(
        madrasaId: String,
        adminEmailOrUid: String,
        isUid: Boolean,
    ) {
        viewModelScope.launch {
            _assignAdminState.value = Resource.Loading
            _assignAdminState.value = adminRepository.removeMadrasaAdmin(madrasaId, adminEmailOrUid, isUid)
        }
    }

    // ------------------------------------------------------------------------
    // Global Forum Moderation & Full Authority (Approve, Manage, Edit, Delete)
    // ------------------------------------------------------------------------

    fun approveGlobalPost(postId: String) {
        viewModelScope.launch {
            when (val res = communityRepository.approveGlobalPost(postId)) {
                is Resource.Success -> _globalActionMessage.value = "গ্লোবাল ফোরাম পোস্ট অনুমোদিত হয়েছে"
                is Resource.Error -> _globalActionMessage.value = res.message
                Resource.Loading -> Unit
            }
        }
    }

    fun rejectOrDeleteGlobalPost(postId: String) {
        viewModelScope.launch {
            when (val res = communityRepository.deletePost(postId)) {
                is Resource.Success -> _globalActionMessage.value = "পোস্টটি মুছে ফেলা হয়েছে"
                is Resource.Error -> _globalActionMessage.value = res.message
                Resource.Loading -> Unit
            }
        }
    }

    fun togglePinGlobalPost(post: ForumPost) {
        viewModelScope.launch {
            communityRepository.togglePinPost(post.postId, !post.isPinned)
            _globalActionMessage.value = if (!post.isPinned) "পোস্টটি পিন করা হয়েছে" else "পিন সরানো হয়েছে"
        }
    }

    fun editGlobalPost(
        postId: String,
        title: String,
        content: String,
        category: ForumCategory,
        contactPhone: String,
        onDone: () -> Unit,
    ) {
        if (title.isBlank() || content.isBlank()) {
            _globalActionMessage.value = "শিরোনাম ও বিস্তারিত বক্তব্য খালি রাখা যাবে না"
            return
        }
        viewModelScope.launch {
            when (val res = communityRepository.updateForumPost(postId, title, content, category, contactPhone)) {
                is Resource.Success -> {
                    _globalActionMessage.value = "পোস্ট সফলভাবে সম্পাদনা (Edit) করা হয়েছে"
                    onDone()
                }
                is Resource.Error -> _globalActionMessage.value = res.message
                Resource.Loading -> Unit
            }
        }
    }

    fun createOfficialGlobalPost(
        title: String,
        category: ForumCategory,
        content: String,
        contactPhone: String,
        imageUri: Uri?,
        onDone: () -> Unit,
    ) {
        if (title.isBlank() || content.isBlank()) {
            _globalActionMessage.value = "শিরোনাম ও বিস্তারিত বক্তব্য লিখুন"
            return
        }
        viewModelScope.launch {
            val s = session.value
            val post = ForumPost(
                madrasaId = ForumPost.GLOBAL_FORUM_ID,
                authorUid = s?.uid.orEmpty().ifBlank { "SUPER_ADMIN" },
                authorName = s?.name.orEmpty().ifBlank { "কেন্দ্রীয় সুপার অ্যাডমিন" },
                authorPhoto = s?.photoUrl.orEmpty(),
                authorRole = UserRole.SUPER_ADMIN,
                category = category,
                title = title.trim(),
                content = content.trim(),
                contactPhone = contactPhone.trim(),
                approvalStatus = VerificationStatus.APPROVED,
            )
            when (val res = communityRepository.createPost(post, imageUri)) {
                is Resource.Success -> {
                    _globalActionMessage.value = "গ্লোবাল ফোরামে অফিশিয়াল পোস্ট প্রকাশিত হয়েছে"
                    onDone()
                }
                is Resource.Error -> _globalActionMessage.value = res.message
                Resource.Loading -> Unit
            }
        }
    }

    fun resetCreateState() {
        _createState.value = CreateMadrasaUiState.Idle
    }

    fun resetAssignAdminState() {
        _assignAdminState.value = null
    }

    fun approveMadrasaUpdateRequest(requestId: String) {
        viewModelScope.launch {
            when (val res = adminRepository.approveMadrasaUpdateRequest(requestId)) {
                is Resource.Success -> _globalActionMessage.value = "মাদরাসার তথ্য পরিবর্তনের আবেদন অনুমোদিত হয়েছে"
                is Resource.Error -> _globalActionMessage.value = res.message
                Resource.Loading -> Unit
            }
        }
    }

    fun rejectMadrasaUpdateRequest(requestId: String, note: String = "") {
        viewModelScope.launch {
            when (val res = adminRepository.rejectMadrasaUpdateRequest(requestId, note)) {
                is Resource.Success -> _globalActionMessage.value = "মাদরাসার তথ্য পরিবর্তনের আবেদন বাতিল করা হয়েছে"
                is Resource.Error -> _globalActionMessage.value = res.message
                Resource.Loading -> Unit
            }
        }
    }
}
