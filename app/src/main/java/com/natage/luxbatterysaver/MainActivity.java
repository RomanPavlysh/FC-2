package com.natage.luxbatterysaver;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.MrecCallbacks;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.natage.luxbatterysaver.helper.Permissions;
import com.natage.luxbatterysaver.utils.BaseActivity;
import com.orhanobut.logger.Logger;
import com.psyberia.powermanager.BatteryReceiver;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;



import static android.media.AudioManager.RINGER_MODE_NORMAL;
import static android.media.AudioManager.RINGER_MODE_SILENT;
import static android.media.AudioManager.RINGER_MODE_VIBRATE;
import static com.natage.luxbatterysaver.SettingsUtils.isAirplaneModeOn;
import static com.natage.luxbatterysaver.SettingsUtils.isMobileDataEnabled;
import static com.natage.luxbatterysaver.SettingsUtils.rateUs;
import static com.natage.luxbatterysaver.utils.Utils.getDeviceName;


public class MainActivity extends BaseActivity implements View.OnClickListener {

    @Inject
    Permissions perm;

    @BindView(R.id.tool_bar)
    Toolbar toolbar;
    @BindView(R.id.TxtLevel)
    TextView TxtLevel;
    @BindView(R.id.TxtVoltage)
    TextView TxtVoltage;
    @BindView(R.id.TxtTemperature)
    TextView TxtTemperature;
    @BindView(R.id.tool_wifi)
    ImageView Tools_WiFi;
    @BindView(R.id.tool_rotate)
    ImageView Tools_Rotate;
    @BindView(R.id.tool_brightness)
    ImageView Tools_Brightness;
    @BindView(R.id.tool_bluetooth)
    ImageView Tools_Bluetooth;
    @BindView(R.id.tool_timeout)
    ImageView Tools_Timeout;
    @BindView(R.id.tool_mode)
    ImageView Tools_Mode;
    @BindView(R.id.PowerSavingMode)
    Button PowerSavingMode;
    @BindView(R.id.CardViewBatteryArc)
    CardView CardViewBatteryArc;
    @BindView(R.id.CardViewTools)
    CardView CardViewTools;
    @BindView(R.id.batteryDetail)
    CardView batteryDetail;
    @BindView(R.id.CardViewRate)
    CardView CardViewRate;
    @BindView(R.id.CardViewShare)
    CardView mCardViewShare;
    @BindView(R.id.btn_feedbak)
    Button btn_feedback;
    @BindView(R.id.btn_ratenow)
    Button btn_ratenow;
    @BindView(R.id.AlertLout)
    RelativeLayout AlertLout;
    @BindView(R.id.AlertText)
    TextView mAlertText;
    @BindView(R.id.adView)
    AdView mAdView;

    private BluetoothAdapter AdapterForBluetooth;

    private String appKey;

    private Intent i;
    private AudioManager am;
    private Integer Issue = 0;

    private AlertDialog.Builder exit_dialog;

    @BindView(R.id.arc_progress)
    ArcProgress progress;

    //private CircleProgress progress;
    //private DonutProgress progress;

    private Timer timer;
    private int brightness;
    private Integer rotate;
    private int timeout;
    private ContentResolver cResolver;
    private Window window;
    private Integer profile;
    InterstitialAd mInterstitialAd;

    private FirebaseAnalytics mFirebaseAnalytics;


    //Receivers
    private BroadcastReceiver chargerReceiver;      //My custom receiver
    private BroadcastReceiver batteryLevelReceiver;



    private class BatteryInfoReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

//            Intent intent1 = intent.getParcelableExtra(ChargingService.EXTRA_LOCATION);
//            Toast.makeText(MainActivity.this, intent1.toString(), Toast.LENGTH_SHORT).show();

            Logger.d(intent.getExtras());

            String temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10 + Character.toString((char) 176) + " C";
            String voltage = (float) intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / (float) 1000 + Character.toString((char) 176) + " V";
            String level = Integer.toString(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0));

            TxtTemperature.setText(temp);
            TxtVoltage.setText(voltage);
            TxtLevel.setText(level);
        }
    }


    //    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {};

    private BatteryInfoReceiver batteryInfoReceiver;

    public MainActivity() {
        Application.getAppComponent().inject(this);
    }

    private AdRequest adRequest;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    // A reference to the service used to get location updates.
    private ChargingService mService = null;

    private boolean mBound;
    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ChargingService.LocalBinder binder = (ChargingService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }

        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        }
        appKey = getResources().getString(R.string.appo_key);

        //Appodeal Interstitial
        Appodeal.initialize(this, appKey, Appodeal.INTERSTITIAL);
        showInterstitial();

        //Apodeal
        Appodeal.setMrecViewId(R.id.appodealMrecView);

        Appodeal.initialize(this, appKey, Appodeal.MREC);
        Appodeal.initialize(this, appKey, Appodeal.BANNER_BOTTOM);
        Appodeal.show(this, Appodeal.MREC);
        Appodeal.show(this, Appodeal.BANNER_BOTTOM);

        //<---------------
//        registerReceiver()
//                startFor..
        getBaseContext().getApplicationContext().sendBroadcast(new Intent("BatteryReceiver"));


        //progress = findViewById(R.id.donut_progress);


        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Admob baner

//        final NativeExpressAdView adView = findViewById(R.id.NativeadView);
//        adView.loadAd(new AdRequest.Builder().build());
//        adView.setVisibility(View.GONE);
//        adView.setAdListener(new AdListener() {
//            @Override
//            public void onAdLoaded() {
//                super.onAdLoaded();
//                adView.setVisibility(View.VISIBLE);
//            }
//        });
//
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);
//        mAdView.setAdListener(new AdListener() {
//            @Override
//            public void onAdLoaded() {
//                super.onAdLoaded();
//                mAdView.setVisibility(View.VISIBLE);
//            }
//        });

        //Handle Oreo
        //removed registerReceiver(this.batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        batteryInfoReceiver = new BatteryInfoReceiver();

        setColorToCardview();

        cResolver = getContentResolver();
        window = getWindow();


        //==========================================================================================

        boolean allowed = perm.checkPermissions(this,
                new String[]{Manifest.permission.WRITE_SETTINGS});

        if (allowed) {
            try {
                makeSettings();
                CheckOnAndOff();

            } catch (Settings.SettingNotFoundException e) {
                //Throw an error case it couldn't be retrieved
                Log.e("Error", "Cannot access system brightness");
                e.printStackTrace();
            }
        } else {
            //Toast.makeText(this, "Success", Toast.LENGTH_LONG);
            //requestPermissionsWithRationale();
            requestPerms();
        }


//        getSupportActionBar().setTitle("Granted: " + granted);
//        writesetting_dialog.show();


        //refreshAd1();
        //refreshAd2();
        getBatteryPercentage();
        SetClickListner();
        CheckIntentToolsOnOrOff();

        //AdMob Inrestitial for start

//        mInterstitialAd = new InterstitialAd(getApplicationContext());
//        mInterstitialAd.setAdUnitId(getResources().getString(R.string.instritial));
//        mInterstitialAd.loadAd(adRequest);
//        mInterstitialAd.setAdListener(new AdListener() {
//            public void onAdLoaded() {
//                showInterstitial();
//            }
//        });



        chargerReceiver = new BatteryReceiver(); //BroadcastReceiver() {
//            @SuppressLint("WrongConstant")
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                // TODO: Awesome things
//                Toast.makeText(context, "@@@@@@@@@@@@@@@@@", 5).show();

//        try {
//            Intent i = new Intent();
//            i.setClassName(context.getPackageName(), context.getPackageName() + ".FastCharger");
////            i.setClassName("com.natage.luxbatterysaver",
////                    "com.natage.luxbatterysaver.FastCharger");
//            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(i);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//            }
//        };
//

        registerReceiver(chargerReceiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));

        //NotificationManager.startServiceInForeground();
        //0 mService.requestPowerState();
    }


//    private void requestPermissionsWithRationale() {
//    }


    private void requestPerms() {

//        String[] permissions = new String[]{
//                Manifest.permission.WRITE_SETTINGS,
//                Manifest.permission.CAMERA,
//
//                Manifest.permission.READ_CONTACTS,
//                Manifest.permission.WRITE_CONTACTS,
//
//                Manifest.permission.READ_CALENDAR,
//                Manifest.permission.WRITE_CALENDAR,
//
//                Manifest.permission.GET_ACCOUNTS,
//
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//                Manifest.permission.ACCESS_FINE_LOCATION,    //gps
//
//
//                Manifest.permission.RECORD_AUDIO,
//
//                READ_PHONE_STATE//<--
//                CALL_PHONE,
//                READ_CALL_LOG
//                WRITE_CALL_LOG
//                ADD_VOICEMAIL
//                USE_SIP
//                PROCESS_OUTGOING_CALLS
//
//                BODY_SENSORS
//
//                SEND_SMS
//
//                READ_SMS
//
//                RECEIVE_SMS
//                RECEIVE_WAP_PUSH
//                RECEIVE_MMS
//
//                READ_EXTERNAL_STORAGE
//                WRITE_EXTERNAL_STORAGE
//
//        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //var1: requestPermissions(permissions, PERMISSION_REQUEST_CODE);
            //var2: ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);


            if (hasePermissions()) {
                try {
                    makeSettings();
                    CheckOnAndOff();

                } catch (Settings.SettingNotFoundException e) {
                    //Throw an error case it couldn't be retrieved
                    Log.e("Error", "Cannot access system brightness");
                    e.printStackTrace();
                }
            } else {
                NotificationManager notificationManager =
                        (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && !notificationManager.isNotificationPolicyAccessGranted()) {

                    donotdisterb_dialog.show();
                }
                writesetting_dialog.show();

            }
        }
    }

    private void showInterstitial() {
//        if (mInterstitialAd.isLoaded()) {
//            mInterstitialAd.show();
//        }
        //if (Appodeal.isLoaded(Appodeal.INTERSTITIAL))
        Appodeal.show(this, Appodeal.INTERSTITIAL);
    }


    private void setColorToCardview() {

        CardViewBatteryArc.setCardBackgroundColor(getApplicationContext().getResources().getColor(R.color.white));
        CardViewTools.setCardBackgroundColor(getApplicationContext().getResources().getColor(R.color.white));
        batteryDetail.setCardBackgroundColor(getApplicationContext().getResources().getColor(R.color.white));
        CardViewRate.setCardBackgroundColor(getApplicationContext().getResources().getColor(R.color.white));
        mCardViewShare.setCardBackgroundColor(getApplicationContext().getResources().getColor(R.color.white));
        CardViewBatteryArc.setCardElevation(0);
        CardViewTools.setCardElevation(0);
        batteryDetail.setCardElevation(0);
        CardViewRate.setCardElevation(0);
        mCardViewShare.setCardElevation(0);
    }

    @Override
    protected void setupDialog() {
        super.setupDialog();

        exit_dialog = new AlertDialog.Builder(MainActivity.this);
        exit_dialog.setTitle("Are you sure?");
        exit_dialog.setMessage("Do you want to exit from this application?");
        exit_dialog.setNegativeButton("Yes",
                (dialog, whichButton) -> {
                    am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    if (am != null) {
                        am.setRingerMode(profile);
                    }
                    setTimeout(3);
                    finish();
                });
    }

    private void SetClickListner() {
        Tools_WiFi.setOnClickListener(this);
        Tools_Rotate.setOnClickListener(this);
        Tools_Bluetooth.setOnClickListener(this);
        Tools_Brightness.setOnClickListener(this);
        Tools_Mode.setOnClickListener(this);
        Tools_Timeout.setOnClickListener(this);
        batteryDetail.setOnClickListener(this);
        PowerSavingMode.setOnClickListener(this);
        mCardViewShare.setOnClickListener(this);
        btn_feedback.setOnClickListener(this);
        btn_ratenow.setOnClickListener(this);
        AlertLout.setOnClickListener(this);

    }

    private void getBatteryPercentage() {
        batteryLevelReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                context.unregisterReceiver(this);
                int currentLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int level = -1;
                if (currentLevel >= 0 && scale > 0) {
                    level = (currentLevel * 100) / scale;
                    Log.e("%", "" + level);
                }

                timer = new Timer();
                final int finalLevel = level;
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(() -> {

                            if (progress.getProgress() == finalLevel) {
                                progress.setProgress(finalLevel);
                                progress.setBottomText("");

                                timer.cancel();
                            } else {
                                progress.setProgress(progress.getProgress() + 1);
                                progress.setBottomText("");
                            }


                        });
                    }
                }, 500, level);
            }
        };

        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelReceiver, batteryLevelFilter);
    }

    @Override
    public void onDestroy() {
        if (timer != null) {
            timer.cancel();
        }
        super.onDestroy();

//        unregisterReceiver(chargerReceiver);//My custom receiver
//removed        unregisterReceiver(batteryInfoReceiver);
//        if (batteryLevelReceiver != null) {
//            unregisterReceiver(batteryLevelReceiver);//Receiver not registered
//        }
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(batteryInfoReceiver);
        super.onPause();
    }

    private void CheckOnAndOff() {

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && wifiManager.isWifiEnabled()) {
            menuSwitcher(Tools_WiFi, R.drawable.tool_wifi_on);
        } else {
            menuSwitcher(Tools_WiFi, R.drawable.tool_wifi_off);
        }

        if (rotate == 1) {
            menuSwitcher(Tools_Rotate, R.drawable.tool_rotate_autorotate);
        } else {
            menuSwitcher(Tools_Rotate, R.drawable.tool_rotate_portiat);
        }

        AdapterForBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (AdapterForBluetooth == null) {
        } else {
            if (AdapterForBluetooth.isEnabled()) {
                menuSwitcher(Tools_Bluetooth, R.drawable.tool_bluetooth_on);
            } else {
                menuSwitcher(Tools_Bluetooth, R.drawable.tool_bluetooth_off);
            }
        }


        if (brightness > 20) {
            menuSwitcher(Tools_Brightness, R.drawable.tool_brightness_on);
        } else {
            menuSwitcher(Tools_Brightness, R.drawable.tool_brightness_off);
        }


        if (timeout == 10000) {
            menuSwitcher(Tools_Timeout, R.drawable.tool_timeout_ten);
        } else if (timeout == 20000) {
            menuSwitcher(Tools_Timeout, R.drawable.tool_timeout_twenty);
        } else if (timeout == 30000) {
            menuSwitcher(Tools_Timeout, R.drawable.tool_timeout_thirty);
        } else if (timeout == 40000) {
            menuSwitcher(Tools_Timeout, R.drawable.tool_timeout_fourty);
        } else {
            menuSwitcher(Tools_Timeout, R.drawable.tool_timeout_fourty);
            setTimeout(3);
            timeout = 40000;
        }

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        switch (am != null ? am.getRingerMode() : -1) {
            case RINGER_MODE_SILENT:
                menuSwitcher(Tools_Mode, R.drawable.tool_profile_silent);
                profile = RINGER_MODE_SILENT;
                break;
            case RINGER_MODE_VIBRATE:
                menuSwitcher(Tools_Mode, R.drawable.tool_profile_vibrate);
                profile = RINGER_MODE_VIBRATE;
                break;
            case RINGER_MODE_NORMAL:
                menuSwitcher(Tools_Mode, R.drawable.tool_profile_normal);
                profile = RINGER_MODE_NORMAL;
                break;
        }

    }

    private void menuSwitcher(ImageView imageView, @DrawableRes int res) {
        try {
            GlideApp
                    .with(this)
                    .load(res)
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Appodeal.onResume(this, Appodeal.MREC);
        Appodeal.onResume(this,Appodeal.BANNER_BOTTOM);
        Appodeal.onResume(this,Appodeal.INTERSTITIAL);
        LocalBroadcastManager.getInstance(this).registerReceiver(batteryInfoReceiver, new IntentFilter(ChargingService.ACTION_BROADCAST));

        CheckIntentToolsOnOrOff();
        if (hasePermissions()) {
            try {
                makeSettings();
                CheckOnAndOff();
            } catch (Settings.SettingNotFoundException e) {
                Log.e("Error", "Cannot access system brightness");
                e.printStackTrace();
            }
        }
    }

    private boolean hasePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.System.canWrite(this);
        }
        return true;
    }

    //Our danger stuff... Do if granted
    private void makeSettings() throws Settings.SettingNotFoundException {
        // Do stuff here
        Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        brightness = Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS);
        rotate = Settings.System.getInt(cResolver, Settings.System.ACCELEROMETER_ROTATION);
        timeout = Settings.System.getInt(cResolver, Settings.System.SCREEN_OFF_TIMEOUT);

        if (timeout > 40000) {
            setTimeout(3);
            timeout = 40000;
        }
        Log.e("timeout", "" + timeout);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {


            case R.id.tool_wifi:
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager != null) {
                    if (wifiManager.isWifiEnabled()) {
                        wifiManager.setWifiEnabled(false);
                        menuSwitcher(Tools_WiFi, R.drawable.tool_wifi_off);
                    } else {
                        wifiManager.setWifiEnabled(true);
                        menuSwitcher(Tools_WiFi, R.drawable.tool_wifi_on);
                    }
                }
                break;

            case R.id.tool_rotate:
                if (grantIfNotGranted()) {
                    if (rotate == 1) {
                        Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, rotate);
                        menuSwitcher(Tools_Rotate, R.drawable.tool_rotate_autorotate);
                        rotate = 0;
                    } else {
                        Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, rotate);
                        menuSwitcher(Tools_Rotate, R.drawable.tool_rotate_portiat);
                        rotate = 1;
                    }
                }
                break;


            case R.id.tool_mode:

                am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                int mode = (am == null) ? -1 : am.getRingerMode();

                switch (mode) {
                    case RINGER_MODE_SILENT:
                        menuSwitcher(Tools_Mode, R.drawable.tool_profile_vibrate);
                        am.setRingerMode(RINGER_MODE_VIBRATE);
                        break;
                    case RINGER_MODE_VIBRATE:
                        menuSwitcher(Tools_Mode, R.drawable.tool_profile_normal);
                        am.setRingerMode(RINGER_MODE_NORMAL);
                        break;
                    case RINGER_MODE_NORMAL:
                        menuSwitcher(Tools_Mode, R.drawable.tool_profile_silent);
                        am.setRingerMode(RINGER_MODE_SILENT);
                        break;

                    default:
                        break;
                }


                break;

            case R.id.tool_bluetooth:

                AdapterForBluetooth = BluetoothAdapter.getDefaultAdapter();
                if (AdapterForBluetooth == null) {
                } else {
                    if (AdapterForBluetooth.isEnabled()) {
                        menuSwitcher(Tools_Bluetooth, R.drawable.tool_bluetooth_off);
                        AdapterForBluetooth.disable();
                    } else {
                        menuSwitcher(Tools_Bluetooth, R.drawable.tool_bluetooth_on);
                        AdapterForBluetooth.enable();
                    }
                }
                break;

            case R.id.tool_brightness:
                if (grantIfNotGranted()) {
                    if (brightness > 20) {
                        Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, 20);
                        WindowManager.LayoutParams layoutpars = window.getAttributes();
                        layoutpars.screenBrightness = 20;
                        window.setAttributes(layoutpars);
                        menuSwitcher(Tools_Brightness, R.drawable.tool_brightness_off);
                        brightness = 20;
                    } else {
                        Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, 254);
                        WindowManager.LayoutParams layoutpars = window.getAttributes();
                        layoutpars.screenBrightness = 254;
                        window.setAttributes(layoutpars);
                        brightness = 254;
                        menuSwitcher(Tools_Brightness, R.drawable.tool_brightness_on);
                    }
                }


                break;


            case R.id.batteryDetail:

                i = new Intent(getApplicationContext(), BatteryDetailActivity.class);
                startActivity(i);

                break;

            case R.id.PowerSavingMode:
                if (grantIfNotGranted()) {
                    StartPowerSavingMode();
                }
                break;

            case R.id.tool_timeout:
                if (grantIfNotGranted()) {
                    if (timeout == 10000) {
                        menuSwitcher(Tools_Timeout, R.drawable.tool_timeout_twenty);
                        setTimeout(1);
                        timeout = 20000;
                    } else if (timeout == 20000) {
                        menuSwitcher(Tools_Timeout, R.drawable.tool_timeout_thirty);
                        setTimeout(2);
                        timeout = 30000;
                    } else if (timeout == 30000) {
                        menuSwitcher(Tools_Timeout, R.drawable.tool_timeout_fourty);
                        setTimeout(3);
                        timeout = 40000;
                    } else if (timeout == 40000) {
                        menuSwitcher(Tools_Timeout, R.drawable.tool_timeout_ten);
                        setTimeout(0);
                        timeout = 10000;
                    } else {
                        menuSwitcher(Tools_Timeout, R.drawable.tool_timeout_fourty);
                        setTimeout(3);
                        timeout = 40000;
                    }
                }
                break;

            case R.id.CardViewShare:

                i = new Intent();
                i.setAction(Intent.ACTION_SEND);
                i.setType("text/plain");
                final String text = "Check out "
                        + getResources().getString(R.string.app_name)
                        + ", the free app for save your battery with " + getResources().getString(R.string.app_name) + ". https://play.google.com/store/apps/details?id="
                        + getPackageName();
                i.putExtra(Intent.EXTRA_TEXT, text);
                Intent sender = Intent.createChooser(i, "Share " + getResources().getString(R.string.app_name));
                startActivity(sender);

                break;

            case R.id.btn_feedbak:

                DisplayMetrics displaymetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int height = displaymetrics.heightPixels;
                int width = displaymetrics.widthPixels;

                PackageManager manager = getApplicationContext()
                        .getPackageManager();
                PackageInfo info = null;
                try {
                    info = manager.getPackageInfo(getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                String version = (info == null) ? "" : info.versionName;

                i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.developer_email)});
                i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name) + "Version =" + version);
                i.putExtra(Intent.EXTRA_TEXT,
                        "\n" + " Device :" + getDeviceName() +
                                "\n" + " SystemVersion:" + Build.VERSION.SDK_INT +
                                "\n" + " Display Height  :" + height + "px" +
                                "\n" + " Display Width  :" + width + "px" +
                                "\n\n" + " Please write your problem to us we will try our best to solve it .." +
                                "\n");

                startActivity(Intent.createChooser(i, "Send Email"));

                break;

            case R.id.btn_ratenow:
                rateUs(this);
                break;

            case R.id.AlertLout:
                i = new Intent(getApplicationContext(), CloseAllTools.class);
                i.putExtra("SetValue", 0);
                startActivity(i);
                break;


        }

    }

    private boolean grantIfNotGranted() {
        if (hasePermissions()) {
            return true;
        }
        requestPerms();
        return true;
    }

    public void CheckIntentToolsOnOrOff() {

        Issue = 0;

        if (isMobileDataEnabled(this)) {
            Issue = Issue + 1;
        }


        LocationManager ManagerForLocation = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean statusOfLocation = ManagerForLocation != null && ManagerForLocation.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (statusOfLocation) {
            Issue = Issue + 1;
        }

        if (!isAirplaneModeOn(getApplicationContext())) {
            Issue = Issue + 1;
        }

        mAlertText.setText(String.valueOf(Issue));

        if (Issue == 0) {
            AlertLout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        // Bind to LocalService
        Intent intent = new Intent(this, ChargingService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    //Call Only if Write permission grnted
    public void StartPowerSavingMode() {
        List<ApplicationInfo> packages;
        PackageManager pm;
        pm = getPackageManager();
        packages = pm.getInstalledApplications(0);

        ActivityManager mActivityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        //  String myPackage = getApplicationContext().getPackageName();
        for (ApplicationInfo packageInfo : packages) {
            Log.e("pakages", packages + "");
            if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) continue;
            //if (packageInfo.packageName.equals(myPackage)) continue;
            if (nonNull(mActivityManager)) {
                mActivityManager.killBackgroundProcesses(packageInfo.packageName);
            }
        }

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            wifiManager.setWifiEnabled(false);
            menuSwitcher(Tools_WiFi, R.drawable.tool_wifi_off);
        }

        AdapterForBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (AdapterForBluetooth == null) {
        } else {
            if (AdapterForBluetooth.isEnabled()) {
                AdapterForBluetooth.disable();
                menuSwitcher(Tools_Bluetooth, R.drawable.tool_bluetooth_off);
            }
        }


        if (brightness > 20) {
            Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, 20);
            WindowManager.LayoutParams layoutpars = window.getAttributes();
            layoutpars.screenBrightness = 20;
            window.setAttributes(layoutpars);
            menuSwitcher(Tools_Brightness, R.drawable.tool_brightness_off);
            brightness = 20;
        }

        Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0); //0 means off, 1 means on
        menuSwitcher(Tools_Rotate, R.drawable.tool_rotate_portiat);

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            am.setRingerMode(RINGER_MODE_SILENT);
            menuSwitcher(Tools_Mode, R.drawable.tool_profile_silent);
        }


        setTimeout(0);
        timeout = 10000;
        menuSwitcher(Tools_Timeout, R.drawable.tool_timeout_ten);

        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fadein);


        PowerSavingMode.startAnimation(fadeInAnimation);
        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                PowerSavingMode.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private boolean nonNull(Object object) {
        return object != null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                break;

            case R.id.action_setting:
                i = new Intent(getApplicationContext(), SettingPrefrence.class);
                startActivity(i);
                break;

            case R.id.action_about:
                i = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(i);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }

    //Call Only if Write permission granted
    private void setTimeout(int screenOffTimeout) {
        int time;
        switch (screenOffTimeout) {
            case 0:
                time = 10000;
                break;
            case 1:
                time = 20000;
                break;
            case 2:
                time = 30000;
                break;
            case 3:
                time = 40000;
                break;
            default:
                time = -1;
        }
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, time);
    }


    @Override
    public void onBackPressed() {
        exit_dialog.show();
    }

    public void testMetod(){
        Log.e("TEG","test");
    }

}
