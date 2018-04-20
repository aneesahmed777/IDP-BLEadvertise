package com.aneesahmed777.ble_advertise;

import android.content.*;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.support.design.widget.Snackbar;

import java.util.Calendar;

class MainBroadcastReceiver extends BroadcastReceiver {
    private static final MainBroadcastReceiver theBroadcastReceiver;

    private static Boolean getInstance_hasBeenCalledAtleastOnce = false;
    private MainActivity notifyActivity = null;

    static {
        theBroadcastReceiver = new MainBroadcastReceiver();
    }

    private MainBroadcastReceiver() {  // Prevents instantiation
        Log.d("MainBroadcastReceiver", "MainBroadcastReceiver()");
    }

    public static MainBroadcastReceiver getInstance() {
        Log.d("MainBroadcastReceiver", "getInstance()");
        getInstance_hasBeenCalledAtleastOnce = true;
        return theBroadcastReceiver;
    }

    public static Boolean getInstance_hasBeenCalledBefore() {
        return getInstance_hasBeenCalledAtleastOnce;
    }

    public void setNotifyActivity(MainActivity activity) {
        Log.d("MainBroadcastReceiver", "setNotifyActivity()");
        notifyActivity = activity;
    }

    public void onReceive(Context context, Intent intent) {
        Log.d("MainBroadcastReceiver", "onReceive()");

        if (notifyActivity != null)
            notifyActivity.onBroadcastReceive(intent);
    }
}

public class MainActivity extends AppCompatActivity {

    private static final int SNACKBAR_DURATION = 10000;  // 10 sec

    private ConstraintLayout layout;
    private TextView txtvInfo;
    private Button btnStart;
    private Button btnStop;
    private Boolean gettingCreated;
    private Boolean working;
    private String data;

    private class DoNothingOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // Do nothing.
        }
    }

    private class ExitActivityOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            finish();
        }
    }

    private class BtnStartListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startAdvertising();
        }
    }

    private class BtnStopListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            stopAdvertising();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Log.d("MainActivity", "onCreate()");
        }
        else {
            Log.d("MainActivity", "onCreate() with bundle");
        }

        setContentView(R.layout.activity_main);

        layout = findViewById(R.id.layout);
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
        txtvInfo = findViewById(R.id.txtv_info);

        btnStart.setOnClickListener(new BtnStartListener());
        btnStop.setOnClickListener(new BtnStopListener());

        txtvInfo.setText(R.string.initialising_activity);
        btnStart.setEnabled(false);
        btnStop.setEnabled(false);
        gettingCreated = true;
        working = false;
        data = null;

        if (!MainBroadcastReceiver.getInstance_hasBeenCalledBefore()) {
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
            IntentFilter statusIntentFilter = new IntentFilter(getString(R.string.action_broadcast_status));
            localBroadcastManager.registerReceiver(MainBroadcastReceiver.getInstance(), statusIntentFilter);
        }

        MainBroadcastReceiver.getInstance().setNotifyActivity(this);

        if (savedInstanceState != null) {
            loadStateFromBundle(savedInstanceState);
        } else {
            Context context = getApplicationContext();
            Intent intentGetInfoAboutService = new Intent(context, MyService.class)
                    .putExtra(getString(R.string.key_command_intent_service), R.string.val_info_intent_service);
            context.startService(intentGetInfoAboutService);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("MainActivity", "onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity", "onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("MainActivity", "onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MainActivity", "onDestroy()");
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.d("MainActivity", "onSaveInstanceState()");

        saveStateIntoBundle(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("MainActivity", "onRestoreInstanceState() with bundle");

        loadStateFromBundle(savedInstanceState);
    }

    private void saveStateIntoBundle(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(getString(R.string.key_gettingCreated_bundle_savedInstanceState), gettingCreated);
        savedInstanceState.putBoolean(getString(R.string.key_working_bundle_savedInstanceState), working);
        savedInstanceState.putString(getString(R.string.key_data_bundle_savedInstanceState), data);
        savedInstanceState.putCharSequence(getString(R.string.key_txtvw_info_bundle_savedInstanceState), txtvInfo.getText());
        Log.d("MainActivity", "saveStateIntoBundle: "+ gettingCreated +", "+working+", "+data);
    }

    private void loadStateFromBundle(Bundle savedInstanceState) {
        gettingCreated = savedInstanceState.getBoolean(getString(R.string.key_gettingCreated_bundle_savedInstanceState));
        working = savedInstanceState.getBoolean(getString(R.string.key_working_bundle_savedInstanceState));
        data = savedInstanceState.getString(getString(R.string.key_data_bundle_savedInstanceState));
        Log.d("MainActivity", "loadStateFromBundle: "+ gettingCreated +", "+working+", "+data);

        if (gettingCreated) {
            btnStart.setEnabled(false);
            btnStop.setEnabled(false);
        } else {
            btnStart.setEnabled(!working);
            btnStop.setEnabled(working);
        }

        txtvInfo.setText(savedInstanceState.getCharSequence(getString(R.string.key_txtvw_info_bundle_savedInstanceState)));
    }

    private void startAdvertising() {
        Log.d("MainActivity", "startAdvertising()");

        data = Calendar.getInstance().getTime().toString();

        Context context = getApplicationContext();
        Intent intentStartAdvertising = new Intent(context, MyService.class)
                .putExtra(getString(R.string.key_command_intent_service), R.string.val_start_intent_service)
                .putExtra(getString(R.string.key_data_intent_service), data);
        context.startService(intentStartAdvertising);

        txtvInfo.setText(R.string.starting_advertisement);
    }

    private void stopAdvertising() {
        Log.d("MainActivity", "stopAdvertising()");

        Context context = getApplicationContext();
        Intent intentStopAdvertising = new Intent(context, MyService.class)
                .putExtra(getString(R.string.key_command_intent_service), R.string.val_stop_intent_service);
        context.startService(intentStopAdvertising);

        txtvInfo.setText(R.string.stopping_advertisement);
    }

    public void onBroadcastReceive(Intent statusIntent) {
        Integer statusResId = statusIntent.getIntExtra(getString(R.string.key_status_action_broadcast_status), 0);
        String status = getString(statusResId);

        if (gettingCreated) {
            gettingCreated = false;
            data = statusIntent.getStringExtra(getString(R.string.key_data_action_broadcast_status));
            Log.d("MainActivity", "onBroadcastReceive: "+status+", "+data);
            switch (statusResId) {
                case R.string.val_busy_action_broadcast_status: {
                    btnStart.setEnabled(false);
                    btnStop.setEnabled(true);
                    txtvInfo.setText(data);
                    working = true;
                    break;
                }
                case R.string.val_idle_action_broadcast_status: {
                    btnStart.setEnabled(true);
                    btnStop.setEnabled(false);
                    txtvInfo.setText(R.string.idle);
                    working = false;
                    break;
                }
            }
        } else {
            Log.d("MainActivity", "onBroadcastReceive: "+status);
            switch (statusResId) {
                case R.string.val_start_ok_action_broadcast_status: {
                    btnStart.setEnabled(false);
                    btnStop.setEnabled(true);
                    txtvInfo.setText(data);
                    working = true;
                    String msg = getString(R.string.advertisement_started);
                    Snackbar snackbar = Snackbar.make(layout, msg, SNACKBAR_DURATION);
                    snackbar.setAction(android.R.string.ok, new DoNothingOnClick());
                    snackbar.show();
                    break;
                }
                case R.string.val_stop_ok_action_broadcast_status: {
                    btnStart.setEnabled(true);
                    btnStop.setEnabled(false);
                    txtvInfo.setText(R.string.idle);
                    data = null;
                    working = false;
                    String msg = getString(R.string.advertisement_stopped);
                    Snackbar snackbar = Snackbar.make(layout, msg, SNACKBAR_DURATION);
                    snackbar.setAction(android.R.string.ok, new DoNothingOnClick());
                    snackbar.show();
                    break;
                }
                case R.string.val_start_fail_action_broadcast_status: {
                    btnStart.setEnabled(true);
                    btnStop.setEnabled(false);
                    txtvInfo.setText(R.string.failed_to_start_advertisement);
                    data = null;
                    break;
                }
                case R.string.val_stop_fail_action_broadcast_status: {
                    btnStart.setEnabled(false);
                    btnStop.setEnabled(true);
                    txtvInfo.setText(R.string.failed_to_stop_advertisement);
                    break;
                }
            }
        }
    }
}
