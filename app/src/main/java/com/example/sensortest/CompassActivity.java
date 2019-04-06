package com.example.sensortest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    private int mAzimuth;
    private boolean north;
    private TextView txt_compass;
    private ImageView compass_img;
    private SensorManager mSensorManager;
    private Sensor mRotationV, mAccelerometer, mMagnetometer;
    private boolean haveSensor = false, haveSensor2 = false;
    private float[] rMat = new float[9];
    private float[] orientation = new float[3];
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private ConstraintLayout compass_view;

    private Vibrator haptic;
    private TextToSpeech speech;
    private static String nativeNorth, nativeEast, nativeWest, nativeSouth;
    private String spoken;

    private static float LOWPASS_ALPHA = 0.70f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        compass_img = (ImageView) findViewById(R.id.img_compass);
        txt_compass = (TextView) findViewById(R.id.txt_compass);
        haptic = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        compass_view = (ConstraintLayout) findViewById(R.id.compass_view);

        try {
            String langCode = Locale.getDefault().getLanguage();
            nativeNorth = new TranslateAPI().execute("North", "en", langCode).get();
            nativeEast = new TranslateAPI().execute("East", "en", langCode).get();
            nativeWest = new TranslateAPI().execute("West", "en", langCode).get();
            nativeSouth = new TranslateAPI().execute("South", "en", langCode).get();
            spoken = "";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            rMat = lowPass(event.values.clone(), rMat);
            SensorManager.getRotationMatrixFromVector(rMat, rMat);
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        } else {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mLastAccelerometer = lowPass(event.values.clone(), mLastAccelerometer);
                mLastAccelerometerSet = true;
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mLastMagnetometer = lowPass(event.values.clone(), mLastMagnetometer);
                mLastMagnetometerSet = true;
            }
            if (mLastAccelerometerSet && mLastMagnetometerSet) {
                SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
                SensorManager.getOrientation(rMat, orientation);
                mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
            }
        }

        mAzimuth = Math.round(mAzimuth);
        compass_img.setRotation(-mAzimuth);

        String where = "N";
        if (mAzimuth >= 345 || mAzimuth <= 15) {
            if (!north) {
                north();
            }
        } else {
            notNorth();
        }

        if (mAzimuth >= 350 || mAzimuth <= 10) {
            where = "N";
        } else if (mAzimuth < 350 && mAzimuth > 280) {
            where = "NW";
        } else if (mAzimuth <= 280 && mAzimuth > 260) {
            where = "W";
            speakOnce(nativeWest);
        } else if (mAzimuth <= 260 && mAzimuth > 190) {
            where = "SW";
        } else if (mAzimuth <= 190 && mAzimuth > 170) {
            where = "S";
            speakOnce(nativeSouth);
        } else if (mAzimuth <= 170 && mAzimuth > 100) {
            where = "SE";
        } else if (mAzimuth <= 100 && mAzimuth > 80) {
            where = "E";
            speakOnce(nativeEast);
        } else if (mAzimuth <= 80 && mAzimuth > 10) {
            where = "NE";
        }

        txt_compass.setText(mAzimuth + "° " + where);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void start() {
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) { //does it support RotationVector? a compass + gyroscope sensor.
            if ((mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) || (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null)) {
                //check for accelerometer and compass sensors
                noSensorsAlert();
            } else {    //register accelerometer and compass sensors
                mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                haveSensor = mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
                haveSensor2 = mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
            }
        } else{ //register RotationVector
            mRotationV = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            haveSensor = mSensorManager.registerListener(this, mRotationV, SensorManager.SENSOR_DELAY_UI);
        }

        speech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    //three character language code
                    speech.setLanguage(new Locale(Locale.getDefault().getISO3Language()));
                }
            }
        });
    }

    private void noSensorsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Your device doesn't support the Compass.")
                .setCancelable(false)
                .setNegativeButton("Close",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        alertDialog.show();
    }

    private void stop() {
        if(haveSensor && haveSensor2){
            mSensorManager.unregisterListener(this,mAccelerometer);
            mSensorManager.unregisterListener(this,mMagnetometer);
        }
        else{
            if(haveSensor)
                mSensorManager.unregisterListener(this,mRotationV);
        }

        if (speech != null) {
            speech.shutdown();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        start();
    }

    private void north() {
        north = true;
        compass_img.setImageResource(R.drawable.north);
        compass_view.setBackgroundColor(Color.BLACK);
        txt_compass.setTextColor(Color.WHITE);
        // source: https://stackoverflow.com/questions/13950338/how-to-make-an-android-device-vibrate
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            haptic.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            haptic.vibrate(500);
        }

        speakOnce(nativeNorth);
    }

    private void speakOnce(String word) {
        if (word != null && !spoken.equals(word)) {
            speech.speak(word, TextToSpeech.QUEUE_FLUSH, null);
        }
        spoken = word;
    }

    private void notNorth() {
        north = false;
        compass_view.setBackgroundColor(Color.WHITE);
        txt_compass.setTextColor(Color.GRAY);
        compass_img.setImageResource(R.drawable.not_north);
    }

    //Source: https://www.built.io/blog/applying-low-pass-filter-to-android-sensor-s-readings
    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + LOWPASS_ALPHA * (input[i] - output[i]);
        }
        return output;
    }

}
