package com.groupnamenotfoundexception.wakeupcall.app.alarm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import com.groupnamenotfoundexception.wakeupcall.app.R;
import com.groupnamenotfoundexception.wakeupcall.app.activities.AlarmAlertActivity;
import com.groupnamenotfoundexception.wakeupcall.app.services.AsleepModeDataCollector;
import com.groupnamenotfoundexception.wakeupcall.app.services.BeforeSleepManager;
import com.groupnamenotfoundexception.wakeupcall.app.services.WakingStateChecker;

/**
 * Created by Alchemistake on 25/07/15.
 */
public class AdvancedAlarm extends Service {
    int alarmIndex;
    SharedPreferences pref;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int i = super.onStartCommand(intent, flags, startId);

        if (intent != null) {
            alarmIndex = intent.getExtras().getInt("Alarm Index");

            pref = getSharedPreferences(getString(R.string.alarm_prefix) + alarmIndex, Context.MODE_PRIVATE);
            boolean isDeleted = pref.getBoolean(getString(R.string.is_deleted), true);
            boolean isEnabled = pref.getBoolean(getString(R.string.is_enabled), false);

            if (isEnabled && !isDeleted) {
                Log.i("Advanced Alarm", "Ring Ring");


                ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "ALARM").acquire();
                Intent alarmAlert = new Intent(this, AlarmAlertActivity.class);
                alarmAlert.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                alarmAlert.putExtra("Alarm Index", alarmIndex);

                pref.edit().putBoolean(getString(R.string.is_enabled), false).commit();

                stopService(new Intent(this, BeforeSleepManager.class));
                stopService(new Intent(this, AsleepModeDataCollector.class));
                stopService(new Intent(this, WakingStateChecker.class));
                startActivity(alarmAlert);
                stopSelf();
            } else {
                Log.i("Advanced Alarm", "ALARM DISABLED OR DELETED");
            }
        }else
        onDestroy();
        return i;
    }
}
