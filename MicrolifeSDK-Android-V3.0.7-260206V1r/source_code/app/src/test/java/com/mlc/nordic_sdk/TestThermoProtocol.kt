package com.mlc.nordic_sdk

import com.mlc.nordic_sdk.protocol.protocol_code.ThermoProtocol
import com.mlc.nordic_sdk.protocol.protocol_code.data.thermo.ThermoMeasureData
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.Mockito.mock



class TestThermoProtocol {
    private val listener: ThermoProtocol.OnDataResponseListener = mock(ThermoProtocol.OnDataResponseListener::class.java)
    private val thermoProtocol = ThermoProtocol.getInstance("UC*ugsoWfQYd0C!@", listener)

    @Test
    fun received_from_bt_A1() {
        thermoProtocol?.solveDataResult("4D41000AA1187A93C5C46900A0F0")

        verify(listener).onResponseDeviceInfo("18:7A:93:C5:C4:69", 0, 2.6f)
    }

    @Test
    fun received_from_bt_A0() {
        thermoProtocol?.solveDataResult("4D41000AA0097B0E638EC902179D")

        verify(listener).onResponseUploadMeasureData(ThermoMeasureData(ambientTemperature=24.27f, measureTemperature=36.83f, mode=0, minute=2, month=11, day=14, hour=9, flagErr=0, flagFever=0, errorCode=0, year=23))
    }
}