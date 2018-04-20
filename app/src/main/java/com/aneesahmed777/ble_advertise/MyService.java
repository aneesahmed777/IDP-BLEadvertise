package com.aneesahmed777.ble_advertise;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class MyService extends Service {

    private static final String DEFAULT_CHANNEL_ID = "channel";
    private static final int NOTIFICATION_ID = 123;

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private LocalBroadcastManager localBroadcastManager;
    private boolean working;
    private String data;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MyService", "onCreate()");

        working = false;
        data = null;

        notificationBuilder = new NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "default";
            String description = "default";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(DEFAULT_CHANNEL_ID, name, importance);
            notificationChannel.setDescription(description);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MyService", "onStartCommand()");

        Intent statusIntent = new Intent(getString(R.string.action_broadcast_status));

        Integer commandResId = intent.getIntExtra(getString(R.string.key_command_intent_service), 0);

        switch (commandResId) {
            case R.string.val_start_intent_service: startAdvertising(intent, statusIntent); break;
            case R.string.val_stop_intent_service: stopAdvertising(statusIntent); break;
            case R.string.val_info_intent_service: reportAdvertisingInfo(statusIntent); break;
        }

        localBroadcastManager.sendBroadcast(statusIntent);

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MyService", "onDestroy()");
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void startAdvertising(Intent intent, Intent statusIntent) {
        Log.d("MyService", "startAdvertising()");

        if (working) {
            statusIntent.putExtra(getString(R.string.key_status_action_broadcast_status),
                    R.string.val_busy_action_broadcast_status);

            return;
        }

        data = intent.getStringExtra(getString(R.string.key_data_intent_service));

        Intent targetIntent = new Intent(this, MainActivity.class);
        PendingIntent  pendingIntent = PendingIntent.getActivity(this, 0, targetIntent, 0);
        Notification notification = notificationBuilder
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.advertisement_active))
                .setContentText(data)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
        startForeground(NOTIFICATION_ID, notification);

        working = true;
        Boolean success = true;

        if (success) {
            statusIntent.putExtra(getString(R.string.key_status_action_broadcast_status),
                    R.string.val_start_ok_action_broadcast_status);
        }
        else {
            statusIntent.putExtra(getString(R.string.key_status_action_broadcast_status),
                    R.string.val_start_fail_action_broadcast_status);

            stopSelf();
        }
    }

    private void stopAdvertising(Intent statusIntent) {
        Log.d("MyService", "stopAdvertising()");

        Boolean success = false;
        if (working) {
            notificationManager.cancel(NOTIFICATION_ID);

            working = false;
            success = true;
        }

        if (success) {
            statusIntent.putExtra(getString(R.string.key_status_action_broadcast_status),
                    R.string.val_stop_ok_action_broadcast_status);
        }
        else {
            statusIntent.putExtra(getString(R.string.key_status_action_broadcast_status),
                    R.string.val_stop_fail_action_broadcast_status);
        }

        stopSelf();
    }

    private void reportAdvertisingInfo(Intent statusIntent) {
        Log.d("MyService", "reportAdvertisingInfo()");

        if (working) {
            statusIntent.putExtra(getString(R.string.key_status_action_broadcast_status),
                    R.string.val_busy_action_broadcast_status);
            statusIntent.putExtra(getString(R.string.key_data_action_broadcast_status), data);
        } else {
            statusIntent.putExtra(getString(R.string.key_status_action_broadcast_status),
                    R.string.val_idle_action_broadcast_status);
        }

        if (!working)
            stopSelf();
    }
}
