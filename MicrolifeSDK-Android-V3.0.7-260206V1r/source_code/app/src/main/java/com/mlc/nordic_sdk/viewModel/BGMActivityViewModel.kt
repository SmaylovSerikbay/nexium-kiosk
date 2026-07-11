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
import com.mlc.nordic_sdk.bluetooth.BluetoothManager
import com.mlc.nordic_sdk.bluetooth.data.DeviceType
import com.mlc.nordic_sdk.protocol.protocol_code.BGMProtocol
import kotlinx.coroutines.launch

class BGMActivityViewModel: ViewModel() {
    private val TAG = "BGMActivityViewModel"
    private var bluetoothManager: BluetoothManager? = null
    var deviceName: String? = ""
    private var deviceType: DeviceType? = null
    var state by mutableStateOf(false)
    private var bgmProtocol: BGMProtocol? = null

    private val _listData = MutableLiveData<List<String>>(emptyList())
    val listData: LiveData<List<String>>
        get() = _listData

    fun setBleManager(bluetoothManager: BluetoothManager?) {
        this.bluetoothManager = bluetoothManager

        startScan()
    }

    fun setBGMProtocol(bgmProtocol: BGMProtocol?) {
        this.bgmProtocol = bgmProtocol
    }

    private fun startScan() {
        bluetoothManager?.startScan(DeviceType.MLC_BGM)
    }

    fun stopScan() {
        bluetoothManager?.stopScan()
    }

    fun writeCommand(data: ByteArray?) {
        addDataList("WRITE -> ${data?.let { Method.byteArrayToString(it) }}")

        viewModelScope.launch {
            bluetoothManager?.writeCommand(data)
        }
    }

    //update UI
    fun addDataList(msg: String) {
        Log.e("addList", msg)

        _listData.value = _listData.value?.let { it + listOf(msg) }
    }

    fun setDeviceInfo(deviceName: String, deviceType: DeviceType?) {
        this.deviceName = deviceName
        this.deviceType = deviceType
    }

    fun setOnDataResponseListener(onDataResponseListener: BGMProtocol.OnDataResponseListener) {
        this.bgmProtocol?.setOnDataResponseListener(onDataResponseListener)
    }

    //ApexBGM
    fun readDeviceName() {
        addDataList("read device name")

        writeCommand(bgmProtocol?.getDeviceName())
    }

    fun readFirmwareVersion() {
        addDataList("read firmware version")

        writeCommand(bgmProtocol?.getFirmwareVersion())
    }

    fun gettingBLEStatus() {
        addDataList("getting ble status")

        writeCommand(bgmProtocol?.gettingBLEStatus())
    }

    fun transmissionOneMeterData() {
        addDataList("transmission one meter data")

        writeCommand(bgmProtocol?.transmissionOneMeterData(1))
    }

    fun blePowerOff() {
        addDataList("ble power off")

        writeCommand(bgmProtocol?.blePowerOff())
    }

    fun readDeviceTime() {
        addDataList("read device time")

        writeCommand(bgmProtocol?.getDeviceTime())
    }

    fun communicationMode() {
        addDataList("communication mode")

        writeCommand(bgmProtocol?.selectCommunicationMode(1))
    }

    //BeneCheck
    fun checkBGM700Function() {
        addDataList("check BGM700 function")

        writeCommand(bgmProtocol?.checkBGM700Function())
    }

    fun checkBGM700BGMTotal() {
        addDataList("check BGM700 BGM total")

        writeCommand(bgmProtocol?.checkBGM700BGMTotal())
    }
    fun downloadBGM700Data() {
        addDataList("download BGM700 data")

        writeCommand(bgmProtocol?.downloadBGM700Data())
    }

    fun checkBGM700UricAcidTotal() {
        addDataList("check BGM700 Uric acid total")

        writeCommand(bgmProtocol?.checkBGM700UricAcidTotal())
    }

    fun downloadBGM700UricAcid() {
        addDataList("download BGM700 Uric acid")

        writeCommand(bgmProtocol?.downloadBGM700UricAcid())
    }

    fun checkBGM700CholesterolTotal() {
        addDataList("check BGM700 Cholesterol total")

        writeCommand(bgmProtocol?.checkBGM700CholesterolTotal())
    }

    fun downloadBGM700Cholesterol() {
        addDataList("download BGM700 Cholesterol")

        writeCommand(bgmProtocol?.downloadBGM700Cholesterol())
    }

    fun solveData(data: List<Byte>) {
        addDataList("NOTIFY -> ${Method.listByteToString(data)}")

        bgmProtocol?.solveDataResult(data)
    }
}