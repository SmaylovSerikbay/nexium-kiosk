package com.mlc.nordic_sdk.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mlc.nordic_sdk.bluetooth.BluetoothManager
import com.mlc.nordic_sdk.bluetooth.data.DeviceType
import com.mlc.nordic_sdk.Method
import com.mlc.nordic_sdk.protocol.protocol_code.WBPProtocol
import com.mlc.nordic_sdk.protocol.protocol_code.data.wbp.MeasurementSetting
import com.mlc.nordic_sdk.protocol.protocol_code.data.wbp.NocturnalInfo
import kotlinx.coroutines.launch
import java.security.SecureRandom

class WBPActivityViewModel: ViewModel() {
    private val TAG = "WBPActivityViewModel"
    private var bluetoothManager: BluetoothManager? = null
    private var deviceName: String? = ""
    private var deviceType: DeviceType? = null
    var state by mutableStateOf(false)

    private var wbpProtocol: WBPProtocol? = null

    private val _listData = MutableLiveData<List<String>>(emptyList())
    val listData: LiveData<List<String>>
        get() = _listData

    fun setBleReceiveManager(bluetoothManager: BluetoothManager?) {
        this.bluetoothManager = bluetoothManager

        startScan()
    }

    fun setDeviceInfo(deviceName: String, deviceType: DeviceType?) {
        this.deviceName = deviceName
        this.deviceType = deviceType
    }

    fun setWBPProtocol(listener: WBPProtocol.OnDataResponseListener) {
        // Input protocol key
        this.wbpProtocol = WBPProtocol.getInstance("", listener)
    }

    private fun startScan() {
        bluetoothManager?.startScan(DeviceType.MLC_WBP)
    }

    fun stopScan() {
        bluetoothManager?.stopScan()
    }

    //update UI
    fun addDataList(msg: String) {
        Log.e("addList", msg)

        _listData.value = _listData.value?.let { it + listOf(msg) }
    }

    fun writeCommand(data: ByteArray?) {
        addDataList("WRITE -> ${data?.let { Method.byteArrayToString(it) }}")

        viewModelScope.launch {
            bluetoothManager?.writeCommand(data)
        }
    }

    //WBP
    fun readUsualModeHistory() {
        addDataList("ReadUsualModeHistory")
        writeCommand(wbpProtocol?.readUsualModeHistory())
    }

    fun readUsualModeHistoryEachMeasurement() {
        addDataList("ReadUsualModeHistoryEachMeasurement")
        writeCommand(wbpProtocol?.readUsualModeHistoryEachMeasurement())
    }

    fun readDiagnosticModeHistory() {
        addDataList("ReadDiagnosticModeHistory")
        writeCommand(wbpProtocol?.readDiagnosticModeHistory())
    }

    fun clearSelectedModeHistory() {
        val randomU = kotlin.random.Random.nextBoolean()
        val randomD = kotlin.random.Random.nextBoolean()
        val randomN = kotlin.random.Random.nextBoolean()

        addDataList("ClearSelectedModeHistory" +
                "\nclearUsualMode: $randomU" +
                "\nclearDiagnosticMode: $randomD" +
                "\nclearNocturnalMode: $randomN"
        )

        writeCommand(wbpProtocol?.clearSelectedModeHistory(usual = randomU, diagnostic = randomD, nocturnal = randomN))
    }

    fun clearCurrentModeHistory() {
        addDataList("ClearCurrentModeHistory")
        writeCommand(wbpProtocol?.clearCurrentModeHistory())
    }

    fun disconnectBluetooth() {
        addDataList("DisconnectBluetooth")
        writeCommand(wbpProtocol?.disconnectBluetooth())
    }

    fun writeDeviceTime() {
        addDataList("WriteDeviceTime")
        writeCommand(wbpProtocol?.writeDeviceTime())
    }

    fun writeNewUserID() {
        val id = generateID()
        addDataList("WriteNewUserID: $id")
        writeCommand(wbpProtocol?.writeNewUserID(id))
    }

    private fun generateID(): String {
        val random = SecureRandom()

        val digits = (1..9)
            .map { random.nextInt(10) }
            .joinToString("")

        val letters = ('A'..'Z')
            .map { it }
            .shuffled(random)
            .take(2)
            .joinToString("")

        return digits + letters
    }

    fun readNocturnalModeSetting() {
        addDataList("ReadNocturnalModeSetting")
        writeCommand(wbpProtocol?.readNocturnalModeSetting())
    }

    fun changeNocturnalModeSetting() {
        addDataList("ChangeNocturnalModeSetting")
        writeCommand(wbpProtocol?.changeNocturnalModeSetting(
            NocturnalInfo(
                openNocturnal = 1,
                startYear = 2018,
                startMonth = 11,
                startDay = 5,
                startHour = 6
            ))
        )
    }

    fun readMeasurementSetting() {
        addDataList("ReadMeasurementSetting")
        writeCommand(wbpProtocol?.readMeasurementSetting())
    }

    fun writeMeasurementSetting(
        measurement_times: Int,
        rest_time: Int,
        interval_time_seconds: Int,
        exclude_average: Int,
        afib_option: Int
    ) {
        addDataList("WriteMeasurementSetting: $measurement_times, $rest_time, $interval_time_seconds, $exclude_average, $afib_option")
        writeCommand(wbpProtocol?.writeMeasurementSetting(
            measurement_times,
            rest_time,
            interval_time_seconds,
            exclude_average,
            afib_option
        ))
    }

    fun readDeviceIDAndInfo() {
        addDataList("ReadDeviceIDAndInfo")
        writeCommand(wbpProtocol?.readDeviceIDAndInfo())
    }

    fun readDeviceTime() {
        addDataList("ReadDeviceTime")
        writeCommand(wbpProtocol?.readDeviceTime())
    }

    fun readUserIDAndVersionData() {
        addDataList("ReadUserIDAndVersionData")
        writeCommand(wbpProtocol?.readUserIDAndVersionData())
    }

    fun readNocturnalModeHistory() {
        addDataList("ReadNocturnalModeHistory")
        writeCommand(wbpProtocol?.readNocturnalModeHistory())
    }

    fun readSerialNumber() {
        addDataList("ReadSerialNumber")
        writeCommand(wbpProtocol?.readSerialNumber())
    }

    fun solveData(data: List<Byte>) {
        addDataList("NOTIFY -> ${Method.listByteToString(data)}")

        wbpProtocol?.solveDataResult(data)
    }
}