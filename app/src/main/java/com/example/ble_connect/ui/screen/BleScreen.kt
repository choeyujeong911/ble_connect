package com.example.ble_connect.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ble_connect.ui.theme.Ble_connectTheme
import com.example.ble_connect.viewmodel.BleViewModel

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

@Composable
fun ScanButton(viewModel: BleViewModel = viewModel()) {
    val context = LocalContext.current  // Toast를 위한 임시 변수(권한 체크를 위한 것)
    val isScanning by viewModel.isScanning  // ViewModel의 스캐닝 상태를 관찰

    // 스캐닝 상태에 따른 버튼 색상 및 텍스트 미리 정의
    val btnColor = if (isScanning) Color.Gray else Color(0xFF0088FF)
    val btnText = if (isScanning) "SCANNING..." else "SCAN"

    Button(
        onClick = {
            val hasPermission = checkBluetoothPermission(context)

            if (hasPermission) {
                Toast.makeText(context, "블루투스 권한 있음!! 5초 스캔 시작", Toast.LENGTH_SHORT).show()

                // ViewModel에 스캔 프로세스 요청
                // 중괄호 안의 내용은 다음과 같은 의미:
                // count라는 Int 타입의 매개변수를 받는 익명함수가
                // Toast.makeText(context, "장치 ${count}개 검색됨", Toast.LENGTH_SHORT).show() 이라는 짧은 실행문을 가짐
                viewModel.startScanningProcess(hasPermission) { count ->
                    Toast.makeText(context, "장치 ${count}개 검색됨", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "권한 없음", Toast.LENGTH_SHORT).show()
            } },
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(24.dp),
        enabled = !isScanning,   // 스캔 중 버튼 클릭 비활성화
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = btnColor,
            contentColor = Color.White,
            disabledContainerColor = Color.Gray
        )
    ) { Text(text = btnText, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
}
// https://developer.android.com/develop/ui/compose/quick-guides/content/finite-scrolling-list?hl=ko 참고함
@Composable
fun DevicesList() {
    Button(onClick = {}) {
        Text(text = "test")
    }
}

@Composable
fun DeviceItem(viewModel: BleViewModel = viewModel()) {
    val isScanning by viewModel.isScanning  // ViewModel의 스캐닝 상태를 관찰
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

/*
* 디자인 시 미리보기를 위한 함수(쓸 때에만 활성화!!)
* 근데 얘 ViewModel이랑은 연동이 안되는 듯함 ㅠㅠ
* */
//@OptIn(ExperimentalMaterial3Api::class)
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    Ble_connectTheme {
//        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//            Column(
//                modifier = Modifier
//                    .padding(10.dp)
//                    .padding(innerPadding)
//                    .fillMaxSize(),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center
//            ) {
//                DevicesList()
//            }
//        }
//    }
//}