package com.qawmialumni.app.presentation.madrasaadmin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.Notice
import com.qawmialumni.app.domain.model.UserProfile
import com.qawmialumni.app.domain.model.Ustad
import com.qawmialumni.app.domain.model.UstadHonorific
import com.qawmialumni.app.presentation.components.BloodGroupPill
import com.qawmialumni.app.presentation.components.ModernEmptyState
import com.qawmialumni.app.presentation.components.QawmiIcons
import com.qawmialumni.app.presentation.components.StatSummaryCard
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import com.qawmialumni.app.presentation.theme.EmeraldTeal
import com.qawmialumni.app.presentation.theme.HeritageGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MadrasaAdminPanel(
    viewModel: MadrasaAdminViewModel = hiltViewModel(),
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val actionState by viewModel.actionState.collectAsState()
    val pendingRes by viewModel.pendingRequests.collectAsState()
    val ustadsRes by viewModel.ustadsList.collectAsState()
    val noticesRes by viewModel.noticesList.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddUstadDialog by remember { mutableStateOf(false) }
    var showCreateNoticeDialog by remember { mutableStateOf(false) }

    val pendingCount = (pendingRes as? Resource.Success)?.data?.size ?: 0
    val ustadCount = (ustadsRes as? Resource.Success)?.data?.size ?: 0
    val noticeCount = (noticesRes as? Resource.Success)?.data?.size ?: 0

    LaunchedEffect(actionState) {
        when (actionState) {
            is ActionState.Success -> {
                snackbarHostState.showSnackbar((actionState as ActionState.Success).message)
                viewModel.resetActionState()
            }
            is ActionState.Error -> {
                snackbarHostState.showSnackbar((actionState as ActionState.Error).message)
                viewModel.resetActionState()
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Admin KPI Summary Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatSummaryCard(
                    title = "পেন্ডিং আবেদন",
                    value = "$pendingCount জন",
                    accentColor = HeritageGold,
                    modifier = Modifier.weight(1f),
                )
                StatSummaryCard(
                    title = "আসাতিযায়ে কেরাম",
                    value = "$ustadCount জন",
                    accentColor = EmeraldPrimary,
                    modifier = Modifier.weight(1f),
                )
                StatSummaryCard(
                    title = "প্রকাশিত নোটিশ",
                    value = "$noticeCount টি",
                    accentColor = EmeraldTeal,
                    modifier = Modifier.weight(1f),
                )
            }

            PrimaryTabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = EmeraldPrimary,
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = {
                        Text(
                            if (pendingCount > 0) "পেন্ডিং ($pendingCount)" else "পেন্ডিং আবেদন",
                            fontWeight = FontWeight.Bold,
                        )
                    },
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = { Text("আসাতিযায়ে কেরাম", fontWeight = FontWeight.Bold) },
                )
                Tab(
                    selected = selectedSubTab == 2,
                    onClick = { selectedSubTab = 2 },
                    text = { Text("নোটিশ প্রকাশ", fontWeight = FontWeight.Bold) },
                )
            }

            when (selectedSubTab) {
                0 -> PendingRequestsSection(viewModel = viewModel)
                1 -> UstadsManagementSection(
                    viewModel = viewModel,
                    onAddUstadClick = { showAddUstadDialog = true },
                )
                2 -> NoticesManagementSection(
                    viewModel = viewModel,
                    onCreateNoticeClick = { showCreateNoticeDialog = true },
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (showAddUstadDialog) {
        AddUstadDialog(
            viewModel = viewModel,
            onDismiss = { showAddUstadDialog = false },
        )
    }

    if (showCreateNoticeDialog) {
        CreateNoticeDialog(
            viewModel = viewModel,
            onDismiss = { showCreateNoticeDialog = false },
        )
    }
}

// ---------------------------------------------------------------------------
// Section 1: Pending Join Requests
// ---------------------------------------------------------------------------
@Composable
fun PendingRequestsSection(viewModel: MadrasaAdminViewModel) {
    val requestsResource by viewModel.pendingRequests.collectAsState()

    when (val res = requestsResource) {
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ModernEmptyState(
                        icon = QawmiIcons.AdminShield,
                        title = "নতুন কোনো পেন্ডিং আবেদন নেই",
                        subtitle = "কোনো ছাত্র বা ফারেগ মাদরাসা কোড দিয়ে আবেদন করলে তা যাচাইয়ের জন্য এখানে প্রদর্শিত হবে।",
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(res.data, key = { it.uid }) { student ->
                        PendingStudentCard(
                            student = student,
                            onApprove = { viewModel.approveStudent(student.uid) },
                            onReject = { viewModel.rejectStudent(student.uid) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PendingStudentCard(
    student: UserProfile,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    if (student.photoUrl.isNotBlank()) {
                        AsyncImage(
                            model = student.photoUrl,
                            contentDescription = student.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text(student.name.take(1), fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                    )
                    if (student.fatherName.isNotBlank()) {
                        Text(
                            text = "পিতা: ${student.fatherName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = "${student.department.banglaName} • ${student.batchYearHijri} হি. | দাখেলা: ${student.dakhilaNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = HeritageGold,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (student.phone.isNotBlank()) {
                        Text(
                            text = "📞 ${student.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                BloodGroupPill(bloodGroup = student.bloodGroup)
            }

            if (student.currentKhedmat.isNotBlank() || student.supervisedByUstadName.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                if (student.supervisedByUstadName.isNotBlank()) {
                    Text(
                        text = "মুরব্বি উস্তাদ: ${student.supervisedByUstadName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                if (student.currentKhedmat.isNotBlank()) {
                    Text(
                        text = "বর্তমান খেদমত: ${student.currentKhedmat}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("বাতিল (Reject)", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("অনুমোদন (Approve)", fontSize = 12.sp)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Section 2: Manage Ustads
// ---------------------------------------------------------------------------
@Composable
fun UstadsManagementSection(
    viewModel: MadrasaAdminViewModel,
    onAddUstadClick: () -> Unit,
) {
    val ustadsResource by viewModel.ustadsList.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val res = ustadsResource) {
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
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("এখনও কোনো উস্তাদের তথ্য যুক্ত করা হয়নি।")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(res.data, key = { it.ustadId }) { ustad ->
                            UstadItemCard(ustad = ustad)
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddUstadClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = HeritageGold,
            contentColor = Color.White,
            shape = CircleShape,
        ) {
            Text("+", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun UstadItemCard(ustad: Ustad) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                if (ustad.photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = ustad.photoUrl,
                        contentDescription = ustad.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Text(ustad.name.take(1), fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ustad.displayNameWithHonorific,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                )
                Text(
                    text = ustad.designation,
                    style = MaterialTheme.typography.bodySmall,
                    color = HeritageGold,
                    fontWeight = FontWeight.SemiBold,
                )
                if (ustad.specialization.isNotBlank()) {
                    Text(
                        text = "দরস / বিশেষত্ব: ${ustad.specialization}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Surface(
                color = if (ustad.honorific == UstadHonorific.DAMAT_BARAKATUHUM) {
                    EmeraldPrimary.copy(alpha = 0.12f)
                } else {
                    HeritageGold.copy(alpha = 0.16f)
                },
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = ustad.honorific.banglaSuffix,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (ustad.honorific == UstadHonorific.DAMAT_BARAKATUHUM) EmeraldPrimary else HeritageGold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUstadDialog(
    viewModel: MadrasaAdminViewModel,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var selectedHonorific by remember { mutableStateOf(UstadHonorific.DAMAT_BARAKATUHUM) }
    var honorificExpanded by remember { mutableStateOf(false) }
    var designation by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> photoUri = uri },
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
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
                    text = "উস্তাদের তথ্য যুক্ত করুন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                )
                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .size(68.dp)
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
                        Text("ছবি দিন", fontSize = 11.sp, color = EmeraldPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("উস্তাদের নাম * (যেমন: আল্লামা ...)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Honorific Dropdown: (দা.বা.) vs (রহ.)
                ExposedDropdownMenuBox(
                    expanded = honorificExpanded,
                    onExpandedChange = { honorificExpanded = !honorificExpanded },
                ) {
                    OutlinedTextField(
                        value = "${selectedHonorific.banglaSuffix} • ${selectedHonorific.arabicSuffix}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("সম্মানসূচক উপাধি (দা.বা. / রহ.)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = honorificExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(10.dp),
                    )
                    ExposedDropdownMenu(
                        expanded = honorificExpanded,
                        onDismissRequest = { honorificExpanded = false },
                    ) {
                        UstadHonorific.entries.forEach { hon ->
                            DropdownMenuItem(
                                text = { Text("${hon.banglaSuffix} • ${hon.arabicSuffix}") },
                                onClick = {
                                    selectedHonorific = hon
                                    honorificExpanded = false
                                },
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = { Text("পদবী * (যেমন: শায়খুল হাদিস ও মুহতামিম)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = specialization,
                    onValueChange = { specialization = it },
                    label = { Text("পাঠদানকৃত কিতাব / বিশেষত্ব (যেমন: সহীহ বুখারী)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("সংক্ষিপ্ত জীবনী / পরিচিতি") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp),
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.addUstad(
                                name = name,
                                honorific = selectedHonorific,
                                designation = designation,
                                specialization = specialization,
                                bio = bio,
                                photoUri = photoUri,
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text("যুক্ত করুন")
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Section 3: Manage Notices
// ---------------------------------------------------------------------------
@Composable
fun NoticesManagementSection(
    viewModel: MadrasaAdminViewModel,
    onCreateNoticeClick: () -> Unit,
) {
    val noticesResource by viewModel.noticesList.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val res = noticesResource) {
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
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("এখনও কোনো নোটিশ প্রকাশ করা হয়নি।")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(res.data, key = { it.noticeId }) { notice ->
                            NoticeItemCard(notice = notice)
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onCreateNoticeClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = HeritageGold,
            contentColor = Color.White,
            shape = CircleShape,
        ) {
            Text("+", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NoticeItemCard(notice: Notice) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (notice.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = notice.imageUrl,
                    contentDescription = notice.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = notice.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary,
            )
            if (notice.eventDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📅 তারিখ/অনুষ্ঠান: ${notice.eventDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = HeritageGold,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notice.details,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "অংশগ্রহণেচ্ছুক সাথী (RSVP): ${notice.rsvpUids.size} জন",
                style = MaterialTheme.typography.bodySmall,
                color = EmeraldPrimary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun CreateNoticeDialog(
    viewModel: MadrasaAdminViewModel,
    onDismiss: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri },
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
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
                    text = "নতুন নোটিশ / দস্তারবন্দী ঘোষণা",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                )
                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Notice Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text("নোটিশের ছবি / ব্যানার যোগ করুন (ঐচ্ছিক)", fontSize = 12.sp, color = EmeraldPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("নোটিশের শিরোনাম *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = eventDate,
                    onValueChange = { eventDate = it },
                    label = { Text("তারিখ / অনুষ্ঠান (যেমন: ২৫ রজব ১৪৪৬ হিজরি)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("বিস্তারিত বিবরণ *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 6,
                    shape = RoundedCornerShape(10.dp),
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.postNotice(
                                title = title,
                                details = details,
                                eventDate = eventDate,
                                imageUri = imageUri,
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text("প্রকাশ করুন")
                    }
                }
            }
        }
    }
}
