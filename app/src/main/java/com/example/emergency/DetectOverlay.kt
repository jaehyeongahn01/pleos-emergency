package com.example.emergency

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager

/**
 * 신고 패턴이 인식되면 운전석 쪽 주행 정보 영역 맨 윗줄에 점을 띄운다.
 * - 운전자가 앱에서 "인식 표시 끄기"를 누르거나, 재부팅(시동 OFF/ON)할 때까지 떠 있다.
 * - 접근성 서비스 전용 오버레이(TYPE_ACCESSIBILITY_OVERLAY)라 추가 권한이 필요 없다.
 * - 터치/포커스를 받지 않으므로 아래 앱(내비 등) 조작에 영향이 없다.
 */
class DetectOverlay(private val service: AccessibilityService) {

    private val wm = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var view: View? = null

    val isShowing: Boolean get() = view != null

    fun show() {
        if (view != null) return
        val density = service.resources.displayMetrics.density
        val sizePx = (DOT_DP * density).toInt()
        val dot = DotView(service)
        val params = WindowManager.LayoutParams(
            sizePx, sizePx,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (MARGIN_X_DP * density).toInt()
            y = (MARGIN_Y_DP * density).toInt()
            title = "EmergencyDetectDot"
        }
        try {
            wm.addView(dot, params)
            view = dot
            EventLog.add(service, "OVERLAY 표시 (${DOT_DP}dp, 끌 때까지 유지)")
        } catch (e: Exception) {
            EventLog.add(service, "OVERLAY 표시 실패: ${e.javaClass.simpleName}: ${e.message}")
        }
    }

    fun hide() {
        view?.let {
            runCatching { wm.removeView(it) }
            view = null
            EventLog.add(service, "OVERLAY 숨김")
        }
    }

    private class DotView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = DOT_COLOR }
        override fun onDraw(canvas: Canvas) {
            val r = minOf(width, height) / 2f
            canvas.drawCircle(width / 2f, height / 2f, r, paint)
        }
    }

    companion object {
        /**
         * 점 크기와 위치. 주행 정보 영역 맨 윗줄, 주행가능거리("500km")와 속도("0km/h") 사이.
         * Pleos 에뮬레이터(2560x1440, 160dpi → 1dp = 1px) 화면에서 맞춘 값이다.
         * 중심 = (294, 44), 지름 32
         */
        const val DOT_DP = 32
        /** 화면 왼쪽 위 기준 점의 왼쪽/위 위치 */
        const val MARGIN_X_DP = 278
        const val MARGIN_Y_DP = 28
        val DOT_COLOR = Color.rgb(33, 118, 255)
    }
}
