package com.mlc.nordic_sdk.activity

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.mlc.nordic_sdk.screen.ThermoScreen
import com.mlc.nordic_sdk.viewModel.ThermoActivityViewModel
import com.mlc.nordic_sdk.protocol.protocol_code.ThermoProtocol
import com.mlc.nordic_sdk.protocol.protocol_code.data.thermo.CalibrateParameter
import com.mlc.nordic_sdk.protocol.protocol_code.data.thermo.ThermoMeasureData
import com.mlc.nordic_sdk.bluetooth.BluetoothManager
import com.mlc.nordic_sdk.bluetooth.OnIMBluetoothLEListener
import com.mlc.nordic_sdk.bluetooth.data.ConnectState
import com.mlc.nordic_sdk.bluetooth.data.DeviceType

class ThermoActivity : ComponentActivity(), OnIMBluetoothLEListener, ThermoProtocol.OnDataResponseListener {
    val TAG = "ThermoActivity"
    private val viewModel by viewModels<ThermoActivityViewModel>()
    private var bluetoothManager: BluetoothManager? = null
    private var deviceName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initParam()

        setContent {
            ThermoScreen(viewModel = viewModel)
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
        viewModel.state = connectState

        when(connectState) {
            ConnectState.Disconnect, ConnectState.ConnectFailed -> {
                viewModel.startScan()
            }
            else -> {}
        }
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

    override fun onResponseDeviceInfo(macAddress: String, workMode: Int, batteryVoltage: Float) {
        viewModel.addDataList("mac address: $macAddress, work mode: $workMode, batteryVoltage: $batteryVoltage")
    }

    override fun onResponseUploadMeasureData(thermoMeasureData: ThermoMeasureData) {
        viewModel.addDataList("$thermoMeasureData")
    }

    override fun onResponseUploadCalibrate(calibrateParameters: List<CalibrateParameter>) {
        viewModel.addDataList("$calibrateParameters")
    }

    override fun onWriteCommand(byteArray: ByteArray) {
        viewModel.writeCommand(byteArray)
    }
}