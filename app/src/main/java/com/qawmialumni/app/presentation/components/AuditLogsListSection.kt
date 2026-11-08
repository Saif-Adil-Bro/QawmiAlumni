package com.qawmialumni.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qawmialumni.app.core.util.DateTimeFormatter
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.AuditActionType
import com.qawmialumni.app.domain.model.AuditLog
import com.qawmialumni.app.domain.model.UserRole
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import com.qawmialumni.app.presentation.theme.EmeraldTeal
import com.qawmialumni.app.presentation.theme.HeritageGold

@Composable
fun AuditLogsListSection(
    auditLogsResource: Resource<List<AuditLog>>,
    modifier: Modifier = Modifier,
) {
    when (val res = auditLogsResource) {
        is Resource.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EmeraldPrimary)
            }
        }
        is Resource.Error -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = res.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is Resource.Success -> {
            if (res.data.isEmpty()) {
                Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ModernEmptyState(
                        icon = QawmiIcons.AuditHistory,
                        title = "কোনো অ্যাক্টিভিটি লগ নেই",
                        subtitle = "ব্যবহারকারী বা অ্যাডমিনদের কার্যক্রম (অনুমোদন, নোটিশ প্রকাশ, ফোরাম মডারেশন) সম্পন্ন হলে এখানে অডিট লগ হিসেবে দেখা যাবে।",
                    )
                }
            } else {
                LazyColumn(
                    modifier = modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = EmeraldPrimary.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = QawmiIcons.AdminShield,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "সর্বমোট ${DateTimeFormatter.toBengaliDigits(res.data.size.toString())}টি কার্যক্রম রেকর্ড",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary,
                                )
                            }
                        }
                    }

                    items(res.data, key = { it.logId.ifBlank { it.timestamp.toString() } }) { log ->
                        AuditLogItemCard(log = log)
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditLogItemCard(log: AuditLog) {
    val actionColor = when (log.actionType) {
        AuditActionType.STUDENT_APPROVED -> EmeraldPrimary
        AuditActionType.STUDENT_REJECTED -> MaterialTheme.colorScheme.error
        AuditActionType.ADMIN_ASSIGNED, AuditActionType.MADRASA_CREATED -> HeritageGold
        AuditActionType.ADMIN_REMOVED -> Color(0xFFC62828)
        AuditActionType.ROLE_UPDATED -> EmeraldTeal
        AuditActionType.NOTICE_POSTED, AuditActionType.NOTICE_RSVP -> EmeraldPrimary
        AuditActionType.FORUM_POST_CREATED, AuditActionType.FORUM_POST_APPROVED -> EmeraldTeal
        AuditActionType.FORUM_POST_DELETED -> MaterialTheme.colorScheme.error
        AuditActionType.FORUM_POST_PINNED -> HeritageGold
        AuditActionType.MADRASA_UPDATE_REQUESTED -> Color(0xFFF59E0B)
        AuditActionType.MADRASA_UPDATE_APPROVED -> EmeraldPrimary
        AuditActionType.MADRASA_UPDATE_REJECTED -> MaterialTheme.colorScheme.error
        AuditActionType.USER_REGISTERED -> Color(0xFF1E88E5)
    }

    val actorRoleLabel = when (log.actorRole) {
        UserRole.SUPER_ADMIN -> "সুপার অ্যাডমিন"
        UserRole.MADRASA_ADMIN -> "মাদরাসা অ্যাডমিন"
        UserRole.STAFF -> "আসাতিযায়ে কেরাম"
        else -> "ফারেগীন"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(actionColor.copy(alpha = 0.25f))
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
        ) {
            // Header: Actor Info + Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(actionColor.copy(alpha = 0.15f))
                            .border(1.dp, actionColor.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = log.actorName.firstOrNull()?.toString() ?: "ক",
                            color = actionColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = log.actorName.ifBlank { "ব্যবহারকারী" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = actionColor.copy(alpha = 0.12f),
                            modifier = Modifier.padding(top = 2.dp),
                        ) {
                            Text(
                                text = actorRoleLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = actionColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }

                Text(
                    text = DateTimeFormatter.formatRelativeBangla(log.timestamp),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Badge + Target Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = actionColor,
                ) {
                    Text(
                        text = log.actionType.banglaLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }

                if (log.targetTitle.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = log.targetTitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                }
            }

            if (log.details.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = log.details,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                )
            }
        }
    }
}
