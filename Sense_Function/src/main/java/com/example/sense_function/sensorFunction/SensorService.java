package com.example.sense_function.sensorFunction;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


import com.example.sense_function.DateHelper;
import com.example.sense_function.StringStore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/*
 * Edit by Zeron
 * Time: 2020.2.26
 *  感知Service，承载获取感知数据功能主体的Class
 *  功能1：记录任务要求的传感器类型，并为其绑定监听器
 *  功能2：获取已被监听的数据，并使用SQLite数据库完成数据存储
 *  功能3：根据任务已接受/完成状态，注册开启/关闭感知任务要求使用的传感器
 */

public class SensorService extends Service implements SensorEventListener {
    private static final String TAG = "SensorService";
    private String className = "SensorService";
    private SenseHelper mSenseHelper;
    private SensorManager mSensorManager;
    private HashMap<String, Integer> mSensorWorkMap;
    private HashMap<Integer, ContentValues> mSensorWorkDataMap;
    private HashMap<Integer, Boolean> mSensorDataChangeYNMap;                               //记录数据是否已被写入，是否是新数据或者旧数据，筛选使用
    private SQLiteDatabase mSensorWritableDB;
    private ContentValues contentValues;
    private Timer timer;

    public SensorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        bindInit();
        // TODO: Return the communication channel to the service.
        return new SensorService_Binder();
    }

    private void bindInit() {
        Log.i("SensorService", "SensorService is on! " + mSenseHelper.getSensorList_TypeInt_String());
        //Log.i("SensorService", "typesStrings_raw: " + mSenseHelper.getSensorList_TypeInt_String());
        String[] typesStrings = mSenseHelper.getSensorList_TypeInt_String().split(StringStore.Divider_1);
        //Log.i("SensorService", "typesStrings: " + typesStrings.);
        int[] types = new int[typesStrings.length];
        for (int i = 0; i < typesStrings.length; i++) {
            types[i] = Integer.parseInt(typesStrings[i]);
            Log.i("SensorService", "type_" + i + ":  " + types[i]);
        }
        //SenseTaskAccept_SensorOn(types);
        //创建SQLite写入的定时器
        initTimer();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSenseHelper = new SenseHelper(this);                                              //传感器感知辅助类
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorWorkMap = new HashMap<>();                                                           //使用hashMap记录传感器被要求使用的状态，每一个感知任务要求使用的传感器，都会为对应的传感器Key的值+1，反之，任务完成则减1，如果值<=0，则解绑对应传感器的监听器，释放资源
        mSensorWritableDB = new SensorSQLiteOpenHelper(this).getWritableDatabase();   //SQLite数据库的初始化，使用创建的数据库辅助类构建
        contentValues = new ContentValues();                                                        //数据存储用到的格式类
        mSensorWorkDataMap = new HashMap<>();
        mSensorDataChangeYNMap = new HashMap<>();


    }

    //TODO：考虑如果应用退出，再次进入后，如何根据已接受任务初始化mSensorWorkMap——使用SP？。另外，使用SP后提供全部暂停感知的方法，将已有Map存储到SP之中提供功能恢复，同时考虑DB的关闭和重新获取

    /*
     * 开启Service
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       String[] types_String = intent.getStringExtra("task_sensor_need").split(StringStore.Divider_1);
       int[] types_Int = new int[types_String.length];
       if(types_String.length > 0){
           for(int i = 0; i < types_String.length; i++) types_Int[i] = Integer.parseInt(types_String[i]);
           SenseTaskAccept_SensorOn(types_Int);
       }
        return super.onStartCommand(intent, flags, startId);
    }

    private void initTimer() {
        Log.i("SensorService", "SensorService Timer starts");
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                //work布尔量记录是否还有传感器要求工作
                boolean work = false;
                for (int i : mSensorWorkMap.values()) {
                    if (i >= 1) work = true;
                }
                Log.i(TAG, "boolean work :" + work);
                //如果还需工作，则便利hashmap拿到type和contentValues通过SQLite写入
                if (work) {
                    Iterator iterator = mSensorWorkDataMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<Integer, ContentValues> entry = (Map.Entry<Integer, ContentValues>) iterator.next();
                        Log.i(TAG, "Now SensorData Store, type:" + entry.getKey());
                        ContentValues temp = entry.getValue();
                        //判定数据变化map中为true才进行下一步，如果成功插入数据就将datamap中数据变化map中的value置为false，避免下次迭代重复记录数据，插入失败返回的是-1
                        if (mSensorDataChangeYNMap.get(entry.getKey())) {
                            if (mSensorWritableDB.insert(StringStore.SensorDataTable_Name, null, temp) > 0) {
                                mSensorDataChangeYNMap.put(entry.getKey(), false);
                            } else {
                                Log.i(TAG, "SQLite DB insert Error! Sensor:" + entry.getKey() + " data didn't record!");
                            }
                        }
                    }
                    Log.i(TAG, "Now SensorData Store displaying over.");
                }
            }
        };
        timer.schedule(task, 1, 5000);
        Log.i(TAG, "Timer Task now starts");

    }

    //关闭服务时关闭书库据/解绑传感器监听器
    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        mSensorWritableDB.close();
        mSensorManager.unregisterListener(this);
        Log.i("SensorService", "SensorService si off! " + mSenseHelper.getSensorList_TypeInt_String());
    }

    //Service运行检测
    private boolean isRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningServiceInfo
                = (ArrayList<ActivityManager.RunningServiceInfo>) activityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo info : runningServiceInfo) {
            if (info.service.getClassName().equals(className)) return true;
        }
        return false;
    }

    //根据传感器Type的Int开启感知
    private void SenseTaskAccept_SensorOn(int[] types) {
        Log.i(TAG, "SenseTaskAccept_SensorOn is called.");
        if (sensorCheck(types)) {
            for (int type : types) {
                //如果传感器已开启列表中不包含该传感器，则将之开启,并创建对应的MapNode
                if (!mSensorWorkMap.containsKey(type + "")) {
                    mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(type), 1000 * 1000);
                    mSensorWorkMap.put(type + "", 1);
                    Log.i(TAG, "Sensor turn on,type : " + type);
                }
                //如果传感器已开启，则将其参与任务的数值+1并重新写入
                else {
                    int tasksCount = mSensorWorkMap.get(type + "") + 1;
                    mSensorWorkMap.remove(type + "");
                    mSensorWorkMap.put(type + "", tasksCount);
                    Log.i(TAG, "Sensor is at work,type : " + type + "now task count : " + tasksCount);
                }
            }
        } else {
            Log.i(TAG, "There are some sensors that the device don't have!");
        }
    }

    //根据传感器Type的Int开启感知，自定义采样周期，单位微秒
    //无效
    /** @deprecated */
    private void SenseTaskAccept_SensorOn(int[] types, int period) {
        if (sensorCheck(types)) {
            for (int type : types) {
                //如果传感器已开启列表中不包含该传感器，则将之开启,并创建对应的MapNode
                if (!mSensorWorkMap.containsKey(type + "")) {
                    mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(type), period);
                    mSensorWorkMap.put(type + "", 1);
                }
                //如果传感器已开启，则将其参与任务的数值+1并重新写入
                else {
                    int tasksMount = mSensorWorkMap.get(type + "") + 1;
                    mSensorWorkMap.remove(type + "");
                    mSensorWorkMap.put(type + "", tasksMount);
                }
            }
        }
    }

    //根据传感器Type的Int减少tasksMount值，如果值小于0则关闭感知
    private void SenseTaskFinish_SensorOff(int[] types) {
        if (sensorCheck(types)) {
            for (int type : types) {
                //如果传感器已开启列表中包含该传感器，则将任务数量制-1
                if (mSensorWorkMap.containsValue(type + "")) {
                    int tasksMount = mSensorWorkMap.get(type + "") - 1;
                    //如果任务数量任务数量=0,则解绑对应的传感器，并且将对应的键值对移除
                    if (tasksMount <= 0) {
                        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(type));
                        mSensorWorkMap.remove(type + "");
                    }
                    //否则，则将其参与任务的数值-1并重新写入
                    else {
                        mSensorWorkMap.remove(type + "");
                        mSensorWorkMap.put(type + "", tasksMount);
                    }
                }
            }
        }
    }

    //根据传感器Type的Int强制关闭感知，忽略tasksMount
    private void SenseTaskFinish_SensorOff_pl(int[] pTypes) {
        for (int type : pTypes) {
            mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(type));
            mSensorWorkMap.remove(type + "");
        }
    }

    /*
     * 多个传感器包含检测，只要有一个不包含则返回
     * 使用了SenseHelper辅助类提供的containSensors方法
     */
    private boolean sensorCheck(int[] types) {
        boolean[] contain = mSenseHelper.containSensors(types);
        boolean hasYN = true;
        for (boolean yn : contain) {
            if (yn == false) hasYN = false;
        }
        //TODO:弹出提示框提醒哪些传感器没有
        return hasYN;
    }

    /*
     * 单传感器包含检测
     */
    private boolean sensorCheck(int type) {
        return mSenseHelper.containSensor(type);
    }


    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }


    //针对已绑定的监听器的Sensor，其产生数据变动时便会触发此方法，使用SQLiteDatabase进行存储
    @Override
    public void onSensorChanged(SensorEvent event) {
        ContentValues temp_contentValues = new ContentValues();
        //使用contentValues构建键值对，以便db使用insert插入
        //存储当前时间，使用日期辅助类帮助转换当前时间为String
        temp_contentValues.put(StringStore.SensorDataTable_SenseType, event.sensor.getType());
        temp_contentValues.put(StringStore.SensorDataTable_SenseTime, DateHelper.getSimpleDateFormat().format(new Date(System.currentTimeMillis())));
        temp_contentValues.put(StringStore.SensorDataTable_SenseData_1, event.values[0]);
        if (event.values.length > 1) {
            temp_contentValues.put(StringStore.SensorDataTable_SenseData_2, event.values[1]);
            if (event.values.length > 2) {
                temp_contentValues.put(StringStore.SensorDataTable_SenseData_3, event.values[2]);
            }
        }
        //将记录临时存储到HashMap之中
        mSensorWorkDataMap.put(event.sensor.getType(), temp_contentValues);
        mSensorDataChangeYNMap.put(event.sensor.getType(), true);
        //temp_contentValues.clear(); //测试使用
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    class SensorService_Binder extends Binder implements SensorService_Interface {
        @Override
        public void binder_sensorOn(int[] types) {
            //接口方法的调用，使得activity和此Service能够通信
            Log.i("SensorService", "Remote the Sensor Service's method 'SenseTaskAccept_SensorOn' through SensorService_Interface;s method 'binder_sensorOn' with SensorService_Binder");
            SenseTaskAccept_SensorOn(types);
            //Log.i(TAG,"Now these sensors are called:" + types.toString());
        }

        @Override
        public void binder_sensorOff(int[] types) {
            //接口方法的调用,关闭传感器感知
            Log.i("SensorService", "Remote the Sensor Service's method 'SenseTaskAccept_SensorOn' through SensorService_Interface;s method 'binder_sensorOn' with SensorService_Binder");
            SenseTaskFinish_SensorOff_pl(types);
        }


    }


}
