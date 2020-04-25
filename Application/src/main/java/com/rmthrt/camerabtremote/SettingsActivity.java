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

//        if(DeviceControlActivity.getDeviceName()==null)
//        {
//            findViewById(R.id.welcomeLayout).setVisibility(View.VISIBLE);
//            findViewById(R.id.resetLayout).setVisibility(View.INVISIBLE);
//        }
//        else
//        {
//            findViewById(R.id.welcomeLayout).setVisibility(View.INVISIBLE);
//            findViewById(R.id.resetLayout).setVisibility(View.VISIBLE);
//        }

        findViewById(R.id.defaultLayout).setVisibility(View.VISIBLE);


        mResetDefaultDeviceButton = findViewById(R.id.resetDefaultDeviceButton);
        mDefaultDeviceTextView = findViewById(R.id.defaultDeviceTextView);

        mDefaultDeviceTextView.setText(DeviceControlActivity.getDeviceName());

        mPhonePairingSwitch = findViewById(R.id.PhonePairingSwitch);
        mBtRemotePairingSwitch = findViewById(R.id.BtRemotePairingSwitch);

        mPhonePairingSwitch.setChecked(MainActivity.persistency.getBoolean(MainActivity.PERSISTENCY_USING_PHONE_OR_BLUETOOTH_PAIRING,false));
        mBtRemotePairingSwitch.setChecked(!MainActivity.persistency.getBoolean(MainActivity.PERSISTENCY_USING_PHONE_OR_BLUETOOTH_PAIRING,false));

        mPhonePairingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = MainActivity.persistency.edit();
                if(mPhonePairingSwitch.isChecked())
                {
                    mBtRemotePairingSwitch.setChecked(false);
                    editor.putBoolean(MainActivity.PERSISTENCY_USING_PHONE_OR_BLUETOOTH_PAIRING, true);
                }
                else
                {
                    mBtRemotePairingSwitch.setChecked(true);
                    editor.putBoolean(MainActivity.PERSISTENCY_USING_PHONE_OR_BLUETOOTH_PAIRING, false);
                }
                editor.commit();
            }
        });

        mBtRemotePairingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = MainActivity.persistency.edit();
                if(mBtRemotePairingSwitch.isChecked())
                {
                    mPhonePairingSwitch.setChecked(false);
                    editor.putBoolean(MainActivity.PERSISTENCY_USING_PHONE_OR_BLUETOOTH_PAIRING, false);
                }
                else
                {
                    mPhonePairingSwitch.setChecked(true);
                    editor.putBoolean(MainActivity.PERSISTENCY_USING_PHONE_OR_BLUETOOTH_PAIRING, true);
                }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
