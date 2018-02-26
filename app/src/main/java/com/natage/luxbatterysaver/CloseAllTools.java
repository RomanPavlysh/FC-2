package com.natage.luxbatterysaver;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appodeal.ads.Appodeal;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.lang.reflect.Method;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.natage.luxbatterysaver.SettingsUtils.isAirplaneModeOn;
import static com.natage.luxbatterysaver.SettingsUtils.isMobileDataEnabled;

public class CloseAllTools extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.tool_bar)
    Toolbar toolbar;
    LocationManager ManagerForLocation;
    Boolean statusOfLocation;
    Integer SetValue;

    @BindView(R.id.LocationCardView)
    CardView LocationCardView;
    @BindView(R.id.AroplaneCardView)
    CardView AroplaneCardView;
    @BindView(R.id.MoblieDataCardView)
    CardView MoblieDataCardView;

    @BindView(R.id.LocationTurnOff)
    Button LocationTurnOff;
    @BindView(R.id.AroplaneTurnOn)
    Button AroplaneTurnOn;
    @BindView(R.id.MoblieDataTurnOff)
    Button MoblieDataTurnOff;

    @BindView(R.id.txtNoMoreIssue)
    TextView txtNoMoreIssue;

    @BindView(R.id.TxtTitleLocation)
    TextView TxtTitleLocation;
    @BindView(R.id.txtDicLocation)
    TextView txtDicLocation;
    @BindView(R.id.TxtTitleAroplane)
    TextView TxtTitleAroplane;
    @BindView(R.id.txtDicAroplane)
    TextView txtDicAroplane;
    @BindView(R.id.TxtTitleMobileData)
    TextView TxtTitleMobileData;
    @BindView(R.id.txtDicMobileData)
    TextView txtDicMobileData;
    @BindView(R.id.loutIssues)
    RelativeLayout loutIssues;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Apodeal
        String appKey = getResources().getString(R.string.appo_key);
        Appodeal.setBannerViewId(R.id.appodealBannerViewCloseTools);
        Appodeal.initialize(this, appKey, Appodeal.BANNER_TOP);
        Appodeal.show(this, Appodeal.BANNER);


        SetValue = getIntent().getIntExtra("SetValue", 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        setContentView(R.layout.a_close_all_tools);
        ButterKnife.bind(this);

        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            if (SetValue == 0) {
                getSupportActionBar().setTitle("Battery Saver");
            } else {
                getSupportActionBar().setTitle("Battery Charger");
            }
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        LocationCardView.setOnClickListener(this);
        AroplaneCardView.setOnClickListener(this);
        MoblieDataCardView.setOnClickListener(this);
        LocationTurnOff.setOnClickListener(this);
        AroplaneTurnOn.setOnClickListener(this);
        MoblieDataTurnOff.setOnClickListener(this);


        SetCarViewColor();

        CheckWhatOn();


        if (SetValue == 0) {
            TxtTitleLocation.setText("2x Battery Saver");
            txtDicLocation.setText("Disable location service and your battery will save 2x");
            TxtTitleAroplane.setText("3x Battery Saver");
            txtDicAroplane.setText("Enable airplane mode and your battery will save 3x");
            TxtTitleMobileData.setText("2x Battery Saver");
            txtDicMobileData.setText("Disable mobile data and your battery will save 2x");
        } else {
            TxtTitleLocation.setText("2x Fast Charger");
            txtDicLocation.setText("Disable location service and your battery will charge 2x faster");
            TxtTitleAroplane.setText("3x Fast Charger");
            txtDicAroplane.setText("Enable airplane mode and your battery will charge 3x faster");
            TxtTitleMobileData.setText("2x Fast Charger");
            txtDicMobileData.setText("Disable mobile data and your battery will charge 2x faster");
        }

//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);
//        mAdView.setAdListener(new AdListener() {
//            @Override
//            public void onAdLoaded() {
//                super.onAdLoaded();
//                mAdView.setVisibility(View.VISIBLE);
//            }
//        });

    }

    private void SetCarViewColor() {

        LocationCardView.setCardBackgroundColor(getApplicationContext().getResources().getColor(R.color.white));
        AroplaneCardView.setCardBackgroundColor(getApplicationContext().getResources().getColor(R.color.white));
        MoblieDataCardView.setCardBackgroundColor(getApplicationContext().getResources().getColor(R.color.white));
        LocationCardView.setCardElevation(0);
        AroplaneCardView.setCardElevation(0);
        MoblieDataCardView.setCardElevation(0);

    }

    private void CheckWhatOn() {

        if (!isMobileDataEnabled(this)) {
            MoblieDataCardView.setVisibility(View.GONE);
        }

        ManagerForLocation = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ManagerForLocation != null) {
            statusOfLocation = ManagerForLocation.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        if (!statusOfLocation) {
            LocationCardView.setVisibility(View.GONE);
        }

        if (isAirplaneModeOn(getApplicationContext())) {
            AroplaneCardView.setVisibility(View.GONE);
        }

        if (LocationCardView.getVisibility() == View.GONE && AroplaneCardView.getVisibility() == View.GONE && MoblieDataCardView.getVisibility() == View.GONE) {
            txtNoMoreIssue.setVisibility(View.VISIBLE);
            loutIssues.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.LocationCardView:

                SetUpLocationIntent();

                break;

            case R.id.AroplaneCardView:

                SetUpAroplaneIntent();

                break;

            case R.id.MoblieDataCardView:

                SetUpMobileDataIntent();

                break;

            case R.id.LocationTurnOff:

                SetUpLocationIntent();

                break;

            case R.id.AroplaneTurnOn:

                SetUpAroplaneIntent();

                break;

            case R.id.MoblieDataTurnOff:

                SetUpMobileDataIntent();

                break;
        }
    }

    private void SetUpMobileDataIntent() {

        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    private void SetUpAroplaneIntent() {
        Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
        startActivity(intent);
    }

    private void SetUpLocationIntent() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        CheckWhatOn();
        Appodeal.onResume(this, Appodeal.BANNER);
    }

    @Override
    public void onPause() {
        super.onPause();
        CheckWhatOn();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                break;


            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }
}



