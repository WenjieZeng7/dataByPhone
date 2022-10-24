package com.example.sensordemo_type_gyroscope;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
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
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class MainActivity extends AppCompatActivity {

    private SensorManager sensorManager;    //  传感器管理 对象
    private Sensor gyroscopeSensor;   //    陀螺仪传感器 对象；
    private Sensor accSensor;   //    加速度传感器 对象；
    private SensorEventListener gyroscopeEventListener; //  陀螺仪事件监听器 对象；
    private SensorEventListener accEventListener;
    private TextView accelerateX;  //加速度
    private TextView accelerateY;
    private TextView accelerateZ;
    private TextView angleX;  //陀螺仪
    private TextView angleY;
    private TextView angleZ;


    private String permissionInfo;


    /**
     * 位置管理器
     */
    private LocationManager locationManager;
    private LocationListener locationListener;
    //    private LinearLayout layoutLocation = null;
    private TextView tvProvider = null;
    private TextView tvTime = null;
    private TextView tvLatitude = null;
    private TextView tvLongitude = null;
    private TextView tvAltitude = null;
    private TextView tvBearing = null;
    private TextView tvSpeed = null;
    private TextView tvAccuracy = null;


    public float accelerateX_value;
    public float accelerateY_value;
    public float accelerateZ_value;
    public float angleX_value;
    public float angleY_value;
    public float angleZ_value;
    private String time;
    private double latitude;
    private double longitude;
    private float speed;


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


    //MQTT
    private final String TAG = "AiotMqtt";
    /* 设备三元组信息 */
    final private String PRODUCTKEY = "gmll0B3WENe";
    final private String DEVICENAME = "test";
    final private String DEVICESECRET = "81690e1ee1a86b12ca73b42378329ddf";

    /* 自动Topic, 用于上报消息 */
    final private String TOPIC = "/" + PRODUCTKEY + "/" + DEVICENAME + "/user/update";
    /* 自动Topic, 用于接受消息 */
    final private String SUB_TOPIC = "/" + PRODUCTKEY + "/" + DEVICENAME + "/user/get";

    /* 阿里云Mqtt服务器域名 */
    final String host = "tcp://" + PRODUCTKEY + ".iot-as-mqtt.cn-shanghai.aliyuncs.com:443";
    //    final String host = "iot-06z00e2ppeme0ap.mqtt.iothub.aliyuncs.com";
    private String clientId;
    private String userName;
    private String passWord;

    MqttAndroidClient mqttAndroidClient;
    private boolean mqttOk = false;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPersimmions();  //定位权限


        accelerateX = (TextView) findViewById(R.id.accelerateX);
        accelerateY = (TextView) findViewById(R.id.accelerateY);
        accelerateZ = (TextView) findViewById(R.id.accelerateZ);

        angleX = (TextView) findViewById(R.id.angleX);
        angleY = (TextView) findViewById(R.id.angleY);
        angleZ = (TextView) findViewById(R.id.angleZ);

        tvProvider = (TextView) findViewById(R.id.tv_provider);
        tvTime = (TextView) findViewById(R.id.tv_time);
        tvLatitude = (TextView) findViewById(R.id.tv_latitude);
        tvLongitude = (TextView) findViewById(R.id.tv_longitude);
        tvAltitude = (TextView) findViewById(R.id.tv_altitude);
        tvBearing = (TextView) findViewById(R.id.tv_bearing);
        tvSpeed = (TextView) findViewById(R.id.tv_speed);
        tvAccuracy = (TextView) findViewById(R.id.tv_accuracy);


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
        //位置相关
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE); //获取位置管理对象


        // 获取得到了sensor，则初始化 传感器的 监听器；
        gyroscopeEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                angleX_value = event.values[0];
                angleY_value = event.values[1];
                angleZ_value = event.values[2];
                angleX.setText("陀螺仪x:" + angleX_value);
                angleY.setText("陀螺仪y:" + angleY_value);
                angleZ.setText("陀螺仪z:" + angleZ_value + "\n");
//                if(mqttOk){
//                    publishMessageGYR("time:"+ date + "陀螺仪x:" + v0 + "\n" + "陀螺仪y:" + v1 + "\n" + "陀螺仪z:" + v2 + "\n");
//                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        //加速度监听器
        accEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                accelerateX_value = event.values[0];
                accelerateY_value = event.values[1];
                accelerateZ_value = event.values[2];
                accelerateX.setText("加速度x:" + accelerateX_value);
                accelerateY.setText("加速度y:" + accelerateX_value);
                accelerateZ.setText("加速度z:" + accelerateX_value + "\n");
//                if(mqttOk){
//                    publishMessageACC("time:"+ date + "加速度x:"+ v0 +"\n"+"加速度y:"+ v1 +"\n"+"加速度z:"+ v2 +"\n");
//                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

//        地理位置监听器
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                getTime();
                tvProvider.setText("方式：" + location.getProvider());
//                time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(location.getTime()));
                latitude = location.getLatitude();  //纬度
                longitude = location.getLongitude();    //经度
                speed = location.getSpeed();
                tvTime.setText("时间：" + time);
                tvLatitude.setText("纬度：" + latitude + " °");
                tvLongitude.setText("经度：" + longitude + " °");
                tvAltitude.setText("海拔：" + location.getAltitude() + " m");
                tvBearing.setText("方向：" + location.getBearing() + " °");
                tvSpeed.setText("速度：" + speed + " m/s");
                tvAccuracy.setText("精度：" + location.getAccuracy() + " m\n");
//                if(mqttOk){
//                    publishMessageLOC("time:"+ date +"\n"+ "latitude:"+ latitude +"\n"+"longitude:"+ longitude +"\n"+"speed:"+ speed +"\n");
//                }
            }
        };

        sensorManager.registerListener(gyroscopeEventListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(accEventListener, accSensor, SensorManager.SENSOR_DELAY_GAME); //GAME的类型，其频率是适中的
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        addData();
        sendData();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // 为传感器 配置 监听器 和 相关属性
        sensorManager.registerListener(gyroscopeEventListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(accEventListener, accSensor, SensorManager.SENSOR_DELAY_GAME); //GAME的类型，其频率是适中的
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
        // 与onStop()和onDestroy()分别表示结束任务时的工作。
        // 为传感器 注销监听器；
//        sensorManager.unregisterListener(gyroscopeEventListener);
//        sensorManager.unregisterListener(accEventListener);
//        locationManager.removeUpdates(locationListener);
    }


    private boolean isRecord;

    /**
     * 写入SQLite
     */
    private void addData() {
        MyDatabaseHelper dbHelper = new MyDatabaseHelper(this, "SQLite1.db", null, 1);
        dbHelper.getWritableDatabase();
        Switch isLocalSave = (Switch) findViewById(R.id.isLocalSave);
        isLocalSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(MainActivity.this, "开始保存", Toast.LENGTH_SHORT).show();
                    {
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                getTime();
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put("time", time);
                                values.put("accelerateX", accelerateX_value);
                                values.put("accelerateY", accelerateY_value);
                                values.put("accelerateZ", accelerateZ_value);
                                values.put("angleX", angleX_value);
                                values.put("angleY", angleY_value);
                                values.put("angleZ", angleZ_value);
                                values.put("latitude", latitude);
                                values.put("longitude", longitude);
                                values.put("speed", speed);
                                db.insert("Sensor1", null, values);
                            }
                        }, 0, 200);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "结束保存", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void sendData() {
        Switch isRemoteSave = (Switch) findViewById(R.id.isRemoteSave);
        Timer timer = new Timer();
        isRemoteSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //MQTT
                    /* 获取Mqtt建连信息clientId, username, password */
                    AiotMqttOption aiotMqttOption = new AiotMqttOption().getMqttOption(PRODUCTKEY, DEVICENAME, DEVICESECRET);
                    if (aiotMqttOption == null) {
                        Log.e(TAG, "device info error");
                    } else {
                        clientId = aiotMqttOption.getClientId();
                        userName = aiotMqttOption.getUsername();
                        passWord = aiotMqttOption.getPassword();
                    }
//
                    /* 创建MqttConnectOptions对象并配置username和password */
                    MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
                    mqttConnectOptions.setUserName(userName);
                    mqttConnectOptions.setPassword(passWord.toCharArray());


                    /* 创建MqttAndroidClient对象, 并设置回调接口 */
                    mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), host, clientId);
                    mqttAndroidClient.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable cause) {
                            Log.i(TAG, "connection lost");
                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {
                            Log.i(TAG, "topic: " + topic + ", msg: " + new String(message.getPayload()));
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {
                            Log.i(TAG, "msg delivered");
                        }
                    });


                    /* Mqtt建连 */
                    try {
                        mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                Log.i(TAG, "connect succeed");
                                subscribeTopic(SUB_TOPIC);
                                mqttOk = true;
                                Toast.makeText(MainActivity.this, "服务器连接成功", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                Log.i(TAG, "connect failed");
                            }
                        });
                    } catch (MqttException e) {
                        Toast.makeText(MainActivity.this, "服务器连接失败", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    //MQTT

                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (mqttOk) {
                                getTime();
                                publishMessage("time:" + time + "加速度x:" + accelerateX_value + "\n" + "加速度y:" + accelerateY_value + "\n" + "加速度z:" + accelerateZ_value + "\n"
                                        + "陀螺仪x:" + new BigDecimal(Float.toString(angleX_value))  + "\n" + "陀螺仪y:" + new BigDecimal(Float.toString(angleY_value)) + "\n" + "陀螺仪z:" + new BigDecimal(Float.toString(angleZ_value)) + "\n"
                                        + "latitude:" + latitude + "\n" + "longitude:" + longitude + "\n" + "speed:" + speed + "\n");
                            }
                        }
                    }, 0, 200);


                } else {
                    timer.cancel();
                    try {
                        mqttAndroidClient.disconnect();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(MainActivity.this, "停止上传", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

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
            if (checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
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


    private void getTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); //yyyy年MM月dd日   HH:mm:ss.SSS
        Date mDate = new Date(System.currentTimeMillis());
        time = formatter.format(mDate);
    }


    /**
     * //     * MQTT建连选项类，输入设备三元组productKey, deviceName和deviceSecret, 生成Mqtt建连参数clientId，username和password.
     * //
     */
    class AiotMqttOption {
        private String username = "";
        private String password = "";
        private String clientId = "";

        public String getUsername() {
            return this.username;
        }

        public String getPassword() {
            return this.password;
        }

        public String getClientId() {
            return this.clientId;
        }

        /**
         * 获取Mqtt建连选项对象
         *
         * @param productKey   产品秘钥
         * @param deviceName   设备名称
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

    /**
     * 向默认的主题/user/update发布消息
     *
     * @param payload 消息载荷
     */
    public void publishMessage(String payload) {
        try {
            if (mqttAndroidClient.isConnected() == false) {
                mqttAndroidClient.connect();
            }

            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes());
            message.setQos(0);
            mqttAndroidClient.publish(TOPIC, message, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "publish succeed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "publish failed!");
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }


    /**
     * 订阅特定的主题
     *
     * @param topic mqtt主题
     */
    public void subscribeTopic(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "subscribed succeed");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "subscribed failed");
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}