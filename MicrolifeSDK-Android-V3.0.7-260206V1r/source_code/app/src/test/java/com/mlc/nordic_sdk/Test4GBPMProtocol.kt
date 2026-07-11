package com.mlc.nordic_sdk

import com.mlc.nordic_sdk.protocol.protocol_code.BPMProtocol
import com.mlc.nordic_sdk.protocol.protocol_code.BPMProtocol.OnDataResponseListener
import com.mlc.nordic_sdk.protocol.protocol_code.data.bpm.DRecordBPM
import com.mlc.nordic_sdk.protocol.protocol_code.data.bpm.DeviceInfo
import com.mlc.nordic_sdk.protocol.protocol_code.data.bpm.DeviceTime
import com.mlc.nordic_sdk.protocol.protocol_code.data.bpm.User
import com.mlc.nordic_sdk.protocol.protocol_code.data.bpm.VersionData
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.text.SimpleDateFormat
import java.util.Date



class Test4GBPMProtocol {
    private val listener: OnDataResponseListener = mock(OnDataResponseListener::class.java)
    private val bpmProtocol = BPMProtocol.getInstance("0hSOY+dQV~JMmLk!", "4G", listener)

    @Test
    fun write_command_to_BPM4G_readHistory() {
        val selectUser = 1L

        val checkSum = 0x4D + 0xFF + 0x00 + 0x09 + 0x00.toLong() + 0x00 + 0x00 + 0x00 + 0x00 + 0x00 + 0x00 + selectUser
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x09.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), selectUser.toByte(), checkSum.toByte())

        val actual = bpmProtocol?.readAllHistory(selectUser)

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_BPM4G_readHistory() {
        bpmProtocol?.solveDataResult("4D3A00270000000001000000000000000000000000000000000000000000000000000000000000000000AF")

        verify(listener).onResponseBPMReadHistory(
            DRecordBPM(mode=0, noOfCurrentMeasurement=0, historyMeasuremeNumber=0, userNumber=1, mamState=0, mData=emptyList())
        )
    }

    @Test
    fun write_command_to_BPM4G_clearHistory() {
        //0xFD: Current User
        val selectUser = 253L

        //4D-FF-00-03-03-FD-4F
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x03.toByte(), 0x03.toByte(), selectUser.toByte(), 0x4F.toByte())

        val actual = bpmProtocol?.clearAllHistory(selectUser)

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_BPM4G_clearHistory() {
        bpmProtocol?.clearAllHistory()
        bpmProtocol?.solveDataResult("4D3A0002810A")

        verify(listener).onResponseBPMClearAllHistory(true)
    }

    @Test
    fun write_command_to_BPM4G_disconnectBluetooth() {
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x02.toByte(), 0x04.toByte(), 0x52.toByte())

        val actual = bpmProtocol?.disconnect()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun write_command_to_BPM4G_readUserAndVersionData() {
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x02.toByte(), 0x05.toByte(), 0x53.toByte())

        val actual = bpmProtocol?.readUserAndVersionData()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_BPM4G_readUserVersionData() {
        //bpmProtocol?.solveDataResult("4D3A003B050162323030323030303030303030303030303030300A383039343834373437544B3030303030303030303552413112061C02630533000C0200D2")
        bpmProtocol?.solveDataResult("4D3A003B050141313030312020202020202020202020202020202342313030322020202020202020202020202020204152413111010A02631733000B01009F")

        verify(listener).onResponseBPMReadUserAndVersionData(
            user = User(no = 1, id = "A1001               ", age = 35),
            versionData = VersionData(
                fwVersion = "RA1",
                year=2017, month=1, day=10, maxUser=2, maxMemory=99, option_ihb=true,
                option_afib=true, option_mam=true, option_tubeless=false,
                deviceBatt=5.1F, option_g_sensor = false, option_single_cycle_afib = true,
                option_isArm = false, p_id = "V1.0.11", arrName = "Display IHB", currentMode = 0
            )
        )
    }

    @Test
    fun write_command_to_BPM4G_writeUserId() {
        val selectUser = 253L
        val id = "user222"
        val age = 15L

        var _id = id
        for(i in 0 until 20 - id.length) {
            _id += "0"
        }

        val id1 = Method.charToLong(_id[0])
        val id2 = Method.charToLong(_id[1])
        val id3 = Method.charToLong(_id[2])
        val id4 = Method.charToLong(_id[3])
        val id5 = Method.charToLong(_id[4])
        val id6 = Method.charToLong(_id[5])
        val id7 = Method.charToLong(_id[6])
        val id8 = Method.charToLong(_id[7])
        val id9 = Method.charToLong(_id[8])
        val id10 = Method.charToLong(_id[9])
        val id11 = Method.charToLong(_id[10])
        val id12 = Method.charToLong(_id[11])
        val id13 = Method.charToLong(_id[12])
        val id14 = Method.charToLong(_id[13])
        val id15 = Method.charToLong(_id[14])
        val id16 = Method.charToLong(_id[15])
        val id17 = Method.charToLong(_id[16])
        val id18 = Method.charToLong(_id[17])
        val id19 = Method.charToLong(_id[18])
        val id20 = Method.charToLong(_id[19])

        val checkSum = (
                0x4D + 0xFF + 0x00 + 0x18 + 0x06.toLong() + selectUser + id1 + id2 + id3 + id4 + id5 + id6 + id7 + id8 + id9 + id10 + id11 + id12 + id13 + id14 + id15 + id16 + id17 + id18 + id19 + id20 + age
                ) and 0xff

        val expected =
            byteArrayOf(
                0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x18.toByte(), 0x06.toByte(), selectUser.toByte(), id1.toByte(),
                id2.toByte(), id3.toByte(), id4.toByte(), id5.toByte(), id6.toByte(), id7.toByte(), id8.toByte(), id9.toByte(),
                id10.toByte(), id11.toByte(), id12.toByte(), id13.toByte(), id14.toByte(), id15.toByte(), id16.toByte(),
                id17.toByte(), id18.toByte(), id19.toByte(), id20.toByte(), age.toByte(), checkSum.toByte()
            )

        val actual = bpmProtocol?.writeUserId(selectUser, id, age)

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_BPM4G_writeUserId() {
        bpmProtocol?.writeUserId()
        bpmProtocol?.solveDataResult("4D3A0002810A")

        verify(listener).onResponseBPMWriteUserId(true)
    }

    @Test
    fun write_command_to_BPM4G_readDeviceIdAndInfo() {
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x02.toByte(), 0x0B.toByte(), 0x59.toByte())

        val actual = bpmProtocol?.readDeviceInfo()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_BPM4G_readDeviceInfo() {
        bpmProtocol?.solveDataResult("4D3A001A0B44C129A9FF5E92424D011170010000020000030009050000B9")

        verify(listener).onResponseBPMReadDeviceInfo(
            DeviceInfo(deviceId="C1:29:A9:FF:5E:92", connectType="Bluetooth", measurement=70000, error1=0, error2=0, error3=9, error5=0)
        )
    }

    @Test
    fun write_command_to_BPM4G_readDeviceTime() {
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x02.toByte(), 0x0C.toByte(), 0x5A.toByte())

        val actual = bpmProtocol?.readDeviceTime()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_BPM4G_readDeviceTime() {
        bpmProtocol?.solveDataResult("4D3A00090C01170B0D0D322530")

        verify(listener).onResponseBPMReadDeviceTime(
            DeviceTime(isTimeReady=true, year=2023, month=11, day=13, hour=13, minute=50, second=37)
        )
    }

    @Test
    fun write_command_to_BPM4G_writeDeviceTime() {
        val nowDate: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        val t1 = nowDate.split(" ")
        val year = (t1[0].split("-")[0].toLong()-2000) and 0xff
        val month = t1[0].split("-")[1].toLong() and 0xff
        val day = t1[0].split("-")[2].toLong() and 0xff
        val hour = t1[1].split(":")[0].toLong() and 0xff
        val minute = t1[1].split(":")[1].toLong() and 0xff
        val second = t1[1].split(":")[2].toLong() and 0xff

        val checkSum = 0x4D + 0xFF + 0x00 + 0x08 + 0x0D + year + month + day + hour + minute + second

        val expected = Method.byteArrayOfInts(0x4D, 0xFF, 0x00, 0x08, 0x0D, year, month, day, hour, minute, second, checkSum)

        val actual = bpmProtocol?.writeDeviceTime()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_BPM4G_writeDeviceTime() {
        bpmProtocol?.writeDeviceTime()
        bpmProtocol?.solveDataResult("4D3A0002810A")

        verify(listener).onResponseBPMWriteDeviceTime(true)
    }

    @Test
    fun write_command_to_BPM4G_checkTransmitOk() {
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x02.toByte(), 0x0E.toByte(), 0x5C.toByte())

        val actual = bpmProtocol?.checkTransmitOk()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun write_command_to_BPM4G_readSerialNumber() {
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x03.toByte(), 0x0F.toByte(), 0x00.toByte(), 0x5E.toByte())

        val actual = bpmProtocol?.readSerialNumber()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun write_command_to_BPM4G_writeSerialNumber() {
        val serialNumber = "10000000000000000000"

        val expected =
            byteArrayOf(
                0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x1A.toByte(), 0x0F.toByte(), 0x01.toByte(), 0x57.toByte(),
                0x53.toByte(), 0x4E.toByte(), 0x31.toByte(), 0x30.toByte(), 0x30.toByte(), 0x30.toByte(), 0x30.toByte(),
                0x30.toByte(), 0x30.toByte(), 0x30.toByte(), 0x30.toByte(), 0x30.toByte(), 0x30.toByte(), 0x30.toByte(),
                0x30.toByte(), 0x30.toByte(), 0x30.toByte(), 0x30.toByte(), 0x30.toByte(), 0x30.toByte(), 0x30.toByte(),
                0x30.toByte(), 0x2F.toByte()
            )

        val actual = bpmProtocol?.writeSerialNumber(serialNumber)

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun write_command_to_BPM4G_readLastData() {
        val checksum = 0x4D + 0xFF + 0x00 + 0x02 + 0x07.toLong()
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x02.toByte(), 0x07.toByte(), checksum.toByte())

        val actual = bpmProtocol?.readLastData()

        Assertions.assertArrayEquals(expected, actual)
    }
}