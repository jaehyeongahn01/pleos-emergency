package com.example.emergency

import android.content.Context
import android.os.Process
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 부팅/서비스 이벤트를 파일과 logcat(tag=BootProbe)에 남긴다.
 * Direct Boot(잠금 해제 전)에서도 쓸 수 있도록 device-protected 저장소를 사용한다.
 */
object EventLog {
    const val TAG = "BootProbe"
    private const val FILE_NAME = "events.log"

    private fun file(context: Context): File {
        val ctx = context.createDeviceProtectedStorageContext()
        return File(ctx.filesDir, FILE_NAME)
    }

    @Synchronized
    fun add(context: Context, event: String) {
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val line = "$time | $event | pid=${Process.myPid()} user=${Process.myUserHandle()}"
        Log.i(TAG, line)
        runCatching { file(context).appendText(line + "\n") }
    }

    @Synchronized
    fun readLast(context: Context, maxLines: Int = 200): String {
        val f = file(context)
        if (!f.exists()) return "(로그 없음)"
        return f.readLines().takeLast(maxLines).joinToString("\n")
    }

    @Synchronized
    fun clear(context: Context) {
        runCatching { file(context).delete() }
    }
}
