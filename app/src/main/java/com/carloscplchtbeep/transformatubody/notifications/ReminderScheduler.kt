package com.carloscplchtbeep.transformatubody.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDateTime
import java.time.ZoneId

class ReminderScheduler(private val context: Context) {
    fun scheduleDaily(hour: Int, minute: Int) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).putExtra("message", "Entrenamiento del dia listo para consultar.")
        val pending = PendingIntent.getBroadcast(context, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val now = LocalDateTime.now()
        var trigger = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!trigger.isAfter(now)) trigger = trigger.plusDays(1)
        alarm.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            AlarmManager.INTERVAL_DAY,
            pending
        )
    }

    fun cancel() {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(context, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarm.cancel(pending)
    }
}

