package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

// Проверка и установка обновлений киоска, раздаваемых собственным backend'ом
// (см. /api/admin/app-releases на сервере — админ загружает APK и публикует релиз,
// киоск лишь спрашивает /api/app/latest-version и, если version_code новее текущего,
// предлагает скачать и установить).
object AppUpdateManager {
    private const val UPDATE_STATUS_PREFS = "nex_update_status"
    private const val KEY_STATUS_PENDING = "pending"
    private const val KEY_STATUS = "status"
    private const val KEY_TARGET_VERSION_CODE = "target_version_code"
    private const val KEY_TARGET_VERSION_NAME = "target_version_name"
    private const val KEY_INSTALLED_VERSION_CODE = "installed_version_code"
    private const val KEY_INSTALLED_VERSION_NAME = "installed_version_name"
    private const val KEY_APK_URL = "apk_url"
    private const val KEY_MESSAGE = "message"
    private const val KEY_REPORTED_AT = "reported_at"

    // Отдельный клиент без логирующего интерцептора большого бинарного тела APK
    private val downloadClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.MINUTES)
        .writeTimeout(5, TimeUnit.MINUTES)
        .build()

    private fun resolveApkUrl(rawUrl: String): String {
        if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) return rawUrl
        val base = "https://nexium-health.com/"
        return if (rawUrl.startsWith("/")) base + rawUrl.substring(1) else base + rawUrl
    }

    // Возвращает описание нового релиза, если version_code на сервере больше текущего
    // BuildConfig.VERSION_CODE, иначе null (обновлений нет или сервер вернул 204).
    suspend fun checkForUpdate(): AppVersionResponse? = withContext(Dispatchers.IO) {
        try {
            val response = NexApiClient.service.getLatestAppVersion(
                NexApiClient.deviceToken,
                BuildConfig.VERSION_CODE,
                BuildConfig.VERSION_NAME
            )
            if (!response.isSuccessful) return@withContext null
            val body = response.body() ?: return@withContext null
            if (body.versionCode <= BuildConfig.VERSION_CODE) return@withContext null
            body
        } catch (e: Exception) {
            null
        }
    }

    // Скачивает APK в приватную директорию приложения (updates/), сообщая прогресс 0..100.
    suspend fun downloadApk(
        context: Context,
        release: AppVersionResponse,
        onProgress: (Int) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
        val updatesDir = File(baseDir, "updates").apply { mkdirs() }
        val destFile = File(updatesDir, "nexium-update-${release.versionCode}.apk")

        val request = Request.Builder().url(resolveApkUrl(release.apkUrl)).build()
        downloadClient.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) throw java.io.IOException("HTTP ${resp.code}")
            val bodyStream = resp.body?.byteStream() ?: throw java.io.IOException("Пустой ответ")
            val totalBytes = resp.body?.contentLength() ?: -1L
            var readBytes = 0L

            destFile.outputStream().use { out ->
                val buffer = ByteArray(8 * 1024)
                while (true) {
                    val read = bodyStream.read(buffer)
                    if (read == -1) break
                    out.write(buffer, 0, read)
                    readBytes += read
                    if (totalBytes > 0) {
                        onProgress(((readBytes * 100) / totalBytes).toInt())
                    }
                }
            }
        }
        destFile
    }

    fun enqueueUpdateStatus(
        context: Context,
        status: String,
        targetVersionCode: Int,
        targetVersionName: String? = null,
        apkUrl: String? = null,
        message: String? = null
    ) {
        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(UPDATE_STATUS_PREFS, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_STATUS_PENDING, true)
            .putString(KEY_STATUS, status)
            .putInt(KEY_TARGET_VERSION_CODE, targetVersionCode)
            .putString(KEY_TARGET_VERSION_NAME, targetVersionName)
            .putInt(KEY_INSTALLED_VERSION_CODE, BuildConfig.VERSION_CODE)
            .putString(KEY_INSTALLED_VERSION_NAME, BuildConfig.VERSION_NAME)
            .putString(KEY_APK_URL, apkUrl)
            .putString(KEY_MESSAGE, message)
            .putLong(KEY_REPORTED_AT, System.currentTimeMillis())
            .apply()

        sendQueuedUpdateStatusAsync(appContext)
    }

    fun sendQueuedUpdateStatusAsync(context: Context) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            sendQueuedUpdateStatus(appContext)
        }
    }

    suspend fun sendQueuedUpdateStatus(context: Context): Boolean = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        NexApiClient.init(appContext)

        val prefs = appContext.getSharedPreferences(UPDATE_STATUS_PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_STATUS_PENDING, false)) return@withContext true

        val targetVersionCode = prefs.getInt(KEY_TARGET_VERSION_CODE, -1)
        if (targetVersionCode <= 0) {
            prefs.edit().clear().apply()
            return@withContext true
        }

        val request = AppUpdateStatusRequest(
            status = prefs.getString(KEY_STATUS, "unknown") ?: "unknown",
            targetVersionCode = targetVersionCode,
            targetVersionName = prefs.getString(KEY_TARGET_VERSION_NAME, null),
            installedVersionCode = prefs.getInt(KEY_INSTALLED_VERSION_CODE, BuildConfig.VERSION_CODE),
            installedVersionName = prefs.getString(KEY_INSTALLED_VERSION_NAME, BuildConfig.VERSION_NAME)
                ?: BuildConfig.VERSION_NAME,
            apkUrl = prefs.getString(KEY_APK_URL, null),
            message = prefs.getString(KEY_MESSAGE, null),
            packageName = appContext.packageName,
            deviceManufacturer = Build.MANUFACTURER ?: "",
            deviceModel = Build.MODEL ?: "",
            androidSdk = Build.VERSION.SDK_INT,
            reportedAt = prefs.getLong(KEY_REPORTED_AT, System.currentTimeMillis())
        )

        try {
            val response = NexApiClient.service.reportAppUpdateStatus(NexApiClient.deviceToken, request)
            if (response.isSuccessful) {
                prefs.edit().clear().apply()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // true, если система разрешает установку APK из этого приложения (Android 8+).
    // Если false — нужно сначала направить пользователя в настройки через
    // unknownSourcesSettingsIntent().
    fun canInstallPackages(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun unknownSourcesSettingsIntent(context: Context): Intent {
        return Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }

    // Запускает системный установщик пакетов для скачанного APK через FileProvider.
    fun installApk(context: Context, apkFile: File) {
        val apkUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
