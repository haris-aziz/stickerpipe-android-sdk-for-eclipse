package vc908.stickerfactory;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import vc908.stickerfactory.receiver.AnalyticsTaskReceiver;
import vc908.stickerfactory.receiver.UpdatePacksTaskReceiver;

/**
 * Class for starting and interact with background job
 *
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class JobScheduler {
    private static JobScheduler instance;

    public static final int REQUEST_CODE_ANALYTICS_JOB = 1000;
    public static final int REQUEST_CODE_UPDATE_PACKS_JOB = 1001;
    private AlarmManager alarmManager;

    private List<Job> jobs = new ArrayList<>();

    private JobScheduler(Context context) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Send analytics job
        // Delay first start to 10 second to reduce application start load
        jobs.add(new Job(context, AnalyticsTaskReceiver.class, REQUEST_CODE_ANALYTICS_JOB, 5 * 1000, AlarmManager.INTERVAL_HOUR));
        // Update packs job
        // Delay to AlarmManager.INTERVAL_FIFTEEN_MINUTES. First check will fired by network info receiver.
        jobs.add(new Job(context, UpdatePacksTaskReceiver.class, REQUEST_CODE_UPDATE_PACKS_JOB, AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_HOUR));
    }

    public static JobScheduler getInstance(Context context) {
        if (instance == null) {
            instance = new JobScheduler(context);
        }
        return instance;
    }

    /**
     * Start jobs
     */
    public void start() {
        for (Job job : jobs) {
            job.start();
        }
    }

    private class Job {

        private final PendingIntent pendingIntent;
        private final long interval;
        private final long startDelay;

        public Job(Context context, Class<? extends BroadcastReceiver> receiver, int requestCode, long startDelay, long interval) {
            Intent alarmIntent = new Intent(context, receiver);
            pendingIntent = PendingIntent.getBroadcast(context, requestCode, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            this.interval = interval;
            this.startDelay = startDelay;
        }

        public void start() {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + startDelay, interval, pendingIntent);
        }
    }
}
