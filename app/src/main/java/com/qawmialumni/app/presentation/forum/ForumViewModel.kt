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

    private val _selectedCategory = MutableStateFlow<ForumCategory?>(null)
    val selectedCategory: StateFlow<ForumCategory?> = _selectedCategory.asStateFlow()

    private val _createPostState = MutableStateFlow<Resource<Unit>?>(null)
    val createPostState: StateFlow<Resource<Unit>?> = _createPostState.asStateFlow()

    val posts: StateFlow<Resource<List<ForumPost>>> = dataStore.userSession
        .flatMapLatest { session ->
            if (session.madrasaId.isBlank()) {
                flowOf(Resource.Success(emptyList()))
            } else {
                _selectedCategory.flatMapLatest { cat ->
                    communityRepository.getForumPosts(session.madrasaId, cat)
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

    fun toggleLike(postId: String, currentLikes: Int) {
        viewModelScope.launch {
            communityRepository.toggleLikePost(postId, currentLikes)
        }
    }

    fun createPost(
        title: String,
        category: ForumCategory,
        content: String,
        imageUri: Uri?,
    ) {
        viewModelScope.launch {
            val session = dataStore.userSession.stateIn(viewModelScope).value
            _createPostState.value = Resource.Loading
            val post = ForumPost(
                madrasaId = session.madrasaId,
                authorUid = session.uid,
                authorName = session.name,
                authorPhoto = session.photoUrl,
                authorRole = session.role,
                category = category,
                title = title.trim(),
                content = content.trim(),
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
            val session = dataStore.userSession.stateIn(viewModelScope).value
            val comment = PostComment(
                authorUid = session.uid,
                authorName = session.name,
                authorPhoto = session.photoUrl,
                text = text.trim(),
            )
            communityRepository.addComment(postId, comment)
        }
    }
}
