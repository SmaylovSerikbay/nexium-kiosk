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
import androidx.compose.material3.TopAppBar
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
import com.mlc.nordic_sdk.bluetooth.data.DeviceType
import com.mlc.nordic_sdk.viewModel.BPMActivityViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun BPMScreen(viewModel: BPMActivityViewModel) {
    val state by mutableStateOf(viewModel.state)
    val deviceType by mutableStateOf(viewModel.deviceType)
    val data by viewModel.listData.observeAsState(initial = emptyList())

    Scaffold(topBar = { TopBar("BPM") }, modifier = Modifier.safeDrawingPadding()) {
        Column(modifier = Modifier
            .padding(it)
            .fillMaxSize()) {
            when(deviceType) {
                DeviceType.MLC_BPM5G -> {
                    if(state) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.readAllHistory() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Read History", fontSize = 10.sp)
                            }
                            Button(
                                onClick = { viewModel.readUserAndVersionData() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Read User And Version Data", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.writeUserId() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Write User ID", fontSize = 10.sp)
                            }
                            Button(
                                onClick = { viewModel.readDeviceInfo() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Read Device Info", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.readDeviceTime() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Read Device Time", fontSize = 10.sp)
                            }
                            Button(
                                onClick = { viewModel.writeDeviceTime() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Write Device Time", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.checkTransmitOK() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Check Transmit OK", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.readSerialNumber() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Read SN", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.readLastData() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Read Last Data", fontSize = 10.sp)
                            }
                            Button(
                                onClick = { viewModel.clearLastData() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Clear Last Data", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.disconnect() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Disconnect", fontSize = 10.sp)
                            }
                        }
                    }
                }
                DeviceType.MLC_BPM4G -> {
                    if(state) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.readUserAndVersionData() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Read Version Data", fontSize = 10.sp)
                            }
                            Button(onClick = { viewModel.readAllHistory() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Read History", fontSize = 10.sp)
                            }
                            Button(onClick = { viewModel.readDeviceInfo() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Read Device Info", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = { viewModel.writeDeviceTime() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Write Device Time", fontSize = 10.sp)
                            }
                            Button(onClick = { viewModel.writeUserId() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Write User Id", fontSize = 10.sp)
                            }
                            Button(onClick = { viewModel.readDeviceTime() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Read Device Time", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = { viewModel.readSerialNumber() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Read SN", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = { viewModel.clearLastData() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Clear Last Data", fontSize = 10.sp)
                            }
                            Button(onClick = { viewModel.readLastData() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Read Last Data", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = { viewModel.clearAllHistory() }, modifier = Modifier
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
                }
                DeviceType.MLC_BPM3G -> {
                    if(state) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.readUserAndVersionData() }, modifier = Modifier
                                    .weight(1f)
                                    .padding(5.dp)) {
                                Text(text = "Read Version Data", fontSize = 10.sp)
                            }
                            Button(onClick = { viewModel.readAllHistory() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Read History", fontSize = 10.sp)
                            }
                            Button(onClick = { viewModel.writeUserId() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Write User Id", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = { viewModel.clearLastData() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Clear Last Data", fontSize = 10.sp)
                            }
                            Button(onClick = { viewModel.readLastData() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                                Text(text = "Read Last Data", fontSize = 10.sp)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = { viewModel.clearAllHistory() }, modifier = Modifier
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

@ExperimentalMaterial3Api
@Composable
fun TopBar(title: String) {
    TopAppBar(
        title = {
            Column() {
                Text(text = title, fontSize = 20.sp)
            }
        }
    )
}

@Composable
fun TextItem(text: String, modifier: Modifier) {
    val color: Color =
        if(text.startsWith("WRITE")) {
            Color.Red
        } else if(text.startsWith("NOTIFY")) {
            Color.Blue
        } else {
            Color.Green
        }
    Text(
        text = text,
        modifier = modifier.padding(start = 5.dp),
        color = color
    )
}