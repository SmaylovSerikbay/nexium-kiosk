package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
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
            val response = NexApiClient.service.getLatestAppVersion(NexApiClient.deviceToken)
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
