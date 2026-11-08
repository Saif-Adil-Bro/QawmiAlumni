package com.qawmialumni.app.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Islamic Scholarly & Modern Aesthetic (Blueprint §7)
val EmeraldDeep = Color(0xFF032B21)             // Deepest Scholarly Forest Green
val EmeraldPrimary = Color(0xFF064E3B)          // Deep Emerald Green
val EmeraldTeal = Color(0xFF0D7A5F)             // Vibrant Emerald Accent
val EmeraldOnPrimary = Color(0xFFFFFFFF)
val EmeraldPrimaryContainer = Color(0xFFD1FAE5) // Soft Mint Green
val EmeraldOnPrimaryContainer = Color(0xFF022C22)

val HeritageGold = Color(0xFFD97706)            // Warm Heritage Gold (Accent / Badges)
val HeritageGoldLight = Color(0xFFF59E0B)       // Luminous Gold Highlight
val HeritageGoldOn = Color(0xFFFFFFFF)
val HeritageGoldContainer = Color(0xFFFEF3C7)
val HeritageGoldOnContainer = Color(0xFF78350F)

val LightBackground = Color(0xFFF8F6F1)         // Warm Parchment Off-White
val LightOnBackground = Color(0xFF0F172A)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF1E293B)
val LightSurfaceVariant = Color(0xFFF1EDE4)
val LightOnSurfaceVariant = Color(0xFF475569)
val LightOutline = Color(0xFFE2DDD3)

val DarkBackground = Color(0xFF0B1320)          // Deep Slate Night
val DarkOnBackground = Color(0xFFF1F5F9)
val DarkSurface = Color(0xFF162032)
val DarkOnSurface = Color(0xFFF1F5F9)
val DarkSurfaceVariant = Color(0xFF26334D)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)
val DarkOutline = Color(0xFF334155)

val StatusApproved = Color(0xFF059669)          // Green
val StatusPending = Color(0xFFD97706)           // Amber / Gold
val StatusRejected = Color(0xFFDC2626)          // Red

// Department Ribbon Colors
val DeptDawraColor = Color(0xFF064E3B)          // Emerald
val DeptIftaColor = Color(0xFFB45309)           // Amber Gold
val DeptTafsirColor = Color(0xFF0F766E)         // Deep Teal
val DeptAdabColor = Color(0xFF4338CA)           // Scholarly Indigo
val DeptQiratColor = Color(0xFF047857)          // Jade
val DeptHifzColor = Color(0xFF6D28D9)           // Royal Purple

// Reusable Gradients
val EmeraldHeroGradient = Brush.linearGradient(
    colors = listOf(EmeraldDeep, EmeraldPrimary, EmeraldTeal),
)

val GoldAccentGradient = Brush.horizontalGradient(
    colors = listOf(HeritageGold, HeritageGoldLight),
)
