package com.qawmialumni.app.presentation.album

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.qawmialumni.app.domain.model.UserProfile
import com.qawmialumni.app.domain.model.UserRole
import com.qawmialumni.app.presentation.components.BloodGroupPill
import com.qawmialumni.app.presentation.components.VerifiedBadge
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import com.qawmialumni.app.presentation.theme.HeritageGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniDetailSheet(
    alumni: UserProfile,
    currentViewerUid: String = "",
    isViewerMadrasaAdmin: Boolean = false,
    onToggleStaffRole: () -> Unit = {},
    onSendDirectMessage: (String, () -> Unit) -> Unit = { _, _ -> },
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var showQuickMessageBox by remember { mutableStateOf(false) }
    var directMessageText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Digital Alumni ID Card Composable (Blueprint §7.3) with QR Verification Matrix
            DigitalAlumniIdCard(alumni = alumni)

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Action Row: WhatsApp, Direct Call, In-App Message, Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (alumni.whatsApp.isNotBlank()) {
                    Button(
                        onClick = { openWhatsApp(context, alumni.whatsApp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("হোয়াটসঅ্যাপ", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    }
                }

                if (alumni.phone.isNotBlank()) {
                    Button(
                        onClick = { dialPhone(context, alumni.phone) },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("সরাসরি কল", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (currentViewerUid.isNotBlank() && alumni.uid != currentViewerUid) {
                    OutlinedButton(
                        onClick = { showQuickMessageBox = !showQuickMessageBox },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("✉️ ইন-অ্যাপ মেসেজ", fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontSize = 12.sp)
                    }
                }

                OutlinedButton(
                    onClick = { shareAlumniCard(context, alumni) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("📤 পরিচিতি শেয়ার", fontWeight = FontWeight.Bold, color = HeritageGold, fontSize = 12.sp)
                }
            }

            // Inline Quick Bucketed Chat Composer
            if (showQuickMessageBox) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.06f)),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "${alumni.name}-কে ইন-অ্যাপ মেসেজ পাঠান:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                value = directMessageText,
                                onValueChange = { directMessageText = it },
                                placeholder = { Text("আসসালামু আলাইকুম...") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    onSendDirectMessage(directMessageText) {
                                        directMessageText = ""
                                        showQuickMessageBox = false
                                        Toast.makeText(
                                            context,
                                            "মেসেজ পাঠানো হয়েছে! চ্যাট ট্যাবে কথোপকথন দেখুন।",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Text("পাঠান")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Detailed information table
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow(label = "দায়িত্ব / রোল", value = alumni.role.banglaLabel)
                    InfoRow(label = "ফারেগ হওয়ার বিভাগ", value = alumni.department.banglaName)
                    InfoRow(label = "পিতার নাম", value = alumni.fatherName)
                    InfoRow(label = "দাখেলা / রোল", value = alumni.dakhilaNumber)
                    InfoRow(label = "হিজরি শিক্ষাবর্ষ", value = "${alumni.batchYearHijri} হিজরি")
                    if (alumni.batchYearEng.isNotBlank()) {
                        InfoRow(label = "ইংরেজি সাল", value = "${alumni.batchYearEng} খ্রি.")
                    }
                    InfoRow(label = "মুরব্বি / তত্ত্বাবধায়ক উস্তাদ", value = alumni.supervisedByUstadName)
                    InfoRow(label = "বর্তমান খেদমত", value = alumni.currentKhedmat)
                    InfoRow(label = "খেদমতের ক্ষেত্র", value = alumni.khedmatCategory.banglaName)
                    InfoRow(label = "রক্তের গ্রুপ", value = alumni.bloodGroup)
                    InfoRow(label = "নিজ জেলা", value = alumni.district)
                    if (alumni.bio.isNotBlank()) {
                        InfoRow(label = "সংক্ষিপ্ত পরিচিতি", value = alumni.bio)
                    }
                }
            }

            // Madrasa Admin Action: Promote/Demote Batch Representative (Staff)
            if (isViewerMadrasaAdmin && alumni.uid != currentViewerUid && alumni.role != UserRole.MADRASA_ADMIN) {
                Spacer(modifier = Modifier.height(14.dp))
                val isAlreadyStaff = alumni.role == UserRole.STAFF
                OutlinedButton(
                    onClick = onToggleStaffRole,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = if (isAlreadyStaff) {
                            "ব্যাচ জিম্মাদার (Staff) দায়িত্ব প্রত্যাহার করুন"
                        } else {
                            "★ ব্যাচ জিম্মাদার (Staff) হিসেবে নিয়োগ দিন"
                        },
                        color = if (isAlreadyStaff) MaterialTheme.colorScheme.error else EmeraldPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Digital Alumni ID Card (Blueprint §7.3):
 * Stylized card with emerald Islamic header, gold crescent badge, photo, details, and Canvas QR Code.
 */
@Composable
fun DigitalAlumniIdCard(alumni: UserProfile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, HeritageGold.copy(alpha = 0.6f), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmeraldPrimary)
                    .padding(vertical = 12.dp, horizontal = 16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "ডিজিটাল ফারেগীন আইডি কার্ড",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                        Text(
                            text = "بطاقة عضوية الخريجين • ${alumni.department.banglaName}",
                            color = HeritageGold,
                            fontSize = 11.sp,
                        )
                    }
                    VerifiedBadge()
                }
            }

            // Body
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, HeritageGold, RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    if (alumni.photoUrl.isNotBlank()) {
                        AsyncImage(
                            model = alumni.photoUrl,
                            contentDescription = alumni.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text(alumni.name.take(1), fontWeight = FontWeight.Bold, fontSize = 30.sp, color = EmeraldPrimary)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alumni.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ব্যাচ: ${alumni.batchYearHijri} হিজরি" +
                            if (alumni.batchYearEng.isNotBlank()) " (${alumni.batchYearEng})" else "",
                        fontSize = 12.sp,
                        color = HeritageGold,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (alumni.dakhilaNumber.isNotBlank()) {
                        Text(
                            text = "দাখেলা নং: ${alumni.dakhilaNumber}",
                            fontSize = 11.sp,
                            color = Color(0xFF475569),
                        )
                    }
                    if (alumni.currentKhedmat.isNotBlank()) {
                        Text(
                            text = alumni.currentKhedmat,
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            maxLines = 1,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    BloodGroupPill(bloodGroup = alumni.bloodGroup)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Zero-Dependency Deterministic Verification QR Code Matrix
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AlumniQrBadge(
                        seed = "qawmialumni://verify/${alumni.madrasaId}/${alumni.uid}",
                        modifier = Modifier.size(62.dp),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ID#${alumni.uid.takeLast(5).uppercase()}",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                    )
                }
            }
        }
    }
}

/**
 * Renders a crisp 15x15 QR-style matrix in Compose Canvas with standard 5x5 finder patterns
 * and deterministic payload bits derived from [seed]. Requires 0 external libraries!
 */
@Composable
fun AlumniQrBadge(
    seed: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .border(1.dp, EmeraldPrimary.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
            .padding(4.dp),
        color = Color.White,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSize = 15
            val cellW = size.width / gridSize
            val cellH = size.height / gridSize

            fun isFinderModule(r: Int, c: Int): Boolean? {
                fun checkCorner(topR: Int, leftC: Int): Boolean? {
                    val dr = r - topR
                    val dc = c - leftC
                    if (dr !in 0..4 || dc !in 0..4) return null
                    if (dr == 0 || dr == 4 || dc == 0 || dc == 4) return true
                    if (dr == 2 && dc == 2) return true
                    return false
                }
                checkCorner(0, 0)?.let { return it }
                checkCorner(0, gridSize - 5)?.let { return it }
                checkCorner(gridSize - 5, 0)?.let { return it }
                return null
            }

            val hash = seed.hashCode()
            for (r in 0 until gridSize) {
                for (c in 0 until gridSize) {
                    val finder = isFinderModule(r, c)
                    val filled = finder ?: run {
                        val bitIndex = (r * gridSize + c) % 31
                        val charCode = seed[(r * 7 + c * 13) % seed.length.coerceAtLeast(1)].code
                        ((hash shr bitIndex) xor (charCode shr (c % 5))) and 1 == 1
                    }
                    if (filled) {
                        drawRect(
                            color = EmeraldPrimary,
                            topLeft = Offset(c * cellW, r * cellH),
                            size = Size(cellW * 0.95f, cellH * 0.95f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.5f),
        )
    }
}

private fun openWhatsApp(context: Context, number: String) {
    val clean = number.replace(Regex("[^0-9]"), "")
    val uri = Uri.parse("https://wa.me/$clean")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
}

private fun dialPhone(context: Context, number: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
    context.startActivity(intent)
}

private fun shareAlumniCard(context: Context, alumni: UserProfile) {
    val shareText = buildString {
        appendLine("🕌 ডিজিটাল ফারেগীন পরিচিতি কার্ড")
        appendLine("নাম: ${alumni.name}")
        appendLine("বিভাগ: ${alumni.department.banglaName} (${alumni.batchYearHijri} হিজরি)")
        if (alumni.currentKhedmat.isNotBlank()) appendLine("খেদমত: ${alumni.currentKhedmat}")
        if (alumni.bloodGroup.isNotBlank()) appendLine("রক্তের গ্রুপ: ${alumni.bloodGroup}")
        if (alumni.district.isNotBlank()) appendLine("জেলা: ${alumni.district}")
        if (alumni.phone.isNotBlank()) appendLine("যোগাযোগ: ${alumni.phone}")
    }
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(sendIntent, "ফারেগীন পরিচিতি শেয়ার করুন"))
}
