package com.mlc.nordic_sdk.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mlc.nordic_sdk.viewModel.BGMActivityViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun BGMScreen(viewModel: BGMActivityViewModel) {
    val state by mutableStateOf(viewModel.state)
    val data by viewModel.listData.observeAsState(initial = emptyList())

    Scaffold(topBar = { TopBar("BGM") }, modifier = Modifier.safeDrawingPadding()) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when(state) {
                true -> {
                    if(viewModel.deviceName?.contains("BeneCheck-") == true) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.checkBGM700Function() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Check BGM700 Function", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.checkBGM700BGMTotal() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Check BGM700 BGM Total", fontSize = 10.sp)
                            }
                            Button(
                                onClick = { viewModel.downloadBGM700Data() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Download BGM700 Data", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.checkBGM700UricAcidTotal() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Check BGM700 Uric acid Total", fontSize = 10.sp)
                            }
                            Button(
                                onClick = { viewModel.downloadBGM700UricAcid() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Download BGM700 Uric acid", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.checkBGM700CholesterolTotal() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Check BGM700 Cholesterol Total", fontSize = 10.sp)
                            }
                            Button(
                                onClick = { viewModel.downloadBGM700Cholesterol() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Download BGM700 Cholesterol", fontSize = 10.sp)
                            }
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.readDeviceName() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Read Device Name", fontSize = 10.sp)
                            }
                            Button(onClick = { viewModel.readFirmwareVersion() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Read Firmware Version", fontSize = 10.sp)
                            }
                            Button(
                                onClick = { viewModel.gettingBLEStatus() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Get BLE Status", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = { viewModel.transmissionOneMeterData() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Transmission One Meter Data", fontSize = 10.sp)
                            }
                            Button(onClick = { viewModel.readDeviceTime() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Read Device Time", fontSize = 10.sp)
                            }
                            Button(
                                onClick = { viewModel.blePowerOff() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "BLE Power Off", fontSize = 10.sp)
                            }
                        }
                    }
                }
                else -> {}
            }
            Text(
                text = "Log",
                modifier = Modifier
                    .background(Color.Gray)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )

            // Remember a CoroutineScope to be able to launch
            val listState = rememberLazyListState()
            val coroutineScope = rememberCoroutineScope()

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                items(data) { index ->
                    TextItem(
                        text = index,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, color = Color(220, 220, 220))
                    )
                }
                coroutineScope.launch {
                    // Animate scroll to the last item
                    listState.animateScrollToItem(index = data.size)
                }
            }
        }
    }
}