package io.github.tyomitch.panel;


import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.util.Pair;


public class NotificationUtils {

    public static final int NOTIFICATION_ID = 1337;
    private static final String NOTIFICATION_CHANNEL_ID = "io.github.tyomitch.panel.app";
    private static final String NOTIFICATION_CHANNEL_NAME = "io.github.tyomitch.panel.app";

    public static Pair<Integer, Notification> getNotification(@NonNull Context context) {
        createNotificationChannel(context);
        Notification notification = createNotification(context);
        NotificationManager notificationManager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
        return new Pair<>(NOTIFICATION_ID, notification);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static void createNotificationChannel(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
    }

    private static Notification createNotification(@NonNull Context context) {
        Intent notifIntent = new Intent(context.getApplicationContext(), ScreenCaptureActivity.class);
        notifIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_camera);
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setContentText(context.getString(R.string.recording));
        builder.setOngoing(true);
        builder.setCategory(Notification.CATEGORY_SERVICE);
        builder.setPriority(Notification.PRIORITY_LOW);
        builder.setShowWhen(true);
        builder.setContentIntent(PendingIntent.getActivity(context.getApplicationContext(), 0, notifIntent, 0));
        return builder.build();
    }

}
