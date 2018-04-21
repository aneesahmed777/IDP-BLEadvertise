package com.aneesahmed777.ble_advertise;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.support.design.widget.Snackbar;

import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.HashSet;

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

        final String action = intent.getAction();
        if (action == null || notifyActivity == null)
            return;

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
            notifyActivity.onBluetoothStateChangeBroadcastReceive(intent);
        else if (action.equals(notifyActivity.getString(R.string.action_broadcast_status)))
            notifyActivity.onServiceStatusBroadcastReceive(intent);
    }
}

class ExitActivityOnClick implements View.OnClickListener {
    private MainActivity activity;

    public ExitActivityOnClick(MainActivity activity) {
        super();
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {
        activity.finish();
    }
}

class RequestPermissionsProceedListener implements View.OnClickListener {

    private MainActivity activity;

    public RequestPermissionsProceedListener(MainActivity activity) {
        super();
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {
        activity.requestPermissions();
    }
}

class RequestEnableBluetoothListener implements View.OnClickListener {

    private MainActivity activity;

    public RequestEnableBluetoothListener(MainActivity activity) {
        super();
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {
        Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBluetoothIntent, MainActivity.REQUEST_ENABLE_BLUETOOTH_CODE);
    }
}

class DoNothingOnClick implements View.OnClickListener {

    private MainActivity activity;

    public DoNothingOnClick(MainActivity activity) {
        super();
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {
        // Do nothing.
    }
}

class BtnStartListener implements View.OnClickListener {

    private MainActivity activity;

    public BtnStartListener(MainActivity activity) {
        super();
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {
        activity.startAdvertising();
    }
}

class BtnStopListener implements View.OnClickListener {

    private MainActivity activity;

    public BtnStopListener(MainActivity activity) {
        super();
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {
        activity.stopAdvertising();
    }
}

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_ENABLE_BLUETOOTH_CODE = 456;
    private static final int REQUEST_PERMISSIONS_CODE = 123;
    private static final HashSet<String> permissionsNecessary;

    private ConstraintLayout layout;
    private TextView txtvInfo;
    private Button btnStart;
    private Button btnStop;
    private HashSet<String> permissionsNotGranted;
    private Boolean gettingCreated;
    private Boolean working;
    private String data;

    private SnackbarManager snackbarManager;

    static {
        permissionsNecessary = new HashSet<>();
        permissionsNecessary.add(android.Manifest.permission.BLUETOOTH);
        permissionsNecessary.add(android.Manifest.permission.BLUETOOTH_ADMIN);
        permissionsNecessary.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private class SnackbarManager {

        private static final int SNACKBAR_DURATION = 10000;  // 10 sec

        Snackbar latest_snackbar;
        Integer resIdText;
        Boolean durationIndefinite;
        Integer resIdActionText;
        String actionListenerClassname;
        private MainActivity activity;

        SnackbarManager(MainActivity activity) {
            latest_snackbar = Snackbar.make(layout, "", SNACKBAR_DURATION);
            resIdText = 0;
            durationIndefinite = false;
            resIdActionText = 0;
            actionListenerClassname = null;
            this.activity = activity;
        }

        void show(Integer resIdText, Boolean durationIndefinite,
                           Integer resIdActionText, String actionListenerClassname) {
            this.resIdText = resIdText;
            this.durationIndefinite = durationIndefinite;
            this.resIdActionText = resIdActionText;
            this.actionListenerClassname = actionListenerClassname;

            latest_snackbar.dismiss();
            latest_snackbar = null;

            create();
            latest_snackbar.show();
        }

        private void create() {
            if (durationIndefinite)
                latest_snackbar = Snackbar.make(layout, resIdText, Snackbar.LENGTH_INDEFINITE);
            else
                latest_snackbar = Snackbar.make(layout, resIdText, SNACKBAR_DURATION);

            TextView snackbarTextView = latest_snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
            snackbarTextView.setMaxLines(5);

            try {
                Class<?> theClass = Class.forName(actionListenerClassname);
                Constructor<?> theClassInstanceConstructor = theClass.getConstructor(MainActivity.class);
                Object doActionOnClick = theClassInstanceConstructor.newInstance(activity);
                latest_snackbar.setAction(resIdActionText, (View.OnClickListener) doActionOnClick);
            } catch (Exception e) {
                e.printStackTrace();
                // ignore
            }
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

        btnStart.setOnClickListener(new BtnStartListener(this));
        btnStop.setOnClickListener(new BtnStopListener(this));

        snackbarManager = new SnackbarManager(this);

        txtvInfo.setText(R.string.initialising_activity);
        btnStart.setEnabled(false);
        btnStop.setEnabled(false);
        gettingCreated = true;
        working = false;
        data = null;
        permissionsNotGranted = new HashSet<>();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            txtvInfo.setText(R.string.device_lacks_ble_feature);
            snackbarManager.show(R.string.app_cannot_work_on_this_device, true,
                    R.string.exit, ExitActivityOnClick.class.getName());
            return;
        }

        if (!MainBroadcastReceiver.getInstance_hasBeenCalledBefore()) {
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(getString(R.string.action_broadcast_status));
            intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            localBroadcastManager.registerReceiver(MainBroadcastReceiver.getInstance(), intentFilter);
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
        savedInstanceState.putBoolean(getString(R.string.key_gettingCreated_bundle_savedState), gettingCreated);
        savedInstanceState.putBoolean(getString(R.string.key_working_bundle_savedState), working);
        savedInstanceState.putString(getString(R.string.key_data_bundle_savedState), data);
        savedInstanceState.putCharSequence(getString(R.string.key_txtvw_info_bundle_savedState), txtvInfo.getText());

        if (snackbarManager.latest_snackbar.isShown()) {
            savedInstanceState.putInt(getString(R.string.key_snackbar_resIdText_bundle_savedState),
                    snackbarManager.resIdText);
            savedInstanceState.putInt(getString(R.string.key_snackbar_resIdActionText_bundle_savedState),
                    snackbarManager.resIdActionText);
            savedInstanceState.putString(getString(R.string.key_snackbar_actionListenerClassname_bundle_savedState),
                    snackbarManager.actionListenerClassname);
            savedInstanceState.putBoolean(getString(R.string.key_snackbar_durationIndefinite_bundle_savedState),
                    snackbarManager.durationIndefinite);
        }

        Log.d("MainActivity", "saveStateIntoBundle: "+ gettingCreated +", "+working+", "+data);
    }

    private void loadStateFromBundle(Bundle savedInstanceState) {
        gettingCreated = savedInstanceState.getBoolean(getString(R.string.key_gettingCreated_bundle_savedState));
        working = savedInstanceState.getBoolean(getString(R.string.key_working_bundle_savedState));
        data = savedInstanceState.getString(getString(R.string.key_data_bundle_savedState));
        Log.d("MainActivity", "loadStateFromBundle: "+ gettingCreated +", "+working+", "+data);

        if (gettingCreated) {
            btnStart.setEnabled(false);
            btnStop.setEnabled(false);
        } else {
            btnStart.setEnabled(!working);
            btnStop.setEnabled(working);
        }

        txtvInfo.setText(savedInstanceState.getCharSequence(getString(R.string.key_txtvw_info_bundle_savedState)));

        Integer resIdText = savedInstanceState.getInt(
                getString(R.string.key_snackbar_resIdText_bundle_savedState), 0);
        if (resIdText != 0) {
            Integer resIdActionText = savedInstanceState.getInt(
                    getString(R.string.key_snackbar_resIdActionText_bundle_savedState), 0);
            String actionListenerClassname = savedInstanceState.getString(
                    getString(R.string.key_snackbar_actionListenerClassname_bundle_savedState));
            Boolean durationIndefinite = savedInstanceState.getBoolean(
                    getString(R.string.key_snackbar_durationIndefinite_bundle_savedState), false);
            snackbarManager.show(resIdText, durationIndefinite, resIdActionText, actionListenerClassname);
        }
    }

    private void showPermissionsRationale() {
        snackbarManager.show(R.string.permissions_rationale, true,
                R.string.proceed, RequestPermissionsProceedListener.class.getName());
    }

    private boolean checkPermissions() {
        permissionsNotGranted.clear();
        for (String permission: permissionsNecessary) {
            if (android.support.v4.content.ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNotGranted.add(permission);
            }
        }

        Log.d("MainActivity", "checkPermissions: permissionsNotGranted "+permissionsNotGranted.toString());

        return permissionsNotGranted.isEmpty();
    }

    public void requestPermissions() {
        if (permissionsNotGranted.isEmpty())
            return;
        String[] permissionsNotGrantedArray = new String[permissionsNotGranted.size()];
        permissionsNotGrantedArray = permissionsNotGranted.toArray(permissionsNotGrantedArray);
        ActivityCompat.requestPermissions(this, permissionsNotGrantedArray, REQUEST_PERMISSIONS_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d("Called", "onRequestPermissionsResult");

        switch (requestCode) {
            case REQUEST_PERMISSIONS_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; ++i) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            permissionsNotGranted.remove(permissions[i]);
                        }
                    }
                }

                Log.d("permissionsNotGranted", permissionsNotGranted.toString());

                boolean areReqdPermissionsGranted = permissionsNotGranted.isEmpty();
                if (areReqdPermissionsGranted)
                    startAdvertising();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH_CODE: {
                if (resultCode == RESULT_OK)
                    startAdvertising();
            }
        }
    }

    public void startAdvertising() {
        Log.d("MainActivity", "startAdvertising()");

        boolean areReqdPermissionsGranted = checkPermissions();
        if (!areReqdPermissionsGranted) {
            showPermissionsRationale();
            return;
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            snackbarManager.show(R.string.device_does_not_support_bt, true,
                    R.string.exit, ExitActivityOnClick.class.getName());
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            snackbarManager.show(R.string.bt_is_disabled, true,
                    R.string.enable, RequestEnableBluetoothListener.class.getName());
            return;
        }

        BluetoothLeAdvertiser advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();

        if (advertiser == null) {
            snackbarManager.show(R.string.device_does_not_support_ble_ad, true,
                    R.string.exit, ExitActivityOnClick.class.getName());
            return;
        }

        data = Calendar.getInstance().getTime().toString();

        Context context = getApplicationContext();
        Intent intentStartAdvertising = new Intent(context, MyService.class)
                .putExtra(getString(R.string.key_command_intent_service), R.string.val_start_intent_service)
                .putExtra(getString(R.string.key_data_intent_service), data);
        context.startService(intentStartAdvertising);

        txtvInfo.setText(R.string.starting_advertisement);
        btnStart.setEnabled(false);
        btnStop.setEnabled(false);
    }

    public void stopAdvertising() {
        Log.d("MainActivity", "stopAdvertising()");

        Context context = getApplicationContext();
        Intent intentStopAdvertising = new Intent(context, MyService.class)
                .putExtra(getString(R.string.key_command_intent_service), R.string.val_stop_intent_service);
        context.startService(intentStopAdvertising);

        txtvInfo.setText(R.string.stopping_advertisement);
        btnStart.setEnabled(false);
        btnStop.setEnabled(false);
    }

    public void onServiceStatusBroadcastReceive(Intent statusIntent) {
        Integer resIdStatus = statusIntent.getIntExtra(getString(R.string.key_status_action_broadcast_status), 0);
        String status = getString(resIdStatus);

        if (gettingCreated) {
            gettingCreated = false;
            data = statusIntent.getStringExtra(getString(R.string.key_data_action_broadcast_status));
            Log.d("MainActivity", "onServiceStatusBroadcastReceive: "+status+", "+data);
            switch (resIdStatus) {
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
            Log.d("MainActivity", "onServiceStatusBroadcastReceive: "+status);
            switch (resIdStatus) {
                case R.string.val_start_ok_action_broadcast_status: {
                    btnStart.setEnabled(false);
                    btnStop.setEnabled(true);
                    txtvInfo.setText(data);
                    working = true;
                    snackbarManager.show(R.string.advertisement_started, false,
                            android.R.string.ok, DoNothingOnClick.class.getName());
                    break;
                }
                case R.string.val_stop_ok_action_broadcast_status: {
                    btnStart.setEnabled(true);
                    btnStop.setEnabled(false);
                    txtvInfo.setText(R.string.idle);
                    data = null;
                    working = false;
                    snackbarManager.show(R.string.advertisement_stopped, false,
                            android.R.string.ok, DoNothingOnClick.class.getName());
                    break;
                }
                case R.string.val_start_fail_action_broadcast_status: {
                    btnStart.setEnabled(true);
                    btnStop.setEnabled(false);
                    txtvInfo.setText(R.string.failed_to_start_advertisement);
                    data = null;
                    snackbarManager.show(R.string.failed_to_start_advertisement, false,
                            android.R.string.ok, DoNothingOnClick.class.getName());
                    break;
                }
//                case R.string.val_stop_fail_action_broadcast_status: {
//                    btnStart.setEnabled(false);
//                    btnStop.setEnabled(true);
//                    txtvInfo.setText(R.string.failed_to_stop_advertisement);
//                    break;
//                }
            }
        }
    }

    public void onBluetoothStateChangeBroadcastReceive (Intent intent) {
        final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
        switch (state) {
            case BluetoothAdapter.STATE_OFF:
                Log.d("MainActivity", "onBluetoothStateChangeBroadcastReceive: "+"STATE_OFF");
                if (working)
                    stopAdvertising();
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                Log.d("MainActivity", "onBluetoothStateChangeBroadcastReceive: "+"STATE_TURNING_OFF");
                break;
        }
    }
}
