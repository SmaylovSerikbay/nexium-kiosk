package com.mlc.nordic_sdk.activity

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.mlc.nordic_sdk.bluetooth.BluetoothManager
import com.mlc.nordic_sdk.bluetooth.OnIMBluetoothLEListener
import com.mlc.nordic_sdk.bluetooth.data.ConnectState
import com.mlc.nordic_sdk.bluetooth.data.DeviceType
import com.mlc.nordic_sdk.screen.WBPScreen
import com.mlc.nordic_sdk.viewModel.WBPActivityViewModel
import com.mlc.nordic_sdk.protocol.protocol_code.WBPProtocol
import com.mlc.nordic_sdk.protocol.protocol_code.data.wbp.DRecordDiagnostic
import com.mlc.nordic_sdk.protocol.protocol_code.data.wbp.DRecordNocturnal
import com.mlc.nordic_sdk.protocol.protocol_code.data.wbp.DRecordUsual
import com.mlc.nordic_sdk.protocol.protocol_code.data.wbp.DRecordUsualEach
import com.mlc.nordic_sdk.protocol.protocol_code.data.wbp.DeviceInfo
import com.mlc.nordic_sdk.protocol.protocol_code.data.wbp.DeviceTime
import com.mlc.nordic_sdk.protocol.protocol_code.data.wbp.MeasurementSetting
import com.mlc.nordic_sdk.protocol.protocol_code.data.wbp.NocturnalInfo
import com.mlc.nordic_sdk.protocol.protocol_code.data.wbp.UserIDAndVersionData

class WBPActivity: ComponentActivity(), OnIMBluetoothLEListener, WBPProtocol.OnDataResponseListener {
    val TAG = "WBPActivity"
    private val viewModel by viewModels<WBPActivityViewModel>()
    private var bluetoothManager: BluetoothManager? = null
    private var deviceName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initParam()

        setContent {
            WBPScreen(viewModel = viewModel)
        }
    }

    private fun initParam() {
        bluetoothManager = BluetoothManager.getInstance(this, this)
        viewModel.setBleReceiveManager(bluetoothManager)
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
        viewModel.setDeviceInfo(deviceName, deviceType)
    }

    override fun onConnectionState(connectState: ConnectState) {
        viewModel.addDataList("$connectState")
        viewModel.state = bluetoothManager?.isCommunicate() == true

        if(connectState == ConnectState.DeviceReady) {
            viewModel.setWBPProtocol(this)
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

    override fun onResponseWBPReadUsualModeHistory(dRecordUsual: DRecordUsual) {
        viewModel.addDataList("onResponseWBPReadUsualModeHistory: $dRecordUsual")
    }

    override fun onResponseWBPReadUsualModeHistoryEachMeasurement(totalPackageNumber: Int, packageNumber: Int, dRecordUsualEach: DRecordUsualEach) {
        viewModel.addDataList("onResponseWBPReadUsualModeHistoryEachMeasurement: $totalPackageNumber, $packageNumber, $dRecordUsualEach")
    }

    override fun onResponseWBPReadDiagnosticModeHistory(dRecordDiagnostic: DRecordDiagnostic) {
        viewModel.addDataList("onResponseWBPReadDiagnosticModeHistory: $dRecordDiagnostic")
    }

    override fun onResponseWBPClearSelectedModeHistory(success: Boolean) {
        viewModel.addDataList("onResponseWBPClearSelectedModeHistory: $success")
    }

    override fun onResponseWBPClearCurrentModeHistory(success: Boolean) {
        viewModel.addDataList("onResponseWBPClearCurrentModeHistory: $success")
    }

    override fun onResponseWBPWriteDeviceTime(success: Boolean) {
        viewModel.addDataList("onResponseWBPWriteDeviceTime: $success")
    }

    override fun onResponseWBPWriteNewUserId(success: Boolean) {
        viewModel.addDataList("onResponseWBPWriteNewUserId: $success")
    }

    override fun onResponseWBPReadNocturnalModeSetting(nocturnalInfo: NocturnalInfo) {
        viewModel.addDataList("onResponseWBPReadNocturnalModeSetting: $nocturnalInfo")
    }

    override fun onResponseWBPChangeNocturnalModeSetting(success: Boolean) {
        viewModel.addDataList("onResponseWBPChangeNocturnalModeSetting: $success")
    }

    override fun onResponseWBPReadMeasurementSetting(measurementSetting: MeasurementSetting) {
        viewModel.addDataList("onResponseWBPReadMeasurementSetting: $measurementSetting")
    }

    override fun onResponseWBPWriteMeasurementSetting(success: Boolean) {
        viewModel.addDataList("onResponseWBPWriteMeasurementSetting: $success")
    }

    override fun onResponseWBPReadDeviceIDAndInfo(deviceInfo: DeviceInfo) {
        viewModel.addDataList("onResponseWBPReadDeviceIDAndInfo: $deviceInfo")
    }

    override fun onResponseWBPReadDeviceTime(deviceTime: DeviceTime) {
        viewModel.addDataList("onResponseWBPReadDeviceTime: $deviceTime")
    }

    override fun onResponseWBPReadUserIDAndVersionData(userIDAndVersionData: UserIDAndVersionData) {
        viewModel.addDataList("onResponseWBPReadUserIDAndVersionData: $userIDAndVersionData")
    }

    override fun onResponseWBPReadNocturnalModeHistory(dRecordNocturnal: DRecordNocturnal) {
        viewModel.addDataList("onResponseWBPReadNocturnalModeHistory: $dRecordNocturnal")
    }

    override fun onResponseWBPReadSerialNumber(serial_number: String) {
        viewModel.addDataList("onResponseWBPReadSerialNumber: $serial_number")
    }

    override fun onResponseWBPNACK(msg: String) {
        viewModel.addDataList("onResponseWBPNACK: $msg")
    }

    override fun onWriteWBPCommand(byteArray: ByteArray, nextCommand: String) {
        viewModel.addDataList(nextCommand)
        viewModel.writeCommand(byteArray)
    }
}