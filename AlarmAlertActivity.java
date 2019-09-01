package com.groupnamenotfoundexception.wakeupcall.app.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import com.groupnamenotfoundexception.wakeupcall.app.R;
import com.groupnamenotfoundexception.wakeupcall.app.activities.games.*;
import com.groupnamenotfoundexception.wakeupcall.app.activities.util.SystemUiHider;
import com.groupnamenotfoundexception.wakeupcall.app.games.Game;
import com.groupnamenotfoundexception.wakeupcall.app.managers.DailySleepManager;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class AlarmAlertActivity extends Activity {
    Vibrator vib;
    private final static long[] VIB_PATTERN = new long[]{1000, 500};
    MediaPlayer mediaPlayer;
    Ringtone[] sound;
    // SMS Footer that inform the reciever
    private final static String SMS_FOOTER = " - Sent by Wake Up Call�, sender is not awake!";
    public static final int GAMES_REQUEST_CODE = 9999;
    int alarmIndex, difficulty, noOfGames, noOfFails;

    SharedPreferences pref;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_alert);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dismiss).setOnTouchListener(mDelayHideTouchListener);

        if (getIntent().getExtras() != null) {
            alarmIndex = getIntent().getExtras().getInt("Alarm Index");
            pref = getSharedPreferences(getString(R.string.alarm_prefix) + alarmIndex, Context.MODE_PRIVATE);
            difficulty = pref.getInt(getString(R.string.diff_games), 2);
            noOfGames = pref.getInt(getString(R.string.no_of_games), 1);
        }

        noOfFails = 0;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        vib.vibrate(VIB_PATTERN, 0);

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.setDataSource(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void startGames(View view) {
        Intent intent;
        mediaPlayer.pause();

        int randomNum = ((int) (Math.random() * 5));


        if (randomNum == 0) {
            intent = new Intent(this, SimonActivity.class);
        } else if (randomNum == 1) {
            intent = new Intent(this, MatchTheObjectsActivity.class);
        } else if (randomNum == 2) {
            intent = new Intent(this, ShakeTheDeviceActivity.class);
        } else if (randomNum == 3) {
            intent = new Intent(this, PressTheBlueActivity.class);
        } else {
            intent = new Intent(this, LazyKillerActivity.class);
        }

        intent.putExtra("DIFFICULTY", difficulty);
        intent.putExtra("REQUESTER", Game.REQUESTER_ADVANCED_ALARM);

        startActivityForResult(intent, GAMES_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data.getExtras().getBoolean("GAME RESULT")) {
            noOfGames--;
        } else {
            noOfFails++;
        }

        Toast.makeText(this, "Games left: " + noOfGames + " Fails: " + noOfFails, Toast.LENGTH_SHORT).show();

        if (noOfGames == 0) {
            SharedPreferences preferences = getSharedPreferences(getString(R.string.alarms_master), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(getString(R.string.end_of_sleep), System.currentTimeMillis());

            GregorianCalendar calendar = new GregorianCalendar();
            if (getSharedPreferences(getString(R.string.alarms_master), Context.MODE_PRIVATE).getInt(getString(R.string.current_week), 0)
                    != calendar.get(Calendar.WEEK_OF_YEAR)) {
                editor.putInt(getString(R.string.current_week), calendar.get(Calendar.WEEK_OF_YEAR));
                editor.putLong(getString(R.string.weekly_sleep), new DailySleepManager(this).getDailySleep());
            } else {
                editor.putLong(getString(R.string.weekly_sleep), preferences.getLong(getString(R.string.weekly_sleep), 0) + new DailySleepManager(this).getDailySleep());
            }

            if (pref.getString(getString(R.string.app_package_name), "").length() > 0)
                launchApp(pref.getString(getString(R.string.app_package_name), ""));
            vib.cancel();
            mediaPlayer.stop();
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        } else if (noOfFails >= 2
                ) {
            String contacts = pref.getString(getString(R.string.contacts), "");
            StringTokenizer stringTokenizer = new StringTokenizer(contacts, ",");

            while (stringTokenizer.hasMoreTokens()) {
                SmsManager.getDefault().sendTextMessage(stringTokenizer.nextToken(), null, pref.getString(getString(R.string.message_body), "") + SMS_FOOTER, null, null);
            }

            noOfGames = pref.getInt(getString(R.string.no_of_games), 5);
            noOfFails = 0;
            mediaPlayer.start();
        } else {
            startGames(findViewById(R.id.dismiss));
        }
    }

    @Override
    protected void onResume() {
        vib.vibrate(VIB_PATTERN, 0);
        super.onResume();
    }

    protected void launchApp(String packageName) {

        Intent mIntent = getPackageManager().getLaunchIntentForPackage(

                packageName);

        if (mIntent != null) {
            startActivity(mIntent);

        }
    }
}
