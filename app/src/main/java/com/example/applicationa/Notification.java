package com.example.applicationa;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;

// Class to manage notifications
public class Notification {

    void sendNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create a NotificationChannel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "default_channel_id";
            CharSequence channelName = "Default Channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        // Create a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default_channel_id")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Data Modified")
                .setContentText("Data in Application A has been modified.")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Show the notification
        notificationManager.notify(1, builder.build());
    }

}
