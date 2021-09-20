package com.example.sss;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class SensingService extends Service {

    public static final String MESSAGE_KEY = "false";
    static final String CHANNEL_ID = "";

    public SensingService() {
    }

    @Override
    public void onCreate(){
        Log.d("StartService", "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID){
        Log.d("StartService", "onStartCommand");
        if(intent == null){
            return Service.START_STICKY;
        }else{
            boolean msg = intent.getBooleanExtra("isStart", false);

            NotificationManager mNotifyManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel;
            NotificationCompat.Builder mBuilder;

            if(msg){
                Log.e("StartService", "is start!!!!!!!!!!");
                Intent mMainIntent = new Intent(this, MainActivity.class);
                PendingIntent mPendingIntent = PendingIntent.getActivity(
                        this, 1, mMainIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    notificationChannel = new NotificationChannel(
                            "sensing_channel_id",
                            "센싱",
                            NotificationManager.IMPORTANCE_DEFAULT
                    );
                    notificationChannel.setDescription("센싱");
                    mNotifyManager.createNotificationChannel(notificationChannel);
                }
                mBuilder =
                        new NotificationCompat.Builder(this, "sensing_channel_id")
                        .setSmallIcon(android.R.drawable.btn_star)
                        .setContentTitle("Sensing")
                        .setContentIntent(mPendingIntent)
                        .setContentText("낙하를 감지하는 중입니다.")
                        .setAutoCancel(false)
                        .setOngoing(true);
                mNotifyManager.notify(001, mBuilder.build());
            }else{
                Log.e("StartService", "is stop!!!!!!!!!!!!!!!!!!!!");
                mNotifyManager.cancelAll();
            }
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("Startservice", "onBind");
        throw new UnsupportedOperationException("Not yet implemented");
    }

}