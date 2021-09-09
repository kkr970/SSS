package com.example.user.sss;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity{

    public static Context mContext;

    private ImageButton buttonAnalyze;
    private ImageButton buttonSetting;
    private Switch sensorswitch;

    private static final int MESSAGE_TIMER_START = 100;
    private static final int MESSAGE_TIMER_REPEAT = 101;
    private static final int MESSAGE_TIMER_STOP = 102;

    private SensorManager mSensorManager = null;

    private SensorEventListener mAccLis;
    private SensorEventListener mOriLis;

    private Sensor mAccelometerSensor = null;
    private Sensor mOrientationSensor = null;

    Timerhandler timerHandler = null;
    SensorData sensortemp = null;
    public SensorData sensorresult = null;
    sensorQueue sensorqueue = null;
    String sensorlog = null;

    TextView acc, ori_Pitch, ori_Roll;

    int flag;
    int shockflag = 0;

    SoundPool soundPool;
    int soundID;

    private SharedPreferences sp;

    String dateResult;
    SimpleDateFormat sfd = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
    String Date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        acc = findViewById(R.id.acc);
        ori_Pitch = findViewById(R.id.pitch);
        ori_Roll = findViewById(R.id.roll);

        timerHandler = new Timerhandler();
        sensorqueue = new sensorQueue();
        sensorlog = "";

        for(int i = 0 ; i < 30 ; i ++){
            sensorqueue.enqueue(9.9, 0, 0);
        }
        sensortemp = new SensorData();
        sensortemp.setAccValue("0.0", "0.0", "0.0");
        sensortemp.setOriValue("0.0", "0.0");
        sensorresult = new SensorData();
        sensorresult.setAccValue("0.0", "0.0", "0.0");
        sensorresult.setOriValue("0.0", "0.0");
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        assert mSensorManager != null;
        mAccelometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mAccLis = new AccelometerListener();
        mOriLis = new OrientationListener();

        soundPool = new SoundPool(5, AudioManager.STREAM_ALARM,0);
        soundID = soundPool.load(this, R.raw.alarm,1);

        sensorswitch = findViewById(R.id.sensorswitch);

        sensorswitch.setOnCheckedChangeListener(new sensorSwitchListener());

        buttonAnalyze = (ImageButton)findViewById(R.id.open_analyze);
        buttonAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAnalyze();
            }
        });

        buttonSetting = (ImageButton)findViewById(R.id.open_setting);
        buttonSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSetting();
            }
        });

        sp = getSharedPreferences("sFile", MODE_PRIVATE);
        sensorlog = sp.getString("text","");

        TimeMethod();
    }

    @Override
    protected void onStop(){
        super.onStop();

        SharedPreferences sharedPreferences = getSharedPreferences("sFile", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String text = sensorlog;
        editor.putString("text",text);

        editor.commit();
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.e("LOG", "onPause()");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("LOG", "onDestroy()");
        stopSensing();
    }


    public void openAnalyze(){
        Intent intent = new Intent(this, analyze.class);
        startActivity(intent);
    }

    public void openSetting(){
        Intent intent = new Intent(this, setting.class);
        startActivity(intent);
    }

    public static class SensorData{
        String accX, accY, accZ;
        String oriPitch, oriRoll;

        SensorData(){
            this.accX = "";
            this.accY = "";
            this.accZ = "";

            this.oriPitch = "";
            this.oriRoll = "";
        }

        void setAccValue(String acX, String acY, String acZ){
            accX = acX;
            accY = acY;
            accZ = acZ;
        }
        void setOriValue(String orPitch, String orRoll){
            oriPitch = orPitch;
            oriRoll = orRoll;
        }
        double getAccSize(){
            double x = Double.parseDouble(accX);
            double y = Double.parseDouble(accY);
            double z = Double.parseDouble(accZ);
            return Math.sqrt(x*x + y*y + z*z);
        }
        double getPitch(){
            return Double.parseDouble(oriPitch);
        }
        double getRoll(){
            return Double.parseDouble(oriRoll);
        }

        String getAccValue() {
            return accX + "," + accY + "," + accZ;
        }
        String getOriValue() {
            return oriPitch + "," + oriRoll;
        }
    }

    //이 원형큐는 큐지만 데이터를 뽑을필요가 없음
    private class sensorQueue{
        private int size = 10;
        private double[][] list = new double[size][3];
        private int rear;

        public sensorQueue(){
            rear = size - 1;
        }
        public void enqueue(double acc, double pitch, double roll){
            rear = (rear+1) % size;
            list[rear][0] = acc;
            list[rear][1] = pitch;
            list[rear][2] = roll;
        }
    }

    private class AccelometerListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {

            String accX = String.format("%.3f",event.values[0]);
            String accY = String.format("%.3f",event.values[1]);
            String accZ = String.format("%.3f",event.values[2]);

            sensortemp.setAccValue( accX , accY, accZ);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
    private class OrientationListener implements SensorEventListener{
        @Override
        public void onSensorChanged(SensorEvent event) {

            String oriPitch = String.format("%.3f",event.values[1]);
            String oriRoll = String.format("%.3f",event.values[2]);

            sensortemp.setOriValue(oriPitch, oriRoll);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    public void startSensing(){
        mSensorManager.registerListener(mAccLis, mAccelometerSensor, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mOriLis, mOrientationSensor, SensorManager.SENSOR_DELAY_UI);
        Log.i("Start Estimation", "Start Estimation");
    }
    public void stopSensing(){
        mSensorManager.unregisterListener(mAccLis);
        mSensorManager.unregisterListener(mOriLis);
        Log.i("Stop Estimation", "Stop Estimation");
    }

    private class Timerhandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg){
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_TIMER_START:
                    Log.e("TimerHandler", "Timer Start");
                    flag = 0;
                    this.removeMessages(MESSAGE_TIMER_REPEAT);
                    this.sendEmptyMessage(MESSAGE_TIMER_REPEAT);
                    break;

                case MESSAGE_TIMER_REPEAT:
                    Log.d("TimerHandler", "Timer Repeat");
                    this.sendEmptyMessageDelayed(MESSAGE_TIMER_REPEAT, 50);
                    if(flag < 5){
                        flag = flag + 1;
                    }else {
                        SensorData sd = sensortemp;
                        switch (shockflag) {
                            case 0:
                                acc.setText(String.valueOf(sd.getAccSize()));
                                ori_Pitch.setText(String.valueOf(sd.getPitch()));
                                ori_Roll.setText(String.valueOf(sd.getRoll()));
                                sensorqueue.enqueue(sd.getAccSize(), sd.getPitch(), sd.getRoll());
                                if(sd.getAccSize() < 1.5) shockflag = shockflag + 1;
                                break;

                            case 1:
                            case 2:
                            case 3:
                            case 4:
                                acc.setText(String.valueOf(sd.getAccSize()));
                                ori_Pitch.setText(String.valueOf(sd.getPitch()));
                                ori_Roll.setText(String.valueOf(sd.getRoll()));
                                sensorqueue.enqueue(sd.getAccSize(), sd.getPitch(), sd.getRoll());
                                if(sd.getAccSize() < 1.5) shockflag = shockflag + 1;
                                else shockflag = 0;
                                break;

                            case 5:
                                acc.setText(String.valueOf(sd.getAccSize()));
                                ori_Pitch.setText(String.valueOf(sd.getPitch()));
                                ori_Roll.setText(String.valueOf(sd.getRoll()));
                                if(sd.getAccSize() >= 1.5){
                                    Log.e("CheckShock","Detected Shock!!");
                                    this.removeMessages(MESSAGE_TIMER_REPEAT);
                                    sensorresult = sd;
                                    soundPool.play(soundID,1f,1f,0,0,1f);
                                    dateResult = Date;
                                    sensorlog = sensorlog + dateResult + "," + whereShock(sd.getPitch(), sd.getRoll()) + ",";
                                    shockflag = 0;
                                }
                                break;
                        }
                    }
                    break;

                case MESSAGE_TIMER_STOP:
                    Log.e("TimerHandler", "Timer Stop");
                    this.removeMessages(MESSAGE_TIMER_REPEAT);
                    break;
            }
        }
    }

    class sensorSwitchListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked) {
                startSensing();
                timerHandler.sendEmptyMessage(MESSAGE_TIMER_START);
            }
            else {
                stopSensing();
                timerHandler.sendEmptyMessage(MESSAGE_TIMER_STOP);
            }
        }
    }

    public String whereShock(double pitch, double roll){
        String temp1 = "";
        String temp2 = "";
        if( (pitch < 10 && pitch > -10) && (roll < 10 && roll > -10)) {
            temp1 = " 뒷면이 아파요";
            temp2 =  "";
        }else if( (pitch <-170 || pitch > 170) && (roll < 10 && roll > -10)){
            temp1 = " 앞면이 아파요";
            temp2 = "";
        }else {
            if(pitch < 10 && pitch > -10){
                temp1 = "이 아파요";
            }else {
                if (pitch <= 0) {
                    temp1 = " 아래가 아파요";
                } else if (pitch > 0) {
                    temp1 =" 위가 아파요";
                }
            }
            if(roll < 10 && roll > -10){
                temp2 = "";
            }else {
                if (roll <= 0) {
                    temp2 = " 오른쪽";
                } else if (roll > 0) {
                    temp2 = " 왼쪽";
                }
            }
        }
        return temp2 + temp1;
    }

    public void TimeMethod(){
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                long now = System.currentTimeMillis();
                Date = sfd.format(now);
            }
        };
        Runnable task = new Runnable() {
            @Override
            public void run() {
                while(true){
                    try{
                        Thread.sleep(1000);
                    }catch (InterruptedException e){}
                    handler.sendEmptyMessage(1);
                }
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }
}
