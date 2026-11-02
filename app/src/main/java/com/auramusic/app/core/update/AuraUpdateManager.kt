package com.auramusic.app.core.update

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import com.auramusic.app.core.extractor.SimpMusicExtractorEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

data class AuraReleaseInfo(
    val version: String,
    val tagName: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val fileName: String,
    val apkSize: Long,
    val publishedAt: String,
    val htmlUrl: String
)

data class UpdateDownloadProgress(
    val writtenBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val fraction: Float = 0f
)

enum class UpdateState {
    IDLE,
    DOWNLOADING,
    READY,
    ERROR
}

object AuraUpdateManager {
    private const val TAG = "AuraUpdateManager"
    private const val GITHUB_REPO = "jlfuertes14/Aura-Music"
    private const val GITHUB_RELEASE_URL = "https://api.github.com/repos/$GITHUB_REPO/releases/latest"

    private const val PREFS_NAME = "aura_update_preferences"
    private const val KEY_LAST_CHECK_MS = "last_check_ms"
    private const val KEY_SNOOZED_VERSION = "snoozed_version"
    private const val KEY_SNOOZED_UNTIL_MS = "snoozed_until_ms"

    private const val CHECK_CADENCE_MS = 15 * 60 * 1000L // 15 minutes
    private const val SNOOZE_DURATION_MS = 24 * 60 * 60 * 1000L // 24 hours

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getCurrentAppVersion(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName?.trim() ?: "1.2"
        } catch (_: Exception) {
            "1.2"
        }
    }

    fun isNewerVersion(remoteVersion: String, localVersion: String): Boolean {
        val cleanRemote = remoteVersion.trim().removePrefix("v").removePrefix("V")
        val cleanLocal = localVersion.trim().removePrefix("v").removePrefix("V")

        val rParts = cleanRemote.split(".").map { it.toIntOrNull() ?: 0 }
        val lParts = cleanLocal.split(".").map { it.toIntOrNull() ?: 0 }

        val length = maxOf(rParts.size, lParts.size, 3)
        for (i in 0 until length) {
            val r = rParts.getOrElse(i) { 0 }
            val l = lParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }
        return false
    }

    /**
     * Checks GitHub Releases API for a newer universal APK release.
     * Direct client-to-GitHub verification (bypasses any intermediate backend server).
     */
    suspend fun checkForUpdates(
        context: Context,
        force: Boolean = false
    ): AuraReleaseInfo? = withContext(Dispatchers.IO) {
        val prefs = getPrefs(context)
        val now = System.currentTimeMillis()

        if (!force) {
            val lastCheck = prefs.getLong(KEY_LAST_CHECK_MS, 0L)
            if (now - lastCheck < CHECK_CADENCE_MS) {
                Log.d(TAG, "Update check skipped: cadence active (${now - lastCheck}ms < ${CHECK_CADENCE_MS}ms)")
                return@withContext null
            }
        }

        try {
            val request = Request.Builder()
                .url(GITHUB_RELEASE_URL)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "AuraMusic-AndroidClient")
                .build()

            SimpMusicExtractorEngine.httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "GitHub release check responded with code ${response.code}")
                    return@withContext null
                }

                val rawJson = response.body?.string() ?: return@withContext null
                val root = JSONObject(rawJson)

                val tagName = root.optString("tag_name", "").trim()
                val version = tagName.removePrefix("v").removePrefix("V")
                val releaseTitle = root.optString("name", "Aura v$version")
                val releaseNotes = root.optString("body", "No release notes provided for this version.")
                val publishedAt = root.optString("published_at", "")
                val htmlUrl = root.optString("html_url", "https://github.com/$GITHUB_REPO/releases/tag/$tagName")

                // Locate universal APK or any .apk in assets
                val assets = root.optJSONArray("assets") ?: return@withContext null
                var apkUrl: String? = null
                var apkName: String? = null
                var apkSize = 0L

                for (i in 0 until assets.length()) {
                    val asset = assets.optJSONObject(i) ?: continue
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        if (apkUrl == null || name.contains("universal", ignoreCase = true)) {
                            apkUrl = asset.optString("browser_download_url")
                            apkName = name
                            apkSize = asset.optLong("size", 0L)
                            if (name.contains("universal", ignoreCase = true)) break
                        }
                    }
                }

                if (apkUrl.isNullOrBlank()) {
                    Log.w(TAG, "Release $tagName does not contain an installable APK asset")
                    return@withContext null
                }

                prefs.edit().putLong(KEY_LAST_CHECK_MS, now).apply()

                val currentVersion = getCurrentAppVersion(context)
                if (!isNewerVersion(version, currentVersion)) {
                    Log.d(TAG, "App is on latest version: local=$currentVersion, remote=$version")
                    return@withContext null
                }

                // Check snooze condition if this is not a user-initiated forced check
                if (!force) {
                    val snoozedVersion = prefs.getString(KEY_SNOOZED_VERSION, null)
                    val snoozedUntil = prefs.getLong(KEY_SNOOZED_UNTIL_MS, 0L)
                    if (snoozedVersion == version && snoozedUntil > now) {
                        Log.d(TAG, "Release $version is snoozed until $snoozedUntil")
                        return@withContext null
                    }
                }

                AuraReleaseInfo(
                    version = version,
                    tagName = tagName,
                    releaseTitle = releaseTitle,
                    releaseNotes = releaseNotes,
                    downloadUrl = apkUrl,
                    fileName = apkName ?: "aura-music-v$version.apk",
                    apkSize = apkSize,
                    publishedAt = publishedAt,
                    htmlUrl = htmlUrl
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates: ${e.message}", e)
            null
        }
    }

    fun snoozeUpdate(context: Context, version: String) {
        val prefs = getPrefs(context)
        prefs.edit()
            .putString(KEY_SNOOZED_VERSION, version)
            .putLong(KEY_SNOOZED_UNTIL_MS, System.currentTimeMillis() + SNOOZE_DURATION_MS)
            .apply()
    }

    /**
     * Downloads the APK file to the app's cache directory with live progress emission.
     */
    suspend fun downloadApk(
        context: Context,
        release: AuraReleaseInfo,
        onProgress: (UpdateDownloadProgress) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val updatesDir = File(context.cacheDir, "updates").apply {
                if (!exists()) mkdirs()
            }

            // Cleanup any previous APKs to preserve disk space
            updatesDir.listFiles()?.forEach { file ->
                if (file.name.endsWith(".apk") && file.name != release.fileName) {
                    try { file.delete() } catch (_: Exception) {}
                }
            }

            val targetFile = File(updatesDir, release.fileName)
            if (targetFile.exists()) {
                targetFile.delete()
            }

            val request = Request.Builder()
                .url(release.downloadUrl)
                .header("User-Agent", "AuraMusic-AndroidClient")
                .build()

            SimpMusicExtractorEngine.httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP ${response.code} downloading update"))
                }

                val body = response.body ?: return@withContext Result.failure(Exception("Empty download body"))
                val totalBytes = if (body.contentLength() > 0) body.contentLength() else release.apkSize
                var writtenBytes = 0L

                val inputStream: InputStream = body.byteStream()
                val outputStream = FileOutputStream(targetFile)

                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    writtenBytes += bytesRead

                    val fraction = if (totalBytes > 0) {
                        (writtenBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    onProgress(UpdateDownloadProgress(writtenBytes, totalBytes, fraction))
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()
            }

            Result.success(targetFile)
        } catch (e: Exception) {
            Log.e(TAG, "Download failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Prompts the Android Package Installer with the downloaded APK using FileProvider.
     */
    fun installApk(context: Context, apkFile: File): Result<Unit> {
        return try {
            if (!apkFile.exists()) {
                return Result.failure(Exception("Downloaded APK file not found"))
            }

            // Check Unknown App Sources permission on Android 8.0+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(settingsIntent)
                }
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(installIntent)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Launch installer failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}
