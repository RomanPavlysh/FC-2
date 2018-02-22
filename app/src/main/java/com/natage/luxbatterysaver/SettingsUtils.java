package com.natage.luxbatterysaver;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.lang.reflect.Method;

/**
 * Created by combo on 12/8/2017.
 * */

public class SettingsUtils {


    public static boolean isAirplaneModeOn(@NonNull Context context) {
        return Settings.System.getInt(context.getContentResolver(),
        Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }

    public static boolean isMobileDataEnabled(@NonNull Context context) {
        try {
            Object connectivityService = context.getSystemService(Context.CONNECTIVITY_SERVICE);
            ConnectivityManager cm = (ConnectivityManager) connectivityService;

            Class<?> c = Class.forName(cm.getClass().getName());
            Method m = c.getDeclaredMethod("getMobileDataEnabled");
            m.setAccessible(true);
            return (boolean) m.invoke(cm);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void rateUs(Context context) {
        try {
            Uri uri = Uri.parse(
                    String.format(context.getString(R.string.market_rate_url),
                            context.getPackageName()));
            context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (ActivityNotFoundException anfe) {


            try {
                context.startActivity(
                        new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/apps/details?id="
                                        + context.getPackageName())));

            } catch (ActivityNotFoundException exp) {
                Toast.makeText(context, "Please install browser or google market", Toast.LENGTH_LONG).show();
            }
        }
    }

}
