package com.example.user.sss;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.example.user.sss.MainActivity.SensorData;

public class analyze extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private View drawerView;

    TextView whereshock;
    TextView date;
    TextView log;

    ImageButton delete;

    SensorData result = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze);

        result = new SensorData();
        result = ((MainActivity)MainActivity.mContext).sensorresult;

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerView = (View)findViewById(R.id.drawer);

        ImageButton btn_open = (ImageButton)findViewById(R.id.sidebar_open_button);
        btn_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(drawerView);
            }
        });

        drawerLayout.setDrawerListener(listener);
        drawerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        ImageButton btn_close = (ImageButton)findViewById(R.id.butten_cancel);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
            }
        });

        ImageButton btn_gotomain = (ImageButton)findViewById(R.id.back_main);
        btn_gotomain.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                openMain();
            }
        });

        ImageButton delete = findViewById(R.id.deletelog);
        delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                ((MainActivity)MainActivity.mContext).sensorlog = "";
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        whereshock = findViewById(R.id.whereshock);
        date = findViewById(R.id.date);
        log = findViewById(R.id.log);

        String[] log1 = ((MainActivity)MainActivity.mContext).sensorlog.split(",");
        if(log1[0] != "") {
            //최근의 로그 주화면에 표시
            int size = log1.length;
            date.setText(log1[size-2]);
            whereshock.setText(log1[size-1]);

            String logtemp = "";
            //로그파일 사이드바에 표시
            for(int i = size-1 ; i >= 0 ; i = i-2){
                logtemp = logtemp + log1[i-1] + "\n" + log1[i] + "\n" + "---------------------------" + "\n";
            }
            log.setText(logtemp);
        }

    }

    DrawerLayout.DrawerListener listener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View view, float v) {

        }
        @Override
        public void onDrawerOpened(@NonNull View view) {

        }
        @Override
        public void onDrawerClosed(@NonNull View view) {

        }
        @Override
        public void onDrawerStateChanged(int i) {

        }
    };

    public void openMain(){
        super.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }
}
