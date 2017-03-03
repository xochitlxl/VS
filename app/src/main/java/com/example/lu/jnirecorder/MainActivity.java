package com.example.lu.jnirecorder;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.lang.Thread.*;


public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("rejni");
    }

    public native String rejnistart();

    public native String rejnistop();


    private static final String LOG_TAG = "RecorderTest1";
    private String FileName = null;
    private Button startnormal;
    private Button stopnormal;
    private Button startRecord;
    private Button binderrec;
    private Button javarec;

    private MediaRecorder mRecorder = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //JNI开始录音
        startRecord = (Button) findViewById(R.id.startrecord);
        //绑定监听器
        startRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String result = rejnistart();//录音5秒
                System.out.println(result);

            }
        });




        //正常录音
        startnormal = (Button)findViewById(R.id.normalre);
        //绑定监听器
        startnormal.setOnClickListener(new startRecordListener());
        //结束录音
        stopnormal = (Button)findViewById(R.id.stopre);
        //绑定监听器
        stopnormal.setOnClickListener(new stopRecordListener());

        FileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        FileName += "/recordertest1.amr";




        //binder录音
        binderrec = (Button) findViewById(R.id.binderre);
        //绑定监听器
        binderrec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    Process nativeApp = Runtime.getRuntime().exec("/system/xbin/su -c /data/data/com.leonnewton.sendsmsreflcet/binder/binder");


                    BufferedReader reader = new BufferedReader(new InputStreamReader(nativeApp.getInputStream()));
                    int read;
                    char[] buffer = new char[4096];
                    StringBuffer output = new StringBuffer();
                    while ((read = reader.read(buffer)) > 0) {
                        output.append(buffer, 0, read);
                    }
                    reader.close();

                    // Waits for the command to finish.
                    nativeApp.waitFor();

                    String nativeOutput = output.toString();
                    System.out.println(nativeOutput);
                    System.out.println("录音结束");


                } catch (IOException e) {

                } catch (InterruptedException e) {

                }

            }
        });



        //java反射
        javarec = (Button) findViewById(R.id.javare);
        javarec.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Class<?> classre =null;
                try {
                    classre = Class.forName("android.media.MediaRecorder");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    Object mRecorder = classre.newInstance();
                    Method source = mRecorder.getClass().getMethod("setAudioSource", int.class);
                    //source.setAccessible(true);
                    source.invoke(mRecorder, 1);
                    Method format = classre.getMethod("setOutputFormat",int.class);
                    format.invoke(mRecorder,0);
                    Method file = classre.getMethod("setOutputFile",String.class);
                    file.invoke(mRecorder, "/mnt/sdcard/javareflect.wma");
                    Method encoder = classre.getMethod("setAudioEncoder",int.class);
                    encoder.invoke(mRecorder, 0);
                    Method pre = classre.getMethod("prepare");
                    pre.invoke(mRecorder);
                    Method sta = classre.getMethod("start");
                    sta.invoke(mRecorder);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Method stopre = classre.getMethod("stop");
                    stopre.invoke(mRecorder);

                }  catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }

            }
        });

    }
    //开始录音
    class startRecordListener implements OnClickListener{
        @Override
        public void onClick(View v){
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            mRecorder.setOutputFile(FileName);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            try{
                mRecorder.prepare();
            }catch (IOException e){
                Log.e(LOG_TAG,"prepare() failed");
            }
            mRecorder.start();
        }
    }

    //停止录音
    class stopRecordListener implements OnClickListener{
        @Override
        public void onClick(View v){
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

}