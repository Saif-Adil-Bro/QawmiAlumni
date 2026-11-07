package com.qawmialumni.app.presentation.album

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.qawmialumni.app.domain.model.UserProfile
import com.qawmialumni.app.presentation.components.BloodGroupPill
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

    var selectedAlumniForDetail by remember { mutableStateOf<UserProfile?>(null) }
    var showAddStudentDialog by remember { mutableStateOf(false) }

    val bloodGroups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Input
            OutlinedTextField(
                value = filterState.query,
                onValueChange = { viewModel.updateQuery(it) },
                placeholder = { Text("নাম, পিতা বা খেদমত দিয়ে খুঁজুন...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )

            // Smart Filter Chips Bar (Blueprint §7)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Batch chips
                batches.forEach { batch ->
                    FilterChip(
                        selected = filterState.batch == batch,
                        onClick = { viewModel.selectBatch(batch) },
                        label = { Text("$batch হিজরি") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White,
                        ),
                    )
                }

                // Blood Group chips (Emergency Donor Finder)
                bloodGroups.forEach { bg ->
                    FilterChip(
                        selected = filterState.bloodGroup == bg,
                        onClick = { viewModel.selectBloodGroup(bg) },
                        label = { Text("রক্ত: $bg") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFDC2626),
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
            }

            // 2-Column Sleek Album Grid View (Blueprint §7.1)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (alumniList.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "কোনো আলামনাই প্রোফাইল পাওয়া যায়নি।",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
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

        // Staff Direct Add Student FAB
        if (showStaffTools) {
            FloatingActionButton(
                onClick = { showAddStudentDialog = true },
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

    selectedAlumniForDetail?.let { alumni ->
        AlumniDetailSheet(
            alumni = alumni,
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
 * 2-column sleek card grid item showing rounded photo, name, Hijri batch badge,
 * Blood group pill, and current Khedmat (Blueprint §7.1).
 */
@Composable
fun AlumniGridCard(
    student: UserProfile,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary.copy(alpha = 0.1f)),
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
                    Text(
                        text = student.name.take(1),
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        fontSize = 24.sp,
                    )
                }
            }

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

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${student.batchYearHijri} হিজরি",
                style = MaterialTheme.typography.bodySmall,
                color = HeritageGold,
                fontWeight = FontWeight.SemiBold,
            )

            if (student.currentKhedmat.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = student.currentKhedmat,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            BloodGroupPill(bloodGroup = student.bloodGroup)
        }
    }
}
