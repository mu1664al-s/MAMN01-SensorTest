package com.example.sensortest;

        import androidx.appcompat.app.AppCompatActivity;

        import android.app.AlertDialog;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.hardware.Sensor;
        import android.hardware.SensorEvent;
        import android.hardware.SensorEventListener;
        import android.hardware.SensorManager;
        import android.os.Bundle;
        import android.os.Vibrator;
        import android.widget.ImageView;
        import android.widget.TextView;

public class AccelerometerActivity extends AppCompatActivity implements SensorEventListener {

    private TextView data_txt;
    private ImageView pointer_img;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private boolean haveSensor = false;
    private float[] mLastAccelerometer = new float[3];

    private Vibrator haptic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        pointer_img = (ImageView) findViewById(R.id.pointer_img);
        data_txt = (TextView) findViewById(R.id.data_txt);
        haptic = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);

            data_txt.setText("X: " + mLastAccelerometer[0] + "\n" + "Y: " + mLastAccelerometer[1] + "\n" + "Z: " + mLastAccelerometer[2]);

            pointer_img.setX(mLastAccelerometer[0]);
            pointer_img.setY(mLastAccelerometer[1]);
        }
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
}
