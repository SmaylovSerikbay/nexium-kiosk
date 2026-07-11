package com.mlc.nordic_sdk.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mlc.nordic_sdk.Method
import com.mlc.nordic_sdk.protocol.protocol_code.BaseProtocol
import com.mlc.nordic_sdk.bluetooth.BluetoothManager
import com.mlc.nordic_sdk.bluetooth.data.DeviceType
import kotlinx.coroutines.launch

class BaseActivityViewModel: ViewModel() {
    private val TAG = "BaseActivityViewModel"
    private var bluetoothManager: BluetoothManager? = null
    private var deviceName: String? = ""
    var state by mutableStateOf(false)
    var deviceType: DeviceType? by mutableStateOf(null)

    private var baseProtocol: BaseProtocol? = null

    //UI variable
    private val _listData = MutableLiveData<List<String>>(emptyList())
    val listData: LiveData<List<String>>
        get() = _listData

    fun setBleReceiveManager(bluetoothManager: BluetoothManager?) {
        this.bluetoothManager = bluetoothManager

        startScan()
    }

    fun setDeviceInfo(deviceName: String, deviceType: DeviceType?, isBPM4G: Boolean?) {
        this.deviceName = deviceName
        this.deviceType = deviceType
    }

    fun setBaseProtocol(listener: BaseProtocol.OnDataResponseListener) {
        // Input protocol key
        this.baseProtocol = BaseProtocol.getInstance(
            "",
            "${deviceType?.name}",
            listener)
    }

    fun startScan() {
        bluetoothManager?.startScan()
    }

    fun stopScan() {
        bluetoothManager?.stopScan()
    }

    fun disconnectGatt() {
        bluetoothManager?.disconnectGatt()
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

    // method
    //bpm function
    fun readBPMUserAndVersionData() {
        addDataList("Read user and version data")

        writeCommand(baseProtocol?.readBPMUserAndVersionData())
    }

    fun readBPMAllHistory() {
        addDataList("Read all history")

        writeCommand(baseProtocol?.readBPMAllHistory())
    }

    fun readBPMDeviceInfo() {
        addDataList("Read device info")

        writeCommand(baseProtocol?.readBPMDeviceInfo())
    }

    fun readBPMDeviceTime() {
        addDataList("Read device time")

        writeCommand(baseProtocol?.readBPMDeviceTime())
    }

    fun clearBPMAllHistory() {
        addDataList("Clear all history")

        writeCommand(baseProtocol?.clearBPMAllHistory())
    }

    fun disconnect() {
        writeCommand(baseProtocol?.disconnectBPM())
    }

    fun writeBPMUserId() {
        addDataList("Write user id")

        writeCommand(baseProtocol?.writeBPMUserData("b1001", 30))
    }

    fun readPFMHistory() {
        addDataList("Read PFM history")

        writeCommand(baseProtocol?.readPFMHistory())
    }

    fun writeBestValue(bestValue: Int) {
        addDataList("Write best value")

        writeCommand(baseProtocol?.writePFMBestValue(bestValue))
    }

    fun readPFMBestValue() {
        addDataList("Read best value")

        writeCommand(baseProtocol?.readPFMBestValue())
    }

    fun writePFMDeviceTime() {
        addDataList("Write device time")

        writeCommand(baseProtocol?.writePFMDeviceTime())
    }

    fun clearPFMHistory() {
        addDataList("Clear history")

        writeCommand(baseProtocol?.clearPFMHistory(1))
    }

    //WBP
    fun readUsualModeHistory() {
        writeCommand(baseProtocol?.readWBPUsualModeHistory())
    }

    fun readDiagnosticModeHistory() {
        writeCommand(baseProtocol?.readWBPDiagnosticModeHistory())
    }

    fun clearSelectedModeHistory() {
        writeCommand(baseProtocol?.clearWBPSelectedModeHistory(usual = true, diagnostic = true))
    }

    fun disconnectBluetooth() {
        writeCommand(baseProtocol?.disconnectWBPBluetooth())
    }

    fun writeDeviceTime() {
        writeCommand(baseProtocol?.writeWBPDeviceTime())
    }

    fun writeNewUserID() {
        writeCommand(baseProtocol?.writeWBPNewUserID())
    }

    fun readDeviceIDAndInfo() {
        writeCommand(baseProtocol?.readWBPDeviceIDAndInfo())
    }

    fun readDeviceTime() {
        writeCommand(baseProtocol?.readWBPDeviceTime())
    }

    fun readUserIDAndVersionData() {
        writeCommand(baseProtocol?.readWBPUserIDAndVersionData())
    }

    fun readSN() {
        writeCommand(baseProtocol?.readSN())
    }

    fun readWBPSN() {
        writeCommand(baseProtocol?.readWBPSN())
    }

    fun solveData(data: List<Byte>) {
        addDataList("NOTIFY -> ${Method.listByteToString(data)}")

        this.baseProtocol?.solveDataResult(data)
    }
}