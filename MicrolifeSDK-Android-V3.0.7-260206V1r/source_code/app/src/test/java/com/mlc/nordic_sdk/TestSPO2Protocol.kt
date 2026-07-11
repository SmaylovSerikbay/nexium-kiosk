package com.mlc.nordic_sdk

import com.mlc.nordic_sdk.protocol.protocol_code.SPO2Protocol
import com.mlc.nordic_sdk.protocol.protocol_code.data.spo2.Spo2Data
import com.mlc.nordic_sdk.protocol.protocol_code.data.spo2.Spo2Limit
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify



class TestSPO2Protocol {
    private val listener: SPO2Protocol.OnDataResponseListener = mock(SPO2Protocol.OnDataResponseListener::class.java)
    private val spo2Protocol = SPO2Protocol.getInstance("%RDYG#na2O@=bbyh", listener)

    @Test
    fun received_graphicsData_from_spo2() {
        spo2Protocol?.solveDataResult("800102030405060708090A")

        verify(listener).onResponseSpo2Data(Spo2Data(points = listOf(1,2,3,4,5,6,7,8,9,10), error = 0))
    }

    @Test
    fun received_spo2Data_from_spo2() {
        spo2Protocol?.solveDataResult("81500001")

        verify(listener).onResponseSpo2Data(Spo2Data(80, 0, 1, null, 0))
    }

    @Test
    fun response_from_spo2_readSPO2Limit() {
        spo2Protocol?.solveDataResult("826446A032")

        verify(listener).onResponseSpo2Limit(Spo2Limit(100, 70, 160, 50))
    }

    @Test
    fun write_command_to_spo2_writeSPO2Limit() {
        val spo2UpperLimit = 100
        val spo2LowerLimit = 70
        val pulUpperLimit = 160
        val pulLowerLimit = 50

        val expected = byteArrayOf(0x02.toByte(), spo2UpperLimit.toByte(), spo2LowerLimit.toByte(), pulUpperLimit.toByte(), pulLowerLimit.toByte())

        val actual = spo2Protocol?.writeSpo2Limit(spo2UpperLimit, spo2LowerLimit, pulUpperLimit, pulLowerLimit)

        Assertions.assertArrayEquals(expected, actual)
    }
}