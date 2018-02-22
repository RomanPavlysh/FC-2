package com.natage.luxbatterysaver.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.natage.luxbatterysaver.oservice.CentralContainer;

/**
 * Created by combo on 12/21/2017.
 * */

public abstract class BaseActivity extends AppCompatActivity {

    protected AlertDialog.Builder writesetting_dialog;
    protected AlertDialog.Builder donotdisterb_dialog;


    protected void setupDialog() {
        //Grand permission dialog
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            writesetting_dialog = new android.support.v7.app.AlertDialog.Builder(BaseActivity.this);
            writesetting_dialog.setTitle("Important!");
            writesetting_dialog.setCancelable(false);
            writesetting_dialog.setMessage("Need write setting permission to set screen brightness, screen timeout, screen rotation, sound profile.");
            writesetting_dialog.setPositiveButton("OK",
                    (dialog, whichButton) -> {


                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);


//                        intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
//                        intent.setData(Uri.parse("package:" + getPackageName()));
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent);
                    });


            donotdisterb_dialog = new android.support.v7.app.AlertDialog.Builder(BaseActivity.this);
            donotdisterb_dialog.setTitle("Important!");
            donotdisterb_dialog.setCancelable(false);
            donotdisterb_dialog.setMessage("Need do not disturb permisson for fast charging.");
            donotdisterb_dialog.setPositiveButton("OK",
                    (dialog, whichButton) -> {
                        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                        startActivity(intent);
                    });
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupDialog();


        //scheduleJob(this);
    }



    private void cancelJob(Context context) {

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        //Cancel all the jobs for this package
        dispatcher.cancelAll();
        // Cancel the job for this tag
        dispatcher.cancel("UniqueTagForYourJob");

    }


}
