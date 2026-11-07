# Qawmi Alumni (رابطة خريجي المدارস القومية)

A high-performance, ultra-slim (<8MB APK), and modern Native Android Application designed for **Qawmi Madrasa Alumni (Faregeen) & Community Management**.

---

## 🌟 Key Features

1. **Multi-Tenant SaaS Architecture:**
   - Single unified application for multiple Madrasas, partitioned by unique `madrasaCode`.
   - Dynamic **Single-App Role-Based Access Control (RBAC)** handling 4 roles: `super_admin`, `madrasa_admin`, `staff`, and `alumni`.

2. **Digital Alumni Directory (Digital Album):**
   - 2-Column sleek grid showing photos, batch badge, blood group pill, and current khedmat.
   - **Smart Filters:** Instantly filter by Hijri Batch Year, Supervised Ustad, Blood Group (Emergency Donor Finder), District, and Khedmat Category directly from local Room cache with zero Firestore latency.
   - **Stylized Digital Alumni ID Card:** Generates a downloadable/shareable card with Madrasa identity, crescent emblem, and verification badge.
   - **1-Click Quick Actions:** Direct WhatsApp and direct phone dialing buttons to offload high-volume communication.

3. **Ilmi Muzakara & Community Forum:**
   - Scholarly discussions categorized into *Ilmi Muzakara*, *Masayel*, *Khedmat/Jobs*, and *General*.
   - Likes, comments subcollection, and image attachments.

4. **Official Notice Board:**
   - Official announcements and reunion notices with event dates and photo banners.

5. **Firestore-Optimized Bucketed Chat:**
   - Chat room bucket model storing recent messages inside room documents to prevent N-read quota exhaustion on Firebase Spark free tier.

6. **Zero-Cost Image Pipeline:**
   - **NO Firebase Storage.**
   - Client-side WebP compression (<60KB) using native Android APIs.
   - Free image hosting via **Freeimage.host API** (primary) and **ImgBB API** (fallback). Only the direct URL string is stored in Firestore.

---

## 🏗️ Architecture & Tech Stack

- **UI:** 100% Jetpack Compose (Material 3) — Single-Activity Architecture (`MainActivity.kt`)
- **Theme:** Islamic Scholarly & Modern Aesthetic (Deep Emerald Green `#064E3B`, Soft Mint `#D1FAE5`, Heritage Gold `#D97706`, Warm Off-White `#FAF8F5`)
- **Architecture:** Clean Architecture + MVVM (Presentation, Domain, Data layers)
- **Dependency Injection:** Dagger Hilt
- **Local Persistence & Caching:**
  - **Room Database:** Offline caching for Alumni, Ustads, and Notices with incremental sync (`updatedAt > lastLocalSync`).
  - **Jetpack DataStore:** Caching active session (`uid`, `role`, `madrasaId`, `status`).
- **Networking:** Retrofit 2 + OkHttp 3 + Kotlinx Serialization
- **Image Loading:** Coil Compose (aggressive 150MB disk + memory caching)
- **Minification:** R8 Full Mode + Resource Shrinking + ABI Splits (Universal, arm64-v8a, armeabi-v7a, x86_64)

---

## 🔒 Security & Zero-Secret CI/CD Policy

All sensitive credentials and API keys are strictly excluded from git via `.gitignore`. During GitHub Actions CI/CD runs, secrets are injected via GitHub Secrets into environment variables and `BuildConfig` fields:

### Required GitHub Repository Secrets

Configure these in **GitHub Repo → Settings → Secrets and variables → Actions**:

| Secret Key | Description |
| :--- | :--- |
| `GOOGLE_SERVICES_JSON_BASE64` | Base64 encoded string of `google-services.json` (`base64 -w 0 google-services.json`) |
| `FREEIMAGE_API_KEY` | Freeimage.host API Key |
| `IMGBB_API_KEY` | ImgBB API Key |
| `RELEASE_KEYSTORE_BASE64` | Base64 encoded `.jks` file (Optional for debug, required for signed release) |
| `KEYSTORE_PASSWORD` | Password for the keystore |
| `KEY_ALIAS` | Alias name of the key |
| `KEY_PASSWORD` | Password for the key |
| `APPETIZE_API_TOKEN` | (Optional) Appetize.io API token for automated browser live preview |
| `APPETIZE_PUBLIC_KEY` | (Optional) Appetize.io app public key to update existing preview URL |

---

## 🚀 Building Locally

Create a `local.properties` file in the project root:

```properties
sdk.dir=/path/to/android-sdk
FREEIMAGE_API_KEY=your_freeimage_key_here
IMGBB_API_KEY=your_imgbb_key_here
```

To build Debug APK:
```bash
./gradlew assembleDebug
```

To build R8-optimized Release APK:
```bash
./gradlew assembleRelease
```
