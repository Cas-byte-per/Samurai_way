package com.example.myapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotificationUtils {

    public static void scheduleNotification(Context context, Task task) {
        long triggerAt = computeTriggerTime(context, task);
        if (triggerAt <= 0) return;

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP,
                triggerAt,
                makePendingIntent(context, task));
    }

    public static void cancelNotification(Context context, Task task) {
        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                (int) task.getId(),
                new Intent(context, NotificationReceiver.class),
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (pi != null) {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.cancel(pi);
        }
    }

    /**
     * Парсит дату+время из задачи и возвращает millis для триггера
     * (ровно за 1 день до дедлайна).
     */
    private static long computeTriggerTime(Context context, Task task) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault());
        Date deadline;
        try {
            String dt = task.getDate() + " " +
                    (task.getTime() == null || task.getTime().isEmpty()
                            ? "00:00"
                            : task.getTime());
            deadline = sdf.parse(dt);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(deadline);
        cal.add(Calendar.DATE, -1);
        return cal.getTimeInMillis();
    }

    /**
     * Строит PendingIntent для NotificationReceiver,
     * кладёт в интент title, description и id задачи.
     */
    private static PendingIntent makePendingIntent(Context context, Task task) {
        Intent intent = new Intent(context, NotificationReceiver.class)
                .putExtra("title", task.getTitle())
                .putExtra("description", task.getDescription())
                .putExtra("id", (int) task.getId());

        return PendingIntent.getBroadcast(
                context,
                (int) task.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
