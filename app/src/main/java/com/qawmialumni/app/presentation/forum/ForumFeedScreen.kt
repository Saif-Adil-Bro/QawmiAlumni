package com.qawmialumni.app.presentation.forum

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.ForumCategory
import com.qawmialumni.app.domain.model.ForumPost
import com.qawmialumni.app.presentation.components.VerifiedBadge
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import com.qawmialumni.app.presentation.theme.HeritageGold

@Composable
fun ForumFeedScreen(
    viewModel: ForumViewModel = hiltViewModel(),
) {
    val postsResource by viewModel.posts.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var showCreatePostDialog by remember { mutableStateOf(false) }
    var selectedPostForComments by remember { mutableStateOf<ForumPost?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { viewModel.selectCategory(null) },
                    label = { Text("সব পোস্ট") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = Color.White,
                    ),
                )
                ForumCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { viewModel.selectCategory(cat) },
                        label = { Text(cat.banglaName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
            }

            // Posts List
            when (val res = postsResource) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EmeraldPrimary)
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = res.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is Resource.Success -> {
                    if (res.data.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("এখনও কোনো ফোরাম পোস্ট নেই। নিচে '+' চাপ দিয়ে নতুন আলোচনা শুরু করুন।")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            items(res.data, key = { it.postId }) { post ->
                                ForumPostCard(
                                    post = post,
                                    onLikeClick = { viewModel.toggleLike(post.postId, post.likesCount) },
                                    onCommentClick = { selectedPostForComments = post },
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showCreatePostDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = EmeraldPrimary,
            contentColor = Color.White,
            shape = CircleShape,
        ) {
            Text("+", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (showCreatePostDialog) {
        CreatePostDialog(
            viewModel = viewModel,
            onDismiss = { showCreatePostDialog = false },
        )
    }

    selectedPostForComments?.let { post ->
        CommentDialog(
            post = post,
            viewModel = viewModel,
            onDismiss = { selectedPostForComments = null },
        )
    }
}

@Composable
fun ForumPostCard(
    post: ForumPost,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Author info row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (post.authorPhoto.isNotBlank()) {
                        AsyncImage(
                            model = post.authorPhoto,
                            contentDescription = post.authorName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text(post.authorName.take(1), fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        VerifiedBadge()
                    }
                    Text(
                        text = post.category.banglaName,
                        style = MaterialTheme.typography.bodySmall,
                        color = HeritageGold,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post content
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
            )

            if (post.imageUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = "Post Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions row (Like, Comment)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onLikeClick) {
                    Text("❤️ ${post.likesCount} পছন্দ", color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = onCommentClick) {
                    Text("💬 ${post.commentsCount} মন্তব্য", color = HeritageGold, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostDialog(
    viewModel: ForumViewModel,
    onDismiss: () -> Unit,
) {
    val createPostState by viewModel.createPostState.collectAsState()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf(ForumCategory.ILMI_MUZAKARA) }
    var catExpanded by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri },
    )

    LaunchedEffect(createPostState) {
        if (createPostState is Resource.Success) {
            viewModel.resetCreateState()
            onDismiss()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
            ) {
                Text(
                    text = "নতুন আলোচনা পোস্ট করুন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                )

                Spacer(modifier = Modifier.height(14.dp))

                ExposedDropdownMenuBox(
                    expanded = catExpanded,
                    onExpandedChange = { catExpanded = !catExpanded },
                ) {
                    OutlinedTextField(
                        value = selectedCat.banglaName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("ক্যাটাগরি") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(10.dp),
                    )
                    ExposedDropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false },
                    ) {
                        ForumCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.banglaName) },
                                onClick = {
                                    selectedCat = cat
                                    catExpanded = false
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("বিষয় / শিরোনাম *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("বিস্তারিত বক্তব্য লিখুন *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 8,
                    shape = RoundedCornerShape(10.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text("ছবি যুক্ত করুন (ঐচ্ছিক)", fontSize = 12.sp, color = EmeraldPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.createPost(title, selectedCat, content, imageUri)
                        },
                        enabled = createPostState !is Resource.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        if (createPostState is Resource.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("পোস্ট করুন")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentDialog(
    post: ForumPost,
    viewModel: ForumViewModel,
    onDismiss: () -> Unit,
) {
    val commentsResource by viewModel.getComments(post.postId).collectAsState()
    var commentText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                Text(
                    text = "মন্তব্যসমূহ: ${post.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(modifier = Modifier.weight(1f)) {
                    when (val res = commentsResource) {
                        is Resource.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = EmeraldPrimary)
                            }
                        }
                        is Resource.Error -> {
                            Text(text = res.message, color = MaterialTheme.colorScheme.error)
                        }
                        is Resource.Success -> {
                            if (res.data.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("এখনও কোনো মন্তব্য নেই। প্রথম মন্তব্য করুন!")
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    items(res.data, key = { it.commentId }) { comm ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = comm.authorName,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = EmeraldPrimary,
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = comm.text,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("মন্তব্য লিখুন...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            viewModel.addComment(post.postId, commentText)
                            commentText = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("পাঠান")
                    }
                }
            }
        }
    }
}
