package com.qawmialumni.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.qawmialumni.app.presentation.theme.DeptAdabColor
import com.qawmialumni.app.presentation.theme.DeptDawraColor
import com.qawmialumni.app.presentation.theme.DeptHifzColor
import com.qawmialumni.app.presentation.theme.DeptIftaColor
import com.qawmialumni.app.presentation.theme.DeptQiratColor
import com.qawmialumni.app.presentation.theme.DeptTafsirColor
import com.qawmialumni.app.presentation.theme.EmeraldHeroGradient
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import com.qawmialumni.app.presentation.theme.HeritageGold
import com.qawmialumni.app.presentation.theme.HeritageGoldLight

/**
 * Zero-APK-bloat custom vector icons built directly in Kotlin bytecode.
 * Avoids pulling in the 20MB `material-icons-extended` dependency.
 */
object QawmiIcons {
    val KitabBook: ImageVector by lazy {
        ImageVector.Builder(
            name = "KitabBook",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
            ) {
                moveTo(21f, 5f)
                curveTo(19.89f, 4.65f, 18.67f, 4.5f, 17.5f, 4.5f)
                curveTo(15.55f, 4.5f, 13.45f, 4.9f, 12f, 6f)
                curveTo(10.55f, 4.9f, 8.45f, 4.5f, 6.5f, 4.5f)
                curveTo(4.55f, 4.5f, 2.45f, 4.9f, 1f, 6f)
                verticalLineTo(20.65f)
                curveTo(1f, 20.9f, 1.25f, 21.15f, 1.5f, 21.15f)
                curveTo(1.6f, 21.15f, 1.65f, 21.1f, 1.75f, 21.1f)
                curveTo(3.1f, 20.45f, 5.05f, 20f, 6.5f, 20f)
                curveTo(8.45f, 20f, 10.55f, 20.4f, 12f, 21.5f)
                curveTo(13.35f, 20.65f, 15.8f, 20f, 17.5f, 20f)
                curveTo(19.15f, 20f, 20.85f, 20.3f, 22.25f, 21.05f)
                curveTo(22.35f, 21.1f, 22.4f, 21.1f, 22.5f, 21.1f)
                curveTo(22.75f, 21.1f, 23f, 20.85f, 23f, 20.6f)
                verticalLineTo(6f)
                curveTo(22.4f, 5.55f, 21.75f, 5.25f, 21f, 5f)
                close()
                moveTo(21f, 18.5f)
                curveTo(19.9f, 18.15f, 18.7f, 18f, 17.5f, 18f)
                curveTo(15.8f, 18f, 13.35f, 18.65f, 12f, 19.5f)
                verticalLineTo(8f)
                curveTo(13.35f, 7.15f, 15.8f, 6.5f, 17.5f, 6.5f)
                curveTo(18.7f, 6.5f, 19.9f, 6.65f, 21f, 7f)
                verticalLineTo(18.5f)
                close()
            }
        }.build()
    }

    val ForumBubble: ImageVector by lazy {
        ImageVector.Builder(
            name = "ForumBubble",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(21f, 6f)
                horizontalLineTo(19f)
                verticalLineTo(15f)
                horizontalLineTo(6f)
                verticalLineTo(17f)
                curveTo(6f, 17.55f, 6.45f, 18f, 7f, 18f)
                horizontalLineTo(18f)
                lineTo(22f, 22f)
                verticalLineTo(7f)
                curveTo(22f, 6.45f, 21.55f, 6f, 21f, 6f)
                close()
                moveTo(17f, 12f)
                verticalLineTo(3f)
                curveTo(17f, 2.45f, 16.55f, 2f, 16f, 2f)
                horizontalLineTo(3f)
                curveTo(2.45f, 2f, 2f, 2.45f, 2f, 3f)
                verticalLineTo(17f)
                lineTo(6f, 13f)
                horizontalLineTo(16f)
                curveTo(16.55f, 13f, 17f, 12.55f, 17f, 12f)
                close()
            }
        }.build()
    }

    val NoticeCampaign: ImageVector by lazy {
        ImageVector.Builder(
            name = "NoticeCampaign",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(18f, 11f)
                verticalLineTo(13f)
                horizontalLineTo(22f)
                verticalLineTo(11f)
                horizontalLineTo(18f)
                close()
                moveTo(16f, 17.61f)
                curveTo(16.96f, 18.32f, 18.21f, 19.26f, 19.2f, 20f)
                curveTo(19.6f, 19.47f, 20f, 18.93f, 20.4f, 18.4f)
                curveTo(19.41f, 17.66f, 18.16f, 16.72f, 17.2f, 16f)
                curveTo(16.8f, 16.54f, 16.4f, 17.08f, 16f, 17.61f)
                close()
                moveTo(20.4f, 5.6f)
                curveTo(20f, 5.07f, 19.6f, 4.53f, 19.2f, 4f)
                curveTo(18.21f, 4.74f, 16.96f, 5.68f, 16f, 6.4f)
                curveTo(16.4f, 6.93f, 16.8f, 7.47f, 17.2f, 8f)
                curveTo(18.16f, 7.28f, 19.41f, 6.35f, 20.4f, 5.6f)
                close()
                moveTo(4f, 9f)
                curveTo(2.9f, 9f, 2f, 9.9f, 2f, 11f)
                verticalLineTo(13f)
                curveTo(2f, 14.1f, 2.9f, 15f, 4f, 15f)
                horizontalLineTo(5f)
                verticalLineTo(19f)
                horizontalLineTo(7f)
                verticalLineTo(15f)
                horizontalLineTo(8f)
                lineTo(13f, 18f)
                verticalLineTo(6f)
                lineTo(8f, 9f)
                horizontalLineTo(4f)
                close()
                moveTo(15.5f, 12f)
                curveTo(15.5f, 10.67f, 14.92f, 9.47f, 14f, 8.65f)
                verticalLineTo(15.34f)
                curveTo(14.92f, 14.53f, 15.5f, 13.33f, 15.5f, 12f)
                close()
            }
        }.build()
    }

    val ChatMessage: ImageVector by lazy {
        ImageVector.Builder(
            name = "ChatMessage",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 2f)
                horizontalLineTo(4f)
                curveTo(2.9f, 2f, 2.01f, 2.9f, 2.01f, 4f)
                lineTo(2f, 22f)
                lineTo(6f, 18f)
                horizontalLineTo(20f)
                curveTo(21.1f, 18f, 22f, 17.1f, 22f, 16f)
                verticalLineTo(4f)
                curveTo(22f, 2.9f, 21.1f, 2f, 20f, 2f)
                close()
                moveTo(6f, 9f)
                horizontalLineTo(18f)
                verticalLineTo(11f)
                horizontalLineTo(6f)
                verticalLineTo(9f)
                close()
                moveTo(14f, 14f)
                horizontalLineTo(6f)
                verticalLineTo(12f)
                horizontalLineTo(14f)
                verticalLineTo(14f)
                close()
                moveTo(18f, 8f)
                horizontalLineTo(6f)
                verticalLineTo(6f)
                horizontalLineTo(18f)
                verticalLineTo(8f)
                close()
            }
        }.build()
    }

    val AdminShield: ImageVector by lazy {
        ImageVector.Builder(
            name = "AdminShield",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 1f)
                lineTo(3f, 5f)
                verticalLineTo(11f)
                curveTo(3f, 16.55f, 6.84f, 21.74f, 12f, 23f)
                curveTo(17.16f, 21.74f, 21f, 16.55f, 21f, 11f)
                verticalLineTo(5f)
                lineTo(12f, 1f)
                close()
                moveTo(10f, 17f)
                lineTo(6f, 13f)
                lineTo(7.41f, 11.59f)
                lineTo(10f, 14.17f)
                lineTo(16.59f, 7.58f)
                lineTo(18f, 9f)
                lineTo(10f, 17f)
                close()
            }
        }.build()
    }

    val FilterSliders: ImageVector by lazy {
        ImageVector.Builder(
            name = "FilterSliders",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 17f)
                verticalLineTo(19f)
                horizontalLineTo(9f)
                verticalLineTo(17f)
                horizontalLineTo(3f)
                close()
                moveTo(3f, 5f)
                verticalLineTo(7f)
                horizontalLineTo(13f)
                verticalLineTo(5f)
                horizontalLineTo(3f)
                close()
                moveTo(13f, 21f)
                verticalLineTo(19f)
                horizontalLineTo(21f)
                verticalLineTo(17f)
                horizontalLineTo(13f)
                verticalLineTo(15f)
                horizontalLineTo(11f)
                verticalLineTo(21f)
                horizontalLineTo(13f)
                close()
                moveTo(7f, 9f)
                verticalLineTo(11f)
                horizontalLineTo(3f)
                verticalLineTo(13f)
                horizontalLineTo(7f)
                verticalLineTo(15f)
                horizontalLineTo(9f)
                verticalLineTo(9f)
                horizontalLineTo(7f)
                close()
                moveTo(21f, 13f)
                verticalLineTo(11f)
                horizontalLineTo(11f)
                verticalLineTo(13f)
                horizontalLineTo(21f)
                close()
                moveTo(15f, 9f)
                horizontalLineTo(17f)
                verticalLineTo(7f)
                horizontalLineTo(21f)
                verticalLineTo(5f)
                horizontalLineTo(17f)
                verticalLineTo(3f)
                horizontalLineTo(15f)
                verticalLineTo(9f)
                close()
            }
        }.build()
    }

    val VisibilityEye: ImageVector by lazy {
        ImageVector.Builder(
            name = "VisibilityEye",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 4.5f)
                curveTo(7f, 4.5f, 2.73f, 7.61f, 1f, 12f)
                curveTo(2.73f, 16.39f, 7f, 19.5f, 12f, 19.5f)
                curveTo(17f, 19.5f, 21.27f, 16.39f, 23f, 12f)
                curveTo(21.27f, 7.61f, 17f, 4.5f, 12f, 4.5f)
                close()
                moveTo(12f, 17f)
                curveTo(9.24f, 17f, 7f, 14.76f, 7f, 12f)
                curveTo(7f, 9.24f, 9.24f, 7f, 12f, 7f)
                curveTo(14.76f, 7f, 17f, 9.24f, 17f, 12f)
                curveTo(17f, 14.76f, 14.76f, 17f, 12f, 17f)
                close()
                moveTo(12f, 9f)
                curveTo(10.34f, 9f, 9f, 10.34f, 9f, 12f)
                curveTo(9f, 13.66f, 10.34f, 15f, 12f, 15f)
                curveTo(13.66f, 15f, 15f, 13.66f, 15f, 12f)
                curveTo(15f, 10.34f, 13.66f, 9f, 12f, 9f)
                close()
            }
        }.build()
    }

    val VisibilityOffEye: ImageVector by lazy {
        ImageVector.Builder(
            name = "VisibilityOffEye",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 7f)
                curveTo(14.76f, 7f, 17f, 9.24f, 17f, 12f)
                curveTo(17f, 12.65f, 16.87f, 13.26f, 16.64f, 13.83f)
                lineTo(19.56f, 16.75f)
                curveTo(21.07f, 15.49f, 22.26f, 13.86f, 23f, 12f)
                curveTo(21.27f, 7.61f, 17f, 4.5f, 12f, 4.5f)
                curveTo(10.6f, 4.5f, 9.26f, 4.75f, 8.02f, 5.2f)
                lineTo(10.17f, 7.35f)
                curveTo(10.74f, 7.13f, 11.35f, 7f, 12f, 7f)
                close()
                moveTo(2f, 4.27f)
                lineTo(4.28f, 6.55f)
                lineTo(4.74f, 7.01f)
                curveTo(3.08f, 8.3f, 1.78f, 10.02f, 1f, 12f)
                curveTo(2.73f, 16.39f, 7f, 19.5f, 12f, 19.5f)
                curveTo(13.55f, 19.5f, 15.03f, 19.2f, 16.38f, 18.66f)
                lineTo(16.8f, 19.08f)
                lineTo(19.73f, 22f)
                lineTo(21f, 20.73f)
                lineTo(3.27f, 3f)
                lineTo(2f, 4.27f)
                close()
            }
        }.build()
    }

    val GraduationCap: ImageVector by lazy {
        ImageVector.Builder(
            name = "GraduationCap",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(5f, 13.18f)
                verticalLineTo(17.18f)
                lineTo(12f, 21f)
                lineTo(19f, 17.18f)
                verticalLineTo(13.18f)
                lineTo(12f, 17f)
                lineTo(5f, 13.18f)
                close()
                moveTo(12f, 3f)
                lineTo(1f, 9f)
                lineTo(12f, 15f)
                lineTo(21f, 10.09f)
                verticalLineTo(17f)
                horizontalLineTo(23f)
                verticalLineTo(9f)
                lineTo(12f, 3f)
                close()
            }
        }.build()
    }

    val NotificationBell: ImageVector by lazy {
        ImageVector.Builder(
            name = "NotificationBell",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 22f)
                curveTo(13.1f, 22f, 14f, 21.1f, 14f, 20f)
                horizontalLineTo(10f)
                curveTo(10f, 21.1f, 10.9f, 22f, 12f, 22f)
                close()
                moveTo(18f, 16f)
                verticalLineTo(11f)
                curveTo(18f, 7.93f, 16.37f, 5.36f, 13.5f, 4.68f)
                verticalLineTo(4f)
                curveTo(13.5f, 3.17f, 12.83f, 2.5f, 12f, 2.5f)
                curveTo(11.17f, 2.5f, 10.5f, 3.17f, 10.5f, 4f)
                verticalLineTo(4.68f)
                curveTo(7.64f, 5.36f, 6f, 7.92f, 6f, 11f)
                verticalLineTo(16f)
                lineTo(4f, 18f)
                verticalLineTo(19f)
                horizontalLineTo(20f)
                verticalLineTo(18f)
                lineTo(18f, 16f)
                close()
            }
        }.build()
    }

    val AuditHistory: ImageVector by lazy {
        ImageVector.Builder(
            name = "AuditHistory",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(13f, 3f)
                curveTo(8.03f, 3f, 4f, 7.03f, 4f, 12f)
                horizontalLineTo(1f)
                lineTo(4.89f, 15.89f)
                lineTo(5f, 16f)
                lineTo(9f, 12f)
                horizontalLineTo(6f)
                curveTo(6f, 8.13f, 9.13f, 5f, 13f, 5f)
                curveTo(16.87f, 5f, 20f, 8.13f, 20f, 12f)
                curveTo(20f, 15.87f, 16.87f, 19f, 13f, 19f)
                curveTo(11.07f, 19f, 9.32f, 18.21f, 8.06f, 16.94f)
                lineTo(6.64f, 18.36f)
                curveTo(8.27f, 19.99f, 10.51f, 21f, 13f, 21f)
                curveTo(17.97f, 21f, 22f, 16.97f, 22f, 12f)
                curveTo(22f, 7.03f, 17.97f, 3f, 13f, 3f)
                close()
                moveTo(12f, 8f)
                verticalLineTo(13f)
                lineTo(16.28f, 15.54f)
                lineTo(17f, 14.33f)
                lineTo(13.5f, 12.25f)
                verticalLineTo(8f)
                horizontalLineTo(12f)
                close()
            }
        }.build()
    }
}

/**
 * Decorative Emerald banner with subtle Canvas-drawn 8-point Islamic geometric stars and Mihrab arches.
 * Zero image assets required — pure GPU vector drawing.
 */
@Composable
fun IslamicGeometricBanner(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(brush = EmeraldHeroGradient),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeColor = HeritageGoldLight.copy(alpha = 0.12f)
            val softWhite = Color.White.copy(alpha = 0.05f)

            // Subtle 8-point Rub el Hizb star in top-right and bottom-left corners
            val starSize = 96.dp.toPx()
            val rightCenter = Offset(size.width - 36.dp.toPx(), 36.dp.toPx())
            for (angle in listOf(0f, 45f)) {
                rotate(degrees = angle, pivot = rightCenter) {
                    drawRect(
                        color = strokeColor,
                        topLeft = Offset(rightCenter.x - starSize / 2, rightCenter.y - starSize / 2),
                        size = Size(starSize, starSize),
                        style = Stroke(width = 2.dp.toPx()),
                    )
                }
            }

            val leftCenter = Offset(32.dp.toPx(), size.height - 20.dp.toPx())
            val smallStar = 68.dp.toPx()
            for (angle in listOf(0f, 45f)) {
                rotate(degrees = angle, pivot = leftCenter) {
                    drawRect(
                        color = softWhite,
                        topLeft = Offset(leftCenter.x - smallStar / 2, leftCenter.y - smallStar / 2),
                        size = Size(smallStar, smallStar),
                        style = Stroke(width = 1.5.dp.toPx()),
                    )
                }
            }

            // Subtle Mihrab Arch silhouette in center background
            val archWidth = 160.dp.toPx()
            val archLeft = (size.width - archWidth) / 2f
            val archTop = 12.dp.toPx()
            val archPath = Path().apply {
                moveTo(archLeft, size.height)
                lineTo(archLeft, archTop + archWidth * 0.45f)
                quadraticTo(
                    size.width / 2f,
                    archTop,
                    archLeft + archWidth,
                    archTop + archWidth * 0.45f,
                )
                lineTo(archLeft + archWidth, size.height)
            }
            drawPath(
                path = archPath,
                color = strokeColor,
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }

        content()
    }
}

@Composable
fun VerifiedBadge(
    modifier: Modifier = Modifier,
    text: String = "ভেরিফায়েড",
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = HeritageGold.copy(alpha = 0.14f),
        border = androidx.compose.foundation.BorderStroke(1.dp, HeritageGold.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(HeritageGold),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp),
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = HeritageGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
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
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = Color(0xFFFEE2E2),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDC2626)),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = bloodGroup,
                color = Color(0xFFB91C1C),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

fun departmentColor(departmentWireValue: String): Color = when (departmentWireValue) {
    "IFTA" -> DeptIftaColor
    "TAFSIR" -> DeptTafsirColor
    "ADAB" -> DeptAdabColor
    "QIRAT" -> DeptQiratColor
    "HIFZ" -> DeptHifzColor
    else -> DeptDawraColor
}

@Composable
fun DepartmentBadge(
    label: String,
    departmentWireValue: String = "DAWRA_HADITH",
    modifier: Modifier = Modifier,
) {
    val accent = departmentColor(departmentWireValue)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = accent.copy(alpha = 0.12f),
    ) {
        Text(
            text = label,
            color = accent,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

/**
 * Circular Avatar with a scholarly Heritage Gold or Emerald double ring and clean fallback initial.
 */
@Composable
fun AvatarWithRing(
    photoUrl: String,
    name: String,
    size: Dp = 56.dp,
    ringColor: Color = HeritageGold,
    backgroundColor: Color = EmeraldPrimary.copy(alpha = 0.12f),
    textColor: Color = EmeraldPrimary,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(width = 2.dp, color = ringColor.copy(alpha = 0.75f), shape = CircleShape)
            .padding(2.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        if (photoUrl.isNotBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        } else {
            val initial = name.trim().take(1).ifBlank { "আ" }
            Text(
                text = initial,
                fontSize = (size.value * 0.38f).sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
            )
        }
    }
}

/**
 * Modern KPI Summary Card used in Admin Dashboards & Directory headers.
 */
@Composable
fun StatSummaryCard(
    title: String,
    value: String,
    accentColor: Color = EmeraldPrimary,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.18f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(accentColor),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Standardized scholarly Empty State card with subtle geometric accent.
 */
@Composable
fun ModernEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(EmeraldPrimary.copy(alpha = 0.08f))
                .border(1.dp, HeritageGold.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(34.dp),
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            Spacer(modifier = Modifier.height(16.dp))
            action()
        }
    }
}
