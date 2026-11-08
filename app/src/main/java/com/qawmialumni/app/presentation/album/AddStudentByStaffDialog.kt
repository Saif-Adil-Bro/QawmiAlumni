package com.qawmialumni.app.presentation.album

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.GraduationDepartment
import com.qawmialumni.app.domain.model.KhedmatCategory
import com.qawmialumni.app.presentation.theme.EmeraldPrimary

/**
 * Flow 2 (Direct Creation by Admin/Staff - Blueprint §5.B):
 * Creates student profile directly with status = 'approved'.
 * If the student later registers with the same phone number, their account automatically links!
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentByStaffDialog(
    viewModel: AlbumViewModel,
    onDismiss: () -> Unit,
) {
    val addState by viewModel.addStudentState.collectAsState()

    var name by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsApp by remember { mutableStateOf("") }
    var dakhilaNumber by remember { mutableStateOf("") }
    var selectedDepartment by remember { mutableStateOf(GraduationDepartment.DAWRA_HADITH) }
    var batchHijri by remember { mutableStateOf("") }
    var batchEng by remember { mutableStateOf("") }
    var supervisedUstadName by remember { mutableStateOf("") }
    var currentKhedmat by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(KhedmatCategory.TEACHING) }
    var bloodGroup by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    var deptExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> photoUri = uri },
    )

    LaunchedEffect(addState) {
        if (addState is Resource.Success) {
            viewModel.resetAddStudentState()
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
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
                    text = "নতুন ফারেগ ছাত্র প্রোফাইল যোগ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                )
                Text(
                    text = "সরাসরি অনুমোদিত (Approved) প্রোফাইল তৈরি হবে",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (photoUri != null) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text("ছবি দিন", fontSize = 11.sp, color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
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

                // Department Selector
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
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("ফোন নম্বর *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(10.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = batchHijri,
                        onValueChange = { batchHijri = it },
                        label = { Text("হিজরি সাল *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dakhilaNumber,
                        onValueChange = { dakhilaNumber = it },
                        label = { Text("দাখেলা নম্বর") },
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
                    value = supervisedUstadName,
                    onValueChange = { supervisedUstadName = it },
                    label = { Text("মুরব্বি / তত্ত্বাবধায়ক উস্তাদ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Khedmat Category Selector
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                ) {
                    OutlinedTextField(
                        value = selectedCategory.banglaName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("খেদমতের ধরন") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(10.dp),
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                    ) {
                        KhedmatCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.banglaName) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                },
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = currentKhedmat,
                    onValueChange = { currentKhedmat = it },
                    label = { Text("বর্তমান খেদমত ও প্রতিষ্ঠান") },
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

                if (addState is Resource.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (addState as Resource.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.addStudentByStaff(
                                name = name,
                                fatherName = fatherName,
                                phone = phone,
                                whatsApp = whatsApp,
                                dakhilaNumber = dakhilaNumber,
                                department = selectedDepartment,
                                batchHijri = batchHijri,
                                batchEng = batchEng,
                                supervisedUstadName = supervisedUstadName,
                                currentKhedmat = currentKhedmat,
                                khedmatCategory = selectedCategory,
                                bloodGroup = bloodGroup,
                                district = district,
                                bio = bio,
                                photoUri = photoUri,
                            )
                        },
                        enabled = addState !is Resource.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        if (addState is Resource.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("সংরক্ষণ করুন")
                        }
                    }
                }
            }
        }
    }
}
