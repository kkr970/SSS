package com.example.sss;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelStoreOwner;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Timer;

public class MainActivity extends AppCompatActivity {
    //레이아웃 연동
    private Switch sensorswitch;
    private ImageButton settingBtn;
    private ImageButton analyzeBtn;
    private Button activateBtn;

    //스위치 상태 유지 SP
    private SharedPreferences switchSP;

    //다른엑티비티 연동
    public static Context mContext;

    //하단 네비게이션 바 생략
    private View decorView;
    private int uiOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //바 생략
        decorView = getWindow().getDecorView();
        uiOption = getWindow().getDecorView().getSystemUiVisibility();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            uiOption |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            uiOption |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            uiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        //권한 확인, 권한 요청
        int pm1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int pm2 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int pm3 = ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE);
        int pm4 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int pm5 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if(pm1 == PackageManager.PERMISSION_DENIED || pm2 == PackageManager.PERMISSION_DENIED ||
           pm3 == PackageManager.PERMISSION_DENIED || pm4 == PackageManager.PERMISSION_DENIED||
           pm5 == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, MODE_PRIVATE
            );
        }
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .permitDiskReads()
                .permitDiskWrites()
                .permitNetwork().build());

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


        //중앙 on/off 스위치
        sensorswitch = findViewById(R.id.sensorswitch);
        sensorswitch.setOnCheckedChangeListener(new sensorSwitchListener());

        //지도 화면 이동
        analyzeBtn = (ImageButton)findViewById(R.id.analyzeBtn);
        analyzeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { openChart(); }
        });

        mContext = this;

        activateBtn = (Button)findViewById(R.id.activateBtn);
        activateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorswitch.setChecked(!sensorswitch.isChecked());
            }
        });

        //On Off스위치 SP
        switchSP = getSharedPreferences("switchSP", MODE_PRIVATE);
        boolean isChecked = switchSP.getBoolean("SWITCH_DATA", false);
        sensorswitch.setChecked(isChecked);
    }

    //내비게이션 바, 액션 바, 풀 스크린
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(hasFocus){
            decorView.setSystemUiVisibility(uiOption);
        }
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

    //지도 이동 버튼
    private void openChart() {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }


    //중앙 on/off 스위치작동 리스너
    class sensorSwitchListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
            if(isChecked){
                activateBtn.setBackgroundResource(R.drawable.active_btn_on);
                activateBtn.setText("ON");
                Intent bgService = new Intent(getApplicationContext(), SensingService.class);
                bgService.putExtra("isStart", true);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    startForegroundService(bgService);
                }else {
                    startService(bgService);
                }
            }
            else{
                activateBtn.setBackgroundResource(R.drawable.active_btn);
                activateBtn.setText("OFF");
                Intent bgService = new Intent(getApplicationContext(), SensingService.class);
                bgService.putExtra("isStart", false);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    startForegroundService(bgService);
                }else {
                    startService(bgService);
                }
                stopService(bgService);
            }
        }
    }

    public Activity getActivity(){
        return this;
    }
}
