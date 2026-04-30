package com.example.ble_connect.data.repository

import com.example.ble_connect.data.ble.BleManager
import com.example.ble_connect.domain.model.BleDevice
import com.example.ble_connect.domain.repository.BleRepository

class BleRepositoryImpl (private val bleManager: BleManager) : BleRepository {
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
        onServiceUuidReceived: (List<String>) -> Unit
    ) {
        bleManager.connectToDevice(
            device.address,
            onConnected = onConnected,
            onServiceUuidReceived = onServiceUuidReceived
        )
    }

    override fun disconnectDevice() {
        bleManager.disconnectDevice()
    }
}