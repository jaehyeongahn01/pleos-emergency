package com.example.emergency

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/**
 * 신고 메시지 설정 화면.
 * - 보내는 휴대폰 번호, 받는 휴대폰 번호(여러 개), 차량 정보, 메시지 문구
 * - 문구에는 {시각} {위치} {차량} 을 넣을 수 있고, 아래 미리보기로 실제 메시지를 확인한다.
 * - 키보드 입력이 필요하므로 정차 중에 사용하는 화면이다.
 */
class SettingsActivity : Activity() {

    private lateinit var sender: EditText
    private lateinit var receivers: EditText
    private lateinit var vehicle: EditText
    private lateinit var template: EditText
    private lateinit var preview: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val s = ReportMessage.load(this)
        val pad = Ui.dp(this, 64)

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }
        form.addView(Ui.title(this, "신고 메시지 설정"))
        form.addView(Ui.body(this, "위급 상황이 인식되면 아래 내용으로 메시지를 만듭니다. 정차 중에 설정해 주세요."),
            lp(top = 8))

        sender = field(form, "보내는 휴대폰 번호", "예: 010-0000-0000", s.sender, phone = true)
        receivers = field(form, "받는 휴대폰 번호 (여러 개는 쉼표로 구분)",
            "예: 010-1111-1111, 010-2222-2222", s.receivers.joinToString(", "), phone = true)
        vehicle = field(form, "차량 정보", "예: 서울 12가 3456 흰색", s.vehicle, phone = false)
        template = field(form, "메시지 문구  ({시각} {위치} {차량} 사용 가능)", "", s.template,
            phone = false, lines = 5)

        form.addView(Ui.body(this, "미리보기", 28f), lp(top = 16))
        preview = TextView(this).apply {
            textSize = 30f
            setTextColor(Ui.TEXT)
            setPadding(pad / 2, pad / 2, pad / 2, pad / 2)
            background = Ui.rounded(Ui.SURFACE, Ui.dp(this@SettingsActivity, 12).toFloat())
            setLineSpacing(0f, 1.2f)
        }
        form.addView(preview, lp(top = 8))

        val buttons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        buttons.addView(Ui.button(this, "저장", primary = true) { save() },
            lp(weight = 1f, right = 8))
        buttons.addView(Ui.button(this, "닫기") { finish() }, lp(weight = 1f, left = 8))
        form.addView(buttons, lp(top = 24))

        val watcher = object : TextWatcher {
            override fun afterTextChanged(e: Editable?) = updatePreview()
            override fun beforeTextChanged(c: CharSequence?, a: Int, b: Int, d: Int) {}
            override fun onTextChanged(c: CharSequence?, a: Int, b: Int, d: Int) {}
        }
        vehicle.addTextChangedListener(watcher)
        template.addTextChangedListener(watcher)
        updatePreview()

        val scroll = ScrollView(this).apply {
            setBackgroundColor(Ui.BG)
            addView(form)
        }
        setContentView(scroll)
    }

    private fun current() = ReportMessage.Settings(
        sender = sender.text.toString(),
        receivers = ReportMessage.parseNumbers(receivers.text.toString()),
        vehicle = vehicle.text.toString(),
        template = template.text.toString().ifBlank { ReportMessage.DEFAULT_TEMPLATE },
    )

    private fun updatePreview() {
        preview.text = ReportMessage.build(current(), System.currentTimeMillis())
    }

    private fun save() {
        val s = current()
        if (s.receivers.isEmpty()) {
            Toast.makeText(this, "받는 번호를 하나 이상 입력해 주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        ReportMessage.save(this, s)
        EventLog.add(this, "SETTINGS 저장 (수신 ${s.receivers.size}명)")
        Toast.makeText(this, "저장했습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }

    /** 라벨 + 입력칸을 form에 추가하고 EditText를 돌려준다. */
    private fun field(
        parent: LinearLayout, label: String, hint: String, value: String,
        phone: Boolean, lines: Int = 1,
    ): EditText {
        parent.addView(Ui.body(this, label, 28f), lp(top = 28))
        val edit = EditText(this).apply {
            setText(value)
            this.hint = hint
            textSize = 32f
            setTextColor(Ui.TEXT)
            setHintTextColor(Ui.SUBTEXT)
            setPadding(Ui.dp(this@SettingsActivity, 20), Ui.dp(this@SettingsActivity, 16),
                Ui.dp(this@SettingsActivity, 20), Ui.dp(this@SettingsActivity, 16))
            background = Ui.rounded(Ui.SURFACE, Ui.dp(this@SettingsActivity, 12).toFloat())
            inputType = when {
                phone -> InputType.TYPE_CLASS_PHONE
                lines > 1 -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                else -> InputType.TYPE_CLASS_TEXT
            }
            if (lines > 1) { setLines(lines); gravity = Gravity.TOP }
        }
        parent.addView(edit, lp(top = 8))
        return edit
    }

    private fun lp(
        weight: Float = 0f, top: Int = 0, left: Int = 0, right: Int = 0,
    ) = LinearLayout.LayoutParams(
        if (weight > 0) 0 else LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT, weight
    ).apply {
        setMargins(Ui.dp(this@SettingsActivity, left), Ui.dp(this@SettingsActivity, top),
            Ui.dp(this@SettingsActivity, right), 0)
    }
}
