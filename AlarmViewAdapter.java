package com.groupnamenotfoundexception.wakeupcall.app.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.groupnamenotfoundexception.wakeupcall.app.R;

import java.util.ArrayList;

/**
 * Adapter that provides connection between view and dataset for The Home Fragment
 */
public class AlarmViewAdapter extends RecyclerView.Adapter<AlarmViewAdapter.ViewHolder>{
    /** SUBCLASSES */
    /**
     * RecyclerView's ViewHolder Sub-Class is Abstract so we need this
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }

    /**
     * PROPERTIES
     */

    // Dataset
    private ArrayList<SharedPreferences> dataSet;

    // Context for access
    private Context context;

    public AlarmViewAdapter(Context context) {
        // Takes the access
        this.context = context;

        // Sets the Dataset
        dataSet = new ArrayList<SharedPreferences>();
        changeDataSet();
    }

    /** METHODS */

    /**
     * When there is need to create a view this will be called
     *
     * @param parent ,the part we want to inflate
     * @param viewType ,type of the view
     * @return ViewHolder to show
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alarm_view, parent, false);

        return new ViewHolder(v);
    }

    /**
     * When some ViewHolder is going to be on screen this will be called
     *
     * @param holder ,holder that going to be on the screen
     * @param position ,position of the holder
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // Gets the needed Views
        final View v = holder.itemView, mon = v.findViewById(R.id.mon), tue = v.findViewById(R.id.tue), wed = v.findViewById(R.id.wed),
                thu = v.findViewById(R.id.thu), fri = v.findViewById(R.id.fri), sat = v.findViewById(R.id.sat), sun = v.findViewById(R.id.sun);
        Switch isEnabled = (Switch) v.findViewById(R.id.isEnabled);
        CardView alarmCard = (CardView) v.findViewById(R.id.alarmCard).getParent();
        TextView alarmTime = (TextView) v.findViewById(R.id.alarmViewTime);

        // Sets the repeat days
        mon.setEnabled(dataSet.get(position).getBoolean(context.getString(R.string.monday), false));
        tue.setEnabled(dataSet.get(position).getBoolean(context.getString(R.string.tuesday), false));
        wed.setEnabled(dataSet.get(position).getBoolean(context.getString(R.string.wednesday), false));
        thu.setEnabled(dataSet.get(position).getBoolean(context.getString(R.string.thursday), false));
        fri.setEnabled(dataSet.get(position).getBoolean(context.getString(R.string.friday), false));
        sat.setEnabled(dataSet.get(position).getBoolean(context.getString(R.string.saturday), false));
        sun.setEnabled(dataSet.get(position).getBoolean(context.getString(R.string.sunday), false));

        // Sets the enabled
        isEnabled.setChecked(dataSet.get(position).getBoolean(context.getString(R.string.is_enabled), true));
        alarmCard.setEnabled(dataSet.get(position).getBoolean(context.getString(R.string.is_enabled), true));
        if (alarmCard.isEnabled()) {
            alarmCard.setAlpha(1);
        } else {
            alarmCard.setAlpha((float) 0.3);
        }

        // Sets the time of the alarm
        int hour = dataSet.get(position).getInt(context.getString(R.string.hour), -27);
        int minute = dataSet.get(position).getInt(context.getString(R.string.minute), -27);

        // Format the text
        StringBuffer stringBuffer = new StringBuffer();
        if (hour >= 0 && minute >= 0) {
            if (hour < 10)
                stringBuffer.append("0");
            stringBuffer.append(hour);
            stringBuffer.append(":");
            if (minute < 10)
                stringBuffer.append("0");
            stringBuffer.append(minute);
        } else {
            stringBuffer.append("ERROR");
        }

        // Put the text
        alarmTime.setText(stringBuffer);

        isEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                CardView parent = (CardView) compoundButton.getParent().getParent().getParent();
                Switch isEnabled = (Switch) compoundButton;

                if (!parent.isEnabled()) {
                    parent.setEnabled(true);
                    parent.setAlpha(1);
                    isEnabled.setChecked(true);
                } else {
                    parent.setEnabled(false);
                    parent.setAlpha((float) 0.3);
                    isEnabled.setChecked(false);
                }

                getSharedPreferences(position).edit().putBoolean(context.getString(R.string.is_enabled), parent.isEnabled()).commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    /**
     * If an alarm is deleted this will be called
     * @param position
     */
    public void remove(int position) {
        // Set isDeleted true
        dataSet.get(position).edit().putBoolean("isDeleted",true).commit();
        // Remove from the dataSet
        dataSet.remove(position);
        // Notify the adapter
        notifyItemRemoved(position);
        // Notify the User
        Toast.makeText(context,"Alarm is Deleted Permanently",Toast.LENGTH_SHORT).show();
    }

    /**
     * When dataSet is changed this will be called
     */
    public void changeDataSet() {
        // Clears Data Set
        dataSet.clear();

        // Check Size
        int dataSetCheckSize =
                context.getSharedPreferences(context.getString(R.string.alarms_master),Context.MODE_PRIVATE).getInt(context.getString(R.string.number_of_alarms), 0);

        // Check and add the Alarm to the RecyclerView
        for (int i = 0; i < dataSetCheckSize; i++) {
            if ( !context.getSharedPreferences(context.getString(R.string.alarm_prefix) + i,Context.MODE_PRIVATE).getBoolean(context.getString(R.string.is_deleted),true)){
                dataSet.add(context.getSharedPreferences(context.getString(R.string.alarm_prefix) + i,Context.MODE_PRIVATE));
            }
        }
    }

    /**
     * Gets the alarmIndex to edit it
     * @param position ,position of the view
     * @return alarmIndex
     */
    public int getAlarmIndex(int position){
        return dataSet.get(position).getInt(context.getString(R.string.alarm_index), 0);
    }

    /**
     * Gets the SharedPreferences for access
     * @param position ,postion of the view
     * @return Desired the SharedPreferences
     */
    public SharedPreferences getSharedPreferences(int position) {
        return dataSet.get(position);
    }
}
