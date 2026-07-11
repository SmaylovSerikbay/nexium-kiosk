package com.mlc.nordic_sdk.activity

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.mlc.nordic_sdk.screen.PFMScreen
import com.mlc.nordic_sdk.viewModel.PFMActivityViewModel
import com.mlc.nordic_sdk.protocol.protocol_code.PFMProtocol
import com.mlc.nordic_sdk.protocol.protocol_code.data.bpm.DeviceTime
import com.mlc.nordic_sdk.protocol.protocol_code.data.pfm.DRecordPFM
import com.mlc.nordic_sdk.protocol.protocol_code.data.pfm.UserIdAndVersionData
import com.mlc.nordic_sdk.bluetooth.BluetoothManager
import com.mlc.nordic_sdk.bluetooth.OnIMBluetoothLEListener
import com.mlc.nordic_sdk.bluetooth.data.ConnectState
import com.mlc.nordic_sdk.bluetooth.data.DeviceType
import com.mlc.nordic_sdk.protocol.protocol_code.data.pfm.PFMWaveForm

class PFMActivity : ComponentActivity(), OnIMBluetoothLEListener, PFMProtocol.OnDataResponseListener {
    val TAG = "PFMActivity"
    private val viewModel by viewModels<PFMActivityViewModel>()
    private var bluetoothManager: BluetoothManager? = null
    private var deviceName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initParam()

        setContent {
            PFMScreen(viewModel = viewModel)
        }
    }

    private fun initParam() {
        bluetoothManager = BluetoothManager.getInstance(this, this)
        viewModel.setBleReceiveManager(bluetoothManager)

        viewModel.setOnDataResponseListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.disconnectBluetooth()
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

    override fun onResponsePFMReadHistory(dRecord: DRecordPFM?) {
        viewModel.addDataList("$dRecord")
    }

    override fun onResponsePFMClearAllHistory(isSuccess: Boolean) {
        viewModel.addDataList("$isSuccess")
    }

    override fun onResponseDisconnect(isSuccess: Boolean) {
        viewModel.addDataList("$isSuccess")
    }

    override fun onResponsePFMReadUserIdAndVersionData(userIdAndVersionData: UserIdAndVersionData) {
        viewModel.addDataList("$userIdAndVersionData")
    }

    override fun onResponsePFMWriteNewUser(isSuccess: Boolean) {
        viewModel.addDataList("$isSuccess")
    }

    override fun onResponsePFMReadLastData(dRecord: DRecordPFM?) {
        viewModel.addDataList("$dRecord")
    }

    override fun onResponsePFMClearLastData(isSuccess: Boolean) {
        viewModel.addDataList("$isSuccess")
    }

    override fun onResponsePFMReadDeviceTime(deviceTime: DeviceTime?) {
        viewModel.addDataList("$deviceTime")
    }

    override fun onResponsePFMWriteDeviceTime(isSuccess: Boolean) {
        viewModel.addDataList("$isSuccess")
    }

    override fun onResponsePFMReadSerialNumber(serialNumber: String?) {
        viewModel.addDataList("$serialNumber")
    }

    override fun onResponsePFMReadBestValue(bestValue: Int, highValue: Int) {
        viewModel.addDataList("bestValue: $bestValue, highValue: $highValue")
    }

    override fun onResponsePFMWriteBestValue(isSuccess: Boolean) {
        viewModel.addDataList("$isSuccess")
    }

    override fun onResponsePFMCheckMode(mode: Int, data: Int) {
        viewModel.addDataList("mode: $mode, data: $data")
    }

    override fun onResponsePFMStartMeasurement(isDeviceReady: Boolean) {
        viewModel.addDataList("$isDeviceReady")
    }

    override fun onResponsePFMReadWaveform(pfmWaveForm: PFMWaveForm) {
        viewModel.addDataList("$pfmWaveForm")
    }
}