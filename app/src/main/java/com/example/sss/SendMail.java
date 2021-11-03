package com.example.sss;

import android.content.Context;
import android.widget.Toast;

import javax.mail.MessagingException;
import javax.mail.SendFailedException;

import androidx.appcompat.app.AppCompatActivity;

public class SendMail extends AppCompatActivity {
    String user = "sssnotreply@gmail.com";
    String password = "potpourri1!";

    public void sendSecurityCode(Context context, String sendTo, String body){
        try{
            GMailSender gMailSender = new GMailSender("sssnotreply@gmail.com", "potpourri1!");
            gMailSender.sendMail("SSS 충격감지!", body, sendTo);
            Toast.makeText(context, "이메일을 성공적으로 보냈습니다.", Toast.LENGTH_SHORT).show();
        }catch (SendFailedException e){
            Toast.makeText(context, "이메일 형식이 잘못되었습니다.", Toast.LENGTH_SHORT).show();
        }catch (MessagingException e){
            Toast.makeText(context, "인터넷 연결을 확인해주십시오", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
