package com.example.sss;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Calendar;

import javax.mail.MessagingException;
import javax.mail.SendFailedException;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;


public class SensingService extends Service {
    public SensingService() {
    }

    //서비스 사용 메세지키
    public static final String MESSAGE_KEY = "false";

    //알림창 채널 ID
    public static Intent serviceIntent = null;

    //센서
    private SensorManager sensormanager = null;

    private SensorEventListener accLis;
    private SensorEventListener magLis;

    private Sensor accSensor = null;
    private Sensor magSensor = null;

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

    //현재 위치 가져오기용
    private LocationManager locationManager = null;
    private static final int REQUEST_CODE_LOCATION = 2;

    //sound
    SoundPool soundPool;
    int soundID;

    //낙하 시간용
    int fallSec;

    //e-mail보내기
    String body = "";
    String userEmail = "";
    
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("StartService", "onCreate");

        //타이머핸들러
        timerHandler = new Timerhandler();

        //센서 리스너 할당
        sensormanager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert sensormanager != null;
        accSensor = sensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accLis = new AccelometerListener();
        magSensor = sensormanager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        magLis = new MagneticfieldListener();

        //위치 리스너 할당
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        //저장용 센서데이터 생성
        sensortemp = new SensorData();
        sensortemp.setAccValue("0.0", "0.0", "0.0");
        sensortemp.setOriValue("0.0", "0.0");
        sensorresult = new SensorData();
        sensorresult.setAccValue("0.0", "0.0", "0.0");
        sensorresult.setOriValue("0.0", "0.0");
        sensorQueue = new SensorQueue();

        //Sound
        soundPool = new SoundPool(5, AudioManager.STREAM_ALARM, 0);
        soundID = soundPool.load(((MainActivity)MainActivity.mContext), R.raw.alarm,1);

        //낙하시간용
        fallSec = 0;
        initializeNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        super.onStartCommand(intent, flags, startID);
        Log.d("StartService", "onStartCommand");

        //백그라운드 서비스용
        serviceIntent = intent;

        if (intent == null) {
            return Service.START_STICKY; //Sticky로 서비스 유지
        } else {
            boolean msg = intent.getBooleanExtra("isStart", false);

            if (msg) {
                //서비스 시작
                Log.e("StartService", "Service is Start");

                //센싱 시작
                startSensing();
                timerHandler.sendEmptyMessage(MESSAGE_TIMER_START);
            } else {
                //서비스 종료
                Log.e("StartService", "Service is Stop");

                //센싱 종료
                stopSensing();

                timerHandler.sendEmptyMessage(MESSAGE_TIMER_STOP);
            }
        }

        return START_STICKY;
    }

    //센싱 시작
    public void startSensing() {
        sensormanager.registerListener(accLis, accSensor, SensorManager.SENSOR_DELAY_UI);
        sensormanager.registerListener(magLis, magSensor, SensorManager.SENSOR_DELAY_UI);
        Log.i("Start Service Estimation", "Start Service Estimation");
    }

    //센싱 종료
    public void stopSensing() {
        sensormanager.unregisterListener(accLis);
        sensormanager.unregisterListener(magLis);
        Log.i("Stop Service Estimation", "Stop Service Estimation");
    }

    //센서의 데이터 값을 넣기 위한 클래스
    public class SensorData {
        String accX, accY, accZ;
        String oriPitch, oriRoll;

        SensorData() {
            this.accX = "";
            this.accY = "";
            this.accZ = "";

            this.oriPitch = "";
            this.oriRoll = "";
        }

        void setAccValue(String acX, String acY, String acZ) {
            accX = acX;
            accY = acY;
            accZ = acZ;
        }

        void setOriValue(String orPitch, String orRoll) {
            oriPitch = orPitch;
            oriRoll = orRoll;
        }

        double getAccSize() {
            double x = Double.parseDouble(accX);
            double y = Double.parseDouble(accY);
            double z = Double.parseDouble(accZ);
            return Math.sqrt(x * x + y * y + z * z);
        }

        double getPitch() {
            return Double.parseDouble(oriPitch);
        }

        double getRoll() {
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
    class SensorQueue {
        private int size = 30;
        private double[][] list = new double[size][3];
        private int rear;

        public SensorQueue() {
            rear = size - 1;
        }

        public void enqueue(double acc, double pitch, double roll) {
            rear = (rear + 1) % size;
            list[rear][0] = acc;
            list[rear][1] = pitch;
            list[rear][2] = roll;
        }

        public double getAcc(int num) {
            return list[num][0];
        }

        public double[][] getList() {
            return list;
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
    class MagneticfieldListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            mGeomagnetic = event.values;

            float R[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, null, mGravity, mGeomagnetic);
            if (success) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);
                String oriPitch = String.format("%.3f", Math.toDegrees(orientation[1]));
                String oriRoll = String.format("%.3f", Math.toDegrees(orientation[2]));
                sensortemp.setOriValue(oriPitch, oriRoll);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    //센서 타이머 핸들러
    class Timerhandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_TIMER_START:
                    Log.e("Service TimerHandler", "Service Timer Start");
                    flag = 0;
                    shockflag = 0;
                    fallSec = 0;
                    this.removeMessages(MESSAGE_TIMER_REPEAT);
                    this.sendEmptyMessage(MESSAGE_TIMER_REPEAT);
                    break;

                case MESSAGE_TIMER_REPEAT:
                    Log.d("Service TimerHandler", "Service Timer Repeat");
                    this.sendEmptyMessageDelayed(MESSAGE_TIMER_REPEAT, 50);
                    //센서 on 후, 약간의 텀을 두어 센서가 작동하는 시간을 기다림
                    if (flag < 5) {
                        flag = flag + 1;
                    } else {
                        //센싱 시작
                        SensorData sd = sensortemp;
                        switch (shockflag) {
                            case 0:
                                /*
                                acc.setText("ACC_VALUE: " + String.valueOf(sd.getAccSize()));
                                ori_Pitch.setText("ORI_PITCH: " + String.valueOf(sd.getPitch()));
                                ori_Roll.setText("ORI_ROLL: " + String.valueOf(sd.getRoll()));
                                */
                                sensorQueue.enqueue(sd.getAccSize(), sd.getPitch(), sd.getRoll());
                                //자유낙하 감지, acc가 0에 수렴할때
                                if (sd.getAccSize() < 1.5) {shockflag = shockflag + 1;}
                                break;

                            case 1:
                            case 2:
                            case 3:
                                sensorQueue.enqueue(sd.getAccSize(), sd.getPitch(), sd.getRoll());
                                //계속 자유낙하인 상태이면 case5 이동, 아니면 case0 이동
                                if (sd.getAccSize() < 1.5) {shockflag = shockflag + 1; fallSec++;}
                                else {shockflag = 0; fallSec = 0;}
                                break;

                            //자유 낙하중임을 인식, shockflag가 계속 5를 유지
                            case 4:
                                sensorQueue.enqueue(sd.getAccSize(), sd.getPitch(), sd.getRoll());
                                fallSec++;
                                //낙하가 끝남, 충격을 크게 받기 때문에 가속도가 높게 나옴
                                if (sd.getAccSize() >= 5) {
                                    Log.e("CheckShock", "Detected Shock!!");
                                    //this.removeMessages(MESSAGE_TIMER_REPEAT);
                                    sensorresult = sd; //충격 시 센서 데이터 결과값을 저장
                                    shockflag = shockflag + 1;
                                }
                                break;

                            //큐에 저장하기 위한 과정(충격순간을 중앙으로 보내기)
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                            case 9://5
                            case 10:
                            case 11:
                            case 12:
                            case 13:
                            case 14://10
                            case 15:
                            case 16:
                            case 17:
                            case 18://14
                                sensorQueue.enqueue(sd.getAccSize(), sd.getPitch(), sd.getRoll());
                                shockflag = shockflag + 1;
                                break;

                            //큐를 엑셀파일로 저장
                            case 19:
                            default:
                                Log.e("CheckShock", "Save Shock Data");
                                sensorQueue.enqueue(sd.getAccSize(), sd.getPitch(), sd.getRoll());
                                //데이터 파일의 수가 10개가 넘어가면 오래된 1개를 지우고, 숫자-1, 마지막에 생성
                                if (fileList().length >= 10) {
                                    deleteFile(fileList()[0]);
                                    File fa[] = getFilesDir().listFiles();
                                    for (int i = 0; i < fa.length; i++) {
                                        fa[i].renameTo(new File(getFilesDir() + "/Data" + (i + 1) + ".csv"));
                                    }
                                }
                                writeFile();
                                SendMail mailServer = new SendMail();
                                userEmail = ((MainActivity)MainActivity.mContext).getUserEmail();
                                mailServer.sendSecurityCode(getApplicationContext(), userEmail, body);
                                // or 큐를 머신러닝을 돌림 충격이라 판단하면 저장
                                shockflag = 0;
                                fallSec = 0;
                                body = "";
                                break;
                        }
                    }
                    break;
                case MESSAGE_TIMER_STOP:
                    Log.e("Service TimerHandler", "Service Timer Stop");
                    this.removeMessages(MESSAGE_TIMER_REPEAT);
                    break;
            }
        }
    }

    //알림창 초기화
    public void initializeNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = manager.getNotificationChannel("SSS");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (channel == null) {
                channel = new NotificationChannel("SSS", "undead_service", NotificationManager.IMPORTANCE_HIGH);
                manager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "SSS");
        builder.setSmallIcon(R.mipmap.ic_launcher);

        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.bigText("어플을 보려면 누르세요.");
        style.setBigContentTitle(null);
        style.setSummaryText("서비스 동작중");

        builder.setContentText(null);
        builder.setContentTitle(null);
        builder.setOngoing(true);
        builder.setStyle(style);
        builder.setWhen(0);
        builder.setShowWhen(false);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();
        startForeground(1, notification);
    }

    //파일 생성
    public void writeFile() {
        Log.d("MAKE_FILE", "MAKE_FILE");

        float[][] output = new float[][]{{0}};
        float[][] input = new float[][]{{9}, {9}, {9}, {9}, {9}, {9}, {9}, {9}, {9}, {9}, {9}, {9}, {9}, {9},
                {9}, {9}, {9}, {9}, {9}, {9}, {9}, {9}, {9}, {9}, {9}, {9}, {9}, {9}, {9}, {9}};

        File file = new File(this.getFilesDir() + "/Data" + (this.fileList().length + 1) + ".csv");
        try {
            file.createNewFile();
            PrintWriter pw = new PrintWriter(file);
            //위치 저장 1번줄
            Location currentLocation = getMyLocation();
            if(currentLocation != null){
                double latitude = currentLocation.getLatitude(); // 경도
                double longitude = currentLocation.getLongitude(); // 위도
                pw.println(latitude+","+longitude);
                body = latitude+","+longitude;
            }else{
                pw.print("37.517235,127.047325"); // 기본=서울위치
                body = "37.517235,127.047325";
            }
            //Pitch Ori 저장 2번줄
            pw.println(sensorresult.getOriValue());

            //ACC 저장 3번줄~32번줄 30개
            for (int i = 0; i < sensorQueue.size; i++) {
                float temp = (float)sensorQueue.getAcc((sensorQueue.rear + i) % sensorQueue.size);
                pw.println(temp);
                input[i][0] = temp;
                //Log.d("temp", String.valueOf(temp) );
            }
            //Percent tflite line-33
            Interpreter tflite = getTflite("isFall.tflite");
            tflite.run(input, output);
            pw.println(String.valueOf(output[0][0]));
            if(output[0][0]>0.5) soundPool.play(soundID, 1f, 1f, 0, 0, 1f);

            //fallSec line-34
            pw.println((float)fallSec*50/1000);

            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //gps 현재위치 가져오기
    private Location getMyLocation() {
        Location currentLocation = null;
        String locationProvider = LocationManager.GPS_PROVIDER;
        //권한 요청
        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( ((MainActivity)MainActivity.mContext), new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, MODE_PRIVATE
            );
        }else {
            //현재 위치 알아오기
            currentLocation = locationManager.getLastKnownLocation(locationProvider);
        }
        return currentLocation;
    }

    //머신러닝
    private Interpreter getTflite(String modelPath){
        try{
            return new Interpreter(loadModelFile( ((MainActivity)MainActivity.mContext), modelPath));
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            stopForeground(true);
        }

        //죽지않는 서비스를 위한 AlarmReceiver
        Log.d("Service onDestory()", "AlarmReceiver");
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 3);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("StartService", "onBind");
        throw new UnsupportedOperationException("Not yet implemented");
    }
}