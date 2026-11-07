package com.qawmialumni.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qawmialumni.app.presentation.theme.HeritageGold

@Composable
fun VerifiedBadge(
    modifier: Modifier = Modifier,
    text: String = "ভেরিফায়েড",
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(HeritageGold.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "✓",
                color = HeritageGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = HeritageGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun BloodGroupPill(
    bloodGroup: String,
    modifier: Modifier = Modifier,
) {
    if (bloodGroup.isBlank()) return
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFFEE2E2))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = bloodGroup,
            color = Color(0xFFB91C1C),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
