package com.example.sensortest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    /** Creates a new compass activity */
    public void compassActivity(View view) {
        Intent intent = new Intent(this, CompassActivity.class);
        startActivity(intent);
    }

    /** Creates a new accelerometer activity */
    public void accelerometerActivity(View view) {  //view is the object that was clicked
        Intent intent = new Intent(this, AccelerometerActivity.class);
        startActivity(intent);
    }
}
