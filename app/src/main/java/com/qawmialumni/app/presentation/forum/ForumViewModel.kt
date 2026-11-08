package com.qawmialumni.app.presentation.forum

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qawmialumni.app.core.datastore.UserPreferencesDataStore
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.ForumCategory
import com.qawmialumni.app.domain.model.ForumPost
import com.qawmialumni.app.domain.model.PostComment
import com.qawmialumni.app.domain.model.UserRole
import com.qawmialumni.app.domain.model.VerificationStatus
import com.qawmialumni.app.domain.repository.CommunityRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ForumViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    val dataStore: UserPreferencesDataStore,
) : ViewModel() {

    val session = dataStore.userSession.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    // false = নিজ মাদরাসা ফোরাম, true = গ্লোবাল কওমি ফোরাম
    private val _isGlobalScope = MutableStateFlow(false)
    val isGlobalScope: StateFlow<Boolean> = _isGlobalScope.asStateFlow()

    private val _selectedCategory = MutableStateFlow<ForumCategory?>(null)
    val selectedCategory: StateFlow<ForumCategory?> = _selectedCategory.asStateFlow()

    private val _createPostState = MutableStateFlow<Resource<Unit>?>(null)
    val createPostState: StateFlow<Resource<Unit>?> = _createPostState.asStateFlow()

    val posts: StateFlow<Resource<List<ForumPost>>> = combine(
        dataStore.userSession,
        _isGlobalScope,
        _selectedCategory,
    ) { userSession, isGlobal, cat ->
        Triple(userSession, isGlobal, cat)
    }.flatMapLatest { (userSession, isGlobal, cat) ->
        val targetForumId = if (isGlobal) ForumPost.GLOBAL_FORUM_ID else userSession.madrasaId
        if (targetForumId.isBlank()) {
            flowOf(Resource.Success(emptyList()))
        } else {
            communityRepository.getForumPosts(
                madrasaId = targetForumId,
                category = cat,
                includePendingForUid = userSession.uid,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Resource.Loading,
    )

    fun setGlobalScope(isGlobal: Boolean) {
        _isGlobalScope.value = isGlobal
    }

    fun selectCategory(cat: ForumCategory?) {
        _selectedCategory.value = cat
    }

    fun toggleLike(post: ForumPost) {
        val currentUid = session.value?.uid ?: return
        if (currentUid.isBlank()) return
        val isCurrentlyLiked = currentUid in post.likedByUids
        viewModelScope.launch {
            communityRepository.toggleLikePost(
                postId = post.postId,
                userId = currentUid,
                isCurrentlyLiked = isCurrentlyLiked,
            )
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            communityRepository.deletePost(postId)
        }
    }

    fun togglePinPost(post: ForumPost) {
        viewModelScope.launch {
            communityRepository.togglePinPost(post.postId, !post.isPinned)
        }
    }

    fun updatePost(
        postId: String,
        title: String,
        content: String,
        category: ForumCategory,
        contactPhone: String,
        onComplete: () -> Unit = {},
    ) {
        if (title.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            communityRepository.updateForumPost(
                postId = postId,
                title = title,
                content = content,
                category = category,
                contactPhone = contactPhone,
            )
            onComplete()
        }
    }

    fun createPost(
        title: String,
        category: ForumCategory,
        content: String,
        contactPhone: String,
        imageUri: Uri?,
        postToGlobal: Boolean = _isGlobalScope.value,
    ) {
        if (title.isBlank() || content.isBlank()) {
            _createPostState.value = Resource.Error("শিরোনাম ও বিস্তারিত বক্তব্য লিখুন")
            return
        }
        viewModelScope.launch {
            val s = session.value ?: return@launch
            _createPostState.value = Resource.Loading
            val targetForumId = if (postToGlobal) ForumPost.GLOBAL_FORUM_ID else s.madrasaId
            val initialApproval = if (postToGlobal && s.role != UserRole.SUPER_ADMIN) {
                VerificationStatus.PENDING
            } else {
                VerificationStatus.APPROVED
            }

            val post = ForumPost(
                madrasaId = targetForumId,
                authorUid = s.uid,
                authorName = s.name.ifBlank { "ফারেগ সদস্য" },
                authorPhoto = s.photoUrl,
                authorRole = s.role,
                category = category,
                title = title.trim(),
                content = content.trim(),
                contactPhone = contactPhone.trim(),
                approvalStatus = initialApproval,
            )
            _createPostState.value = communityRepository.createPost(post, imageUri)
        }
    }

    fun resetCreateState() {
        _createPostState.value = null
    }

    fun getComments(postId: String): StateFlow<Resource<List<PostComment>>> {
        return communityRepository.getComments(postId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Resource.Loading,
            )
    }

    fun addComment(postId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val s = session.value ?: return@launch
            val comment = PostComment(
                authorUid = s.uid,
                authorName = s.name.ifBlank { "সদস্য" },
                authorPhoto = s.photoUrl,
                text = text.trim(),
            )
            communityRepository.addComment(postId, comment)
        }
    }
}
