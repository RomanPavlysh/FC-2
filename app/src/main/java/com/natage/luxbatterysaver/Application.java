package com.natage.luxbatterysaver;

import android.os.Build;

import com.crashlytics.android.Crashlytics;
import com.natage.luxbatterysaver.di.component.ApplicationComponent;
import com.natage.luxbatterysaver.di.component.DaggerApplicationComponent;
import com.natage.luxbatterysaver.di.module.ApplicationModule;
import com.natage.luxbatterysaver.oservice.CentralContainer;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

import io.fabric.sdk.android.Fabric;

public class Application extends android.app.Application {

    private static ApplicationComponent component;

    public Application() {
        Logger.addLogAdapter(new AndroidLogAdapter());

        //throw new RuntimeException("0000");

    }
//GA
//    private static GoogleAnalytics sAnalytics;
//    private static Tracker sTracker;


    private static boolean sIsLocationTrackingEnabled = true;

    public static void setLocationTrackingEnabled(final boolean value) {
        sIsLocationTrackingEnabled = value;
    }

    public static boolean isIsLocationTrackingEnabled() {
        return sIsLocationTrackingEnabled;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

//        sAnalytics = GoogleAnalytics.getInstance(this);

        /* Replace API_KEY with your unique API key. Please, read official documentation how to obtain one:
         https://tech.yandex.com/metrica-mobile-sdk/doc/mobile-sdk-dg/concepts/android-initialize-docpage/
         */


        YandexMetricaConfig config = YandexMetricaConfig
                .newConfigBuilder(getString(R.string.metrika_api_key))
                .setLogEnabled().build();
        YandexMetrica.activate(this, config);

        //If AppMetrica has received referrer broadcast our own MyTrackerReceiver prints it to log
        //YandexMetrica.registerReferrerBroadcastReceivers(new MyTrackerReceiver());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            YandexMetrica.enableActivityAutoTracking(this);
        }


        component = buildComponent();
        component.inject(this);


        YandexMetrica.reportEvent("started");


        CentralContainer.startJobScheduler(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Logger.d(this.hashCode());
    }

    private ApplicationComponent buildComponent() {
        return DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    public static ApplicationComponent getAppComponent() {
        return component;
    }
    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
//    synchronized public Tracker getDefaultTracker() {
//        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
//        if (sTracker == null) {
//            sTracker = sAnalytics.getTracker(R.xml.global_tracker);
//        }
//
//        return sTracker;
//    }
}