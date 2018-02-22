package com.psyberia.powermanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.natage.luxbatterysaver.FastCharger;
import com.orhanobut.logger.Logger;

//AliveReceiver
public class BatteryReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

//        //Intent { act=android.intent.action.ACTION_POWER_DISCONNECTED flg=0x4000010 cmp=com.natage.luxbatterysaver/.BatteryReceiver }
//        //Intent { act=android.intent.action.ACTION_POWER_CONNECTED flg=0x4000010 cmp=com.natage.luxbatterysaver/.BatteryReceiver }

        if (Intent.ACTION_POWER_CONNECTED.equals(action) && !FastCharger.attached
//                || action.equals(Intent.ACTION_BATTERY_CHANGED //add custom stuff
                ) {
            Logger.d("Phone was connected to power");
            try {

//                Intent o = new Intent(context, FastCharger.class);
//                o.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(o);
                Intent i = new Intent();
                i.setClassName(context.getPackageName(), context.getPackageName() + ".FastCharger");
//            i.setClassName("com.natage.luxbatterysaver",
//                    "com.natage.luxbatterysaver.FastCharger");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
// else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
//            // Do something when power disconnected
//            Toast.makeText(context, "Disconnected", Toast.LENGTH_LONG).show();
//        }
    }
}
/*
* This broadcast receiver declares an intent-filter for a protected broadcast action string,
* which can only be sent by the system, not third-party applications. However, the receiver's onReceive method does not appear to call getAction to ensure that the received Intent's action string matches the expected value, potentially making it possible for another actor to send a spoofed intent with no action string
* or a different action string and cause undesired behavior.
* */