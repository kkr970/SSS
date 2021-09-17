package com.example.sss;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;

public class MainActivity extends AppCompatActivity {
    //레이아웃 연동
    private Switch sensorswitch;
    //시험용 가속도, 피치, 롤 텍스트
    TextView acc, ori_Pitch, ori_Roll;


    //센서
    private SensorManager mSensorManager = null;

    private SensorEventListener mAccLis;
    private SensorEventListener mMagLis;

    private Sensor mAccelometerSensor = null;
    private Sensor mMagneticfieldSensor = null;

    SensorData sensortemp = null; // 센서데이터 저장용
    public SensorData sensorresult = null; // 센서결과 저장용
    float[] mGravity;
    float[] mGeomagnetic;


    //타이머핸들러
    Timerhandler timerHandler = null;
    private static final int MESSAGE_TIMER_START = 100;
    private static final int MESSAGE_TIMER_REPEAT = 101;
    private static final int MESSAGE_TIMER_STOP = 102;
    int flag;
    int shockflag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //가속도 피치 롤 텍스트
        acc = findViewById(R.id.acc);
        ori_Pitch = findViewById(R.id.pitch);
        ori_Roll = findViewById(R.id.roll);

        //타이머핸들러
        timerHandler = new Timerhandler();

        //센서매니저
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //중앙 on/off 스위치
        sensorswitch = findViewById(R.id.sensorswitch);
        sensorswitch.setOnCheckedChangeListener(new sensorSwitchListener());

        //센서, 리스너 할당
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert mSensorManager != null;
        mAccelometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccLis = new AccelometerListener();
        mMagneticfieldSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mMagLis = new MagneticfieldListener();

        //저장용 센서데이터 생성
        sensortemp = new SensorData();
        sensortemp.setAccValue("0.0", "0.0", "0.0");
        sensortemp.setOriValue("0.0", "0.0");
        sensorresult = new SensorData();
        sensorresult.setAccValue("0.0", "0.0", "0.0");
        sensorresult.setOriValue("0.0", "0.0");

    }
    @Override
    protected void onStop(){
        super.onStop();
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

    //센싱 시작
    public void startSensing(){
        mSensorManager.registerListener(mAccLis, mAccelometerSensor, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mMagLis, mMagneticfieldSensor, SensorManager.SENSOR_DELAY_UI);
        Log.i("Start Estimation", "Start Estimation");
    }
    //센싱 종료
    public void stopSensing(){
        mSensorManager.unregisterListener(mAccLis);
        mSensorManager.unregisterListener(mMagLis);
        Log.i("Stop Estimation", "Stop Estimation");
    }


    //센서의 데이터 값을 넣기 위한 클래스
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


    //가속도센서 리스너
    private class AccelometerListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            mGravity = event.values;

            String accX = String.format("%.3f", event.values[0]);
            String accY = String.format("%.3f", event.values[1]);
            String accZ = String.format("%.3f", event.values[2]);

            sensortemp.setAccValue(accX, accY, accZ);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
    //자기장센서 리스너
    private class MagneticfieldListener implements SensorEventListener{
        @Override
        public void onSensorChanged(SensorEvent event) {
            mGeomagnetic = event.values;

            float R[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, null, mGravity, mGeomagnetic);
            if(success){
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                String oriPitch = String.format("%.3f",Math.toDegrees(orientation[1]));
                String oriRoll = String.format("%.3f",Math.toDegrees(orientation[2]));
                sensortemp.setOriValue(oriPitch, oriRoll);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }


    //중앙 on/off 스위치작동 리스너
    class sensorSwitchListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
            if(isChecked){
                startSensing();
                timerHandler.sendEmptyMessage(MESSAGE_TIMER_START);
            }
            else{
                stopSensing();
                timerHandler.sendEmptyMessage(MESSAGE_TIMER_STOP);
            }
        }
    }

    //타이머핸들러
    private class Timerhandler extends Handler{
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
                                acc.setText("ACC_VALUE: " + String.valueOf(sd.getAccSize()));
                                ori_Pitch.setText("ORI_PITCH: " + String.valueOf(sd.getPitch()));
                                ori_Roll.setText("ORI_ROLL: " + String.valueOf(sd.getRoll()));
                                if(sd.getAccSize() < 1.5) shockflag = shockflag + 1;
                                break;

                            case 1:
                            case 2:
                            case 3:
                            case 4:
                                acc.setText(String.valueOf(sd.getAccSize()));
                                ori_Pitch.setText(String.valueOf(sd.getPitch()));
                                ori_Roll.setText(String.valueOf(sd.getRoll()));
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
}
