package com.qawmialumni.app.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------
// Freeimage.host (Chevereto API v1)
// Success: { "status_code":200, "image": { "url":"https://iili.io/xxx.webp", ... }, "status_txt":"OK" }
// Error:   { "status_code":400, "error": { "message":"...", "code":... }, "status_txt":"..." }
// ---------------------------------------------------------------------------

@Serializable
data class FreeImageResponse(
    @SerialName("status_code") val statusCode: Int? = null,
    @SerialName("status_txt") val statusText: String? = null,
    val image: FreeImageData? = null,
    val error: ApiErrorBody? = null,
)

@Serializable
data class FreeImageData(
    val name: String? = null,
    val extension: String? = null,
    val size: Long? = null,
    val width: Int? = null,
    val height: Int? = null,
    /** Direct link to the original uploaded file. */
    val url: String? = null,
    @SerialName("display_url") val displayUrl: String? = null,
    val image: ImageVariant? = null,
)

// ---------------------------------------------------------------------------
// ImgBB
// Success: { "data": { "url":"https://i.ibb.co/xxx/yyy.webp", ... }, "success":true, "status":200 }
// Error:   { "status_code":400, "error": { "message":"...", "code":... }, "status_txt":"..." }
// ---------------------------------------------------------------------------

@Serializable
data class ImgBBResponse(
    val data: ImgBBData? = null,
    val success: Boolean = false,
    val status: Int? = null,
    @SerialName("status_code") val statusCode: Int? = null,
    val error: ApiErrorBody? = null,
)

@Serializable
data class ImgBBData(
    val id: String? = null,
    /** Direct link to the original uploaded file. */
    val url: String? = null,
    @SerialName("display_url") val displayUrl: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val size: Long? = null,
    val image: ImageVariant? = null,
)

// ---------------------------------------------------------------------------
// Shared
// ---------------------------------------------------------------------------

@Serializable
data class ImageVariant(
    val filename: String? = null,
    val mime: String? = null,
    val url: String? = null,
)

@Serializable
data class ApiErrorBody(
    val message: String? = null,
    val code: Int? = null,
)
