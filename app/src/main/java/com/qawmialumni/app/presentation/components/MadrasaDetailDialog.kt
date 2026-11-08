package com.qawmialumni.app.presentation.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.Madrasa
import com.qawmialumni.app.domain.model.VerificationStatus
import com.qawmialumni.app.presentation.album.AlbumViewModel
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import com.qawmialumni.app.presentation.theme.HeritageGold
import com.qawmialumni.app.presentation.theme.HeritageGoldLight

@Composable
fun MadrasaDetailDialog(
    madrasa: Madrasa,
    viewModel: AlbumViewModel,
    onDismiss: () -> Unit,
) {
    val pendingReq by viewModel.pendingMadrasaUpdateRequest.collectAsState()
    val updateState by viewModel.madrasaUpdateRequestState.collectAsState()

    var isEditMode by remember { mutableStateOf(false) }

    var name by remember(madrasa) { mutableStateOf(madrasa.name) }
    var address by remember(madrasa) { mutableStateOf(madrasa.address) }
    var establishedYear by remember(madrasa) { mutableStateOf(madrasa.establishedYear) }
    var contactEmail by remember(madrasa) { mutableStateOf(madrasa.pendingAdminEmails.firstOrNull().orEmpty()) }
    var selectedLogoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedLogoUri = uri },
    )

    LaunchedEffect(updateState) {
        if (updateState is Resource.Success) {
            isEditMode = false
            viewModel.resetMadrasaUpdateRequestState()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 18.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEditMode) "মাদরাসা তথ্য সংশোধন আবেদন" else "মাদরাসা প্রোফাইল ও বিবরণ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                        )
                        Text(
                            text = "কোড: [${madrasa.code}]",
                            style = MaterialTheme.typography.bodySmall,
                            color = HeritageGold,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "বন্ধ করুন", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Pending Banner if there is a pending request
                if (pendingReq != null && pendingReq?.status == VerificationStatus.PENDING) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "সুপার অ্যাডমিনের অনুমোদনের অপেক্ষায়",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFB45309),
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "আপনার পাঠানো তথ্য পরিবর্তনের আবেদন জমা হয়েছে। সুপার অ্যাডমিন অনুমোদন (Accept) করলে তা কার্যকর হবে।",
                                fontSize = 12.sp,
                                color = Color(0xFF92400E),
                            )
                            if (pendingReq?.requestedName?.isNotBlank() == true && pendingReq?.requestedName != madrasa.name) {
                                Text(
                                    text = "• প্রস্তাবিত নাম: ${pendingReq?.requestedName}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF78350F),
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                            if (pendingReq?.requestedAddress?.isNotBlank() == true && pendingReq?.requestedAddress != madrasa.address) {
                                Text(
                                    text = "• প্রস্তাবিত ঠিকানা: ${pendingReq?.requestedAddress}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF78350F),
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                if (!isEditMode) {
                    // VIEW MODE
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Madrasa Logo
                        if (madrasa.logoUrl.isNotBlank()) {
                            AsyncImage(
                                model = madrasa.logoUrl,
                                contentDescription = madrasa.name,
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .border(2.5.dp, HeritageGold, CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary.copy(alpha = 0.1f))
                                    .border(2.5.dp, HeritageGold, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "ق",
                                    fontSize = 42.sp,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = madrasa.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Info cards
                        DetailItemRow(label = "স্থাপিত", value = if (madrasa.establishedYear.isNotBlank()) "${madrasa.establishedYear} খ্রি." else "অনির্দিষ্ট")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        DetailItemRow(label = "ঠিকানা / অবস্থান", value = madrasa.address.ifBlank { "অনির্দিষ্ট" })
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        DetailItemRow(
                            label = "অ্যাডমিন ইমেইল",
                            value = madrasa.pendingAdminEmails.joinToString(", ").ifBlank { "কোনো ইমেইল যুক্ত নেই" },
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        DetailItemRow(
                            label = "নিবন্ধিত ফারেগ / আলামনাই",
                            value = "${madrasa.totalAlumniCount} জন",
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { isEditMode = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "তথ্য পরিবর্তন / সংশোধনের আবেদন", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                } else {
                    // EDIT MODE
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "তথ্য পরিবর্তনের পর তা পেন্ডিং থাকবে। সুপার অ্যাডমিন গ্রহণ (Accept) করলে আনুষ্ঠানিকভাবে কার্যকর হবে।",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Logo picker
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .border(2.dp, HeritageGold, CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selectedLogoUri != null) {
                                AsyncImage(
                                    model = selectedLogoUri,
                                    contentDescription = "New Logo",
                                    modifier = Modifier.size(84.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                )
                            } else if (madrasa.logoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = madrasa.logoUrl,
                                    contentDescription = "Current Logo",
                                    modifier = Modifier.size(84.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Text(text = "লোগো নির্বাচন", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text(
                            text = "নতুন লোগো পরিবর্তন করতে ট্যাপ করুন",
                            fontSize = 11.sp,
                            color = EmeraldPrimary,
                            modifier = Modifier.padding(top = 4.dp),
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("মাদরাসার পূর্ণ নাম") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("মাদরাসার ঠিকানা / জেলা") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = establishedYear,
                            onValueChange = { establishedYear = it },
                            label = { Text("প্রতিষ্ঠার সাল (যেমন: 1985)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = contactEmail,
                            onValueChange = { contactEmail = it },
                            label = { Text("অফিশিয়াল অ্যাডমিন ইমেইল") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                        )

                        if (updateState is Resource.Error) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = (updateState as Resource.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { isEditMode = false },
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Text("বাতিল")
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Button(
                                onClick = {
                                    viewModel.submitMadrasaUpdateRequest(
                                        name = name,
                                        address = address,
                                        establishedYear = establishedYear,
                                        contactEmail = contactEmail,
                                        newLogoUri = selectedLogoUri,
                                    )
                                },
                                enabled = updateState !is Resource.Loading && name.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                modifier = Modifier.weight(1.5f).height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                if (updateState is Resource.Loading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                } else {
                                    Text("আবেদন জমা দিন", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItemRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
