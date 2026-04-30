package com.example.ble_connect.domain.model

import android.bluetooth.BluetoothDevice

data class BleDevice(
    val name: String,
    val address: String,        // MAC 주소
    val rssi: Int               // 신호 세기
    val device: BluetoothDevice // 장치 자체
)