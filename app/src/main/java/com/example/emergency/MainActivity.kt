package com.example.emergency

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 검증용 화면.
 * - 접근성 서비스(버튼 감지) 켜짐 여부
 * - MUTE 4번 연속 누르기 인식 여부(마지막 시각, 누적 횟수, 당시 포그라운드 앱)
 * - 버튼 관련 이벤트 로그
 */
class MainActivity : Activity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var serviceStatus: TextView
    private lateinit var detectStatus: TextView
    private lateinit var logView: TextView
    private lateinit var scroll: ScrollView

    private val refresh = object : Runnable {
        override fun run() {
            render()
            handler.postDelayed(this, 1_000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventLog.add(this, "ACTIVITY onCreate")
        ButtonService.clearIfNewBoot(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        serviceStatus = TextView(this).apply { textSize = 18f }
        detectStatus = TextView(this).apply {
            textSize = 26f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(24, 48, 24, 48)
        }

        val buttons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        fun button(label: String, action: () -> Unit) = Button(this).apply {
            text = label
            setOnClickListener { action() }
            buttons.addView(this, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }
        button("접근성 설정 열기") {
            try {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } catch (e: Exception) {
                Toast.makeText(this, "접근성 설정 화면을 열 수 없음: ${e.javaClass.simpleName}", Toast.LENGTH_LONG).show()
            }
        }
        button("인식 표시 끄기") {
            // 왼쪽 위 점만 끈다. 누적 횟수와 로그는 남긴다.
            getSharedPreferences(ButtonService.PREFS, MODE_PRIVATE).edit()
                .remove(ButtonService.KEY_LAST_DETECT).apply()
            EventLog.add(this, "ACTIVITY 인식 표시 끄기")
            render()
        }
        button("인식 기록 초기화") {
            getSharedPreferences(ButtonService.PREFS, MODE_PRIVATE).edit().clear().apply()
            EventLog.clear(this)
            render()
        }

        logView = TextView(this).apply {
            typeface = Typeface.MONOSPACE
            textSize = 12f
            setPadding(8, 16, 8, 16)
        }
        scroll = ScrollView(this).apply { addView(logView) }

        root.addView(serviceStatus)
        root.addView(detectStatus, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = 16; bottomMargin = 16
        })
        root.addView(buttons)
        root.addView(scroll, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        handler.post(refresh)
    }

    override fun onPause() {
        handler.removeCallbacks(refresh)
        super.onPause()
    }

    private fun isServiceEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.split(':').any { it.startsWith("$packageName/") && it.endsWith("ButtonService") }
    }

    private fun render() {
        val on = isServiceEnabled()
        serviceStatus.text = if (on) "버튼 감지 서비스: 켜짐" else "버튼 감지 서비스: 꺼짐 (접근성 설정에서 켜야 함)"
        serviceStatus.setTextColor(if (on) Color.rgb(0, 128, 0) else Color.rgb(200, 0, 0))

        val prefs = getSharedPreferences(ButtonService.PREFS, MODE_PRIVATE)
        val last = prefs.getLong(ButtonService.KEY_LAST_DETECT, 0L)
        if (last == 0L) {
            val count = prefs.getInt(ButtonService.KEY_COUNT, 0)
            detectStatus.text = "MUTE ${ButtonService.PRESS_COUNT}번 연속 누르기: 대기 중" +
                if (count > 0) "\n(지금까지 누적 ${count}회 인식)" else ""
            detectStatus.setTextColor(Color.DKGRAY)
            detectStatus.setBackgroundColor(Color.rgb(235, 235, 235))
        } else {
            val time = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(last))
            val count = prefs.getInt(ButtonService.KEY_COUNT, 0)
            val fg = prefs.getString(ButtonService.KEY_LAST_FOREGROUND, "?")
            detectStatus.text = "신고 패턴 인식됨\n$time  (누적 ${count}회)\n당시 화면: $fg"
            detectStatus.setTextColor(Color.WHITE)
            detectStatus.setBackgroundColor(Color.rgb(200, 0, 0))
        }

        val lines = EventLog.readLast(this, 1000).lines()
            .filter { it.contains("| KEY ") || it.contains("| DETECT ") || it.contains("| A11Y ") || it.contains("| OVERLAY ") || it.contains("표시 끄기") || it.contains("| BOOT ") }
            .takeLast(60)
        logView.text = if (lines.isEmpty()) "(버튼 이벤트 없음)" else lines.joinToString("\n")
        scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}
