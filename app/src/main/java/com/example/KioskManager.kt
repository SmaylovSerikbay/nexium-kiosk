package com.example

import android.app.admin.DevicePolicyManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInstaller
import android.content.pm.ResolveInfo
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
    private const val UPDATE_PREFS = "nex_silent_updates"
    private const val KEY_PENDING_VERSION = "pending_version"
    private const val KEY_PENDING_SINCE = "pending_since"
    private const val PENDING_INSTALL_TIMEOUT_MS = 2 * 60 * 60 * 1000L
    private const val KIOSK_MAX_TIME_TO_LOCK_MS = Long.MAX_VALUE

    fun isDeviceOwner(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            ?: return false
        val isOwner = dpm.isDeviceOwnerApp(context.packageName)
        Log.d(TAG, "isDeviceOwner: $isOwner")
        return isOwner
    }

    // Разрешает приложению работать в Lock Task Mode и назначает MainActivity
    // постоянным HOME-приложением. После загрузки Android запускает HOME сам,
    // поэтому отдельный BOOT_COMPLETED receiver не нужен.
    // Саму блокировку (startLockTask) вызывает MainActivity.onResume.
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

            val homeFilter = IntentFilter(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            val homeActivity = ComponentName(context, MainActivity::class.java)
            dpm.addPersistentPreferredActivity(admin, homeFilter, homeActivity)
            Log.d(TAG, "MainActivity configured as persistent HOME activity")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dpm.setLockTaskFeatures(admin, DevicePolicyManager.LOCK_TASK_FEATURE_NONE)
                Log.d(TAG, "setLockTaskFeatures successful")
            }

            dpm.setMaximumTimeToLock(admin, KIOSK_MAX_TIME_TO_LOCK_MS)
            Log.d(TAG, "Screen timeout disabled for kiosk mode")
        } catch (e: Exception) {
            Log.e(TAG, "Error in configureLockTask", e)
        }
    }

    // Снимает политики, которые мешают обслуживанию устройства вне киоск-режима.
    // Device Owner при этом остаётся назначенным, чтобы киоск можно было включить обратно
    // без factory reset и повторного dpm set-device-owner.
    fun disableKioskPolicies(context: Context) {
        Log.d(TAG, "disableKioskPolicies called")
        if (!isDeviceOwner(context)) {
            Log.w(TAG, "Not a device owner, skipping disableKioskPolicies")
            return
        }
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = KioskDeviceAdminReceiver.componentName(context)
        try {
            dpm.setLockTaskPackages(admin, emptyArray())
            dpm.setMaximumTimeToLock(admin, 0L)
            dpm.clearPackagePersistentPreferredActivities(admin, context.packageName)
            restoreSystemHomeActivity(context, dpm, admin)
            Log.d(TAG, "Kiosk policies disabled")
        } catch (e: Exception) {
            Log.e(TAG, "Error disabling kiosk policies", e)
        }
    }

    private fun restoreSystemHomeActivity(
        context: Context,
        dpm: DevicePolicyManager,
        admin: ComponentName
    ) {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        val homeActivity = context.packageManager
            .queryIntentActivities(homeIntent, 0)
            .filterNot { it.activityInfo.packageName == context.packageName }
            .sortedWith(compareByDescending<ResolveInfo> {
                it.activityInfo.packageName == "com.sec.android.app.launcher"
            }.thenByDescending {
                it.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
            })
            .firstOrNull()
            ?.let { ComponentName(it.activityInfo.packageName, it.activityInfo.name) }

        if (homeActivity == null) {
            Log.w(TAG, "No system HOME activity found to restore")
            return
        }

        val homeFilter = IntentFilter(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        dpm.addPersistentPreferredActivity(admin, homeFilter, homeActivity)
        Log.d(TAG, "Restored system HOME activity: $homeActivity")
    }

    // Тихая установка APK без единого диалога — работает только когда приложение
    // является Device Owner (обычные приложения такого права не имеют по дизайну ОС).
    // Не запускаем второй PackageInstaller session для той же версии, пока первая
    // ещё выполняется. Зависшее состояние автоматически протухает, чтобы обновление
    // можно было повторить без перезапуска приложения.
    fun shouldAttemptSilentInstall(context: Context, versionCode: Int): Boolean {
        val prefs = context.getSharedPreferences(UPDATE_PREFS, Context.MODE_PRIVATE)
        if (prefs.getInt(KEY_PENDING_VERSION, -1) != versionCode) return true

        val pendingSince = prefs.getLong(KEY_PENDING_SINCE, 0L)
        val isStillPending = pendingSince > 0L &&
            System.currentTimeMillis() - pendingSince < PENDING_INSTALL_TIMEOUT_MS
        if (!isStillPending) clearPendingSilentInstall(context, versionCode)
        return !isStillPending
    }

    fun installSilently(context: Context, apkFile: File, versionCode: Int) {
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

            markSilentInstallPending(context, versionCode)

            val intent = Intent(context, InstallResultReceiver::class.java).apply {
                putExtra(InstallResultReceiver.EXTRA_VERSION_CODE, versionCode)
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            val pendingIntent = PendingIntent.getBroadcast(context, sessionId, intent, flags)
            session.commit(pendingIntent.intentSender)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка тихой установки обновления", e)
            AppUpdateManager.enqueueUpdateStatus(
                context = context,
                status = "failed",
                targetVersionCode = versionCode,
                message = e.localizedMessage ?: "PackageInstaller session failed"
            )
            clearPendingSilentInstall(context, versionCode)
            session.abandon()
        } finally {
            session.close()
        }
    }

    private fun markSilentInstallPending(context: Context, versionCode: Int) {
        val saved = context.getSharedPreferences(UPDATE_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_PENDING_VERSION, versionCode)
            .putLong(KEY_PENDING_SINCE, System.currentTimeMillis())
            .commit()
        check(saved) { "Не удалось сохранить состояние тихого обновления" }
    }

    fun clearPendingSilentInstall(context: Context, versionCode: Int) {
        val prefs = context.getSharedPreferences(UPDATE_PREFS, Context.MODE_PRIVATE)
        val pendingVersion = prefs.getInt(KEY_PENDING_VERSION, -1)
        if (versionCode > 0 && pendingVersion != versionCode) return
        prefs.edit()
            .remove(KEY_PENDING_VERSION)
            .remove(KEY_PENDING_SINCE)
            .apply()
    }

    fun relaunchAfterPackageUpdate(context: Context, reason: String) {
        val appContext = context.applicationContext
        val kioskEnabled = appContext
            .getSharedPreferences("nex_settings", Context.MODE_PRIVATE)
            .getBoolean("kiosk_mode_enabled", true)

        if (kioskEnabled) {
            configureLockTask(appContext)
        }

        val launchIntent = appContext.packageManager
            .getLaunchIntentForPackage(appContext.packageName)
            ?: Intent(appContext, MainActivity::class.java)
        launchIntent
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        try {
            appContext.startActivity(launchIntent)
            Log.i(TAG, "Relaunched app after package update: $reason")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to relaunch app after package update: $reason", e)
        }
    }
}

// Получает статус тихой установки от PackageInstaller (см. KioskManager.installSilently).
// Ничего не показывает пользователю — только пишет в лог для диагностики.
class InstallResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
        val versionCode = intent.getIntExtra(EXTRA_VERSION_CODE, -1)
        KioskManager.clearPendingSilentInstall(context, versionCode)
        if (status == PackageInstaller.STATUS_SUCCESS) {
            Log.i("InstallResultReceiver", "Обновление до versionCode=$versionCode установлено успешно")
            AppUpdateManager.enqueueUpdateStatus(
                context = context,
                status = "installed",
                targetVersionCode = versionCode,
                message = "PackageInstaller reported success"
            )
            KioskManager.relaunchAfterPackageUpdate(context, "silent install versionCode=$versionCode")
        } else {
            Log.e(
                "InstallResultReceiver",
                "Ошибка установки versionCode=$versionCode: status=$status message=$message"
            )
            AppUpdateManager.enqueueUpdateStatus(
                context = context,
                status = "failed",
                targetVersionCode = versionCode,
                message = "PackageInstaller status=$status message=$message"
            )
        }
    }

    companion object {
        const val EXTRA_VERSION_CODE = "com.example.extra.UPDATE_VERSION_CODE"
    }
}

class PackageReplacedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        Log.i("PackageReplacedReceiver", "Package replaced, relaunching app")
        AppUpdateManager.enqueueUpdateStatus(
            context = context,
            status = "installed",
            targetVersionCode = BuildConfig.VERSION_CODE,
            targetVersionName = BuildConfig.VERSION_NAME,
            message = "MY_PACKAGE_REPLACED received"
        )
        KioskManager.relaunchAfterPackageUpdate(context, "MY_PACKAGE_REPLACED")
    }
}
