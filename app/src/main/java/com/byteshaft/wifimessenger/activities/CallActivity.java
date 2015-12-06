package com.byteshaft.wifimessenger.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.byteshaft.wifimessenger.R;
import com.byteshaft.wifimessenger.utils.AudioCall;
import com.byteshaft.wifimessenger.utils.MessagingHelpers;
import com.byteshaft.wifimessenger.utils.ServiceHelpers;

import java.net.InetAddress;
import java.net.UnknownHostException;


// SensorEventListener used for receiving notifications

public class CallActivity extends Activity implements SensorEventListener, View.OnClickListener {

    TextView textViewContactName;
    ImageButton buttonCallAccept;
    ImageButton buttonCallReject;
    AudioCall audioCall;
    private Ringtone ringtone;
    private Vibrator vibrator;
    public static boolean IN_CALL;
    private static CallActivity sInstance;
    private static boolean callActivityVisible;
    private String ipAddress;

    AudioManager mAudioManager;

    public static CallActivity getInstance() {
        return sInstance;
    }

    public static boolean isRunning() {
        return sInstance != null;
    }

    public static boolean isVisible() {
        return callActivityVisible;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_activity);
        //CallActivity is a boolean
        callActivityVisible = true;
        //sIn is reference to CallAct class
        sInstance = this;
        // Creating vibrator, ringtone
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //creating textview for contact name, button for accepting call, rejecting call
        textViewContactName = (TextView) findViewById(R.id.tv_contact_name_call);
        buttonCallAccept = (ImageButton) findViewById(R.id.button_call_accept);
        buttonCallReject = (ImageButton) findViewById(R.id.button_call_reject);
        //getting intent to start activity
        Intent intent = getIntent();
        //store data pertaining to callState & ip address
        String callSate = intent.getStringExtra("CALL_STATE");
        ipAddress = intent.getStringExtra("IP_ADDRESS");
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            //gets instance of ipaddress
            audioCall = AudioCall.getInstance(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();

        }
        // if else responds to whether call is incoming or outgoing and it will set
        //..images and contact name

        if (callSate.equals("OUTGOING")) {
            String contact = intent.getStringExtra("CONTACT_NAME");
            textViewContactName.setText("Calling: " + contact);
            buttonCallAccept.setVisibility(View.GONE);
        } else if (callSate.equals("INCOMING")) {
            String contact = intent.getStringExtra("CONTACT_NAME");
            textViewContactName.setText("Incoming: " + contact);
            buttonCallAccept.setVisibility(View.VISIBLE);
            vibrator.vibrate(500);
            ringtone.play();
        }
        //defines listener

        buttonCallAccept.setOnClickListener(this);
        buttonCallReject.setOnClickListener(this);
    }
    //since SensorEventListener is implemented ,need to overide some  of the methods

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk >= Build.VERSION_CODES.LOLLIPOP) {
            SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            Sensor mProximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            sensorManager.registerListener(this, mProximitySensor, SensorManager.SENSOR_DELAY_UI);
            PowerManager manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = manager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "Your Tag");
            if (event.values[0] != mProximitySensor.getMaximumRange() && IN_CALL && callActivityVisible) {
                wl.acquire();
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.screenBrightness = 0;
                getWindow().setAttributes(params);
                Log.e("onSensorChanged", "NEAR");
            } else {
                if (wl.isHeld()) {
                    wl.release();
                }
                Log.e("onSensorChanged", "FAR");
            }
        }
    }
    //pause,resume ,stop ,destroy lifecycle methods are overidden to set boolean false
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        callActivityVisible = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        callActivityVisible = true;
    }
    //on stop i.e when home button is clicked ringtone stops playing
    @Override
    protected void onStop() {
        super.onStop();
        if (ringtone.isPlaying()) {
            ringtone.stop();
        }
        callActivityVisible = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        callActivityVisible = false;
    }
    //when call is incoming,user has 2 options,either reject or accept,tat interface is defined here
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_call_accept:
                if (ringtone.isPlaying()) {
                    ringtone.stop();
                }

                //AudioManager class provides access to volume and ring control

                setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
//                mAudioManager.setMode(AudioManager.MODE_IN_CALL);
//                mAudioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_RAISE, 14);
                vibrator.cancel();

                //TO START CALL
                audioCall.startCall();
                IN_CALL = true;
                MessagingHelpers.sendMessage("ACC:", ipAddress, ServiceHelpers.BROADCAST_PORT);
                //button to accept should disappear
                buttonCallAccept.setVisibility(View.GONE);
                break;
            case R.id.button_call_reject:
                if (ringtone.isPlaying()) {
                    //when rejected,stop ringtone
                    ringtone.stop();
                }
                //when user has accepted call n is goin to end call at that moment
                if (IN_CALL) {
                    audioCall.endCall();
                    MessagingHelpers.sendMessage("END:", ipAddress, ServiceHelpers.BROADCAST_PORT);
                    IN_CALL = false;
                    //end call and set IN_CALL false
                    mAudioManager.setMode(AudioManager.MODE_NORMAL);
                } else {
                    MessagingHelpers.sendMessage("REJ:", ipAddress, ServiceHelpers.BROADCAST_PORT);
                }
                finish();
                break;
        }
    }
}
