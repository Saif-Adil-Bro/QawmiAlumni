package com.qawmialumni.app.presentation.chat

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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.ChatRoom
import com.qawmialumni.app.presentation.components.AvatarWithRing
import com.qawmialumni.app.presentation.components.ModernEmptyState
import com.qawmialumni.app.presentation.components.QawmiIcons
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import com.qawmialumni.app.presentation.theme.HeritageGold
import com.qawmialumni.app.presentation.theme.HeritageGoldLight

@Composable
fun ChatListScreen(
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val roomsResource by viewModel.chatRooms.collectAsState()
    val session by viewModel.dataStore.userSession.collectAsState(initial = null)

    var activeRoomForChat by remember { mutableStateOf<ChatRoom?>(null) }

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
                    contentPadding = PaddingValues(16.dp),
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

    activeRoomForChat?.let { room ->
        val currentUid = session?.uid.orEmpty()
        val otherUid = room.participants.firstOrNull { it != currentUid } ?: ""
        val otherName = room.participantNames[otherUid] ?: "সাথী"
        val otherPhoto = room.participantPhotos[otherUid] ?: ""

        ChatRoomDialog(
            room = room,
            currentUid = currentUid,
            otherUid = otherUid,
            otherName = otherName,
            otherPhoto = otherPhoto,
            viewModel = viewModel,
            onDismiss = { activeRoomForChat = null },
        )
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
                size = 50.dp,
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

@Composable
fun ChatRoomDialog(
    room: ChatRoom,
    currentUid: String,
    otherUid: String,
    otherName: String,
    otherPhoto: String,
    viewModel: ChatViewModel,
    onDismiss: () -> Unit,
) {
    val roomResource by viewModel.observeRoom(room.roomId).collectAsState()
    var messageText by remember { mutableStateOf("") }

    val liveRoom = (roomResource as? Resource.Success)?.data ?: room

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(540.dp)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Branded Emerald Chat Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldPrimary)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AvatarWithRing(
                        photoUrl = otherPhoto,
                        name = otherName,
                        size = 40.dp,
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
                        )
                        Text(
                            text = "সরাসরি ইন-অ্যাপ মেসেজ",
                            fontSize = 11.sp,
                            color = HeritageGoldLight,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                        )
                    }
                }

                // Bucketed Messages List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
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
                                    .widthIn(max = 260.dp)
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
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
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
                                    .padding(horizontal = 14.dp, vertical = 9.dp),
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

                // Sticky Input Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text("আসসালামু আলাইকুম, বার্তা লিখুন...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(50),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.sendMessage(
                                    roomId = room.roomId,
                                    recipientId = otherUid,
                                    recipientName = otherName,
                                    recipientPhoto = otherPhoto,
                                    text = messageText,
                                )
                                messageText = ""
                            },
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
    }
}
