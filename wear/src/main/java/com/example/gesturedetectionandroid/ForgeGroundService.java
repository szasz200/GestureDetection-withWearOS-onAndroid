package com.example.gesturedetectionandroid;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.Provider;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ForgeGroundService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private Accelerometer accelerometer;
    private Gyroscope gyroscope;
    private Gravity gravity;
    String datapath = "/message_path";
    public static String TAG = "WearListActivity";
    public static NotificationChannel CHANNEL = new NotificationChannel("proba","A neve", NotificationManager.IMPORTANCE_NONE);
    Node mNode; // the connected device to send the message to
    GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError=false;

    public static String SERVICE_CALLED_WEAR = "WearListClicked";




    @Override
    public void onCreate(){
        super.onCreate();
        accelerometer = new Accelerometer(this);
        gyroscope = new Gyroscope(this);
        gravity = new Gravity(this);
        accelerometer.setListener(new Accelerometer.Listener() {
            //on translation method of accelerometer
            @Override
            public void onTranslation(long timestamp,float tx, float ty, float ts) {
                // set the color red if the device moves in positive x axis
                // System.out.println(System.currentTimeMillis()+" ACC:"+tx+","+ty+','+ts);
                new ForgeGroundService.SendThread(datapath, "acc:"+timestamp+","+tx+","+ty+","+ts+"\n").start();
            }
        });

        // create a listener for gyroscope
        gyroscope.setListener(new Gyroscope.Listener() {
            // on rotation method of gyroscope
            @Override
            public void onRotation(long timestemp,float rx, float ry, float rz) {
                // set the color green if the device rotates on positive z axis
                //System.out.println(System.currentTimeMillis()+" GYR:" + rx + "," + ry + ',' + rz);
                new ForgeGroundService.SendThread(datapath, "gyr:"+timestemp+","+rx+","+ry+","+rz+"\n").start();
            }
        });

        gravity.setListener(new Gravity.Listener() {
            @Override
            public void onRotation(long timestamp, float tx, float ty, float ts) {
                new ForgeGroundService.SendThread(datapath, "gra:"+timestamp+","+tx+","+ty+","+ts+"\n").start();
            }
        });
        mGoogleApiClient = new GoogleApiClient.Builder(this)
              .addApi(Wearable.API)
            .addConnectionCallbacks(this)
           .addOnConnectionFailedListener(this)
          .build();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
     //   System.out.println("vege");
        accelerometer.unregister();
        gyroscope.unregister();
        Settings.isrunning = false;

    }




    @Override
    public int onStartCommand(Intent intent,int flags,int stratId){
        Settings.isrunning = true;
        accelerometer.register();
        gyroscope.register();
        gravity.register();
        CHANNEL.setLightColor(Color.BLUE);
        CHANNEL.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(CHANNEL);
        Intent notiintent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notiintent, 0);

        Notification notification =
                new Notification.Builder(this, "proba")
                        .setContentTitle("megy ugye")
                        .setContentText("fut az alkalmazas")
                        .setSmallIcon(R.drawable.googleg_standard_color_18)
                        .setContentIntent(pendingIntent)
                        .setTicker("proba")
                        .build();

        startForeground(101,notification);


        return START_REDELIVER_INTENT;
    }






    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    class SendThread extends Thread {
        String path;
        String message;

        //constructor
        SendThread(String p, String msg) {
            path = p;
            message = msg;
        }

        //sends the message via the thread.  this will send to all wearables connected, but
        //since there is (should only?) be one, so no problem.
        public void run() {
            //first get all the nodes, ie connected wearable devices.
            Task<List<Node>> nodeListTask =
                    Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            try {
                // Block on a task and get the result synchronously (because this is on a background
                // thread).
                List<Node> nodes = Tasks.await(nodeListTask);

                //Now send the message to each device.
                for (Node node : nodes) {
                    Task<Integer> sendMessageTask =
                            Wearable.getMessageClient(getApplicationContext()).sendMessage(node.getId(), path, message.getBytes());

                    try {
                        // Block on a task and get the result synchronously (because this is on a background
                        // thread).
                        Integer result = Tasks.await(sendMessageTask);
                    //    Log.v(TAG, "SendThread: message send to " + node.getDisplayName());

                    } catch (ExecutionException exception) {
                        Log.e(TAG, "Task failed: " + exception);

                    } catch (InterruptedException exception) {
                        Log.e(TAG, "Interrupt occurred: " + exception);
                    }

                }

            } catch (ExecutionException exception) {
                Log.e(TAG, "Task failed: " + exception);

            } catch (InterruptedException exception) {
                Log.e(TAG, "Interrupt occurred: " + exception);
            }
        }
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
