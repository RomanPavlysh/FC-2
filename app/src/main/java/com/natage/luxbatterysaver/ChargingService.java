package com.natage.luxbatterysaver;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationCallback;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationResult;
//import com.google.android.gms.location.LocationServices;
import com.natage.luxbatterysaver.utils.Utils;
import com.orhanobut.logger.Logger;
import com.psyberia.powermanager.BatteryReceiver;

import java.util.List;

import static com.natage.luxbatterysaver.BuildConfig.DEBUG;


public class ChargingService extends Service {
    private static final String PACKAGE_NAME =
            "com.natage.luxbatterysaver";

    private static final String TAG = ChargingService.class.getSimpleName();

    /**
     * The name of the channel for notifications.
     */
    private static final String CHANNEL_ID = "channel_01";

    static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";

    static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";

    private final IBinder mBinder = new LocalBinder();

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private static final int NOTIFICATION_ID = 12345688;

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private boolean mChangingConfiguration = false;

    private NotificationManager mNotificationManager;


    /**
     * Location
     * <p>
     * Contains parameters used by {@\link com.google.android.gms.location.FusedLocationProviderApi}.
     */
//    private LocationRequest mLocationRequest;

    /**
     * Provides access to the Fused Location Provider API.
     */
//    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Callback for changes in location.
     */
//    private LocationCallback mLocationCallback;

    private Handler mServiceHandler;

    /**
     * The current location.
     */
    //private Location mLocation;

    private BroadcastReceiver batteryReceiver = new BatteryReceiver();
    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                    onNewChanged(intent);
                }
            }
        }
    };

    public ChargingService() {
        Logger.d("Created");
    }

    @Override
    public void onCreate() {

//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//
//        mLocationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                super.onLocationResult(locationResult);
//                onNewLocation(locationResult.getLastLocation());
//            }
//        };


        //@ Detector
        registerReceiver(batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));



        //Location
        //createLocationRequest();
        //getLastLocation();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");
        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION, false);
        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            removeCharging();
            stopSelf();
        }
        // Tells the system to not try to recreate the service after it has been killed.
        //return Service.START_NOT_STICKY;
        return Service.START_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.i(TAG, "in onBind()");
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }


    @Override
    public void onRebind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(TAG, "in onRebind()");
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Logger.d("Last client unbound from service");

        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        //if (!mChangingConfiguration && Utils.requestingCharging(this)) {
        Logger.d("Starting foreground service");
            /*
            // TODO(developer). If targeting O, use the following code.
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                mNotificationManager.startServiceInForeground(new Intent(this,
                        ChargingService.class), NOTIFICATION_ID, getNotification());
            } else {
                startForeground(NOTIFICATION_ID, getNotification());
            }
             */
        startForeground(NOTIFICATION_ID, getNotification());

//            ContextCompat.startForegroundService(intent.);
        //}
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(batteryInfoReceiver);
        unregisterReceiver(batteryReceiver);

        mServiceHandler.removeCallbacksAndMessages(null);


        requestCharging();//<------------

        super.onDestroy();
    }

    public void requestPowerState() {
        Intent myService = new Intent(getApplicationContext(), ChargingService.class);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(myService);
//        } else {
        startService(myService);
//        }
    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void requestCharging() {
        Log.i(TAG, "Requesting location updates");
        //Utils.setRequestingCharging(this, true);
        startService(new Intent(getApplicationContext(), ChargingService.class));
//        try {
//            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
//        } catch (SecurityException unlikely) {
//            //Utils.setRequestingCharging(this, false);
//            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
//        }
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void removeCharging() {
        Log.i(TAG, "Removing location updates");
        try {
            //mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            //Utils.setRequestingCharging(this, false);
            stopSelf();
        } catch (SecurityException unlikely) {
            //Utils.setRequestingCharging(this, true);
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }


    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    private Notification getNotification() {
        Intent intent = new Intent(this, ChargingService.class);

        CharSequence text = "@" + batteryInfoMessage;//Utils.getBatteryInfoText(batteryInfoMessage);

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // The PendingIntent to launch activity.
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .addAction(R.drawable.ic_stat_name, getString(R.string.app_name), activityPendingIntent)
                .addAction(R.drawable.ic_cancel, getString(R.string.remove_battery_info), servicePendingIntent)
                .setContentText(text)
                .setContentTitle(Utils.getLocationTitle(this))
                .setOngoing(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.setPriority(Notification.PRIORITY_HIGH);
        }
        builder.setSmallIcon(R.drawable.ic_stat_name)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        return builder.build();
    }


//    private void getLastLocation() {
//        try {
//            mFusedLocationClient.getLastLocation()
//                    .addOnCompleteListener(task -> {
//                        if (task.isSuccessful() && task.getResult() != null) {
//                            mLocation = task.getResult();
//                        } else {
//                            Log.w(TAG, "Failed to get location.");
//                        }
//                    });
//        } catch (SecurityException unlikely) {
//            Log.e(TAG, "Lost location permission." + unlikely);
//        }
//    }


    private String batteryInfoMessage;

    private void onNewChanged(@NonNull Intent intent) {

        //Forward data to MainActivity
        forwardIntent(intent);

        String technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
        String temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10 + Character.toString((char) 176) + " C";
        String voltage = (float) intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / (float) 1000 + Character.toString((char) 176) + " V";
        String level = Integer.toString(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0));

        batteryInfoMessage = String.format("%1s: %2$s %3$s %4$s", technology, temp, voltage, level);

        //Logger.d(batteryInfoMessage);
        //Logger.d(intent.getExtras());

        // Update notification content if running as a foreground service.
        if (serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification());
        }
    }

    private void forwardIntent(@NonNull Intent intent) {
        //Notify anyone listening for broadcasts about the new location.
        Intent o = new Intent(ACTION_BROADCAST);
        //o.putExtra(EXTRA_LOCATION, /*location*/intent);
        //o.putExtra(EXTRA_LOCATION, intent.getExtras());
        o.putExtras(intent);

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(o);
        //if(DEBUG)Toast.makeText(this, intent.toString(), Toast.LENGTH_SHORT).show();
    }

//    private void onNewLocation(Location location) {
//        Log.i(TAG, "New location: " + location);
//
//        mLocation = location;
//
//        // Notify anyone listening for broadcasts about the new location.
//        Intent intent = new Intent(ACTION_BROADCAST);
//        intent.putExtra(EXTRA_LOCATION, location);
//        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
//
//        // Update notification content if running as a foreground service.
//        if (serviceIsRunningInForeground(this)) {
//            mNotificationManager.notify(NOTIFICATION_ID, getNotification());
//        }
//    }

    /**
     * Sets the location request parameters.
     */
//    private void createLocationRequest() {
//        mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
//        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        ChargingService getService() {
            return ChargingService.this;
        }
    }


    /**
     * Returns true if this is a foreground service.
     *
     * @param context The {@link Context}.
     */
    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> list;
        if (manager != null) {
            list = manager.getRunningServices(Integer.MAX_VALUE);
            for (ActivityManager.RunningServiceInfo service : list) {
                if (getClass().getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
