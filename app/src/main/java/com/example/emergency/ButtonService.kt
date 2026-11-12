package com.example.emergency

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

/**
 * 다른 앱(내비게이션 등)이 화면에 떠 있어도 핸들 버튼 입력을 받기 위한 접근성 서비스.
 *
 * - MUTE 버튼을 [PRESS_COUNT]번 연속(간격 [MAX_GAP_MS] 이하)으로 누르면 "신고 패턴 인식"으로 기록한다.
 * - 키 이벤트는 소비하지 않는다(return false). 즉 기존 음소거 동작은 그대로 유지된다.
 * - 소리, 진동, 팝업 없음. 인식되면 왼쪽 위에 점만 띄운다 ([DetectOverlay]).
 * - 볼륨/미디어/음소거 계열 키만 추적한다. 그 외 키(문자 입력 등)는 읽지 않는다.
 */
class ButtonService : AccessibilityService() {

    private val pressTimes = ArrayList<Long>()
    private var foregroundPackage: String = "?"

    private fun recordDetection(message: String) {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_DETECT_BOOT, currentBootCount(this))
            .putLong(KEY_LAST_DETECT, System.currentTimeMillis())
            .putString(KEY_LAST_FOREGROUND, foregroundPackage)
            .putInt(KEY_COUNT, prefs.getInt(KEY_COUNT, 0) + 1)
            .apply()
        EventLog.add(this, "DETECT $message (포그라운드=$foregroundPackage)")
        // 앱을 열지 않아도 운전자가 인식 여부를 알 수 있게 점을 띄운다 (앱에서 끌 때까지 유지).
        overlay?.show()
        onEmergencyDetected()
    }

    private var overlay: DetectOverlay? = null

    /**
     * 인식 상태(KEY_LAST_DETECT)에 맞춰 점을 켜고 끈다.
     * 앱의 "인식 표시 끄기"가 KEY_LAST_DETECT를 지우면 여기서 점이 사라진다.
     * (MainActivity와 같은 프로세스라 변경이 바로 전달된다)
     */
    private val prefsListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, _ ->
        syncOverlay(p)
    }

    private fun syncOverlay(p: android.content.SharedPreferences) {
        if (p.getLong(KEY_LAST_DETECT, 0L) != 0L) overlay?.show() else overlay?.hide()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlay = DetectOverlay(this)
        EventLog.add(this, "A11Y 서비스 연결됨")
        // 재부팅(시동 OFF/ON) 뒤라면 이전 부팅의 인식 표시를 지운다.
        clearIfNewBoot(this)
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
        // 같은 부팅 안에서 서비스만 재시작된 경우엔 인식 표시를 다시 띄운다.
        syncOverlay(prefs)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            event.packageName?.toString()?.let { foregroundPackage = it }
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (!isTracked(event.keyCode)) {
            // 진단용: 추적 대상이 아닌 키도 "서비스에 키가 도달하는지"만 확인하기 위해 종류 없이 기록
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                EventLog.add(this, "KEY (기타 키 수신, 내용 미기록)")
            }
            return false
        }

        // 길게 누름 중 반복되는 DOWN(repeatCount>0)은 로그가 너무 많아지므로 기록하지 않는다.
        if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
            EventLog.add(this, "KEY DOWN ${KeyEvent.keyCodeToString(event.keyCode)} (포그라운드=$foregroundPackage)")
        } else if (event.action == KeyEvent.ACTION_UP) {
            EventLog.add(this, "KEY UP   ${KeyEvent.keyCodeToString(event.keyCode)}")
        }

        if (isMute(event.keyCode) && event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
            val now = event.eventTime
            // 직전 누름과 간격이 너무 길면 새 시퀀스로 시작
            if (pressTimes.isNotEmpty() && now - pressTimes.last() > MAX_GAP_MS) {
                pressTimes.clear()
            }
            pressTimes.add(now)
            EventLog.add(this, "KEY MUTE 연속 ${pressTimes.size}/$PRESS_COUNT")
            if (pressTimes.size >= PRESS_COUNT) {
                val span = pressTimes.last() - pressTimes.first()
                pressTimes.clear()
                recordDetection("MUTE ${PRESS_COUNT}회 연속 누름 인식 (${span}ms)")
            }
        }
        return false // 이벤트를 소비하지 않음
    }

    override fun onInterrupt() {
        pressTimes.clear()
    }

    override fun onDestroy() {
        getSharedPreferences(PREFS, MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(prefsListener)
        overlay?.hide()
        overlay = null
        EventLog.add(this, "A11Y 서비스 종료")
        super.onDestroy()
    }

    /** 실제 신고 전송이 붙을 자리. 지금은 기록만 한다. */
    private fun onEmergencyDetected() {
        // TODO: GPS/차량정보 수집 후 관제 서버로 전송 (현재는 미구현)
    }

    private fun isMute(code: Int) =
        code == KeyEvent.KEYCODE_VOLUME_MUTE || code == KeyEvent.KEYCODE_MUTE

    private fun isTracked(code: Int) = when (code) {
        KeyEvent.KEYCODE_VOLUME_MUTE,
        KeyEvent.KEYCODE_VOLUME_UP,
        KeyEvent.KEYCODE_VOLUME_DOWN,
        KeyEvent.KEYCODE_MUTE,
        KeyEvent.KEYCODE_MEDIA_NEXT,
        KeyEvent.KEYCODE_MEDIA_PREVIOUS,
        KeyEvent.KEYCODE_MEDIA_PLAY,
        KeyEvent.KEYCODE_MEDIA_PAUSE,
        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> true
        else -> false
    }

    companion object {
        /** 몇 번 연속으로 눌러야 인식하는지 */
        const val PRESS_COUNT = 4
        /** 누름 사이 최대 간격. 이보다 길면 처음부터 다시 센다. */
        const val MAX_GAP_MS = 1_500L
        const val PREFS = "detect"
        const val KEY_LAST_DETECT = "last_detect"
        const val KEY_LAST_FOREGROUND = "last_foreground"
        const val KEY_COUNT = "count"
        /** 인식 당시의 시스템 부팅 횟수 */
        const val KEY_DETECT_BOOT = "detect_boot"

        /** 시스템 부팅 횟수 (재부팅할 때마다 1씩 증가). 못 읽으면 -1 */
        fun currentBootCount(context: android.content.Context): Int =
            android.provider.Settings.Global.getInt(
                context.contentResolver, android.provider.Settings.Global.BOOT_COUNT, -1
            )

        /**
         * 인식 표시가 이전 부팅에서 켜진 것이면 지운다(점 꺼짐). 누적 횟수와 로그는 남긴다.
         * 재부팅 = 시동 OFF/ON 으로 간주한다.
         */
        fun clearIfNewBoot(context: android.content.Context) {
            val prefs = context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
            if (prefs.getLong(KEY_LAST_DETECT, 0L) == 0L) return
            val now = currentBootCount(context)
            if (prefs.getInt(KEY_DETECT_BOOT, Int.MIN_VALUE) != now) {
                prefs.edit().remove(KEY_LAST_DETECT).apply()
                EventLog.add(context, "BOOT 새 부팅(boot_count=$now) → 이전 인식 표시 해제")
            }
        }
    }
}
