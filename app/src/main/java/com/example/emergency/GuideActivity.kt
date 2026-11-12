package com.example.emergency

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * 사용법 안내. 앱을 처음 열 때 자동으로 보여 주고, 이후에는 메인 화면의 '도움말'로 다시 볼 수 있다.
 * 쪽 순서: 소개 → 화면 예시 → 핸들 버튼 → 인식 후 → 끄는 법 → 도움말 안내
 */
class GuideActivity : Activity() {

    private class Page(
        val title: String,
        val art: GuideArt.Kind?,
        val text: String,
        val extra: (() -> String)? = null,
        val artFraction: Float = 0.6f,
    )

    private lateinit var pages: List<Page>
    private var index = 0

    private lateinit var content: LinearLayout
    private lateinit var progress: TextView
    private lateinit var prev: Button
    private lateinit var next: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pages = buildPages()
        index = savedInstanceState?.getInt(KEY_INDEX) ?: 0

        val pad = Ui.dp(this, 64)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Ui.BG)
            setPadding(pad, pad / 2, pad, pad / 2)
        }

        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(content) }
        root.addView(scroll, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        prev = Ui.button(this, "이전") { go(index - 1) }
        next = Ui.button(this, "다음", primary = true) {
            if (index == pages.lastIndex) finish() else go(index + 1)
        }
        progress = TextView(this).apply {
            textSize = 28f
            setTextColor(Ui.SUBTEXT)
            gravity = Gravity.CENTER
        }
        bar.addView(prev, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        bar.addView(progress, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        bar.addView(next, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(bar, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = Ui.dp(this@GuideActivity, 12)
        })

        setContentView(root)
        show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_INDEX, index)
    }

    /** 끝까지 보거나 닫으면 '안내를 본 것'으로 기록한다. */
    override fun onStop() {
        markSeen(this)
        super.onStop()
    }

    private fun go(i: Int) {
        index = i.coerceIn(0, pages.lastIndex)
        show()
    }

    private fun show() {
        val p = pages[index]
        val wrap = LinearLayout.LayoutParams.WRAP_CONTENT
        val match = LinearLayout.LayoutParams.MATCH_PARENT
        val gap = Ui.dp(this, 16)
        content.removeAllViews()
        content.addView(Ui.title(this, p.title))

        val extraText = p.extra?.invoke()
        if (p.art != null && extraText != null) {
            // 그림(왼쪽)과 예시 메시지(오른쪽)를 나란히 놓아 한 화면에 다 보이게 한다.
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            row.addView(GuideArt(this, p.art, 1f), LinearLayout.LayoutParams(0, wrap, 1f))
            row.addView(extraBox(extraText), LinearLayout.LayoutParams(0, wrap, 1f).apply {
                leftMargin = gap * 2
                gravity = Gravity.TOP
            })
            content.addView(row, LinearLayout.LayoutParams(match, wrap).apply { topMargin = gap })
            content.addView(Ui.body(this, p.text), LinearLayout.LayoutParams(match, wrap).apply { topMargin = gap })
        } else {
            if (p.art != null) {
                content.addView(GuideArt(this, p.art, p.artFraction), LinearLayout.LayoutParams(match, wrap).apply {
                    topMargin = gap
                    gravity = Gravity.CENTER_HORIZONTAL
                })
            }
            content.addView(Ui.body(this, p.text), LinearLayout.LayoutParams(match, wrap).apply { topMargin = gap })
            extraText?.let { content.addView(extraBox(it), LinearLayout.LayoutParams(match, wrap).apply { topMargin = gap }) }
        }
        progress.text = "${index + 1} / ${pages.size}"
        prev.visibility = if (index == 0) View.INVISIBLE else View.VISIBLE
        next.text = if (index == pages.lastIndex) "시작하기" else "다음"
    }

    /** 신고 메시지 예시 같은 박스 */
    private fun extraBox(text: String) = TextView(this).apply {
        this.text = text
        textSize = 28f
        setTextColor(Ui.TEXT)
        setLineSpacing(0f, 1.2f)
        setPadding(Ui.dp(this@GuideActivity, 20), Ui.dp(this@GuideActivity, 16),
            Ui.dp(this@GuideActivity, 20), Ui.dp(this@GuideActivity, 16))
        background = Ui.rounded(Ui.SURFACE, Ui.dp(this@GuideActivity, 12).toFloat())
    }

    private fun buildPages(): List<Page> {
        val count = ButtonService.PRESS_COUNT
        return listOf(
            Page(
                "강도 몰래 신고",
                null,
                "위협을 받아 휴대폰이나 화면을 쓸 수 없을 때, 핸들의 버튼만 눌러 조용히 도움을 요청하는 앱입니다.\n\n" +
                    "처음 한 번만 설정하고 나면, 앱을 열어 두지 않아도 내비게이션이나 음악 앱을 쓰는 중에 동작합니다."
            ),
            Page(
                "1. 화면은 이렇게 생겼습니다",
                GuideArt.Kind.DISPLAY_PLAIN,
                "왼쪽은 속도와 주행거리가 보이는 주행 정보 영역, 오른쪽은 내비게이션 같은 앱 화면, 아래는 앱 바(독)입니다.\n" +
                    "아무 앱을 쓰고 있어도 괜찮습니다.",
                artFraction = 0.72f,
            ),
            Page(
                "2. 핸들 버튼을 누릅니다",
                GuideArt.Kind.WHEEL,
                "핸들의 음소거(MUTE) 버튼을 ${count}번 연속으로 빠르게 누르세요. " +
                    "누르는 사이가 1.5초 이상 벌어지면 처음부터 다시 셉니다.\n\n" +
                    "평소 소리를 끄고 켜는 동작과 같아서 옆 사람에게 특별해 보이지 않습니다.\n" +
                    "(그림은 예시이며 버튼 모양과 위치는 차종마다 다릅니다.)",
                artFraction = 0.48f,
            ),
            Page(
                "3. 인식되면",
                GuideArt.Kind.DISPLAY_DOT,
                (if (SEND_IMPLEMENTED)
                    "화면 왼쪽 위, 주행거리와 속도 사이에 파란 점이 켜지고 설정한 번호로 신고 메시지가 자동으로 전송됩니다."
                else
                    "화면 왼쪽 위, 주행거리와 속도 사이에 파란 점이 켜지고 신고 메시지가 준비됩니다.\n" +
                        "(현재 버전은 메시지 전송 기능을 준비 중입니다.)") +
                    "\n소리나 팝업은 나오지 않습니다. 오른쪽은 신고 메시지 예시입니다.",
                extra = { ReportMessage.build(ReportMessage.load(this), System.currentTimeMillis()) },
            ),
            Page(
                "4. 파란 점을 끄려면",
                GuideArt.Kind.OFF,
                "• 앱을 열고 '인식 표시 끄기'를 누르세요.\n" +
                    "• 시동을 껐다 켜면 자동으로 꺼집니다.\n\n" +
                    "파란 점은 직접 끄기 전까지 계속 켜져 있습니다."
            ),
            Page(
                "5. 다시 보고 싶으면",
                GuideArt.Kind.HELP,
                "이 설명은 언제든 다시 볼 수 있습니다. 앱을 열고 '도움말' 버튼을 누르세요.\n\n" +
                    "신고 메시지와 받는 번호는 '신고 메시지 설정'에서 바꿀 수 있습니다."
            ),
        )
    }

    companion object {
        private const val KEY_INDEX = "page"
        private const val PREFS = "app"
        private const val KEY_SEEN = "guide_seen"

        /** 신고 메시지를 실제로 전송하는 기능이 들어가면 true 로 바꾼다. 안내 문구가 함께 바뀐다. */
        const val SEND_IMPLEMENTED = false

        fun isSeen(context: Context): Boolean =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_SEEN, false)

        private fun markSeen(context: Context) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_SEEN, true).apply()
        }
    }
}
