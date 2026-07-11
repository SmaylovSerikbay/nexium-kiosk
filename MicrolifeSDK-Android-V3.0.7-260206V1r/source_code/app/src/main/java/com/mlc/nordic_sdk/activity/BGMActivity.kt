package com.mlc.nordic_sdk.activity

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.mlc.nordic_sdk.protocol.protocol_code.data.bgm.BleStatus
import com.mlc.nordic_sdk.protocol.protocol_code.data.bgm.Date
import com.mlc.nordic_sdk.protocol.protocol_code.data.bgm.MeterData
import com.mlc.nordic_sdk.screen.BGMScreen
import com.mlc.nordic_sdk.viewModel.BGMActivityViewModel
import com.mlc.nordic_sdk.bluetooth.BluetoothManager
import com.mlc.nordic_sdk.bluetooth.OnIMBluetoothLEListener
import com.mlc.nordic_sdk.bluetooth.data.ConnectState
import com.mlc.nordic_sdk.bluetooth.data.DeviceType
import com.mlc.nordic_sdk.protocol.protocol_code.BGMProtocol
import com.mlc.nordic_sdk.protocol.protocol_code.data.bgm.BGM700Data
import com.mlc.nordic_sdk.protocol.protocol_code.data.bgm.BGM700Function

class BGMActivity: ComponentActivity(), OnIMBluetoothLEListener, BGMProtocol.OnDataResponseListener {
    val TAG = "BGMActivity"
    private val viewModel by viewModels<BGMActivityViewModel>()
    private var bluetoothManager: BluetoothManager? = null
    private var bgmProtocol: BGMProtocol? = null
    private var deviceName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initParam()

        setContent {
            BGMScreen(viewModel = viewModel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.blePowerOff()
        viewModel.stopScan()
        bluetoothManager?.unregisterBluetoothStateReceiver()
    }

    override fun getOnBackInvokedDispatcher(): OnBackInvokedDispatcher {
        bluetoothManager = null

        return super.getOnBackInvokedDispatcher()
    }

    private fun initParam() {
        // Input protocol key
        bgmProtocol = BGMProtocol.getInstance("", this)
        viewModel.setBGMProtocol(bgmProtocol)

        bluetoothManager = BluetoothManager.getInstance(this, this)
        viewModel.setBleManager(bluetoothManager)
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

    override fun onResponseBLEDeviceName(ack: String, bleStatus: Int, deviceName: String) {
        viewModel.addDataList("ack: $ack, bleStatus: $bleStatus, deviceName: $deviceName")
    }

    override fun onResponseBLEFirmwareVersion(ack: String, bleStatus: Int, firmVersion: String) {
        viewModel.addDataList("ack: $ack, bleStatus: $bleStatus, firmVersion: $firmVersion")
    }

    override fun onResponseGettingBLEStatus(ack: String, bleStatus: BleStatus) {
        viewModel.addDataList("ack: $ack, bleStatus: $bleStatus")
    }

    override fun onResponseTransmissionOneMeterData(ack: String, index: Int, len: Int?, meterData: MeterData?) {
        viewModel.addDataList("ack: $ack, index: $index, len: $len, meterData: $meterData")
    }

    override fun onResponseBLEPowerOff(ack: String) {
        viewModel.addDataList("ack: $ack")
    }

    override fun onResponseReadDeviceTime(ack: String, date: Date) {
        viewModel.addDataList("ack: $ack, date: $date")
    }

    override fun onResponseUploadMeterData(uploadMeterData: MeterData) {
        viewModel.addDataList("uploadMeterData: $uploadMeterData")
    }

    override fun onResponseMeterDataTransmittedCompletely(isSuccess: Boolean) {
        viewModel.addDataList("isSuccess: $isSuccess")
    }

    override fun onResponseCommunicationMode(isSuccess: Boolean) {
        viewModel.addDataList("isSuccess: $isSuccess")

        viewModel.communicationMode()
    }

    override fun onWriteCommand(responseCommand: String, byteArray: ByteArray) {
        viewModel.addDataList(responseCommand)

        viewModel.writeCommand(byteArray)
    }

    override fun onResponseCheckBGM700Function(bgm700Function: BGM700Function) {
        viewModel.addDataList("bgm700Function: $bgm700Function")
    }

    override fun onResponseCheckBGM700BGMTotal(total: Int) {
        viewModel.addDataList("total: $total")
    }

    override fun onResponseDownloadBGM700BGM(bgm700Data: BGM700Data) {
        viewModel.addDataList("bgM700Data: $bgm700Data")
    }

    override fun onResponseCheckBGM700UricAcidTotal(total: Int) {
        viewModel.addDataList("total: $total")
    }

    override fun onResponseDownloadBGM700UricAcid(bgm700Data: BGM700Data) {
        viewModel.addDataList("bgM700Data: $bgm700Data")
    }

    override fun onResponseCheckBGM700CholesterolTotal(total: Int) {
        viewModel.addDataList("total: $total")
    }

    override fun onResponseDownloadBGM700Cholesterol(bgm700Data: BGM700Data) {
        viewModel.addDataList("bgM700Data: $bgm700Data")
    }
}