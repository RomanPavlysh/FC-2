package com.psyberia.powermanager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.natage.luxbatterysaver.FastCharger;
import com.natage.luxbatterysaver.oservice.CentralContainer;
import com.orhanobut.logger.Logger;

/**
 * Created by combo on 12/21/2017.
 * */

public class PowerMgrReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Logger.d(intent.getAction());

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_USER_PRESENT.equals(action)) {

            //Toast.makeText(context, "BOOT_LOADER", Toast.LENGTH_LONG).show();

            CentralContainer.startJobScheduler(context);
        }
    }
}
