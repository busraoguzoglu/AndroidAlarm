package com.groupnamenotfoundexception.wakeupcall.app.alarm;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.groupnamenotfoundexception.wakeupcall.app.R;

/**
 * This BroadcastReceiver automatically (re)starts the alarm when the device is
 * rebooted. This receiver is set to be disabled (android:enabled="false") in the
 * application's manifest file. When the user sets the alarm, the receiver is enabled.
 * When the user cancels the alarm, the receiver is disabled, so that rebooting the
 * device will not trigger this receiver.
 */
public class Restarter extends BroadcastReceiver {
    Receiver alarm = new Receiver();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            int checkSize = context.getSharedPreferences(context.getString(R.string.alarms_master),
                    Context.MODE_PRIVATE).getInt(context.getString(R.string.number_of_alarms),0);
            SharedPreferences pref;
            for (int i = 0; i < checkSize; i++) {
                pref = context.getSharedPreferences(context.getString(R.string.alarm_prefix) + i,Context.MODE_PRIVATE);
                if( !pref.getBoolean(context.getString(R.string.is_deleted),true)){
                    alarm.setAlarm(context,pref.getInt(context.getString(R.string.alarm_index),0),Receiver.BEFORE_SLEEP);
                }
            }
        }
    }
}
