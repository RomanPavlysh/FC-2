package com.natage.luxbatterysaver;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.natage.luxbatterysaver.helper.Permissions;
import com.natage.luxbatterysaver.utils.BaseActivity;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.fanrunqi.waveprogress.WaveProgressView;

import static android.media.AudioManager.RINGER_MODE_NORMAL;
import static android.media.AudioManager.RINGER_MODE_SILENT;
import static android.media.AudioManager.RINGER_MODE_VIBRATE;
import static com.natage.luxbatterysaver.SettingsUtils.isAirplaneModeOn;
import static com.natage.luxbatterysaver.SettingsUtils.isMobileDataEnabled;
import static com.natage.luxbatterysaver.SettingsUtils.rateUs;
import static com.natage.luxbatterysaver.utils.Utils.getDeviceName;

public class FastCharger extends BaseActivity implements View.OnClickListener {


    public static boolean attached = false;


    @Inject
    Permissions perm;

    @BindView(R.id.tool_bar)
    Toolbar toolbar;

    private BroadcastReceiver batteryLevelReceiver;
    Animation Rotate;
    BluetoothAdapter AdapterForBluetooth;

    private int brightness;
    private int rotate;
    private int timeout;
    private ContentResolver cResolver;
    private Window window;
    private AudioManager am;

    private Intent i;




    private Integer Issue = 0;
    private Integer Profile;

    public FastCharger() {
        Application.getAppComponent().inject(this);
    }


    @BindView(R.id.waveProgressbar)
    WaveProgressView waveProgressView;
    @BindView(R.id.FirstTickleProcess)
    ImageView FirstTickleProcess;
    @BindView(R.id.SecondTickleProcess)
    ImageView SecondTickleProcess;
    @BindView(R.id.ThirdTickleProcess)
    ImageView ThirdTickleProcess;
    @BindView(R.id.tool_wifi)
    ImageView  Tools_WiFi;
    @BindView(R.id.tool_rotate)
    ImageView  Tools_Rotate;
    @BindView(R.id.tool_brightness)
    ImageView  Tools_Brightness;
    @BindView(R.id.tool_bluetooth)
    ImageView  Tools_Bluetooth;
    @BindView(R.id.tool_mode)
    ImageView  Tools_Mode;
    @BindView(R.id.tool_timeout)
    ImageView  Tools_Timeout;
    @BindView(R.id.StartFastCharger)
    Button StartFastCharger;
    @BindView(R.id.main_lout_toolsview)
    RelativeLayout main_lout_toolsview;
    @BindView(R.id.main_lout_tickleview)
    RelativeLayout main_lout_tickleview;
    @BindView(R.id.beforeFullCharge)
    RelativeLayout beforeFullCharge;
    @BindView(R.id.afterFullCharge)
    RelativeLayout afterFullCharge;
    @BindView(R.id.CardViewRate)
    CardView CardViewRate;
    @BindView(R.id.CardViewBattery)
    CardView CardViewBattery;
    @BindView(R.id.CardViewTickleview)
    CardView CardViewTickleview;
    @BindView(R.id.CardViewTools)
    CardView CardViewTools;
    @BindView(R.id.CardViewShare)
    CardView mCardViewShare;
    @BindView(R.id.ratenow)
    RelativeLayout ratenow;
    @BindView(R.id.sharenow)
    RelativeLayout sharenow;
    @BindView(R.id.btn_feedbak)
    Button btn_feedback;
    @BindView(R.id.btn_ratenow)
    Button btn_ratenow;
    @BindView(R.id.AlertLout)
    RelativeLayout AlertLout;
    @BindView(R.id.AlertText)
    TextView mAlertText;
    @BindView(R.id.PowerType)
    TextView PowerType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        attached = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fast_charger);
        ButterKnife.bind(this);


        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }


        cResolver = getContentResolver();

        window = getWindow();
        getBatteryPercentage();
        CheckOnAndOff();
        SetClickListner();
        CheckIntentToolsOnOrOff();
        setColorToCardview();

        StartFastCharger.setVisibility(View.VISIBLE);

        final NativeExpressAdView adView = findViewById(R.id.NativeadView);
        adView.loadAd(new AdRequest.Builder().build());
        adView.setVisibility(View.GONE);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setVisibility(View.VISIBLE);
            }
        });


        Rotate = AnimationUtils.loadAnimation(this, R.anim.rotate_clockwise);

        cResolver = getContentResolver();
        window = getWindow();

        boolean allowed = perm.checkPermissions(this,
                new String[]{android.Manifest.permission.WRITE_SETTINGS});

        if (allowed) {
            try {
                makeSettings();
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


    }

    //Our danger stuff
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


    private void SetClickListner() {
        Tools_WiFi.setOnClickListener(this);
        StartFastCharger.setOnClickListener(this);
        Tools_Mode.setOnClickListener(this);
        Tools_Bluetooth.setOnClickListener(this);
        Tools_Brightness.setOnClickListener(this);
        Tools_Timeout.setOnClickListener(this);
        Tools_Rotate.setOnClickListener(this);
        mCardViewShare.setOnClickListener(this);
        btn_feedback.setOnClickListener(this);
        btn_ratenow.setOnClickListener(this);
        AlertLout.setOnClickListener(this);
    }

    private void setColorToCardview() {
        CardViewRate.setCardBackgroundColor(getApplicationContext().getResources().getColor(R.color.white));
        CardViewBattery.setCardBackgroundColor(getApplicationContext().getResources().getColor(R.color.white));
        CardViewTickleview.setCardBackgroundColor(getApplicationContext().getResources().getColor(R.color.white));
        CardViewTools.setCardBackgroundColor(getApplicationContext().getResources().getColor(R.color.white));
        mCardViewShare.setCardBackgroundColor(getApplicationContext().getResources().getColor(R.color.white));
        CardViewBattery.setCardElevation(0);
        CardViewTools.setCardElevation(0);
        CardViewTickleview.setCardElevation(0);
        CardViewRate.setCardElevation(0);
        mCardViewShare.setCardElevation(0);
    }

    public void CheckIntentToolsOnOrOff() {

        Issue = 0;

        if (isMobileDataEnabled(this)) {
            Issue = Issue + 1;
        }

        LocationManager ManagerForLocation = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean statusOfLocation = false;
        if (ManagerForLocation != null) {
            statusOfLocation = ManagerForLocation.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

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


    private void getBatteryPercentage() {
        batteryLevelReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                    int currentLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    int level = -1;
                    if (currentLevel >= 0 && scale > 0) {
                        level = (currentLevel * 100) / scale;
                        Log.e("%", "" + level);
                    }

                    waveProgressView.setMaxProgress(100);
                    waveProgressView.setCurrent(level, String.valueOf(level) + "%");
                    waveProgressView.setWave(8, 130);
                    waveProgressView.setText("#4D000000", 70);
                    waveProgressView.setWaveColor("#4DFF9800");
                    waveProgressView.setmWaveSpeed(15);

                    FirstTickleProcess.setVisibility(View.GONE);
                    SecondTickleProcess.setVisibility(View.GONE);
                    ThirdTickleProcess.setVisibility(View.GONE);

                    if (level <= 90) {
                        FirstTickleProcess.startAnimation(Rotate);
                        SecondTickleProcess.clearAnimation();
                        ThirdTickleProcess.clearAnimation();
                        FirstTickleProcess.setVisibility(View.VISIBLE);
                        SecondTickleProcess.setVisibility(View.GONE);
                        ThirdTickleProcess.setVisibility(View.GONE);
                    }
                    if (level <= 98 && level >= 91) {
                        SecondTickleProcess.startAnimation(Rotate);
                        FirstTickleProcess.clearAnimation();
                        ThirdTickleProcess.clearAnimation();
                        SecondTickleProcess.setVisibility(View.VISIBLE);
                        FirstTickleProcess.setVisibility(View.GONE);
                        ThirdTickleProcess.setVisibility(View.GONE);
                    }
                    if (level >= 99) {
                        ThirdTickleProcess.startAnimation(Rotate);
                        FirstTickleProcess.clearAnimation();
                        SecondTickleProcess.clearAnimation();
                        ThirdTickleProcess.setVisibility(View.VISIBLE);
                        FirstTickleProcess.setVisibility(View.GONE);
                        SecondTickleProcess.setVisibility(View.GONE);
                    }

                    switch (status) {
                        case BatteryManager.BATTERY_PLUGGED_AC:
                            PowerType.setText("AC");
                            break;
                        case BatteryManager.BATTERY_PLUGGED_USB:
                            PowerType.setText("USB");
                            break;
                        case BatteryManager.BATTERY_STATUS_FULL:
                            ChangeStateFullCharged();
                            break;

                    }
                }
                if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
                    finish();
                }


            }
        };
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        IntentFilter PowerDisconnectFilter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(batteryLevelReceiver, batteryLevelFilter);
        registerReceiver(batteryLevelReceiver, PowerDisconnectFilter);

    }

    private void ChangeStateFullCharged() {
        beforeFullCharge.setVisibility(View.GONE);
        afterFullCharge.setVisibility(View.VISIBLE);


        final Animation slideinforratenow = AnimationUtils.loadAnimation(this, R.anim.slide_in);
        final Animation slideinforsharenow = AnimationUtils.loadAnimation(this, R.anim.slide_in);

        CardViewRate.setVisibility(View.VISIBLE);
        CardViewRate.startAnimation(slideinforratenow);
        slideinforratenow.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                mCardViewShare.setVisibility(View.VISIBLE);
                mCardViewShare.startAnimation(slideinforsharenow);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

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
        int mode = (am == null) ? -1 : am.getRingerMode();

        switch (mode) {
            case RINGER_MODE_SILENT:
                menuSwitcher(Tools_Mode, R.drawable.tool_profile_silent);
                Profile = RINGER_MODE_SILENT;
                break;
            case RINGER_MODE_VIBRATE:
                menuSwitcher(Tools_Mode, R.drawable.tool_profile_vibrate);
                Profile = RINGER_MODE_VIBRATE;
                break;
            case RINGER_MODE_NORMAL:
                menuSwitcher(Tools_Mode, R.drawable.tool_profile_normal);
                Profile = RINGER_MODE_NORMAL;
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckOnAndOff();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CheckOnAndOff();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        CheckOnAndOff();
    }

    @Override
    protected void onDestroy() {

        attached = false;

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            am.setRingerMode(Profile);
        }

        if (batteryLevelReceiver != null) {
            unregisterReceiver(batteryLevelReceiver);
        }

        setTimeout(3);
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.StartFastCharger:

                //  StartFastCharger.setVisibility(View.GONE);

                final Animation slide_down = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);

                final Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadeout);


                CardViewTools.startAnimation(slide_down);
                slide_down.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        CardViewTools.setVisibility(View.GONE);
                        CardViewTickleview.setVisibility(View.VISIBLE);
                        CardViewTickleview.startAnimation(slide_up);
                        StartFastChargerMethod();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });


                break;

            case R.id.tool_wifi:
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager != null) {
                    wifiManager.setWifiEnabled(false);
                    menuSwitcher(Tools_WiFi, R.drawable.tool_wifi_off);
                }
                break;

            case R.id.tool_rotate:

                if (rotate == 1 && grantIfNotGranted()) {
                    Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                    menuSwitcher(Tools_Rotate, R.drawable.tool_rotate_portiat);
                    rotate = 1;
                }

                break;


            case R.id.tool_mode:

                am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                switch (am != null ? am.getRingerMode() : -1) {
                    case RINGER_MODE_SILENT:
                        break;
                    case RINGER_MODE_VIBRATE:
                        menuSwitcher(Tools_Mode, R.drawable.tool_profile_silent);
                        am.setRingerMode(RINGER_MODE_SILENT);
                        break;
                    case RINGER_MODE_NORMAL:
                        menuSwitcher(Tools_Mode, R.drawable.tool_profile_silent);
                        am.setRingerMode(RINGER_MODE_SILENT);
                        break;

                    default:
                        menuSwitcher(Tools_Mode, R.drawable.tool_profile_silent);
                        am.setRingerMode(RINGER_MODE_SILENT);
                }


                break;

            case R.id.tool_bluetooth:

                AdapterForBluetooth = BluetoothAdapter.getDefaultAdapter();
                if (AdapterForBluetooth == null) {
                } else {
                    if (AdapterForBluetooth.isEnabled()) {
                        menuSwitcher(Tools_Bluetooth, R.drawable.tool_bluetooth_off);
                        AdapterForBluetooth.disable();
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
                    }
                }
                break;

            case R.id.tool_timeout:

                if (timeout == 10000) {
                    menuSwitcher(Tools_Timeout, R.drawable.tool_timeout_ten);
                    setTimeout(0);
                    timeout = 10000;
                } else {
                    menuSwitcher(Tools_Timeout, R.drawable.tool_timeout_ten);
                    setTimeout(0);
                    timeout = 10000;
                }

                break;

            case R.id.CardViewShare:

                i = new Intent();
                i.setAction(Intent.ACTION_SEND);
                i.setType("text/plain");
                final String text = "Check out "
                        + getResources().getString(R.string.app_name)
                        + ", the free app for save your battery with Battery saver. https://play.google.com/store/apps/details?id="
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
                String version = info != null ? info.versionName : "";

                i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"EMAIL"});  // PUT YOUR EMAIL HERE
                i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name) + version);
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
                i.putExtra("SetValue", 1);
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

    private void requestPerms() {
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

    private boolean hasePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.System.canWrite(this);
        }
        return true;
    }

    private void menuSwitcher(ImageView imageView, @DrawableRes int res) {
        try {
            GlideApp.with(this)
                    .load(res)
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void StartFastChargerMethod() {
        List<ApplicationInfo> packages;
        PackageManager pm;
        pm = getPackageManager();
        //get a list of installed apps.
        packages = pm.getInstalledApplications(0);

        ActivityManager mActivityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        //  String myPackage = getApplicationContext().getPackageName();
        for (ApplicationInfo packageInfo : packages) {
            Log.e("pakages", packages + "");
            if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) continue;
            //if (packageInfo.packageName.equals(myPackage)) continue;
            if (mActivityManager != null) {
                mActivityManager.killBackgroundProcesses(packageInfo.packageName);
            }
        }

        if (brightness > 20) {
            if (hasePermissions()) {
                Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, 20);
                WindowManager.LayoutParams layoutpars = window.getAttributes();
                layoutpars.screenBrightness = 20;
                window.setAttributes(layoutpars);
                menuSwitcher(Tools_Brightness, R.drawable.tool_brightness_off);
                brightness = 20;
            }
        }

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            wifiManager.setWifiEnabled(false);
        }
        menuSwitcher(Tools_WiFi, R.drawable.tool_wifi_off);
        AdapterForBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (AdapterForBluetooth == null) {
        } else {
            if (AdapterForBluetooth.isEnabled()) {

                AdapterForBluetooth.disable();
            }
        }
        if (hasePermissions()) {
            Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
        }
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


        StartFastCharger.startAnimation(fadeInAnimation);
        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                StartFastCharger.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }


    //if granted
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

}
