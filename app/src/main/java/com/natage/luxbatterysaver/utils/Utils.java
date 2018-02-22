package com.natage.luxbatterysaver.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.natage.luxbatterysaver.ChargingService;
import com.natage.luxbatterysaver.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by combo on 12/21/2017.
 * */

public class Utils {

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }
        return phrase.toString();
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static boolean requestingCharging(Context context) {
        return true;//PreferenceManager.getDefaultSharedPreferences(context)
        //.getBoolean(KEY_REQUESTING_CHARGING_UPDATES, false);
    }

    public static String getLocationTitle(Context context) {
        return context.getString(R.string.app_name);
        //R.string.location_updated, DateFormat.getDateTimeInstance().format(new Date())
    }
    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
//    static boolean requestingLocationUpdates(Context context) {
//        return PreferenceManager.getDefaultSharedPreferences(context)
//                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
//    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @\\param requestingLocationUpdates The location updates state.
     */
//    static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
//        PreferenceManager.getDefaultSharedPreferences(context)
//                .edit()
//                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
//                .apply();
//    }




    public static boolean isMyServiceRunning(Class<?> serviceClass, @NonNull Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


}
