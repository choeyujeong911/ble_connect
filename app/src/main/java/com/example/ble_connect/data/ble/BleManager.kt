package com.example.ble_connect.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log

class BleManager(private val context: Context) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val bleScanner = bluetoothAdapter?.bluetoothLeScanner

    // 스캔된 장치들을 임시로 저장할 리스트
    private val scannedDevices = mutableListOf<ScanResult>()

    // 스캔 중지 시 필요한 콜백을 전역 변수로 관리
    private var currentScanCallback: ScanCallback? = null

    @SuppressLint("MissingPermission") // 호출부(ViewModel/UI)에서 권한을 이미 체크했다고 가정
    fun startScan(onDeviceFound: (Int) -> Unit) {
        if (bleScanner == null) {
            Log.e("BleManager", "Bluetooth LE 스캐너를 사용할 수 없습니다.")
            return
        }

        scannedDevices.clear() // 새로운 스캔 시작 시 리스트 초기화

        // 스캔 결과가 들어오는 콜백
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                // 로그에 찍히는지 먼저 확인!
                Log.d("BleManager", "장치 발견: ${result.device.address}")

                // 동일한 장치가 중복 추가되지 않도록 체크 (MAC 주소 기준)
                if (scannedDevices.none { it.device.address == result.device.address }) {
                    scannedDevices.add(result)
                    Log.d("BleManager", "장치 발견: ${result.device.address}")
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e("BleManager", "스캔 실패: $errorCode")
            }
        }

        // 스캔 시작
        bleScanner.startScan(scanCallback)
    }

    // 주의: 실제 앱에서는 배터리 절약을 위해 일정 시간 뒤 반드시 stopScan을 호출해야 합니다.
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
}