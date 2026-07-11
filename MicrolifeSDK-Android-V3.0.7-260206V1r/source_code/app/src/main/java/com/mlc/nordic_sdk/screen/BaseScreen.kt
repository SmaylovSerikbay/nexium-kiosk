package com.mlc.nordic_sdk.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import com.mlc.nordic_sdk.viewModel.BaseActivityViewModel
import com.mlc.nordic_sdk.bluetooth.data.DeviceType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun BaseScreen(viewModel: BaseActivityViewModel) {
    val state by mutableStateOf(viewModel.state)
    val deviceType by mutableStateOf(viewModel.deviceType)
    val data by viewModel.listData.observeAsState(initial = emptyList())

    Scaffold(topBar = { TopBar("Base") }, modifier = Modifier.safeDrawingPadding()) {
        Column(modifier = Modifier
            .padding(it)
            .fillMaxSize()) {
            if(state) {
                when(deviceType) {
                    DeviceType.MLC_BPM3G -> {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.readBPMUserAndVersionData() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Read Version Data", fontSize = 10.sp)
                            }
                            Button(onClick = { viewModel.readBPMAllHistory() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Read History", fontSize = 10.sp)
                            }
                            Button(onClick = { viewModel.writeBPMUserId() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Write User Id", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = { viewModel.clearBPMAllHistory() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Clear All History", fontSize = 10.sp)
                            }
                            Button(onClick = { viewModel.disconnect() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Disconnect", fontSize = 10.sp)
                            }
                        }
                    }
                    DeviceType.MLC_BPM4G,
                    DeviceType.WAG_BPM4G,
                    DeviceType.MLC_BPM5G -> {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.readBPMUserAndVersionData() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Read Version Data", fontSize = 10.sp)
                            }
                            Button(onClick = { viewModel.readBPMAllHistory() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Read History", fontSize = 10.sp)
                            }
                            Button(onClick = { viewModel.readBPMDeviceInfo() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Read Device Info", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = { viewModel.writeBPMUserId() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Write User Id", fontSize = 10.sp)
                            }
                            Button(
                                onClick = { viewModel.readSN() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "read SN", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = { viewModel.clearBPMAllHistory() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Clear All History", fontSize = 10.sp)
                            }
                            Button(onClick = { viewModel.disconnect() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Disconnect", fontSize = 10.sp)
                            }
                        }
                    }
                    DeviceType.MLC_PF2 -> {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.readPFMHistory() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Read All History", fontSize = 10.sp)
                            }
                            Button(
                                onClick = { viewModel.writeBestValue(400) }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Write Best Value", fontSize = 10.sp)
                            }
                            Button(onClick = { viewModel.readPFMBestValue() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Read Best Value", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = { viewModel.writePFMDeviceTime() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Write Device Time", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.readSN() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "read SN", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.clearPFMHistory() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Clear History", fontSize = 10.sp)
                            }
                            Button(onClick = { viewModel.disconnect() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Disconnect", fontSize = 10.sp)
                            }
                        }
                    }
                    DeviceType.MLC_WBP -> {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.readDeviceIDAndInfo() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Read device ID and info", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.readUsualModeHistory() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Read usual mode history", fontSize = 10.sp)
                            }
                            Button(
                                onClick = { viewModel.readDiagnosticModeHistory() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Read diagnostic mode history", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.writeDeviceTime() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Write device time", fontSize = 10.sp)
                            }
                            /*
                            Button(
                                onClick = { viewModel.readDeviceTime() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Read device time", fontSize = 10.sp)
                            }

                             */
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.readUserIDAndVersionData() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Read user ID and version data", fontSize = 10.sp)
                            }
                            Button(
                                onClick = { viewModel.writeNewUserID() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Write new user ID", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.readWBPSN() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Read WBP SN", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.disconnectBluetooth() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Disconnect", fontSize = 10.sp)
                            }
                            Button(
                                onClick = { viewModel.clearSelectedModeHistory() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Clear selected mode history", fontSize = 10.sp)
                            }
                        }
                    }
                    else -> {}
                }
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