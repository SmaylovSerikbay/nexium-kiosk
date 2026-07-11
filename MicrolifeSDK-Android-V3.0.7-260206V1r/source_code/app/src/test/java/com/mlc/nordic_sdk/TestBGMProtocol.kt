package com.mlc.nordic_sdk

import com.mlc.nordic_sdk.protocol.protocol_code.BGMProtocol
import com.mlc.nordic_sdk.protocol.protocol_code.data.bgm.BleStatus
import com.mlc.nordic_sdk.protocol.protocol_code.data.bgm.Date
import com.mlc.nordic_sdk.protocol.protocol_code.data.bgm.MeterData
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock

class TestBGMProtocol {
    private val listener: BGMProtocol.OnDataResponseListener = mock(BGMProtocol.OnDataResponseListener::class.java)
    private val bgmProtocol = BGMProtocol.getInstance("kV@P%qM&#kWe#F~*", listener)

    @Test
    fun write_command_to_BGM_readDeviceName() {
        val expected = byteArrayOf(
            0x91.toByte(),
            0x05.toByte(),
            0x27.toByte(),
            0x01.toByte(),
            0xBE.toByte(),
        )

        val actual = bgmProtocol?.getDeviceName()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_BGM_readDeviceName() {
        bgmProtocol?.getDeviceName()
        bgmProtocol?.solveDataResult("920C110B4170657842696F62")

        Mockito.verify(listener).onResponseBLEDeviceName("success", 11, "ApexBio")
    }

    @Test
    fun write_command_to_BGM_readFirmwareVersion() {
        val expected = byteArrayOf(
            0x91.toByte(),
            0x05.toByte(),
            0x27.toByte(),
            0x02.toByte(),
            0xBF.toByte(),
        )

        val actual = bgmProtocol?.getFirmwareVersion()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_BGM_readFirmwareVersion() {
        bgmProtocol?.getFirmwareVersion()
        bgmProtocol?.solveDataResult("920A110B302E312E36AB")

        Mockito.verify(listener).onResponseBLEFirmwareVersion("success", 11, "0.1.6")
    }

    @Test
    fun write_command_to_BGM_getBLEState() {
        val expected = byteArrayOf(
            0x91.toByte(),
            0x05.toByte(),
            0x27.toByte(),
            0x20.toByte(),
            0xDD.toByte(),
        )

        val actual = bgmProtocol?.gettingBLEStatus()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_BGM_getBLEState() {
        bgmProtocol?.gettingBLEStatus()
        bgmProtocol?.solveDataResult("9206110B0BBF")

        Mockito.verify(listener).onResponseGettingBLEStatus("success", BleStatus(connection=1, binding=1, bleMode=1))
    }

    @Test
    fun write_command_to_BGM_transmission() {
        val expected = byteArrayOf(
            0x91.toByte(),
            0x07.toByte(),
            0x27.toByte(),
            0x35.toByte(),
            0x02.toByte(),
            0x00.toByte(),
            0xF6.toByte()
        )

        val actual = bgmProtocol?.transmissionOneMeterData(2)

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_BGM_transmission() {
        bgmProtocol?.transmissionOneMeterData(2)
        bgmProtocol?.solveDataResult("930E1100020102030405060708D8")

        Mockito.verify(listener).onResponseTransmissionOneMeterData(
            "success",
            2, null,  MeterData(
                unit=0, checkMark=0, year=2001, month=0, event=0, ctl=0,
                mealType="01", hour=0, day=3, min=1, result=1541)
        )
    }

    @Test
    fun write_command_to_BGM_powerOff() {
        val expected = byteArrayOf(
            0x91.toByte(),
            0x06.toByte(),
            0x26.toByte(),
            0x36.toByte(),
            0x01.toByte(),
            0xF4.toByte(),
        )

        val actual = bgmProtocol?.blePowerOff()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_BGM_powerOff() {
        bgmProtocol?.blePowerOff()
        bgmProtocol?.solveDataResult("930411A8")

        Mockito.verify(listener).onResponseBLEPowerOff("success")
    }

    @Test
    fun write_command_to_BGM_readDeviceTime() {
        val expected = byteArrayOf(
            0x91.toByte(),
            0x05.toByte(),
            0x27.toByte(),
            0x45.toByte(),
            0x02.toByte(),
        )

        val actual = bgmProtocol?.getDeviceTime()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_BGM_readDeviceTime() {
        bgmProtocol?.getDeviceTime()
        bgmProtocol?.solveDataResult("9309111308150F2612")

        Mockito.verify(listener).onResponseReadDeviceTime("success", Date(2019, 8, 21, 15, 38))
    }

    @Test
    fun response_from_BGM_uploadMeterData() {
        bgmProtocol?.solveDataResult("930D265001020304050607083A")

        Mockito.verify(listener).onResponseUploadMeterData(MeterData(0, 0, 2001, 0, 0, "01", 0, 0, 3, 1, 1541))
    }

    @Test
    fun response_from_BGM_transmissionStatus() {
        bgmProtocol?.solveDataResult("930627520113")

        Mockito.verify(listener).onResponseMeterDataTransmittedCompletely(true)
    }

    @Test
    fun response_from_BGM_communicationMode() {
        bgmProtocol?.solveDataResult("9305275312")

        Mockito.verify(listener).onResponseCommunicationMode(true)
    }
}