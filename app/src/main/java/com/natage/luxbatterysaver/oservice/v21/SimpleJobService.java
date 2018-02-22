package com.natage.luxbatterysaver.oservice.v21;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;

import com.natage.luxbatterysaver.ChargingService;
import com.orhanobut.logger.Logger;

import static com.natage.luxbatterysaver.utils.Utils.isMyServiceRunning;

/**
 * Created by combo on 12/15/2017.
 * */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SimpleJobService extends JobService {

    //cqk
    private Handler handler = new Handler(message -> {
        Logger.d("JobService task running");
        jobFinished((JobParameters) message.obj, true);
        return true;
    });

    public boolean onStartJob(JobParameters jobParameters) {
        Logger.d("jobSchedulerService start job");
        runService(this);
        this.handler.sendMessage(Message.obtain(this.handler, 1, jobParameters));
        return true;
    }

    public boolean onStopJob(JobParameters jobParameters) {
        Logger.d("jobSchedulerService stop job");
        this.handler.removeMessages(1);
        return false;
    }


    public void runService(Context context) {
        Intent serviceIntent = new Intent(context, ChargingService.class);
        boolean working = isMyServiceRunning(ChargingService.class, context);
        if (!working) {
            startService(serviceIntent);
        } else {
            //stopService(serviceIntent);
            Logger.d("Skip run service");
        }
    }
}
