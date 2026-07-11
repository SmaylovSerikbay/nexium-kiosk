package com.mlc.nordic_sdk

import com.mlc.nordic_sdk.protocol.protocol_code.BPMProtocol
import com.mlc.nordic_sdk.protocol.protocol_code.data.bpm.DRecordBPM
import com.mlc.nordic_sdk.protocol.protocol_code.data.bpm.User
import com.mlc.nordic_sdk.protocol.protocol_code.data.bpm.VersionData
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.text.SimpleDateFormat
import java.util.Date



class Test3GBPMProtocol {
    private val listener: BPMProtocol.OnDataResponseListener = mock(BPMProtocol.OnDataResponseListener::class.java)
    private val bpmProtocol = BPMProtocol.getInstance("0hSOY+dQV~JMmLk!", "3G", listener)

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun write_command_to_BPM3G_readHistoryAndSynchronizeTiming() {
        GlobalScope.launch {
            val nowDate: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
            val t1 = nowDate.split(" ")
            val year = (t1[0].split("-")[0].toLong()-2000) and 0xff
            val month = t1[0].split("-")[1].toLong() and 0xff
            val day = t1[0].split("-")[2].toLong() and 0xff
            val hour = t1[1].split(":")[0].toLong() and 0xff
            val minute = t1[1].split(":")[1].toLong() and 0xff
            val second = t1[1].split(":")[2].toLong() and 0xff

            val checkSum = 0x4D + 0xFF + 0x00 + 0x08 + 0x02 + year + month + day + hour + minute + second

            val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x08.toByte(), 0x00.toByte(), year.toByte(),
                month.toByte(), day.toByte(), hour.toByte(), minute.toByte(), (second).toByte(), checkSum.toByte())

            val actual = bpmProtocol?.readAllHistory()

            Assertions.assertArrayEquals(expected, actual)
        }
    }

    @Test
    fun response_from_BPM3G_readHistoryAndSynchronizeTiming() {
        bpmProtocol?.solveDataResult("4D31001E0000010001030000000000000000000000000000000000000000000000A1")

        verify(listener).onResponseBPMReadHistory(
            DRecordBPM(mode=0, noOfCurrentMeasurement=1, historyMeasuremeNumber=0, userNumber=1, mamState=3, mData= emptyList())
        )
    }

    @Test
    fun write_command_to_BPM3G_clearHistory() {
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x02.toByte(), 0x03.toByte(), 0x51.toByte())

        val actual = bpmProtocol?.clearAllHistory()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_BPM3G_clearHistory() {
        bpmProtocol?.clearAllHistory()
        bpmProtocol?.solveDataResult("4D3A0002810A")

        verify(listener).onResponseBPMClearAllHistory(true)
    }

    @Test
    fun write_command_to_BPM3G_disconnectBluetooth() {
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x02.toByte(), 0x04.toByte(), 0x52.toByte())

        val actual = bpmProtocol?.disconnect()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun write_command_to_BPM3G_readUserAndVersionData() {
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x02.toByte(), 0x05.toByte(), 0x53.toByte())

        val actual = bpmProtocol?.readUserAndVersionData()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_BPM3G_readUserVersionData() {
        bpmProtocol?.solveDataResult("4D310019050137333331333130363542563552473112030E02630531BF")

        verify(listener).onResponseBPMReadUserAndVersionData(
            user = User(no = 1, id = "733131065BV", age = 53),
            versionData = VersionData(
                fwVersion = "RG1",
                year=2018, month=3, day=14, maxUser=2, maxMemory=99, option_ihb=true,
                option_afib=false, option_mam=true, deviceBatt=4.9F
            )
        )
    }

    @Test
    fun write_command_to_BPM3G_writeUserId() {
        val id = "user111"
        val age = 12L

        var _id = id
        for(i in 0 until 11 - id.length) {
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

        val checkSum = (0x4D + 0xFF + 0x00 + 0x0E + 0x06 +  id1 + id2 + id3 + id4 + id5 + id6 + id7 + id8 + id9 + id10 + id11 + age) and 0xff

        val expected =
            byteArrayOf(
                0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x0E.toByte(), 0x06.toByte(), id1.toByte(), id2.toByte(),
                id3.toByte(), id4.toByte(), id5.toByte(), id6.toByte(), id7.toByte(), id8.toByte(), id9.toByte(),
                id10.toByte(), id11.toByte(), age.toByte(), checkSum.toByte()
            )

        val actual = bpmProtocol?.writeUserId(id = id, age = age)

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_BPM3G_writeUserId() {
        bpmProtocol?.writeUserId()
        bpmProtocol?.solveDataResult("4D3A0002810A")

        verify(listener).onResponseBPMWriteUserId(true)
    }

    @Test
    fun write_command_to_BPM3G_readLastData() {
        val checksum = 0x4D + 0xFF + 0x00 + 0x02 + 0x07.toLong()
        val expected = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x00.toByte(), 0x02.toByte(), 0x07.toByte(), checksum.toByte())

        val actual = bpmProtocol?.readLastData()

        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_BPM3G_readLastData() {
        bpmProtocol?.readLastData()
        bpmProtocol?.solveDataResult("4D3100029212")

        //無資料
        verify(listener).onResponseBPMReadLastData(null, 0, 0, 0, false)
    }
}