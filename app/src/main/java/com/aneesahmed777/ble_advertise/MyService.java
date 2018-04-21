package com.aneesahmed777.ble_advertise;

import android.app.*;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.nio.charset.Charset;
import java.util.UUID;

public class MyService extends Service {

    private static final String DEFAULT_CHANNEL_ID = "channel";
    private static final int NOTIFICATION_ID = 123;

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private LocalBroadcastManager localBroadcastManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeAdvertiser advertiser;
    private AdvertiseCallback advertisingCallback;
    Intent statusIntent;
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

        bluetoothAdapter = null;
        advertiser = null;
        advertisingCallback = null;
        statusIntent = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MyService", "onStartCommand()");

        statusIntent = new Intent(getString(R.string.action_broadcast_status));

        Integer resIdCommand = intent.getIntExtra(getString(R.string.key_command_intent_service), 0);

        switch (resIdCommand) {
            case R.string.val_start_intent_service: startAdvertising_part1(intent); break;
            case R.string.val_stop_intent_service: stopAdvertising(); break;
            case R.string.val_info_intent_service: reportAdvertisingInfo(); break;
        }

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

    private void startAdvertising_part1(Intent intent) {
        Log.d("MyService", "startAdvertising_part1()");

        if (working) {
            statusIntent.putExtra(getString(R.string.key_status_action_broadcast_status),
                    R.string.val_busy_action_broadcast_status);
            localBroadcastManager.sendBroadcast(statusIntent);
            statusIntent = null;

            return;
        }

        data = intent.getStringExtra(getString(R.string.key_data_intent_service));

        Boolean success = false;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
                if (advertiser != null) {
                    success = true;
                }
            }
        }

        if (!success) {
            statusIntent.putExtra(getString(R.string.key_status_action_broadcast_status),
                    R.string.val_start_fail_action_broadcast_status);
            localBroadcastManager.sendBroadcast(statusIntent);
            statusIntent = null;

            stopSelf();
            return;
        }

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .build();

        ParcelUuid pUuid = new ParcelUuid(UUID.fromString("821adb04-1288-4dde-8587-adfe0ecf4961"));

        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceData(pUuid, "Hello!!!".getBytes(Charset.forName("UTF-8")))
                .build();

        advertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                Log.d("BLE", "onStartSuccess()");
                startAdvertising_part2();
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.e( "BLE", "onStartFailure: " + errorCode );
                super.onStartFailure(errorCode);
                statusIntent.putExtra(getString(R.string.key_status_action_broadcast_status),
                        R.string.val_start_fail_action_broadcast_status);
                localBroadcastManager.sendBroadcast(statusIntent);
                statusIntent = null;

                stopSelf();
            }
        };

        advertiser.startAdvertising(settings, advertiseData, advertisingCallback);
    }

    private void startAdvertising_part2() {
        Log.d("MyService", "startAdvertising_part2()");

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

        statusIntent.putExtra(getString(R.string.key_status_action_broadcast_status),
                R.string.val_start_ok_action_broadcast_status);
        localBroadcastManager.sendBroadcast(statusIntent);
        statusIntent = null;
    }

    private void stopAdvertising() {
        Log.d("MyService", "stopAdvertising()");

        if (working) {
            notificationManager.cancel(NOTIFICATION_ID);
            advertiser.stopAdvertising(advertisingCallback);

            working = false;
        }

        statusIntent.putExtra(getString(R.string.key_status_action_broadcast_status),
                    R.string.val_stop_ok_action_broadcast_status);
        localBroadcastManager.sendBroadcast(statusIntent);
        statusIntent = null;

        stopSelf();
    }

    private void reportAdvertisingInfo() {
        Log.d("MyService", "reportAdvertisingInfo()");

        if (working) {
            statusIntent.putExtra(getString(R.string.key_status_action_broadcast_status),
                    R.string.val_busy_action_broadcast_status);
            statusIntent.putExtra(getString(R.string.key_data_action_broadcast_status), data);
        } else {
            statusIntent.putExtra(getString(R.string.key_status_action_broadcast_status),
                    R.string.val_idle_action_broadcast_status);
        }
        localBroadcastManager.sendBroadcast(statusIntent);
        statusIntent = null;

        if (!working)
            stopSelf();
    }
}
