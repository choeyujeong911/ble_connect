package com.example.ble_connect.domain.model

import android.bluetooth.BluetoothDevice

data class BleGattService (
    val serviceUuid: String,
    val characteristic: List<BleGattCharacteristic>
)