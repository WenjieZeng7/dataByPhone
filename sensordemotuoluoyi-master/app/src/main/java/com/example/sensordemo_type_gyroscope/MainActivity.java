 package com.example.sensordemo_type_gyroscope;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;


public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivity";
    private static final String ACTION_USB_PERMISSION = "cn.wch.wchusbdriver.USB_PERMISSION";
    private static final String RECEIVE_DATA = "RECEIVE_DATA"; //接收数据
    private static final int MESSAGE_RECEIVE = 0;

    //netty
    private NettyClient nettyClient;
    private EditText host;
    private EditText port;

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
    private float accuracy;

    //合宙
    private TextView airRMC = null;
    private Button mBtnOpen; //打开串口
    private boolean mIsOpen; //串口是否打开
    private int baudRate = 115200; //波特率
    private byte dataBit = 8; //数据位
    private byte stopBit = 1; //停止位
    private byte parity = 0;  //奇偶校验，0：不校验。加了会乱码。
    private byte flowControl = 0;  //流控

    private TextView airvalid = null;
    private TextView airtime = null;
    private TextView airlatitude = null;
    private TextView airlongitude = null;
    private TextView airspeed = null;
    private TextView airbearing = null;

    private String airvalid_value;
    private String airtime_value;
    private String airlatitude_value;  //double
    private String airlongitude_value;  //double
    private String airspeed_value;  //float
    private String airbearing_value;  //float

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


//    //MQTT
//    private final String TAG = "AiotMqtt";
//    /* 设备三元组信息 */
//    final private String PRODUCTKEY = "gmll0B3WENe";
//    final private String DEVICENAME = "test";
//    final private String DEVICESECRET = "81690e1ee1a86b12ca73b42378329ddf";
//
//    /* 自动Topic, 用于上报消息 */
//    final private String GYR_TOPIC = "/" + PRODUCTKEY + "/" + DEVICENAME + "/user/update";  //陀螺仪
//    final private String ACC_TOPIC = "/" + PRODUCTKEY + "/" + DEVICENAME + "/user/update";  //加速度
//    final private String LOC_TOPIC = "/" + PRODUCTKEY + "/" + DEVICENAME + "/user/update";  //位置服务
//    /* 自动Topic, 用于接受消息 */
//    final private String SUB_TOPIC = "/" + PRODUCTKEY + "/" + DEVICENAME + "/user/get";
//
//    /* 阿里云Mqtt服务器域名 */
//    final String host = "tcp://" + PRODUCTKEY + ".iot-as-mqtt.cn-shanghai.aliyuncs.com:443";
//    //    final String host = "iot-06z00e2ppeme0ap.mqtt.iothub.aliyuncs.com";
//    private String clientId;
//    private String userName;
//    private String passWord;
//
//    MqttAndroidClient mqttAndroidClient;
//    private boolean mqttOk = false;



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyApplication.driver = new CH34xUARTDriver(
                (UsbManager) getSystemService(Context.USB_SERVICE), this,
                ACTION_USB_PERMISSION);

        getPersimmions();  //定位权限
        initView();


        //开启服务
//        Intent intent = new Intent(this,MyService.class);
//        this.startForegroundService(intent);

        //绑定服务
//        Intent bindIntent = new Intent(this,MyService.class);
//        ServiceConnection serviceConnection = new ServiceConnection() {
//            @Override
//            public void onServiceConnected(ComponentName name, IBinder service) {
//                //绑定成功后todo
//                MyService myService = ((MyService.MyBinder)service).getService();
//                Log.d("hello","bindService start");
//                new MyService.LocationCallback() {
//                    @Override
//                    public void onLocation(Location location) {
//                        Log.d("hello", String.valueOf(location.getLatitude()));
//                    }
//                };
//            }
//
//            @Override
//            public void onServiceDisconnected(ComponentName name) {
//                //取消绑定后todo
//            }
//        };
//        bindService(bindIntent,serviceConnection, Service.BIND_AUTO_CREATE);




        //MQTT
        /* 获取Mqtt建连信息clientId, username, password */
//        AiotMqttOption aiotMqttOption = new AiotMqttOption().getMqttOption(PRODUCTKEY, DEVICENAME, DEVICESECRET);
//        if (aiotMqttOption == null) {
//            Log.e(TAG, "device info error");
//        } else {
//            clientId = aiotMqttOption.getClientId();
//            userName = aiotMqttOption.getUsername();
//            passWord = aiotMqttOption.getPassword();
//        }
////
//        /* 创建MqttConnectOptions对象并配置username和password */
//        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
//        mqttConnectOptions.setUserName(userName);
//        mqttConnectOptions.setPassword(passWord.toCharArray());
//
//
//        /* 创建MqttAndroidClient对象, 并设置回调接口 */
//        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), host, clientId);
//        mqttAndroidClient.setCallback(new MqttCallback() {
//            @Override
//            public void connectionLost(Throwable cause) {
//                Log.i(TAG, "connection lost");
//            }
//
//            @Override
//            public void messageArrived(String topic, MqttMessage message) throws Exception {
//                Log.i(TAG, "topic: " + topic + ", msg: " + new String(message.getPayload()));
//            }
//
//            @Override
//            public void deliveryComplete(IMqttDeliveryToken token) {
//                Log.i(TAG, "msg delivered");
//            }
//        });
//

        /* Mqtt建连 */
//        try {
//            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    Log.i(TAG, "connect succeed");
//                    subscribeTopic(SUB_TOPIC);
//                    mqttOk = true;
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    Log.i(TAG, "connect failed");
//                }
//            });
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
        //MQTT


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
//        Button start = (Button) findViewById(R.id.addData_start);
//        Button end = (Button) findViewById(R.id.addData_end);
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
                                values.put("accuracy", accuracy);
                                values.put("airvalid",airvalid_value);
                                values.put("airtime",airtime_value);
                                values.put("airlatitude",airlatitude_value);
                                values.put("airlongitude",airlongitude_value);
                                values.put("airspeed",airspeed_value);
                                values.put("airbearing",airbearing_value);
                                db.insert("Sensor1", null, values);
                            }
                        }, 0, 10);
                    }
                }else{
                    Toast.makeText(MainActivity.this, "结束保存", Toast.LENGTH_SHORT).show();
                }
            }
        });

//        start.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(MainActivity.this, "开始保存", Toast.LENGTH_SHORT).show();
//                isRecord = true;
//                if (isRecord = true) {
//                    Timer timer = new Timer();
//                    timer.schedule(new TimerTask() {
//                        @Override
//                        public void run() {
//                            SQLiteDatabase db = dbHelper.getWritableDatabase();
//                            ContentValues values = new ContentValues();
//                            values.put("time", time);
//                            values.put("accelerateX", accelerateX_value);
//                            values.put("accelerateY", accelerateY_value);
//                            values.put("accelerateZ", accelerateZ_value);
//                            values.put("angleX", angleX_value);
//                            values.put("angleY", angleY_value);
//                            values.put("angleZ", angleZ_value);
//                            values.put("latitude", latitude);
//                            values.put("longitude", longitude);
//                            values.put("speed", speed);
//                            db.insert("Sensor1", null, values);
//                        }
//                    }, 0, 200);
//                }
//            }
//        });
//        end.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                isRecord = false;
//                Toast.makeText(MainActivity.this, "结束保存", Toast.LENGTH_SHORT).show();
//            }
//        });


//        clear.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SQLiteDatabase db = dbHelper.getWritableDatabase();
//                db.delete("Sensor1", "id > ?", new String[]{"0"});
//                Toast.makeText(MainActivity.this, "清除完成", Toast.LENGTH_SHORT).show();
//            }
//        });
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
     * 初始化界面
     */
    private void initView(){
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

        host = (EditText) findViewById(R.id.host);
        port = (EditText) findViewById(R.id.port);


        airRMC = (TextView) findViewById(R.id.airRMC);
        airvalid = (TextView) findViewById(R.id.airvalid);
        airtime = (TextView) findViewById(R.id.airtime);
        airlatitude = (TextView) findViewById(R.id.airlatitude);
        airlongitude = (TextView) findViewById(R.id.airlongitude);
        airspeed = (TextView) findViewById(R.id.airspeed);
        airbearing = (TextView) findViewById(R.id.airbearing);
        airtime = (TextView) findViewById(R.id.airtime);
        mBtnOpen = findViewById(R.id.btn_open);
        mBtnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mIsOpen) {
                    // 执行打开串口操作
                    openUsbDevice();
                } else {
                    // 执行关闭串口操作
                    closeUsbDevice();
                }
            }
        });
        refreshUIAndDeviceOpenState(mIsOpen); //更改“打开串口”的显示值


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
//                getTime();
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
                tvProvider.setText("方式：" + location.getProvider());
//                time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(location.getTime()));
                latitude = location.getLatitude();  //纬度
                longitude = location.getLongitude();    //经度
                speed = location.getSpeed();
                accuracy = location.getAccuracy();
                tvTime.setText("时间：" + time);
                tvLatitude.setText("纬度：" + latitude + " °");
                tvLongitude.setText("经度：" + longitude + " °");
                tvAltitude.setText("海拔：" + location.getAltitude() + " m");
                tvBearing.setText("方向：" + location.getBearing() + " °");
                tvSpeed.setText("速度：" + speed + " m/s");
                tvAccuracy.setText("精度：" + accuracy + " m\n");
//                if(mqttOk){
//                    publishMessageLOC("time:"+ date +"\n"+ "latitude:"+ latitude +"\n"+"longitude:"+ longitude +"\n"+"speed:"+ speed +"\n");
//                }
            }
        };

        sensorManager.registerListener(gyroscopeEventListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(accEventListener, accSensor, SensorManager.SENSOR_DELAY_GAME); //GAME的类型，其频率是适中的
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);



    }


    /**
     * 执行关闭串口操作
     */
    private void closeUsbDevice() {

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        MyApplication.driver.CloseDevice();
        // 更新按钮的状态
        refreshUIAndDeviceOpenState(false);
        Toast.makeText(this, "串口已关闭", Toast.LENGTH_SHORT).show();
    }


    /**
     * 执行打开串口操作
     */
    private void openUsbDevice() {
        int retval = MyApplication.driver.ResumeUsbPermission();
        Log.d(TAG, "ResumeUsbPermission retval is " + retval);
        if (retval == 0) {
            retval = MyApplication.driver.ResumeUsbList();
            if (retval == 0) {
                // 打开串口成功
                if (MyApplication.driver.mDeviceConnection != null) {
                    if (!MyApplication.driver.UartInit()) {
                        // 初始化失败
                        Toast.makeText(MainActivity.this, "Initialization failed!",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(this, "串口已打开", Toast.LENGTH_SHORT).show();
                    // 进行一次默认配置操作
                    MyApplication.driver.SetConfig(baudRate, stopBit, dataBit, parity, flowControl);
                    // 更新UI和是否打开串口的标志
                    refreshUIAndDeviceOpenState(true);
                    //发送$PGKC242,0,1,0,0,0,0*2A\r\n。设置只回传RMC语句
                    setRMC();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // 开启读线程读取串口接收到的数据
                    new ReadThread().start();
                } else {
                    Toast.makeText(this, "打开串口失败", Toast.LENGTH_SHORT).show();
                }
            } else if (retval == -1) {
                // 打开串口失败
                Toast.makeText(this, "打开串口失败", Toast.LENGTH_SHORT).show();
                MyApplication.driver.CloseDevice();
            } else {
                //授权
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setIcon(R.mipmap.ic_launcher_round);
                builder.setTitle("未授权限");
                builder.setMessage("确认退出吗？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                });
                builder.setNegativeButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                    }
                });
                builder.show();
            }
        } else {
            Toast.makeText(this, "未发现有效串口设备", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 发送$PGKC242,0,1,0,0,0,0*2A\r\n。设置只回传RMC语句
     */
    private void setRMC() {
        sendWriteData("$PGKC242,0,1,0,0,0,0*2A\r\n");
    }


    /**
     * 执行发送数据操作
     *
     * @param data
     */
    private void sendWriteData(String data) {
        byte[] bytes = null;
        bytes = toByteArray(data);

        // 写数据，第一个参数为需要发送的字节数组，第二个参数为需要发送的字节长度
        // 返回实际发送的字节长度
        int length = MyApplication.driver.WriteData(bytes, bytes.length);
        if (length < 0) {
            Toast.makeText(this, "发送数据失败", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 将 String 类型数据转化为 byte[] 数组
     *
     * @param arg
     * @return
     */
    private byte[] toByteArray(String arg) {

        if (arg != null) {
            /* 先去除String中的' '，然后将String转换为char数组 */
            char[] NewArray = new char[1000];
            char[] array = arg.toCharArray();
            int length = 0;
            for (int i = 0; i < array.length; i++) {
                if (array[i] != ' ') {
                    NewArray[length] = array[i];
                    length++;
                }
            }

            byte[] byteArray = new byte[length];
            for (int i = 0; i < length; i++) {
                byteArray[i] = (byte) NewArray[i];
            }
            return byteArray;

        }
        return new byte[]{};
    }

    /**
     * 根据是否已经打开串口刷新部分按钮的状态
     *
     * @param isOpen 是否已经打开串口
     */
    private void refreshUIAndDeviceOpenState(boolean isOpen) {
        mIsOpen = isOpen;
        if (isOpen) {
            mBtnOpen.setText("关闭串口");
        } else {
            mBtnOpen.setText("打开串口");
        }
    }


    /**
     * //     * MQTT建连选项类，输入设备三元组productKey, deviceName和deviceSecret, 生成Mqtt建连参数clientId，username和password.
     * //
     */
//    class AiotMqttOption {
//        private String username = "";
//        private String password = "";
//        private String clientId = "";
//
//        public String getUsername() {
//            return this.username;
//        }
//
//        public String getPassword() {
//            return this.password;
//        }
//
//        public String getClientId() {
//            return this.clientId;
//        }
//
//        /**
//         * 获取Mqtt建连选项对象
//         *
//         * @param productKey   产品秘钥
//         * @param deviceName   设备名称
//         * @param deviceSecret 设备机密
//         * @return AiotMqttOption对象或者NULL
//         */
//        public AiotMqttOption getMqttOption(String productKey, String deviceName, String deviceSecret) {
//            if (productKey == null || deviceName == null || deviceSecret == null) {
//                return null;
//            }
//
//            try {
//                String timestamp = Long.toString(System.currentTimeMillis());
//
//                // clientId
//                this.clientId = productKey + "." + deviceName + "|timestamp=" + timestamp +
//                        ",_v=paho-android-1.0.0,securemode=2,signmethod=hmacsha256|";
//
//                // userName
//                this.username = deviceName + "&" + productKey;
//
//                // password
//                String macSrc = "clientId" + productKey + "." + deviceName + "deviceName" +
//                        deviceName + "productKey" + productKey + "timestamp" + timestamp;
//                String algorithm = "HmacSHA256";
//                Mac mac = Mac.getInstance(algorithm);
//                SecretKeySpec secretKeySpec = new SecretKeySpec(deviceSecret.getBytes(), algorithm);
//                mac.init(secretKeySpec);
//                byte[] macRes = mac.doFinal(macSrc.getBytes());
//                password = String.format("%064x", new BigInteger(1, macRes));
//            } catch (Exception e) {
//                e.printStackTrace();
//                return null;
//            }
//            return this;
//        }
//    }

    /**
     * 加速度
     * 向默认的主题/user/update发布消息
     *
     * @param payload 消息载荷
     */
//    public void publishMessageACC(String payload) {
//        try {
//            if (mqttAndroidClient.isConnected() == false) {
//                mqttAndroidClient.connect();
//            }
//
//            MqttMessage message = new MqttMessage();
//            message.setPayload(payload.getBytes());
//            message.setQos(0);
//            mqttAndroidClient.publish(ACC_TOPIC, message, null, new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    Log.i(TAG, "publish succeed!");
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    Log.i(TAG, "publish failed!");
//                }
//            });
//        } catch (MqttException e) {
//            Log.e(TAG, e.toString());
//            e.printStackTrace();
//        }
//    }

    /**
     * 陀螺仪
     * 向默认的主题/user/update发布消息
     *
     * @param payload 消息载荷
     */
//    public void publishMessageGYR(String payload) {
//        try {
//            if (mqttAndroidClient.isConnected() == false) {
//                mqttAndroidClient.connect();
//            }
//
//            MqttMessage message = new MqttMessage();
//            message.setPayload(payload.getBytes());
//            message.setQos(0);
//            mqttAndroidClient.publish(GYR_TOPIC, message, null, new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    Log.i(TAG, "publish succeed!");
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    Log.i(TAG, "publish failed!");
//                }
//            });
//        } catch (MqttException e) {
//            Log.e(TAG, e.toString());
//            e.printStackTrace();
//        }
//    }


    /**
     * 位置信息
     * 向默认的主题/user/update发布消息
     *
     * @param payload 消息载荷
     */
//    public void publishMessageLOC(String payload) {
//        try {
//            if (mqttAndroidClient.isConnected() == false) {
//                mqttAndroidClient.connect();
//            }
//
//            MqttMessage message = new MqttMessage();
//            message.setPayload(payload.getBytes());
//            message.setQos(0);
//            mqttAndroidClient.publish(LOC_TOPIC, message, null, new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    Log.i(TAG, "publish succeed!");
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    Log.i(TAG, "publish failed!");
//                }
//            });
//        } catch (MqttException e) {
//            Log.e(TAG, e.toString());
//            e.printStackTrace();
//        }
//    }


    /**
     * 订阅特定的主题
     *
     * @param topic mqtt主题
     */
//    public void subscribeTopic(String topic) {
//        try {
//            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    Log.i(TAG, "subscribed succeed");
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    Log.i(TAG, "subscribed failed");
//                }
//            });
//
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
//    }


    /**
     *
     */
    private void sendData() {
        Switch isRemoteSave = (Switch) findViewById(R.id.isRemoteSave);
        Switch isSend = (Switch) findViewById(R.id.connectbtn);
        Timer timer = new Timer();
        isRemoteSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(MainActivity.this, "开始上传", Toast.LENGTH_SHORT).show();
                    if(nettyClient != null){
//                        send("11111111");
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                StringBuilder sb = new StringBuilder();
                                getTime();
                                sb.append(time+",");
                                sb.append(new BigDecimal(Float.toString(accelerateX_value))+",");
                                sb.append(new BigDecimal(Float.toString(accelerateY_value))+",");
                                sb.append(new BigDecimal(Float.toString(accelerateZ_value))+",");
                                sb.append(new BigDecimal(Float.toString(angleX_value))+",");
                                sb.append(new BigDecimal(Float.toString(angleY_value))+",");
                                sb.append(new BigDecimal(Float.toString(angleZ_value))+",");
                                sb.append(new BigDecimal(Double.toString(latitude))+",");
                                sb.append(new BigDecimal(Double.toString(longitude))+",");
                                sb.append(new BigDecimal(Float.toString(speed)) + ",");
                                sb.append(new BigDecimal(Float.toString(accuracy)));
                                send(sb.toString());
                            }
                        }, 0, 200);
                    }
                }else{
                    timer.cancel();
                    Toast.makeText(MainActivity.this, "停止上传", Toast.LENGTH_SHORT).show();
                }
            }
        });
        isSend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(MainActivity.this, "开始连接", Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            connect();
                        }
                    }).start();
                }else{
                    stop();
                    Toast.makeText(MainActivity.this, "断开连接", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 创建ChatClient对象，用于连接服务器
     */
    public void connect(){
        try {
            // 创建一个ChatClient实例
            nettyClient = new NettyClient(host.getText().toString(),Integer.parseInt(port.getText().toString()));
            // 开始尝试连接服务器
            nettyClient.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 断开与netty服务器的连接
     */
    public void stop() {
        nettyClient.stop();
    }


    /**
     * 发送消息
     * @param
     */
    public void send(String str){
        Log.e(TAG, "send: "+ str );
        nettyClient.sendMsg(str);
    }

    /**
     * 以下保证转为字符串时，不采用科学计数法
     * @param f
     * @return
     */
    public String big(float f){
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        return nf.format(f);
    }
    public String big2(double d){
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        return nf.format(d);
    }



    /**
     * 读取接收数据的线程
     */
    private class ReadThread extends Thread {
        @Override
        public void run() {
            byte[] buffer = new byte[512];
            StringBuilder sb = new StringBuilder();
            while (true) {
//                try {
//                    Thread.sleep(10);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                if(!mIsOpen){
                    break;
                }
                // 读取数据,返回实际读取的字节数
                int length = MyApplication.driver.ReadData(buffer, buffer.length);
                if (length > 0) {
                    String recv = new String(buffer,0,length);
//                    Message msg = Message.obtain();
                    sb.append(recv);
                    if(sb.length() >= 75){
                        Message msg = Message.obtain();
                        msg.obj = sb.toString();
                        mHandler.sendMessage(msg);  //把数据返回给主线程处理
                        sb.delete(0,sb.length());
                    }
//                    msg.obj = recv;
//                    mHandler.sendMessage(msg);
                }
            }
        }
    }

//    //显示数据
//    private Handler mHandler = new Handler(new Handler.Callback() {
//        @Override
//        public boolean handleMessage(Message msg) {
//            String out = (String) msg.obj;
//            airRMC.setText(out);
//            return false;
//        }
//    });

    //显示数据
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            String out = (String) msg.obj;
            //$GNRMC,083647.000,A,3015.82926,N,12006.91929,E,0.000,243.76,060522,,,A,V*3C
            String[] split = out.split(",");
            airvalid.setText("定位状态：" + split[2]);
            airlatitude.setText("纬度：" + split[3]);
            airlongitude.setText("经度：" + split[5]);
            airbearing.setText("航向：" + split[8]);
            airspeed.setText("速度：" + split[7]);
            airtime.setText("时间：" + split[9] + " " + split[1]);
            airRMC.setText(out);

            airvalid_value = split[2];
            airlatitude_value = split[3];
            airlongitude_value = split[5];
            airspeed_value = split[7];
            airbearing_value = split[8];
            airtime_value = split[9] + " " + split[1];
        }
    };

}