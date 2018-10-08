package com.example.boguslaw.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://192.168.43.223:3000/");
        } catch (URISyntaxException e) {}
    }
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private TextView textView;
    private double x = 0;
    private double y = 0;
    private double z = 0;
    private int iterator = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(MainActivity.this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        textView = (TextView) findViewById(R.id.textView);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(iterator > 1000) iterator = 0;
                textView.setText(String.format("X:\t\t\t %.4f \nY:\t\t\t %.4f \nZ:\t\t\t %.4f \nIterator: %d", x, y, z,iterator));

                JSONObject accelerometerDate = new JSONObject();
                try {
                    accelerometerDate.put("x", x);
                    accelerometerDate.put("y", y);
                    accelerometerDate.put("z", z);
                    accelerometerDate.put("i", iterator);
                    mSocket.emit("message", accelerometerDate);
                    iterator ++;
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }, 0, 200);
        mSocket.connect();
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

        }

/*        @Override
        public void call(final Object.. args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }*/
    };

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        x = sensorEvent.values[0];
        y = sensorEvent.values[1];
        z = sensorEvent.values[2];
        //if(iterator > 1000) iterator = 0;
        //textView.setText(String.format("X: %.4f \nY: %.4f \nZ: %.4f\nI: %d", x, y, z, iterator));

       /* JSONObject accelerometerDate = new JSONObject();
        try {
            accelerometerDate.put("x", x);
            accelerometerDate.put("y", y);
            accelerometerDate.put("z", z);
            accelerometerDate.put("i", iterator);
            mSocket.emit("message", accelerometerDate);
            iterator ++;
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mSocket.off("message", onNewMessage);
    }
}

