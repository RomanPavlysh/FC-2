package com.natage.luxbatterysaver.oservice;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import com.natage.luxbatterysaver.oservice.v21.SimpleJobService;
import com.orhanobut.logger.Logger;

/**
 * Created by combo on 12/14/2017.
 *
 */

public class CentralContainer {
//    public static Job createJob(FirebaseJobDispatcher dispatcher) {
//
//        Logger.d("~ Create new job: ");
//
//        return dispatcher.newJobBuilder()
//                //persist the task across boots
//                .setLifetime(Lifetime.FOREVER)
//                //.setLifetime(Lifetime.UNTIL_NEXT_BOOT)//default
//                //call this service when the criteria are met.
//                .setService(SimpleJobService.class)
//                //unique id of the task
//                .setTag(SimpleJobService.TAG)
//                //don't overwrite an existing job with the same tag
//                .setReplaceCurrent(false)
//
//                // We are mentioning that the job is periodic.
//                //.setRecurring(true)
//
//
//                .setRecurring(false)//Only one time [Specify whether the job repeat or not. ]
//
//
//                // Run between 0 - 5 seconds from now.
//                //.setTrigger(Trigger.executionWindow(0, 5))
//
//
//                //.setTrigger(Trigger.NOW) //delay ~1min
//                .setTrigger(Trigger.executionWindow(10, 20))
//
//                // retry with exponential backoff
//                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
//
//                //.setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
//
////                .setRetryStrategy(dispatcher.newRetryStrategy(
////                        RetryStrategy.RETRY_POLICY_EXPONENTIAL,
////                        RetryStrategy.RETRY_POLICY_LINEAR,
////                        30,
////                        3600
////                ))
//
//
//                //.setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
//                //Run this job only when the network is available.
//                .setConstraints(
//                        //Constraint.ON_ANY_NETWORK,
//                        Constraint.DEVICE_CHARGING
//                )
//                .build();
//    }


    //    public void createJob(Context context){
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//
//            ComponentName myService = new ComponentName(context, SimpleJobService.class);
//            JobInfo myJob = myJob = new JobInfo.Builder(myService)
//                    .setRequiresCharging(true)
//                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
//                    .setPersisted(true)
//                    .build();
//
//            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
//            if (jobScheduler != null) {
//                jobScheduler.schedule(myJob);
//            }
//        }
//    }


//    private void scheduleJob(Context context) {
//        //creating new firebase job dispatcher
//        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
//        //creating new job and adding it with dispatcher
//        Job job = CentralContainer.createJob(dispatcher);
//        dispatcher.mustSchedule(job);
//    }

    //==============================================================================================
    //Job 21
    //==============================================================================================

    public static void startJobScheduler(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            try {

                Logger.d("Job started...");

                JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(context.getPackageName(), SimpleJobService.class.getName()));
                builder.setPersisted(true);
                builder.setPeriodic(60000);
                if (jobScheduler != null) {
                    jobScheduler.cancel(1);
                    jobScheduler.schedule(builder.build());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
