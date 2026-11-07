package com.qawmialumni.app.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import com.qawmialumni.app.presentation.theme.HeritageGold

@Composable
fun PendingApprovalScreen(
    onCheckStatus: () -> Unit,
    onSignOut: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(HeritageGold.copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "⏳",
                    fontSize = 42.sp,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "আবেদনটি অনুমোদনের অপেক্ষায় রয়েছে",
                style = MaterialTheme.typography.headlineMedium,
                color = EmeraldPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "আপনার মাদরাসার দায়িত্বশীল অ্যাডমিন বা উস্তাদ আপনার আবেদনটি যাচাই করে অনুমোদন দিলেই আপনি ডিজিটাল আলামনাই ডিরেক্টরি, ইলমি মুযাকারা ও ফোরাম ব্যবহার করতে পারবেন।",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
            )

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = onCheckStatus,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("স্ট্যাটাস পুনরায় যাচাই করুন", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("লগআউট", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
