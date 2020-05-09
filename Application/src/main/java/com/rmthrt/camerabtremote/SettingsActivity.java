package com.rmthrt.camerabtremote;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;




public class SettingsActivity extends Activity {


    public static Button mResetDefaultDeviceButton;
    public static TextView mDefaultDeviceTextView;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        String  alreadyPairedDevice = MainActivity.persistency.getString(MainActivity.PERSISTENCY_DEVICE_ADDRESS,null);


        mDefaultDeviceTextView = findViewById(R.id.defaultDeviceTextView);


        if(alreadyPairedDevice==null)
        {
            findViewById(R.id.welcomeTitleLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.helpTitleLayout).setVisibility(View.GONE);
            findViewById(R.id.defaultDeviceLayout).setVisibility(View.GONE);
            findViewById(R.id.helpLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.settingsTitleLayout).setVisibility(View.INVISIBLE);
            findViewById(R.id.helpButtonLayout).setVisibility(View.INVISIBLE);

        }
        else
        {
            mDefaultDeviceTextView.setText(alreadyPairedDevice);
            findViewById(R.id.welcomeTitleLayout).setVisibility(View.INVISIBLE);
            findViewById(R.id.helpTitleLayout).setVisibility(View.GONE);
            findViewById(R.id.settingsTitleLayout).setVisibility(View.VISIBLE);

            findViewById(R.id.defaultDeviceLayout).setVisibility(View.VISIBLE);

            findViewById(R.id.helpLayout).setVisibility(View.INVISIBLE);

            findViewById(R.id.helpButtonLayout).setVisibility(View.VISIBLE);
        }











    }

    public void resetDefaultDeviceClick(View v) {

        SharedPreferences.Editor editor = MainActivity.persistency.edit();
        editor.putString(MainActivity.PERSISTENCY_DEVICE_ADDRESS, null);
        editor.commit();

        DeviceControlActivity.resetDeviceName();
        mDefaultDeviceTextView.setText("None");

    }

    public void closeClick(View v) {

        this.finish();

    }

    public void helpClick(View v) {

        findViewById(R.id.helpTitleLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.helpLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.helpButtonLayout).setVisibility(View.INVISIBLE);
        findViewById(R.id.settingsTitleLayout).setVisibility(View.INVISIBLE);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
