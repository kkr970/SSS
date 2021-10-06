package com.example.sss;

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
import android.os.Environment;
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

import java.io.File;
import java.util.Timer;

public class MainActivity extends AppCompatActivity {
    //레이아웃 연동
    private Switch sensorswitch;
    private ImageButton settingBtn;
    private ImageButton analyzeBtn;


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


        //중앙 on/off 스위치
        sensorswitch = findViewById(R.id.sensorswitch);
        sensorswitch.setOnCheckedChangeListener(new sensorSwitchListener());

        //On Off스위치 SP
        switchSP = getSharedPreferences("switchSP", MODE_PRIVATE);
        boolean isChecked = switchSP.getBoolean("SWITCH_DATA", false);
        sensorswitch.setChecked(isChecked);

        //지도 화면 이동
        analyzeBtn = (ImageButton)findViewById(R.id.analyzeBtn);
        analyzeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {openChart(); }
        });
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
                Intent bgService = new Intent(getApplicationContext(), SensingService.class);
                bgService.putExtra("isStart", true);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    startForegroundService(bgService);
                }else {
                    startService(bgService);
                }
            }
            else{
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
}
