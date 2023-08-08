package com.example.gesturedetectionandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;

public class MessageReceiver extends BroadcastReceiver {

    private Context context;



    public MessageReceiver(Context context){
        this.context = context;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getStringExtra("message");
        //  Log.v(TAG, "Main activity received message: " + message);
        // Display message in UI
        logthis(message);
        // writeFileOnInternalStorage(getApplicationContext(),".csv","meszbazdmeg");

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
            //  System.out.println("meglettttttttttttt");
            // System.out.println(dir.getAbsolutePath());
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public void logthis(String newinfo) {
        if (newinfo.compareTo("") != 0) {
            //  System.out.println(newinfo);
            String[] splitinfo = newinfo.split(":");
            if (splitinfo[0].equalsIgnoreCase("acc")){
                if (Setting.mainActivity != null){
               // Setting.mainActivity.acc.setText(splitinfo[1]);
                String[] szavak = splitinfo[1].split(",");
                Setting.mainActivity.adat1 = Float.parseFloat(szavak[1]);
                Setting.mainActivity.adat2 = Float.parseFloat(szavak[2]);
                    Setting.mainActivity.adat3 = Float.parseFloat(szavak[3]);
               //     System.out.println(Float.parseFloat(szavak[3]));
                    // acc.setText(splitinfo[1]);

                }
                writeFileOnInternalStorage(this.context,Setting.filename+"acc.csv",splitinfo[1]);
                //             writeFileOnInternalStorage(getApplicationContext(),filane1+"gyr.csv","");

            }
            else{
                if (Setting.mainActivity != null) {
                    Setting.mainActivity.gyro.setText(splitinfo[1]);
                }
                //           writeFileOnInternalStorage(getApplicationContext(),filane1+"acc.csv","");
                writeFileOnInternalStorage(this.context,Setting.filename+"gyr.csv",splitinfo[1]);

            }
        }
    }
}
