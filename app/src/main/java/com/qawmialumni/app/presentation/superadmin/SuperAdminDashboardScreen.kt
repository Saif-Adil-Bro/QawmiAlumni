package com.qawmialumni.app.presentation.superadmin

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import com.qawmialumni.app.domain.model.Madrasa
import com.qawmialumni.app.domain.model.VerificationStatus
import com.qawmialumni.app.presentation.components.AvatarWithRing
import com.qawmialumni.app.presentation.components.ModernEmptyState
import com.qawmialumni.app.presentation.components.QawmiIcons
import com.qawmialumni.app.presentation.components.StatSummaryCard
import com.qawmialumni.app.presentation.theme.EmeraldDeep
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import com.qawmialumni.app.presentation.theme.EmeraldTeal
import com.qawmialumni.app.presentation.theme.HeritageGold
import com.qawmialumni.app.presentation.theme.HeritageGoldLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminDashboardScreen(
    viewModel: SuperAdminViewModel = hiltViewModel(),
    onSignOut: () -> Unit,
) {
    val madrasasResource by viewModel.madrasasList.collectAsState()
    val globalPostsResource by viewModel.globalForumPosts.collectAsState()
    val auditLogsResource by viewModel.allAuditLogs.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showCreateOfficialGlobalPostDialog by remember { mutableStateOf(false) }
    var selectedMadrasaForAdminAssign by remember { mutableStateOf<Madrasa?>(null) }
    var selectedGlobalPostForEdit by remember { mutableStateOf<ForumPost?>(null) }
    var selectedGlobalPostForDetail by remember { mutableStateOf<ForumPost?>(null) }
    var showNotificationSettingsDialog by remember { mutableStateOf(false) }

    val pendingGlobalCount = (globalPostsResource as? Resource.Success)?.data
        ?.count { it.approvalStatus == VerificationStatus.PENDING } ?: 0

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = QawmiIcons.AdminShield,
                                    contentDescription = null,
                                    tint = HeritageGoldLight,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("সুপার অ্যাডমিন ড্যাশবোর্ড", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("মাদরাসা ও গ্লোবাল ফোরাম পূর্ণ নিয়ন্ত্রণ", fontSize = 11.sp, color = HeritageGoldLight)
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showNotificationSettingsDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.16f)),
                        ) {
                            Icon(
                                imageVector = QawmiIcons.NotificationBell,
                                contentDescription = "নোটিফিকেশন",
                                tint = HeritageGoldLight,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onSignOut,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(HeritageGold),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "লগআউট",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = EmeraldPrimary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
                HorizontalDivider(thickness = 1.5.dp, color = HeritageGold.copy(alpha = 0.45f))

                // 2-Tab Navigation: Madrasa Management vs Global Forum Full Authority
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = EmeraldPrimary,
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            selectedGlobalPostForDetail = null
                        },
                        text = {
                            Text(
                                text = "🏛️ মাদরাসা ম্যানেজমেন্ট",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                            )
                        },
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                        },
                        text = {
                            val badgeSuffix = if (pendingGlobalCount > 0) " ($pendingGlobalCount পেন্ডিং)" else ""
                            Text(
                                text = "🌍 গ্লোবাল ফোরাম$badgeSuffix",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (pendingGlobalCount > 0 && selectedTab != 1) HeritageGold else EmeraldPrimary,
                            )
                        },
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                            selectedGlobalPostForDetail = null
                        },
                        text = {
                            Text(
                                text = "📋 অডিট ও অ্যাক্টিভিটি",
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                            )
                        },
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = HeritageGold,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(50),
                    icon = {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Madrasa")
                    },
                    text = {
                        Text("নতুন মাদরাসা", fontWeight = FontWeight.Bold)
                    },
                )
            } else if (selectedTab == 1 && selectedGlobalPostForDetail == null) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateOfficialGlobalPostDialog = true },
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(50),
                    icon = {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Global Post")
                    },
                    text = {
                        Text("গ্লোবাল ঘোষণা", fontWeight = FontWeight.Bold)
                    },
                )
            }
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val forward = targetState > initialState
                    (androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(240)) +
                        androidx.compose.animation.slideInHorizontally(
                            animationSpec = androidx.compose.animation.core.tween(280, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> if (forward) fullWidth / 4 else -fullWidth / 4 }
                        )) togetherWith
                        (androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(180)) +
                            androidx.compose.animation.slideOutHorizontally(
                                animationSpec = androidx.compose.animation.core.tween(240, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                targetOffsetX = { fullWidth -> if (forward) -fullWidth / 4 else fullWidth / 4 }
                            ))
                },
                label = "SuperAdminTabTransition",
            ) { tab ->
                when (tab) {
                    0 -> {
                        MadrasaManagementTabContent(
                            madrasasResource = madrasasResource,
                            onAssignAdminClick = { selectedMadrasaForAdminAssign = it },
                        )
                    }
                    1 -> {
                        // Tab 1: Global Forum Authority (Approve, Manage, Edit, Pin, Delete)
                        AnimatedContent(
                            targetState = selectedGlobalPostForDetail,
                            transitionSpec = {
                                if (targetState != null) {
                                    (androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(240)) +
                                        androidx.compose.animation.slideInHorizontally(
                                            animationSpec = androidx.compose.animation.core.tween(280, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                            initialOffsetX = { fullWidth -> fullWidth / 3 }
                                        )) togetherWith
                                        (androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(180)) +
                                            androidx.compose.animation.slideOutHorizontally(
                                                animationSpec = androidx.compose.animation.core.tween(240, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                                targetOffsetX = { fullWidth -> -fullWidth / 4 }
                                            ))
                                } else {
                                    (androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(240)) +
                                        androidx.compose.animation.slideInHorizontally(
                                            animationSpec = androidx.compose.animation.core.tween(280, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                            initialOffsetX = { fullWidth -> -fullWidth / 3 }
                                        )) togetherWith
                                        (androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(180)) +
                                            androidx.compose.animation.slideOutHorizontally(
                                                animationSpec = androidx.compose.animation.core.tween(240, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                                targetOffsetX = { fullWidth -> fullWidth / 4 }
                                            ))
                                }
                            },
                            label = "SuperAdminGlobalPostDetailTransition",
                        ) { activeDetail ->
                            if (activeDetail != null) {
                                val livePost = (globalPostsResource as? Resource.Success)?.data
                                    ?.firstOrNull { it.postId == activeDetail.postId } ?: activeDetail

                                SuperAdminGlobalPostDetailView(
                                    post = livePost,
                                    onBack = { selectedGlobalPostForDetail = null },
                                    onApprove = { viewModel.approveGlobalPost(livePost.postId) },
                                    onRejectOrDelete = {
                                        viewModel.rejectOrDeleteGlobalPost(livePost.postId)
                                        selectedGlobalPostForDetail = null
                                    },
                                    onEdit = { selectedGlobalPostForEdit = livePost },
                                    onTogglePin = { viewModel.togglePinGlobalPost(livePost) },
                                )
                            } else {
                                GlobalForumAuthorityTabContent(
                                    globalPostsResource = globalPostsResource,
                                    onOpenDetail = { selectedGlobalPostForDetail = it },
                                    onApprove = { viewModel.approveGlobalPost(it.postId) },
                                    onRejectOrDelete = { viewModel.rejectOrDeleteGlobalPost(it.postId) },
                                    onEdit = { selectedGlobalPostForEdit = it },
                                    onTogglePin = { viewModel.togglePinGlobalPost(it) },
                                )
                            }
                        }
                    }
                    else -> {
                        // Tab 2: Activity & Audit Logs
                        com.qawmialumni.app.presentation.components.AuditLogsListSection(
                            auditLogsResource = auditLogsResource,
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateMadrasaDialog(
            viewModel = viewModel,
            onDismiss = { showCreateDialog = false },
        )
    }

    selectedMadrasaForAdminAssign?.let { madrasa ->
        AssignMadrasaAdminDialog(
            madrasa = madrasa,
            viewModel = viewModel,
            onDismiss = { selectedMadrasaForAdminAssign = null },
        )
    }

    selectedGlobalPostForEdit?.let { post ->
        SuperAdminEditGlobalPostDialog(
            post = post,
            viewModel = viewModel,
            onDismiss = { selectedGlobalPostForEdit = null },
        )
    }

    if (showCreateOfficialGlobalPostDialog) {
        SuperAdminCreateGlobalPostDialog(
            viewModel = viewModel,
            onDismiss = { showCreateOfficialGlobalPostDialog = false },
        )
    }

    if (showNotificationSettingsDialog) {
        com.qawmialumni.app.presentation.components.NotificationSettingsDialog(
            dataStore = viewModel.dataStore,
            notificationHelper = viewModel.notificationHelper,
            onDismiss = { showNotificationSettingsDialog = false },
        )
    }
}

@Composable
private fun MadrasaManagementTabContent(
    madrasasResource: Resource<List<Madrasa>>,
    onAssignAdminClick: (Madrasa) -> Unit,
) {
    when (madrasasResource) {
        is Resource.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EmeraldPrimary)
            }
        }
        is Resource.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = madrasasResource.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is Resource.Success -> {
            if (madrasasResource.data.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ModernEmptyState(
                        icon = QawmiIcons.AdminShield,
                        title = "এখনও কোনো মাদরাসা নিবন্ধন করা হয়নি",
                        subtitle = "নিচের 'নতুন মাদরাসা যুক্ত করুন' বাটনে চাপ দিয়ে প্রথম মাদরাসা ও তার অ্যাডমিন ইমেইল যুক্ত করুন।",
                    )
                }
            } else {
                val totalMadrasas = madrasasResource.data.size
                val totalAdminsAssigned = madrasasResource.data.count {
                    it.adminUids.isNotEmpty() || it.pendingAdminEmails.isNotEmpty()
                }
                val totalAlumni = madrasasResource.data.sumOf { it.totalAlumniCount }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            StatSummaryCard(
                                title = "নিবন্ধিত মাদরাসা",
                                value = "$totalMadrasas টি",
                                accentColor = EmeraldPrimary,
                                modifier = Modifier.weight(1f),
                            )
                            StatSummaryCard(
                                title = "অ্যাডমিনযুক্ত",
                                value = "$totalAdminsAssigned টি",
                                accentColor = HeritageGold,
                                modifier = Modifier.weight(1f),
                            )
                            StatSummaryCard(
                                title = "মোট ফারেগীন",
                                value = "$totalAlumni জন",
                                accentColor = EmeraldTeal,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    items(madrasasResource.data, key = { it.madrasaId }) { madrasa ->
                        MadrasaAdminCard(
                            madrasa = madrasa,
                            onAssignAdminClick = { onAssignAdminClick(madrasa) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlobalForumAuthorityTabContent(
    globalPostsResource: Resource<List<ForumPost>>,
    onOpenDetail: (ForumPost) -> Unit,
    onApprove: (ForumPost) -> Unit,
    onRejectOrDelete: (ForumPost) -> Unit,
    onEdit: (ForumPost) -> Unit,
    onTogglePin: (ForumPost) -> Unit,
) {
    var filterMode by remember { mutableIntStateOf(0) } // 0 = All, 1 = Pending, 2 = Approved

    when (globalPostsResource) {
        is Resource.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EmeraldPrimary)
            }
        }
        is Resource.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = globalPostsResource.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is Resource.Success -> {
            val allPosts = globalPostsResource.data
            val pendingCount = allPosts.count { it.approvalStatus == VerificationStatus.PENDING }
            val approvedCount = allPosts.count { it.approvalStatus == VerificationStatus.APPROVED }
            val pinnedCount = allPosts.count { it.isPinned }

            val filteredPosts = when (filterMode) {
                1 -> allPosts.filter { it.approvalStatus == VerificationStatus.PENDING }
                2 -> allPosts.filter { it.approvalStatus == VerificationStatus.APPROVED }
                else -> allPosts
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        StatSummaryCard(
                            title = "পেন্ডিং পোস্ট",
                            value = "$pendingCount টি",
                            accentColor = HeritageGold,
                            modifier = Modifier.weight(1f),
                        )
                        StatSummaryCard(
                            title = "অনুমোদিত পোস্ট",
                            value = "$approvedCount টি",
                            accentColor = EmeraldPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        StatSummaryCard(
                            title = "পিন করা পোস্ট",
                            value = "$pinnedCount টি",
                            accentColor = EmeraldTeal,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = filterMode == 0,
                            onClick = { filterMode = 0 },
                            label = { Text("সব পোস্ট (${allPosts.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White,
                            ),
                        )
                        FilterChip(
                            selected = filterMode == 1,
                            onClick = { filterMode = 1 },
                            label = { Text("অনুমোদনের অপেক্ষায় ($pendingCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HeritageGold,
                                selectedLabelColor = Color.White,
                            ),
                        )
                        FilterChip(
                            selected = filterMode == 2,
                            onClick = { filterMode = 2 },
                            label = { Text("অনুমোদিত ($approvedCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldTeal,
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                }

                if (filteredPosts.isEmpty()) {
                    item {
                        ModernEmptyState(
                            icon = QawmiIcons.ForumBubble,
                            title = "গ্লোবাল ফোরামে কোনো পোস্ট নেই",
                            subtitle = "সদস্যদের পাঠানো গ্লোবাল ফোরাম পোস্ট এখানে অনুমোদন (Approval), সম্পাদনা (Edit) ও পিন করার জন্য প্রদর্শিত হবে।",
                        )
                    }
                } else {
                    items(filteredPosts, key = { it.postId }) { post ->
                        SuperAdminGlobalForumPostCard(
                            post = post,
                            onClick = { onOpenDetail(post) },
                            onApprove = { onApprove(post) },
                            onRejectOrDelete = { onRejectOrDelete(post) },
                            onEdit = { onEdit(post) },
                            onTogglePin = { onTogglePin(post) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuperAdminGlobalForumPostCard(
    post: ForumPost,
    onClick: () -> Unit,
    onApprove: () -> Unit,
    onRejectOrDelete: () -> Unit,
    onEdit: () -> Unit,
    onTogglePin: () -> Unit,
) {
    val isPending = post.approvalStatus == VerificationStatus.PENDING

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isPending || post.isPinned) 1.5.dp else 1.dp,
            color = when {
                isPending -> HeritageGold
                post.isPinned -> EmeraldPrimary
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
        ) {
            // Top Status & Category Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Surface(
                        color = if (isPending) HeritageGold.copy(alpha = 0.18f) else EmeraldPrimary.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            text = if (isPending) "⏳ অনুমোদনের অপেক্ষায়" else "✓ প্রকাশিত",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPending) HeritageGold else EmeraldPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }

                    if (post.isPinned) {
                        Surface(
                            color = HeritageGold.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(50),
                        ) {
                            Text(
                                text = "📌 পিন করা",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HeritageGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }
                }

                Surface(
                    color = EmeraldTeal.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(50),
                ) {
                    Text(
                        text = post.category.banglaName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldTeal,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = post.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "লেখক: ${post.authorName} (${post.authorRole.banglaLabel})",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = EmeraldPrimary,
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            // Super Admin Authority Action Bar: Approve / Edit / Pin / Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isPending) {
                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp),
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("অনুমোদন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp), tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("এডিট", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                    }

                    OutlinedButton(
                        onClick = onTogglePin,
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(13.dp), tint = HeritageGold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (post.isPinned) "আনপিন" else "পিন",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HeritageGold,
                        )
                    }
                }

                IconButton(
                    onClick = onRejectOrDelete,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = if (isPending) Icons.Default.Close else Icons.Default.Delete,
                        contentDescription = "মুছুন",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

/**
 * Full-Page Global Post Detail View inside Super Admin Dashboard
 */
@Composable
private fun SuperAdminGlobalPostDetailView(
    post: ForumPost,
    onBack: () -> Unit,
    onApprove: () -> Unit,
    onRejectOrDelete: () -> Unit,
    onEdit: () -> Unit,
    onTogglePin: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val isPending = post.approvalStatus == VerificationStatus.PENDING

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = EmeraldDeep,
            tonalElevation = 3.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "ফিরে যান",
                        tint = Color.White,
                    )
                }
                Text(
                    text = "গ্লোবাল পোস্ট বিস্তারিত ও মডারেশন",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "এডিট", tint = HeritageGoldLight)
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = if (isPending) HeritageGold.copy(alpha = 0.18f) else EmeraldPrimary.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(50),
                ) {
                    Text(
                        text = if (isPending) "⏳ অনুমোদনের অপেক্ষায় (Pending)" else "✓ গ্লোবাল ফোরামে প্রকাশিত",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPending) HeritageGold else EmeraldPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
                Surface(
                    color = EmeraldTeal.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(50),
                ) {
                    Text(
                        text = post.category.banglaName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldTeal,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = post.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "লেখক: ${post.authorName} • ${post.authorRole.banglaLabel}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = EmeraldPrimary,
            )

            if (post.imageUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = post.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp,
            )

            if (post.contactPhone.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = EmeraldPrimary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(
                        text = "📞 যোগাযোগ নম্বর: ${post.contactPhone}",
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
        }

        // Bottom Super Admin Action Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 6.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (isPending) {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text("অনুমোদন", fontWeight = FontWeight.Bold)
                    }
                }
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text("সম্পাদনা", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                }
                OutlinedButton(
                    onClick = onTogglePin,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(if (post.isPinned) "আনপিন" else "পিন", fontWeight = FontWeight.Bold, color = HeritageGold)
                }
                OutlinedButton(
                    onClick = onRejectOrDelete,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("মুছুন", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminEditGlobalPostDialog(
    post: ForumPost,
    viewModel: SuperAdminViewModel,
    onDismiss: () -> Unit,
) {
    var title by remember { mutableStateOf(post.title) }
    var content by remember { mutableStateOf(post.content) }
    var contactPhone by remember { mutableStateOf(post.contactPhone) }
    var selectedCategory by remember { mutableStateOf(post.category) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
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
                    text = "গ্লোবাল ফোরাম পোস্ট সম্পাদনা (সুপার অ্যাডমিন)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                )
                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                ) {
                    OutlinedTextField(
                        value = selectedCategory.banglaName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("বিভাগ / ক্যাটাগরি") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(10.dp),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        ForumCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.banglaName) },
                                onClick = {
                                    selectedCategory = cat
                                    expanded = false
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
                    shape = RoundedCornerShape(10.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("বিস্তারিত বিবরণ *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    maxLines = 10,
                    shape = RoundedCornerShape(10.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("যোগাযোগের ফোন নম্বর (ঐচ্ছিক)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(10.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.editGlobalPost(
                                postId = post.postId,
                                title = title,
                                content = content,
                                category = selectedCategory,
                                contactPhone = contactPhone,
                            ) {
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank() && content.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text("আপডেট সংরক্ষণ করুন")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminCreateGlobalPostDialog(
    viewModel: SuperAdminViewModel,
    onDismiss: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ForumCategory.ILMI_MUZAKARA) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
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
                    text = "গ্লোবাল ফোরামে অফিশিয়াল পোস্ট প্রকাশ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                )
                Text(
                    text = "সুপার অ্যাডমিন হিসেবে এই পোস্টটি সরাসরি সব মাদরাসার গ্লোবাল ফোরামে পিন হয়ে প্রকাশিত হবে।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                ) {
                    OutlinedTextField(
                        value = selectedCategory.banglaName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("বিভাগ / ক্যাটাগরি") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(10.dp),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        ForumCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.banglaName) },
                                onClick = {
                                    selectedCategory = cat
                                    expanded = false
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
                    shape = RoundedCornerShape(10.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("বিস্তারিত বিবরণ *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    maxLines = 10,
                    shape = RoundedCornerShape(10.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("যোগাযোগের ফোন নম্বর (ঐচ্ছিক)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(10.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.createOfficialGlobalPost(
                                title = title,
                                category = selectedCategory,
                                content = content,
                                contactPhone = contactPhone,
                                imageUri = null,
                            ) {
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank() && content.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text("প্রকাশ করুন")
                    }
                }
            }
        }
    }
}

@Composable
fun MadrasaAdminCard(
    madrasa: Madrasa,
    onAssignAdminClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarWithRing(
                    photoUrl = madrasa.logoUrl,
                    name = madrasa.name,
                    size = 54.dp,
                    ringColor = HeritageGold,
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = madrasa.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                    )
                    if (madrasa.address.isNotBlank()) {
                        Text(
                            text = madrasa.address,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = HeritageGold.copy(alpha = 0.16f),
                        ) {
                            Text(
                                text = "কোড: ${madrasa.code}",
                                fontWeight = FontWeight.Bold,
                                color = HeritageGold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ফারেগীন: ${madrasa.totalAlumniCount} জন",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val adminInfo = when {
                    madrasa.pendingAdminEmails.isNotEmpty() ->
                        "অ্যাডমিন ইমেইল: ${madrasa.pendingAdminEmails.joinToString(", ")}"
                    madrasa.adminUids.isNotEmpty() ->
                        "সক্রিয় অ্যাডমিন: ${madrasa.adminUids.size} জন"
                    else -> "অ্যাডমিন এখনও নির্ধারণ করা হয়নি"
                }
                Text(
                    text = adminInfo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )

                OutlinedButton(
                    onClick = onAssignAdminClick,
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text("অ্যাডমিন নিয়োগ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                }
            }
        }
    }
}

@Composable
fun AssignMadrasaAdminDialog(
    madrasa: Madrasa,
    viewModel: SuperAdminViewModel,
    onDismiss: () -> Unit,
) {
    val assignState by viewModel.assignAdminState.collectAsState()
    var adminEmailsInput by remember { mutableStateOf("") }

    LaunchedEffect(assignState) {
        if (assignState is Resource.Success) {
            viewModel.resetAssignAdminState()
            adminEmailsInput = ""
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
                    text = "মাদরাসা অ্যাডমিন ব্যবস্থাপনা",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${madrasa.name} (${madrasa.code})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HeritageGold,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Section 1: Current Assigned Admins List with Removal
                Text(
                    text = "বর্তমান অ্যাডমিনদের তালিকা:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(6.dp))

                val allAdmins = (madrasa.pendingAdminEmails.map { Pair(it, false) } +
                    madrasa.adminUids.map { Pair(it, true) }).distinctBy { it.first }

                if (allAdmins.isEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "এখনও কোনো অ্যাডমিন নিয়োগ করা হয়নি। নিচে ইমেইল দিয়ে নিয়োগ করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        allAdmins.forEach { (identifier, isUid) ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = identifier,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = if (isUid) "সক্রিয় অ্যাকাউন্ট (UID)" else "নিযুক্ত ইমেইল (Pending)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isUid) EmeraldPrimary else HeritageGold,
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            viewModel.removeAdminFromMadrasa(
                                                madrasaId = madrasa.madrasaId,
                                                adminEmailOrUid = identifier,
                                                isUid = isUid,
                                            )
                                        },
                                        modifier = Modifier.size(28.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "অপসারণ",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                Spacer(modifier = Modifier.height(14.dp))

                // Section 2: Add New Admins (Support multiple comma-separated emails)
                Text(
                    text = "নতুন অ্যাডমিন নিয়োগ করুন (একাধিক সম্ভব):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = adminEmailsInput,
                    onValueChange = { adminEmailsInput = it },
                    label = { Text("অ্যাডমিনের ইমেইল *") },
                    placeholder = { Text("admin1@mail.com, admin2@mail.com") },
                    supportingText = {
                        Text("কমা (,) দিয়ে একসাথে একাধিক অ্যাডমিনের ইমেইল লিখতে পারেন")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                )

                if (assignState is Resource.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (assignState as Resource.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("সম্পন্ন")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val emails = adminEmailsInput
                                .split(",", ";", "\n", " ")
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                            if (emails.isNotEmpty()) {
                                viewModel.assignMultipleAdminsToMadrasa(madrasa.madrasaId, emails)
                            }
                        },
                        enabled = assignState !is Resource.Loading && adminEmailsInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        if (assignState is Resource.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Text("অ্যাডমিন যোগ করুন")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateMadrasaDialog(
    viewModel: SuperAdminViewModel,
    onDismiss: () -> Unit,
) {
    val createState by viewModel.createState.collectAsState()

    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var establishedYear by remember { mutableStateOf("") }
    var adminEmail by remember { mutableStateOf("") }
    var logoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> logoUri = uri },
    )

    LaunchedEffect(createState) {
        if (createState is CreateMadrasaUiState.Success) {
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
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "নতুন মাদরাসা নিবন্ধন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Logo picker
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (logoUri != null) {
                        AsyncImage(
                            model = logoUri,
                            contentDescription = "Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text("লোগো দিন", fontSize = 11.sp, color = EmeraldPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (code.isBlank()) {
                            code = viewModel.generateUniqueCode(it)
                        }
                    },
                    label = { Text("মাদরাসার নাম *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.uppercase() },
                        label = { Text("মাদরাসা কোড *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedButton(
                        onClick = { code = viewModel.generateUniqueCode(name) },
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text("জেনারেট", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("ঠিকানা / অবস্থান") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = establishedYear,
                    onValueChange = { establishedYear = it },
                    label = { Text("প্রতিষ্ঠা সাল (যেমন: ১৪০৫ হিজরি / ১৯৮৫ খ্রি.)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = adminEmail,
                    onValueChange = { adminEmail = it },
                    label = { Text("মাদরাসা অ্যাডমিনের ইমেইল") },
                    placeholder = { Text("admin@madrasa.org") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(10.dp),
                )

                if (createState is CreateMadrasaUiState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (createState as CreateMadrasaUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.createMadrasa(
                                name = name,
                                code = code,
                                address = address,
                                establishedYear = establishedYear,
                                adminEmail = adminEmail,
                                logoUri = logoUri,
                            )
                        },
                        enabled = createState !is CreateMadrasaUiState.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        if (createState is CreateMadrasaUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Text("সংরক্ষণ করুন")
                        }
                    }
                }
            }
        }
    }
}
