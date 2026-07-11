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
import com.mlc.nordic_sdk.bluetooth.data.ConnectState
import com.mlc.nordic_sdk.viewModel.SPO2ActivityViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun SPO2Screen(viewModel: SPO2ActivityViewModel) {
    val state by mutableStateOf(viewModel.state)
    val data by viewModel.listData.observeAsState(initial = emptyList())

    Scaffold(topBar = { TopBar("SPO2") }, modifier = Modifier.safeDrawingPadding()) {
        Column(modifier = Modifier
            .padding(it)
            .fillMaxSize()) {
            if(state) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { viewModel.readSPO2Limit() }, modifier = Modifier
                            .weight(1f)
                            .padding(5.dp)) {
                        Text(text = "Read SPO2 Limit", fontSize = 10.sp)
                    }
                    Button(onClick = { viewModel.writeSPO2Limit() }, modifier = Modifier
                        .weight(1f)
                        .padding(5.dp)) {
                        Text(text = "Write SPO2 Limit", fontSize = 10.sp)
                    }
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