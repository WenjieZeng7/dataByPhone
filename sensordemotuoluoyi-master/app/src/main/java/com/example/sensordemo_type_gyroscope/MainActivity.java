package com.example.sensordemo_type_gyroscope;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    private SensorManager sensorManager;    //  传感器管理 对象
    private Sensor gyroscopeSensor;   //    陀螺仪传感器 对象；
    private Sensor accSensor;   //    陀螺仪传感器 对象；
    private SensorEventListener gyroscopeEventListener; //  陀螺仪事件监听器 对象；
    private SensorEventListener accEventListener;
    private TextView txt_show1;
    private TextView txt_show2;

    private String permissionInfo;


    /**
     * 请求位置许可的ID
     */
    private static final int REQUEST_LOCATION = 1;

    /**
     * 位置许可
     */
    private static String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    /**
     * 位置管理器
     */
    private LocationManager locationManager = null;
    private LinearLayout layoutLocation = null;
    private TextView tvProvider = null;
    private TextView tvTime = null;
    private TextView tvLatitude = null;
    private TextView tvLongitude = null;
    private TextView tvAltitude = null;
    private TextView tvBearing = null;
    private TextView tvSpeed = null;
    private TextView tvAccuracy = null;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPersimmions();

        // 获取一个传感器管理器 对象
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // 获取 陀螺仪创安起对象
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        //获取加速度传感器对象
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        // 判断是否获取到了传感器，如果没有的话直接结束activity；
        if (gyroscopeSensor == null) {
            Toast.makeText(this, "The device has no Gyroscope !", Toast.LENGTH_LONG).show();
            finish();
        }
        txt_show1 = findViewById(R.id.txt_show1);
        txt_show2 = findViewById(R.id.txt_show2);

        //位置相关
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE); //获取位置管理对象

        tvProvider = (TextView) findViewById(R.id.tv_provider);
        tvTime = (TextView) findViewById(R.id.tv_time);
        tvLatitude = (TextView) findViewById(R.id.tv_latitude);
        tvLongitude = (TextView) findViewById(R.id.tv_longitude);
        tvAltitude = (TextView) findViewById(R.id.tv_altitude);
        tvBearing = (TextView) findViewById(R.id.tv_bearing);
        tvSpeed = (TextView) findViewById(R.id.tv_speed);
        tvAccuracy = (TextView) findViewById(R.id.tv_accuracy);

        // 获取得到了sensor，则初始化 传感器的 监听器；
        gyroscopeEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                // 当传感器数据发生变化时 ： 进行处理
                // 下面逻辑： 当绕z轴的加速度大于0.5 设置为蓝色； 小于-0.5则设置为黄色；
//                switch (event.sensor.getType()) {
//                    case Sensor.TYPE_GYROSCOPE:
//                        if (event.values[1] > 1.5f) {
//                            getWindow().getDecorView().setBackgroundColor(Color.BLUE);
//                        } else if (event.values[1] < -1.5f) {
//                            getWindow().getDecorView().setBackgroundColor(Color.YELLOW);
//                        }
//                        break;
//                    default:
//                        break;
//                }
                txt_show1.setText("陀螺仪x:"+event.values[0]+"\n"+"陀螺仪y:"+event.values[1]+"\n"+"陀螺仪z:"+event.values[2]+"\n");
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        //加速度监听器
        accEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                // 当传感器数据发生变化时 ： 进行处理
                // 下面逻辑： 当绕z轴的加速度大于0.5 设置为蓝色； 小于-0.5则设置为黄色；
//                switch (event.sensor.getType()) {
//                    case Sensor.TYPE_GYROSCOPE:
//                        if (event.values[1] > 1.5f) {
//                            getWindow().getDecorView().setBackgroundColor(Color.BLUE);
//                        } else if (event.values[1] < -1.5f) {
//                            getWindow().getDecorView().setBackgroundColor(Color.YELLOW);
//                        }
//                        break;
//                    default:
//                        break;
//                }
                txt_show2.setText("加速度x:"+event.values[0]+"\n"+"加速度y:"+event.values[1]+"\n"+"加速度z:"+event.values[2]+"\n");
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        //地理位置监听器
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                            tvProvider.setText("方式："+location.getProvider());
                            tvTime.setText("时间："+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(location.getTime())));
                            tvLatitude.setText("纬度："+location.getLatitude() + " °");
                            tvLongitude.setText("经度："+location.getLongitude() + " °");
                            tvAltitude.setText("海拔："+location.getAltitude() + " m");
                            tvBearing.setText("方向："+location.getBearing() + " °");
                            tvSpeed.setText("速度："+location.getSpeed() + " m/s");
                            tvAccuracy.setText("精度："+location.getAccuracy() + " m");

                }
            };

    }

    @Override
    protected void onResume() {
        super.onResume();
        // 为传感器 配置 监听器 和 相关属性
        sensorManager.registerListener(gyroscopeEventListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(accEventListener, accSensor, SensorManager.SENSOR_DELAY_FASTEST);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//        // 注册位置服务，获取系统位置
//        if (checkPermission()) {
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//        } else {
//            requestLocationPermissions();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 为传感器 注销监听器；
        sensorManager.unregisterListener(gyroscopeEventListener);
        sensorManager.unregisterListener(accEventListener);
        locationManager.removeUpdates(locationListener);
    }

//    /**
//     * 检查权限
//     * @return
//     */
//    private boolean checkPermission() {
//        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED
//                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            return false;
//        }
//        return true;
//    }

//    /**
//     * 请求位置权限
//     */
//    private void requestLocationPermissions() {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                Manifest.permission.ACCESS_FINE_LOCATION)
//                || ActivityCompat.shouldShowRequestPermissionRationale(this,
//                Manifest.permission.ACCESS_COARSE_LOCATION)) {
//            Snackbar.make(layoutLocation, R.string.app_location_permission_demonstrate_access,
//                    Snackbar.LENGTH_INDEFINITE)
//                    .setAction(R.string.app_ok, new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            ActivityCompat.requestPermissions(getActivity(),
//                                    PERMISSIONS_LOCATION, REQUEST_LOCATION);
//                        }
//                    }).show();
//        } else {
//            ActivityCompat.requestPermissions(getActivity(),
//                    PERMISSIONS_LOCATION, REQUEST_LOCATION);
//        }
//    }

    @TargetApi(23)
    private void getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            /*
             * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
             */
            // 读写权限
            if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
            }
            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), 127);
            }
        }
    }

    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(permission)) {
                return true;
            } else {
                permissionsList.add(permission);
                return false;
            }
        } else {
            return true;
        }
    }


    /**
     //     * MQTT建连选项类，输入设备三元组productKey, deviceName和deviceSecret, 生成Mqtt建连参数clientId，username和password.
     //     */
    class AiotMqttOption {
        private String username = "";
        private String password = "";
        private String clientId = "";

        public String getUsername() { return this.username;}
        public String getPassword() { return this.password;}
        public String getClientId() { return this.clientId;}

        /**
         * 获取Mqtt建连选项对象
         * @param productKey 产品秘钥
         * @param deviceName 设备名称
         * @param deviceSecret 设备机密
         * @return AiotMqttOption对象或者NULL
         */
        public AiotMqttOption getMqttOption(String productKey, String deviceName, String deviceSecret) {
            if (productKey == null || deviceName == null || deviceSecret == null) {
                return null;
            }

            try {
                String timestamp = Long.toString(System.currentTimeMillis());

                // clientId
                this.clientId = productKey + "." + deviceName + "|timestamp=" + timestamp +
                        ",_v=paho-android-1.0.0,securemode=2,signmethod=hmacsha256|";

                // userName
                this.username = deviceName + "&" + productKey;

                // password
                String macSrc = "clientId" + productKey + "." + deviceName + "deviceName" +
                        deviceName + "productKey" + productKey + "timestamp" + timestamp;
                String algorithm = "HmacSHA256";
                Mac mac = Mac.getInstance(algorithm);
                SecretKeySpec secretKeySpec = new SecretKeySpec(deviceSecret.getBytes(), algorithm);
                mac.init(secretKeySpec);
                byte[] macRes = mac.doFinal(macSrc.getBytes());
                password = String.format("%064x", new BigInteger(1, macRes));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return this;
        }
    }

}