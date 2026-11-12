package com.example.emergency

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** 부팅/업데이트 인텐트를 받으면 ProbeService(foreground service)를 시작한다. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        EventLog.add(context, "RECEIVER ${intent.action}")
        try {
            context.startForegroundService(Intent(context, ProbeService::class.java))
            EventLog.add(context, "RECEIVER startForegroundService 호출 성공")
        } catch (e: Exception) {
            EventLog.add(context, "RECEIVER 서비스 시작 실패: ${e.javaClass.simpleName}: ${e.message}")
        }
    }
}
