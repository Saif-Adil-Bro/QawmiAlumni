package com.qawmialumni.app.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Result of a client-side compression.
 *
 * @property bytes WebP-encoded image bytes (target: < 60 KB).
 */
class CompressedImage(
    val bytes: ByteArray,
    val width: Int,
    val height: Int,
    val quality: Int,
) {
    val sizeBytes: Int get() = bytes.size
    val mimeType: String get() = MIME_TYPE
    val fileName: String get() = "qa_${System.currentTimeMillis()}.webp"

    companion object {
        const val MIME_TYPE = "image/webp"
    }
}

/** Target size profile for compression (Blueprint §5.C.2). */
enum class ImageTarget(val maxDimension: Int) {
    /** Profile photos & forum/notice post images: 800px longest edge. */
    FULL(Constants.IMAGE_MAX_DIMENSION_FULL),

    /** Thumbnails: 400px longest edge. */
    THUMBNAIL(Constants.IMAGE_MAX_DIMENSION_THUMB),
}

/**
 * Native, dependency-free WebP compressor.
 *
 * Pipeline:
 * 1. Read image bounds only (no full decode) and pick a power-of-two `inSampleSize`
 *    so a 12MP camera photo never gets fully loaded into memory.
 * 2. Decode, fix EXIF orientation, scale down keeping aspect ratio to [ImageTarget.maxDimension].
 * 3. Encode with `WEBP_LOSSY` (API 30+) or `WEBP` (API < 30) starting at 70% quality.
 * 4. If still >= 60 KB, step quality down to 40%, then progressively shrink dimensions
 *    until the result fits under [Constants.IMAGE_MAX_BYTES].
 */
@Singleton
class WebpImageCompressor @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    suspend fun compress(
        uri: Uri,
        target: ImageTarget = ImageTarget.FULL,
        maxBytes: Int = Constants.IMAGE_MAX_BYTES,
    ): CompressedImage = withContext(Dispatchers.Default) {
        val source = decodeSampled(uri, target.maxDimension)
        val oriented = applyExifOrientation(uri, source)
        var working = scaleDown(oriented, target.maxDimension)
        if (oriented !== working && oriented !== source) oriented.recycle()
        if (source !== working && !source.isRecycled) source.recycle()

        try {
            var result = encodeWithinBudget(working, maxBytes)
            // Quality floor reached but still too large -> shrink dimensions by 20% and retry.
            while (result == null) {
                val longest = max(working.width, working.height)
                val next = (longest * 0.8f).roundToInt()
                if (next < Constants.IMAGE_MIN_DIMENSION) break
                val smaller = scaleDown(working, next)
                if (smaller !== working) working.recycle()
                working = smaller
                result = encodeWithinBudget(working, maxBytes)
            }
            // Extremely noisy images: accept the smallest encode we can produce.
            result ?: encode(working, Constants.WEBP_MIN_QUALITY)
        } finally {
            working.recycle()
        }
    }

    // ------------------------------------------------------------------------

    private fun encodeWithinBudget(bitmap: Bitmap, maxBytes: Int): CompressedImage? {
        var quality = Constants.WEBP_START_QUALITY
        while (quality >= Constants.WEBP_MIN_QUALITY) {
            val encoded = encode(bitmap, quality)
            if (encoded.sizeBytes < maxBytes) return encoded
            quality -= 10
        }
        return null
    }

    private fun encode(bitmap: Bitmap, quality: Int): CompressedImage {
        val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
        }
        val out = ByteArrayOutputStream(64 * 1024)
        if (!bitmap.compress(format, quality, out)) {
            throw IOException("WebP encoding failed")
        }
        return CompressedImage(out.toByteArray(), bitmap.width, bitmap.height, quality)
    }

    private fun decodeSampled(uri: Uri, maxDimension: Int): Bitmap {
        val resolver = context.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            ?: throw IOException("Unable to open image: $uri")
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw IOException("Unsupported or corrupt image")
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: throw IOException("Failed to decode image")
    }

    /** Largest power-of-two sample size that still keeps the longest edge >= [maxDimension]. */
    private fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
        var sample = 1
        val longest = max(width, height)
        while (longest / (sample * 2) >= maxDimension) sample *= 2
        return sample
    }

    private fun applyExifOrientation(uri: Uri, bitmap: Bitmap): Bitmap {
        val orientation = runCatching {
            context.contentResolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            }
        }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.postRotate(90f); matrix.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.postRotate(270f); matrix.postScale(-1f, 1f) }
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /** Scales so the longest edge equals [maxDimension] (aspect ratio preserved). No upscaling. */
    private fun scaleDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val longest = max(bitmap.width, bitmap.height)
        if (longest <= maxDimension) return bitmap
        val ratio = maxDimension.toFloat() / longest
        val w = (bitmap.width * ratio).roundToInt().coerceAtLeast(1)
        val h = (bitmap.height * ratio).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, w, h, true)
    }
}
