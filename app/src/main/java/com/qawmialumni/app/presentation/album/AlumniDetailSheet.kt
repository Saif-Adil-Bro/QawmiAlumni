package com.qawmialumni.app.presentation.album

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.qawmialumni.app.domain.model.UserProfile
import com.qawmialumni.app.presentation.components.BloodGroupPill
import com.qawmialumni.app.presentation.components.VerifiedBadge
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import com.qawmialumni.app.presentation.theme.HeritageGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniDetailSheet(
    alumni: UserProfile,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

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
            // Digital Alumni ID Card Composable (Blueprint §7.3)
            DigitalAlumniIdCard(alumni = alumni)

            Spacer(modifier = Modifier.height(20.dp))

            // 1-Click WhatsApp & Phone Call Action Buttons (Blueprint §5.D.2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (alumni.whatsApp.isNotBlank()) {
                    Button(
                        onClick = { openWhatsApp(context, alumni.whatsApp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("হোয়াটসঅ্যাপ", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                if (alumni.phone.isNotBlank()) {
                    Button(
                        onClick = { dialPhone(context, alumni.phone) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("সরাসরি কল", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Detailed information table
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow(label = "পিতার নাম", value = alumni.fatherName)
                    InfoRow(label = "দাখেলা / রোল", value = alumni.dakhilaNumber)
                    InfoRow(label = "হিজরি শিক্ষাবর্ষ", value = "${alumni.batchYearHijri} হিজরি")
                    if (alumni.batchYearEng.isNotBlank()) {
                        InfoRow(label = "ইংরেজি সাল", value = "${alumni.batchYearEng} খ্রি.")
                    }
                    InfoRow(label = "তত্ত্বাবধায়ক উস্তাদ", value = alumni.supervisedByUstadName)
                    InfoRow(label = "বর্তমান খেদমত", value = alumni.currentKhedmat)
                    InfoRow(label = "খেদমতের ধরন", value = alumni.khedmatCategory.banglaName)
                    InfoRow(label = "রক্তের গ্রুপ", value = alumni.bloodGroup)
                    InfoRow(label = "নিজ জেলা", value = alumni.district)
                    if (alumni.bio.isNotBlank()) {
                        InfoRow(label = "পরিচিতি", value = alumni.bio)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Digital Alumni ID Card (Blueprint §7.3):
 * Stylized card with emerald Islamic header, gold crescent badge, QR mockup, photo, and details.
 */
@Composable
fun DigitalAlumniIdCard(alumni: UserProfile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, HeritageGold.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
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
                            text = "ডিজিটাল আলামনাই কার্ড",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                        Text(
                            text = "بطاقة عضوية الخريجين",
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
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
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
                        Text(alumni.name.take(1), fontWeight = FontWeight.Bold, fontSize = 32.sp, color = EmeraldPrimary)
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alumni.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ফারেগ: ${alumni.batchYearHijri} হিজরি (${alumni.batchYearEng} খ্রি.)",
                        fontSize = 12.sp,
                        color = HeritageGold,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (alumni.currentKhedmat.isNotBlank()) {
                        Text(
                            text = alumni.currentKhedmat,
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            maxLines = 2,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    BloodGroupPill(bloodGroup = alumni.bloodGroup)
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
