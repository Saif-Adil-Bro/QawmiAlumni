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

## 🔔 Notification Delivery Model (recipient-addressed)

Notifications **never** fire on the device that performs the action. A message, notice, forum
post or self-profile edit notifies **the other party** — not the author.

1. **Publish (actor's device):** one `notifications/{id}` Firestore document is written per
   recipient, with `recipientUid`, `senderUid`, `type`, `title`, `message`, `targetId`,
   `createdAt`, `isRead`. The actor's own uid is filtered out before writing.
2. **Deliver (recipient's device):** every signed-in device listens to
   `notifications.whereEqualTo("recipientUid", myUid)` (a single-field filter — no composite
   index and no Cloud Functions required on the free Spark plan) and raises the in-app
   banner + system push only for documents addressed to it.
3. **Routing rules**

| Action | Who gets notified |
| :--- | :--- |
| Send a chat message | The other participant of that room only |
| Publish a notice | Every approved member of that madrasa (author excluded) |
| Publish an approved forum post | Madrasa members (global forum → all users) + super admins, author excluded |
| Approve / reject a join request, change a role | The affected member |
| Member edits own profile / adds a photo | Madrasa admins **and** super admins (member excluded) |
| Madrasa admin submits an info-change request | Super admins (requester excluded) |
| Super admin approves / rejects that request | The requesting madrasa admin |

`NotificationHelper.bindUser(uid)` is bound to the DataStore session in `QawmiAlumniApp`, so
switching account re-binds delivery and `signOut()` wipes the local list — this prevents both
"my own message notifying me" and cross-account notification leaks. Firestore rules in
`firestore.rules` allow `create` only for a *different* recipient, and `read/update/delete`
only for the recipient.

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
