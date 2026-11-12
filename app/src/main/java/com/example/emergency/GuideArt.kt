package com.example.emergency

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.View

/**
 * 사용법 안내에 쓰는 그림. 이미지 파일 없이 코드로 그린다.
 *  - DISPLAY: Pleos 화면 배치와 파란 점 위치
 *  - WHEEL:   핸들과 강조한 MUTE 버튼
 *  - HELP:    앱 화면 위쪽의 도움말 버튼
 */
class GuideArt(
    context: Context,
    private val kind: Kind,
    /** 안내 화면 폭에서 그림이 차지하는 비율 */
    private val widthFraction: Float = 0.6f,
) : View(context) {

    enum class Kind { DISPLAY_PLAIN, DISPLAY_DOT, WHEEL, HELP, OFF }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val d = context.resources.displayMetrics.density

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val w = (MeasureSpec.getSize(widthSpec) * widthFraction).toInt()
        // Pleos 화면 비율(16:9)로 높이를 정한다.
        val ratio = if (kind == Kind.WHEEL) 438f / 686f else 9f / 16f
        setMeasuredDimension(w, (w * ratio).toInt())
    }

    override fun onDraw(c: Canvas) {
        when (kind) {
            Kind.DISPLAY_PLAIN -> drawScreenshot(c, R.drawable.guide_screen_plain, withDot = false)
            Kind.DISPLAY_DOT -> drawScreenshot(c, R.drawable.guide_screen_dot, withDot = true)
            Kind.WHEEL -> drawWheel(c)
            Kind.HELP -> drawAppButtons(c, highlight = 0, caption = "사용법을 다시 보려면\n앱을 열고 '도움말'을 누르세요")
            Kind.OFF -> drawAppButtons(c, highlight = 2, caption = "파란 점을 끄려면\n앱을 열고 '인식 표시 끄기'를 누르세요")
        }
    }

    // ── Pleos 화면 (실제 에뮬레이터 캡처 위에 설명 표시) ─────────────────
    private fun drawScreenshot(c: Canvas, resId: Int, withDot: Boolean) {
        val w = width.toFloat()
        val h = height.toFloat()
        val bmp = bitmap(resId) ?: return

        // 모서리를 둥글게 자르고 캡처를 그린다.
        val clip = Path().apply { addRoundRect(RectF(0f, 0f, w, h), 14f * d, 14f * d, Path.Direction.CW) }
        c.save()
        c.clipPath(clip)
        c.drawBitmap(bmp, Rect(0, 0, bmp.width, bmp.height), RectF(0f, 0f, w, h), bitmapPaint)
        c.restore()

        // 실제 화면(2560x1440) 비율 기준 좌표
        fun x(px: Float) = w * px / 2560f
        fun y(py: Float) = h * py / 1440f

        if (withDot) {
            // 파란 점 위치 (중심 294, 44)
            val cx = x(294f)
            val cy = y(44f)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f * d
            paint.color = Color.rgb(255, 60, 60)
            c.drawCircle(cx, cy, h * 0.05f, paint)
            c.drawLine(cx + h * 0.035f, cy + h * 0.035f, x(560f), y(250f), paint)
            tag(c, "여기에 파란 점이 켜집니다", x(420f), y(250f), h * 0.05f)
        } else {
            tag(c, "① 주행 정보", x(200f), y(870f), h * 0.05f)
            tag(c, "② 앱 화면 (내비게이션·음악 등)", x(1150f), y(300f), h * 0.05f)
            tag(c, "③ 앱 바 (독)", x(1900f), y(1272f), h * 0.05f)
        }
    }

    private var cached: Pair<Int, Bitmap>? = null
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    private fun bitmap(resId: Int): Bitmap? {
        cached?.let { if (it.first == resId) return it.second }
        val bmp = BitmapFactory.decodeResource(resources, resId) ?: return null
        cached = resId to bmp
        return bmp
    }

    // ── 핸들 (사용자가 제공한 오디오 버튼 사진 위에 MUTE 강조) ─────────────
    private fun drawWheel(c: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val bmp = bitmap(R.drawable.guide_wheel) ?: return
        val clip = Path().apply { addRoundRect(RectF(0f, 0f, w, h), 14f * d, 14f * d, Path.Direction.CW) }
        c.save()
        c.clipPath(clip)
        c.drawBitmap(bmp, Rect(0, 0, bmp.width, bmp.height), RectF(0f, 0f, w, h), bitmapPaint)
        c.restore()

        // 사진(686x438) 좌표 기준. ③ 음소거 버튼 = (648, 277)
        fun x(px: Float) = w * px / 686f
        fun y(py: Float) = h * py / 438f
        val cx = x(642f)
        val cy = y(277f)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f * d
        paint.color = Color.rgb(255, 60, 60)
        c.drawCircle(cx, cy, h * 0.085f, paint)
        c.drawLine(cx, cy - h * 0.085f, x(560f), y(112f), paint)
        tag(c, "③ 음소거(MUTE) 버튼을", x(330f), y(58f), h * 0.07f)
        tag(c, "빠르게 4번 누르세요", x(330f), y(100f), h * 0.07f)
    }

    // ── 앱 화면의 버튼들 (도움말 / 인식 표시 끄기) ───────────────────
    private fun drawAppButtons(c: Canvas, highlight: Int, caption: String) {
        val w = width.toFloat()
        val h = height.toFloat()
        fill(c, RectF(0f, 0f, w, h), Ui.BG, 14f * d)
        text(c, "강도 몰래 신고", w * 0.05f, h * 0.20f, h * 0.075f, Color.WHITE, bold = true)
        val names = listOf("도움말", "신고 메시지 설정", "인식 표시 끄기")
        val widths = listOf(0.20f, 0.32f, 0.28f)
        var x = w * 0.05f
        names.forEachIndexed { i, n ->
            val bw = w * widths[i]
            val rect = RectF(x, h * 0.32f, x + bw, h * 0.52f)
            fill(c, rect, if (i == highlight) Ui.ACCENT else Ui.SURFACE, 10f * d)
            text(c, n, rect.left + bw * 0.1f, rect.centerY() + h * 0.022f, h * 0.055f, Color.WHITE)
            if (i == highlight) {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f * d
                paint.color = Color.rgb(255, 80, 80)
                c.drawRoundRect(RectF(rect.left - 8f * d, rect.top - 8f * d, rect.right + 8f * d, rect.bottom + 8f * d),
                    12f * d, 12f * d, paint)
            }
            x += bw + w * 0.03f
        }
        label(c, caption, w * 0.05f, h * 0.70f, h * 0.06f)
    }

    // ── 그리기 도우미 ─────────────────────────────────────────────
    private fun fill(c: Canvas, r: RectF, color: Int, radius: Float) {
        paint.style = Paint.Style.FILL
        paint.color = color
        if (radius > 0f) c.drawRoundRect(r, radius, radius, paint) else c.drawRect(r, paint)
    }

    private fun text(c: Canvas, s: String, x: Float, y: Float, size: Float, color: Int, bold: Boolean = false) {
        paint.style = Paint.Style.FILL
        paint.color = color
        paint.textSize = size
        paint.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        c.drawText(s, x, y, paint)
    }

    /** 사진 위에서도 잘 읽히도록 빨간 바탕에 흰 글씨를 쓴다. (x, y 는 글씨 왼쪽 아래 기준) */
    private fun tag(c: Canvas, s: String, x: Float, y: Float, size: Float) {
        paint.style = Paint.Style.FILL
        paint.textSize = size
        paint.typeface = Typeface.DEFAULT_BOLD
        val tw = paint.measureText(s)
        val padX = size * 0.45f
        val padY = size * 0.35f
        paint.color = Color.rgb(230, 40, 40)
        c.drawRoundRect(RectF(x - padX, y - size - padY * 0.2f, x + tw + padX, y + padY), 8f * d, 8f * d, paint)
        paint.color = Color.WHITE
        c.drawText(s, x, y, paint)
    }

    /** 줄바꿈이 있는 빨간 설명 글씨 */
    private fun label(c: Canvas, s: String, x: Float, y: Float, size: Float) {
        s.split("\n").forEachIndexed { i, line ->
            text(c, line, x, y + i * size * 1.3f, size, Color.rgb(255, 80, 80), bold = true)
        }
    }
}
