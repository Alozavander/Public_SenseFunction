package com.example.sense_function.sensorFunction;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.sense_function.StringStore;
import com.example.sense_function.saveFile.FileExport;

import java.io.File;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.widget.Toast.LENGTH_LONG;

/*
 * Edit by Liao JiaHao
 * Time: 2020.7.9
 * Description : SenseFunction整个功能模块的接口集成类
 * 功能：
 * 1. 开启/关闭SenseService
 * 2. 开启/关闭对应传感器感知
 * 3. 查看感知数据
 * 4. 删除感知数据
 * 5. 保存感知数据——>csv文件(自定义路径)
 *
 */
public class SenseFunction {
    private static final String TAG = "SenseFunction";
    private Context mContext;
    private SensorService_Interface sensorService_interface;
    private boolean isBind;
    private ServiceConnection conn;
    public SenseHelper sh;
    public SQLiteDatabase mReadableDatabase;

    /* 绑定/启动Service服务的Activity的Context */
    public SenseFunction(Context pContext) {
        mContext = pContext;
    }

    /* 开启SenseService
     */
    public void On_SenseService() {
        Log.i(TAG, "=======Now Init the sensor Service...===========");
        //初始化传感器感知服务Service
        sh = new SenseHelper(mContext);
        Intent intent = new Intent(mContext, SensorService.class);
        if (conn == null) {
            Log.i(TAG, "===========connection creating...============");
            conn = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {

                    sensorService_interface = (SensorService_Interface) service;
                    Log.i(TAG, "sensorService_interface connection is done.");
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.i(TAG, "sensorService disconnected.");
                }
            };
        } else {
            Log.i(TAG, "===============sensorService connection exits.================");
        }
        isBind = mContext.getApplicationContext().bindService(intent, conn, BIND_AUTO_CREATE);
        Log.i(TAG, "=============SensorService has been bound :" + isBind + "==============");
    }

    /* 关闭SenseService
     * 请在开启service的代码结构中的onDestroy方法加入此方法
     */
    public void Off_SenseService() {
        if (isBind) {
            isBind = false;
            mContext.unbindService(conn);
        }
    }

    /* 开启传感器感知
     * 由sensorType_array指定开启的传感器类型
     */
    public void on_sensor(int[] sensorType_array) {
        if (isBind) {
            if (sensorService_interface != null) {
                sensorService_interface.binder_sensorOn(sensorType_array);
                Log.i(TAG, "SensorService's sensorOn has been remote.");
            } else {
                Toast.makeText(mContext, "sensorService_interface is null. Please init SenseService use On_SenseService method.", LENGTH_LONG).show();
                Log.i(TAG, "sensorService_interface is null. Please init SenseService use On_SenseService method.");
            }
        } else {
            Toast.makeText(mContext, "SensorService has been bound :" + isBind + ". Please  init SenseService use On_SenseService method.", LENGTH_LONG).show();
            Log.i(TAG, "SensorService has been bound :" + isBind + ". Please  init SenseService use On_SenseService method.");
        }
    }

    /* 关闭传感器感知
     * 由sensorType_array指定关闭的传感器类型
     */
    public void off_sensor(int[] sensorType_array) {
        if (isBind) {
            if (sensorService_interface != null) {
                sensorService_interface.binder_sensorOff(sensorType_array);
                Log.i(TAG, "SensorService's sensorOff has been remote.");
            } else {
                Toast.makeText(mContext, "sensorService_interface is null. Please init SenseService use On_SenseService method.", LENGTH_LONG).show();
                Log.i(TAG, "sensorService_interface is null. Please init SenseService use On_SenseService method.");
            }
        } else {
            Toast.makeText(mContext, "SensorService has been bound :" + isBind + ". Please  init SenseService use On_SenseService method.", LENGTH_LONG).show();
            Log.i(TAG, "SensorService has been bound :" + isBind + ". Please  init SenseService use On_SenseService method.");
        }
    }

    /* 查询传感器数据
     * 返回对应的cursor
     * cursor使用完成后应当使用close讲它关闭
     */
    public Cursor query_senseData(int pSensorType) {
        SQLiteDatabase db = new SensorSQLiteOpenHelper(mContext).getReadableDatabase();
        Cursor c = db.query(StringStore.SensorDataTable_Name,
                new String[]{StringStore.SensorDataTable_id,
                        StringStore.SensorDataTable_SenseType,
                        StringStore.SensorDataTable_SenseTime,
                        StringStore.SensorDataTable_SenseData_1,
                        StringStore.SensorDataTable_SenseData_2,
                        StringStore.SensorDataTable_SenseData_3},
                StringStore.SensorDataTable_SenseType + "=?", new String[]{pSensorType + ""}, null, null, null);
        return c;
    }

    /* 删除传感器数据
     * 返回删除的数据数量，-1为出错，如果startTime和endTime为null，则该传感器类型的感知数据全部删除
     */
    public int SQLiteDelete(int sensorType, String startTime, String endTime) {
        SQLiteDatabase db = new SensorSQLiteOpenHelper(mContext).getReadableDatabase();
        int lI = -1;
        //四种时间不同的情况
        if (startTime == null && endTime != null) {
            String whereClaus = StringStore.SensorDataTable_SenseType + "=?" + " AND " + StringStore.SensorDataTable_SenseTime + " < ?";
            lI = db.delete(StringStore.SensorDataTable_Name,
                    whereClaus, new String[]{sensorType + "", endTime});
            Log.e(TAG, "Where Claus is : " + whereClaus);
            Log.e(TAG, "EndTime is : " + endTime);
            Log.e(TAG, "Delete result : " + lI);
        } else if (startTime != null && endTime == null) {
            String whereClaus = StringStore.SensorDataTable_SenseType + "=?" + " AND " + StringStore.SensorDataTable_SenseTime + " > ?";
            lI = db.delete(StringStore.SensorDataTable_Name,
                    whereClaus, new String[]{sensorType + "", startTime});
            Log.e(TAG, "Where Claus is : " + whereClaus);
            Log.e(TAG, "StartTime is : " + startTime);
            Log.e(TAG, "Delete result : " + lI);
        } else if (startTime == null && endTime == null) {
            String whereClaus = StringStore.SensorDataTable_SenseType + "=?";
            lI = db.delete(StringStore.SensorDataTable_Name,
                    whereClaus, new String[]{sensorType + ""});
            Log.e(TAG, "Where Claus is : " + whereClaus);
            Log.e(TAG, "Delete result : " + lI);
        } else {
            String whereClaus = StringStore.SensorDataTable_SenseType + "=?" + " AND " + StringStore.SensorDataTable_SenseTime + " > ? AND " + StringStore.SensorDataTable_SenseTime + " < ?";
            lI = db.delete(StringStore.SensorDataTable_Name,
                    whereClaus, new String[]{sensorType + "", startTime, endTime});
            Log.e(TAG, "Where Claus is : " + whereClaus);
            Log.e(TAG, "StartTime is : " + startTime);
            Log.e(TAG, "EndTime is : " + endTime);
            Log.e(TAG, "Delete result : " + lI);
        }
        db.close();
        return lI;
    }

    /* 保存成文件
     * 返回保存的File类
     * 文件默认名为senseData.csv，默认路径为sd/SensorDataStore/senseData.csv
     * 自定义路径请检查父文件夹不存在的错误
     */
    public File storeDataToCSV(int sensorType, String fileName, String fileParentPath) {
        File saveFile;
        Cursor c = new SensorSQLiteOpenHelper(mContext).getReadableDatabase().query(StringStore.SensorDataTable_Name,
                new String[]{StringStore.SensorDataTable_SenseType,
                        StringStore.SensorDataTable_SenseTime,
                        StringStore.SensorDataTable_SenseData_1,
                        StringStore.SensorDataTable_SenseData_2,
                        StringStore.SensorDataTable_SenseData_3},
                null, null, null, null, null);
        saveFile = FileExport.ExportToCSV(c, fileName, fileParentPath);
        Toast.makeText(mContext, "Output finishing. The file path is :" + saveFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        return saveFile;
    }
}
