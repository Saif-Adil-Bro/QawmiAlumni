package com.qawmialumni.app.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.core.graphics.drawable.toBitmap
import androidx.exifinterface.media.ExifInterface
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
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
 * 1. Read image bytes in a single pass from ContentResolver (avoiding multi-open PhotoPicker issues).
 * 2. Read image bounds and pick a power-of-two `inSampleSize` so a 12MP camera photo never gets
 *    fully decoded at original resolution.
 * 3. Decode, fix EXIF orientation, scale down keeping aspect ratio to [ImageTarget.maxDimension].
 * 4. Encode with `WEBP_LOSSY` (API 30+) or `WEBP` (API < 30) starting at 70% quality.
 * 5. If still >= 60 KB, step quality down to 40%, then progressively shrink dimensions
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
    ): CompressedImage = withContext(Dispatchers.IO) {
        val rawBytes = readUriBytes(uri)
        val (source, orientation) = if (rawBytes != null && rawBytes.isNotEmpty()) {
            val decoded = runCatching { decodeSampledFromBytes(rawBytes, target.maxDimension) }.getOrNull()
                ?: decodeViaImageDecoder(uri, target.maxDimension)
                ?: decodeViaCoil(uri, target.maxDimension)
                ?: throw IOException("Unable to decode image: $uri")
            val exifOrientation = readExifOrientation(rawBytes)
            decoded to exifOrientation
        } else {
            val fallbackBitmap = decodeViaImageDecoder(uri, target.maxDimension)
                ?: decodeViaCoil(uri, target.maxDimension)
                ?: throw IOException("Unable to open image: $uri")
            fallbackBitmap to ExifInterface.ORIENTATION_NORMAL
        }

        val oriented = applyExifOrientation(source, orientation)
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

    private fun readUriBytes(uri: Uri): ByteArray? {
        val resolver = context.contentResolver
        runCatching {
            resolver.openInputStream(uri)?.use { stream ->
                val bytes = stream.readBytes()
                if (bytes.isNotEmpty()) return bytes
            }
        }
        runCatching {
            resolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
                afd.createInputStream().use { stream ->
                    val bytes = stream.readBytes()
                    if (bytes.isNotEmpty()) return bytes
                }
            }
        }
        runCatching {
            resolver.openFileDescriptor(uri, "r")?.use { pfd ->
                FileInputStream(pfd.fileDescriptor).use { stream ->
                    val bytes = stream.readBytes()
                    if (bytes.isNotEmpty()) return bytes
                }
            }
        }
        return null
    }

    private fun decodeSampledFromBytes(rawBytes: ByteArray, maxDimension: Int): Bitmap {
        // NOTE: BitmapFactory.decodeByteArray ALWAYS returns null when inJustDecodeBounds = true!
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw IOException("Unsupported or corrupt image")
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, options)
            ?: throw IOException("Failed to decode image")
    }

    private fun decodeViaImageDecoder(uri: Uri, maxDimension: Int): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null
        return runCatching {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
                val longest = max(info.size.width, info.size.height)
                if (longest > maxDimension) {
                    val ratio = maxDimension.toFloat() / longest
                    val w = (info.size.width * ratio).roundToInt().coerceAtLeast(1)
                    val h = (info.size.height * ratio).roundToInt().coerceAtLeast(1)
                    decoder.setTargetSize(w, h)
                }
            }
        }.getOrNull()
    }

    private suspend fun decodeViaCoil(uri: Uri, maxDimension: Int): Bitmap? {
        return runCatching {
            val request = ImageRequest.Builder(context)
                .data(uri)
                .size(maxDimension)
                .allowHardware(false)
                .build()
            val result = context.imageLoader.execute(request)
            if (result is SuccessResult) {
                val bmp = (result.drawable as? BitmapDrawable)?.bitmap
                    ?: result.drawable.toBitmap(config = Bitmap.Config.ARGB_8888)
                // Always copy so recycling in compressor does not corrupt Coil's memory cache
                bmp.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                null
            }
        }.getOrNull()
    }

    /** Largest power-of-two sample size that still keeps the longest edge >= [maxDimension]. */
    private fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
        var sample = 1
        val longest = max(width, height)
        while (longest / (sample * 2) >= maxDimension) sample *= 2
        return sample
    }

    private fun readExifOrientation(rawBytes: ByteArray): Int {
        return runCatching {
            ByteArrayInputStream(rawBytes).use { input ->
                ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            }
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
    }

    private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
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
