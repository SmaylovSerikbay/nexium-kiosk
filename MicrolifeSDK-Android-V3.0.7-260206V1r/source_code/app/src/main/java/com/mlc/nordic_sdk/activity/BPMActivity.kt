package com.mlc.nordic_sdk.activity

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.mlc.nordic_sdk.protocol.protocol_code.BPMProtocol
import com.mlc.nordic_sdk.protocol.protocol_code.data.bpm.CurrentAndMData
import com.mlc.nordic_sdk.protocol.protocol_code.data.bpm.DRecordBPM
import com.mlc.nordic_sdk.protocol.protocol_code.data.bpm.DeviceInfo
import com.mlc.nordic_sdk.protocol.protocol_code.data.bpm.DeviceTime
import com.mlc.nordic_sdk.protocol.protocol_code.data.bpm.User
import com.mlc.nordic_sdk.protocol.protocol_code.data.bpm.VersionData
import com.mlc.nordic_sdk.screen.BPMScreen
import com.mlc.nordic_sdk.viewModel.BPMActivityViewModel
import com.mlc.nordic_sdk.bluetooth.BluetoothManager
import com.mlc.nordic_sdk.bluetooth.OnIMBluetoothLEListener
import com.mlc.nordic_sdk.bluetooth.data.ConnectState
import com.mlc.nordic_sdk.bluetooth.data.DeviceType

class BPMActivity : ComponentActivity(), OnIMBluetoothLEListener, BPMProtocol.OnDataResponseListener {
    val TAG = "BPMActivity"
    private val viewModel by viewModels<BPMActivityViewModel>()
    private var bluetoothManager: BluetoothManager? = null
    private var deviceName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initParam()

        setContent {
            BPMScreen(viewModel = viewModel)
        }
    }

    private fun initParam() {
        bluetoothManager = BluetoothManager.getInstance(this, this)
        viewModel.setBleReceiveManager(bluetoothManager)
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
        viewModel.setDeviceInfo(deviceName, deviceType)
    }

    override fun onConnectionState(connectState: ConnectState) {
        viewModel.addDataList("$connectState")
        viewModel.state = bluetoothManager?.isCommunicate() == true

        if(connectState == ConnectState.DeviceReady || connectState == ConnectState.Bonded) {
            if(viewModel.bpmProtocol == null) {
                viewModel.setBPMProtocol(this)

                //viewModel.readUserAndVersionData()
                //viewModel.readSerialNumber()
            }
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

    override fun onResponseBPMReadHistory(dRecord: DRecordBPM?) {
        viewModel.addDataList("$dRecord")
    }

    override fun onResponseBPMReadUserAndVersionData(user: User, versionData: VersionData) {
        viewModel.addDataList("$user, $versionData")
    }

    override fun onResponseBPMReadDeviceTime(deviceTime: DeviceTime) {
        viewModel.addDataList("$deviceTime")
    }

    override fun onResponseBPMReadLastData(
        mode: Int?,
        currentMode: Int?,
        historyMeasurementNumber: Int?,
        userNumber: Int?,
        mamState: Int?,
        dRecord: CurrentAndMData?
    ) {
        viewModel.addDataList(
            "mode=$mode, currentMode=$currentMode, historyMeasurementNumber=$historyMeasurementNumber, userNumber=$userNumber, mamState=$mamState, dRecord=$dRecord"
        )
    }

    override fun onResponseBPMReadDeviceInfo(deviceInfo: DeviceInfo) {
        viewModel.addDataList("$deviceInfo")
    }

    override fun onResponseBPMClearAllHistory(isSuccess: Boolean) {
        viewModel.addDataList("$isSuccess")
    }

    override fun onResponseBPMClearLastData(isSuccess: Boolean) {
        viewModel.addDataList("$isSuccess")
    }

    override fun onResponseBPMWriteDeviceTime(isSuccess: Boolean) {
        viewModel.addDataList("$isSuccess")
    }

    override fun onResponseBPMWriteUserId(isSuccess: Boolean) {
        viewModel.addDataList("$isSuccess")
    }

    override fun onResponseBPMCheckTransmitOk(isSuccess: Boolean) {
        viewModel.addDataList("$isSuccess")
    }

    override fun onResponseBPMReadSerialNumber(serialNumber: String) {
        viewModel.addDataList(serialNumber)
    }

    override fun onWriteCommand(byteArray: ByteArray?, nextCommand: String) {
        viewModel.writeCommand(byteArray)
    }

    override fun onResponseBPMNack(cmd: Int?) {
        viewModel.addDataList("BPM response nack: $cmd")
    }
}