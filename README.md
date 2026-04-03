# ble_connect 프로젝트

## 프로젝트 구조 설명
```
com.example.ble_connect
 │
 ├─ ui
 │   ├─ screen
 │   │   └─ BleScreen.kt
 │   └─ theme
 │       ├─ Color.kt
 │       ├─ Theme.kt
 │       └─ Type.kt
 │
 ├─ viewmodel
 │   └─ BleViewModel.kt
 │
 ├─ data
 │   ├─ ble
 │   │   └─ BleManager.kt
 │   └─ repository
 │       ├─ BleRepository.kt
 │       └─ BleRepositoryImpl.kt
 │
 ├─ MainActivity.kt
 └─ DeviceActivity.kt
```

## 각 패키지 내 Kotlin 파일 용도
- `MainActivity.kt` : 앱 메인 화면
- `DeviceActivity.kt` : BLE 장치 연결 후 정보 출력 화면
- `BleViewModel.kt` : asdf
- `BleScreen.kt` : 
- `Color.kt` : 
- `Theme.kt` : 
- `Type.kt` : 
- `BleManager.kt` : 
- `BleRepository.kt` : 인터페이스 정의
- `BleRepositoryImpl.kt` : 정의된 인터페이스대로 구현