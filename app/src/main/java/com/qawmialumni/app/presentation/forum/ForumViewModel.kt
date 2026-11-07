package com.qawmialumni.app.presentation.forum

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qawmialumni.app.core.datastore.UserPreferencesDataStore
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.ForumCategory
import com.qawmialumni.app.domain.model.ForumPost
import com.qawmialumni.app.domain.model.PostComment
import com.qawmialumni.app.domain.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _selectedCategory = MutableStateFlow<ForumCategory?>(null)
    val selectedCategory: StateFlow<ForumCategory?> = _selectedCategory.asStateFlow()

    private val _createPostState = MutableStateFlow<Resource<Unit>?>(null)
    val createPostState: StateFlow<Resource<Unit>?> = _createPostState.asStateFlow()

    val posts: StateFlow<Resource<List<ForumPost>>> = dataStore.userSession
        .flatMapLatest { userSession ->
            if (userSession.madrasaId.isBlank()) {
                flowOf(Resource.Success(emptyList()))
            } else {
                _selectedCategory.flatMapLatest { cat ->
                    communityRepository.getForumPosts(userSession.madrasaId, cat)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Loading,
        )

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

    fun createPost(
        title: String,
        category: ForumCategory,
        content: String,
        contactPhone: String,
        imageUri: Uri?,
    ) {
        if (title.isBlank() || content.isBlank()) {
            _createPostState.value = Resource.Error("শিরোনাম ও বিস্তারিত বক্তব্য লিখুন")
            return
        }
        viewModelScope.launch {
            val s = session.value ?: return@launch
            _createPostState.value = Resource.Loading
            val post = ForumPost(
                madrasaId = s.madrasaId,
                authorUid = s.uid,
                authorName = s.name,
                authorPhoto = s.photoUrl,
                authorRole = s.role,
                category = category,
                title = title.trim(),
                content = content.trim(),
                contactPhone = contactPhone.trim(),
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
                authorName = s.name,
                authorPhoto = s.photoUrl,
                text = text.trim(),
            )
            communityRepository.addComment(postId, comment)
        }
    }
}
