package com.rmthrt.camerabtremote;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;


public class SettingsActivity extends Activity {


    public Switch mAutoConnectSwitch;
    public static Button mResetDefaultDeviceButton;
    public static TextView mDefaultDeviceTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAutoConnectSwitch = findViewById(R.id.AutoConnectSwitch);
        mResetDefaultDeviceButton = findViewById(R.id.resetDefaultDeviceButton);
        mDefaultDeviceTextView = findViewById(R.id.defaultDeviceTextView);

        mDefaultDeviceTextView.setText(DeviceControlActivity.getDeviceName());
        boolean val = MainActivity.persistency.getBoolean(MainActivity.PERSISTENCY_AUTO_CONNECT,false);
        mAutoConnectSwitch.setChecked(MainActivity.persistency.getBoolean(MainActivity.PERSISTENCY_AUTO_CONNECT,false));


        mAutoConnectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = MainActivity.persistency.edit();

                editor.putBoolean(MainActivity.PERSISTENCY_AUTO_CONNECT, mAutoConnectSwitch.isChecked());


                editor.commit();
            }
        });

    }

    public void resetDefaultDeviceClick(View v) {

        SharedPreferences.Editor editor = MainActivity.persistency.edit();
        editor.putString(MainActivity.PERSISTENCY_DEVICE_ADDRESS, "None");
        editor.commit();

        mDefaultDeviceTextView.setText("None");

    }

    public void closeClick(View v) {

        this.finish();

    }




}
