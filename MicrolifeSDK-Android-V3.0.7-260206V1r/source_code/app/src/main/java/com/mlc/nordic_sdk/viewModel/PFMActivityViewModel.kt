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
import com.mlc.nordic_sdk.protocol.protocol_code.PFMProtocol
import com.mlc.nordic_sdk.bluetooth.BluetoothManager
import com.mlc.nordic_sdk.bluetooth.data.DeviceType
import kotlinx.coroutines.launch

class PFMActivityViewModel: ViewModel() {
    private val TAG = "PFMActivityViewModel"
    private var bluetoothManager: BluetoothManager? = null
    private var deviceName: String? = ""
    var state by mutableStateOf(false)

    // Input protocol key
    private var pfmProtocol: PFMProtocol? = PFMProtocol.getInstance("")

    private val _listData = MutableLiveData<List<String>>(emptyList())
    val listData: LiveData<List<String>>
        get() = _listData

    fun setOnDataResponseListener(listener: PFMProtocol.OnDataResponseListener) {
        pfmProtocol?.setOnDataResponseListener(listener)
    }

    fun setBleReceiveManager(bluetoothManager: BluetoothManager?) {
        this.bluetoothManager = bluetoothManager

        startScan()
    }

    fun setDeviceName(deviceName: String?) {
        this.deviceName = deviceName
    }

    private fun startScan() {
        bluetoothManager?.startScan(scanDeviceType = DeviceType.MLC_PF2)
    }

    fun stopScan() {
        bluetoothManager?.stopScan()
    }

    //update UI
    fun addDataList(msg: String) {
        Log.e("addList", msg)

        _listData.value = _listData.value?.let { it + listOf(msg) }
    }

    //bluetooth function
    private fun writeCommand(data: ByteArray?) {
        addDataList("WRITE -> ${data?.let { Method.byteArrayToString(it) }}")

        viewModelScope.launch {
            bluetoothManager?.writeCommand(data)
        }
    }

    //pfm function
    fun readAllHistory() {
        addDataList("Read all history")

        writeCommand(pfmProtocol?.readAllHistoryData())
    }

    fun clearAllHistory() {
        addDataList("Clear all history")

        writeCommand(pfmProtocol?.clearAllHistoryData())
    }

    fun disconnectBluetooth() {
        writeCommand(pfmProtocol?.disconnectBluetooth())
    }

    fun readUserIdAndVersionData() {
        addDataList("Read user and version data")

        writeCommand(pfmProtocol?.readUserIdAndVersionData())
    }

    fun writeNewUser() {
        addDataList("Write ner user id")

        writeCommand(pfmProtocol?.writeNewUser())
    }

    fun readLastData() {
        addDataList("Read last data")

        writeCommand(pfmProtocol?.readLastData())
    }

    fun clearLastData() {
        addDataList("Clear last data")

        writeCommand(pfmProtocol?.clearLastData())
    }

    fun readDeviceTime() {
        addDataList("Read device time")

        writeCommand(pfmProtocol?.readDeviceTime())
    }

    fun writeDeviceTime() {
        addDataList("Write device time")

        writeCommand(pfmProtocol?.writeDeviceTime())
    }

    fun readSerialNumber() {
        addDataList("Read SN")

        writeCommand(pfmProtocol?.readSerialNumber())
    }

    fun readBestValue() {
        addDataList("Read best value")

        writeCommand(pfmProtocol?.readBestValue())
    }

    fun writeBestValue() {
        addDataList("Write best value")

        writeCommand(pfmProtocol?.writeBestValue())
    }

    fun checkMode() {
        addDataList("Check mode")

        writeCommand(pfmProtocol?.checkMode())
    }

    fun startMeasurement() {
        addDataList("Start measurement")

        writeCommand(pfmProtocol?.startMeasurement())
    }

    fun readWaveform() {
        addDataList("Read waveform")

        writeCommand(pfmProtocol?.readWaveform())
    }

    fun solveData(data: List<Byte>) {
        addDataList("NOTIFY -> ${Method.listByteToString(data)}")

        this.pfmProtocol?.solveDataResult(data)
    }
}