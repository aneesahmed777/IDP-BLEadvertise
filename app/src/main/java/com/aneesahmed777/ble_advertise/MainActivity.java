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

final class MainBroadcastReceiver extends BroadcastReceiver {
    private static final MainBroadcastReceiver theBroadcastReceiver;

    private static Boolean getInstance_hasBeenCalledAtleastOnce = false;

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

    public void onReceive(Context context, Intent intent) {
        String status = intent.getStringExtra(context.getString(R.string.key_status_action_broadcast_status));
        Log.d("MainBroadcastReceiver", "onReceive: "+status);
    }
}

public class MainActivity extends AppCompatActivity {

    private static final int SNACKBAR_DURATION = 10000;  // 10 sec

    private ConstraintLayout layout;
    private TextView txtvInfo;
    private Button btnStart;
    private Button btnStop;
    private boolean working;
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

        txtvInfo.setText("");
        btnStop.setEnabled(false);
        working = false;
        data = null;

        if (savedInstanceState != null)
            loadStateFromBundle(savedInstanceState);
        else {
            Context context = getApplicationContext();
            String sharedPrefFilename = getString(R.string.file_sharedpref_ble_advertising_info);
            SharedPreferences sharedPref = context.getSharedPreferences(sharedPrefFilename, Context.MODE_PRIVATE);
            working = sharedPref.getBoolean(getString(R.string.key_working_sharedpref_ble_advertising_info), false);
            data = sharedPref.getString(getString(R.string.key_data_sharedpref_ble_advertising_info), null);

            Log.d("MainActivity", "onCreate: load from sharedPref: "+working+", "+data);

            btnStart.setEnabled(!working);
            btnStop.setEnabled(working);
            txtvInfo.setText(data);
        }

        if (!MainBroadcastReceiver.getInstance_hasBeenCalledBefore()) {
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
            IntentFilter statusIntentFilter = new IntentFilter(getString(R.string.action_broadcast_status));
            localBroadcastManager.registerReceiver(MainBroadcastReceiver.getInstance(), statusIntentFilter);
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
        savedInstanceState.putBoolean(getString(R.string.key_working_bundle_savedInstanceState), working);
        savedInstanceState.putString(getString(R.string.key_data_bundle_savedInstanceState), data);
        Log.d("MainActivity", "saveStateIntoBundle: "+working+", "+data);
    }

    private void loadStateFromBundle(Bundle savedInstanceState) {
        working = savedInstanceState.getBoolean(getString(R.string.key_working_bundle_savedInstanceState));
        data = savedInstanceState.getString(getString(R.string.key_data_bundle_savedInstanceState));
        Log.d("MainActivity", "loadStateFromBundle: "+working+", "+data);

        btnStart.setEnabled(!working);
        btnStop.setEnabled(working);
        txtvInfo.setText(data);
    }

    private void startAdvertising() {
        Log.d("MainActivity", "startAdvertising()");

        data = Calendar.getInstance().getTime().toString();

        Context context = getApplicationContext();
        Intent intent = new Intent(context, MyService.class);
        intent.putExtra(getString(R.string.key_data_intent_launch_MyService), data);
        if (context.startService(intent) == null) {
            Log.d("MainActivity", "startAdvertising: Failed to startService()");
            txtvInfo.setText(R.string.failed_to_start_advertising);
            return;
        }

        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
        txtvInfo.setText(data);
        working = true;

        String msg = getString(R.string.advertising_started);
        Snackbar snackbar = Snackbar.make(layout, msg, SNACKBAR_DURATION);
        snackbar.setAction(android.R.string.ok, new DoNothingOnClick());
        snackbar.show();
    }

    private void stopAdvertising() {
        Log.d("MainActivity", "stopAdvertising()");

        Context context = getApplicationContext();
        Intent intent = new Intent(context, MyService.class);
        if (!context.stopService(intent)) {
            Log.d("MainActivity", "stopAdvertising: stopService() returned false");
            return;
        }

        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
        txtvInfo.setText("");
        data = null;
        working = false;

        String msg = getString(R.string.advertising_stopped);
        Snackbar snackbar = Snackbar.make(layout, msg, SNACKBAR_DURATION);
        snackbar.setAction(android.R.string.ok, new DoNothingOnClick());
        snackbar.show();
    }
}
