package com.example.emergency

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Handler
import android.os.IBinder
import android.os.Looper

/** 상주 여부를 확인하기 위한 foreground service. 10초마다 heartbeat를 기록한다. */
class ProbeService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var beat = 0
    private val heartbeat = object : Runnable {
        override fun run() {
            beat++
            EventLog.add(this@ProbeService, "SERVICE heartbeat #$beat")
            handler.postDelayed(this, HEARTBEAT_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        EventLog.add(this, "SERVICE onCreate")
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "BootProbe", NotificationManager.IMPORTANCE_MIN)
        )
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("실행 중")
            .build()
        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        EventLog.add(this, "SERVICE startForeground 성공")
        handler.post(heartbeat)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // intent == null 이면 시스템이 프로세스 종료 후 START_STICKY로 재시작한 것
        EventLog.add(this, "SERVICE onStartCommand intent=${if (intent == null) "null(재시작)" else "있음"}")
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        EventLog.add(this, "SERVICE onTaskRemoved")
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        handler.removeCallbacks(heartbeat)
        EventLog.add(this, "SERVICE onDestroy")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "bootprobe"
        private const val NOTIFICATION_ID = 1
        private const val HEARTBEAT_MS = 10_000L
    }
}
