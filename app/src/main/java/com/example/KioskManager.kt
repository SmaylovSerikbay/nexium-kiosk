package com.example

import android.app.admin.DevicePolicyManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import java.io.File

// Централизует всё, что требует прав Device Owner: полную блокировку киоска
// (запрет сворачивания/переключения на другие приложения через Lock Task Mode)
// и тихую установку APK-обновлений без единого системного диалога.
//
// Device Owner нельзя включить из самого приложения — это осознанное ограничение
// безопасности Android. Один раз перед выдачей устройства в эксплуатацию нужно
// выполнить на чистом устройстве (без добавленных Google-аккаунтов):
//
//   adb shell dpm set-device-owner com.aistudio.nexiumhealth.qptwyx/com.example.KioskDeviceAdminReceiver
//
// Если команда выше выдает ошибку "Already has accounts", удалите все Google-аккаунты в настройках.
//   - блокирует экран в Lock Task Mode (недоступны Home/Recents/шторка/переключение приложений)
//   - устанавливает APK-обновления без каких-либо диалогов
//
// Если Device Owner не назначен, приложение продолжает работать в обычном режиме:
// кнопка "назад" перехватывается (см. BackHandler в MainActivity.kt), а обновления
// ставятся через системный диалог установки (см. AppUpdateManager.kt).
object KioskManager {
    private const val TAG = "KioskManager"

    fun isDeviceOwner(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            ?: return false
        val isOwner = dpm.isDeviceOwnerApp(context.packageName)
        Log.d(TAG, "isDeviceOwner: $isOwner")
        return isOwner
    }

    // Разрешает этому приложению работать в Lock Task Mode. Саму блокировку
    // (startLockTask) нужно вызывать из Activity — см. MainActivity.onResume.
    fun configureLockTask(context: Context) {
        Log.d(TAG, "configureLockTask called")
        if (!isDeviceOwner(context)) {
            Log.w(TAG, "Not a device owner, skipping configureLockTask")
            return
        }
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = KioskDeviceAdminReceiver.componentName(context)
        try {
            dpm.setLockTaskPackages(admin, arrayOf(context.packageName))
            Log.d(TAG, "setLockTaskPackages successful")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dpm.setLockTaskFeatures(admin, DevicePolicyManager.LOCK_TASK_FEATURE_NONE)
                Log.d(TAG, "setLockTaskFeatures successful")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in configureLockTask", e)
        }
    }

    // Тихая установка APK без единого диалога — работает только когда приложение
    // является Device Owner (обычные приложения такого права не имеют по дизайну ОС).
    fun installSilently(context: Context, apkFile: File) {
        val packageInstaller = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        params.setAppPackageName(context.packageName)

        val sessionId = packageInstaller.createSession(params)
        val session = packageInstaller.openSession(sessionId)
        try {
            session.openWrite("nexium_update", 0, apkFile.length()).use { out ->
                apkFile.inputStream().use { input -> input.copyTo(out) }
                session.fsync(out)
            }

            val intent = Intent(context, InstallResultReceiver::class.java)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            val pendingIntent = PendingIntent.getBroadcast(context, sessionId, intent, flags)
            session.commit(pendingIntent.intentSender)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка тихой установки обновления", e)
            session.abandon()
        } finally {
            session.close()
        }
    }
}

// Получает статус тихой установки от PackageInstaller (см. KioskManager.installSilently).
// Ничего не показывает пользователю — только пишет в лог для диагностики.
class InstallResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
        if (status == PackageInstaller.STATUS_SUCCESS) {
            Log.i("InstallResultReceiver", "Обновление установлено успешно")
        } else {
            Log.e("InstallResultReceiver", "Ошибка установки обновления: status=$status message=$message")
        }
    }
}
