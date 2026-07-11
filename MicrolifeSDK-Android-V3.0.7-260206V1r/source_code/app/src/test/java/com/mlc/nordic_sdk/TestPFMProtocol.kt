package com.mlc.nordic_sdk

import com.mlc.nordic_sdk.protocol.protocol_code.PFMProtocol
import com.mlc.nordic_sdk.protocol.protocol_code.data.bpm.DeviceTime
import com.mlc.nordic_sdk.protocol.protocol_code.data.pfm.DRecordPFM
import com.mlc.nordic_sdk.protocol.protocol_code.data.pfm.MData
import com.mlc.nordic_sdk.protocol.protocol_code.data.pfm.MResult
import com.mlc.nordic_sdk.protocol.protocol_code.data.pfm.PFMWaveForm
import com.mlc.nordic_sdk.protocol.protocol_code.data.pfm.UserIdAndVersionData
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.text.SimpleDateFormat
import java.util.Date



class TestPFMProtocol {
    private val listener: PFMProtocol.OnDataResponseListener = mock(PFMProtocol.OnDataResponseListener::class.java)
    private val pfmProtocol = PFMProtocol.getInstance("z~CVbU2%b6CCJLW1", listener)

    @Test
    fun write_command_to_PFM_readHistory() {
        val selectUser = 1L

        val checkSum = 0x4D + 0xFF + 0x00 + 0x09 + 0x00 + 0x00 + 0x00 + 0x00 + 0x00 + 0x00 + 0x00 + selectUser
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x09.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), selectUser.toByte(), checkSum.toByte())

        val actual = pfmProtocol?.readAllHistoryData(selectUser)

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_PFM_readHistory() {
        pfmProtocol?.solveDataResult("4D4F00310000000301FF000000000000000000000000000000000000000000000000000000000000000016010203040801DF000000")

        Mockito.verify(listener).onResponsePFMReadHistory(
            DRecordPFM(
                mode=0,
                historyMeasurementTimes=3,
                userNumber=1,
                mamVersion=255,
                mData= listOf(
                    MData(year=22, month=1, day=2, hour=3, minute=4, pef=264, fev1=223),
                )
            )
        )
    }

    @Test
    fun write_command_to_PFM_clearHistory() {
        val selectUser = 253L

        val checkSum = 0x4D + 0xFF + 0x00 + 0x03 + 0x03 + selectUser
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x03.toByte(), 0x03.toByte(), selectUser.toByte(), checkSum.toByte())

        val actual = pfmProtocol?.clearAllHistoryData(selectUser)

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_PFM_clearHistory() {
        pfmProtocol?.clearAllHistoryData()
        pfmProtocol?.solveDataResult("4D3A0002810A")

        Mockito.verify(listener).onResponsePFMClearAllHistory(true)
    }

    @Test
    fun write_command_to_PFM_disconnect() {
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x02.toByte(), 0x04.toByte(), 0x52.toByte())

        val actual = pfmProtocol?.disconnectBluetooth()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun write_command_to_PFM_readUserIdAndVersionData() {
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x02.toByte(), 0x05.toByte(), 0x53.toByte())

        val actual = pfmProtocol?.readUserIdAndVersionData()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_PFM_readUserIdAndVersionData() {
        pfmProtocol?.solveDataResult("4D4F003B050141313030312020202020202020202020202020202342313030322020202020202020202020202020204152413111010A02006333000B010000")

        Mockito.verify(listener).onResponsePFMReadUserIdAndVersionData(
            UserIdAndVersionData(
                currentUserNo=1,
                user1_id="A1001               ",
                user1_age=35,
                user2_id= "B1002               ",
                user2_age=65,
                fwVersion = "RA1",
                vYear=17,
                vMonth=1,
                vDay=10,
                maxUser=2,
                maxMemory=99,
                deviceBatt = 5.1F,
                p_id="V1.0.11",
                arrName="",
                currentMode=0
            )
        )
    }

    @Test
    fun write_command_to_PFM_writeUserId() {
        val selectUser = 2L
        val id = "b2002"
        val age = 10L

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

        val checkSum = (0x4D + 0xFF + 0x00 + 0x18 + 0x06 + selectUser + id1 + id2 + id3 + id4 + id5 + id6 + id7 + id8 + id9 + id10 + id11 + id12 + id13 + id14 + id15 + id16 + id17 + id18 + id19 + id20 + age) and 0xff

        val expected = byteArrayOfInts(
            0x4D, 0xFF, 0x00, 0x18, 0x06, selectUser, id1, id2, id3, id4, id5, id6, id7, id8, id9, id10, id11, id12, id13, id14, id15, id16, id17, id18, id19, id20, age, checkSum
        )

        val actual = pfmProtocol?.writeNewUser(selectUser, id, age)

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_PFM_writeUserId() {
        pfmProtocol?.writeNewUser()
        pfmProtocol?.solveDataResult("4D3A0002911A")

        Mockito.verify(listener).onResponsePFMWriteNewUser(false)
    }

    @Test
    fun write_command_to_PFM_readLastData() {
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x02.toByte(), 0x07.toByte(), 0x55.toByte())

        val actual = pfmProtocol?.readLastData()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_PFM_readLastData() {
        pfmProtocol?.solveDataResult("4D4F00130700000301FF000016010203040801DF0000C2")

        Mockito.verify(listener).onResponsePFMReadLastData(
            DRecordPFM(
                mode=0,
                historyMeasurementTimes=3,
                userNumber=1,
                mamVersion=255,
                mData= listOf(
                    MData(year=22, month=1, day=2, hour=3, minute=4, pef=264, fev1=223),
                )
            )
        )
    }

    @Test
    fun write_command_to_PFM_clearLastData() {
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x02.toByte(), 0x08.toByte(), 0x56.toByte())

        val actual = pfmProtocol?.clearLastData()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_PFM_clearLastData() {
        pfmProtocol?.clearLastData()
        pfmProtocol?.solveDataResult("4D3A0002811A")

        Mockito.verify(listener).onResponsePFMClearLastData(true)
    }

    @Test
    fun write_command_to_PFM_readDeviceTime() {
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x02.toByte(), 0x0C.toByte(), 0x5A.toByte())

        val actual = pfmProtocol?.readDeviceTime()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_PFM_readDeviceTime() {
        pfmProtocol?.solveDataResult("4D4F00090C0111050d130c2600")

        Mockito.verify(listener).onResponsePFMReadDeviceTime(
            DeviceTime(
                isTimeReady=null,
                year=17,
                month=5,
                day=13,
                hour=19,
                minute=12,
                second=38
            )
        )
    }

    @Test
    fun write_command_to_PFM_writeDeviceTime() {
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

        val actual = pfmProtocol?.writeDeviceTime()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_PFM_writeDeviceTime() {
        pfmProtocol?.writeDeviceTime()
        pfmProtocol?.solveDataResult("4D3A0002811A")

        Mockito.verify(listener).onResponsePFMWriteDeviceTime(true)
    }

    @Test
    fun write_command_to_PFM_readSerialNumber() {
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x03.toByte(), 0x0F.toByte(), 0x00.toByte(), 0x5E.toByte())

        val actual = pfmProtocol?.readSerialNumber()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_PFM_readSerialNumber() {
        pfmProtocol?.solveDataResult("4D4F00170F00313030303030303030303030303030303030303083")

        Mockito.verify(listener).onResponsePFMReadSerialNumber("10000000000000000000")
    }

    @Test
    fun write_command_to_PFM_writeSerialNumber() {
        val serialNumber = "10000000000000000000"

        val expected =
            byteArrayOf(
                0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x1A.toByte(), 0x0F.toByte(), 0x01.toByte(), 0x57.toByte(),
                0x53.toByte(), 0x4E.toByte(), 0x31.toByte(), 0x30.toByte(), 0x30.toByte(), 0x30.toByte(), 0x30.toByte(),
                0x30.toByte(), 0x30.toByte(), 0x30.toByte(), 0x30.toByte(), 0x30.toByte(), 0x30.toByte(), 0x30.toByte(),
                0x30.toByte(), 0x30.toByte(), 0x30.toByte(), 0x30.toByte(), 0x30.toByte(), 0x30.toByte(), 0x30.toByte(),
                0x30.toByte(), 0x2F.toByte()
            )

        val actual = pfmProtocol?.writeSerialNumber(serialNumber)

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_PFM_writeSerialNumber() {
        pfmProtocol?.writeSerialNumber()
        pfmProtocol?.solveDataResult("4D4F00040F018131")

        Mockito.verify(listener).onResponsePFMWriteSerialNumber(true)
    }

    @Test
    fun write_command_to_PFM_readBestValue() {
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x02.toByte(), 0x10.toByte(), 0x5E.toByte())

        val actual = pfmProtocol?.readBestValue()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_PFM_readBestValue() {
        pfmProtocol?.solveDataResult("4D4F000610F401900100")

        Mockito.verify(listener).onResponsePFMReadBestValue(bestValue=500, highValue = 400)
    }

    @Test
    fun write_command_to_PFM_writeBestValue() {
        val bestValue = 500
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x04.toByte(), 0x11.toByte(), 0xF4.toByte(), 0x01.toByte(), 0x56.toByte())

        val actual = pfmProtocol?.writeBestValue(bestValue)

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_PFM_writeBestValue() {
        pfmProtocol?.writeBestValue()
        pfmProtocol?.solveDataResult("4D4F0002811F")

        Mockito.verify(listener).onResponsePFMWriteBestValue(true)
    }

    @Test
    fun write_command_to_PFM_startMeasure() {
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x02.toByte(), 0x13.toByte(), 0x61.toByte())

        val actual = pfmProtocol?.startMeasurement()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_PFM_startMeasure() {
        pfmProtocol?.solveDataResult("4D4F0003139100")

        Mockito.verify(listener).onResponsePFMStartMeasurement(false)
    }

    @Test
    fun write_command_to_PFM_readWaveform() {
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x02.toByte(), 0x14.toByte(), 0x62.toByte())

        val actual = pfmProtocol?.readWaveform()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_PFM_readWaveForm() {
        pfmProtocol?.solveDataResult("4D4F0014140100FFFF010002000300040506070809000000")

        Mockito.verify(listener).onResponsePFMReadWaveform(
            PFMWaveForm(
                mData = listOf(1),
                mResult = MResult(
                    pef = 1,
                    fev1 = 2,
                    fvc = 3,
                    year = 4,
                    month = 5,
                    day = 6,
                    hour = 7,
                    minute = 8,
                    second = 9
                )
            )
        )
    }

    private fun byteArrayOfInts(vararg ints: Long) =
        ByteArray(ints.size) { pos -> ints[pos].toByte() }
}