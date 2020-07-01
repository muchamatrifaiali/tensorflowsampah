package com.felhr.serialportexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class Main3Activity extends AppCompatActivity  implements SensorEventListener {

    private TextView magneticX;
    private TextView magneticY;
    private TextView magneticZ;
    private SensorManager sensorManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Capture magnetic sensor related view elements
        magneticX = (TextView) findViewById(R.id.valMag_X);
        magneticY = (TextView) findViewById(R.id.valMag_Y);
        magneticZ = (TextView) findViewById(R.id.valMag_Z);

        // Register magnetic sensor
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // Unregister the listener
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        // Unregister the listener
        sensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register magnetic sensor
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Ignoring this for now

    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        synchronized (this) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticX.setText( Float.toString( sensorEvent.values[0]));
                magneticY.setText( Float.toString( sensorEvent.values[1]));
//                magneticZ.setText( Float.toString( sensorEvent.values[2]));
                double magnitude = Math.sqrt((sensorEvent.values[0] * sensorEvent.values[0]) + (sensorEvent.values[1] * sensorEvent.values[1]) + (sensorEvent.values[2] * sensorEvent.values[2]));
                magneticZ.setText( Double.toString( sensorEvent.values[2]));
            }
        }

    }

}