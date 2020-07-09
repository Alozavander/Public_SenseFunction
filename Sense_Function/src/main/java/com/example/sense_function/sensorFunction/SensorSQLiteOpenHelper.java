package com.example.sense_function.sensorFunction;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.sense_function.StringStore;



/*
 *  SQLite打开辅助类
 *  专门为Sensor存储感知数据设立
 *  功能：根据传入的SensorType(依照SenseHelper中对SensorType存储到SP的格式进行解析)分别为当前设备支持的各类传感器创建对应Table
 */
public class SensorSQLiteOpenHelper extends SQLiteOpenHelper {

    public final String senseTime = "senseTime";
    public final String snumber = "snumber";
    private String types;


    public SensorSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    public SensorSQLiteOpenHelper(Context context) {
        super(context, StringStore.SensorDatabase_Name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists "
                + StringStore.SensorDataTable_Name + "("
                + StringStore.SensorDataTable_id + " integer primary key autoincrement,"
                + StringStore.SensorDataTable_SenseType + " integer,"
                + StringStore.SensorDataTable_SenseTime + " text,"
                + StringStore.SensorDataTable_SenseData_1 + " text,"
                + StringStore.SensorDataTable_SenseData_2 + " text,"
                + StringStore.SensorDataTable_SenseData_3 + " text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
