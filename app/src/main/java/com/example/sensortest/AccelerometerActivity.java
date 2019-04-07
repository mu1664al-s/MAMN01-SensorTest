package com.example.sensortest;

        import androidx.appcompat.app.AppCompatActivity;
        import androidx.constraintlayout.widget.ConstraintLayout;

        import android.app.AlertDialog;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.graphics.Point;
        import android.hardware.Sensor;
        import android.hardware.SensorEvent;
        import android.hardware.SensorEventListener;
        import android.hardware.SensorManager;
        import android.os.Bundle;
        import android.os.Vibrator;
        import android.view.Display;
        import android.widget.ImageView;
        import android.widget.TextView;

        import java.lang.reflect.Array;

public class AccelerometerActivity extends AppCompatActivity implements SensorEventListener {

    private TextView data_txt;
    private ImageView pointer_img;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private boolean haveSensor = false;
    private float[] mLastAccelerometer = new float[3];

    private static float LOWPASS_ALPHA = 0.06f;

    private Vibrator haptic;
    private int screenWidth, screenHeight;
    private ConstraintLayout view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        pointer_img = (ImageView) findViewById(R.id.pointer_img);
        data_txt = (TextView) findViewById(R.id.data_txt);
        haptic = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //Source: http://www.androidtutorialshub.com/how-to-get-width-and-height-android-screen-in-pixels/
        view = (ConstraintLayout) findViewById(R.id.accelerometer_view);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mLastAccelerometer = lowPass(event.values.clone(), mLastAccelerometer);

            data_txt.setText("X: " + mLastAccelerometer[0] + "\n" + "Y: " + mLastAccelerometer[1] + "\n" + "Z: " + mLastAccelerometer[2]);

            pointer_img.setX(relativeX(mLastAccelerometer[0], screenWidth));
            pointer_img.setY(relativeY(mLastAccelerometer[1], screenHeight));
        }
    }

    private float relativeX(float pos, int to) {
        return to/2 - (to/20)*pos;
    }

    private float relativeY(float pos, int to) {
        return (to/11)*pos;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void start() {
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {
            //check for accelerometer and compass sensors
            noSensorsAlert();
        } else {    //register accelerometer and compass sensors
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            haveSensor = mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    private void noSensorsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Your device doesn't support a accelerometer.")
                .setCancelable(false)
                .setNegativeButton("Close",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        alertDialog.show();
    }

    private void stop() {
        if(haveSensor){
            mSensorManager.unregisterListener(this, mAccelerometer);
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

    //Source: https://www.built.io/blog/applying-low-pass-filter-to-android-sensor-s-readings
    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + LOWPASS_ALPHA * (input[i] - output[i]);
        }
        return output;
    }
}
