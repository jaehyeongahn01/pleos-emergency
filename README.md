<div align="center">

# 🚨 강도 몰래 신고 🚨

**원형교차로 팀**

택시 기사가 위협받는 상황에서, 핸들의 음소거 버튼만 눌러 강도에게  
들키지 않고 긴급 신고하는 Pleos Connect 차량용 앱

**APK:** [`apk/원형교차로_강도몰래신고_release.apk`](apk/원형교차로_강도몰래신고_release.apk)

![Platform](https://img.shields.io/badge/Platform-Pleos%20Connect-14204A)
![Android](https://img.shields.io/badge/Android-minSdk%2028-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)
![Version](https://img.shields.io/badge/Version-0.2.0-E5484D)

<img src="docs/images/gangdo_main.png" width="860" alt="강도 몰래 신고 메인 이미지">

</div>

---

현재 버전은 **0.2.0 (핵심 감지 기능 + 사용법 안내 + 신고 메시지 설정)** 입니다.  
계속 업데이트하고 있으며, 변경 내용은 [CHANGELOG.md](CHANGELOG.md)에 기록합니다.

---

## 📖 목차

1. [APK 파일 위치](#apk-파일-위치)
2. [한눈에 보기](#한눈에-보기)
3. [기능 구현 현황](#기능-구현-현황)
4. [테스트 환경](#테스트-환경)
5. [실행 방법](#실행-방법)
6. [소스에서 빌드](#소스에서-빌드)
7. [동작 구조](#동작-구조)
8. [에뮬레이터 검증 결과](#에뮬레이터-검증-결과)
9. [확인 필요한 제한 사항들](#확인-필요한-제한-사항들)
10. [로드맵](#로드맵)

---

## APK 파일 위치

| 항목 | 경로 |
|---|---|
| release APK | [`apk/원형교차로_강도몰래신고_release.apk`](apk/원형교차로_강도몰래신고_release.apk) |
| 패키지명 | `com.example.emergency` |
| 버전 | 0.2.0 (versionCode 3) |

---

## 한눈에 보기

<div align="center">

<img src="docs/images/gangdo_main_2.png" width="900" alt="MUTE 버튼 4번으로 강도 몰래 신고하는 4컷 만화">

</div>

1. 운행을 시작할 때 앱을 한 번 실행해 둡니다. 이후에는 재부팅해도 자동으로 시작됩니다.
2. 기사는 평소처럼 내비게이션 등 다른 앱을 사용합니다.
3. 위급할 때 핸들의 **MUTE 버튼을 빠르게 4번** 누릅니다.
4. 앱이 화면을 바꾸지 않고 패턴을 인식하고, 주행 정보 영역 맨 윗줄에 작은 파란 점으로 인식됐음을 알려 줍니다.
5. 설정해 둔 문구로 신고 메시지가 만들어집니다. (실제 발송은 다음 단계)

<table>
<tr>
<td width="50%" align="center"><img src="docs/images/overlay_before_final.png" alt="MUTE 4회 입력 전"><br><b>MUTE 4회 입력 전</b></td>
<td width="50%" align="center"><img src="docs/images/overlay_dot.png" alt="MUTE 4회 입력 후"><br><b>MUTE 4회 입력 후</b></td>
</tr>
</table>

<div align="center">

*내비게이션이 떠 있는 상태에서 MUTE 4회 입력 전(왼쪽)과 후(오른쪽)의 화면입니다.  
차량 배터리와 0km/h 사이의 “파란 점”이 인식 표시입니다.*

</div>

---

## 기능 구현 현황

| 기능 | 상태 |
|---|---|
| MUTE 버튼 4회 연속 누르기 감지 (다른 앱 사용 중에도 동작) | ✅ 구현, 에뮬레이터 검증 |
| 오인식 방지 (누름 간격 1.5초 초과 시 다시 셈, 3회 이하 무시) | ✅ 구현, 에뮬레이터 검증 |
| 기존 음소거 동작 유지 (짝수 횟수라 원래 소리 상태로 돌아옴) | ✅ 구현, 에뮬레이터 검증 |
| 인식 표시 점 (앱에서 끄거나 재부팅하면 사라짐) | ✅ 구현, 에뮬레이터 검증 |
| 부팅 시 자동 시작 | ✅ 구현, 에뮬레이터 검증 |
| 앱 아이콘 (Pleos 기본 앱 스타일, 독과 앱 목록에 표시) | ✅ 구현, 에뮬레이터 검증 |
| 사용법 안내 (처음 실행 시 자동 표시, '도움말'로 다시 보기) | ✅ 구현, 에뮬레이터 검증 |
| 신고 메시지 설정 (보내는/받는 번호, 차량 정보, 문구 편집, 미리보기) | ✅ 구현, 에뮬레이터 검증 |
| 인식 시 설정한 문구로 신고 메시지 생성 | ✅ 구현, 에뮬레이터 검증 (생성까지만) |
| 신고 메시지 실제 발송 (문자 등) |  예정 (발송 방식 결정 필요) |
| GPS 위치 연동 (메시지의 {위치}) |  예정 |
| 평범한 앱처럼 보이는 위장 UI |  예정 (현재 화면은 검증용) |

---

## 테스트 환경

| 항목 | 값 |
|---|---|
| 에뮬레이터 | **Pleos Connect v2.1.0** (AVD 이름 `Pleos_Connect_v2.1.0`, 빌드 `RELEASE.release_connect_v2.1.0`) |
| 시스템 이미지 | `system-images/android-34/pleos_car_emulator/arm64-v8a` (Android Automotive, 42dot) |
| Android 버전 | Android 14 (API 34) |
| 화면 규격 | **2560 × 1440 px, 160 dpi** (1dp = 1px), 가로 |
| 에뮬레이터 RAM | 4 GB |
| 테스트한 사용자 | 운전자 프로필 (user 10) |
| 호스트 | macOS (Apple Silicon), Android Studio, JDK 17 |
| 빌드 도구 | Gradle 8.11.1, Android Gradle Plugin 8.7.3, Kotlin 2.0.21 |
| 앱 SDK 설정 | minSdk 28, targetSdk 34, compileSdk 34 |

---

## 실행 방법

### 1. 설치

```bash
adb install apk/원형교차로_강도몰래신고_release.apk
```

### 2. 앱 실행

앱 목록에서 앱을 한 번 엽니다. Android는 설치 후 한 번도 실행하지 않은 앱에는 부팅 알림을 보내지 않기 때문에, 이 단계를 거쳐야 이후 재부팅 때 자동으로 시작됩니다.

### 3. 접근성 서비스 켜기 (필수)

버튼 입력은 접근성 서비스로 받기 때문에, 서비스를 켜야 동작합니다. 에뮬레이터에서는 아래 명령으로 켜는 것을 확인했습니다.

```bash
adb shell settings put secure enabled_accessibility_services com.example.emergency/com.example.emergency.ButtonService
adb shell settings put secure accessibility_enabled 1
```

앱 화면 맨 위에 **"버튼 감지 서비스: 켜짐"**(초록)이 보이면 준비된 것입니다. 앱의 "접근성 설정 열기" 버튼으로도 설정 화면을 열 수 있지만, Pleos 설정 화면에서 직접 켜는 경로는 아직 확인하지 못했습니다.

### 4. 사용법 안내

앱을 처음 열면 6쪽짜리 사용법 안내가 자동으로 나옵니다.  
이후에는 앱 첫 화면의 **도움말** 버튼으로 다시 볼 수 있습니다.

<table>
<tr>
<td width="33%"><img src="docs/images/guide_screen.png" alt="화면 구성 안내"><br><b>화면 구성 안내</b></td>
<td width="33%"><img src="docs/images/guide_wheel.png" alt="핸들 버튼 안내"><br><b>핸들 버튼 안내</b></td>
<td width="33%"><img src="docs/images/guide_page.png" alt="인식되면 안내"><br><b>인식되면 안내</b></td>
</tr>
</table>

### 5. 신고 메시지 설정

앱 첫 화면의 **신고 메시지 설정**에서 보내는 번호, 받는 번호(여러 개는 쉼표로 구분), 차량 정보, 메시지 문구를 정합니다. 문구에는 `{시각}` `{위치}` `{차량}` 을 넣을 수 있고 아래 미리보기로 실제 메시지를 확인합니다. 키보드 입력이 필요해서 정차 중에 설정하도록 만들었습니다.

<div align="center">

<img src="docs/images/settings.png" width="860" alt="신고 메시지 설정">

</div>

> 입력한 번호는 기기 안에만 저장되며 저장소에는 올라가지 않습니다.  
현재 버전은 감지되면 이 설정으로 메시지를 만들어서 기록하는 데까지 구현했습니다.

### 6. MUTE 버튼 입력 테스트

에뮬레이터에는 핸들 MUTE 버튼이 없습니다.  
그래서 가상 하드웨어 키보드를 만들어 MUTE 키를 보내는 스크립트를 제공하였습니다.

```bash
# 내비게이션 등 다른 앱을 띄워 둔 상태에서
./tools/mute_button.sh
# 스페이스/엔터 = MUTE 한 번 누르기, q = 종료
```

- 빠르게 4번 누르면 화면 왼쪽 위 배터리와 km/h 사이에 파란 점이 나타납니다.
- 앱을 열면 빨간 박스에 "신고 패턴 인식됨"과 시각, 당시 화면에 떠 있던 앱이 표시됩니다.
- 점을 끄려면 앱에서 **"인식 표시 끄기"** 를 누르거나 재부팅합니다.

로그는 `adb logcat -s BootProbe` 로 볼 수 있습니다.

---

## 소스에서 빌드

```bash
# debug 빌드
./gradlew assembleDebug

# 제출용 release APK (서명 키 필요) → apk/<팀명>_<앱이름>_release.apk
./gradlew exportReleaseApk
```

release 서명에는 저장소에 포함되지 않은 `keystore.properties` 가 필요합니다.

```properties
storeFile=/path/to/release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

APK 파일명에 쓰는 팀명과 앱 이름은 `gradle.properties` 의 `submission.teamName`, `submission.appName` 에서 바꿀 수 있습니다.

---

## 동작 구조

```text
핸들 MUTE 버튼
   │  (키 이벤트, 다른 앱이 화면에 있어도 수신)
   ▼
ButtonService (AccessibilityService)
   ├─ 4회 연속 누르기 판단 (간격 ≤ 1.5초)
   ├─ 인식 기록 저장 (시각, 횟수, 당시 앱, 부팅 번호)
   ├─ DetectOverlay: 파란 점 표시 (터치 통과, 끌 때까지 유지)
   └─ onEmergencyDetected(): ReportMessage로 신고 메시지 생성 (발송은 다음 단계)

BootReceiver → ProbeService (foreground service): 부팅 시 자동 시작, 상주 확인
MainActivity: 서비스 상태, 인식 상태, 로그 확인 (검증용 화면)
```

| 파일 | 역할 |
|---|---|
| `app/src/main/java/com/example/emergency/ButtonService.kt` | 버튼 입력 감지, 패턴 판단, 인식 기록 |
| `.../DetectOverlay.kt` | 인식 표시 점 (크기, 위치, 색은 파일 하단 상수) |
| `.../MainActivity.kt` | 첫 화면 (서비스 상태, 인식 상태, 도움말/설정 버튼, 로그) |
| `.../GuideActivity.kt`, `.../GuideArt.kt` | 사용법 안내 6쪽과 안내용 그림(화면 배치, 핸들, 버튼) |
| `.../SettingsActivity.kt`, `.../ReportMessage.kt` | 신고 메시지 설정 화면과 메시지 생성 |
| `.../Ui.kt` | 차량 화면용 공통 스타일 (큰 글씨, 큰 버튼) |
| `.../BootReceiver.kt`, `.../ProbeService.kt` | 부팅 시 자동 시작, 상주 서비스 |
| `.../EventLog.kt` | 이벤트 로그 (logcat 태그 `BootProbe`) |
| `tools/mute_button.sh`, `tools/mute_x4.jsonl` | 에뮬레이터용 MUTE 입력 도구 |

---

## 에뮬레이터 검증 결과

| 시나리오 | 결과 |
|---|---|
| 내비게이션이 떠 있을 때 MUTE 4회 연속 | 인식됨, 화면은 내비 그대로 |
| MUTE 3회 / 2초 간격으로 천천히 누르기 | 인식 안 됨 |
| 인식 후 15초 경과 | 표시 점 유지 |
| 인식 후 재부팅 | 표시 점 사라짐, 이후 다시 인식 가능 |
| 앱을 한 번 실행한 뒤 재부팅 | 백그라운드 서비스와 버튼 감지 자동 재시작 |
| 설치만 하고 실행하지 않은 채 재부팅 | 자동 시작 안 됨 (Android 정책) |

---

## 확인 필요한 제한 사항들

- **실차 핸들 버튼**: 에뮬레이터에서는 가상 키보드로 MUTE 키를 보냈습니다. 실차 핸들 버튼이 같은 경로(`KEYCODE_VOLUME_MUTE`)로 들어오는지는 아직 확인하지 못했습니다.
- **시동 OFF/ON**: 에뮬레이터의 재부팅으로 대신 시험했습니다. 실차에서 시동을 끄면 헤드유닛이 재부팅되는지, 절전만 하는지는 확인이 필요합니다.
- **시스템 볼륨 팝업**: MUTE를 누를 때마다 Pleos 시스템의 볼륨 팝업이 잠깐 표시됩니다. 앱에서 제어할 수 없습니다.
- **접근성 서비스 켜기**: 실차에서 사용자가 직접 켜는 경로와 앱 마켓 심사 허용 여부는 확인이 필요합니다.
- **표시 점 위치**: 2560×1440, 160dpi 화면에 맞춘 값입니다. 화면 규격이 다르면 위치가 달라질 수 있습니다.

---

## 로드맵

- [ ] 신고 메시지 실제 발송 (방식 결정 필요)
- [ ] 인식 시 GPS(Fused Location)와 차량 정보(Pleos Vehicle SDK) 수집
- [ ] 발송 결과를 확인하는 표시
- [ ] 접근성 서비스가 꺼졌을 때 관제 서버에 "감지 불가" 보고
- [ ] 평범한 앱처럼 보이는 위장 UI
- [ ] 오인식 방지 조건 보강 (주행/정차 상태 등)
- [ ] 장시간(8~12시간) 상주 검증
