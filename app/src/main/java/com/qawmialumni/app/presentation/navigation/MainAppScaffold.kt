package com.qawmialumni.app.presentation.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qawmialumni.app.domain.model.UserRole
import com.qawmialumni.app.presentation.album.AlbumDirectoryScreen
import com.qawmialumni.app.presentation.album.AlbumViewModel
import com.qawmialumni.app.presentation.album.SelfProfileEditDialog
import com.qawmialumni.app.presentation.chat.ChatListScreen
import com.qawmialumni.app.presentation.components.QawmiIcons
import com.qawmialumni.app.presentation.forum.ForumFeedScreen
import com.qawmialumni.app.presentation.madrasaadmin.MadrasaAdminPanel
import com.qawmialumni.app.presentation.notice.NoticeBoardScreen
import com.qawmialumni.app.presentation.theme.EmeraldDeep
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import com.qawmialumni.app.presentation.theme.HeritageGold
import com.qawmialumni.app.presentation.theme.HeritageGoldLight

/**
 * MainAppScaffold: The central shell of the app adapting to the user's role.
 * Tabs:
 * 0: Digital Album (Directory)
 * 1: Community Forum (Ilmi Muzakara)
 * 2: Notice Board
 * 3: Messages / Chat
 * 4: Admin Tools (shown only if showAdminTab = true)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    showAdminTab: Boolean,
    showStaffTools: Boolean,
    onSignOut: () -> Unit,
    albumViewModel: AlbumViewModel = hiltViewModel(),
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var isChatRoomOpen by remember { mutableStateOf(false) }
    var isForumDetailOpen by remember { mutableStateOf(false) }
    var showSelfProfileDialog by remember { mutableStateOf(false) }
    var showNotificationSettingsDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val session by albumViewModel.session.collectAsState()

    val hideScaffoldChrome = (selectedTab == 3 && isChatRoomOpen) || (selectedTab == 1 && isForumDetailOpen)

    LaunchedEffect(Unit) {
        albumViewModel.notificationHelper.inAppNotifications.collect { inApp ->
            snackbarHostState.showSnackbar("${inApp.title}: ${inApp.message}")
        }
    }

    val roleLabel = when (session?.role) {
        UserRole.SUPER_ADMIN -> "সুপার অ্যাডমিন"
        UserRole.MADRASA_ADMIN -> "মাদরাসা অ্যাডমিন"
        UserRole.STAFF -> "আসাতিযায়ে কেরাম / স্টাফ"
        else -> "ভেরিফায়েড ফারেগ"
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (!hideScaffoldChrome) {
                Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .border(1.5.dp, HeritageGoldLight, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "ق",
                                    color = HeritageGoldLight,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = when (selectedTab) {
                                        0 -> "ডিজিটাল ফারেগীন অ্যালবাম"
                                        1 -> "ইলমি মুযাকারা ও ফোরাম"
                                        2 -> "দস্তারবন্দী ও নোটিশ বোর্ড"
                                        3 -> "মেসেজ ও যোগাযোগ"
                                        4 -> "মাদরাসা অ্যাডমিন প্যানেল"
                                        else -> "কওমি আলামনাই"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                                Text(
                                    text = roleLabel,
                                    fontSize = 11.sp,
                                    color = HeritageGoldLight,
                                    fontWeight = FontWeight.SemiBold,
                                )
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
                                modifier = Modifier.size(20.dp),
                                tint = HeritageGoldLight,
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { showSelfProfileDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.16f)),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "প্রোফাইল",
                                modifier = Modifier.size(20.dp),
                                tint = HeritageGoldLight,
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onSignOut,
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(HeritageGold.copy(alpha = 0.85f)),
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
                        titleContentColor = Color.White,
                    ),
                )
                HorizontalDivider(thickness = 1.5.dp, color = HeritageGold.copy(alpha = 0.45f))
            }
        }
        },
        bottomBar = {
            if (!hideScaffoldChrome) {
                Column {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    )
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp,
                    ) {
                        val navColors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldDeep,
                            selectedTextColor = EmeraldPrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = HeritageGold.copy(alpha = 0.22f),
                        )
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = {
                                Icon(
                                    imageVector = QawmiIcons.KitabBook,
                                    contentDescription = "অ্যালবাম",
                                    modifier = Modifier.size(22.dp),
                                )
                            },
                            label = {
                                Text(
                                    "অ্যালবাম",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                )
                            },
                            colors = navColors,
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = {
                                Icon(
                                    imageVector = QawmiIcons.ForumBubble,
                                    contentDescription = "ফোরাম",
                                    modifier = Modifier.size(22.dp),
                                )
                            },
                            label = {
                                Text(
                                    "ফোরাম",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                )
                            },
                            colors = navColors,
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = {
                                Icon(
                                    imageVector = QawmiIcons.NoticeCampaign,
                                    contentDescription = "নোটিশ",
                                    modifier = Modifier.size(22.dp),
                                )
                            },
                            label = {
                                Text(
                                    "নোটিশ",
                                    fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                                )
                            },
                            colors = navColors,
                        )
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            icon = {
                                Icon(
                                    imageVector = QawmiIcons.ChatMessage,
                                    contentDescription = "চ্যাট",
                                    modifier = Modifier.size(22.dp),
                                )
                            },
                            label = {
                                Text(
                                    "চ্যাট",
                                    fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Medium,
                                )
                            },
                            colors = navColors,
                        )
                        if (showAdminTab) {
                            NavigationBarItem(
                                selected = selectedTab == 4,
                                onClick = { selectedTab = 4 },
                                icon = {
                                    Icon(
                                        imageVector = QawmiIcons.AdminShield,
                                        contentDescription = "অ্যাডমিন",
                                        modifier = Modifier.size(22.dp),
                                    )
                                },
                                label = {
                                    Text(
                                        "অ্যাডমিন",
                                        fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Medium,
                                    )
                                },
                                colors = navColors,
                            )
                        }
                    }
                }
            }
        },
        contentWindowInsets = if (hideScaffoldChrome) WindowInsets(0, 0, 0, 0) else ScaffoldDefaults.contentWindowInsets,
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            androidx.compose.animation.AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val forward = targetState > initialState
                    (androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(240)) +
                        androidx.compose.animation.slideInHorizontally(
                            animationSpec = androidx.compose.animation.core.tween(280, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> if (forward) fullWidth / 4 else -fullWidth / 4 }
                        )) togetherWith
                        (androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(200)) +
                            androidx.compose.animation.slideOutHorizontally(
                                animationSpec = androidx.compose.animation.core.tween(240, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                targetOffsetX = { fullWidth -> if (forward) -fullWidth / 4 else fullWidth / 4 }
                            ))
                },
                label = "MainTabTransition",
            ) { tab ->
                when (tab) {
                    0 -> AlbumDirectoryScreen(showStaffTools = showStaffTools, viewModel = albumViewModel)
                    1 -> ForumFeedScreen(onDetailActiveChange = { isForumDetailOpen = it })
                    2 -> NoticeBoardScreen()
                    3 -> ChatListScreen(onRoomActiveChange = { isChatRoomOpen = it })
                    4 -> if (showAdminTab) MadrasaAdminPanel()
                }
            }
        }
    }

    if (showSelfProfileDialog) {
        SelfProfileEditDialog(
            viewModel = albumViewModel,
            onDismiss = { showSelfProfileDialog = false },
        )
    }

    if (showNotificationSettingsDialog) {
        com.qawmialumni.app.presentation.components.NotificationSettingsDialog(
            dataStore = albumViewModel.dataStore,
            notificationHelper = albumViewModel.notificationHelper,
            onDismiss = { showNotificationSettingsDialog = false },
        )
    }
}
