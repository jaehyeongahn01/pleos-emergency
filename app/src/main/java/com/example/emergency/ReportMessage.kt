package com.example.emergency

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 신고 메시지 설정(발신/수신 번호, 차량 정보, 문구)과 메시지 생성.
 * 설정은 기기 안 SharedPreferences("report")에만 저장되며 저장소에는 올라가지 않는다.
 *
 * 문구에 쓸 수 있는 자리표시자:
 *   {시각} 신고 시각, {위치} 위치 정보, {차량} 차량 정보
 */
object ReportMessage {
    private const val PREFS = "report"
    private const val KEY_SENDER = "sender"
    private const val KEY_RECEIVERS = "receivers"
    private const val KEY_VEHICLE = "vehicle"
    private const val KEY_TEMPLATE = "template"
    const val KEY_LAST_MESSAGE = "last_message"

    const val DEFAULT_TEMPLATE =
        "[긴급] 택시 기사 위급 상황입니다.\n" +
            "시각: {시각}\n" +
            "위치: {위치}\n" +
            "차량: {차량}\n" +
            "연락이 어려울 수 있으니 전화 대신 112에 신고해 주세요."

    /** 위치 연동 전까지 쓰는 문구 */
    const val LOCATION_PENDING = "(위치 연동 예정)"

    data class Settings(
        val sender: String,
        val receivers: List<String>,
        val vehicle: String,
        val template: String,
    )

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(context: Context): Settings {
        val p = prefs(context)
        return Settings(
            sender = p.getString(KEY_SENDER, "").orEmpty(),
            receivers = parseNumbers(p.getString(KEY_RECEIVERS, "").orEmpty()),
            vehicle = p.getString(KEY_VEHICLE, "").orEmpty(),
            template = p.getString(KEY_TEMPLATE, null) ?: DEFAULT_TEMPLATE,
        )
    }

    fun save(context: Context, s: Settings) {
        prefs(context).edit()
            .putString(KEY_SENDER, s.sender.trim())
            .putString(KEY_RECEIVERS, s.receivers.joinToString(", "))
            .putString(KEY_VEHICLE, s.vehicle.trim())
            .putString(KEY_TEMPLATE, s.template)
            .apply()
    }

    /** "010-1111-2222, 010 3333 4444" → ["010-1111-2222", "010 3333 4444"] */
    fun parseNumbers(text: String): List<String> =
        text.split(',', '\n', ';').map { it.trim() }.filter { it.isNotEmpty() }

    /** 설정한 문구에 자리표시자를 채워 실제 메시지를 만든다. */
    fun build(s: Settings, timeMillis: Long, location: String = LOCATION_PENDING): String {
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date(timeMillis))
        return s.template
            .replace("{시각}", time)
            .replace("{위치}", location)
            .replace("{차량}", s.vehicle.ifBlank { "(차량 정보 미입력)" })
    }

    /** 감지되었을 때 호출. 메시지를 만들어 기록한다. (실제 발송은 아직 없음) */
    fun onDetected(context: Context, timeMillis: Long) {
        val s = load(context)
        val message = build(s, timeMillis)
        prefs(context).edit().putString(KEY_LAST_MESSAGE, message).apply()
        val to = if (s.receivers.isEmpty()) "수신 번호 없음" else "수신 ${s.receivers.size}명"
        EventLog.add(context, "REPORT 메시지 생성 ($to, 발송은 미구현): ${message.lineSequence().first()}")
    }

    fun lastMessage(context: Context): String? = prefs(context).getString(KEY_LAST_MESSAGE, null)
}
