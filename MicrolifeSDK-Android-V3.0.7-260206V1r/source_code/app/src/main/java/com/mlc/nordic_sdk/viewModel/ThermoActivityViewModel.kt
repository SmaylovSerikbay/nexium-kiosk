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
import com.mlc.nordic_sdk.protocol.protocol_code.ThermoProtocol
import com.mlc.nordic_sdk.bluetooth.BluetoothManager
import com.mlc.nordic_sdk.bluetooth.data.ConnectState
import com.mlc.nordic_sdk.bluetooth.data.DeviceType
import kotlinx.coroutines.launch

class ThermoActivityViewModel: ViewModel() {
    private val TAG = "ThermoActivityViewModel"
    private var deviceName: String? = ""
    private var bluetoothManager: BluetoothManager? = null

    // Input protocol key
    private var thermoProtocol: ThermoProtocol? = ThermoProtocol.getInstance("")

    var state by mutableStateOf(ConnectState.Disconnect)

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

    fun setOnDataResponseListener(onDataResponseListener: ThermoProtocol.OnDataResponseListener) {
        this.thermoProtocol?.setOnDataResponseListener(onDataResponseListener)
    }

    fun startScan() {
        bluetoothManager?.startScan(DeviceType.MLC_Thermo)
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
    fun writeCommand(data: ByteArray) {
        addDataList("WRITE -> ${Method.byteArrayToString(data)}")

        viewModelScope.launch {
            bluetoothManager?.writeCommand(data)
        }
    }

    fun disconnect() {
        bluetoothManager?.disconnectGatt()
    }

    fun solveData(data: List<Byte>) {
        addDataList("NOTIFY -> ${Method.listByteToString(data)}")

        this.thermoProtocol?.solveDataResult(data)
    }
}