package com.example.gesturedetectionandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseLocalModel;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseCustomLocalModel;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.MappedByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {


    protected Handler handler;
    TextView acc;
    TextView gyro;
    String filane1;
    TextView filename;
    Button savefile;
    FirebaseModelInterpreter interpreter;
    float adatok[][][] = new float[1][120][3];
    float adat1=0;
    float adat2=0;
    float adat3=0;
    float probaadatok[][] = new float[120][3];
    TextView result;
    ImageView resultimage;


    public void rrun() {
        System.out.println("osztalytas");
        FirebaseModelInputOutputOptions inputOutputOptions =
                null;
        try {
            inputOutputOptions = new FirebaseModelInputOutputOptions.Builder()
                    .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 120,  3})
                    .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 1})
                    .build();
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }
        FirebaseModelInputs inputs = null;
        try {
            inputs = new FirebaseModelInputs.Builder()
                    .add(adatok)  // add() as many input arrays as your model requires
                    .build();
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }
        System.out.println("megmegyek");
        // modelInput = TensorBuffer.createFixedSize(new float[][][]{1,120,3}, DataType.FLOAT32);
        // modelOutput = TensorBuffer.createFixedSize(new float[1][1], DataType.FLOAT32);

        interpreter.run(inputs, inputOutputOptions)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseModelOutputs>() {
                            @Override
                            public void onSuccess(FirebaseModelOutputs result) {
                                // ...
                              //  System.out.println(result.toString());
                                //System.out.println(String.valueOf(result.getOutput(0)));
                                float probacska[][] = result.getOutput(0);
                                System.out.println(probacska[0][0]);
                              //  System.out.println(result.getOutput(0).toString());
                                if (probacska[0][0]<0.5){
                                    resultimage.setImageResource(R.mipmap.eating);
                                    MainActivity.this.result.setText("result :0, result prob:"+probacska[0][0]);
                                }
                                else {
                                    resultimage.setImageResource(R.mipmap.eeating);
                                    MainActivity.this.result.setText("result :1, result prob:"+probacska[0][0]);
                                }

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                // ...
                                System.out.println("bajvan");
                                System.out.println(e);
                            }
                        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScheduledExecutorService executor =  Executors.newScheduledThreadPool(1);
        Runnable periodicTask = new Runnable(){
            @Override
            public void run() {
               // Log.i("megyek","belepet");
               //System.out.println("mentes");
                //adatok[0]= Arrays.copyOfRange(adatok[0], 1, adatok[0].length);
                float proba[][] = adatok[0].clone();
                System.arraycopy(adatok[0], 1, proba, 0, adatok[0].length-1);
              //  System.out.println(probaadatok.length);
              //  System.out.println(probaadatok[0].length);
              //  System.out.println(probaadatok[0][0]);
                proba[proba.length-1][0] = adat1;
                proba[proba.length-1][1] = adat2;
                proba[proba.length-1][2] = adat3;
              //  System.out.println(adat3);
               // System.out.println(adatok[0][119][0]);
              //  System.out.println(adatok[0][119][1]);
              //  System.out.println(adatok[0][119][2]);
              //  Log.i("megyek",String.valueOf(probaadatok[119][0]));
                adatok[0] = proba.clone();
                acc.setText(String.valueOf(adat1)+" "+String.valueOf(adat2)+" "+String.valueOf(adat3));
               // result.setText( adatok[0][119][0]+" "+ adatok[0][119][1]+" "+ adatok[0][119][2]);
            }
        };
        ScheduledFuture<?> periodicFuture = executor.scheduleAtFixedRate(periodicTask, 500, 500, TimeUnit.MILLISECONDS);

        ScheduledExecutorService executor1 =  Executors.newScheduledThreadPool(1);
        Runnable periodicTask1 = new Runnable(){
            @Override
            public void run() {
                System.out.println("osztalytas");
                FirebaseModelInputOutputOptions inputOutputOptions =
                        null;
                try {
                    inputOutputOptions = new FirebaseModelInputOutputOptions.Builder()
                            .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 120,  3})
                            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 1})
                            .build();
                } catch (FirebaseMLException e) {
                    e.printStackTrace();
                }
                FirebaseModelInputs inputs = null;
                try {
                    inputs = new FirebaseModelInputs.Builder()
                            .add(adatok)  // add() as many input arrays as your model requires
                            .build();
                } catch (FirebaseMLException e) {
                    e.printStackTrace();
                }
                System.out.println("megmegyek");

                interpreter.run(inputs, inputOutputOptions)
                        .addOnSuccessListener(
                                new OnSuccessListener<FirebaseModelOutputs>() {
                                    @Override
                                    public void onSuccess(FirebaseModelOutputs result) {
                                        // ...
                                        //  System.out.println(result.toString());
                                        //System.out.println(String.valueOf(result.getOutput(0)));
                                        float probacska[][] = result.getOutput(0);
                                        System.out.println(probacska[0][0]);
                                        //  System.out.println(result.getOutput(0).toString());
                                        if (probacska[0][0]<0.5){
                                            resultimage.setImageResource(R.mipmap.eating);
                                            MainActivity.this.result.setText("result :0, result prob:"+probacska[0][0]);
                                        }
                                        else {
                                            resultimage.setImageResource(R.mipmap.eeating);
                                            MainActivity.this.result.setText("result :1, result prob:"+probacska[0][0]);
                                        }

                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        System.out.println("bajvan");
                                        System.out.println(e);
                                    }
                                });
              }
        };
        ScheduledFuture<?> periodicFuture1 = executor1.scheduleAtFixedRate(periodicTask1, 1, 1, TimeUnit.MINUTES);


        FirebaseCustomRemoteModel remoteModel =
                new FirebaseCustomRemoteModel.Builder("gesture").build();

        FirebaseModelDownloadConditions conditionss = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();

        FirebaseCustomLocalModel localModel = new FirebaseCustomLocalModel.Builder()
                .setAssetFilePath("model/model.tflite")
                .build();

        FirebaseModelManager.getInstance().isModelDownloaded(remoteModel)
                .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean isDownloaded) {
                        FirebaseModelInterpreterOptions options;
                        if (isDownloaded) {
                            options = new FirebaseModelInterpreterOptions.Builder(remoteModel).build();
                            System.out.println("remote");
                        } else {
                            options = new FirebaseModelInterpreterOptions.Builder(localModel).build();
                            System.out.println("local");
                        }
                        try {
                            interpreter = FirebaseModelInterpreter.getInstance(options);
                            rrun();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // ...
                    }
                });




       // System.out.println(adatok[0].length-1);


        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        LocalDateTime now = LocalDateTime.now();
        //System.out.println(dtf.format(now));
        filane1 = dtf.format(now);
        writeFileOnInternalStorage(getApplicationContext(),filane1+"acc.csv","");
        writeFileOnInternalStorage(getApplicationContext(),filane1+"gyr.csv","");
        writeFileOnInternalStorage(getApplicationContext(),filane1+"gra.csv","");
        Setting.filename = filane1;

        setContentView(R.layout.activity_main);
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle stuff = msg.getData();
                logthis(stuff.getString("logthis"));
                return true;
            }
        });
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver(getApplicationContext());
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
        acc = findViewById(R.id.textView2);
        gyro = findViewById(R.id.textView4);
        filename = findViewById(R.id.filename_text);
        savefile = findViewById(R.id.save_button);
        result = findViewById(R.id.resulttextview);
        resultimage = findViewById(R.id.resultimage);
        Setting.mainActivity = this;
        savefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filane1 = filename.getText().toString();
                writeFileOnInternalStorage(getApplicationContext(),filane1+"acc.csv","");
                writeFileOnInternalStorage(getApplicationContext(),filane1+"gyr.csv","");
                writeFileOnInternalStorage(getApplicationContext(),filane1+"gra.csv","");
                Setting.filename = filane1;
            }
        });
    }


    public void writeFileOnInternalStorage(Context mcoContext, String sFileName, String sBody){
       // File dir = new File(mcoContext.getFilesDir(), "mydir");
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File dir = new File(path, "adatok");
        if(!dir.exists()){
            dir.mkdir();
        }

        try {
            File gpxfile = new File(dir, sFileName);
            FileWriter writer = new FileWriter(gpxfile,true);
            writer.append(sBody);
            writer.flush();
            writer.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public void logthis(String newinfo) {
        if (newinfo.compareTo("") != 0) {
           String[] splitinfo = newinfo.split(":");
            System.out.println(splitinfo[0]);
           if (splitinfo[0].equalsIgnoreCase("acc")){
               writeFileOnInternalStorage(getApplicationContext(),filane1+"acc.csv",splitinfo[1]);
           }
           if(splitinfo[0].equalsIgnoreCase("gyr")){
               gyro.setText(splitinfo[1]);
               writeFileOnInternalStorage(getApplicationContext(),filane1+"gyr.csv",splitinfo[1]);
           }
           if(splitinfo[0].equalsIgnoreCase("gra")){
               writeFileOnInternalStorage(getApplicationContext(),filane1+"gra.csv",splitinfo[1]);
           }
        }
    }
}