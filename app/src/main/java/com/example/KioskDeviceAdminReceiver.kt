package com.example

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent

// Требуется системой для работы Device Owner (см. KioskManager.kt).
// Сам по себе этот класс ничего не делает — вся логика лочки/тихой установки
// живёт в KioskManager, которому просто нужен ComponentName этого ресивера.
class KioskDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
    }

    companion object {
        fun componentName(context: Context): ComponentName =
            ComponentName(context.applicationContext, KioskDeviceAdminReceiver::class.java)
    }
}
