package com.mlc.nordic_sdk

import com.mlc.nordic_sdk.protocol.protocol_code.WBPProtocol
import com.mlc.nordic_sdk.protocol.protocol_code.data.wbp.DRecordUsual
import com.mlc.nordic_sdk.protocol.protocol_code.data.wbp.MData
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.text.SimpleDateFormat
import java.util.Date



class TestWBPProtocol {
    private val listener: WBPProtocol.OnDataResponseListener = mock(WBPProtocol.OnDataResponseListener::class.java)
    private val wbpProtocol = WBPProtocol.getInstance("!KgrCkFL-7!T_bzd", listener)

    @Test
    fun write_to_WBP_readUsualModeHistory() {
        val checkSum = 0x4D +0xFF +0x02 + 0x00
        val excepted = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x02.toByte(), 0x00.toByte(), checkSum.toByte())

        val actual = wbpProtocol?.readUsualModeHistory()

        assertArrayEquals(excepted, actual)
    }

    @Test
    fun response_from_WBP_readUsualModeHistory() {
        wbpProtocol?.solveDataResult("4D5125000000FA000000000000000000000000000000000000000000000000007B5628C1246B209A")
        Mockito.verify<WBPProtocol.OnDataResponseListener>(listener).onResponseWBPReadUsualModeHistory(
        //Mockito.verify<WBPProtocol.OnDataResponseListener>(listener).onResponseReadUsualModeHistory(
            DRecordUsual(
                0,
                250,
                listOf(MData(
                    systole = 123,
                    diastole = 86,
                    hr = 40,
                    year = 2012,
                    month = 1,
                    day = 4,
                    hour = 17,
                    minute = 43,
                    option_arr = false,
                    option_usu = true,
                    option_dia = false,
                    option_afib = false,
                    option_am = false,
                    option_pm = false
                ))
            )
        )
    }

    @Test
    fun write_to_WBP_readUsualModeHistoryEachMeasurement() {
        val checkSum = 0x4D + 0xFF + 0x03 + 0x00 + 0xF1
        val excepted = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x03.toByte(), 0x00.toByte(), 0xF1.toByte(), checkSum.toByte())

        val actual = wbpProtocol?.readUsualModeHistoryEachMeasurement()

        assertArrayEquals(excepted, actual)
    }

    @Test
    fun response_from_WBP_readUsualModeHistoryEachMeasurement() {

    }

    @Test
    fun write_to_WBP_readDiagnosticModeHistory() {
        val checkSum = 0x4D + 0xFF + 0x02 + 0x01
        val excepted = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x02.toByte(), 0x01.toByte(), checkSum.toByte())

        val actual = wbpProtocol?.readDiagnosticModeHistory()

        assertArrayEquals(excepted, actual)
    }

    @Test
    fun response_from_WBP_readDiagnosticModeHistory() {

    }

    @Test
    fun write_to_WBP_clearSelectedModeHistory() {
        val excepted = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x03.toByte(), 0x02.toByte(), 0x03.toByte(), 0x54.toByte())

        val actual = wbpProtocol?.clearSelectedModeHistory(usual = true, diagnostic = true)

        assertArrayEquals(excepted, actual)
    }

    @Test
    fun response_from_WBP_clearSelectedModeHistory() {
        wbpProtocol?.solveDataResult("4D5103028124")

        //Mockito.verify(listener).onResponseClearSelectedModeHistory(true)
        Mockito.verify<WBPProtocol.OnDataResponseListener>(listener).onResponseWBPClearSelectedModeHistory(true)
    }

    @Test
    fun write_to_WBP_clearCurrentModeHistory() {
        val excepted = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x02.toByte(), 0x03.toByte(), 0x51.toByte())

        val actual = wbpProtocol?.clearCurrentModeHistory()

        assertArrayEquals(excepted, actual)
    }

    @Test
    fun response_from_WBP_clearCurrentModeHistory() {
        wbpProtocol?.solveDataResult("4D5103038125")

        //Mockito.verify(listener).onResponseClearCurrentModeHistory(true)
        Mockito.verify<WBPProtocol.OnDataResponseListener>(listener).onResponseWBPClearCurrentModeHistory(true)
    }

    @Test
    fun write_to_WBP_disconnectBluetooth() {
        val excepted = byteArrayOf(0x4D.toByte(), 0xFF.toByte(), 0x02.toByte(), 0x04.toByte(), 0x52.toByte())

        val actual = wbpProtocol?.disconnectBluetooth()

        assertArrayEquals(excepted, actual)
    }

    @Test
    fun write_to_WBP_writeDeviceTime() {
        val nowDate: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        val t1 = nowDate.split(" ")
        val year = (t1[0].split("-")[0].toLong()-2000) and 0xff
        val month = t1[0].split("-")[1].toLong() and 0xff
        val day = t1[0].split("-")[2].toLong() and 0xff
        val hour = t1[1].split(":")[0].toLong() and 0xff
        val minute = t1[1].split(":")[1].toLong() and 0xff
        val second = t1[1].split(":")[2].toLong() and 0xff

        val checkSum = 0x4D + 0xFF + 0x08 + 0x05 + year + month + day + hour + minute + second

        val expected = Method.byteArrayOfInts(0x4D, 0xFF, 0x08, 0x05, year, month, day, hour, minute, second, checkSum)

        val actual = wbpProtocol?.writeDeviceTime()

        assertArrayEquals(expected, actual)
    }

    @Test
    fun response_from_WBP_writeDeviceTime() {
        wbpProtocol?.solveDataResult("4D5103058127")

        //Mockito.verify(listener).onResponseWriteDeviceTime(true)
        Mockito.verify<WBPProtocol.OnDataResponseListener>(listener).onResponseWBPWriteDeviceTime(true)
    }
}