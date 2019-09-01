package com.groupnamenotfoundexception.wakeupcall.app.alarm;


import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import com.groupnamenotfoundexception.wakeupcall.app.R;
import com.groupnamenotfoundexception.wakeupcall.app.services.BeforeSleepManager;
import com.groupnamenotfoundexception.wakeupcall.app.services.WakingStateChecker;

/**
 * This {@code IntentService} does the app's actual work.
 * {@code SampleAlarmReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class Scheduler extends IntentService {
    public Scheduler() {
        super("SchedulingService");
    }

    public static final String TAG = "Scheduling Demo";
    // An ID used to post the notification.
    public static final int NOTIFICATION_ID = 1;
    // The string the app searches for in the Google home page content. If the app finds
    // the string, it indicates the presence of a doodle.
    private NotificationManager mNotificationManager;

    @Override
    protected void onHandleIntent(Intent intent) {
        int alarmType = intent.getExtras().getInt("Alarm State");
        int alarmIndex = intent.getExtras().getInt("Alarm Index");

        SharedPreferences pref = getSharedPreferences(getString(R.string.alarm_prefix) + alarmIndex, Context.MODE_PRIVATE);
        boolean isDeleted = pref.getBoolean(getString(R.string.is_deleted), false);
        boolean isEnabled = pref.getBoolean(getString(R.string.is_enabled),true);

        if(isEnabled && !isDeleted) {
            Log.i("ALARM:", String.valueOf(alarmType));

            if (alarmType == Receiver.BEFORE_SLEEP) {
                Intent beforeSleep = new Intent(this, BeforeSleepManager.class);
                beforeSleep.putExtra("START?", false);
                beforeSleep.putExtra("Alarm Index", alarmIndex);
                startService(beforeSleep);
                new Receiver().setAlarm(this, alarmIndex, Receiver.WAKING_STATE);
            } else if (alarmType == Receiver.WAKING_STATE) {
                Intent wakingChecker = new Intent(this, WakingStateChecker.class);
                wakingChecker.putExtra("Alarm Index", alarmIndex);
                startService(wakingChecker);
                new Receiver().setAlarm(this, alarmIndex, Receiver.THE_ALARM);
            } else if (alarmType == Receiver.THE_ALARM) {
                Intent advancedAlarm = new Intent(this, AdvancedAlarm.class);
                intent.putExtra("Alarm Index", alarmIndex);
                startService(advancedAlarm);
            }
        }

        Receiver.completeWakefulIntent(intent);
    }
}
