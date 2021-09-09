package com.example.user.sss;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class Activity_Register extends AppCompatActivity {

    private ImageButton imageButton;
    private Button button;
    private Button button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__register);

        imageButton = (ImageButton) findViewById(R.id.goback);
        button = (Button) findViewById(R.id.autoBtn);
        button2 = (Button) findViewById(R.id.manBtn);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBackPrivate();
            }
        });
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                openActivityAuto();
            }
        });
        button2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                openActivityManual();
            }
        });
    }
    public void goBackPrivate(){
        super.onBackPressed();
    }
    public void openActivityAuto(){
        Intent intent = new Intent(this, Activity_Auto.class);
        startActivity(intent);
    }
    public void openActivityManual(){
        Intent intent = new Intent(this, Activity_Manual.class);
        startActivity(intent);
    }
}
