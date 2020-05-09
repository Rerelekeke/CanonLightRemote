package com.rmthrt.camerabtremote;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Method;

import static android.os.SystemClock.sleep;
import static com.rmthrt.camerabtremote.GlobalConstants.PAIRING_MODE_IS_PHONE;
import static com.rmthrt.camerabtremote.GlobalConstants.PAIRING_MODE_IS_REMOTE;

public class PairingErrorActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pairing_error_advicer);


        if(PAIRING_MODE_IS_REMOTE == true && PAIRING_MODE_IS_PHONE == false)
        {
            findViewById(R.id.PhonePairingLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.RemotePairingLayout).setVisibility(View.INVISIBLE);
        }
        if(PAIRING_MODE_IS_REMOTE == false && PAIRING_MODE_IS_PHONE == true)
        {
            findViewById(R.id.RemotePairingLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.PhonePairingLayout).setVisibility(View.INVISIBLE);

        }

    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e("PairingErroorActivity", e.getMessage());
        }
    }
    public void unboundClick(View v) {

        unpairDevice(MainActivity.mdevice);

        DeviceControlActivity.thisActivity.finish();

        this.finish();

    }

    public void cancelClick(View v) {



        //DeviceControlActivity.thisActivity.finish();
//        sleep(100);
//        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//        startActivity(intent);

        this.finish();

    }

}
