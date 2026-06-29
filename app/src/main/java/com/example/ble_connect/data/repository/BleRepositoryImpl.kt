package com.example.ble_connect.data.repository

import com.example.ble_connect.data.ble.BleManager
import com.example.ble_connect.domain.model.BleDevice
import com.example.ble_connect.domain.model.BleGattService
import com.example.ble_connect.domain.repository.BleRepository

class BleRepositoryImpl (private val bleManager: BleManager) : BleRepository {
    val receivedValue = bleManager.receivedValue
    override fun startScan() {
        bleManager.startScan {  }
    }
    override fun stopScan() {
        bleManager.stopScan()
    }
    override fun getScannedCount(): Int {
        return bleManager.getScannedCount()
    }
    override fun getScannedDevices(): List<BleDevice> {
        return bleManager.getScannedDevices()
    }

    override fun connectToDevice(
        device: BleDevice,
        onConnected: (Boolean) -> Unit,
        onDeviceUpdated: (BleDevice) -> Unit,
        onValueReceived: (String) -> Unit
    ) {
        bleManager.connectToDevice(
            device.address,
            onConnected = onConnected,
            onDeviceUpdated = onDeviceUpdated,
            onValueReceived = onValueReceived
        )
    }

    override fun disconnectDevice() {
        bleManager.disconnectDevice()
    }
}