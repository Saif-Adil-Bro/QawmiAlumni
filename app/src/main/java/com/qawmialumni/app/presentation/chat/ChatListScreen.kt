package com.qawmialumni.app.presentation.chat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.ChatRoom
import com.qawmialumni.app.presentation.components.AvatarWithRing
import com.qawmialumni.app.presentation.components.ModernEmptyState
import com.qawmialumni.app.presentation.components.QawmiIcons
import com.qawmialumni.app.presentation.theme.EmeraldDeep
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import com.qawmialumni.app.presentation.theme.HeritageGold
import com.qawmialumni.app.presentation.theme.HeritageGoldLight

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith

@Composable
fun ChatListScreen(
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val roomsResource by viewModel.chatRooms.collectAsState()
    val session by viewModel.dataStore.userSession.collectAsState(initial = null)

    var activeRoomForChat by remember { mutableStateOf<ChatRoom?>(null) }

    AnimatedContent(
        targetState = activeRoomForChat,
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
        label = "ChatScreenTransition",
    ) { room ->
        if (room != null) {
            val currentUid = session?.uid.orEmpty()
            val otherUid = room.participants.firstOrNull { it != currentUid } ?: ""
            val otherName = room.participantNames[otherUid] ?: "সাথী"
            val otherPhoto = room.participantPhotos[otherUid] ?: ""

            FullPageChatRoomScreen(
                room = room,
                currentUid = currentUid,
                otherUid = otherUid,
                otherName = otherName,
                otherPhoto = otherPhoto,
                viewModel = viewModel,
                onBack = { activeRoomForChat = null },
            )
        } else {
            when (val res = roomsResource) {
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
                        icon = QawmiIcons.ChatMessage,
                        title = "কোনো ইন-অ্যাপ কথোপকথন নেই",
                        subtitle = "ফারেগীন অ্যালবাম থেকে যেকোনো সাথী বা উস্তাদের প্রোফাইলে গিয়ে 'ইন-অ্যাপ মেসেজ' বাটনে চাপ দিলে এখানে চ্যাট শুরু হবে।",
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(res.data, key = { it.roomId }) { room ->
                        val otherUid = room.participants.firstOrNull { it != session?.uid } ?: ""
                        val otherName = room.participantNames[otherUid] ?: "সাথী"
                        val otherPhoto = room.participantPhotos[otherUid] ?: ""

                        ChatRoomCard(
                            name = otherName,
                            photoUrl = otherPhoto,
                            lastMessage = room.lastMessage,
                            messageCount = room.recentMessages.size,
                            onClick = { activeRoomForChat = room },
                        )
                    }
                }
            }
        }
    }
}
}
}

@Composable
fun ChatRoomCard(
    name: String,
    photoUrl: String,
    lastMessage: String,
    messageCount: Int = 0,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarWithRing(
                photoUrl = photoUrl,
                name = name,
                size = 52.dp,
                ringColor = HeritageGold,
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = lastMessage.ifBlank { "কথোপকথন শুরু করুন..." },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (messageCount > 0) {
                Surface(
                    color = EmeraldPrimary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(50),
                ) {
                    Text(
                        text = "$messageCount মেসেজ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

/**
 * Full-Page WhatsApp-Style Chat Screen (replaces the old popup dialog).
 */
@Composable
fun FullPageChatRoomScreen(
    room: ChatRoom,
    currentUid: String,
    otherUid: String,
    otherName: String,
    otherPhoto: String,
    viewModel: ChatViewModel,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    val roomResource by viewModel.observeRoom(room.roomId).collectAsState()
    var messageText by remember { mutableStateOf("") }

    val liveRoom = (roomResource as? Resource.Success)?.data ?: room

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFECE5DD).copy(alpha = 0.35f)),
    ) {
        // WhatsApp-Style Full-Width Emerald Header Bar
        Surface(
            color = EmeraldDeep,
            tonalElevation = 4.dp,
            shadowElevation = 4.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "ফিরে যান",
                        tint = Color.White,
                    )
                }

                AvatarWithRing(
                    photoUrl = otherPhoto,
                    name = otherName,
                    size = 42.dp,
                    ringColor = HeritageGoldLight,
                    backgroundColor = Color.White.copy(alpha = 0.15f),
                    textColor = Color.White,
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = otherName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "● সরাসরি ইন-অ্যাপ চ্যাট (কওমি নেটওয়ার্ক)",
                        fontSize = 11.sp,
                        color = HeritageGoldLight,
                    )
                }
            }
        }

        // Full-Screen Scrollable Message Area
        if (liveRoom.recentMessages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(14.dp),
                    tonalElevation = 1.dp,
                ) {
                    Text(
                        text = "আসসালামু আলাইকুম ওয়া রাহমাতুল্লাহ!\n$otherName-এর সাথে আপনার কথোপকথন শুরু করুন।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(liveRoom.recentMessages.reversed()) { msg ->
                    val isMe = msg.senderId == currentUid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 300.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isMe) 16.dp else 4.dp,
                                        bottomEnd = if (isMe) 4.dp else 16.dp,
                                    )
                                )
                                .background(if (isMe) EmeraldPrimary else MaterialTheme.colorScheme.surface)
                                .then(
                                    if (!isMe) {
                                        Modifier.border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                            RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = 4.dp,
                                                bottomEnd = 16.dp,
                                            ),
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Text(
                                text = msg.text,
                                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

        // Sticky Bottom WhatsApp-Style Message Input Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 6.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("আসসালামু আলাইকুম, বার্তা লিখুন...") },
                    modifier = Modifier.weight(1f),
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(
                                roomId = room.roomId,
                                recipientId = otherUid,
                                recipientName = otherName,
                                recipientPhoto = otherPhoto,
                                text = messageText,
                            )
                            messageText = ""
                        }
                    },
                    enabled = messageText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "পাঠান",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
