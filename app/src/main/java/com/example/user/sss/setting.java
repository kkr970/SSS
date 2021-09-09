package com.example.user.sss;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class setting extends AppCompatActivity{
    private ImageButton imgButton;
    private Button btn1;
    private Button btn2;
    private Button btn3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        imgButton = (ImageButton) findViewById(R.id.imageButton1);
        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMain();
            }
        });

        btn1 = (Button) findViewById(R.id.register);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRegister();
            }
        });

        btn2 = (Button) findViewById(R.id.alarm);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAlarm_setting();
            }
        });

        btn3 = (Button) findViewById(R.id.data);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openData_setting();
            }
        });


    }

    public void openMain(){
        super.onBackPressed();
    }

    public void openRegister(){
        Intent intent = new Intent(this, Activity_Register.class);
        startActivity(intent);
    }
    public void openAlarm_setting(){
        Intent intent = new Intent(this, Activity_Alarmsetting.class);
        startActivity(intent);
    }
    public void openData_setting(){
        Intent intent = new Intent(this, Activity_Datasetting.class);
        startActivity(intent);
    }
}
