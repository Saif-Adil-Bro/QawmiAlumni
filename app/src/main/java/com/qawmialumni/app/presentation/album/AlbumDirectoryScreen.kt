package com.qawmialumni.app.presentation.album

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qawmialumni.app.domain.model.GraduationDepartment
import com.qawmialumni.app.domain.model.KhedmatCategory
import com.qawmialumni.app.domain.model.UserProfile
import com.qawmialumni.app.domain.model.UserRole
import com.qawmialumni.app.presentation.components.AvatarWithRing
import com.qawmialumni.app.presentation.components.BloodGroupPill
import com.qawmialumni.app.presentation.components.DepartmentBadge
import com.qawmialumni.app.presentation.components.ModernEmptyState
import com.qawmialumni.app.presentation.components.QawmiIcons
import com.qawmialumni.app.presentation.components.departmentColor
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import com.qawmialumni.app.presentation.theme.HeritageGold

@Composable
fun AlbumDirectoryScreen(
    showStaffTools: Boolean,
    viewModel: AlbumViewModel = hiltViewModel(),
) {
    val alumniList by viewModel.alumniList.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val batches by viewModel.batches.collectAsState()
    val ustadNames by viewModel.ustadNames.collectAsState()
    val session by viewModel.session.collectAsState()

    LaunchedEffect(session?.madrasaId) {
        val madrasaId = session?.madrasaId.orEmpty()
        if (madrasaId.isNotBlank()) {
            viewModel.triggerSync(madrasaId)
        }
    }

    var selectedAlumniForDetail by remember { mutableStateOf<UserProfile?>(null) }
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var showAdvancedFilters by remember { mutableStateOf(false) }

    val bloodGroups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Search & Filter Bar Surface
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 6.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = filterState.query,
                            onValueChange = { viewModel.updateQuery(it) },
                            placeholder = {
                                Text(
                                    "নাম, পিতা, দাখেলা নং, জেলা বা খেদমত খুঁজুন...",
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = EmeraldPrimary,
                                )
                            },
                            trailingIcon = {
                                if (filterState.query.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateQuery("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Filter Drawer Toggle Button
                        Surface(
                            modifier = Modifier
                                .height(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { showAdvancedFilters = !showAdvancedFilters },
                            color = if (showAdvancedFilters || filterState.hasActiveFilters) {
                                EmeraldPrimary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = QawmiIcons.FilterSliders,
                                    contentDescription = "Filters",
                                    tint = if (showAdvancedFilters || filterState.hasActiveFilters) {
                                        Color.White
                                    } else {
                                        EmeraldPrimary
                                    },
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ফিল্টার",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (showAdvancedFilters || filterState.hasActiveFilters) {
                                        Color.White
                                    } else {
                                        EmeraldPrimary
                                    },
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Row 1 (Always visible quick chips): Reset + Batch + Department
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (filterState.hasActiveFilters) {
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.resetFilters() },
                                label = { Text("✕ সব মুছুন", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF475569),
                                    selectedLabelColor = Color.White,
                                ),
                            )
                        }

                        batches.forEach { batch ->
                            FilterChip(
                                selected = filterState.batch == batch,
                                onClick = { viewModel.selectBatch(batch) },
                                label = { Text("$batch হি.", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.White,
                                ),
                            )
                        }

                        GraduationDepartment.entries.forEach { dept ->
                            FilterChip(
                                selected = filterState.department == dept.wireValue,
                                onClick = { viewModel.selectDepartment(dept.wireValue) },
                                label = { Text(dept.banglaName, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HeritageGold,
                                    selectedLabelColor = Color.White,
                                ),
                            )
                        }
                    }

                    // Collapsible Row 2: Khedmat Category, Supervised Ustad & Emergency Blood Donor Chips
                    AnimatedVisibility(
                        visible = showAdvancedFilters ||
                            filterState.khedmatCategory.isNotBlank() ||
                            filterState.ustadName.isNotBlank() ||
                            filterState.bloodGroup.isNotBlank(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 14.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            KhedmatCategory.entries.forEach { cat ->
                                FilterChip(
                                    selected = filterState.khedmatCategory == cat.wireValue,
                                    onClick = { viewModel.selectKhedmatCategory(cat.wireValue) },
                                    label = { Text(cat.banglaName, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF047857),
                                        selectedLabelColor = Color.White,
                                    ),
                                )
                            }

                            ustadNames.forEach { ustadName ->
                                FilterChip(
                                    selected = filterState.ustadName == ustadName,
                                    onClick = { viewModel.selectUstadName(ustadName) },
                                    label = { Text("উস্তাদ: $ustadName", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF0F766E),
                                        selectedLabelColor = Color.White,
                                    ),
                                )
                            }

                            bloodGroups.forEach { bg ->
                                FilterChip(
                                    selected = filterState.bloodGroup == bg,
                                    onClick = { viewModel.selectBloodGroup(bg) },
                                    label = { Text("রক্ত: $bg", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFDC2626),
                                        selectedLabelColor = Color.White,
                                    ),
                                )
                            }
                        }
                    }

                    // Count & Sync Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "মোট প্রদর্শিত ফারেগীন: ${alumniList.size} জন",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextButton(
                            onClick = {
                                session?.madrasaId?.takeIf { it.isNotBlank() }?.let {
                                    viewModel.triggerSync(it)
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(15.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("সিঙ্ক করুন", fontSize = 12.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

            // 2-Column Sleek Album Grid View (Blueprint §7.1)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 88.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (alumniList.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        ModernEmptyState(
                            icon = QawmiIcons.KitabBook,
                            title = "কোনো ফারেগীন প্রোফাইল পাওয়া যায়নি",
                            subtitle = if (filterState.hasActiveFilters) {
                                "আপনার নির্বাচিত ফিল্টার বা সার্চের সাথে মিল পাওয়া যায়নি।"
                            } else {
                                "এই মাদরাসার অ্যালবামে এখনও কোনো ভেরিফায়েড প্রোফাইল যুক্ত হয়নি।"
                            },
                            action = if (filterState.hasActiveFilters) {
                                {
                                    TextButton(onClick = { viewModel.resetFilters() }) {
                                        Text("সব ফিল্টার মুছে ফেলুন", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                null
                            },
                        )
                    }
                } else {
                    items(alumniList, key = { it.uid }) { student ->
                        AlumniGridCard(
                            student = student,
                            onClick = { selectedAlumniForDetail = student },
                        )
                    }
                }
            }
        }

        // Staff / Admin Direct Add Student Extended FAB
        if (showStaffTools) {
            ExtendedFloatingActionButton(
                onClick = { showAddStudentDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = HeritageGold,
                contentColor = Color.White,
                shape = RoundedCornerShape(50),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Student",
                    )
                },
                text = {
                    Text("ফারেগ যুক্ত করুন", fontWeight = FontWeight.Bold)
                },
            )
        }
    }

    selectedAlumniForDetail?.let { alumni ->
        val isViewerMadrasaAdmin = session?.role == UserRole.MADRASA_ADMIN || session?.role == UserRole.SUPER_ADMIN
        val currentViewerUid = session?.uid.orEmpty()

        AlumniDetailSheet(
            alumni = alumni,
            currentViewerUid = currentViewerUid,
            isViewerMadrasaAdmin = isViewerMadrasaAdmin,
            onToggleStaffRole = {
                viewModel.toggleStaffRole(alumni) {
                    selectedAlumniForDetail = null
                }
            },
            onSendDirectMessage = { msgText, onSent ->
                viewModel.sendDirectMessageToAlumni(alumni, msgText, onSent)
            },
            onDismiss = { selectedAlumniForDetail = null },
        )
    }

    if (showAddStudentDialog) {
        AddStudentByStaffDialog(
            viewModel = viewModel,
            onDismiss = { showAddStudentDialog = false },
        )
    }
}

/**
 * Sanad-style 2-column card grid item with department color ribbon, gold-ringed avatar,
 * batch pill, Khedmat subtitle, and blood/district footer.
 */
@Composable
fun AlumniGridCard(
    student: UserProfile,
    onClick: () -> Unit,
) {
    val deptAccent = departmentColor(student.department.wireValue)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top Department Color Ribbon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(deptAccent),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AvatarWithRing(
                    photoUrl = student.photoUrl,
                    name = student.name,
                    size = 62.dp,
                    ringColor = HeritageGold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(4.dp))

                DepartmentBadge(
                    label = "${student.batchYearHijri} হি. • ${student.department.banglaName}",
                    departmentWireValue = student.department.wireValue,
                )

                if (student.role == UserRole.STAFF) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = HeritageGold.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            text = "★ ব্যাচ জিম্মাদার",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = HeritageGold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }

                if (student.currentKhedmat.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = student.currentKhedmat,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    BloodGroupPill(bloodGroup = student.bloodGroup)
                    if (student.district.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp),
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = student.district,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}
