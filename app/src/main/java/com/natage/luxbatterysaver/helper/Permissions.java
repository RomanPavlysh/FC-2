package com.natage.luxbatterysaver.helper;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.ArrayMap;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Permissions {


    private final Context mContext;

    public static final int PERMISSION_MAIN_ACTIVITY = 443;

    private Map<String, Integer> map = new HashMap<>();


    public Permissions(Context context) {
        this.mContext = context;


//        for (String p : permissions) {
//            int bb = mContext.checkCallingOrSelfPermission(p);
//            map.put(p, bb);
//        }
//
//        Logger.d(permissions);
    }


    public boolean checkPermissions(Context context, String[] perm_arr) {

        for (String permission : perm_arr) {

            Integer res = map.get(permission);   //granted or not?

            if (res == null) {
                //Variant #1:
                // res = mContext.checkCallingOrSelfPermission(permission);

                res = ContextCompat.checkSelfPermission(context, permission)/* == PackageManager.PERMISSION_GRANTED*/;

                map.put(permission, res);
            }
            if (res == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }

        return true;
    }


//    public boolean hasPermissions(Context context) {
//        int res;
//
//        for (String perms : permissions) {
//            res = context.checkCallingOrSelfPermission(perms);
//            if (!(res == PackageManager.PERMISSION_GRANTED)) {
//                return false;
//            }
//        }
//        return true;
//    }
}
