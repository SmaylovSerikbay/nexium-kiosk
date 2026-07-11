package com.mlc.nordic_sdk.activity

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.mlc.nordic_sdk.protocol.protocol_code.BaseProtocol
import com.mlc.nordic_sdk.protocol.protocol_code.data.bpm.CurrentAndMData
import com.mlc.nordic_sdk.protocol.protocol_code.data.bpm.DRecordBPM
import com.mlc.nordic_sdk.protocol.protocol_code.data.bpm.DeviceInfo
import com.mlc.nordic_sdk.protocol.protocol_code.data.bpm.DeviceTime
import com.mlc.nordic_sdk.protocol.protocol_code.data.bpm.User
import com.mlc.nordic_sdk.protocol.protocol_code.data.bpm.VersionData
import com.mlc.nordic_sdk.protocol.protocol_code.data.pfm.DRecordPFM
import com.mlc.nordic_sdk.protocol.protocol_code.data.spo2.Spo2Data
import com.mlc.nordic_sdk.protocol.protocol_code.data.thermo.ThermoMeasureData
import com.mlc.nordic_sdk.screen.BaseScreen
import com.mlc.nordic_sdk.viewModel.BaseActivityViewModel
import com.mlc.nordic_sdk.bluetooth.BluetoothManager
import com.mlc.nordic_sdk.bluetooth.OnIMBluetoothLEListener
import com.mlc.nordic_sdk.bluetooth.data.ConnectState
import com.mlc.nordic_sdk.bluetooth.data.DeviceType
import com.mlc.nordic_sdk.protocol.protocol_code.data.ebody.EBodyMeasureData
import com.mlc.nordic_sdk.protocol.protocol_code.data.ebody.SyncTimeData
import com.mlc.nordic_sdk.protocol.protocol_code.data.pfm.UserIdAndVersionData
import com.mlc.nordic_sdk.protocol.protocol_code.data.wbp.DRecordDiagnostic
import com.mlc.nordic_sdk.protocol.protocol_code.data.wbp.DRecordUsual
import com.mlc.nordic_sdk.protocol.protocol_code.data.wbp.UserIDAndVersionData

class BaseActivity: ComponentActivity(), OnIMBluetoothLEListener, BaseProtocol.OnDataResponseListener {
    val TAG = "BaseActivity"
    private val viewModel by viewModels<BaseActivityViewModel>()
    private var bluetoothManager: BluetoothManager? = null
    private var deviceName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initParam()

        setContent {
            BaseScreen(viewModel = viewModel)
        }
    }

    private fun initParam() {
        bluetoothManager = BluetoothManager.getInstance(this, this)
        viewModel.setBleReceiveManager(bluetoothManager)
    }

    override fun onStop() {
        super.onStop()

        viewModel.disconnect()
        viewModel.stopScan()
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.disconnect()
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
        viewModel.setDeviceInfo(deviceName, deviceType, true)
    }

    override fun onConnectionState(connectState: ConnectState) {
        viewModel.addDataList("$connectState")
        viewModel.state = bluetoothManager?.isCommunicate() == true

        when(connectState) {
            ConnectState.Connected -> {
                viewModel.setBaseProtocol(this)
            }
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
        viewModel.addDataList("head=$head")
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

    override fun onResponseBPMReadHistory(dRecord: DRecordBPM?) {
        viewModel.addDataList("$dRecord")
    }

    override fun onResponseBPMReadLastHistory(
        mode: Int?,
        currentMode: Int?,
        historyMeasurementNumber: Int?,
        userNumber: Int?,
        mamState: Int?,
        dRecord: CurrentAndMData?
    ) {
        viewModel.addDataList("$mode, $currentMode, $historyMeasurementNumber, $userNumber, $mamState, $dRecord")
    }

    override fun onResponseBPMClearHistory(isSuccess: Boolean) {
        viewModel.addDataList("$isSuccess")
    }

    override fun onResponseBPMReadUserAndVersionData(user: User, versionData: VersionData) {
        viewModel.addDataList("$user, $versionData")
    }

    override fun onResponseBPMWriteUser(isSuccess: Boolean) {
        viewModel.addDataList("$isSuccess")
    }

    override fun onResponseBPMReadDeviceInfo(deviceInfo: DeviceInfo) {
        viewModel.addDataList("$deviceInfo")
    }

    override fun onResponseBPMWriteDeviceTime(isSuccess: Boolean) {
        viewModel.addDataList("$isSuccess")
    }

    override fun onResponseBPMReadSN(sn: String) {
        viewModel.addDataList(sn)
    }

    override fun onResponseBPMNack(cmd: Int?) {
        viewModel.addDataList("BPM response nack: $cmd")
    }

    override fun onResponsePFMReadUserAndVersionData(userIdAndVersionData: UserIdAndVersionData) {
        viewModel.addDataList("$userIdAndVersionData")
    }

    override fun onResponsePFMReadHistory(dRecord: DRecordPFM?) {
        viewModel.addDataList("$dRecord")
    }

    override fun onResponsePFMWriteDeviceTime(isSuccess: Boolean) {
        viewModel.addDataList("$isSuccess")
    }

    override fun onResponsePFMReadBestValue(bestValue: Int, highValue: Int) {
        viewModel.addDataList("bestValue: $bestValue, highValue: $highValue")
    }

    override fun onResponsePFMWriteBestValue(isSuccess: Boolean) {
        viewModel.addDataList("$isSuccess")
    }

    override fun onResponsePFMClearHistory(isSuccess: Boolean) {
        viewModel.addDataList("$isSuccess")
    }

    override fun onResponsePFMWriteUser(isSuccess: Boolean) {
        viewModel.addDataList("$isSuccess")
    }

    override fun onResponsePFMReadSN(serialNumber: String?) {
        viewModel.addDataList("$serialNumber")
    }

    override fun onResponseSpo2Data(spo2Data: Spo2Data) {
        viewModel.addDataList("$spo2Data")
    }

    override fun onResponseThermoReadDeviceInfo(
        macAddress: String,
        workMode: Int,
        batteryVoltage: Float
    ) {
        viewModel.addDataList("$macAddress, $workMode, $batteryVoltage")
    }

    override fun onResponseThermoUploadMeasureData(thermoMeasureData: ThermoMeasureData) {
        viewModel.addDataList("$thermoMeasureData")
    }

    override fun onResponseWBPReadUsualModeHistory(dRecordUsual: DRecordUsual) {
        viewModel.addDataList("$dRecordUsual")
    }

    override fun onResponseWBPReadDiagnosticModeHistory(dRecordDiagnostic: DRecordDiagnostic) {
        viewModel.addDataList("$dRecordDiagnostic")
    }

    override fun onResponseWBPClearHistory(success: Boolean) {
        viewModel.addDataList("$success")
    }

    override fun onResponseWBPWriteDeviceTime(success: Boolean) {
        viewModel.addDataList("$success")
    }

    override fun onResponseWBPWriteUser(success: Boolean) {
        viewModel.addDataList("$success")
    }

    override fun onResponseWBPReadDeviceInfo(deviceInfo: com.mlc.nordic_sdk.protocol.protocol_code.data.wbp.DeviceInfo) {
        viewModel.addDataList("$deviceInfo")
    }

    override fun onResponseWBPReadUserAndVersionData(userIDAndVersionData: UserIDAndVersionData) {
        viewModel.addDataList("$userIDAndVersionData")
    }

    override fun onResponseWBPReadSN(sn: String) {
        viewModel.addDataList(sn)
    }

    override fun onResponseWBPNack(msg: String) {
        viewModel.addDataList(msg)
    }

    override fun onWriteCommand(byteArray: ByteArray?, nextCommand: String) {
        viewModel.writeCommand(byteArray)
    }
}