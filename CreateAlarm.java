package madsmurfzz.com.takeyourpills;

import madsmurfzz.com.takeyourpills.medicines.*;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TimePicker;
import android.view.View;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.DateFormat;

public class CreateAlarm extends AppCompatActivity {

    // Widgets & Components

    Button alarmButton;
    Button cancelButton;
    TimePicker alarmTime;

    CheckBox mon;
    CheckBox tue;
    CheckBox wed;
    CheckBox thu;
    CheckBox fri;
    CheckBox sat;
    CheckBox sun;

    Medicine med;

    // Methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_alarm);

        // Retrieve Medicine Information

        Bundle data = this.getIntent().getExtras();
        med = (Medicine)(data.get("medToAdd"));

        // Associate Widgets

        alarmButton = (Button) findViewById(R.id.alarmButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        alarmTime = (TimePicker) findViewById(R.id.alarmTime);

        // Associate Checkboxes

        mon = (CheckBox) findViewById(R.id.mon);
        tue = (CheckBox) findViewById(R.id.tue);
        wed = (CheckBox) findViewById(R.id.wed);
        thu = (CheckBox) findViewById(R.id.thu);
        fri = (CheckBox) findViewById(R.id.fri);
        sat = (CheckBox) findViewById(R.id.sat);
        sun = (CheckBox) findViewById(R.id.sun);

    }

    // Buttons' Listeners

    public void onCreateAlarm(View v){

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent( this, AlarmDisplay.class );
        PendingIntent alarmPendingIntent;
        alarmPendingIntent = PendingIntent.getActivity( this, 0, alarmIntent, 0 );


        AlarmManager myAlarm = med.getAlarm( (med.getAlarms().size() - 1 );

        // alarmManager.setRepeating( AlarmManager.RTC_WAKEUP,,alarmPendingIntent );
        med.addAlarm(alarmManager);




    }

    public void onCancel(View v){
        Intent retIntent = new Intent(this, MedicineSetter.class);
        startActivity(retIntent);
    }


}
