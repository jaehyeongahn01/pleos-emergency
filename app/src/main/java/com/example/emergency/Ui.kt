package com.example.emergency

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.Button
import android.widget.TextView

/** 차량 화면용 공통 스타일 (큰 글씨, 큰 터치 영역, 높은 대비). */
object Ui {
    val BG = Color.rgb(18, 18, 18)
    val SURFACE = Color.rgb(40, 40, 40)
    val TEXT = Color.WHITE
    val SUBTEXT = Color.rgb(190, 190, 190)
    val ACCENT = Color.rgb(33, 118, 255) // 인식 점과 같은 파란색
    val DANGER = Color.rgb(200, 0, 0)

    fun dp(context: Context, v: Int): Int = (v * context.resources.displayMetrics.density).toInt()

    fun rounded(color: Int, radiusPx: Float, strokeColor: Int? = null, strokePx: Int = 0) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radiusPx
            if (strokeColor != null) setStroke(strokePx, strokeColor)
        }

    fun title(context: Context, text: String, sizeSp: Float = 56f) = TextView(context).apply {
        this.text = text
        textSize = sizeSp
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(TEXT)
    }

    fun body(context: Context, text: String, sizeSp: Float = 36f) = TextView(context).apply {
        this.text = text
        textSize = sizeSp
        setTextColor(SUBTEXT)
        setLineSpacing(0f, 1.25f)
    }

    /** 큰 버튼. primary=true 면 파란색. */
    fun button(context: Context, label: String, primary: Boolean = false, onClick: () -> Unit) =
        Button(context).apply {
            text = label
            textSize = 34f
            isAllCaps = false
            setTextColor(TEXT)
            gravity = Gravity.CENTER
            minHeight = dp(context, 120)
            setPadding(dp(context, 24), 0, dp(context, 24), 0)
            background = rounded(if (primary) ACCENT else SURFACE, dp(context, 16).toFloat())
            setOnClickListener { onClick() }
        }
}
