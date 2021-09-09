package com.example.user.sss;


import android.content.Intent;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageButton;

public class Activity_Alarmsetting extends AppCompatActivity {
    private Button button;
    private Button button2;
    private ImageButton imageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__alarmsetting);

        imageButton = (ImageButton) findViewById(R.id.goback1);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBackPrivate();
            }
        });

        button = (Button) findViewById(R.id.eMailBtn);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                openActivityAuto();
            }
        });

        button2 = (Button) findViewById(R.id.phoneSyncBtn);
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
        Intent intent = new Intent(this, Activity_RegisterMail.class);
        startActivity(intent);
    }
    public void openActivityManual(){
        Intent intent = new Intent(this, Activity_RegisterPhoneNum.class);
        startActivity(intent);
    }
}
