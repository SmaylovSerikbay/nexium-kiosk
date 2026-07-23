package com.example

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.mlc.nordic_sdk.XlogUtils
import com.mlc.nordic_sdk.bluetooth.BluetoothManager
import com.mlc.nordic_sdk.bluetooth.OnIMBluetoothLEListener
import com.mlc.nordic_sdk.bluetooth.data.ConnectState
import com.mlc.nordic_sdk.bluetooth.data.DeviceType
import com.mlc.nordic_sdk.protocol.protocol_code.ThermoProtocol
import com.mlc.nordic_sdk.protocol.protocol_code.data.thermo.CalibrateParameter
import com.mlc.nordic_sdk.protocol.protocol_code.data.thermo.ThermoMeasureData
import kotlinx.coroutines.*

/**
 * Microlife NC-150 BT — Microlife Nordic SDK v3.0.7
 *
 * КАК РАБОТАЕТ SDK (из анализа байткода BluetoothManager):
 *   1. BluetoothManager.startScan(MLC_Thermo) → запускает BLE-скан
 *   2. При нахождении устройства SDK СНАЧАЛА вызывает наш onScanResult,
 *      ПОТОМ сам создаёт BluetoothConnection и вызывает .connect()
 *      (никаких дополнительных вызовов с нашей стороны не нужно)
 *   3. BluetoothConnection.initialize() включает notify на GATT-характеристике
 *   4. Данные приходят в onReceivedBleDataResult — мы ОБЯЗАНЫ передать их в
 *      ThermoProtocol.solveDataResult(data) — он распарсит и вызовет наш
 *      onResponseUploadMeasureData(ThermoMeasureData)
 *   5. В onWriteCommand SDK просит нас записать команду обратно на устройство —
 *      вызываем btManager?.writeCommand(cmd)
 *
 * ИСПРАВЛЕННЫЕ БАГИ:
 *   - Убран ручной парсинг пакетов (неправильный формат)
 *   - Данные теперь передаются в thermoProtocol.solveDataResult()
 *   - Keepalive удалён (вызывал сброс соединения)
 *   - ThermoProtocol.getInstance передаёт listener напрямую в конструктор
 *   - MAC-фильтрация: если задан MAC — игнорируем другие устройства
 */
object MicrolifeManager {

    private const val TAG = "MicrolifeManager"

    val statusText  = mutableStateOf("Не подключён")
    val isConnected = mutableStateOf(false)
    val isMeasuring = mutableStateOf(false)

    private var btManager: BluetoothManager? = null
    private var thermoProtocol: ThermoProtocol? = null
    private var targetMac: String = ""

    private var onResultCb: ((Double) -> Unit)? = null
    private var onErrorCb:  ((String) -> Unit)? = null

    private var lastWorkMode: Int = -1

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timeoutJob: Job? = null
    private var resultJob: Job? = null
    private var latestResult: Pair<Long, Double>? = null

    // Auto-retry state
    private var retryCount    = 0
    private var currentCtx:   Context? = null
    private var currentMacAddr: String = ""
    private var savedResultCb: ((Double) -> Unit)? = null
    private var savedErrorCb:  ((String) -> Unit)? = null

    // ─── Public API ───────────────────────────────────────────────────────────

    fun connect(
        context: Context,
        macAddress: String,
        onResult: (Double) -> Unit,
        onError:  (String) -> Unit
    ) {
        retryCount = 0
        currentCtx     = context
        currentMacAddr = macAddress
        savedResultCb  = onResult
        savedErrorCb   = onError

        startScan(context, macAddress, onResult, onError)
    }

    private fun startScan(
        context: Context,
        macAddress: String,
        onResult: (Double) -> Unit,
        onError:  (String) -> Unit
    ) {
        onResultCb = onResult
        onErrorCb  = onError
        targetMac  = macAddress.uppercase().trim()

        isMeasuring.value = true
        statusText.value  = "Поиск термометра…"
        lastWorkMode = -1
        resultJob?.cancel()
        latestResult = null

        val activity = context as? Activity
        if (activity == null) {
            onError("Требуется Activity контекст")
            isMeasuring.value = false
            return
        }

        // ВАЖНО: ThermoProtocol(listener) через Kotlin default-args обнуляет listener (bitmask=1)
        // Используем reflection чтобы вызвать приватный primary конструктор напрямую
        thermoProtocol = try {
            val ctor = ThermoProtocol::class.java.getDeclaredConstructor(
                ThermoProtocol.OnDataResponseListener::class.java
            )
            ctor.isAccessible = true
            ctor.newInstance(thermoListener) as ThermoProtocol
        } catch (e: Exception) {
            Log.e(TAG, "Reflection constructor failed: ${e.message} — fallback to setListener")
            // Fallback: создаём с null listener, потом устанавливаем
            val ctor = ThermoProtocol::class.java.getDeclaredConstructors()
                .firstOrNull { it.parameterCount == 3 }
            ctor?.isAccessible = true
            val instance = ctor?.newInstance(null, 1, null) as? ThermoProtocol
            instance
        }
        // Всегда явно устанавливаем listener через публичный метод
        thermoProtocol?.setOnDataResponseListener(thermoListener)
        Log.d(TAG, "ThermoProtocol создан: $thermoProtocol, listener установлен")

        // Инициализируем логгер SDK
        XlogUtils.initXlog(activity, true)

        // Останавливаем предыдущую сессию и даём BLE-стеку время освободиться
        try { btManager?.stopScan()       } catch (_: Exception) {}
        try { btManager?.disconnectGatt() } catch (_: Exception) {}
        btManager = null

        // Создаём новый BluetoothManager (singleton через Companion)
        // SDK внутри onScanResult сам вызывает BluetoothConnection.connect()
        btManager = BluetoothManager.Companion.getInstance(activity, bleListener)

        // Запускаем скан на MLC_Thermo устройства
        btManager?.startScan(DeviceType.MLC_Thermo)
        Log.d(TAG, "Scan started. TargetMAC='$targetMac' (attempt ${retryCount + 1})")

        // 30-секундный таймаут
        timeoutJob?.cancel()
        timeoutJob = scope.launch {
            delay(30_000)
            if (isMeasuring.value) {
                isMeasuring.value = false
                statusText.value = "Таймаут поиска"
                onErrorCb?.invoke(
                    "Термометр не найден за 30 сек.\n" +
                    "Убедитесь что термометр NC-150 включён."
                )
            }
        }
    }

    private fun autoRetry() {
        // Не делаем retry если disconnect() уже был вызван
        if (!isMeasuring.value) {
            Log.d(TAG, "autoRetry отменён — isMeasuring=false (disconnect был вызван)")
            return
        }
        if (retryCount >= 3) {
            isConnected.value = false
            isMeasuring.value = false
            statusText.value = "Ошибка подключения"
            onErrorCb?.invoke("Не удалось подключиться после 3 попыток.\nПопробуйте снова.")
            return
        }
        retryCount++
        Log.d(TAG, "Auto-retry #$retryCount…")
        val ctx = currentCtx ?: return
        val mac = currentMacAddr
        if (mac.isBlank()) return
        startScan(ctx, mac, savedResultCb ?: {}, savedErrorCb ?: {})
    }

    fun disconnect() {
        // Отменяем все coroutine-задачи
        timeoutJob?.cancel()
        timeoutJob = null
        resultJob?.cancel()
        resultJob = null
        latestResult = null
        // Сбрасываем флаги ДО отключения — чтобы autoRetry не запустился снова
        isMeasuring.value = false
        isConnected.value = false
        // Отключаемся от BLE
        try { btManager?.stopScan()       } catch (_: Exception) {}
        try { btManager?.disconnectGatt() } catch (_: Exception) {}
        // ВАЖНО: обнуляем btManager чтобы освободить BLE-стек для других устройств (Omron)
        btManager = null
        thermoProtocol = null
        // Очищаем callbacks чтобы не было утечек
        onResultCb = null
        onErrorCb  = null
        currentCtx = null
        statusText.value = "Не подключён"
        Log.d(TAG, "disconnect() — BLE освобождён")
    }

    // ─── ThermoProtocol listener ──────────────────────────────────────────────

    private val thermoListener = object : ThermoProtocol.OnDataResponseListener {

        override fun onResponseDeviceInfo(macAddress: String, workMode: Int, batteryVoltage: Float) {
            Log.d(TAG, "DeviceInfo: mac=$macAddress workMode=$workMode battery=${batteryVoltage}V")
            lastWorkMode = workMode
            statusText.value = "Готов — нажмите START на термометре"
        }

        override fun onResponseUploadMeasureData(thermoMeasureData: ThermoMeasureData) {
            val data = thermoMeasureData
            val temp = data.measureTemperature.toDouble()
            Log.d(TAG, ">>> MeasureData: temp=$temp flagErr=${data.flagErr} mode=${data.mode} workMode=$lastWorkMode")
            if (data.flagErr != 0) {
                Log.w(TAG, "flagErr=${data.flagErr} — ошибка измерения, пропускаем")
                statusText.value = "Ошибка измерения (flagErr=${data.flagErr})"
                return
            }
            if (temp in 20.0..50.0) {
                val timestamp =
                    data.year.toLong() * 100_000_000 +
                    data.month * 1_000_000 +
                    data.day * 10_000 +
                    data.hour * 100 +
                    data.minute
                if (latestResult == null || timestamp >= latestResult!!.first) {
                    latestResult = timestamp to temp
                }
                resultJob?.cancel()
                resultJob = scope.launch {
                    delay(1_500)
                    latestResult?.second?.let(::deliverResult)
                }
            } else {
                Log.w(TAG, "Неправдоподобная температура=$temp — пропускаем")
                statusText.value = "Неверное значение: ${temp}°C"
            }
        }

        override fun onResponseUploadCalibrate(calibrateParameters: List<CalibrateParameter>) {
            Log.d(TAG, "Calibrate params: ${calibrateParameters.size}")
        }

        override fun onWriteCommand(byteArray: ByteArray) {
            // SDK требует записать эту команду обратно на устройство
            Log.d(TAG, "onWriteCommand: ${byteArray.toHex()} (${byteArray.size} bytes)")
            // Используем Dispatchers.IO чтобы не блокировать Main thread
            // и выполняем немедленно без delay
            scope.launch(Dispatchers.IO) {
                try {
                    val mgr = btManager
                    if (mgr == null) {
                        Log.e(TAG, "writeCommand: btManager is null!")
                        return@launch
                    }
                    mgr.writeCommand(byteArray)
                    Log.d(TAG, "writeCommand OK: ${byteArray.toHex()}")
                } catch (e: Exception) {
                    Log.e(TAG, "writeCommand FAILED: ${e.message}", e)
                }
            }
        }
    }

    // ─── BLE listener ─────────────────────────────────────────────────────────

    private val bleListener = object : OnIMBluetoothLEListener {

        override fun onScanResult(
            device: BluetoothDevice,
            deviceName: String,
            deviceType: DeviceType?,
            macAddress: String?
        ) {
            val mac = (macAddress ?: "").uppercase().trim()
            Log.d(TAG, "onScanResult: name='$deviceName' mac='$mac' type=$deviceType targetMac='$targetMac'")

            // Если задан конкретный MAC — фильтруем
            if (targetMac.isNotBlank() && mac != targetMac) {
                Log.d(TAG, "  → MAC не совпадает, пропускаем")
                return
            }

            // Устройство найдено — SDK сам вызовет connect() после нашего return
            statusText.value = "Найден ${deviceName.ifEmpty { mac }}. Подключение…"
            Log.d(TAG, "  → Устройство принято, SDK подключается…")
        }

        override fun onConnectionState(connectState: ConnectState) {
            val state = connectState
            Log.d(TAG, "onConnectionState: $state")
            when (state) {
                ConnectState.Connected   -> {
                    isConnected.value = true
                    statusText.value  = "Подключено…"
                }
                ConnectState.Bonding     -> statusText.value = "Сопряжение…"
                ConnectState.Bonded      -> statusText.value = "Сопряжено…"
                ConnectState.DeviceReady -> {
                    isConnected.value = true
                    statusText.value  = "✓ Готов — нажмите START на термометре NC-150"
                    Log.d(TAG, "DeviceReady — ждём измерения")
                }
                ConnectState.Disconnect  -> {
                    isConnected.value = false
                    Log.d(TAG, "Disconnect (isMeasuring=${isMeasuring.value})")
                    if (isMeasuring.value) {
                        // Потеря соединения во время ожидания — переподключаемся
                        statusText.value = "Переподключение…"
                        scope.launch {
                            delay(2000)
                            autoRetry()
                        }
                    }
                    // Если isMeasuring==false — результат уже получен, ничего не делаем
                }
                ConnectState.ConnectFailed, ConnectState.ERROR_133 -> {
                    isConnected.value = false
                    statusText.value  = "Ошибка, повтор ${retryCount + 1}/3…"
                    Log.w(TAG, "ConnectFailed/ERROR_133 — auto-retry in 1.5s")
                    scope.launch {
                        delay(1500)
                        autoRetry()
                    }
                }
                else -> Log.d(TAG, "Unhandled state: $state")
            }
        }

        override fun onConnectionState(connectState: ConnectState, state: Int) {
            Log.d(TAG, "onConnectionState: $connectState code=$state")
            onConnectionState(connectState)
        }

        /**
         * ГЛАВНЫЙ МЕТОД: сюда SDK доставляет все BLE-данные с устройства.
         * Передаём их в ThermoProtocol.solveDataResult() — он распарсит
         * и вызовет onResponseUploadMeasureData() или onWriteCommand().
         */
        override fun onReceivedBleDataResult(data: List<Byte>, head: Int?) {
            if (data.isEmpty()) return
            val hex = data.joinToString(" ") { "%02X".format(it) }
            Log.d(TAG, "onReceivedBleDataResult: ${data.size}b head=$head hex=[$hex]")

            val proto = thermoProtocol
            if (proto == null) {
                Log.e(TAG, "thermoProtocol IS NULL — невозможно обработать пакет!")
                return
            }

            // Передаём в официальный SDK-парсер
            Log.d(TAG, "Вызываем solveDataResult...")
            try {
                proto.solveDataResult(data)
                Log.d(TAG, "solveDataResult выполнен успешно")
            } catch (e: Exception) {
                Log.e(TAG, "solveDataResult EXCEPTION: ${e.javaClass.simpleName}: ${e.message}", e)
            }
        }

        override fun onResponseSWRevision(swRevision: String) { Log.d(TAG, "SW=$swRevision") }
        override fun onResponseFWRevision(fwRevision: String) { Log.d(TAG, "FW=$fwRevision") }
        override fun onResponseHWRevision(hwRevision: String) { Log.d(TAG, "HW=$hwRevision") }
        override fun onBtStateChanged(isEnable: Boolean) { Log.d(TAG, "BT enabled=$isEnable") }
        override fun onResponseFailedMessage(msg: String) {
            Log.w(TAG, "FailedMessage: $msg")
            statusText.value = "⚠ $msg"
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun deliverResult(temp: Double) {
        timeoutJob?.cancel()
        isMeasuring.value = false
        isConnected.value = false
        // Термометр на своём экране ОБРЕЗАЕТ второй знак после запятой, а не округляет
        // (36.26 → "36.2", не "36.3"). String.format("%.1f", ...) округляет — из-за этого
        // наше значение расходилось с прибором на 0.1°C. Обрезаем так же, как прибор.
        val cents = Math.round(temp * 100)
        val temp1dp = (cents / 10) / 10.0
        val formatted = String.format("%.1f", temp1dp)
        statusText.value  = "Результат: $formatted °C"
        Log.d(TAG, ">>> RESULT: $temp°C (обрезано до $formatted°C)")
        // onResponseUploadMeasureData приходит из BLE-потока SDK, а не с Main —
        // без этого post() обновление Compose-state из onResultCb молча не применяется.
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            onResultCb?.invoke(temp1dp)
        }
        scope.launch {
            delay(500)
            try { btManager?.disconnectGatt() } catch (_: Exception) {}
        }
    }

    private fun ByteArray.toHex() = joinToString(" ") { "%02X".format(it) }

    // ─── Helper functions used by MainActivity ─────────────────────────────

    /** Returns true if thermometerMode is microlife_ble */
    fun isActiveMode(thermometerMode: String, thermometerName: String): Boolean =
        thermometerMode == "microlife_ble"

    /** Returns true if device name looks like a Microlife thermometer */
    fun isMicrolifeDevice(name: String): Boolean {
        val n = name.uppercase()
        return n.contains("MICROLIFE") || n.contains("MLC") || n.contains("NC-150") ||
               n.contains("NC150") || n.contains("THERMO")
    }
}
