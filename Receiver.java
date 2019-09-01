package com.groupnamenotfoundexception.wakeupcall.app.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import com.groupnamenotfoundexception.wakeupcall.app.R;

import java.util.Calendar;

/**
 * When the alarm fires, this WakefulBroadcastReceiver receives the broadcast Intent
 * and then starts the IntentService {@code SampleSchedulingService} to do some work.
 */
public class Receiver extends WakefulBroadcastReceiver {
    public final static int BEFORE_SLEEP = 0;
    public final static int WAKING_STATE  = 1;
    public final static int THE_ALARM = 2;

    // The app's AlarmManager, which provides access to the system alarm services.
    private AlarmManager alarmMgr;

    @Override
    public void onReceive(Context context, Intent intent) {
        int alarmType = intent.getExtras().getInt("Alarm State");
        int alarmIndex = intent.getExtras().getInt("Alarm Index");

        Intent service = new Intent(context, Scheduler.class);
        service.putExtra("Alarm State", alarmType);
        service.putExtra("Alarm Index", alarmIndex);

        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, service);
    }
    /**
     * Sets a repeating alarm that runs once a day at approximately 8:30 a.m. When the
     * alarm fires, the app broadcasts an Intent to this WakefulBroadcastReceiver.
     * @param context
     */
    public void setAlarm(Context context, int alarmIndex, int alarmType) {
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        cancelAlarm(context,alarmIndex);
        Intent intent = new Intent(context, Receiver.class);
        intent.putExtra("Alarm State",alarmType);
        intent.putExtra("Alarm Index",alarmIndex);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context,alarmIndex,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        SharedPreferences pref = context.getSharedPreferences(context.getString(R.string.alarm_prefix) + alarmIndex, Context.MODE_PRIVATE);
        int hour = pref.getInt(context.getString(R.string.hour), -1);
        int min = pref.getInt(context.getString(R.string.minute),-1);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        if(alarmType == BEFORE_SLEEP){
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, min - 5);
            calendar.set(Calendar.SECOND,0);
        }else if(alarmType == WAKING_STATE){
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, min - 1);
            calendar.set(Calendar.SECOND,0);
        }else if(alarmType == THE_ALARM){
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, min);
            calendar.set(Calendar.SECOND,0);
        }

//        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
//                calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
        alarmMgr.setExact(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), alarmIntent);
        // Enable {@code SampleBootReceiver} to automatically restart the alarm when the
        // device is rebooted.
        ComponentName receiver = new ComponentName(context, Restarter.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    /**
     * Cancels the alarm.
     * @param context
     */
    public void cancelAlarm(Context context, int alarmIndex) {
        // If the alarm has been set, cancel it.
        if (alarmMgr!= null) {
            Intent intent = new Intent(context, Receiver.class);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context,alarmIndex,intent,PendingIntent.FLAG_CANCEL_CURRENT);
            alarmMgr.cancel(alarmIntent);
        }
    }
}
