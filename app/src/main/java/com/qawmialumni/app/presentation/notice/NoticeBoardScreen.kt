package com.qawmialumni.app.presentation.notice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.Notice
import com.qawmialumni.app.presentation.madrasaadmin.MadrasaAdminViewModel
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import com.qawmialumni.app.presentation.theme.HeritageGold

@Composable
fun NoticeBoardScreen(
    adminViewModel: MadrasaAdminViewModel = hiltViewModel(),
) {
    val noticesResource by adminViewModel.noticesList.collectAsState()

    SurfaceBox(noticesResource = noticesResource)
}

@Composable
private fun SurfaceBox(noticesResource: Resource<List<Notice>>) {
    when (noticesResource) {
        is Resource.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EmeraldPrimary)
            }
        }
        is Resource.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = noticesResource.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is Resource.Success -> {
            if (noticesResource.data.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "এখনও কোনো নোটিশ প্রকাশিত হয়নি।",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(noticesResource.data, key = { it.noticeId }) { notice ->
                        PublicNoticeCard(notice = notice)
                    }
                }
            }
        }
    }
}

@Composable
fun PublicNoticeCard(notice: Notice) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
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
                    text = "তারিখ/অনুষ্ঠান: ${notice.eventDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = HeritageGold,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notice.details,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
