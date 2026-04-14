package com.example.ble_connect.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ble_connect.data.ble.BleManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BleViewModel(application: Application) : AndroidViewModel(application) {
    // 장치 목록을 관리하는 State
    private val _foundDevicesCount = mutableStateOf(0)
    val foundDevicesCount: State<Int> =_foundDevicesCount

    // 2. BleManager 인스턴스를 생성합니다.
    private val bleManager = BleManager(application.applicationContext)

    fun startScanningProcess(hasPermission: Boolean, onCountReady: (Int) -> Unit) {
        if (!hasPermission) return

        // ViewModel 내부의 코루틴 스코프에서 실행
        viewModelScope.launch {
            // 1. 실제 스캔 시작 로직 호출 (BleManager 등 활용)
            bleManager.startScan { /* 실시간 개수 반영 필요 시 작성 */ }

            // 2. 5초 대기
            delay(5000)

            // 5. 스캔 중지 (매니저에 stopScan 함수가 구현되어 있어야 함)
            //bleManager.stopScan()

            // 6. 최종 개수 파악 및 알림
            val count = bleManager.getScannedCount()
            _foundDevicesCount.value = count
            onCountReady(count)
        }
    }
}