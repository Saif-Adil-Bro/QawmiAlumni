package com.qawmialumni.app.presentation.album

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.GraduationDepartment
import com.qawmialumni.app.domain.model.KhedmatCategory
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import com.qawmialumni.app.presentation.theme.HeritageGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfProfileEditDialog(
    viewModel: AlbumViewModel,
    onDismiss: () -> Unit,
) {
    val currentProfile by viewModel.currentSelfProfile.collectAsState()
    val saveState by viewModel.selfProfileState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSelfProfile()
    }

    LaunchedEffect(saveState) {
        if (saveState is Resource.Success) {
            viewModel.resetSelfProfileState()
            onDismiss()
        }
    }

    val profile = currentProfile

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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "আমার প্রোফাইল ও ডিজিটাল আইডি",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                )
                Text(
                    text = "আপনার বর্তমান খেদমত, ফোন ও তথ্যাবলী হালনাগাদ করুন",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (profile == null) {
                    CircularProgressIndicator(color = EmeraldPrimary, modifier = Modifier.padding(24.dp))
                } else {
                    // Photo picker / current photo
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
                        when {
                            newPhotoUri != null -> {
                                AsyncImage(
                                    model = newPhotoUri,
                                    contentDescription = "New Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                            profile.photoUrl.isNotBlank() -> {
                                AsyncImage(
                                    model = profile.photoUrl,
                                    contentDescription = "Current Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                            else -> {
                                Text("ছবি পরিবর্তন", fontSize = 11.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("পূর্ণ নাম *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fatherName,
                        onValueChange = { fatherName = it },
                        label = { Text("পিতার নাম") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("ফোন নম্বর") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(10.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(
                            value = whatsApp,
                            onValueChange = { whatsApp = it },
                            label = { Text("হোয়াটসঅ্যাপ") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(10.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = deptExpanded,
                        onExpandedChange = { deptExpanded = !deptExpanded },
                    ) {
                        OutlinedTextField(
                            value = selectedDepartment.banglaName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("ফারেগ হওয়ার বিভাগ") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(10.dp),
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

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = batchHijri,
                            onValueChange = { batchHijri = it },
                            label = { Text("হিজরি সাল") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(
                            value = bloodGroup,
                            onValueChange = { bloodGroup = it.uppercase() },
                            label = { Text("রক্তের গ্রুপ") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = supervisedUstad,
                        onValueChange = { supervisedUstad = it },
                        label = { Text("মুরব্বি / তত্ত্বাবধায়ক উস্তাদ") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = catExpanded,
                        onExpandedChange = { catExpanded = !catExpanded },
                    ) {
                        OutlinedTextField(
                            value = selectedCategory.banglaName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("বর্তমান খেদমতের ক্ষেত্র") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(10.dp),
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

                    OutlinedTextField(
                        value = currentKhedmat,
                        onValueChange = { currentKhedmat = it },
                        label = { Text("বর্তমান দায়িত্ব ও মাদরাসা/প্রতিষ্ঠান") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text("নিজ জেলা") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("সংক্ষিপ্ত পরিচিতি") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp),
                    )

                    if (saveState is Resource.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (saveState as Resource.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text("বাতিল") }
                        Spacer(modifier = Modifier.width(8.dp))
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
                            enabled = saveState !is Resource.Loading,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            if (saveState is Resource.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            } else {
                                Text("আপডেট করুন")
                            }
                        }
                    }
                }
            }
        }
    }
}
