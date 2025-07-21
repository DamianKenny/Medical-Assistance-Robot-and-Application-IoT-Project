package com.nibm.healthassistance;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.nibm.healthassistance.WelcomeScreen;

public class NotificationHelper {

    private static final String CHANNEL_ID = "supplement_reminder";
    private static final String CHANNEL_NAME = "Supplement Reminders";
    private static final String CHANNEL_DESCRIPTION = "Notifications for supplement reminders";

    private Context context;
    private NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void showSupplementReminder(String supplementName, String time) {
        Intent intent = new Intent(context, WelcomeScreen.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Time to take your supplement!")
                .setContentText("Don't forget to take your " + supplementName + " at " + time)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }
}

/*
 * Usage in MainActivity:
 *
 * Add this to the notification button click listener:
 *
 * NotificationHelper notificationHelper = new NotificationHelper(this);
 * notificationHelper.showSupplementReminder("Omega 3", "9:00 AM");
 *
 * Don't forget to add notification permission in AndroidManifest.xml:
 * <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
 */