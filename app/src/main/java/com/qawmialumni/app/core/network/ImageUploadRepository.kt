package com.qawmialumni.app.core.network

import android.net.Uri
import android.util.Log
import com.qawmialumni.app.BuildConfig
import com.qawmialumni.app.core.network.model.FreeImageResponse
import com.qawmialumni.app.core.network.model.ImgBBResponse
import com.qawmialumni.app.core.util.CompressedImage
import com.qawmialumni.app.core.util.ImageTarget
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.core.util.WebpImageCompressor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Zero-cost image pipeline (Blueprint §5.C):
 *
 *   Photo Picker Uri ──► WebP (< 60 KB) ──► Freeimage.host ──(fail)──► ImgBB ──► direct URL
 *
 * Only the returned URL string should be persisted to Firestore. Firebase Storage is never used.
 * API keys are read strictly from [BuildConfig] (injected from GitHub Secrets / local.properties).
 */
@Singleton
class ImageUploadRepository @Inject constructor(
    private val compressor: WebpImageCompressor,
    private val freeImageHostApi: FreeImageHostApi,
    private val imgBBApi: ImgBBApi,
    private val json: Json,
) {

    /** Compresses the picked image and uploads it. Returns the direct image URL on success. */
    suspend fun uploadImage(
        uri: Uri,
        target: ImageTarget = ImageTarget.FULL,
    ): Resource<String> {
        val compressed = try {
            compressor.compress(uri, target)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return Resource.Error("ছবি প্রসেস করা যায়নি (Image processing failed): ${e.message}", e)
        }
        return uploadCompressed(compressed)
    }

    /** Uploads already-compressed bytes: primary host first, fallback on any failure. */
    suspend fun uploadCompressed(image: CompressedImage): Resource<String> = withContext(Dispatchers.IO) {
        val errors = mutableListOf<String>()

        if (BuildConfig.FREEIMAGE_API_KEY.isNotBlank()) {
            when (val primary = safeCall("Freeimage.host") { uploadToFreeImage(image) }) {
                is Resource.Success -> return@withContext primary
                is Resource.Error -> errors += primary.message
                Resource.Loading -> Unit
            }
        } else {
            errors += "Freeimage.host: API key not configured"
        }

        if (BuildConfig.IMGBB_API_KEY.isNotBlank()) {
            when (val fallback = safeCall("ImgBB") { uploadToImgBB(image) }) {
                is Resource.Success -> return@withContext fallback
                is Resource.Error -> errors += fallback.message
                Resource.Loading -> Unit
            }
        } else {
            errors += "ImgBB: API key not configured"
        }

        Log.w(TAG, "All image hosts failed: $errors")
        Resource.Error("ছবি আপলোড ব্যর্থ হয়েছে (Image upload failed). ${errors.joinToString(" | ")}")
    }

    // ------------------------------------------------------------------------

    private suspend fun uploadToFreeImage(image: CompressedImage): Resource<String> {
        val response = freeImageHostApi.upload(
            key = BuildConfig.FREEIMAGE_API_KEY.toTextPart(),
            source = image.toFilePart(fieldName = "source"),
            action = "upload".toTextPart(),
            format = "json".toTextPart(),
        )
        val body = response.body() ?: response.parseError<FreeImageResponse>()
        val url = body?.image?.url ?: body?.image?.image?.url
        return if (response.isSuccessful && !url.isNullOrBlank()) {
            Resource.Success(url.toHttps())
        } else {
            Resource.Error("Freeimage.host: ${body?.error?.message ?: "HTTP ${response.code()}"}")
        }
    }

    private suspend fun uploadToImgBB(image: CompressedImage): Resource<String> {
        val response = imgBBApi.upload(
            key = BuildConfig.IMGBB_API_KEY,
            image = image.toFilePart(fieldName = "image"),
        )
        val body = response.body() ?: response.parseError<ImgBBResponse>()
        val url = body?.data?.url ?: body?.data?.image?.url
        return if (response.isSuccessful && body?.success == true && !url.isNullOrBlank()) {
            Resource.Success(url.toHttps())
        } else {
            Resource.Error("ImgBB: ${body?.error?.message ?: "HTTP ${response.code()}"}")
        }
    }

    private inline fun safeCall(host: String, block: () -> Resource<String>): Resource<String> =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Resource.Error("$host: ${e.message ?: e.javaClass.simpleName}", e)
        }

    private inline fun <reified T> Response<*>.parseError(): T? =
        errorBody()?.string()?.let { raw -> runCatching { json.decodeFromString<T>(raw) }.getOrNull() }

    private fun String.toTextPart() = toRequestBody(TEXT_PLAIN)

    private fun CompressedImage.toFilePart(fieldName: String): MultipartBody.Part =
        MultipartBody.Part.createFormData(
            fieldName,
            fileName,
            bytes.toRequestBody(mimeType.toMediaType()),
        )

    private fun String.toHttps() = if (startsWith("http://")) replaceFirst("http://", "https://") else this

    private companion object {
        const val TAG = "ImageUploadRepo"
        val TEXT_PLAIN = "text/plain".toMediaType()
    }
}
