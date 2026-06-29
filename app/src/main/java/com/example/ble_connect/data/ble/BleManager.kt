package com.example.ble_connect.data.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import kotlin.collections.mutableListOf
import com.example.ble_connect.domain.model.BleDevice
import com.example.ble_connect.domain.model.BleGattCharacteristic
import com.example.ble_connect.domain.model.BleGattService
import java.util.UUID

class BleManager(private val context: Context) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val bleScanner = bluetoothAdapter?.bluetoothLeScanner

    // 스캔된 장치들을 Android의 기본 자료형인 ScanResult 객체 형태로 저장할 리스트.
    // mutableListOf은 Kotlin에서 '가변 리스트'라는 의미. (화면을 다시 그리는 mutableStateOf랑은 다른 거임)
    private val scannedDevices = mutableListOf<ScanResult>()

    // 스캔 중지 시 필요한 콜백을 전역 변수로 관리
    private var currentScanCallback: ScanCallback? = null

    private var bluetoothGatt: BluetoothGatt? = null
    private var onConnectionChanged: ((Boolean) -> Unit)? = null
    private var onServicesReceivedCallback: ((List<BleGattService>) -> Unit)? = null

    private var connectedDevice: BleDevice? = null
    private var onDeviceUpdatedCallback: ((BleDevice) -> Unit)? = null

    @SuppressLint("MissingPermission")
    fun startScan(onDeviceFound: (Int) -> Unit) {
        if (bleScanner == null) {
            Log.e("BleManager", "Bluetooth LE 스캐너를 사용할 수 없습니다.")
            return
        }

        scannedDevices.clear() // 새로운 스캔 시작 시 리스트 초기화

        // 스캔 결과가 들어오는 콜백
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                // 동일한 장치가 중복 추가되지 않도록 체크 (MAC 주소 기준)
                if (scannedDevices.none { it.device.address == result.device.address }) {
                    scannedDevices.add(result)
                    Log.d("BleManager", "장치 발견: ${result.device.address}")
                    onDeviceFound(scannedDevices.size)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e("BleManager", "스캔 실패: $errorCode")
            }
        }

        currentScanCallback = scanCallback
        bleScanner.startScan(scanCallback)
    }

    // 배터리 절약을 위해 일정 시간 뒤 호출될 스캔 중지 함수
    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (bleScanner != null && currentScanCallback != null) {
            bleScanner.stopScan(currentScanCallback)
            currentScanCallback = null // 참조 해제
            Log.d("BleManager", "스캔이 중지되었습니다.")
        }
    }

    fun getScannedCount(): Int {
        return scannedDevices.size
    }

    @SuppressLint("MissingPermission")
    fun getScannedDevices(): List<BleDevice> {
        return scannedDevices.map { result ->
            BleDevice(
                name = result.device.name ?: "Unknown",
                address = result.device.address,
                rssi = result.rssi,
                device = result.device
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(
        address: String,
        onConnected: (Boolean) -> Unit,
        onDeviceUpdated: (BleDevice) -> Unit
    ) {
        val scanResult = scannedDevices.firstOrNull {
            it.device.address == address
        }
        if (scanResult == null) {
            Log.e("BleManager", "연결할 장치를 찾을 수 없습니다: $address")
            onConnected(false)
            return
        }

        onConnectionChanged = onConnected
        onDeviceUpdatedCallback = onDeviceUpdated

        bluetoothGatt?.close()
        bluetoothGatt = null

        Log.d("BleManager", "연결 시도: $address")

        bluetoothGatt = scanResult.device.connectGatt(context, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    fun disconnectDevice() {
        Log.d("BleManager", "연결 해제 요청")

        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null

        onConnectionChanged?.invoke(false)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e("BleManager", "GATT 오류 발생: status=$status, newState=$newState")

                onConnectionChanged?.invoke(false)

                gatt.close()
                if (bluetoothGatt == gatt) {
                    bluetoothGatt = null
                }
                return
            }

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d("BleManager", "GATT 연결 성공")

                    bluetoothGatt = gatt
                    onConnectionChanged?.invoke(true)

                    gatt.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d("BleManager", "GATT 연결 해제")

                    onConnectionChanged?.invoke(false)

                    gatt.close()
                    if (bluetoothGatt == gatt) {
                        bluetoothGatt = null
                    }
                }
            }
        }
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(
            gatt: BluetoothGatt,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e("BLE", "${gatt.services.size} services found")
                val services = gatt.services.map { service ->
                    BleGattService(
                        serviceUuid = service.uuid.toString(),
                        characteristic = service.characteristics.map { characteristic ->
                            BleGattCharacteristic(
                                characteristicUuid = characteristic.uuid.toString(),
                                properties = characteristic.properties
                            )
                        }
                    )
                }
                gatt.services.forEach { service ->
                    Log.d("BleManager", "Service UUID: ${service.uuid}")
                    service.characteristics.forEach { characteristic ->
                        Log.d("BleManager", "  Characteristic UUID: ${characteristic.uuid}")
                        Log.d("BleManager", "  Properties: ${characteristic.properties}")
                        characteristic.descriptors.forEach { descriptor ->
                            Log.d("BleManager", "    Descriptor UUID: ${descriptor.uuid}")
                        }
                        if (characteristic.properties == 18) {
                            Log.d("BLE", "prop=18 !")
                            subscribeNotification(gatt, characteristic)
                        }
                    }
                }
                connectedDevice = connectedDevice?.copy(
                    services = services
                )

                connectedDevice?.let {
                    onDeviceUpdatedCallback?.invoke(it)
                }
            } else {
                Log.e("BleManager", "서비스 탐색 실패: $status")
                onServicesReceivedCallback?.invoke(emptyList())
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            Log.d("BLE", "onCharacteristicChanged !")
            val text = characteristic.value.decodeToString()
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    context,
                    "수신값 : $text",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        private fun subscribeNotification(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val success = gatt.setCharacteristicNotification(characteristic, true)
            Log.d("BLE", "setCharacteristicNotification ! (in subNf)")
            val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            val writeSuccess = gatt.writeDescriptor(descriptor)
            Log.d("BLE", "writeDescriptor ! (in subNf)")
        }
    }

}