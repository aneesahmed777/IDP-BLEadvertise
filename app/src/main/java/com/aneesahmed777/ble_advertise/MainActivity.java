package com.aneesahmed777.ble_advertise;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.Snackbar;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mText;
    private Button mAdvertiseButton;
    private Button mDiscoverButton;
    private HashSet<String> permissionsNotGranted;
    private static HashSet<String> permissions;
    private final int PERMISSIONS_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mText = (TextView) findViewById( R.id.text );
        mDiscoverButton = (Button) findViewById( R.id.discover_btn );
        mAdvertiseButton = (Button) findViewById( R.id.advertise_btn );

        mDiscoverButton.setOnClickListener( this );
        mAdvertiseButton.setOnClickListener( this );

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            mAdvertiseButton.setEnabled(false);
            mDiscoverButton.setEnabled(false);
        }

        if ( BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported() )
            mText.setText("Multiple advertisement supported.");
        else
            mText.setText("Multiple advertisement not supported.");

        permissions = new HashSet<>();
        permissions.add(android.Manifest.permission.BLUETOOTH);
        permissions.add(android.Manifest.permission.BLUETOOTH_ADMIN);
        permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionsNotGranted = new HashSet<>();
    }

    @Override
    public void onClick(View v) {
        if( v.getId() == R.id.discover_btn ) {
            discover();
        } else if( v.getId() == R.id.advertise_btn ) {
            advertise();
        }
    }

    private void showPermissionsRationale() {
        String prompt = "This app requires Bluetooth and Location permissions to access the BLE chip in your device.";
        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.linear_layout), prompt, Snackbar.LENGTH_INDEFINITE);
        mySnackbar.setAction("Proceed", new RequestPermissionsProceedListener());
        TextView mySnackbarTextView = mySnackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        mySnackbarTextView.setMaxLines(5);
        mySnackbar.show();
    }

    private void requestPermissions() {
        if (permissionsNotGranted.isEmpty())
            return;
        String[] permissionsNotGrantedArray = new String[permissionsNotGranted.size()];
        permissionsNotGrantedArray = permissionsNotGranted.toArray(permissionsNotGrantedArray);
        ActivityCompat.requestPermissions(this, permissionsNotGrantedArray, PERMISSIONS_REQUEST_CODE);
    }

    private boolean checkPermissions() {
        permissionsNotGranted.clear();
        for (String permission: permissions) {
            if (android.support.v4.content.ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNotGranted.add(permission);
            }
        }

        Log.d("permissionsNotGranted", permissionsNotGranted.toString());

        if (permissionsNotGranted.isEmpty())
            return true;
        else
            return false;
    }

    public void advertise() {
        boolean areReqdPermissionsGranted = checkPermissions();
        if (!areReqdPermissionsGranted) {
            showPermissionsRationale();
            return;
        }

        mText.setText("Advertising...");

        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

        if (advertiser == null)
            throw new AssertionError("advertiser cannot be null");

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable( false )
                .build();

        ParcelUuid pUuid = new ParcelUuid( UUID.fromString("821adb04-1288-4dde-8587-adfe0ecf4961") );

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName( true )
                .addServiceData( pUuid, "Hello!!!".getBytes( Charset.forName( "UTF-8" ) ) )
                .build();

        AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.v("BLE","Advertising Started");
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.e( "BLE", "Advertising onStartFailure: " + errorCode );
                super.onStartFailure(errorCode);
            }
        };

        advertiser.startAdvertising( settings, data, advertisingCallback );
    }

    public void discover() {
        Toast.makeText(this, "Not yet implemented", Toast.LENGTH_SHORT).show();
    }

    class RequestPermissionsProceedListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            requestPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d("Called", "onRequestPermissionsResult");

        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
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
                    advertise();
            }
        }
    }
}
