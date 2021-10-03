package com.example.sss;

/*
지금 여기 있는 센서관련 코드들 삭제해야하는지는 나중에 판단하는게 좋을듯
  --센서 관련 코드의 시작, 끝은 스위치 리스너, Destroy()에 있음--
현재 백그라운드 서비스가 잘 작동하는지 확인(낙하시 파일 저장이 되는가?)한 뒤에
삭제
 */

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelStoreOwner;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;

public class MainActivity extends AppCompatActivity {
    //레이아웃 연동
    private Switch sensorswitch;
    private ImageButton settingBtn;

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
    public SensorQueue sensorQueue = null; // 센서데이터 저장용 큐
    float[] mGravity;
    float[] mGeomagnetic;

    //타이머핸들러
    Timerhandler timerHandler = null;
    private static final int MESSAGE_TIMER_START = 100;
    private static final int MESSAGE_TIMER_REPEAT = 101;
    private static final int MESSAGE_TIMER_STOP = 102;
    int flag;
    int shockflag = 0;

    //스위치 상태 유지 SP
    private SharedPreferences switchSP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //권한 확인, 권한 요청
        int pm1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int pm2 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int pm3 = ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE);
        if(pm1 == PackageManager.PERMISSION_DENIED || pm2 == PackageManager.PERMISSION_DENIED ||
                pm3 == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.FOREGROUND_SERVICE}, MODE_PRIVATE
            );
        }
        Intent i = new Intent();
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if(pm.isIgnoringBatteryOptimizations(packageName)){
            i.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        }else{
            i.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            i.setData(Uri.parse("package: "+packageName));
        }

        //UI 이동 버튼
          //Settings 이동
        settingBtn = (ImageButton)findViewById(R.id.settingBtn);
        settingBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){ openSetting(); }
        });

        //가속도 피치 롤 텍스트
        acc = findViewById(R.id.acc);
        ori_Pitch = findViewById(R.id.pitch);
        ori_Roll = findViewById(R.id.roll);

        //타이머핸들러
        timerHandler = new Timerhandler();

        //중앙 on/off 스위치
        sensorswitch = findViewById(R.id.sensorswitch);
        sensorswitch.setOnCheckedChangeListener(new sensorSwitchListener());

        //On Off스위치 SP
        switchSP = getSharedPreferences("switchSP", MODE_PRIVATE);
        boolean isChecked = switchSP.getBoolean("SWITCH_DATA", false);
        sensorswitch.setChecked(isChecked);

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
        sensorQueue = new SensorQueue();


    }
    @Override
    protected void onStop(){
        super.onStop();
        switchSave();
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

        //stopSensing();
    }

    //UI이동 버튼
       //Settings Open
    public void openSetting(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    //스위치 데이터값 저장함수 SP
    private void switchSave(){
        SharedPreferences.Editor editor = switchSP.edit();
        editor.putBoolean("SWITCH_DATA", sensorswitch.isChecked());
        editor.apply();
    }


    //중앙 on/off 스위치작동 리스너
    class sensorSwitchListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
            if(isChecked){
                //startSensing();
                Intent bgService = new Intent(getApplicationContext(), SensingService.class);
                bgService.putExtra("isStart", true);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    startForegroundService(bgService);
                    //timerHandler.sendEmptyMessage(MESSAGE_TIMER_START);
                }else {
                    startService(bgService);
                    //timerHandler.sendEmptyMessage(MESSAGE_TIMER_START);
                }
            }
            else{
                //stopSensing();
                Intent bgService = new Intent(getApplicationContext(), SensingService.class);
                bgService.putExtra("isStart", false);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    startForegroundService(bgService);
                }else {
                    startService(bgService);
                }
                stopService(bgService);

                //timerHandler.sendEmptyMessage(MESSAGE_TIMER_STOP);
            }
        }
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
    public class SensorData{
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

        //값을 스트링으로 나열
        String getAccValue() {
            return accX + "," + accY + "," + accZ;
        }
        String getOriValue() {
            return oriPitch + "," + oriRoll;
        }
    }
    //센서 데이터 저장, 엑셀로 꺼내오기 위한 큐
    class SensorQueue{
        private int size = 30;
        private double[][] list = new double[size][3];
        private int rear;

        public SensorQueue() {
            rear = size - 1;
        }
        public void enqueue(double acc, double pitch, double roll){
            rear = (rear+1) % size;
            list[rear][0] = acc;
            list[rear][1] = pitch;
            list[rear][2] = roll;
        }

    }

    //가속도센서 리스너
    class AccelometerListener implements SensorEventListener {
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
    class MagneticfieldListener implements SensorEventListener{
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

    //타이머핸들러, 충격감지 및 현재 센서 상태 temp 저장
    class Timerhandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg){
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_TIMER_START:
                    Log.e("TimerHandler", "Timer Start");
                    flag = 0;
                    shockflag = 0;
                    this.removeMessages(MESSAGE_TIMER_REPEAT);
                    this.sendEmptyMessage(MESSAGE_TIMER_REPEAT);
                    break;

                case MESSAGE_TIMER_REPEAT:
                    Log.d("TimerHandler", "Timer Repeat");
                    this.sendEmptyMessageDelayed(MESSAGE_TIMER_REPEAT, 50);
                    //센서 on 후, 약간의 텀을 두어 센서가 작동하는 시간을 기다림
                    if(flag < 5){
                        flag = flag + 1;
                    }else{
                        //센싱 시작
                        SensorData sd = sensortemp;
                        switch (shockflag) {
                            case 0:
                                acc.setText("ACC_VALUE: " + String.valueOf(sd.getAccSize()));
                                ori_Pitch.setText("ORI_PITCH: " + String.valueOf(sd.getPitch()));
                                ori_Roll.setText("ORI_ROLL: " + String.valueOf(sd.getRoll()));
                                sensorQueue.enqueue(sd.getAccSize(), sd.getPitch(), sd.getRoll());
                                //자유낙하 감지, acc가 0에 수렴할때
                                if(sd.getAccSize() < 1.5) shockflag = shockflag + 1;
                                break;

                            case 1: case 2: case 3: case 4:
                                acc.setText("ACC_VALUE: " + String.valueOf(sd.getAccSize()));
                                ori_Pitch.setText("ORI_PITCH: " + String.valueOf(sd.getPitch()));
                                ori_Roll.setText("ORI_ROLL: " + String.valueOf(sd.getRoll()));
                                sensorQueue.enqueue(sd.getAccSize(), sd.getPitch(), sd.getRoll());
                                //계속 자유낙하인 상태이면 case5 이동, 아니면 case0 이동
                                if(sd.getAccSize() < 1.5) shockflag = shockflag + 1;
                                else shockflag = 0;
                                break;

                            //자유 낙하중임을 인식, shockflag가 계속 5를 유지
                            case 5:
                                acc.setText("ACC_VALUE: " + String.valueOf(sd.getAccSize()));
                                ori_Pitch.setText("ORI_PITCH: " + String.valueOf(sd.getPitch()));
                                ori_Roll.setText("ORI_ROLL: " + String.valueOf(sd.getRoll()));
                                sensorQueue.enqueue(sd.getAccSize(), sd.getPitch(), sd.getRoll());
                                //낙하가 끝남, 충격을 크게 받기 때문에 가속도가 높게 나옴
                                if(sd.getAccSize() >= 1.5){
                                    Log.e("CheckShock","Detected Shock!!");
                                    this.removeMessages(MESSAGE_TIMER_REPEAT);
                                    sensorresult = sd; //충격 시 센서 데이터 결과값을 저장
                                    shockflag = shockflag + 1;
                                }
                                break;

                            //큐에 저장하기 위한 과정(충격순간을 중앙으로 보내기)
                            case 6: case 7: case 8: case 9: case 10:
                            case 11: case 12: case 13: case 14: case 15:
                            case 16: case 17: case 18: case 19:
                                acc.setText("ACC_VALUE: " + String.valueOf(sd.getAccSize()));
                                ori_Pitch.setText("ORI_PITCH: " + String.valueOf(sd.getPitch()));
                                ori_Roll.setText("ORI_ROLL: " + String.valueOf(sd.getRoll()));
                                sensorQueue.enqueue(sd.getAccSize(), sd.getPitch(), sd.getRoll());
                                shockflag = shockflag + 1;
                                break;

                            //큐를 엑셀파일로 저장
                            case 20:
                                acc.setText("ACC_VALUE: " + String.valueOf(sd.getAccSize()));
                                ori_Pitch.setText("ORI_PITCH: " + String.valueOf(sd.getPitch()));
                                ori_Roll.setText("ORI_ROLL: " + String.valueOf(sd.getRoll()));
                                sensorQueue.enqueue(sd.getAccSize(), sd.getPitch(), sd.getRoll());
                                // 여기에 큐를 엑셀파일로 추출하고 저장하는 기능 추가
                                // or 큐를 머신러닝을 돌림 충격이라 판단하면 저장
                                shockflag = 0;
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
