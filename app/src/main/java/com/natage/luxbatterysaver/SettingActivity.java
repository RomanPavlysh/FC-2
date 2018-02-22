package com.natage.luxbatterysaver;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.DisplayMetrics;

import static com.natage.luxbatterysaver.SettingsUtils.rateUs;
import static com.natage.luxbatterysaver.utils.Utils.getDeviceName;


public class SettingActivity extends PreferenceFragment {

    private Intent i;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);

        PackageManager manager = getActivity().getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(getActivity().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        final String version = (info == null) ? "" : info.versionName;

        Preference Licence = findPreference("Licence");
        Licence.setOnPreferenceClickListener(preference -> {
            Intent i = new Intent(getActivity().getApplicationContext(), LicenseActivity.class);
            startActivity(i);
            return true;
        });

        Preference MoreApp = findPreference("MoreApp");
        MoreApp.setOnPreferenceClickListener(preference -> {

            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:" + getString(R.string.developer_name))));// ADD YOUR DEVELOPER NAME HERE USE + FOR SPACE

            return true;
        });

        Preference RateUs = findPreference("RateUs");
        RateUs.setOnPreferenceClickListener(preference -> {
            rateUs(getActivity());
            return true;
        });

        Preference ShareApp = findPreference("ShareApp");
        ShareApp.setOnPreferenceClickListener(preference -> {

            i = new Intent();
            i.setAction(Intent.ACTION_SEND);
            i.setType("text/plain");
            final String text = "Check out "
                    + getResources().getString(R.string.app_name)
                    + ", the free app for save your battery with " + getResources().getString(R.string.app_name) + ". https://play.google.com/store/apps/details?id="
                    + getActivity().getPackageName();
            i.putExtra(Intent.EXTRA_TEXT, text);
            Intent sender = Intent.createChooser(i, "Share " + getResources().getString(R.string.app_name));
            startActivity(sender);

            return true;
        });

        Preference About = findPreference("About");
        About.setOnPreferenceClickListener(preference -> {

            i = new Intent(getActivity(), AboutActivity.class);
            startActivity(i);

            return true;
        });

        Preference FeedBack = findPreference("FeedBack");
        FeedBack.setOnPreferenceClickListener(preference -> {

            DisplayMetrics displaymetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int height = displaymetrics.heightPixels;
            int width = displaymetrics.widthPixels;


            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.developer_email)});
            intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name) + " Version = " + version);
            intent.putExtra(Intent.EXTRA_TEXT,
                    "\n" + " Device :" + getDeviceName() +
                            "\n" + " SystemVersion:" + Build.VERSION.SDK_INT +
                            "\n" + " Display Height  :" + height + "px" +
                            "\n" + " Display Width  :" + width + "px" +
                            "\n\n" + " Please write your problem to us we will try our best to solve it .." +
                            "\n");

            startActivity(Intent.createChooser(intent, "Send Email"));

            return true;
        });


        Preference Update = findPreference("Update");
        Update.setOnPreferenceClickListener(preference -> {
            rateUs(getActivity());
            return true;
        });

        Preference Version = findPreference("Version");
        Version.setSummary(version);
    }


}

