package com.example.sss;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback{
    private GoogleMap mMap;
    private ImageButton backBtn;
    private DrawerLayout drawerLayout;
    private View drawerView;
    private ImageButton sidebarOn;
    private ImageButton sidebarClose;

    TextView dateTime;
    TextView longlati;
    TextView tfResultText;
    TextView addressText;
    TextView fallTimeText;


    //정보들
    String shockTime = null;
    String latitudeLongitude = "0,0";
    String tfResult = "0";
    String address = "";
    String fallTime = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //뒤로 가기
        backBtn = (ImageButton)findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapActivity.super.onBackPressed();
            }
        });

        // 사이드 바 열기
        sidebarOn = (ImageButton)findViewById(R.id.sidebar_on);
        sidebarOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(drawerView);
            }
        });

        // 사이드 바 닫기
        sidebarClose = (ImageButton) findViewById(R.id.sidebar_off);
        sidebarClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.closeDrawer(drawerView);
            }
        });

        //sidebar
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerView = (View)findViewById(R.id.drawer2);

        //drawerLayout.setDrawerListener(listener);
        drawerLayout.addDrawerListener(listener);
        drawerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        //정보 가져오기
        readCSV();
        if(shockTime != null) {
            dateTime = findViewById(R.id.dateTime);
            dateTime.setText(shockTime);

            longlati = findViewById(R.id.txtLatitudeLongitude);
            longlati.setText("위도,경도: "+latitudeLongitude);

            tfResultText = findViewById(R.id.txtTfResult);
            tfResultText.setText("머신러닝 결과 : "+tfResult);


            addressText = findViewById(R.id.txtAddress);
            String[] temp = new String[2];
            temp = latitudeLongitude.split(",");
            Log.d("double", temp[0]+","+temp[1]);
            address = getCurrentAddress(Double.valueOf(temp[0]), Double.valueOf(temp[1]));
            addressText.setText("주소 : "+address);


            fallTimeText = findViewById(R.id.txtFallTime);
            fallTimeText.setText("머무른 시간 : "+fallTime);
        }

    }

    //google map 표기 관련
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng SEOUL = new LatLng(37.56, 126.97);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(SEOUL);
        markerOptions.title("서울");
        markerOptions.snippet("한국의 수도");
        mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
    }

    DrawerLayout.DrawerListener listener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            //슬라이드 했을때
        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {
            //Drawer가 오픈된 상황일때 호출
        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {
            // 닫힌 상황일 때 호출
        }

        @Override
        public void onDrawerStateChanged(int newState) {
            // 특정상태가 변결될 때 호출
        }
    };

    //csv파일 읽기
    private void readCSV(){
        File fa[] = getFilesDir().listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().startsWith("Data");
            }
        });
        int num = fa.length;
        BufferedReader br = null;
        String path = getFilesDir().getAbsolutePath()+"/Data"+(num)+".csv";
        Log.e("READCSV", path);
        try {
            String line;
            int i = 0;
            br = new BufferedReader(new FileReader(path));
            while((line = br.readLine()) != null){
                switch (i){
                    case 0:
                        shockTime = line;
                        Log.d("READCSV",line);
                        i++;
                        break;
                    case 1:
                        latitudeLongitude = line;
                        Log.d("READCSV",line);
                        i++;
                        break;
                    case 2:
                        tfResult = line;
                        Log.d("READCSV",line);
                        i++;
                        break;
                    case 3:
                        fallTime = line;
                        Log.d("READCSV",line);
                        i++;
                        break;
                    case 4:
                        i++;
                        break;
                    default:
                        break;
                }

            }
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }


        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";
    }

}