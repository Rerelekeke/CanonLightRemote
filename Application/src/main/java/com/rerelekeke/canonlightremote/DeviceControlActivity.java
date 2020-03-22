package com.rerelekeke.canonlightremote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import static android.os.SystemClock.sleep;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";


    private IBinder mIBinder;

    private TextView mConnectionState;
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private boolean mWasConnected = false;
    private Button mButtonShutter;



    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mIBinder = service;
            initBluetoothConnection(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };


    private void initBluetoothConnection(IBinder service) {
        mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
        mWasConnected = true;
        if (!mBluetoothLeService.initialize()) {
            //Log.d(TAG, "Unable to initialize Bluetooth");
            finish();
        }
        // Automatically connects to the device upon successful start-up initialization.
        //Log.d(TAG, "Service in onServiceConnected");
        if (BluetoothLeService.mConnectionState == BluetoothLeService.STATE_CONNECTED) {
            mConnected = true;
            updateConnectionState(R.string.connected);
            invalidateOptionsMenu();
        } else if (BluetoothLeService.mConnectionState == BluetoothLeService.STATE_CONNECTING) {
            // Do wait
        } else {
            mBluetoothLeService.connect(mDeviceAddress);
        }
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_GATT_IS_PAIRED: received data from the device.  This can be a result of read
    //                        or notification operations.
    // ACTION_GATT_PAIRING_FIRST_PART: received data from the device.  This can be a result of read
    //                        or notification operations.
    // ACTION_GATT_PAIRING_SECOND_PART: received data from the device.  This can be a result of read
    //                        or notification operations.
    //TODO comment properly new steps
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (GlobalConstants.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (GlobalConstants.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                mBluetoothLeService.connect(mDeviceAddress);
            } else if (GlobalConstants.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                mBluetoothLeService.CheckIfPaired();
            }
            else if (GlobalConstants.ACTION_GATT_IS_PAIRED.equals(action)) {
                mBluetoothLeService.pairAndConnectByStep(7);
                sleep(1000);
            }
            else if (GlobalConstants.ACTION_GATT_PAIRING_FIRST_PART.equals(action)) {
                //updateConnectionState(R.string.connecting);
                mBluetoothLeService.pairAndConnectFirstPart();
            }
            else if (GlobalConstants.ACTION_GATT_PAIRING_SECOND_PART.equals(action)) {
                mBluetoothLeService.pairAndConnectSecondPart();
            }
        }
    };

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");

            final String action = intent.getAction();


            if (GlobalConstants.ACTION_MSG_SHUTTER_BUTTON_CLICK.equals(action)) {
                //Log.d("receiver", "Got message: " + message);
                mButtonShutter.setPressed(true);
                final Handler timerHandler = new Handler();
                timerHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mButtonShutter.setPressed(false);
                    }
                }, 200);
            }


        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remote_control);

        mButtonShutter = findViewById(R.id.btn_shutter);

        mButtonShutter.setEnabled(false);



        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);

        mConnectionState = findViewById(R.id.connection_state);

        try{
            getActionBar().setTitle(mDeviceName);
        }catch (Exception ex){}
        getActionBar().setDisplayHomeAsUpEnabled(true);

        callBluetoothService();

        //Log.d(TAG, "Activity created");
    }

    private void callBluetoothService()
    {
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        gattServiceIntent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, mDeviceName);
        gattServiceIntent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
        bindService(gattServiceIntent, mServiceConnection, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, BluetoothLeService.class));
        } else {
            startService(new Intent(this, BluetoothLeService.class));

        }

        //Log.d(TAG, "Activity created");
    }


    @Override
    protected void onResume() {
        super.onResume();
        setConfigDisplay();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, makeUIUpdateIntentFilter());
        if(mBluetoothLeService == null && mWasConnected == true)
        {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) mIBinder).getService();
            mBluetoothLeService.setMediaSession(true,true);
        }
        if (mBluetoothLeService != null && BluetoothLeService.mConnectionState == BluetoothLeService.STATE_DISCONNECTED) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            //Log.d(TAG, "Connect request result=" + result);
        }

        BluetoothLeService.isControlActivityVisible = true;
        if (BluetoothLeService.mConnectionState == BluetoothLeService.STATE_CONNECTED) {
            updateConnectionState(R.string.connected);
            mBluetoothLeService.stopForegroundService();
            BluetoothLeService.mConnectionState = BluetoothLeService.STATE_DISCONNECTED;
            callBluetoothService();
            mBluetoothLeService.setMediaSession(true,true);
        } else if (BluetoothLeService.mConnectionState == BluetoothLeService.STATE_CONNECTING) {
            updateConnectionState(R.string.connecting);
            invalidateOptionsMenu();
        } else {
            updateConnectionState(R.string.disconnected);
            invalidateOptionsMenu();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        BluetoothLeService.isControlActivityVisible = false;
        unregisterReceiver(mGattUpdateReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm.isInteractive())
        {
            mBluetoothLeService.setMediaSession(false,false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_control, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect: mConnectionState.setText(R.string.connecting);
                if (mBluetoothLeService == null) {
                    initBluetoothConnection(mIBinder);
                    return true;
                }
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                warnDisconnect(false);
                return true;
            case android.R.id.home:
                warnDisconnect(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
                if (resourceId == R.string.connected) {
                    mConnectionState.setTextColor(Color.parseColor("#FFFFFF"));
                    mButtonShutter.setEnabled(true);
                } else {
                    mConnectionState.setTextColor(Color.parseColor("#aa0000"));
                    mButtonShutter.setEnabled(false);
                }
            }
        });
    }


    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GlobalConstants.ACTION_GATT_CONNECTED);
        intentFilter.addAction(GlobalConstants.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(GlobalConstants.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(GlobalConstants.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(GlobalConstants.ACTION_GATT_PAIRING_SECOND_PART);
        intentFilter.addAction(GlobalConstants.ACTION_GATT_IS_PAIRED);
        intentFilter.addAction(GlobalConstants.ACTION_GATT_PAIRING_FIRST_PART);
        return intentFilter;
    }

    private static IntentFilter makeUIUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GlobalConstants.ACTION_MSG_DELAY);
        intentFilter.addAction(GlobalConstants.ACTION_MSG_PROGRESS);
        intentFilter.addAction(GlobalConstants.ACTION_MSG_SHUTTER_BUTTON_CLICK);
        return intentFilter;
    }

    // Method called when shutter button is clicked
    public void shutterClick(View v) {
        mBluetoothLeService.clickShutter();
    }


    private void setConfigDisplay() {

        mButtonShutter.setBackgroundResource(R.drawable.center_btn_shutter_photo);
        mButtonShutter.setText("");

        if (mBluetoothLeService == null) {
            return;
        }
    }

    @Override
    public void onBackPressed() {
        warnDisconnect(true);
    }

    private void warnDisconnect(final boolean goBack) {

        if (BluetoothLeService.mConnectionState == BluetoothLeService.STATE_DISCONNECTED) {
            BluetoothLeService.isControlActivityVisible = false;
            mBluetoothLeService.tryServiceStopSelf();
            finish();
            return;
        }

        new AlertDialog.Builder(this)
                .setMessage(R.string.bluetooth_disconnect_message)
                .setPositiveButton(R.string.bluetooth_disconnect_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (goBack) {
                            BluetoothLeService.isControlActivityVisible = false;
                        }
                        mBluetoothLeService.disconnect();
                        dialogInterface.dismiss();
                        if (goBack) {
                            finish();
                        }
                    }
                })
                .setNegativeButton(R.string.dialogs_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

}
