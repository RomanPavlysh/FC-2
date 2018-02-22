package com.natage.luxbatterysaver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class BatteryDetailActivity extends AppCompatActivity {

    ListView detailList;
    ListAdepter listAdepter;
    Toolbar toolbar;
    private AdView mAdView;
    String[] detail_name = {"Temperature", "Voltage", "Level", "Technology", "Health"};
    String[] detail_value = new String[6];
    Integer[] detailImage = {R.drawable.temperature, R.drawable.voltage, R.drawable.battery_level, R.drawable.technology, R.drawable.battery_health};
    public BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            detail_value[0] = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10 + Character.toString((char) 176) + " C";
            detail_value[1] = (float) intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / (float) 1000 + Character.toString((char) 176) + " V";
            detail_value[2] = Integer.toString(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0));
            detail_value[3] = intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
            if (detail_value[3].equalsIgnoreCase("")) {
                detail_value[3] = "-";
            }

            int battery_helth = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);

            switch (battery_helth) {
                case 1:
                    detail_value[4] = "UNKNOWN";
                    break;
                case 2:
                    detail_value[4] = "GOOD";
                    break;
                case 3:
                    detail_value[4] = "OVERHEAT";
                    break;
                case 4:
                    detail_value[4] = "DEAD";
                    break;
                case 5:
                    detail_value[4] = "OVER_VOLTAGE";
                    break;
                case 6:
                    detail_value[4] = "UNSPECIFIED_FAILURE";
                    break;
                case 7:
                    detail_value[4] = "COLD";
                    break;

            }

            listAdepter = new ListAdepter(BatteryDetailActivity.this, detail_name, detail_value, detailImage);
            detailList.setAdapter(listAdepter);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        setContentView(R.layout.a_battery_detail);
        toolbar = findViewById(R.id.tool_bar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Battery information");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        detailList = findViewById(R.id.detailList);
        registerReceiver(this.batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdView.setVisibility(View.VISIBLE);
            }
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
}

