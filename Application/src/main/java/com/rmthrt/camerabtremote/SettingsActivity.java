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


    public static Button mResetDefaultDeviceButton;
    public static TextView mDefaultDeviceTextView;

    private Switch mPhonePairingSwitch;
    private Switch mBtRemotePairingSwitch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if(DeviceControlActivity.getDeviceName()==null)
        {
            findViewById(R.id.welcomeLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.defaulLayout).setVisibility(View.INVISIBLE);
        }
        else
        {
            findViewById(R.id.welcomeLayout).setVisibility(View.INVISIBLE);
            findViewById(R.id.defaulLayout).setVisibility(View.VISIBLE);
        }

        mResetDefaultDeviceButton = findViewById(R.id.resetDefaultDeviceButton);
        mDefaultDeviceTextView = findViewById(R.id.defaultDeviceTextView);

        mDefaultDeviceTextView.setText(DeviceControlActivity.getDeviceName());

        mPhonePairingSwitch = findViewById(R.id.PhonePairingSwitch);
        mBtRemotePairingSwitch = findViewById(R.id.BtRemotePairingSwitch);

        mPhonePairingSwitch.setChecked(!MainActivity.persistency.getBoolean(MainActivity.PERSISTENCY_USING_PHONE_OR_BLUETOOTH_PAIRING,false));
        mBtRemotePairingSwitch.setChecked(MainActivity.persistency.getBoolean(MainActivity.PERSISTENCY_USING_PHONE_OR_BLUETOOTH_PAIRING,false));

        mPhonePairingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mPhonePairingSwitch.isChecked())
                {
                    mBtRemotePairingSwitch.setChecked(false);
                }
            }
        });

        mBtRemotePairingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mBtRemotePairingSwitch.isChecked())
                {
                    mPhonePairingSwitch.setChecked(false);
                }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor editor = MainActivity.persistency.edit();
        editor.putBoolean(MainActivity.PERSISTENCY_USING_PHONE_OR_BLUETOOTH_PAIRING, false);
        editor.commit();
    }



}
