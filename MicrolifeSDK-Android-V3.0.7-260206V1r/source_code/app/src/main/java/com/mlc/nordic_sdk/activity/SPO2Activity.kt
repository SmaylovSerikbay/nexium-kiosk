package com.mlc.nordic_sdk.activity

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.mlc.nordic_sdk.viewModel.SPO2ActivityViewModel
import com.mlc.nordic_sdk.screen.SPO2Screen
import com.mlc.nordic_sdk.protocol.protocol_code.SPO2Protocol
import com.mlc.nordic_sdk.protocol.protocol_code.data.spo2.Spo2Data
import com.mlc.nordic_sdk.protocol.protocol_code.data.spo2.Spo2Limit
import com.mlc.nordic_sdk.bluetooth.BluetoothManager
import com.mlc.nordic_sdk.bluetooth.OnIMBluetoothLEListener
import com.mlc.nordic_sdk.bluetooth.data.ConnectState
import com.mlc.nordic_sdk.bluetooth.data.DeviceType

class SPO2Activity : ComponentActivity(), OnIMBluetoothLEListener, SPO2Protocol.OnDataResponseListener {
    val TAG = "SPO2Activity"
    private val viewModel by viewModels<SPO2ActivityViewModel>()
    private var bluetoothManager: BluetoothManager? = null
    private var deviceName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initParam()

        setContent {
            SPO2Screen(viewModel = viewModel)
        }
    }

    private fun initParam() {
        bluetoothManager = BluetoothManager.getInstance(this, this)
        viewModel.setBleReceiveManager(bluetoothManager)

        viewModel.setOnDataResponseListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.stopScan()
        bluetoothManager?.unregisterBluetoothStateReceiver()
    }

    override fun getOnBackInvokedDispatcher(): OnBackInvokedDispatcher {
        bluetoothManager = null

        return super.getOnBackInvokedDispatcher()
    }

    override fun onScanResult(
        device: BluetoothDevice,
        deviceName: String,
        deviceType: DeviceType?,
        macAddress: String?
    ) {
        this.deviceName = deviceName
        viewModel.addDataList("scan result: $deviceName, device type: $deviceType, mac address: $macAddress")
        viewModel.setDeviceName(deviceName)
    }

    override fun onConnectionState(connectState: ConnectState) {
        viewModel.addDataList("$connectState")
        viewModel.state = bluetoothManager?.isCommunicate() == true
    }

    override fun onConnectionState(connectState: ConnectState, state: Int) {
        if (connectState == ConnectState.ConnectFailed)
        {
            when(state) {
                0x22 -> { viewModel.addDataList("Fail, get $deviceName BLE sevices UUID is \"Time Out\" " ) }
                else -> { viewModel.addDataList(" Connected has Error !! $state ")}
            }
        }
    }

    override fun onReceivedBleDataResult(data: List<Byte>, head: Int?) {
        viewModel.solveData(data)
    }

    override fun onResponseSWRevision(swRevision: String) {
        viewModel.addDataList("software revision=$swRevision")
    }

    override fun onResponseFWRevision(fwRevision: String) {
        viewModel.addDataList("firmware revision=$fwRevision")
    }

    override fun onResponseHWRevision(hwRevision: String) {
        viewModel.addDataList("hardware revision=$hwRevision")
    }

    override fun onBtStateChanged(isEnable: Boolean) {
        viewModel.addDataList("onBtStateChanged($isEnable)")
    }

    override fun onResponseFailedMessage(msg: String) {
        viewModel.addDataList(msg)
    }

    override fun onResponseSpo2Data(spo2Data: Spo2Data) {
        viewModel.addDataList("$spo2Data")
    }

    override fun onResponseSpo2Limit(spo2Limit: Spo2Limit?) {
        viewModel.addDataList("$spo2Limit")
    }
}