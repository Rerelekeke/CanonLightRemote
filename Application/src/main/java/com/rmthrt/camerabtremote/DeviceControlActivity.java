package com.rmthrt.camerabtremote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static android.os.SystemClock.sleep;


import static com.rmthrt.camerabtremote.GlobalConstants.PAIRING_MODE_IS_REMOTE;
import static com.rmthrt.camerabtremote.GlobalConstants.PAIRING_MODE_IS_PHONE;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */




public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "PERSISTENCY_DEVICE_ADDRESS";
    public static final String USING_VIBRATOR = "USING_VIBRATOR";
    public static final String NOT_USING_VIBRATOR = "NOT_USING_VIBRATOR";
    public static final String USING_VOLUME_BUTTONS = "USING_VOLUME_BUTTONS";
    public static final String NOT_USING_VOLUME_BUTTONS = "NOT_USING_VOLUME_BUTTONS";
    public static final String USING_HEADSET = "USING_HEADSET";
    public static final String NOT_USING_HEADSET = "NOT_USING_HEADSET";



    public static Activity thisActivity;
    private IBinder mIBinder;

    private TextView mConnectionState;
    static private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private boolean mWasConnected = false;
    private Button mButtonShutter;
    public static Button mButtonHeadset;
    public static Button mButtonVolumeButtons;
    public static Button mButtonVibrator;




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
        initButtons();
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
                invalidateOptionsMenu();

            } else if (GlobalConstants.ACTION_GATT_CONNECTED_AND_PAIRED.equals(action)){
                updateConnectionState(R.string.connected);
            } else if (GlobalConstants.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                //mBluetoothLeService.connect(mDeviceAddress);
            } else if (GlobalConstants.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

                //check if camera is in remote or phone bluetooth pairing status
                mBluetoothLeService.phoneOrRemoteMode();

            }
            else if (GlobalConstants.REMOTE_OR_PHONE_GOOD_RESPONSE.equals(action)) {
                if (PAIRING_MODE_IS_PHONE) {
                    mBluetoothLeService.CheckIfPaired();
                    return;
                }
                if (PAIRING_MODE_IS_REMOTE)
                {
                    mBluetoothLeService.pairAndConnect();
                    return;
                }
            }
            else if (GlobalConstants.REMOTE_OR_PHONE_BAD_RESPONSE.equals(action)) {
                mBluetoothLeService.mUsingVibrator = false;
                mBluetoothLeService.disconnect();
                intent = new Intent(getApplicationContext(), PairingErrorActivity.class);
                startActivity(intent);
                mBluetoothLeService.stopForegroundService();
                mBluetoothLeService.tryServiceStopSelf();
                finish();
            }
            else if (GlobalConstants.PHONE_END_OF_PAIRING.equals(action)) {
                mBluetoothLeService.pairAndConnectByStep(7);
                sleep(1000);
                updateConnectionState(R.string.connected);
            }
            else if (GlobalConstants.PHONE_PAIRING_FIRST_PART.equals(action)) {
                updateConnectionState(R.string.connecting);
                mBluetoothLeService.pairAndConnectFirstPart();
                mBluetoothLeService.isPaired();
            }
            else if (GlobalConstants.PHONE_PAIRING_SECOND_PART.equals(action)) {
                mBluetoothLeService.pairAndConnectSecondPart();
            }
            else if(GlobalConstants.ACTION_GATT_WAS_ALREADY_PAIRED.equals(action))
            {
                //if(false == mBluetoothLeService.getFullPairing()) {
                    updateConnectionState(R.string.connected);
                //}
            }

        }
    };


    static public String getDeviceName()
    {
        return mDeviceName;
    }
    static public void resetDeviceName()
    {
        mDeviceName = null;
    }
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

    private void initButtons()
    {



        if(MainActivity.persistency.getBoolean(MainActivity.PERSISTENCY_USING_HEADSET,true) == true)
        {
            mButtonHeadset.setSelected(true);
            sendBroadcast(new Intent(USING_HEADSET));
        }
        else
        {
            mButtonHeadset.setSelected(false);
            sendBroadcast(new Intent(NOT_USING_HEADSET));
        }

        if(MainActivity.persistency.getBoolean(MainActivity.PERSISTENCY_USING_VOLUME_BUTTONS,true) == true)
        {
            mButtonVolumeButtons.setSelected(true);
            sendBroadcast(new Intent(USING_VOLUME_BUTTONS));
        }
        else
        {
            mButtonVolumeButtons.setSelected(false);
            sendBroadcast(new Intent(NOT_USING_VOLUME_BUTTONS));
        }

        if(MainActivity.persistency.getBoolean(MainActivity.PERSISTENCY_USING_VIBRATOR,true) == true)
        {
            mButtonVibrator.setSelected(true);
            sendBroadcast(new Intent(USING_VIBRATOR));
        }
        else
        {
            mButtonVibrator.setSelected(false);
            sendBroadcast(new Intent(NOT_USING_VIBRATOR));
        }


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        thisActivity = this;
        setContentView(R.layout.remote_control);


        mButtonShutter = findViewById(R.id.btn_shutter);

        mButtonHeadset = findViewById(R.id.btn_headset);
        mButtonVolumeButtons = findViewById(R.id.btn_volume_buttons);
        mButtonVibrator = findViewById(R.id.btn_vibrator);


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

        SharedPreferences.Editor editor = MainActivity.persistency.edit();
        editor.putString(MainActivity.PERSISTENCY_DEVICE_ADDRESS, mDeviceAddress);
        editor.commit();

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

        final Intent intent = getIntent();



        thisActivity = this;
        setConfigDisplay();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, makeUIUpdateIntentFilter());

        initButtons();

        if(mBluetoothLeService == null && mWasConnected == true)
        {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) mIBinder).getService();
        }
        if (mBluetoothLeService != null && BluetoothLeService.mConnectionState == BluetoothLeService.STATE_DISCONNECTED) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            //Log.d(TAG, "Connect request result=" + result);
        }



        BluetoothLeService.isControlActivityVisible = true;

        //if(mBluetoothLeService.mUsingVibrator == null) return;

        if (BluetoothLeService.mConnectionState == BluetoothLeService.STATE_CONNECTED) {
            mBluetoothLeService.mUsingVibrator = false;
            updateConnectionState(R.string.connected);
            mBluetoothLeService.mUsingVibrator = true;

        } else if (BluetoothLeService.mConnectionState == BluetoothLeService.STATE_CONNECTING) {
            if(PAIRING_MODE_IS_REMOTE)
            {
                mBluetoothLeService.pairAndConnect();


            if(PAIRING_MODE_IS_REMOTE)
            {
                mBluetoothLeService.pairAndConnect();
            }
            updateConnectionState(R.string.connecting);
            invalidateOptionsMenu();
            }
        } else {
            updateConnectionState(R.string.disconnected);
            invalidateOptionsMenu();
        }
    }

    private void updateMetadata() {
    }

    @Override
    protected void onPause() {
        super.onPause();
        BluetoothLeService.isControlActivityVisible = false;
        unregisterReceiver(mGattUpdateReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //TODO Check if still needed :
        if (pm.isInteractive())
        {
            mBluetoothLeService.ms.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_STOPPED, 0, 0) //you simulate a player which plays something.
                    .build());

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
            case R.id.menu_connect:
                if (mBluetoothLeService == null) {
                    initBluetoothConnection(mIBinder);
                    return true;
                }
                //mBluetoothLeService.connect(mDeviceAddress);
                mConnectionState.setText(R.string.connecting);
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


                    mConnectionState.setTextColor(Color.parseColor("#087f23"));
                    mButtonShutter.setEnabled(true);
                    mBluetoothLeService.ms.setActive(true);
                    mBluetoothLeService.vibrate(200,3);

                }


                if (resourceId != R.string.connected)
                {
                    mConnectionState.setTextColor(Color.parseColor("#b5362f"));
                    mButtonShutter.setEnabled(false);
                    if(mBluetoothLeService!=null)
                    {
                        mBluetoothLeService.ms.setActive(false);
                    }
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
        intentFilter.addAction(GlobalConstants.REMOTE_OR_PHONE_GOOD_RESPONSE);
        intentFilter.addAction(GlobalConstants.REMOTE_OR_PHONE_BAD_RESPONSE);
        intentFilter.addAction(GlobalConstants.PHONE_PAIRING_SECOND_PART);
        intentFilter.addAction(GlobalConstants.PHONE_END_OF_PAIRING);
        intentFilter.addAction(GlobalConstants.PHONE_PAIRING_FIRST_PART);
        intentFilter.addAction(GlobalConstants.ACTION_GATT_WAS_ALREADY_PAIRED);
        return intentFilter;
    }

    private static IntentFilter makeUIUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GlobalConstants.ACTION_MSG_DELAY);
        intentFilter.addAction(GlobalConstants.ACTION_MSG_PROGRESS);
        intentFilter.addAction(GlobalConstants.ACTION_MSG_SHUTTER_BUTTON_CLICK);
        return intentFilter;
    }

    // Methods called when shutter button is clicked

    public void settingsClick(View v) {
        final Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);

    }


    public void shutterClick(View v) {
        mBluetoothLeService.clickShutter();
    }

    public void volumeButtonsClick(View v) {

        if(mButtonVolumeButtons.isSelected())
        {
            mButtonVolumeButtons.setSelected(false);
            sendBroadcast(new Intent(NOT_USING_VOLUME_BUTTONS));
        }
        else
        {
            mButtonVolumeButtons.setSelected(true);
            sendBroadcast(new Intent(USING_VOLUME_BUTTONS));
        }

    }

    public void vibrateClick(View v) {

        if(mButtonVibrator.isSelected())
        {
            mButtonVibrator.setSelected(false);
            sendBroadcast(new Intent(NOT_USING_VIBRATOR));
        }
        else
        {
            mButtonVibrator.setSelected(true);
            sendBroadcast(new Intent(USING_VIBRATOR));
        }
    }

    public void headsetClick(View v) {

        if(mButtonHeadset.isSelected())
        {
            mButtonHeadset.setSelected(false);
            sendBroadcast(new Intent(NOT_USING_HEADSET));

        }
        else
        {
            mButtonHeadset.setSelected(true);
            sendBroadcast(new Intent(USING_HEADSET));
        }
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
