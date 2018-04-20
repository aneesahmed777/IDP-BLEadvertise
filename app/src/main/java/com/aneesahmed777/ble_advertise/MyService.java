package com.aneesahmed777.ble_advertise;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
        if (working)
            Log.d("MyService", "onStartCommand() already working");
        else
            Log.d("MyService", "onStartCommand()");

        if (working) {
            Intent statusIntent = new Intent(getString(R.string.action_broadcast_status));
            statusIntent.putExtra(getString(R.string.key_status_action_broadcast_status), getString(R.string.val_busy_action_broadcast_status));
            localBroadcastManager.sendBroadcast(statusIntent);
            return Service.START_STICKY;
        }

        data = intent.getStringExtra(getString(R.string.key_data_intent_launch_MyService));

        Intent targetIntent = new Intent(this, MainActivity.class);
        PendingIntent  pendingIntent = PendingIntent.getActivity(this, 0, targetIntent, 0);
        Notification notification = notificationBuilder
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.advertising_active))
                .setContentText(data)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
        startForeground(NOTIFICATION_ID, notification);

        working = true;

        Context context = getApplicationContext();
        String sharedPrefFilename = getString(R.string.file_sharedpref_ble_advertising_info);
        SharedPreferences sharedPref = context.getSharedPreferences(sharedPrefFilename, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.key_working_sharedpref_ble_advertising_info), working);
        editor.putString(getString(R.string.key_data_sharedpref_ble_advertising_info), data);
        Boolean success = editor.commit();

        if (success) {
            Intent statusIntent = new Intent(getString(R.string.action_broadcast_status));
            statusIntent.putExtra(getString(R.string.key_status_action_broadcast_status),
                    getString(R.string.val_ok_action_broadcast_status));
            localBroadcastManager.sendBroadcast(statusIntent);
            return Service.START_STICKY;
        }
        else {
            Intent statusIntent = new Intent(getString(R.string.action_broadcast_status));
            statusIntent.putExtra(getString(R.string.key_status_action_broadcast_status),
                    getString(R.string.val_fail_action_broadcast_status));
            localBroadcastManager.sendBroadcast(statusIntent);
            stopSelf();
            return Service.START_NOT_STICKY;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MyService", "onDestroy()");

        if (working) {
            notificationManager.cancel(NOTIFICATION_ID);
            Context context = getApplicationContext();
            String sharedPrefFilename = getString(R.string.file_sharedpref_ble_advertising_info);
            SharedPreferences sharedPref = context.getSharedPreferences(sharedPrefFilename, Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            Boolean success = editor.commit();

            Intent statusIntent = new Intent(getString(R.string.action_broadcast_status));
            if (success)
                statusIntent.putExtra(getString(R.string.key_status_action_broadcast_status),
                        getString(R.string.val_ok_action_broadcast_status));
            else
                statusIntent.putExtra(getString(R.string.key_status_action_broadcast_status),
                        getString(R.string.val_fail_action_broadcast_status));
            localBroadcastManager.sendBroadcast(statusIntent);

            working = false;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
