package com.qawmialumni.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.qawmialumni.app.core.datastore.NotificationSettings
import com.qawmialumni.app.core.datastore.UserPreferencesDataStore
import com.qawmialumni.app.core.notification.NotificationHelper
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import com.qawmialumni.app.presentation.theme.HeritageGold
import com.qawmialumni.app.presentation.theme.HeritageGoldLight
import kotlinx.coroutines.launch

@Composable
fun NotificationSettingsDialog(
    dataStore: UserPreferencesDataStore,
    notificationHelper: NotificationHelper,
    onDismiss: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val settingsFlow = remember { dataStore.notificationSettings }
    val settings by settingsFlow.collectAsState(initial = NotificationSettings())

    var testSentMessage by remember { mutableStateOf<String?>(null) }

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
                .padding(horizontal = 16.dp, vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                // Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldPrimary)
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.16f))
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
                            Column {
                                Text(
                                    text = "নোটিফিকেশন কাস্টমাইজ",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                                Text(
                                    text = "প্রয়োজনীয় আপডেট নিয়ন্ত্রণ করুন",
                                    fontSize = 12.sp,
                                    color = HeritageGoldLight,
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.16f)),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "বন্ধ",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "আপনার পছন্দমতো নোটিফিকেশন অন বা অফ রাখুন:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    // 1. Chat Messages Toggle
                    NotificationToggleCard(
                        icon = QawmiIcons.ChatMessage,
                        title = "মেসেজ নোটিফিকেশন",
                        subtitle = "সহপাঠী ও ফারেগীনদের নতুন বার্তা আসলে সাথে সাথে অবগত করবে।",
                        isChecked = settings.chatNotificationsEnabled,
                        onCheckedChange = { checked ->
                            coroutineScope.launch {
                                dataStore.updateNotificationSettings(
                                    chatEnabled = checked,
                                    noticeEnabled = settings.noticeNotificationsEnabled,
                                    forumEnabled = settings.forumNotificationsEnabled,
                                )
                            }
                        },
                    )

                    // 2. Notices Toggle
                    NotificationToggleCard(
                        icon = QawmiIcons.NoticeCampaign,
                        title = "নোটিশ ও ঘোষণা",
                        subtitle = "দস্তারবন্দী মহাসম্মেলন ও মাদরাসার অফিশিয়াল নোটিশের তাৎক্ষণিক বিজ্ঞপ্তি।",
                        isChecked = settings.noticeNotificationsEnabled,
                        onCheckedChange = { checked ->
                            coroutineScope.launch {
                                dataStore.updateNotificationSettings(
                                    chatEnabled = settings.chatNotificationsEnabled,
                                    noticeEnabled = checked,
                                    forumEnabled = settings.forumNotificationsEnabled,
                                )
                            }
                        },
                    )

                    // 3. Forum Posts Toggle
                    NotificationToggleCard(
                        icon = QawmiIcons.ForumBubble,
                        title = "ফোরাম আলোচনা ও মুযাকারা",
                        subtitle = "প্রতিষ্ঠানে বা গ্লোবাল ফোরামে নতুন পোস্ট ও ইলমি আলোচনা এলে নোটিফিকেশন পাবেন।",
                        isChecked = settings.forumNotificationsEnabled,
                        onCheckedChange = { checked ->
                            coroutineScope.launch {
                                dataStore.updateNotificationSettings(
                                    chatEnabled = settings.chatNotificationsEnabled,
                                    noticeEnabled = settings.noticeNotificationsEnabled,
                                    forumEnabled = checked,
                                )
                            }
                        },
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Test Notification Button
                    OutlinedButton(
                        onClick = {
                            notificationHelper.sendTestNotification()
                            testSentMessage = "একটি টেস্ট নোটিফিকেশন পাঠানো হয়েছে!"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            imageVector = QawmiIcons.NotificationBell,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = EmeraldPrimary,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "নোটিফিকেশন টেস্ট করুন",
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                        )
                    }

                    if (testSentMessage != null) {
                        Text(
                            text = testSentMessage.orEmpty(),
                            color = EmeraldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text = "সম্পন্ন",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationToggleCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isChecked) EmeraldPrimary.copy(alpha = 0.4f) else Color.LightGray.copy(alpha = 0.3f)
            )
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isChecked) EmeraldPrimary.copy(alpha = 0.12f) else Color.Gray.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isChecked) EmeraldPrimary else Color.Gray,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp,
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = EmeraldPrimary,
                    uncheckedThumbColor = Color.LightGray,
                    uncheckedTrackColor = Color.DarkGray.copy(alpha = 0.2f),
                ),
            )
        }
    }
}
