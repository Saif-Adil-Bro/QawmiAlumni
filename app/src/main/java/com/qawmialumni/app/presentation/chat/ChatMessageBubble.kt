package com.qawmialumni.app.presentation.chat

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qawmialumni.app.core.util.DateTimeFormatter
import com.qawmialumni.app.domain.model.ChatMessage
import com.qawmialumni.app.domain.model.MessageStatus
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * WhatsApp-style chat bubble.
 *
 * - right aligned emerald bubble for my own messages, left aligned white bubble for the peer
 * - time stamp in the corner
 * - tick indicator: clock (sending) → single grey tick (sent) → double grey tick (delivered)
 *   → double blue tick (read)
 * - long press opens the message actions (copy / delete for everyone)
 * - a deleted message keeps its place with an italic "এই বার্তাটি মুছে ফেলা হয়েছে"
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    isMe: Boolean,
    /** Resolved tick state: SENDING → SENT → DELIVERED → READ. */
    tickStatus: MessageStatus,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bubbleShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isMe) 16.dp else 4.dp,
        bottomEnd = if (isMe) 4.dp else 16.dp,
    )

    val deleted = message.isDeletedPlaceholder

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(bubbleShape)
                .background(
                    when {
                        isMe -> EmeraldPrimary
                        else -> MaterialTheme.colorScheme.surface
                    }
                )
                .then(
                    if (!isMe) {
                        Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), bubbleShape)
                    } else {
                        Modifier
                    }
                )
                .combinedClickable(onClick = {}, onLongClick = onLongPress)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = if (deleted) "🚫 এই বার্তাটি মুছে ফেলা হয়েছে" else message.text,
                color = when {
                    isMe -> Color.White.copy(alpha = if (deleted) 0.75f else 1f)
                    deleted -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontStyle = if (deleted) FontStyle.Italic else FontStyle.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )

            Spacer(modifier = Modifier.size(2.dp))

            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatTime(message.timestamp),
                    color = if (isMe) Color(0xFFD1FAE5) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                )
                if (isMe && !deleted) {
                    Spacer(modifier = Modifier.width(4.dp))
                    MessageStatusIndicator(
                        status = if (message.status == MessageStatus.SENDING) {
                            MessageStatus.SENDING
                        } else {
                            tickStatus
                        },
                        unreadColor = Color(0xFFD1FAE5),
                    )
                }
            }
        }
    }
}

/**
 * Clock → single tick → double tick. Blue double tick when read.
 */
@Composable
fun MessageStatusIndicator(
    status: MessageStatus,
    unreadColor: Color,
) {
    val tint = if (status == MessageStatus.READ) Color(0xFF38BDF8) else unreadColor
    if (status == MessageStatus.SENDING) {
        CircularProgressIndicator(
            modifier = Modifier.size(11.dp),
            strokeWidth = 1.4.dp,
            color = tint,
        )
        return
    }
    Canvas(modifier = Modifier.size(width = 17.dp, height = 11.dp)) {
        val stroke = Stroke(
            width = size.height * 0.17f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        fun checkPath(offsetX: Float): Path = Path().apply {
            moveTo(offsetX, size.height * 0.55f)
            lineTo(offsetX + size.width * 0.26f, size.height * 0.85f)
            lineTo(offsetX + size.width * 0.62f, size.height * 0.18f)
        }
        if (status == MessageStatus.SENT) {
            drawPath(checkPath(size.width * 0.18f), tint, style = stroke)
        } else {
            // Delivered / read → WhatsApp's overlapping double tick.
            drawPath(checkPath(0f), tint, style = stroke)
            drawPath(checkPath(size.width * 0.36f), tint, style = stroke)
        }
    }
}

fun formatTime(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return DateTimeFormatter.toBengaliDigits(sdf.format(Date(timestamp)))
}

/** Small centred separator that shows a short status tip when the peer is online-ish. */
@Composable
fun ChatTickLegend() {
    Text(
        text = "✓ পাঠানো হয়েছে   ✓✓ ডেলিভারি হয়েছে   ✓✓ নীল = পড়া হয়েছে",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 10.sp,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(vertical = 2.dp),
    )
}
