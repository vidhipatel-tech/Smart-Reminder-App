package com.example.smartreminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class ReminderBroadcast extends BroadcastReceiver {

    private static final String CHANNEL_ID = "smart_reminder_channel";
    private static final String TAG = "ReminderBroadcast";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        int taskId = intent.getIntExtra("taskId", -1);
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        String date = intent.getStringExtra("date"); // Get date extra
        String time = intent.getStringExtra("time"); // Get time extra

        Log.d(TAG, "onReceive triggered for taskId: " + taskId);

        if (taskId == -1 || title == null) return;

        createNotificationChannel(context);

        // Intent to open ReminderActivity
        Intent activityIntent = new Intent(context, ReminderActivity.class);
        activityIntent.putExtra("taskId", taskId);
        activityIntent.putExtra("title", title);
        activityIntent.putExtra("description", description);
        activityIntent.putExtra("datetime", date + " " + time); // Correctly pass combined datetime
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent activityPendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            activityPendingIntent = PendingIntent.getActivity(context, taskId, activityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            activityPendingIntent = PendingIntent.getActivity(context, taskId, activityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        // Use custom sound from res/raw
        Uri customSoundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.alarm_sound);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(description != null && !description.isEmpty() ? description : "Task Reminder")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(activityPendingIntent)
                .setAutoCancel(true)
                .setSound(customSoundUri) // Use the custom sound
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(activityPendingIntent, true); // this forces the full-screen activity

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(taskId, builder.build());
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Smart Reminder Channel";
            String description = "Channel for task reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Set custom sound for the channel
            Uri customSoundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.alarm_sound);
            channel.setSound(customSoundUri, null);

            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }
}