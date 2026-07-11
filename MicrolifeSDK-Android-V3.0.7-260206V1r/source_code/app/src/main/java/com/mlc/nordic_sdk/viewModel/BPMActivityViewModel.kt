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
import com.mlc.nordic_sdk.protocol.protocol_code.BPMProtocol
import com.mlc.nordic_sdk.bluetooth.BluetoothManager
import com.mlc.nordic_sdk.bluetooth.data.DeviceType
import kotlinx.coroutines.launch

class BPMActivityViewModel: ViewModel() {
    private val TAG = "BPMActivityViewModel"
    private var bluetoothManager: BluetoothManager? = null
    private var deviceName: String? = ""
    var deviceType: DeviceType? by mutableStateOf(null)
    var state by mutableStateOf(false)

    var bpmProtocol: BPMProtocol? = null

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

    fun setBPMProtocol(listener: BPMProtocol.OnDataResponseListener) {
        // Input protocol key
        this.bpmProtocol = BPMProtocol.getInstance("", "${deviceType?.name}", listener)
    }

    private fun startScan() {
        bluetoothManager?.startScan(DeviceType.MLC_ALL)
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

    //bpm function
    fun readUserAndVersionData() {
        addDataList("Read user and version data")

        writeCommand(bpmProtocol?.readUserAndVersionData())
    }

    fun readAllHistory() {
        addDataList("Read all history")

        writeCommand(bpmProtocol?.readAllHistory())
    }

    fun readDeviceInfo() {
        addDataList("Read device info")

        writeCommand(bpmProtocol?.readDeviceInfo())
    }

    fun readDeviceTime() {
        addDataList("Read device time")

        writeCommand(bpmProtocol?.readDeviceTime())
    }

    fun clearAllHistory() {
        addDataList("Clear all history")

        writeCommand(bpmProtocol?.clearAllHistory())
    }

    fun disconnect() {
        writeCommand(bpmProtocol?.disconnect())
    }

    fun readLastData() {
        addDataList("Read last data")

        writeCommand(bpmProtocol?.readLastData())
    }

    fun clearLastData() {
        addDataList("Clear last data")

        writeCommand(bpmProtocol?.clearLastData())
    }

    fun writeUserId() {
        addDataList("Write user id")

        writeCommand(bpmProtocol?.writeUserId("b1001", 30))
    }

    fun writeDeviceTime() {
        addDataList("write device time")

        writeCommand(bpmProtocol?.writeDeviceTime())
    }

    fun readSerialNumber() {
        addDataList("read SN")

        writeCommand(bpmProtocol?.readSerialNumber())
    }

    fun checkTransmitOK() {
        addDataList("check transmit ok")

        writeCommand(bpmProtocol?.checkTransmitOk())
    }

    fun solveData(data: List<Byte>) {
        addDataList("NOTIFY -> ${Method.listByteToString(data)}")

        this.bpmProtocol?.solveDataResult(data)
    }
}