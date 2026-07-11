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
import com.mlc.nordic_sdk.protocol.protocol_code.SPO2Protocol
import com.mlc.nordic_sdk.bluetooth.BluetoothManager
import com.mlc.nordic_sdk.bluetooth.data.ConnectState
import com.mlc.nordic_sdk.bluetooth.data.DeviceType
import kotlinx.coroutines.launch

class SPO2ActivityViewModel : ViewModel() {
    private val TAG = "SPO2ActivityViewModel"
    private var bluetoothManager: BluetoothManager? = null
    private var deviceName: String? = ""

    // Input protocol key
    private var spo2Protocol: SPO2Protocol? = SPO2Protocol.getInstance("")

    var state by mutableStateOf(false)

    //UI variable
    private val _listData = MutableLiveData<List<String>>(emptyList())
    val listData: LiveData<List<String>>
        get() = _listData

    fun setBleReceiveManager(bluetoothManager: BluetoothManager?) {
        this.bluetoothManager = bluetoothManager

        startScan()
    }

    fun setDeviceName(deviceName: String?) {
        this.deviceName = deviceName
    }

    fun setOnDataResponseListener(onDataResponseListener: SPO2Protocol.OnDataResponseListener) {
        this.spo2Protocol?.setOnDataResponseListener(onDataResponseListener)
    }

    private fun startScan() {
        bluetoothManager?.startScan(DeviceType.MLC_SPO2)
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

    //spo2 function
    fun readSPO2Limit() {
        addDataList("Read spo2 limit")

        writeCommand(spo2Protocol?.readSpo2Limit())
    }

    fun writeSPO2Limit() {
        addDataList("Write spo2 limit")

        writeCommand(spo2Protocol?.writeSpo2Limit(100, 70, 160, 50))
    }

    fun solveData(data: List<Byte>) {
        addDataList("NOTIFY -> ${Method.listByteToString(data)}")

        this.spo2Protocol?.solveDataResult(data)
    }
}