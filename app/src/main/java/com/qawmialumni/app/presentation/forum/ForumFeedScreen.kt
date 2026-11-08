package com.qawmialumni.app.presentation.forum

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.togetherWith
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.ForumCategory
import com.qawmialumni.app.domain.model.ForumPost
import com.qawmialumni.app.domain.model.UserRole
import com.qawmialumni.app.domain.model.VerificationStatus
import com.qawmialumni.app.presentation.components.AvatarWithRing
import com.qawmialumni.app.presentation.components.ModernEmptyState
import com.qawmialumni.app.presentation.components.QawmiIcons
import com.qawmialumni.app.presentation.components.VerifiedBadge
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.DisposableEffect
import com.qawmialumni.app.presentation.theme.HeritageGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumFeedScreen(
    viewModel: ForumViewModel = hiltViewModel(),
    onDetailActiveChange: (Boolean) -> Unit = {},
) {
    val postsResource by viewModel.posts.collectAsState()
    val isGlobalScope by viewModel.isGlobalScope.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val session by viewModel.session.collectAsState()

    var showCreatePostDialog by remember { mutableStateOf(false) }
    var selectedPostIdForDetail by remember { mutableStateOf<String?>(null) }
    var postToEdit by remember { mutableStateOf<ForumPost?>(null) }

    val currentUid = session?.uid.orEmpty()
    val isSuperAdmin = session?.role == UserRole.SUPER_ADMIN
    val isMadrasaModerator = session?.role == UserRole.MADRASA_ADMIN ||
        session?.role == UserRole.STAFF ||
        isSuperAdmin

    val currentPosts = (postsResource as? Resource.Success)?.data.orEmpty()
    val activeDetailPost = remember(selectedPostIdForDetail, currentPosts) {
        currentPosts.firstOrNull { it.postId == selectedPostIdForDetail }
    }

    LaunchedEffect(activeDetailPost) {
        onDetailActiveChange(activeDetailPost != null)
    }

    DisposableEffect(Unit) {
        onDispose {
            onDetailActiveChange(false)
        }
    }

    androidx.compose.animation.AnimatedContent(
        targetState = activeDetailPost,
        transitionSpec = {
            if (targetState != null) {
                // Opening detail screen: smooth slide in from right + fade in
                (androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(240)) +
                    androidx.compose.animation.slideInHorizontally(
                        animationSpec = androidx.compose.animation.core.tween(280, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                        initialOffsetX = { fullWidth -> fullWidth / 3 },
                    )) togetherWith
                    (androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(180)) +
                        androidx.compose.animation.slideOutHorizontally(
                            animationSpec = androidx.compose.animation.core.tween(240, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                            targetOffsetX = { fullWidth -> -fullWidth / 4 },
                        ))
            } else {
                // Going back to list: smooth slide in from left + fade in
                (androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(240)) +
                    androidx.compose.animation.slideInHorizontally(
                        animationSpec = androidx.compose.animation.core.tween(280, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                        initialOffsetX = { fullWidth -> -fullWidth / 3 },
                    )) togetherWith
                    (androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(180)) +
                        androidx.compose.animation.slideOutHorizontally(
                            animationSpec = androidx.compose.animation.core.tween(240, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                            targetOffsetX = { fullWidth -> fullWidth / 4 },
                        ))
            }
        },
        label = "ForumDetailTransition",
    ) { detailPost ->
        if (detailPost != null) {
            val canModerateThisPost = if (detailPost.isGlobal) {
                isSuperAdmin
            } else {
                isMadrasaModerator
            }
            val canEditOrDelete = canModerateThisPost || detailPost.authorUid == currentUid

            ForumPostDetailScreen(
                post = detailPost,
                isLikedByMe = currentUid.isNotBlank() && currentUid in detailPost.likedByUids,
                canPin = canModerateThisPost,
                canEditOrDelete = canEditOrDelete,
                viewModel = viewModel,
                onBack = { selectedPostIdForDetail = null },
                onLikeClick = { viewModel.toggleLike(detailPost) },
                onPinClick = { viewModel.togglePinPost(detailPost) },
                onEditClick = { postToEdit = detailPost },
                onDeleteClick = {
                    viewModel.deletePost(detailPost.postId)
                    selectedPostIdForDetail = null
                },
            )
        } else {
            // Summary List View (Madrasa Forum vs Global Forum)
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Scope Tabs: নিজ মাদরাসা vs গ্লোবাল ফোরাম
                    PrimaryTabRow(
                        selectedTabIndex = if (isGlobalScope) 1 else 0,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = EmeraldPrimary,
                    ) {
                        Tab(
                            selected = !isGlobalScope,
                            onClick = { viewModel.setGlobalScope(false) },
                            text = {
                                Text(
                                    "মাদরাসা ফোরাম",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                )
                            },
                        )
                        Tab(
                            selected = isGlobalScope,
                            onClick = { viewModel.setGlobalScope(true) },
                            text = {
                                Text(
                                    "গ্লোবাল ফোরাম",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                )
                            },
                        )
                    }

                // Category Filter Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { viewModel.selectCategory(null) },
                            label = { Text("সব আলোচনা", fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White,
                            ),
                        )
                        ForumCategory.entries.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { viewModel.selectCategory(cat) },
                                label = { Text(cat.banglaName, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.White,
                                ),
                            )
                        }
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

                // Compact Summary Cards List
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
                                ModernEmptyState(
                                    icon = QawmiIcons.ForumBubble,
                                    title = if (isGlobalScope) {
                                        "গ্লোবাল ফোরামে এখনও কোনো পোস্ট নেই"
                                    } else {
                                        "মাদরাসা ফোরামে এখনও কোনো পোস্ট নেই"
                                    },
                                    subtitle = if (isGlobalScope) {
                                        "সারা দেশের কওমি ফারেগীনদের সাথে ইলমি মুযাকারা বা খেদমতের বিজ্ঞপ্তি শেয়ার করতে নিচে 'নতুন পোস্ট' বাটনে চাপুন।"
                                    } else {
                                        "আপনার মাদরাসার সাথীদের সাথে আলোচনা শুরু করতে নিচে 'নতুন পোস্ট' বাটনে চাপুন।"
                                    },
                                    action = {
                                        Button(
                                            onClick = { showCreatePostDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                            shape = RoundedCornerShape(12.dp),
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("নতুন আলোচনা শুরু করুন", fontWeight = FontWeight.Bold)
                                        }
                                    },
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 88.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                items(res.data, key = { it.postId }) { post ->
                                    val isLikedByMe = currentUid.isNotBlank() && currentUid in post.likedByUids
                                    ForumSummaryCard(
                                        post = post,
                                        isLikedByMe = isLikedByMe,
                                        onClick = { selectedPostIdForDetail = post.postId },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            ExtendedFloatingActionButton(
                onClick = { showCreatePostDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(50),
                icon = {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "New Post")
                },
                text = {
                    Text("নতুন পোস্ট", fontWeight = FontWeight.Bold)
                },
            )
        }
    }
}

    if (showCreatePostDialog) {
        CreatePostDialog(
            viewModel = viewModel,
            defaultGlobal = isGlobalScope,
            onDismiss = { showCreatePostDialog = false },
        )
    }

    postToEdit?.let { editingPost ->
        EditPostDialog(
            post = editingPost,
            onSave = { newTitle, newContent, newCat, newPhone ->
                viewModel.updatePost(
                    postId = editingPost.postId,
                    title = newTitle,
                    content = newContent,
                    category = newCat,
                    contactPhone = newPhone,
                    onComplete = { postToEdit = null },
                )
            },
            onDismiss = { postToEdit = null },
        )
    }
}

/**
 * Compact Forum Summary Card for the feed list:
 * Displays category badge, pinned/pending status, title, 2-line preview snippet,
 * optional thumbnail on the right, and author + like/comment counters at the bottom.
 * Clicking opens [ForumPostDetailScreen].
 */
@Composable
fun ForumSummaryCard(
    post: ForumPost,
    isLikedByMe: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (post.isPinned) {
                    Modifier.border(1.5.dp, HeritageGold, RoundedCornerShape(16.dp))
                } else {
                    Modifier.border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                        RoundedCornerShape(16.dp),
                    )
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Badges Row (Category + Pinned / Pending / Global)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = EmeraldPrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text(
                            text = post.category.banglaName,
                            fontSize = 11.sp,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }

                    if (post.isGlobal) {
                        Surface(
                            color = HeritageGold.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(6.dp),
                        ) {
                            Text(
                                text = "গ্লোবাল",
                                fontSize = 10.sp,
                                color = HeritageGold,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (post.approvalStatus == VerificationStatus.PENDING) {
                        Surface(
                            color = HeritageGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50),
                        ) {
                            Text(
                                text = "অনুমোদনের অপেক্ষায়",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = HeritageGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                    }
                    if (post.isPinned) {
                        Surface(
                            color = HeritageGold.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(50),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = HeritageGold,
                                    modifier = Modifier.size(12.dp),
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "পিন করা",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HeritageGold,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Middle Row: Title + 2-line Summary Snippet + Optional Compact Thumbnail
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = post.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp,
                    )
                }

                if (post.imageUrl.isNotBlank()) {
                    Spacer(modifier = Modifier.width(12.dp))
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                RoundedCornerShape(10.dp),
                            ),
                        contentScale = ContentScale.Crop,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(8.dp))

            // Footer Row: Author + Like/Comment Counts + "বিস্তারিত পড়ুন"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarWithRing(
                        photoUrl = post.authorPhoto,
                        name = post.authorName,
                        size = 26.dp,
                        ringColor = HeritageGold,
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = post.authorName,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isLikedByMe) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${post.likesCount}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = QawmiIcons.ForumBubble,
                            contentDescription = null,
                            tint = HeritageGold,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${post.commentsCount}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Text(
                        text = "বিস্তারিত →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                    )
                }
            }
        }
    }
}

/**
 * FULL-PAGE Forum Post Detail Screen with Facebook / Instagram-style inline comments
 * and a sticky bottom comment composer bar.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ForumPostDetailScreen(
    post: ForumPost,
    isLikedByMe: Boolean,
    canPin: Boolean,
    canEditOrDelete: Boolean,
    viewModel: ForumViewModel,
    onBack: () -> Unit,
    onLikeClick: () -> Unit,
    onPinClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val commentsFlow = remember(post.postId) { viewModel.getComments(post.postId) }
    val commentsResource by commentsFlow.collectAsState()
    var commentText by remember { mutableStateOf("") }
    var showZoomImage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Top Navigation Bar for Detail Page
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = EmeraldPrimary,
                        )
                    }
                    Text(
                        text = if (post.isGlobal) "গ্লোবাল ফোরাম আলোচনা" else "ফোরাম আলোচনা",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (canPin) {
                        IconButton(onClick = onPinClick) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Pin",
                                tint = if (post.isPinned) HeritageGold else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (canEditOrDelete) {
                        IconButton(onClick = onEditClick) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = EmeraldPrimary,
                            )
                        }
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

        // Full Scrollable Post Body + Inline Comments Feed (Facebook / Instagram style)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .imeNestedScroll(),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            // Item 1: Full Post Article
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                    ) {
                        // Author Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AvatarWithRing(
                                photoUrl = post.authorPhoto,
                                name = post.authorName,
                                size = 48.dp,
                                ringColor = HeritageGold,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = post.authorName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    VerifiedBadge()
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        color = EmeraldPrimary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(6.dp),
                                    ) {
                                        Text(
                                            text = post.category.banglaName,
                                            fontSize = 11.sp,
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        )
                                    }
                                    if (post.isGlobal) {
                                        Surface(
                                            color = HeritageGold.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp),
                                        ) {
                                            Text(
                                                text = "গ্লোবাল ফোরাম",
                                                fontSize = 11.sp,
                                                color = HeritageGold,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Full Post Title
                        Text(
                            text = post.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                            fontSize = 20.sp,
                            lineHeight = 28.sp,
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Full Untruncated Post Content
                        Text(
                            text = post.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp,
                        )

                        if (post.contactPhone.isNotBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${post.contactPhone}"))
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "কল করুন: ${post.contactPhone}",
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        if (post.imageUrl.isNotBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            AsyncImage(
                                model = post.imageUrl,
                                contentDescription = post.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                        RoundedCornerShape(14.dp),
                                    )
                                    .clickable { showZoomImage = true },
                                contentScale = ContentScale.Crop,
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                        Spacer(modifier = Modifier.height(6.dp))

                        // Facebook-style Engagement Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(onClick = onLikeClick) {
                                Icon(
                                    imageVector = if (isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Like",
                                    tint = if (isLikedByMe) Color(0xFFDC2626) else EmeraldPrimary,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${post.likesCount} পছন্দ",
                                    color = if (isLikedByMe) Color(0xFFDC2626) else EmeraldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = QawmiIcons.ForumBubble,
                                    contentDescription = "Comments",
                                    tint = HeritageGold,
                                    modifier = Modifier.size(19.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${post.commentsCount} মন্তব্য",
                                    color = HeritageGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                )
                            }
                        }
                    }
                }
            }

            // Item 2: Comments Section Header
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "সকল মন্তব্য (${post.commentsCount})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }

            // Item 3+: Inline Comments List
            when (val res = commentsResource) {
                is Resource.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = EmeraldPrimary)
                        }
                    }
                }
                is Resource.Error -> {
                    item {
                        Text(
                            text = res.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
                is Resource.Success -> {
                    if (res.data.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "এখনও কোনো মন্তব্য নেই। নিচে প্রথম মন্তব্যটি লিখুন!",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    } else {
                        items(res.data, key = { it.commentId }) { comm ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                AvatarWithRing(
                                    photoUrl = comm.authorPhoto,
                                    name = comm.authorName,
                                    size = 36.dp,
                                    ringColor = HeritageGold,
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(
                                        topStart = 4.dp,
                                        topEnd = 16.dp,
                                        bottomStart = 16.dp,
                                        bottomEnd = 16.dp,
                                    ),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    ),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                        Text(
                                            text = comm.authorName,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary,
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = comm.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 20.sp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sticky Bottom Comment Composer Bar (Like Facebook / Instagram)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
        ) {
            Column {
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("আপনার সুচিন্তিত মন্তব্য লিখুন...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(50),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                viewModel.addComment(post.postId, commentText)
                                commentText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Comment",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }

    if (showZoomImage && post.imageUrl.isNotBlank()) {
        Dialog(
            onDismissRequest = { showZoomImage = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = post.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(440.dp),
                        contentScale = ContentScale.Fit,
                    )
                    IconButton(
                        onClick = { showZoomImage = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f)),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 100%-Width Create Post Dialog supporting both Madrasa Forum and Global Forum.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostDialog(
    viewModel: ForumViewModel,
    defaultGlobal: Boolean = false,
    onDismiss: () -> Unit,
) {
    val createPostState by viewModel.createPostState.collectAsState()

    var postToGlobal by remember { mutableStateOf(defaultGlobal) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "নতুন ফোরাম পোস্ট তৈরি করুন",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scope Selector: Madrasa vs Global
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = !postToGlobal,
                        onClick = { postToGlobal = false },
                        label = { Text("নিজ মাদরাসা ফোরাম", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    FilterChip(
                        selected = postToGlobal,
                        onClick = { postToGlobal = true },
                        label = { Text("গ্লোবাল কওমি ফোরাম", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HeritageGold,
                            selectedLabelColor = Color.White,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                }

                if (postToGlobal) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "নোট: গ্লোবাল ফোরামের পোস্ট সুপার অ্যাডমিন অনুমোদনের পর সকলের জন্য উন্মুক্ত হবে।",
                        fontSize = 11.sp,
                        color = HeritageGold,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

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
                        shape = RoundedCornerShape(12.dp),
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
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("বিস্তারিত বক্তব্য লিখুন *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    maxLines = 10,
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("যোগাযোগের ফোন নম্বর (ঐচ্ছিক)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                            )
                            Text(
                                "ছবি যুক্ত করুন (স্বয়ংক্রিয় <60KB WebP)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldPrimary,
                            )
                        }
                    }
                }

                if (createPostState is Resource.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (createPostState as Resource.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.createPost(
                                title = title,
                                category = selectedCat,
                                content = content,
                                contactPhone = contactPhone,
                                imageUri = imageUri,
                                postToGlobal = postToGlobal,
                            )
                        },
                        enabled = createPostState !is Resource.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        if (createPostState is Resource.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("প্রকাশ হচ্ছে...")
                        } else {
                            Text("পোস্ট জমা দিন", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 100%-Width Edit Post Dialog (usable by Author, Madrasa Moderator, or Super Admin).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostDialog(
    post: ForumPost,
    onSave: (title: String, content: String, category: ForumCategory, contactPhone: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var title by remember(post) { mutableStateOf(post.title) }
    var content by remember(post) { mutableStateOf(post.content) }
    var contactPhone by remember(post) { mutableStateOf(post.contactPhone) }
    var selectedCat by remember(post) { mutableStateOf(post.category) }
    var catExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
            ) {
                Text(
                    text = "পোস্ট সম্পাদনা (Edit Post)",
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
                        shape = RoundedCornerShape(12.dp),
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
                    label = { Text("শিরোনাম *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("বিস্তারিত বক্তব্য *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    maxLines = 10,
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("যোগাযোগের ফোন নম্বর") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(title, content, selectedCat, contactPhone) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("পরিবর্তন সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
