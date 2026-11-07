package com.qawmialumni.app.core.network

import com.qawmialumni.app.core.network.model.FreeImageResponse
import com.qawmialumni.app.core.network.model.ImgBBResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

/**
 * Primary image host — Freeimage.host.
 * Docs: https://freeimage.host/page/api
 *
 * The API key is sent as a multipart form field (not in the URL) so it never
 * appears in URL-based logs.
 */
interface FreeImageHostApi {

    @Multipart
    @POST("api/1/upload")
    suspend fun upload(
        @Part("key") key: RequestBody,
        @Part source: MultipartBody.Part,          // form field name: "source"
        @Part("action") action: RequestBody,       // "upload"
        @Part("format") format: RequestBody,       // "json"
    ): Response<FreeImageResponse>
}

/**
 * Fallback image host — ImgBB.
 * Docs: https://api.imgbb.com/
 */
interface ImgBBApi {

    @Multipart
    @POST("1/upload")
    suspend fun upload(
        @Query("key") key: String,
        @Part image: MultipartBody.Part,           // form field name: "image"
    ): Response<ImgBBResponse>
}
