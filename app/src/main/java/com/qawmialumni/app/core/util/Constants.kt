package com.qawmialumni.app.core.util

object Constants {

    // ---------- Free image hosting APIs (Blueprint §5.C) ----------
    /** Primary host. Endpoint: POST https://freeimage.host/api/1/upload */
    const val FREEIMAGE_BASE_URL = "https://freeimage.host/"
    /** Fallback host. Endpoint: POST https://api.imgbb.com/1/upload */
    const val IMGBB_BASE_URL = "https://api.imgbb.com/"

    // ---------- Client-side WebP compression ----------
    /** Max longest edge for profile photos and forum/notice post images. */
    const val IMAGE_MAX_DIMENSION_FULL = 800
    /** Max longest edge for thumbnails. */
    const val IMAGE_MAX_DIMENSION_THUMB = 400
    /** Starting WebP quality (70%). */
    const val WEBP_START_QUALITY = 70
    /** Lowest quality we accept before shrinking dimensions instead. */
    const val WEBP_MIN_QUALITY = 40
    /** Hard size ceiling for uploaded images: < 60 KB. */
    const val IMAGE_MAX_BYTES = 60 * 1024
    /** Never shrink below this longest edge while chasing the size limit. */
    const val IMAGE_MIN_DIMENSION = 240

    // ---------- Network ----------
    const val NETWORK_CONNECT_TIMEOUT_SEC = 20L
    const val NETWORK_IO_TIMEOUT_SEC = 45L
}
