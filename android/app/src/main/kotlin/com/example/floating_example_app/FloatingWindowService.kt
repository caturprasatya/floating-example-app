package com.example.floating_example_app

import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat

class FloatingWindowService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var usageStatsManager: UsageStatsManager
    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 1000L // Check every second

    private val targetPackages = listOf(
        "us.zoom.videomeetings",
        "com.google.android.apps.meetings",
        "com.microsoft.teams"
    )

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        createFloatingWindow()
        startForeground(NOTIFICATION_ID, createNotification())
        startUsageTracking()
    }

    private fun createFloatingWindow() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_window_layout, null)
        windowManager.addView(floatingView, params)
        floatingView.visibility = View.GONE
    }

    private fun createNotification(): Notification {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("floating_service", "Floating Window Service")
        } else {
            ""
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Floating Window Service")
            .setContentText("Monitoring for video conferencing apps")
            .setSmallIcon(R.drawable.ic_notification)
            .build()
    }

    private fun createNotificationChannel(channelId: String, channelName: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        return channelId
    }

    private fun startUsageTracking() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                checkCurrentApp()
                handler.postDelayed(this, checkInterval)
            }
        }, checkInterval)
    }

    private fun checkCurrentApp() {
        val time = System.currentTimeMillis()
        val usageEvents = usageStatsManager.queryEvents(time - 1000, time)
        val event = UsageEvents.Event()
        var targetAppInForeground = false

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND && targetPackages.contains(event.packageName)) {
                targetAppInForeground = true
                break
            }
        }

        floatingView.visibility = if (targetAppInForeground) View.VISIBLE else View.GONE
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(floatingView)
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}

