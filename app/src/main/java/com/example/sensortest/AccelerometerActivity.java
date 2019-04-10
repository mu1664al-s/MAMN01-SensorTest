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
        import android.os.Build;
        import android.os.Bundle;
        import android.os.VibrationEffect;
        import android.os.Vibrator;
        import android.view.Display;
        import android.widget.ImageView;
        import android.widget.Switch;
        import android.widget.TextView;

        import java.lang.reflect.Array;

public class AccelerometerActivity extends AppCompatActivity implements SensorEventListener {

    private TextView data_txt;
    private ImageView pointer_img;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private boolean haveSensor = false;
    private float[] mLastAccelerometer = new float[3];

    private static float LOWPASS_ALPHA = 0.1f;

    private Vibrator haptic;
    private int pointerHeight, screenWidth;
    private ConstraintLayout view;
    private TextView angle_txt;
    private boolean balanced;

    private Switch vibrationSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        pointer_img = findViewById(R.id.pointer_img);
        data_txt = findViewById(R.id.data_txt);
        angle_txt = findViewById(R.id.angle_txt);
        haptic = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        vibrationSwitch = findViewById(R.id.vibration_switch);

        view = findViewById(R.id.accelerometer_view);
        //Source: https://stackoverflow.com/questions/39660918/android-constraintlayout-getwidth-return-0
        //Source: https://stackoverflow.com/questions/13840007/what-exactly-does-the-post-method-do
        pointer_img.post(new Runnable() {
            @Override
            public void run() {
                //height is ready
                pointerHeight = pointer_img.getHeight();
                screenWidth = view.getWidth();
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mLastAccelerometer = lowPass(event.values.clone(), mLastAccelerometer);

            data_txt.setText("X: " + mLastAccelerometer[0] + "\n" + "Y: " + mLastAccelerometer[1] + "\n" + "Z: " + mLastAccelerometer[2]);

            double alphaYZ = relativeALPHA_YZ(mLastAccelerometer[1], mLastAccelerometer[2]);
            pointer_img.setX(relativeY(alphaYZ, screenWidth));

            long angle = Math.round(Math.toDegrees(alphaYZ));

            angle_txt.setText(Math.round(Math.toDegrees(alphaYZ)) + "°");

            if (angle == 0L) {
                if (!balanced) {
                    pointer_img.setImageResource(R.drawable.balance);
                    if (vibrationSwitch.isChecked()) {
                        // source: https://stackoverflow.com/questions/13950338/how-to-make-an-android-device-vibrate
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            haptic.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            //deprecated in API 26
                            haptic.vibrate(500);
                        }
                    }

                    balanced = true;
                }
            } else {
                pointer_img.setImageResource(R.drawable.of_balance);
                balanced = false;
            }
        }
    }

    private double relativeALPHA_YZ(float posY, float posZ) {
        double alpha = Math.atan((posY)/(posZ));
        return ((posY < 0) ? alpha : alpha);
    }

    private float relativeY(double alpha, int to) {
        return (float) (to/2 + Math.sin(alpha)*to/2) - pointerHeight/2;
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
