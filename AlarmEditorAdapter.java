package com.groupnamenotfoundexception.wakeupcall.app.adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.groupnamenotfoundexception.wakeupcall.app.R;
import com.groupnamenotfoundexception.wakeupcall.app.activities.AlarmEditorActivity;

/**
 * Adapter that provides connection between view and dataset for the Alarm Editor Activity
 */
public class AlarmEditorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
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

    // Alarm Editor Activity For Access
    AlarmEditorActivity alarmEditorActivity;

    /** CONSTRUCTORS */
    public AlarmEditorAdapter(AlarmEditorActivity alarmEditorActivity) {
        // Hands Access
        this.alarmEditorActivity = alarmEditorActivity;
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
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Check the view type
        // It is an Alarm Time
        if (viewType == 0) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm_editor_alarm_time, parent, false));
        }
        // It is Game Settings
        else if (viewType == 1) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm_editor_game_settings, parent, false));
        }
        // It is Not Awake SMS
        else if (viewType == 2) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm_editor_not_awake_sms, parent, false));
        }
        else if (viewType == 3) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm_editor_after_alarm_app, parent, false));
        }

        // If there is something wrong return null
        return null;
    }

    /**
     * When some ViewHolder is going to be on screen this will be called
     *
     * @param holder ,holder that going to be on the screen
     * @param position ,position of the holder
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Check the view type via Position
        // It is an Alarm Time
        if (position == 0) {
            // Sets up the values in the view from the SharedPreferences

            // Give acces of The TextView to Alarm Editor Activity
            TextView alarmTime = (TextView) holder.itemView.findViewById(R.id.alarmTime);
            alarmEditorActivity.setAlarmTime(alarmTime);

            // Get the hour and the minute from SharedPreferences
            int hour = alarmEditorActivity.getPref().getInt(alarmEditorActivity.getString(R.string.hour), -27);
            int minute = alarmEditorActivity.getPref().getInt(alarmEditorActivity.getString(R.string.minute), -27);

            // Format the Time
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
                stringBuffer.append("Please select a time");
            }
            // Puts to formatted text to the alarmTime
            alarmTime.setText(stringBuffer);
        }

        // It is Game Settings
        else if(position == 1){
            final TextView noOfGames = (TextView) holder.itemView.findViewById(R.id.noOfGames);
            final SeekBar difficulty = (SeekBar) holder.itemView.findViewById(R.id.difficultySeekBar);

            noOfGames.setText( String.valueOf(alarmEditorActivity.getPref().getInt(alarmEditorActivity.getString(R.string.no_of_games),1)));
            difficulty.setProgress(alarmEditorActivity.getPref().getInt(alarmEditorActivity.getString(R.string.diff_games), 0) - 1);

            alarmEditorActivity.setNoOfGames(noOfGames);
            alarmEditorActivity.setSeekBar(difficulty);
        }

        // It is Not Awake SMS
        else if (position == 2) {
            // Sets up the values in the view from the SharedPreferences

            // Give access to Alarm Editor Activity
            EditText contactsTextField = (EditText) holder.itemView.findViewById(R.id.contactsTextField);
            EditText messageBody = (EditText) holder.itemView.findViewById(R.id.messageBody);
            alarmEditorActivity.setContactsTextField(contactsTextField);
            alarmEditorActivity.setMessageBody(messageBody);

            // Sets the texts from SharedPreferences
            contactsTextField.setText(alarmEditorActivity.getPref().getString(alarmEditorActivity.getString(R.string.contacts), ""));
            messageBody.setText(alarmEditorActivity.getPref().getString(alarmEditorActivity.getString(R.string.message_body), ""));
        }

        // It is After Alarm App
        else if (position == 3) {
            TextView appName = (TextView) holder.itemView.findViewById(R.id.label);
            ImageView appIcon = (ImageView) holder.itemView.findViewById(R.id.icon);

            alarmEditorActivity.setAppName(appName);
            alarmEditorActivity.setAppIcon(appIcon);
        }
    }


    @Override
    public int getItemCount() {
        // There will be only 6 Settings group therefore it will be a static number
        return 4;
    }

    /**
     * Determines the View Type
     *
     * @param position ,position of the ViewHolder
     * @return the integer that indicates the type
     */
    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
