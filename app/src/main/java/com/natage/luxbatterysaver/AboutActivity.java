package com.natage.luxbatterysaver;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.natage.luxbatterysaver.SettingsUtils.rateUs;
import static com.natage.luxbatterysaver.utils.Utils.getDeviceName;

public class AboutActivity extends AppCompatActivity {

    @BindView(R.id.about_1)
    CardView about_1;
    @BindView(R.id.CardViewShare)
    CardView mCardViewShare;
    @BindView(R.id.btn_feedbak)
    Button btn_feedback;
    @BindView(R.id.btn_ratenow)
    Button btn_ratenow;
    @BindView(R.id.CardViewRate)
    CardView CardViewRate;


    @BindView(R.id.tool_bar)
    Toolbar toolbar;
    @BindView(R.id.txtRateTitle)
    TextView rateTitle;

    private Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_about);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setInitialConfiguration();
        setScreenElements();

    }

    private void setInitialConfiguration() {
        getSupportActionBar().setTitle("");//About
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void setScreenElements() {
        rateTitle.setText(String.format(getString(R.string.txt_rate_title), getString(R.string.app_name)));

        about_1.setCardBackgroundColor(getApplicationContext().getResources().getColor(R.color.white));
        mCardViewShare.setCardBackgroundColor(getApplicationContext().getResources().getColor(R.color.white));
        CardViewRate.setCardBackgroundColor(getApplicationContext().getResources().getColor(R.color.white));
        about_1.setCardElevation(0);
        CardViewRate.setCardElevation(0);
        mCardViewShare.setCardElevation(0);

        about_1.setOnClickListener(view -> {
            i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("market://search?q=pub:" + getResources().getString(R.string.developer_name))); // ADD YOUR DEVELOPER NAME HERE USE + FOR SPACE
            if (getPackageManager().queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY).size() >= 1) {
                startActivity(i);
            }
        });

        btn_ratenow.setOnClickListener(v -> {
            // TODO Auto-generated method stub
            rateUs(this);
        });

        btn_feedback.setOnClickListener(v -> {
            // TODO Auto-generated method stub

            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int height = displaymetrics.heightPixels;
            int width = displaymetrics.widthPixels;
            PackageManager manager = getApplicationContext().getPackageManager();
            PackageInfo info = null;
            try {
                info = manager.getPackageInfo(getPackageName(), 0);
            } catch (NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String version = info.versionName;

            i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.developer_email)}); // PUT YOUR EMAIL HERE
            i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name) + version);
            i.putExtra(Intent.EXTRA_TEXT,
                    "\n" + " Device :" + getDeviceName() +
                            "\n" + " SystemVersion:" + Build.VERSION.SDK_INT +
                            "\n" + " Display Height  :" + height + "px" +
                            "\n" + " Display Width  :" + width + "px" +
                            "\n\n" + " Please write your problem to us we will try our best to solve it .." +
                            "\n");

            startActivity(Intent.createChooser(i, "Send Email"));
        });

        mCardViewShare.setOnClickListener(v -> {
            // TODO Auto-generated method stub
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
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
