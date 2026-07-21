package com.example.ble_connect.ui.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ble_connect.DeviceActivity
import com.example.ble_connect.viewmodel.BleViewModel
import com.example.ble_connect.domain.model.BleDevice

// 현재 앱이 블루투스 스캔 권한을 가지고 있는지 확인하는 함수
fun checkBluetoothPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12 이상: BLUETOOTH_SCAN 권한 확인
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        // Android 11 이하: ACCESS_FINE_LOCATION 권한 확인
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}

// 장치 이름을 정돈해주는 함수
fun cutLongWord(s: String, len: Int=20): String {
    var result = ""
    if(s == "Unknown") {
        result = "-"
    } else if(s.length >= len) {
        result = s.substring(0, len-4) + "..."
    } else {
        result = s
    }
    return result
}

// https://developer.android.com/develop/ui/compose/quick-guides/content/finite-scrolling-list?hl=ko 참고함
@Composable
fun DevicesList(modifier: Modifier, viewModel: BleViewModel = viewModel()) {
    val deviceList = viewModel.devices.filter {
        it.name.isNotBlank() && it.name != "Unknown"
    }

    // 추가
    val context = LocalContext.current
    val isConnected by viewModel.isConnected
    val services by viewModel.services

//    LaunchedEffect(isConnected) {
//        if (isConnected) {
//            Toast.makeText(
//                context,
//                "장치 연결 성공",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//    }

//    LaunchedEffect(services) {
//        val firstServiceUuid = services.firstOrNull()?.serviceUuid
//        if (!firstServiceUuid.isNullOrEmpty()) {
//            Toast.makeText(context, firstServiceUuid, Toast.LENGTH_LONG).show()
//        }
//    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = deviceList,
            key = { device -> device.address }
        ) { device ->
                DeviceItem(device = device, index = deviceList.indexOf(device))
        }
    }
}

@Composable
fun DeviceItem(
    viewModel: BleViewModel = viewModel(),
    device: BleDevice,
    index: Int
) {
    val context = LocalContext.current

    val services by viewModel.services
    val isDiscoveringServices by viewModel.isDiscoveringServices

    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = (index + 1).toString(),
            color = Color(0xFF0088FF),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = cutLongWord(device.name),
            modifier = Modifier.clickable {
                showDialog = true
            }
        )

        Button(
            onClick = {
                viewModel.stopScanning()
                viewModel.connectToDevice(device)
                showDialog = true
            }
        ) {
            Text(
                text = "Connect",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                viewModel.disconnectDevice()
                viewModel.startScanning()
            },
            title = {
                Text(text = "${device.name}")
            },
            text = {
                Column (
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(text = "MAC : ${device.address}")
                    Text(text = "RSSI : ${device.rssi} dBm")
                    if (isDiscoveringServices) Text(text = "GATT Services : 검색 중...")
                    else {
                        Text(text = "GATT Services : ${services.size}")
                        services.forEachIndexed { serviceIndex, service ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Service ${serviceIndex + 1}",
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "UUID : ${service.serviceUuid}")
                            Text(text = "Characteristics : ${service.characteristic.size}")

                            service.characteristic.forEachIndexed {
                                    characteristicIndex,
                                    characteristic ->
                                Column (
                                    modifier = Modifier.padding(start=16.dp, top=8.dp)
                                ) {
                                    Text(text = "Characteristic ${characteristicIndex + 1}")
                                    Text(text = "UUID : ${characteristic.characteristicUuid}")
                                    Text(text = "Properties : ${characteristic.properties}")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false

                        val intent = Intent(context, DeviceActivity::class.java).apply {
                            putExtra("device_name", device.name)
                            putExtra("device_address", device.address)
                        }

                        context.startActivity(intent)
                    },
                    enabled = !isDiscoveringServices
                ) {
                    Text(text = "Connect")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        viewModel.disconnectDevice()
                        viewModel.startScanning()
                    }
                ) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}