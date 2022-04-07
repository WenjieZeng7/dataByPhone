package com.example.sensordemo_type_gyroscope;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class MyService extends Service {
    public MyService() {
    }

    private final String CHANNEL_ID = "my notification channelId";

    private LocationManager locationManager;
    private android.location.LocationListener locationListener;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /**
         * 前台通知
         */
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //地理位置监听器
        locationListener = new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("hello","Lat  " +
                        "" +
                        "itude:"+location.getLatitude()+"\t"+"Longitude"+location.getLongitude());
//                tvProvider.setText("方式：" + location.getProvider());
////                time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(location.getTime()));
//                latitude = location.getLatitude();  //纬度
//                longitude = location.getLongitude();    //经度
//                speed = location.getSpeed();
//                tvTime.setText("时间：" + time);
//                tvLatitude.setText("纬度：" + latitude + " °");
//                tvLongitude.setText("经度：" + longitude + " °");
//                tvAltitude.setText("海拔：" + location.getAltitude() + " m");
//                tvBearing.setText("方向：" + location.getBearing() + " °");
//                tvSpeed.setText("速度：" + speed + " m/s");
//                tvAccuracy.setText("精度：" + location.getAccuracy() + " m\n");
//                if(mqttOk){
//                    publishMessageLOC("time:"+ date +"\n"+ "latitude:"+ latitude +"\n"+"longitude:"+ longitude +"\n"+"speed:"+ speed +"\n");
//                }
            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onCreate() {
        Log.d("hello","service start");
        super.onCreate();
        createNotificationChannel();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,0,new Intent(this,MainActivity.class),0
        );//设置点击通知回到应用,pending的意思就是等待
        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle("demo")
                .setContentText("正在后台定位")
                .setSmallIcon(R.drawable.ic_android_black_24dp)
                .setContentIntent(pendingIntent)  //设置点击通知回到应用
                .build();
        startForeground(1,notification);
    }

    @Override
    public void onDestroy() {
        Log.d("hello","service destroy!");
        super.onDestroy();
    }

    //通过binder实现activity和service之间的通信
    public class MyBinder extends Binder{
        public MyService getService(){
            return MyService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
//        return new MyBinder();
    }

    //创建通知channelId
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  //安卓版本高于8.0
            CharSequence name = "my GNSS notification channel";  //手机设置中心中的通知的名字
            int importance = NotificationManager.IMPORTANCE_DEFAULT;  //开启通知时会振动，若是改为LOW，则不提醒
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

//    //位置的回调
//    public interface LocationCallback {
//        void onLocation(Location location);
//    }
//    private LocationCallback mLocationCallback;
//    private class LocationListener implements android.location.LocationListener {
//        public LocationListener(String provider) {
//            Log.d("hello","GNSS start");
//        }
//        @Override
//        public void onLocationChanged(Location location) {
//            if(mLocationCallback!=null){
//                mLocationCallback.onLocation(location);
//            }
//        }
//    }


}