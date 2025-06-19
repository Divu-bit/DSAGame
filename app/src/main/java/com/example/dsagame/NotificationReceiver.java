package com.example.dsagame;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_challenge")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Daily DSA Challenge")
                .setContentText("Solve today's challenge to keep your streak alive!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(100, builder.build());
    }
}