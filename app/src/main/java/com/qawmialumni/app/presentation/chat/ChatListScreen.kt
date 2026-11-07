package com.qawmialumni.app.presentation.chat

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.ChatRoom
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import com.qawmialumni.app.presentation.theme.HeritageGold

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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "কোনো সরাসরি ইন-অ্যাপ কথোপকথন নেই।",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "যেকোনো সাথী বা উস্তাদের প্রোফাইলে গিয়ে সরাসরি মেসেজ পাঠান অথবা হোয়াটসঅ্যাপ/কল করুন।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
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
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                if (photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Text(name.take(1), fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = lastMessage.ifBlank { "বার্তা পাঠান..." },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
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
                .height(520.dp)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(otherName.take(1), fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = otherName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bucketed Messages List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
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
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 14.dp,
                                            topEnd = 14.dp,
                                            bottomStart = if (isMe) 14.dp else 2.dp,
                                            bottomEnd = if (isMe) 2.dp else 14.dp,
                                        )
                                    )
                                    .background(if (isMe) EmeraldPrimary else Color(0xFFE2E8F0))
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    text = msg.text,
                                    color = if (isMe) Color.White else Color(0xFF0F172A),
                                    fontSize = 14.sp,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Input box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("বার্তা লিখুন...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
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
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("পাঠান")
                    }
                }
            }
        }
    }
}
