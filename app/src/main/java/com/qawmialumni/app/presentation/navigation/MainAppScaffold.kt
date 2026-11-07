package com.qawmialumni.app.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qawmialumni.app.presentation.album.AlbumDirectoryScreen
import com.qawmialumni.app.presentation.album.AlbumViewModel
import com.qawmialumni.app.presentation.album.SelfProfileEditDialog
import com.qawmialumni.app.presentation.chat.ChatListScreen
import com.qawmialumni.app.presentation.forum.ForumFeedScreen
import com.qawmialumni.app.presentation.madrasaadmin.MadrasaAdminPanel
import com.qawmialumni.app.presentation.notice.NoticeBoardScreen
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import com.qawmialumni.app.presentation.theme.HeritageGold

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
    var showSelfProfileDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { showSelfProfileDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.16f),
                            contentColor = Color.White,
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("👤 প্রোফাইল", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = onSignOut,
                        colors = ButtonDefaults.buttonColors(containerColor = HeritageGold),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("লগআউট", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EmeraldPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("📖", fontSize = 20.sp) },
                    label = { Text("অ্যালবাম") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.15f),
                    ),
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("💬", fontSize = 20.sp) },
                    label = { Text("ফোরাম") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.15f),
                    ),
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Text("📢", fontSize = 20.sp) },
                    label = { Text("নোটিশ") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.15f),
                    ),
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Text("✉️", fontSize = 20.sp) },
                    label = { Text("চ্যাট") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.15f),
                    ),
                )
                if (showAdminTab) {
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = { Text("⚙️", fontSize = 20.sp) },
                        label = { Text("অ্যাডমিন") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldPrimary,
                            indicatorColor = EmeraldPrimary.copy(alpha = 0.15f),
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            when (selectedTab) {
                0 -> AlbumDirectoryScreen(showStaffTools = showStaffTools, viewModel = albumViewModel)
                1 -> ForumFeedScreen()
                2 -> NoticeBoardScreen()
                3 -> ChatListScreen()
                4 -> if (showAdminTab) MadrasaAdminPanel()
            }
        }
    }

    if (showSelfProfileDialog) {
        SelfProfileEditDialog(
            viewModel = albumViewModel,
            onDismiss = { showSelfProfileDialog = false },
        )
    }
}
