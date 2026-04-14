# ble_connect 프로젝트

## 프로젝트 구조 설명
```
com.example.ble_connect
 ├─ ui
 │   ├─ screen
 │   │   └─ BleScreen.kt
 │   └─ theme
 │       ├─ Color.kt
 │       ├─ Theme.kt
 │       └─ Type.kt
 ├─ viewmodel
 │   └─ BleViewModel.kt
 ├─ data
 │   ├─ ble
 │   │   └─ BleManager.kt
 │   └─ repository
 │       ├─ BleRepository.kt
 │       └─ BleRepositoryImpl.kt
 ├─ MainActivity.kt
 └─ DeviceActivity.kt
```

## 각 패키지 내 Kotlin 파일 용도
- `MainActivity.kt` : 앱 메인 화면, Compose UI 시작점 및 BleViewModel 관찰
- `DeviceActivity.kt` : BLE 장치 연결 후 수신한 데이터를 표시하는 화면
- `BleViewModel.kt` : UI 상태 관리, Repository를 통해 BLE 데이터 수집 및 가공, UI 이벤트 처리(스캔 시작, 연결 요청 등)
- `BleScreen.kt` : 메인 UI 구성(스캔 버튼, BLE 장치 리스트 표시), ViewModel 상태에 따라 화면에 렌더링
- `Color.kt` : 앱에서 사용하는 색상을 정의
- `Theme.kt` : 앱 전체 테마 설정
- `Type.kt` : 텍스트 스타일 정의
- `BleManager.kt` : 스캔, GaTT 연결, Notify 수신 등 저수준의 BLE 로직 담당(Android BLE API 직접 처리)
- `BleRepository.kt` : BLE 데이터 처리 기능에 대한 인터페이스 정의
- `BleRepositoryImpl.kt` : BleRepository를 구현한 것, BleManager를 사용해 실제 BLE 동작 수행 및 Flow 형태로 데이터 제공