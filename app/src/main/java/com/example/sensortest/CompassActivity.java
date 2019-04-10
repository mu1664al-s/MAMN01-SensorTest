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
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Locale;

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
    private static String[] nativeDirections = new String[4];
    private static String[] directions = {"North", "East", "South", "West"};
    private String spoken;

    private Switch soundSwitch, vibrationSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        compass_img = findViewById(R.id.img_compass);
        txt_compass = findViewById(R.id.txt_compass);
        haptic = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        compass_view = findViewById(R.id.compass_view);

        soundSwitch = findViewById(R.id.sound_switch);
        vibrationSwitch = findViewById(R.id.vibration_switch);

        String langCode = Locale.getDefault().getLanguage();
        for (String direction : directions) {
            new TranslateAPI().translate(direction, "en", langCode);
        }
        spoken = "";
    }

    public void setNativeDirection(String pair) {
        String[] data = pair.split(":");
        int index = Arrays.asList(directions).indexOf(data[0]);
        Log.d(data[0], data[1] + "  " + index);
        nativeDirections[index] = data[1];
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            rMat = lowPass(rMat, rMat, 0.4f);
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mLastAccelerometer = lowPass(event.values.clone(), mLastAccelerometer, 0.08f);
            mLastAccelerometerSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mLastMagnetometer = lowPass(event.values.clone(), mLastMagnetometer, 0.18f);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
            //SensorManager.getOrientation(rMat, orientation);
            rMat = lowPass(rMat, rMat, 0.2f);
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }

        mAzimuth = Math.round(mAzimuth);
        compass_img.setRotation(-mAzimuth);

        String where = "N";
        if (!(mAzimuth >= 350 || mAzimuth <= 10)) {
            notNorth();
        }

        if (mAzimuth >= 350 || mAzimuth <= 10) {
            where = "N";
            if (!north) {
                north();
            }
        } else if (mAzimuth < 350 && mAzimuth > 280) {
            where = "NW";
            spoken = "";
        } else if (mAzimuth <= 280 && mAzimuth > 260) {
            where = "W";
            speakOnce(nativeDirections[3]);
        } else if (mAzimuth <= 260 && mAzimuth > 190) {
            where = "SW";
            spoken = "";
        } else if (mAzimuth <= 190 && mAzimuth > 170) {
            where = "S";
            speakOnce(nativeDirections[2]);
        } else if (mAzimuth <= 170 && mAzimuth > 100) {
            where = "SE";
            spoken = "";
        } else if (mAzimuth <= 100 && mAzimuth > 80) {
            where = "E";
            speakOnce(nativeDirections[1]);
        } else if (mAzimuth <= 80 && mAzimuth > 10) {
            where = "NE";
            spoken = "";
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
        soundSwitch.setTextColor(Color.WHITE);
        vibrationSwitch.setTextColor(Color.WHITE);

        if (vibrationSwitch.isChecked()) {
            // source: https://stackoverflow.com/questions/13950338/how-to-make-an-android-device-vibrate
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                haptic.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                haptic.vibrate(500);
            }
        }
        if (soundSwitch.isChecked()) {
            speakOnce(nativeDirections[0]);
        }
    }

    private void speakOnce(String word) {
        if (word != null && spoken != null && !spoken.equals(word) && soundSwitch.isChecked()) {
            speech.speak(word, TextToSpeech.QUEUE_FLUSH, null);
        }
        spoken = word;
    }

    private void notNorth() {
        north = false;
        compass_view.setBackgroundColor(Color.WHITE);
        txt_compass.setTextColor(Color.GRAY);
        soundSwitch.setTextColor(Color.GRAY);
        vibrationSwitch.setTextColor(Color.GRAY);
        compass_img.setImageResource(R.drawable.not_north);
    }

    //Source: https://www.built.io/blog/applying-low-pass-filter-to-android-sensor-s-readings
    private float[] lowPass( float[] input, float[] output, float alpha ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + alpha * (input[i] - output[i]);
        }
        return output;
    }

private class TranslateAPI extends HTTPcon {
    private static final String API_KEY = "trnsl.1.1.20190406T161250Z.d535ecb82d0bb929.0fb2ded77d67b60b2911527e20f72e3fe3b1e994";

    public void translate(String... params) {
        String url = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=" + API_KEY + "&text=" + params[0] + "&lang=" + params[1] + "-" + params[2] + "&format=plain";
        super.execute(params[0], url);
    }

    protected void callback(String result) {
        setNativeDirection(result);
    }
}
}
