package com.qawmialumni.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.qawmialumni.app.core.datastore.UserPreferencesDataStore
import com.qawmialumni.app.core.notification.InAppNotification
import com.qawmialumni.app.core.notification.NotificationHelper
import com.qawmialumni.app.core.notification.NotificationType
import com.qawmialumni.app.presentation.theme.EmeraldDeep
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import com.qawmialumni.app.presentation.theme.HeritageGold
import com.qawmialumni.app.presentation.theme.HeritageGoldLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modern Notification Center Dialog / Screen.
 * Lists all incoming alerts & notices with unread indicators, category filters,
 * and a header Settings Icon (⚙️) that opens the NotificationSettingsDialog.
 */
@Composable
fun NotificationCenterDialog(
    dataStore: UserPreferencesDataStore,
    notificationHelper: NotificationHelper,
    onDismiss: () -> Unit,
    onNavigateToChat: ((roomId: String) -> Unit)? = null,
    onNavigateToForumPost: ((postId: String) -> Unit)? = null,
    onNavigateToNotice: ((noticeId: String) -> Unit)? = null,
) {
    val notifications by notificationHelper.notificationsList.collectAsState()
    var selectedFilter by remember { mutableStateOf<NotificationType?>(null) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val unreadCount = remember(notifications) { notifications.count { !it.isRead } }

    val filteredList = remember(notifications, selectedFilter) {
        if (selectedFilter == null) {
            notifications
        } else {
            notifications.filter { it.type == selectedFilter }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 12.dp, vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Banner
                Surface(
                    color = EmeraldDeep,
                    tonalElevation = 4.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.14f))
                                .border(1.5.dp, HeritageGoldLight, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = QawmiIcons.NotificationBell,
                                contentDescription = null,
                                tint = HeritageGoldLight,
                                modifier = Modifier.size(22.dp),
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "নোটিফিকেশন সেন্টার",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            Text(
                                text = if (unreadCount > 0) "মোট ${notifications.size}টি বার্তা • ${unreadCount}টি নতুন" else "সকল বার্তা পঠিত (${notifications.size}টি)",
                                fontSize = 11.sp,
                                color = HeritageGoldLight,
                            )
                        }

                        // Settings Icon (⚙️) Button as requested by user
                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.16f)),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "নোটিফিকেশন সেটিংস",
                                tint = HeritageGoldLight,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Close Dialog Button (✕)
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.16f)),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "বন্ধ করুন",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Filter Chips & Quick Actions Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilterChip(
                            selected = selectedFilter == null,
                            onClick = { selectedFilter = null },
                            label = { Text("সকল (${notifications.size})", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White,
                            ),
                        )
                        FilterChip(
                            selected = selectedFilter == NotificationType.CHAT,
                            onClick = { selectedFilter = NotificationType.CHAT },
                            label = { Text("বার্তা", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White,
                            ),
                        )
                        FilterChip(
                            selected = selectedFilter == NotificationType.NOTICE,
                            onClick = { selectedFilter = NotificationType.NOTICE },
                            label = { Text("নোটিশ", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White,
                            ),
                        )
                        FilterChip(
                            selected = selectedFilter == NotificationType.FORUM,
                            onClick = { selectedFilter = NotificationType.FORUM },
                            label = { Text("ফোরাম", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }

                    if (notifications.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(
                                onClick = { notificationHelper.markAllAsRead() },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("সব পঠিত করুন", fontSize = 12.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                            }

                            TextButton(
                                onClick = { notificationHelper.clearAll() },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("সব মুছুন", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                HorizontalDivider(thickness = 0.8.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                // Notification Items List or Empty State
                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = QawmiIcons.NotificationBell,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(32.dp),
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "কোনো নোটিফিকেশন নেই",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "আপনার মাদরাসার নতুন নোটিশ, বার্তা বা ফোরাম পোস্ট এলে সাথে সাথে এখানে পাওয়া যাবে।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 24.dp),
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            OutlinedButton(
                                onClick = { notificationHelper.sendTestNotification() },
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Icon(
                                    imageVector = QawmiIcons.NotificationBell,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("টেস্ট নোটিফিকেশন পাঠান")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(filteredList, key = { it.id }) { item ->
                            NotificationCardItem(
                                notification = item,
                                onClick = {
                                    notificationHelper.markAsRead(item.id)
                                    onDismiss()
                                    when (item.type) {
                                        NotificationType.CHAT -> onNavigateToChat?.invoke(item.targetId)
                                        NotificationType.FORUM -> onNavigateToForumPost?.invoke(item.targetId)
                                        NotificationType.NOTICE -> onNavigateToNotice?.invoke(item.targetId)
                                        NotificationType.ADMIN -> {}
                                    }
                                },
                                onDelete = { notificationHelper.deleteNotification(item.id) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        NotificationSettingsDialog(
            dataStore = dataStore,
            notificationHelper = notificationHelper,
            onDismiss = { showSettingsDialog = false },
        )
    }
}

@Composable
private fun NotificationCardItem(
    notification: InAppNotification,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val (icon, tint, bg) = when (notification.type) {
        NotificationType.CHAT -> Triple(
            QawmiIcons.ChatMessage,
            EmeraldPrimary,
            EmeraldPrimary.copy(alpha = 0.12f),
        )
        NotificationType.NOTICE -> Triple(
            QawmiIcons.NoticeCampaign,
            HeritageGold,
            HeritageGold.copy(alpha = 0.16f),
        )
        NotificationType.FORUM -> Triple(
            QawmiIcons.ForumBubble,
            Color(0xFF2563EB),
            Color(0xFF2563EB).copy(alpha = 0.12f),
        )
        NotificationType.ADMIN -> Triple(
            QawmiIcons.AdminShield,
            EmeraldDeep,
            EmeraldDeep.copy(alpha = 0.12f),
        )
    }

    val typeLabel = when (notification.type) {
        NotificationType.CHAT -> "বার্তা"
        NotificationType.NOTICE -> "নোটিশ"
        NotificationType.FORUM -> "ফোরাম"
        NotificationType.ADMIN -> "অ্যাডমিন"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = if (notification.isRead) MaterialTheme.colorScheme.surface else EmeraldPrimary.copy(alpha = 0.05f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (notification.isRead) 0.8.dp else 1.2.dp,
            color = if (notification.isRead) MaterialTheme.colorScheme.outline.copy(alpha = 0.25f) else EmeraldPrimary.copy(alpha = 0.4f),
        ),
        tonalElevation = if (notification.isRead) 1.dp else 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(bg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = tint.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp),
                        ) {
                            Text(
                                text = typeLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = tint,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }

                        if (!notification.isRead) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(HeritageGold),
                            )
                        }
                    }

                    Text(
                        text = formatRelativeTime(notification.timestamp),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (notification.isRead) FontWeight.SemiBold else FontWeight.Bold,
                    color = if (notification.isRead) MaterialTheme.colorScheme.onSurface else EmeraldPrimary,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "মুছুন",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    if (timestamp <= 0L) return "পূর্বে"
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "এইমাত্র"
        minutes < 60 -> "$minutes মিনিট আগে"
        hours < 24 -> "$hours ঘণ্টা আগে"
        days < 7 -> "$days দিন আগে"
        else -> {
            val sdf = SimpleDateFormat("dd MMM, yyyy", Locale("bn", "BD"))
            runCatching { sdf.format(Date(timestamp)) }.getOrDefault("পূর্বে")
        }
    }
}
