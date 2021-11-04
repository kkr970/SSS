package com.example.sss;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class RegisterActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private EditText userEmail;
    private Button submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //사용자 이메일 칸
        userEmail = (EditText)findViewById(R.id.userEmail);

        backBtn = (ImageButton)findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterActivity.super.onBackPressed();
            }
        });

        //이메일 등록 버튼
        submitBtn = (Button)findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //등록 버튼 이벤트
                if(submitBtn.getText() == "수정"){
                    submitBtn.setText("등록");
                    submitBtn.setBackgroundResource(R.drawable.submit_btn_on);
                    userEmail.setHint("E-mail을 입력하세요.");
                    userEmail.setEnabled(true);
                } else {
                    submitBtn.setText("수정");
                    submitBtn.setBackgroundResource(R.drawable.submit_btn);
                    userEmail.setHint("수정 버튼을 눌러 E-mail을 입력하세요.");
                    userEmail.setEnabled(false);
                }
            }
        });

    }
}