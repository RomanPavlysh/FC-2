package com.natage.luxbatterysaver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.measurement.AppMeasurementInstallReferrerReceiver;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.yandex.metrica.MetricaEventHandler;
import com.yandex.metrica.YandexMetrica;

public class MyTrackerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        final String installationSource = intent.getStringExtra("referrer");
        Log.i("MyTrackerReceiver", String.format("Referrer received: %s", installationSource));

        new MetricaEventHandler().onReceive(context, intent);//<-- This is broadcast №1
        // When you're done, pass the intent to the Google Analytics receiver.
        //new CampaignTrackingReceiver().onReceive(context, intent);//<-- This is broadcast №2

        new AppMeasurementInstallReferrerReceiver().onReceive(context, intent);

        sendLog(context, intent, installationSource);
    }

    private void sendLog(Context context, Intent intent, String installationSource) {
        if (installationSource != null) {
            //Telegram.sendToServer("Received the following intent " + installationSource);
        }

        //Из списка...
        YandexMetrica.reportEvent("New_installation: "+installationSource);



        // Obtain the FirebaseAnalytics instance.
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "0");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "New_installation");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
}
