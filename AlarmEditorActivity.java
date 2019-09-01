package com.groupnamenotfoundexception.wakeupcall.app.activities;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import com.groupnamenotfoundexception.wakeupcall.app.R;
import com.groupnamenotfoundexception.wakeupcall.app.adapters.AlarmEditorAdapter;
import com.groupnamenotfoundexception.wakeupcall.app.alarm.Receiver;

import java.util.Date;
import java.util.StringTokenizer;

/**
 * Activity that holds all the edit option to initialize an alarm.
 * <p/>
 * Main Look of Activity Done by Caner Caliskaner
 * Not Awake SMS Done by Caner Caliskaner
 * Basic Alarm Editing Done by Caner Caliskaner
 */
public class AlarmEditorActivity extends Activity {
    /**
     * CONSTANTS
     */
    // Request Code for picking contacts
    private final int PICK_CONTACT = 2015;

    /**
     * PROPERTIES
     */
    // Shared Preferences
    private SharedPreferences.Editor editor;
    private SharedPreferences pref;

    // Recycler View that holds every editing options as cards
    private RecyclerView settings;
    private AlarmEditorAdapter adapter;

    // Time Selector Properties present in the "Enter the Alarm Time" Card
    private TextView alarmTime;
    private TimePickerDialog timePickerDialog;
    private TimePickerDialog.OnTimeSetListener onTimeSetListener;
    private int pickedHour, pickedMin;
    private Receiver receiver;

    // The text fields (EditTexts) in the "Not Awake SMS" Card
    private EditText contactsTextField, messageBody;

    // Alarm Index
    private int alarmIndex;

    // Game Settings
    private int noOfGamesCount, gameDifficulty;
    private TextView noOfGames;
    private SeekBar seekBar;

    // After Alarm App
    private TextView appName;
    private ImageView appIcon;

    /** METHODS */
    /**
     * Creates the view when called. Does the job of Constructor. Presented by Android, Overridden by Project Group
     *
     * @param savedInstanceState ,If the view has a saved instance - in this case there is none - loads this.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the view from the XML Layout File
        setContentView(R.layout.activity_alarm_editor);

        // Finds the alarm index provided by Home Fragment from the Intent Object that called this Class
        alarmIndex = getIntent().getExtras().getInt(getString(R.string.alarm_index));

        // Finds and assigns RecyclerView from XML Layout File
        settings = (RecyclerView) findViewById(R.id.alarm_editor);

        // Sets up RecyclerView
        settings.setLayoutManager(new LinearLayoutManager(this));
        settings.setHasFixedSize(true);
        adapter = new AlarmEditorAdapter(this);
        settings.setAdapter(adapter);

        // Get the Shared Preferences and Editor Object according to the alarm index
        pref = getSharedPreferences(getString(R.string.alarm_prefix) + alarmIndex, MODE_PRIVATE);
        editor = pref.edit();

        // Get Picked Time - If not Selected Go get the current Time
        pickedHour = pref.getInt(getString(R.string.hour), new Date().getHours());
        pickedMin = pref.getInt(getString(R.string.minute), new Date().getMinutes());
        receiver = new Receiver();

        onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                // When Time is set put them into respective places
                pickedHour = hour;
                pickedMin = minute;

                // Update The TimePicker
                timePicker.setCurrentHour(hour);
                timePicker.setCurrentMinute(minute);

                // Format the Text
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

                // Put the text into Indicator TextView
                alarmTime.setText(stringBuffer);
            }
        };
        // Sets Time Picker Dialog
        timePickerDialog = new TimePickerDialog(this, onTimeSetListener, pickedHour, pickedMin, true);

        // Game Settings
        gameDifficulty = pref.getInt(getString(R.string.diff_games),0) - 1;
        noOfGamesCount = pref.getInt(getString(R.string.no_of_games),1);

        // Should be deleted, just a test code!
        editor.putInt(getString(R.string.alarm_index), alarmIndex);
        editor.putBoolean(getString(R.string.is_deleted), false);
        editor.putBoolean(getString(R.string.is_enabled),true);
        editor.commit();
    }

    /**
     * When Activity is Closed this will be called.
     */
    @Override
    protected void onStop() {
        // Save Alarm Time
        saveAlarmTime();

        // Saves the message
        saveMessage();

        // Saves Game Settings
        saveGameSettings();

        // Informs User that Alarm is Saved
        Toast.makeText(this, "Alarm is Saved", Toast.LENGTH_SHORT).show();

        // Set the alarm
        receiver.setAlarm(AlarmEditorActivity.this, alarmIndex, Receiver.BEFORE_SLEEP);

        // Stops Activity
        super.onStop();
    }

    /**
     * When Activity is Closed this will be called.
     */
    @Override
    protected void onPause() {
        // Save Alarm Time
        saveAlarmTime();

        // Saves the message
        saveMessage();

        // Saves Game Settings
        saveGameSettings();

        // Informs User that Alarm is Saved
        Toast.makeText(this, "Alarm is Saved", Toast.LENGTH_SHORT).show();

        // Set the alarm
        receiver.setAlarm(AlarmEditorActivity.this, alarmIndex, Receiver.BEFORE_SLEEP);

        // Stops Activity
        super.onPause();
    }
    /**
     * This method is called after a Activity started with startActivityForResult(Intent,int) method
     *
     * @param requestCode ,request code for not confusing the request such as contacts and apps
     * @param resultCode ,result of the Activity called
     * @param data ,dataset that is provided by Called Activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check the request code
        // It is contacts!
        if (requestCode == PICK_CONTACT && resultCode == RESULT_OK) {
            // Get URI of the selected contact
            Uri contactUri = data.getData();

            // Set the cursor Object to find the phone number from the selected contact
            Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
            cursor.moveToFirst();

            // Column number of Phone Number Retrieved
            int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

            // Previously selected contacts are retrieved
            String contacts = contactsTextField.getText().toString();

            // If there is not a comma add comma
            if (contacts.length() != 0 && contacts.charAt(contacts.length() - 1) != ',')
                contactsTextField.append(",");

            // Add the selected contact's number to the text field
            contactsTextField.append(cursor.getString(column) + ",");
            editor.putString(getString(R.string.contacts),String.valueOf(contactsTextField.getText())).commit();
        }

        String packageAdress = getSharedPreferences(getString(R.string.alarm_prefix) + alarmIndex, Context.MODE_PRIVATE).getString(getString(R.string.app_package_name),"");

        PackageManager pm = getPackageManager();

        try {
            appName.setText(pm.getApplicationLabel(pm.getApplicationInfo(packageAdress, 0)));
            appIcon.setImageDrawable(pm.getApplicationIcon(packageAdress));
        }catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called by "CONTACTS" Button launches Contacts App
     *
     * @param view ,View that called the Method - Required for in XML Method Binding
     */
    public void launchContacts(View view) {
        // Create the intent
        Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);

        // Start Activity For Result with Request Code of PICK_CONTACT
        startActivityForResult(i, PICK_CONTACT);
    }

    /**
     * Called when user is pressed the "Enter Alarm Time" Card
     *
     * @param view ,View that called the Method - Required for in XML Method Binding
     */
    public void showTimePicker(View view) {
        timePickerDialog.show();
    }

    /**
     * Called when user is pressed the "Enter Alarm Time" Card
     *
     * @param view ,View that called the Method - Required for in XML Method Binding
     */
    public void showAppSelector(View view) {
        Intent intent = new Intent(this, AfterAlarmAppChooserActivity.class);
        intent.putExtra("Alarm Index", alarmIndex);
        startActivityForResult(intent,1);
    }
    /**
     * Saves messages to the SharedPreferences
     */
    private void saveMessage() {
        editor.putString(getString(R.string.contacts), String.valueOf(contactsTextField.getText()));
        editor.putString(getString(R.string.message_body), String.valueOf(messageBody.getText()));
        editor.commit();
    }

    /**
     * Saves alarm time to the SharedPreferences
     */
    private void saveAlarmTime() {
        editor.putInt(getString(R.string.hour), pickedHour);
        editor.putInt(getString(R.string.minute), pickedMin);
        editor.commit();
    }

    private void saveGameSettings() {
        editor.putInt(getString(R.string.no_of_games), noOfGamesCount);
        editor.putInt(getString(R.string.diff_games), seekBar.getProgress() + 1);
        editor.commit();
    }

    // GETTERS
    public SharedPreferences.Editor getEditor() {
        return editor;
    }

    public SharedPreferences getPref() {
        return pref;
    }

    // SETTERS
    public void setContactsTextField(EditText contactsTextField) {
        this.contactsTextField = contactsTextField;
    }

    public void setMessageBody(EditText messageBody) {
        this.messageBody = messageBody;
    }

    public void setAlarmTime(TextView time) {
        this.alarmTime = time;
    }

    public void setNoOfGames(TextView noOfGames) {
        this.noOfGames = noOfGames;
    }

    public void setSeekBar(SeekBar seekBar) {
        this.seekBar = seekBar;
    }

    public void setAppName(TextView appName) {
        this.appName = appName;
    }

    public void setAppIcon(ImageView appIcon) {
        this.appIcon = appIcon;
    }

    /**
     * When a Hardware button is clicked this will be called
     *
     * @param keyCode ,Code of the button or key that is pressed
     * @param event   ,KeyEvent
     * @return Indicate success of the operation
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // if button is menu button show Credits Page
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            startActivity(new Intent(this, CreditsActivity.class));
            return true;
        }
        // if it is not do normal job of the key
        return super.onKeyDown(keyCode, event);
    }

    public void increaseNumberOfGames(View view){
        noOfGames.setText(String.valueOf(++noOfGamesCount));
    }
    public void decreaseNumberOfGames(View view){
        if( --noOfGamesCount < 1)
            noOfGamesCount++;
        noOfGames.setText(String.valueOf(noOfGamesCount));
    }
}
