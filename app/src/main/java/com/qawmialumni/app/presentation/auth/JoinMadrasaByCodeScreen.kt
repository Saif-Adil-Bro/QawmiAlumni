package com.qawmialumni.app.presentation.auth

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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.qawmialumni.app.domain.model.GraduationDepartment
import com.qawmialumni.app.domain.model.KhedmatCategory
import com.qawmialumni.app.presentation.components.IslamicGeometricBanner
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import com.qawmialumni.app.presentation.theme.HeritageGold
import com.qawmialumni.app.presentation.theme.HeritageGoldLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinMadrasaByCodeScreen(
    viewModel: JoinMadrasaViewModel,
    initialName: String = "",
    initialPhone: String = "",
    onSubmitSuccess: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()

    var madrasaCode by remember { mutableStateOf("") }

    // Profile form fields
    var name by remember { mutableStateOf(initialName) }
    var fatherName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(initialPhone) }
    var whatsApp by remember { mutableStateOf(initialPhone) }
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

    var departmentDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> photoUri = uri },
    )

    LaunchedEffect(uiState) {
        if (uiState is JoinUiState.SubmittedSuccess) {
            onSubmitSuccess()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IslamicGeometricBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, bottom = 28.dp, start = 20.dp, end = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Surface(
                        color = HeritageGold.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            text = "ধাপ ২ / ২ • মাদরাসা ও সনদ তথ্য",
                            color = HeritageGoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "মাদরাসা নির্বাচন ও ফারেগীন নিবন্ধন",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "আপনার মাদরাসার কোড দিয়ে যুক্ত হোন এবং দস্তারবন্দী/ফারেগীন তথ্য পূরণ করুন",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Step A: Madrasa Code Verification Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(3.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "১. মাদরাসা কোড যাচাই (Madrasa Code)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = madrasaCode,
                            onValueChange = { madrasaCode = it.uppercase() },
                            placeholder = { Text("যেমন: DARUL-ULOOM-01") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.verifyCode(madrasaCode) },
                            enabled = uiState !is JoinUiState.Loading,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(54.dp),
                        ) {
                            Text("যাচাই করুন", fontWeight = FontWeight.Bold)
                        }
                    }

                    viewModel.verifiedMadrasa?.let { madrasa ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = EmeraldPrimary.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(HeritageGold, shape = CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = madrasa.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary,
                                    )
                                    if (madrasa.address.isNotBlank()) {
                                        Text(
                                            text = madrasa.address,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Step B: Form (Visible only if verified)
            if (viewModel.verifiedMadrasa != null) {
                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "২. ব্যক্তিগত ও শিক্ষাবর্ষের তথ্য",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                            modifier = Modifier.align(Alignment.Start),
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Photo picker preview
                        Box(
                            modifier = Modifier
                                .size(96.dp)
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
                            if (photoUri != null) {
                                AsyncImage(
                                    model = photoUri,
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("ছবি দিন", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                    Text("(WebP Auto)", fontSize = 10.sp, color = HeritageGold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("পূর্ণ নাম *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = fatherName,
                            onValueChange = { fatherName = it },
                            label = { Text("পিতার নাম") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("ফোন নম্বর *") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(12.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = whatsApp,
                                onValueChange = { whatsApp = it },
                                label = { Text("হোয়াটসঅ্যাপ") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(12.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Graduation Department Dropdown (দাওরায়ে হাদিস / ইফতা / তাফসীর...)
                        ExposedDropdownMenuBox(
                            expanded = departmentDropdownExpanded,
                            onExpandedChange = { departmentDropdownExpanded = !departmentDropdownExpanded },
                        ) {
                            OutlinedTextField(
                                value = selectedDepartment.banglaName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("ফারেগ হওয়ার বিভাগ (Department) *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = departmentDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                shape = RoundedCornerShape(12.dp),
                            )
                            ExposedDropdownMenu(
                                expanded = departmentDropdownExpanded,
                                onDismissRequest = { departmentDropdownExpanded = false },
                            ) {
                                GraduationDepartment.entries.forEach { dept ->
                                    DropdownMenuItem(
                                        text = { Text(dept.banglaName) },
                                        onClick = {
                                            selectedDepartment = dept
                                            departmentDropdownExpanded = false
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
                                label = { Text("হিজরি শিক্ষাবর্ষ (যেমন: ১৪৪৪) *") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = batchEng,
                                onValueChange = { batchEng = it },
                                label = { Text("ইংরেজি সাল (যেমন: ২০২৩)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = dakhilaNumber,
                                onValueChange = { dakhilaNumber = it },
                                label = { Text("দাখেলা / রোল নম্বর") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = bloodGroup,
                                onValueChange = { bloodGroup = it.uppercase() },
                                label = { Text("রক্তের গ্রুপ (যেমন: O+)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = supervisedUstadName,
                            onValueChange = { supervisedUstadName = it },
                            label = { Text("মুরব্বি / তত্ত্বাবধায়ক উস্তাদ") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Khedmat Category Dropdown
                        ExposedDropdownMenuBox(
                            expanded = categoryDropdownExpanded,
                            onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                        ) {
                            OutlinedTextField(
                                value = selectedCategory.banglaName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("বর্তমান খেদমতের ক্ষেত্র") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                shape = RoundedCornerShape(12.dp),
                            )
                            ExposedDropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false },
                            ) {
                                KhedmatCategory.entries.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.banglaName) },
                                        onClick = {
                                            selectedCategory = cat
                                            categoryDropdownExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = currentKhedmat,
                            onValueChange = { currentKhedmat = it },
                            label = { Text("বর্তমান দায়িত্ব ও মাদরাসা/মসজিদের নাম") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = district,
                            onValueChange = { district = it },
                            label = { Text("নিজ জেলা") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("সংক্ষিপ্ত পরিচিতি / দোয়া কামনা") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                }

                if (uiState is JoinUiState.Error) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = (uiState as JoinUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        viewModel.submitProfile(
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = uiState !is JoinUiState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    if (uiState is JoinUiState.Loading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (uploadProgress) "ছবি কম্প্রেস ও আপলোড হচ্ছে..." else "জমা দেওয়া হচ্ছে...")
                        }
                    } else {
                        Text("আবেদন জমা দিন", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
