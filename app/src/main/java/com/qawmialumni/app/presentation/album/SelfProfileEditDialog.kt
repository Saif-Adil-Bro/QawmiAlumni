package com.qawmialumni.app.presentation.album

import android.content.Intent
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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.GraduationDepartment
import com.qawmialumni.app.domain.model.KhedmatCategory
import com.qawmialumni.app.domain.model.UserProfile
import com.qawmialumni.app.domain.model.UserRole
import com.qawmialumni.app.presentation.components.AvatarWithRing
import com.qawmialumni.app.presentation.components.BloodGroupPill
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import com.qawmialumni.app.presentation.theme.HeritageGold
import com.qawmialumni.app.presentation.theme.HeritageGoldLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfProfileDialog(
    viewModel: AlbumViewModel,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit,
) {
    val currentProfile by viewModel.currentSelfProfile.collectAsState()
    val saveState by viewModel.selfProfileState.collectAsState()
    val currentMadrasa by viewModel.currentMadrasa.collectAsState()
    val context = LocalContext.current

    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadSelfProfile()
    }

    LaunchedEffect(saveState) {
        if (saveState is Resource.Success) {
            viewModel.resetSelfProfileState()
            isEditing = false
        }
    }

    val profile = currentProfile

    // Form fields
    var name by remember(profile) { mutableStateOf(profile?.name.orEmpty()) }
    var fatherName by remember(profile) { mutableStateOf(profile?.fatherName.orEmpty()) }
    var phone by remember(profile) { mutableStateOf(profile?.phone.orEmpty()) }
    var whatsApp by remember(profile) { mutableStateOf(profile?.whatsApp.orEmpty()) }
    var selectedDepartment by remember(profile) {
        mutableStateOf(profile?.department ?: GraduationDepartment.DAWRA_HADITH)
    }
    var batchHijri by remember(profile) { mutableStateOf(profile?.batchYearHijri.orEmpty()) }
    var supervisedUstad by remember(profile) { mutableStateOf(profile?.supervisedByUstadName.orEmpty()) }
    var currentKhedmat by remember(profile) { mutableStateOf(profile?.currentKhedmat.orEmpty()) }
    var selectedCategory by remember(profile) {
        mutableStateOf(profile?.khedmatCategory ?: KhedmatCategory.TEACHING)
    }
    var bloodGroup by remember(profile) { mutableStateOf(profile?.bloodGroup.orEmpty()) }
    var district by remember(profile) { mutableStateOf(profile?.district.orEmpty()) }
    var bio by remember(profile) { mutableStateOf(profile?.bio.orEmpty()) }
    var newPhotoUri by remember { mutableStateOf<Uri?>(null) }

    var deptExpanded by remember { mutableStateOf(false) }
    var catExpanded by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> newPhotoUri = uri },
    )

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
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (isEditing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { isEditing = false },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "ফিরে যান",
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "প্রোফাইল তথ্য হালনাগাদ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                            )
                        }
                    } else {
                        Column {
                            Text(
                                text = "আমার প্রোফাইল ও আইডি",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                            )
                            Text(
                                text = "কওমি নেটওয়ার্ক পরিচিতি",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isEditing && profile != null) {
                            IconButton(
                                onClick = { isEditing = true },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary.copy(alpha = 0.12f)),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "প্রোফাইল এডিট",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "বন্ধ করুন",
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (profile == null) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EmeraldPrimary)
                    }
                } else if (!isEditing) {
                    // ==========================================
                    // 1. PROFILE VIEW MODE
                    // ==========================================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AvatarWithRing(
                            photoUrl = profile.photoUrl,
                            name = profile.name,
                            size = 88.dp,
                            ringColor = HeritageGold,
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        if (profile.fatherName.isNotBlank()) {
                            Text(
                                text = "পিতা: ${profile.fatherName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Role & Madrasa Badges
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            val (roleBadgeBg, roleBadgeText) = when (profile.role) {
                                UserRole.SUPER_ADMIN -> Color(0xFF7C3AED) to "সুপার অ্যাডমিন"
                                UserRole.MADRASA_ADMIN -> EmeraldPrimary to "মাদরাসা অ্যাডমিন"
                                UserRole.STAFF -> Color(0xFF0284C7) to "উস্তাদ / জিম্মাদার"
                                UserRole.ALUMNI -> HeritageGold to "ভেরিফায়েড ফারেগ"
                            }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = roleBadgeBg.copy(alpha = 0.14f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, roleBadgeBg.copy(alpha = 0.4f)),
                            ) {
                                Text(
                                    text = roleBadgeText,
                                    color = roleBadgeBg,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                )
                            }

                            if (currentMadrasa != null) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                ) {
                                    Text(
                                        text = "[${currentMadrasa?.code}] ${currentMadrasa?.name}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Details Card 1: Contact
                        ProfileSectionCard(title = "যোগাযোগ") {
                            ProfileDetailRow("মোবাইল", profile.phone.ifBlank { "তথ্য নেই" })
                            if (profile.whatsApp.isNotBlank()) {
                                ProfileDetailRow("হোয়াটসঅ্যাপ", profile.whatsApp)
                            }
                            if (profile.email.isNotBlank()) {
                                ProfileDetailRow("ইমেইল", profile.email)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Details Card 2: Academic
                        ProfileSectionCard(title = "শিক্ষাগত পরিচয়") {
                            ProfileDetailRow("বিভাগ", profile.department.banglaName)
                            ProfileDetailRow("শিক্ষাবর্ষ (হিজরি)", profile.batchYearHijri.ifBlank { "অনির্দিষ্ট" })
                            if (profile.batchYearEng.isNotBlank()) {
                                ProfileDetailRow("শিক্ষাবর্ষ (ঈসায়ী)", profile.batchYearEng)
                            }
                            if (profile.supervisedByUstadName.isNotBlank()) {
                                ProfileDetailRow("মুশরিফ / দরস", profile.supervisedByUstadName)
                            }
                            if (profile.dakhilaNumber.isNotBlank()) {
                                ProfileDetailRow("দাখিলা নম্বর", profile.dakhilaNumber)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Details Card 3: Current Khedmat
                        ProfileSectionCard(title = "বর্তমান খেদমত ও ঠিকানা") {
                            ProfileDetailRow("বর্তমান খেদমত", profile.currentKhedmat.ifBlank { "খেদমত যুক্ত নেই" })
                            ProfileDetailRow("খেদমতের ধরন", profile.khedmatCategory.banglaName)
                            ProfileDetailRow("নিজ জেলা", profile.district.ifBlank { "অনির্দিষ্ট" })
                            if (profile.bloodGroup.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text("রক্তের গ্রুপ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    BloodGroupPill(bloodGroup = profile.bloodGroup)
                                }
                            }
                            if (profile.bio.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("পরিচিতি / বায়ো:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(profile.bio, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))

                        // LOGOUT BUTTON AT THE BOTTOM OF THE PROFILE PAGE
                        Button(
                            onClick = {
                                onDismiss()
                                onSignOut()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "লগআউট",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "লগআউট করুন",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White,
                            )
                        }
                    }
                } else {
                    // ==========================================
                    // 2. PROFILE EDIT MODE
                    // ==========================================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Photo Picker
                        Box(
                            modifier = Modifier
                                .size(88.dp)
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
                            if (newPhotoUri != null) {
                                AsyncImage(
                                    model = newPhotoUri,
                                    contentDescription = "New Photo",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                )
                            } else if (profile.photoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = profile.photoUrl,
                                    contentDescription = "Current Photo",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Text(text = "ছবি পরিবর্তন", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text(
                            text = "ছবি পরিবর্তন করতে ট্যাপ করুন",
                            fontSize = 11.sp,
                            color = EmeraldPrimary,
                            modifier = Modifier.padding(top = 4.dp),
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("পূর্ণ নাম *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = fatherName,
                            onValueChange = { fatherName = it },
                            label = { Text("পিতার নাম") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("মোবাইল নম্বর *") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = whatsApp,
                            onValueChange = { whatsApp = it },
                            label = { Text("হোয়াটসঅ্যাপ নম্বর") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Department Dropdown
                        ExposedDropdownMenuBox(
                            expanded = deptExpanded,
                            onExpandedChange = { deptExpanded = !deptExpanded },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            OutlinedTextField(
                                value = selectedDepartment.banglaName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("ফারেগ / সমাপনী বিভাগ") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            )
                            ExposedDropdownMenu(
                                expanded = deptExpanded,
                                onDismissRequest = { deptExpanded = false },
                            ) {
                                GraduationDepartment.entries.forEach { dept ->
                                    DropdownMenuItem(
                                        text = { Text(dept.banglaName) },
                                        onClick = {
                                            selectedDepartment = dept
                                            deptExpanded = false
                                        },
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = batchHijri,
                            onValueChange = { batchHijri = it },
                            label = { Text("ফারেগ হওয়ার হিজরি সন (যেমন: ১৪৪৫)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = supervisedUstad,
                            onValueChange = { supervisedUstad = it },
                            label = { Text("মুশরিফ / প্রধান উস্তাদের নাম") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = currentKhedmat,
                            onValueChange = { currentKhedmat = it },
                            label = { Text("বর্তমান খেদমত ও প্রতিষ্ঠান") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Category Dropdown
                        ExposedDropdownMenuBox(
                            expanded = catExpanded,
                            onExpandedChange = { catExpanded = !catExpanded },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            OutlinedTextField(
                                value = selectedCategory.banglaName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("খেদমতের ধরন") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            )
                            ExposedDropdownMenu(
                                expanded = catExpanded,
                                onDismissRequest = { catExpanded = false },
                            ) {
                                KhedmatCategory.entries.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.banglaName) },
                                        onClick = {
                                            selectedCategory = cat
                                            catExpanded = false
                                        },
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = district,
                                onValueChange = { district = it },
                                label = { Text("নিজ জেলা") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = bloodGroup,
                                onValueChange = { bloodGroup = it },
                                label = { Text("ব্লাড গ্রুপ") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("সংক্ষিপ্ত পরিচিতি / বায়ো") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                        )

                        if (saveState is Resource.Error) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = (saveState as Resource.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { isEditing = false },
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Text("বাতিল")
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Button(
                                onClick = {
                                    val updated = profile.copy(
                                        name = name.trim(),
                                        fatherName = fatherName.trim(),
                                        phone = phone.trim(),
                                        whatsApp = whatsApp.ifBlank { phone }.trim(),
                                        department = selectedDepartment,
                                        batchYearHijri = batchHijri.trim(),
                                        supervisedByUstadName = supervisedUstad.trim(),
                                        currentKhedmat = currentKhedmat.trim(),
                                        khedmatCategory = selectedCategory,
                                        bloodGroup = bloodGroup.trim(),
                                        district = district.trim(),
                                        bio = bio.trim(),
                                    )
                                    viewModel.updateSelfProfile(updated, newPhotoUri)
                                },
                                enabled = saveState !is Resource.Loading && name.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                modifier = Modifier.weight(1.5f).height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                if (saveState is Resource.Loading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                                } else {
                                    Text("সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
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
fun SelfProfileEditDialog(
    viewModel: AlbumViewModel,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit = {},
) = SelfProfileDialog(viewModel, onDismiss, onSignOut)

@Composable
private fun ProfileSectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary,
                modifier = Modifier.padding(bottom = 6.dp),
            )
            content()
        }
    }
}

@Composable
private fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
