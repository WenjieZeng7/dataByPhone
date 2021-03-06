package com.example.sensordemo_type_gyroscope;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    public static final String CREATE_BOOK = "create table Sensor1("
            + "id integer primary key autoincrement,"
            + "time text,"
            + "accelerateX real,"
            + "accelerateY real,"
            + "accelerateZ real,"
            + "angleX real,"
            + "angleY real,"
            + "angleZ real,"
            + "latitude real,"
            + "longitude real,"
            + "speed real,"
            + "accuracy real,"
            + "airvalid text,"
            + "airtime text,"
            + "airlatitude text,"
            + "airlongitude text,"
            + "airspeed text,"
            + "airbearing text)";
    private Context mContext;

    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BOOK);
        Toast.makeText(mContext, "Create succeeded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
