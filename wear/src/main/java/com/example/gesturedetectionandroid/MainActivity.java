package com.example.gesturedetectionandroid;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gesturedetectionandroid.databinding.ActivityMainBinding;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Button startbutton;
    private Button stopbutton;
    private TextView status;
    private ActivityMainBinding binding;

    Node mNode; // the connected device to send the message to
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        startbutton = binding.watchStartButton;
        stopbutton = binding.watchStopButton;
        status = binding.watchStatusLabel;
        status.setBackgroundColor(Color.RED);
        stopbutton.setEnabled(false);
        startbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    status.setBackgroundColor(Color.GREEN);
                    stopbutton.setEnabled(true);
                    startbutton.setEnabled(false);
                //    accelerometer.register();
                //    gyroscope.register();
                Intent services = new Intent(MainActivity.this,ForgeGroundService.class);
                startService(services);
                Settings.service = services;

            }
        });
        stopbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startbutton.setEnabled(true);
                stopbutton.setEnabled(false);
                status.setBackgroundColor(Color.RED);
              //  accelerometer.unregister();
              //  gyroscope.unregister();
                stopService(Settings.service);
                Settings.service=null;
            }
        });
        if (Settings.isrunning){
            status.setBackgroundColor(Color.GREEN);
            stopbutton.setEnabled(true);
            startbutton.setEnabled(false);
        }
        else{
            startbutton.setEnabled(true);
            stopbutton.setEnabled(false);
            status.setBackgroundColor(Color.RED);
        }
    }




    @Override
    protected void onStart() {
        super.onStart();
    }

    private void resolveNode() {

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                        for (Node node : nodes.getNodes()) {
                            mNode = node;
                        }
                    }
                });
    }




    @Override
    protected void onResume() {
        super.onResume();

        // this will send notification to
        // both the sensors to register
        //accelerometer.register();
       // gyroscope.register();
    }

    // create on pause method
    @Override
    protected void onPause() {
        super.onPause();

        // this will send notification in
        // both the sensors to unregister
      //  accelerometer.unregister();
      //  gyroscope.unregister();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        resolveNode();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}