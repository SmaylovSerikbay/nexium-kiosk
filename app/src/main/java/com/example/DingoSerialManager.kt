package com.example

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.*

/**
 * Dingo E-200 / MAX40 Protocol USB Serial Manager
 * 4800 BAUD, 8N1, NO PARITY
 */
object DingoSerialManager {

    private const val TAG = "DingoSerial"
    private const val ACTION_USB_PERMISSION = "com.example.USB_PERMISSION_DINGO"

    val statusText  = mutableStateOf("Не подключён")
    val isConnected = mutableStateOf(false)
    val isMeasuring = mutableStateOf(false)

    private var port: UsbSerialPort? = null
    private var measureJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var receiver: BroadcastReceiver? = null

    // MAX40 commands
    private const val CMD_START = "\$STARTSENTECH\r\n"
    private const val CMD_FAST  = "\$FASTSENTECH\r\n"
    private const val CMD_STOP  = "\$STOPSENTECH\r\n"

    // MAX40 responses
    private const val RESP_READY   = "\$END"
    private const val RESP_CALIB   = "\$CALIBRATION"
    private const val RESP_WAIT    = "\$WAIT"
    private const val RESP_STANBY  = "\$STANBY"
    private const val RESP_TRIGGER = "\$TRIGGER"
    private const val RESP_BREATH  = "\$BREATH"
    private const val RESP_PASS    = "\$R:PASS"
    private const val RESP_FAIL    = "\$R:FAIL"
    private const val RESP_TIMEOUT = "\$TIME,OUT"
    private const val RESP_FLOW    = "\$FLOW,ERR"

    /** USB device is physically visible (no permission needed to check) */
    fun isDeviceAvailable(context: Context): Boolean {
        return try {
            val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            UsbSerialProber.getDefaultProber().findAllDrivers(usbManager).isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "isDeviceAvailable error: ${e.message}")
            false
        }
    }

    /** Android has already granted permission for the device */
    fun hasPermission(context: Context): Boolean {
        return try {
            val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            val drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
            drivers.isNotEmpty() && usbManager.hasPermission(drivers[0].device)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Request USB permission.
     * Shows the Android system "Allow USB access?" dialog.
     * Safe to call on any Android version.
     */
    fun requestPermission(
        context: Context,
        onGranted: () -> Unit,
        onDenied: (String) -> Unit
    ) {
        try {
            val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            val drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)

            if (drivers.isEmpty()) {
                onDenied("USB устройство не найдено")
                return
            }

            val device: UsbDevice = drivers[0].device

            // Already permitted — skip dialog
            if (usbManager.hasPermission(device)) {
                Log.d(TAG, "Already has permission")
                onGranted()
                return
            }

            // Unregister stale receiver
            safeUnregister(context)

            // Create receiver BEFORE registering
            val br = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context, intent: Intent) {
                    if (intent.action != ACTION_USB_PERMISSION) return
                    safeUnregister(ctx)
                    val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    Log.d(TAG, "USB permission result: $granted")
                    if (granted) {
                        statusText.value = "Разрешение получено ✓"
                        onGranted()
                    } else {
                        statusText.value = "Разрешение отклонено"
                        onDenied("Нажмите 'Разрешить' в диалоге USB")
                    }
                }
            }
            receiver = br

            // Register receiver — wrapped in try/catch for safety
            val filter = IntentFilter(ACTION_USB_PERMISSION)
            try {
                // Android 14+ requires RECEIVER_NOT_EXPORTED for non-system broadcasts
                if (Build.VERSION.SDK_INT >= 34) {
                    context.registerReceiver(br, filter, Context.RECEIVER_NOT_EXPORTED)
                } else if (Build.VERSION.SDK_INT >= 33) {
                    context.registerReceiver(br, filter, Context.RECEIVER_NOT_EXPORTED)
                } else {
                    context.registerReceiver(br, filter)
                }
            } catch (e: Exception) {
                Log.e(TAG, "registerReceiver failed: ${e.message}")
                receiver = null
                onDenied("Ошибка: ${e.message}")
                return
            }

            // Build PendingIntent — explicit intent required on Android 14+
            val permIntent = PendingIntent.getBroadcast(
                context,
                0,
                Intent(ACTION_USB_PERMISSION).apply {
                    setPackage(context.packageName) // explicit — required on API 34+
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            statusText.value = "Запрос разрешения USB…"
            usbManager.requestPermission(device, permIntent)
            Log.d(TAG, "requestPermission sent")

        } catch (e: Exception) {
            Log.e(TAG, "requestPermission crashed: ${e.message}", e)
            onDenied("Ошибка запроса разрешения: ${e.message}")
        }
    }

    /**
     * Open the USB serial port (call after permission granted).
     */
    fun connect(context: Context): Boolean {
        return try {
            val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            val drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
            if (drivers.isEmpty()) {
                statusText.value = "USB устройство не найдено"
                return false
            }
            val driver = drivers[0]
            val connection = usbManager.openDevice(driver.device)
            if (connection == null) {
                statusText.value = "openDevice вернул null — нет разрешения"
                Log.e(TAG, "openDevice returned null")
                return false
            }
            val p = driver.ports[0]
            p.open(connection)
            p.setParameters(4800, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
            port = p
            isConnected.value = true
            statusText.value = "Подключён ✓"
            Log.d(TAG, "Connected to ${driver.device.deviceName}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "connect error: ${e.message}", e)
            statusText.value = "Ошибка подключения: ${e.message}"
            false
        }
    }

    fun disconnect() {
        measureJob?.cancel()
        try { port?.close() } catch (_: Exception) {}
        port = null
        isConnected.value = false
        isMeasuring.value = false
        statusText.value = "Не подключён"
    }

    /**
     * Full flow: request permission if needed → connect → measure.
     */
    fun startMeasurement(
        context: Context,
        fastTest: Boolean = false,
        onStatusUpdate: (String) -> Unit,
        onResult: (Double) -> Unit,
        onError: (String) -> Unit
    ) {
        if (isConnected.value) {
            doMeasure(fastTest, onStatusUpdate, onResult, onError)
            return
        }

        requestPermission(
            context = context,
            onGranted = {
                if (connect(context)) {
                    doMeasure(fastTest, onStatusUpdate, onResult, onError)
                } else {
                    onError(statusText.value)
                }
            },
            onDenied = { reason ->
                onError(reason)
            }
        )
    }

    fun stopMeasurement() {
        try { sendCommand(CMD_STOP) } catch (_: Exception) {}
        measureJob?.cancel()
        isMeasuring.value = false
    }

    // ─── Private ──────────────────────────────────────────────────────────────

    private fun doMeasure(
        fastTest: Boolean,
        onStatusUpdate: (String) -> Unit,
        onResult: (Double) -> Unit,
        onError: (String) -> Unit
    ) {
        isMeasuring.value = true
        measureJob?.cancel()
        measureJob = scope.launch {
            try {
                sendCommand(measurementCommand(fastTest))
                withContext(Dispatchers.Main) { onStatusUpdate("Команда отправлена…") }

                val buf = StringBuilder()
                val raw = ByteArray(256)
                var elapsed = 0
                val maxWait = 90_000

                while (elapsed < maxWait) {
                    val n = try { port?.read(raw, 200) ?: 0 } catch (_: Exception) { 0 }
                    if (n > 0) buf.append(String(raw, 0, n, Charsets.US_ASCII))

                    while (buf.contains("\n")) {
                        val i = buf.indexOf("\n")
                        val line = buf.substring(0, i).trim()
                        buf.delete(0, i + 1)
                        if (line.isEmpty()) continue
                        Log.d(TAG, "← $line")
                        if (processLine(line, onStatusUpdate, onResult, onError)) return@launch
                    }
                    elapsed += 200
                }

                withContext(Dispatchers.Main) {
                    isMeasuring.value = false
                    onError("Таймаут 90 сек")
                }
            } catch (e: Exception) {
                Log.e(TAG, "measure error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    isMeasuring.value = false
                    onError("Ошибка: ${e.message}")
                }
            }
        }
    }

    /** Returns true for terminal responses (result / error). */
    private suspend fun processLine(
        line: String,
        onStatusUpdate: (String) -> Unit,
        onResult: (Double) -> Unit,
        onError: (String) -> Unit
    ): Boolean = withContext(Dispatchers.Main) {
        when {
            line.startsWith(RESP_READY)   -> { onStatusUpdate("✓ Готов"); false }
            line.startsWith(RESP_CALIB)   -> { onStatusUpdate("Калибровка…"); false }
            line.startsWith(RESP_WAIT)    -> { onStatusUpdate("Стабилизация…"); false }
            line.startsWith(RESP_STANBY)  -> { onStatusUpdate("Дуйте в мундштук!"); false }
            line.startsWith(RESP_TRIGGER) -> { onStatusUpdate("Выдох зафиксирован…"); false }
            line.startsWith(RESP_BREATH)  -> { onStatusUpdate("Анализ воздуха…"); false }
            line.startsWith(RESP_PASS)    -> {
                isMeasuring.value = false; onStatusUpdate("ПРОЙДЕН ✓"); onResult(0.00); true
            }
            line.startsWith(RESP_FAIL)    -> {
                isMeasuring.value = false; onStatusUpdate("НЕ ПРОЙДЕН ✗"); onResult(0.50); true
            }
            line.startsWith("\$R:")        -> {
                val v = parseResult(line)
                isMeasuring.value = false
                onStatusUpdate("Результат: ${String.format("%.3f", v)} mg/L")
                onResult(v); true
            }
            line.startsWith(RESP_TIMEOUT) -> { isMeasuring.value = false; onError("Таймаут: нет выдоха"); true }
            line.startsWith(RESP_FLOW)    -> { isMeasuring.value = false; onError("Ошибка потока"); true }
            line.startsWith("\$BAT,LOW")  -> { isMeasuring.value = false; onError("Батарея разряжена"); true }
            line.startsWith("\$SENSOR,ERR") || line.startsWith("\$SYSTEM,ERR") -> {
                isMeasuring.value = false; onError("Ошибка датчика: $line"); true
            }
            else -> false
        }
    }

    private fun sendCommand(cmd: String) {
        port?.write(cmd.toByteArray(Charsets.US_ASCII), 1000)
    }

    internal fun measurementCommand(fastTest: Boolean) = if (fastTest) CMD_FAST else CMD_START

    private fun safeUnregister(context: Context) {
        receiver?.let {
            try { context.unregisterReceiver(it) } catch (_: Exception) {}
            receiver = null
        }
    }

    private fun parseResult(line: String): Double {
        return try {
            val payload = line.removePrefix("\$R:").trim()
            val parts = payload.split(",")
            val raw = parts.getOrElse(0) { "0" }.trim()
            val unit = parts.getOrElse(2) { "M" }.trim()
            if (raw.equals("OVER", ignoreCase = true)) return 2.501
            when (unit) {
                "M" -> (raw.toIntOrNull()?.div(1000.0)) ?: (raw.toDoubleOrNull() ?: 0.0)
                else -> raw.toDoubleOrNull() ?: 0.0
            }
        } catch (_: Exception) { 0.0 }
    }
}
