package com.qawmialumni.app.presentation.superadmin

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.Madrasa
import com.qawmialumni.app.presentation.components.AvatarWithRing
import com.qawmialumni.app.presentation.components.ModernEmptyState
import com.qawmialumni.app.presentation.components.QawmiIcons
import com.qawmialumni.app.presentation.components.StatSummaryCard
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import com.qawmialumni.app.presentation.theme.EmeraldTeal
import com.qawmialumni.app.presentation.theme.HeritageGold
import com.qawmialumni.app.presentation.theme.HeritageGoldLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminDashboardScreen(
    viewModel: SuperAdminViewModel = hiltViewModel(),
    onSignOut: () -> Unit,
) {
    val madrasasResource by viewModel.madrasasList.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedMadrasaForAdminAssign by remember { mutableStateOf<Madrasa?>(null) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = QawmiIcons.AdminShield,
                                    contentDescription = null,
                                    tint = HeritageGoldLight,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("সুপার অ্যাডমিন ড্যাশবোর্ড", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("মাদরাসা ও প্ল্যাটফর্ম ম্যানেজমেন্ট", fontSize = 11.sp, color = HeritageGoldLight)
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onSignOut,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(HeritageGold),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "লগআউট",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = EmeraldPrimary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
                HorizontalDivider(thickness = 1.5.dp, color = HeritageGold.copy(alpha = 0.45f))
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = HeritageGold,
                contentColor = Color.White,
                shape = RoundedCornerShape(50),
                icon = {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Madrasa")
                },
                text = {
                    Text("নতুন মাদরাসা যুক্ত করুন", fontWeight = FontWeight.Bold)
                },
            )
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            when (val res = madrasasResource) {
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
                                title = "এখনও কোনো মাদরাসা নিবন্ধন করা হয়নি",
                                subtitle = "নিচের 'নতুন মাদরাসা যুক্ত করুন' বাটনে চাপ দিয়ে প্রথম মাদরাসা ও তার অ্যাডমিন ইমেইল যুক্ত করুন।",
                            )
                        }
                    } else {
                        val totalMadrasas = res.data.size
                        val totalAdminsAssigned = res.data.count {
                            it.adminUids.isNotEmpty() || it.pendingAdminEmails.isNotEmpty()
                        }
                        val totalAlumni = res.data.sumOf { it.totalAlumniCount }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 88.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    StatSummaryCard(
                                        title = "নিবন্ধিত মাদরাসা",
                                        value = "$totalMadrasas টি",
                                        accentColor = EmeraldPrimary,
                                        modifier = Modifier.weight(1f),
                                    )
                                    StatSummaryCard(
                                        title = "অ্যাডমিনযুক্ত",
                                        value = "$totalAdminsAssigned টি",
                                        accentColor = HeritageGold,
                                        modifier = Modifier.weight(1f),
                                    )
                                    StatSummaryCard(
                                        title = "মোট ফারেগীন",
                                        value = "$totalAlumni জন",
                                        accentColor = EmeraldTeal,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                            items(res.data, key = { it.madrasaId }) { madrasa ->
                                MadrasaAdminCard(
                                    madrasa = madrasa,
                                    onAssignAdminClick = { selectedMadrasaForAdminAssign = madrasa },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateMadrasaDialog(
            viewModel = viewModel,
            onDismiss = { showCreateDialog = false },
        )
    }

    selectedMadrasaForAdminAssign?.let { madrasa ->
        AssignMadrasaAdminDialog(
            madrasa = madrasa,
            viewModel = viewModel,
            onDismiss = { selectedMadrasaForAdminAssign = null },
        )
    }
}

@Composable
fun MadrasaAdminCard(
    madrasa: Madrasa,
    onAssignAdminClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarWithRing(
                    photoUrl = madrasa.logoUrl,
                    name = madrasa.name,
                    size = 54.dp,
                    ringColor = HeritageGold,
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = madrasa.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                    )
                    if (madrasa.address.isNotBlank()) {
                        Text(
                            text = madrasa.address,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = HeritageGold.copy(alpha = 0.16f),
                        ) {
                            Text(
                                text = "কোড: ${madrasa.code}",
                                fontWeight = FontWeight.Bold,
                                color = HeritageGold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ফারেগীন: ${madrasa.totalAlumniCount} জন",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val adminInfo = when {
                    madrasa.pendingAdminEmails.isNotEmpty() ->
                        "অ্যাডমিন ইমেইল: ${madrasa.pendingAdminEmails.joinToString(", ")}"
                    madrasa.adminUids.isNotEmpty() ->
                        "সক্রিয় অ্যাডমিন: ${madrasa.adminUids.size} জন"
                    else -> "অ্যাডমিন এখনও নির্ধারণ করা হয়নি"
                }
                Text(
                    text = adminInfo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )

                OutlinedButton(
                    onClick = onAssignAdminClick,
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text("অ্যাডমিন নিয়োগ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                }
            }
        }
    }
}

@Composable
fun AssignMadrasaAdminDialog(
    madrasa: Madrasa,
    viewModel: SuperAdminViewModel,
    onDismiss: () -> Unit,
) {
    val assignState by viewModel.assignAdminState.collectAsState()
    var adminEmail by remember {
        mutableStateOf(madrasa.pendingAdminEmails.firstOrNull().orEmpty())
    }

    LaunchedEffect(assignState) {
        if (assignState is Resource.Success) {
            viewModel.resetAssignAdminState()
            onDismiss()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                Text(
                    text = "মাদরাসা অ্যাডমিন নিয়োগ / পরিবর্তন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = madrasa.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HeritageGold,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = adminEmail,
                    onValueChange = { adminEmail = it },
                    label = { Text("অ্যাডমিনের ইমেইল *") },
                    placeholder = { Text("admin@madrasa.org") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(10.dp),
                )

                if (assignState is Resource.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (assignState as Resource.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.assignAdminToMadrasa(madrasa.madrasaId, adminEmail)
                        },
                        enabled = assignState !is Resource.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        if (assignState is Resource.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Text("অ্যাডমিন নিশ্চিত করুন")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateMadrasaDialog(
    viewModel: SuperAdminViewModel,
    onDismiss: () -> Unit,
) {
    val createState by viewModel.createState.collectAsState()

    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var establishedYear by remember { mutableStateOf("") }
    var adminEmail by remember { mutableStateOf("") }
    var logoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> logoUri = uri },
    )

    LaunchedEffect(createState) {
        if (createState is CreateMadrasaUiState.Success) {
            viewModel.resetCreateState()
            onDismiss()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
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
                    text = "নতুন মাদরাসা নিবন্ধন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Logo picker
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
                    if (logoUri != null) {
                        AsyncImage(
                            model = logoUri,
                            contentDescription = "Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text("লোগো দিন", fontSize = 11.sp, color = EmeraldPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (code.isBlank()) {
                            code = viewModel.generateUniqueCode(it)
                        }
                    },
                    label = { Text("মাদরাসার নাম *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.uppercase() },
                        label = { Text("মাদরাসা কোড *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedButton(
                        onClick = { code = viewModel.generateUniqueCode(name) },
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text("জেনারেট", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("ঠিকানা / অবস্থান") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = establishedYear,
                    onValueChange = { establishedYear = it },
                    label = { Text("প্রতিষ্ঠা সাল (যেমন: ১৪০৫ হিজরি / ১৯৮৫ খ্রি.)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = adminEmail,
                    onValueChange = { adminEmail = it },
                    label = { Text("মাদরাসা অ্যাডমিনের ইমেইল") },
                    placeholder = { Text("admin@madrasa.org") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(10.dp),
                )

                if (createState is CreateMadrasaUiState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (createState as CreateMadrasaUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.createMadrasa(
                                name = name,
                                code = code,
                                address = address,
                                establishedYear = establishedYear,
                                adminEmail = adminEmail,
                                logoUri = logoUri,
                            )
                        },
                        enabled = createState !is CreateMadrasaUiState.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        if (createState is CreateMadrasaUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Text("সংরক্ষণ করুন")
                        }
                    }
                }
            }
        }
    }
}
