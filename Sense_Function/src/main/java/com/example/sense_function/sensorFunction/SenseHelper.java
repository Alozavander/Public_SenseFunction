package com.example.sense_function.sensorFunction;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

import com.example.sense_function.R;
import com.example.sense_function.StringStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/*
 * Edit by Liao JiaHao
 * Author LJH
 * Time: 2020.07.09
 *  感知功能组件
 */
public class SenseHelper {
    private final String TAG = "SenseHelper";
    private static Context mContext;
    private List<Integer> mSensorType_List;
    private String mDivider;

    public SenseHelper(Context context) {
        mContext = context;
        mSensorType_List = new ArrayList<Integer>();
        mDivider = StringStore.Divider_1;
        String tempString = getSensorList_TypeInt_String();
        Log.i(TAG, "Now SenseHelp Create, sensorTypeGet: " + tempString);
        // 如果是预设的错误默认值则重新初始化设备拥有的传感器int值表
        if (tempString.equals(StringStore.SP_StringError)) {
            Log.i(TAG, "Now SenseHelp Get The Sensor List of the Device");
            SensorManager sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
            List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
            Integer[] type_list = new Integer[sensorList.size()];
            for (int i = 0; i < sensorList.size(); i++) {
                Sensor sensor = sensorList.get(i);
                type_list[i] = sensor.getType();
                mSensorType_List.add(type_list[i]);
            }
            //写入信息表
            if (storeSensorTypeInfo(type_list))
                Log.i(TAG, "mSensorType_List has been wrote into the SQLite");
            else Log.i(TAG, "mSensorType_List has not been wrote into the SQLite");
        }
        //如果成功取出则进行处理，通过设定的划分符进行分割
        else {
            Log.i(TAG, "Sensor List Has Been Wrote");
            String[] typeInt = tempString.split(mDivider);
            for (String string : typeInt) {
                int i = Integer.parseInt(string);
                Log.i(TAG, "typeInt:" + string);
                mSensorType_List.add(i);
            }
        }
    }

    /*
     *  传感器列表获取函数（纯字符）
     *  如果未获取则返回StringStore.SP_StringError，需要在使用此方法后进行判定
     */
    public String getSensorList_Name() {
        return mContext.getSharedPreferences(StringStore.SensorSP_XMLName, Context.MODE_PRIVATE).
                getString(StringStore.SensorDataSP_List_String, StringStore.SP_StringError);
    }

    /*
     *  传感器Type的int值表获取函数
     *  如果未获取则返回StringStore.SP_StringError，需要在使用此方法后进行判定
     *  返回的是SP记录的传感器int组成的字符串
     */
    public String getSensorList_TypeInt_String() {
        //测试所用mContext.getSharedPreferences(StringStore.SensorSP_XMLName, Context.MODE_PRIVATE).edit().remove(StringStore.SensorDataSP_List_TypeInt).commit();
        return mContext.getSharedPreferences(StringStore.SensorSP_XMLName, Context.MODE_PRIVATE).
                getString(StringStore.SensorDataSP_List_TypeInt, StringStore.SP_StringError);
    }

    /*
     *  传感器Type的int值表获取函数
     *  返回的是传感器名字在String.xml文件中的前缀字符串数组，用于构建sensor的menu
     */
    public String[] getSensorList_TypeInt_Strings() {
        String tempString = getSensorList_TypeInt_String();
        String[] types = tempString.split(mDivider);
        if(types.length <= 0){
            return new String[]{"Null"};
        }else {
            String[] sensors = new String[types.length];
            for(int i = 0; i < sensors.length; i ++){
                sensors[i] = sensorType2XmlName(mContext, Integer.parseInt(types[i]));
                /*switch (Integer.parseInt(types[i])) {
                    case Sensor.TYPE_ACCELEROMETER:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_ACCELEROMETER);
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_MAGNETIC_FIELD);
                        break;
                    case Sensor.TYPE_ORIENTATION:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_ORIENTATION);
                        break;
                    case Sensor.TYPE_GYROSCOPE:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_GYROSCOPE);
                        break;
                    case Sensor.TYPE_LIGHT:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_LIGHT);
                        break;
                    case Sensor.TYPE_PRESSURE:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_PRESSURE);
                        break;
                    case Sensor.TYPE_TEMPERATURE:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_TEMPERATURE);
                        break;
                    case Sensor.TYPE_PROXIMITY:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_PROXIMITY);
                        break;
                    case Sensor.TYPE_GRAVITY:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_GRAVITY);
                        break;
                    case Sensor.TYPE_LINEAR_ACCELERATION:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_LINEAR_ACCELERATION);
                        break;
                    case Sensor.TYPE_ROTATION_VECTOR:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_ROTATION_VECTOR);
                        break;
                    case Sensor.TYPE_RELATIVE_HUMIDITY:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_RELATIVE_HUMIDITY);
                        break;
                    case Sensor.TYPE_AMBIENT_TEMPERATURE:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_AMBIENT_TEMPERATURE);
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_MAGNETIC_FIELD_UNCALIBRATED);
                        break;
                    case Sensor.TYPE_GAME_ROTATION_VECTOR:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_GAME_ROTATION_VECTOR);
                        break;
                    case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_GYROSCOPE_UNCALIBRATED);
                        break;
                    case Sensor.TYPE_SIGNIFICANT_MOTION:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_SIGNIFICANT_MOTION);
                        break;
                    case Sensor.TYPE_STEP_DETECTOR:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_STEP_DETECTOR);
                        break;
                    case Sensor.TYPE_STEP_COUNTER:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_STEP_COUNTER);
                        break;
                    case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_GEOMAGNETIC_ROTATION_VECTOR);
                        break;
                    case Sensor.TYPE_HEART_RATE:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_HEART_RATE);
                        break;
                    case Sensor.TYPE_HEART_BEAT:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_HEART_BEAT);
                        break;
                    case Sensor.TYPE_POSE_6DOF:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_POSE_6DOF);
                        break;
                    case Sensor.TYPE_STATIONARY_DETECT:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_STATIONARY_DETECT);
                        break;
                    case Sensor.TYPE_MOTION_DETECT:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_MOTION_DETECT);
                        break;
                    case Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_LOW_LATENCY_OFFBODY_DETECT);
                        break;
                    case Sensor.TYPE_ACCELEROMETER_UNCALIBRATED:
                        sensors[i] = mContext.getString(R.string.Sensor_TYPE_ACCELEROMETER_UNCALIBRATED);
                        break;
                    default:
                        //TODO:做未匹配的传感器记录
                        break;
                }*/
            }
            return sensors;
        }
    }


    /*
     *  传感器Type的int值表获取函数
     *  如果未获取则返回StringStore.SP_StringError，需要在使用此方法后进行判定
     *  返回的是SP记录的传感器int组成的字符串
     */
    public int[] getSensorList_TypeInt_Integers() {
        String temp = getSensorList_TypeInt_String();
        String[] temp_strings = temp.split(mDivider);
        int[] result;
        if (temp.equals(StringStore.SP_StringError)) {
            Toast.makeText(mContext, "Sensor List SP Error by SenseHelper", Toast.LENGTH_SHORT).show();
            result = new int[1];
            result[0] = -1;
        } else {
            result = new int[temp_strings.length];
            for (int i = 0; i < temp_strings.length; i++) {
                result[i] = Integer.parseInt(temp_strings[i]);
            }
        }
        return result;
    }

    /*
     *  查询手机是否含有该传感器，单个传感器查询
     */
    public boolean containSensor(int sensor_type) {
        if (mSensorType_List.size() <= 0) {
            Log.i(TAG, "mSensorType_List's size is :" + mSensorType_List.size());
            return false;
        } else {
            Log.i(TAG, "mSensorType_List:" + mSensorType_List.toString());
            if (mSensorType_List.contains(sensor_type)) return true;
            else {
                Log.i(TAG, "The device dont have sensor :" + sensor_type);
                return false;
            }
        }
    }

    /*
     *  查询手机是否含有该传感器，多个传感器查询
     *  返回boolean数组，对应下标是传入的传感器列表的小标，可通过下标进行判定/UI提示
     */
    public boolean[] containSensors(int[] sensor_type_list) {
        boolean[] results = new boolean[sensor_type_list.length];
        for (int i = 0; i < sensor_type_list.length; i++) {
            if (containSensor(sensor_type_list[i])) results[i] = true;
            else results[i] = false;
        }
        return results;
    }

    /*
     *  存储传感器Type数据，使用StringStore中统一规定的Divider字符分割符
     */
    public boolean storeSensorTypeInfo(Integer[] sensorTypes) {
        if (sensorTypes.length <= 0) return false;
        else {
            //做重复筛除
            Set<Integer> typeSet = new HashSet<>();
            for(int i : sensorTypes) typeSet.add(i);
            //List转换做排序
            LinkedList<Integer> lLinkedList = new LinkedList<>(typeSet);
            Collections.sort(lLinkedList);

            Iterator lIterator = lLinkedList.iterator();
            String record = lIterator.next() + "";
            while(lIterator.hasNext()){
                record = record + mDivider + lIterator.next() ;
            }
            return mContext.getSharedPreferences(StringStore.SensorSP_XMLName, Context.MODE_PRIVATE).edit()
                    .putString(StringStore.SensorDataSP_List_TypeInt, record)
                    .commit();
        }
    }

    /*
     * Todo: 创建配套多语言的string，创建Set对Type_Int加入字符集中的字符，使用switch，然后根据set返回String[]
     *
     *  根据设备具有的传感器类型，返回中文/英文...等语言的传感器名称列表，用以实现创建传感器多选提示框等。
     */
    public String[] sensorList_TypeInts2NameStrings(int[] typeList) {
        String[] lStrings = new String[typeList.length];
        for(int i = 0; i < typeList.length; i ++){
            lStrings[i] = sensorType2XmlName(mContext, typeList[i]);
        }
        return lStrings;
    }


    public int[] sensorList_NameStrings2TypeInts(String[] nameList) {
        int[] lInts = new int[nameList.length];
        for(int i = 0; i < nameList.length; i ++){
            lInts[i] = sensorXmlName2Type(mContext, nameList[i]);
        }
        return lInts;
    }

    /*
     *  传感器的参照表函数
     *  @param xmlName:String.xml字符文件存储的传感器名字
     *  @return int 返回对应传感器的Type的int值
     */
    public static int sensorXmlName2Type(Context pContext, String xmlName) {
        int sensorType = -2;                 //不为-1的原因是type中-1被使用表示为all
        if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_ACCELEROMETER))) sensorType = Sensor.TYPE_ACCELEROMETER;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_MAGNETIC_FIELD))) sensorType = Sensor.TYPE_MAGNETIC_FIELD;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_ORIENTATION))) sensorType = Sensor.TYPE_ORIENTATION;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_GYROSCOPE))) sensorType = Sensor.TYPE_GYROSCOPE;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_LIGHT))) sensorType = Sensor.TYPE_LIGHT;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_PRESSURE))) sensorType = Sensor.TYPE_PRESSURE;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_TEMPERATURE))) sensorType = Sensor.TYPE_TEMPERATURE;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_PROXIMITY))) sensorType = Sensor.TYPE_PROXIMITY;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_GRAVITY))) sensorType = Sensor.TYPE_GRAVITY;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_LINEAR_ACCELERATION))) sensorType = Sensor.TYPE_LINEAR_ACCELERATION;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_ROTATION_VECTOR))) sensorType = Sensor.TYPE_ROTATION_VECTOR;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_RELATIVE_HUMIDITY))) sensorType = Sensor.TYPE_RELATIVE_HUMIDITY;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_AMBIENT_TEMPERATURE))) sensorType = Sensor.TYPE_AMBIENT_TEMPERATURE;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_MAGNETIC_FIELD_UNCALIBRATED))) sensorType = Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_GAME_ROTATION_VECTOR))) sensorType = Sensor.TYPE_GAME_ROTATION_VECTOR;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_GYROSCOPE_UNCALIBRATED))) sensorType = Sensor.TYPE_GYROSCOPE_UNCALIBRATED;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_SIGNIFICANT_MOTION))) sensorType = Sensor.TYPE_SIGNIFICANT_MOTION;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_STEP_DETECTOR))) sensorType = Sensor.TYPE_STEP_DETECTOR;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_STEP_COUNTER))) sensorType = Sensor.TYPE_STEP_COUNTER;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_GEOMAGNETIC_ROTATION_VECTOR))) sensorType = Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_HEART_RATE))) sensorType = Sensor.TYPE_HEART_RATE;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_HEART_BEAT))) sensorType = Sensor.TYPE_HEART_BEAT;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_POSE_6DOF))) sensorType = Sensor.TYPE_POSE_6DOF;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_STATIONARY_DETECT))) sensorType = Sensor.TYPE_STATIONARY_DETECT;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_MOTION_DETECT))) sensorType = Sensor.TYPE_MOTION_DETECT;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_LOW_LATENCY_OFFBODY_DETECT))) sensorType = Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT;
        else if(xmlName.equals(pContext.getString(R.string.Sensor_TYPE_ACCELEROMETER_UNCALIBRATED))) sensorType = Sensor.TYPE_ACCELEROMETER_UNCALIBRATED;
        return sensorType;
    }


    /*
     *  传感器的参照表函数
     *  @param int 传感器Type的int值
     *  @return xmlName  返回String.xml字符文件存储的传感器名字
     */
    public static String sensorType2XmlName(Context pContext, Integer sensorType) {
        String result = StringStore.SP_StringError;
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                result = pContext.getString(R.string.Sensor_TYPE_ACCELEROMETER);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                result = pContext.getString(R.string.Sensor_TYPE_MAGNETIC_FIELD);
                break;
            case Sensor.TYPE_ORIENTATION:
                result = pContext.getString(R.string.Sensor_TYPE_ORIENTATION);
                break;
            case Sensor.TYPE_GYROSCOPE:
                result = pContext.getString(R.string.Sensor_TYPE_GYROSCOPE);
                break;
            case Sensor.TYPE_LIGHT:
                result = pContext.getString(R.string.Sensor_TYPE_LIGHT);
                break;
            case Sensor.TYPE_PRESSURE:
                result = pContext.getString(R.string.Sensor_TYPE_PRESSURE);
                break;
            case Sensor.TYPE_TEMPERATURE:
                result = pContext.getString(R.string.Sensor_TYPE_TEMPERATURE);
                break;
            case Sensor.TYPE_PROXIMITY:
                result = pContext.getString(R.string.Sensor_TYPE_PROXIMITY);
                break;
            case Sensor.TYPE_GRAVITY:
                result = pContext.getString(R.string.Sensor_TYPE_GRAVITY);
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                result = pContext.getString(R.string.Sensor_TYPE_LINEAR_ACCELERATION);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                result = pContext.getString(R.string.Sensor_TYPE_ROTATION_VECTOR);
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                result = pContext.getString(R.string.Sensor_TYPE_RELATIVE_HUMIDITY);
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                result = pContext.getString(R.string.Sensor_TYPE_AMBIENT_TEMPERATURE);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                result = pContext.getString(R.string.Sensor_TYPE_MAGNETIC_FIELD_UNCALIBRATED);
                break;
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                result = pContext.getString(R.string.Sensor_TYPE_GAME_ROTATION_VECTOR);
                break;
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                result = pContext.getString(R.string.Sensor_TYPE_GYROSCOPE_UNCALIBRATED);
                break;
            case Sensor.TYPE_SIGNIFICANT_MOTION:
                result = pContext.getString(R.string.Sensor_TYPE_SIGNIFICANT_MOTION);
                break;
            case Sensor.TYPE_STEP_DETECTOR:
                result = pContext.getString(R.string.Sensor_TYPE_STEP_DETECTOR);
                break;
            case Sensor.TYPE_STEP_COUNTER:
                result = pContext.getString(R.string.Sensor_TYPE_STEP_COUNTER);
                break;
            case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                result = pContext.getString(R.string.Sensor_TYPE_GEOMAGNETIC_ROTATION_VECTOR);
                break;
            case Sensor.TYPE_HEART_RATE:
                result = pContext.getString(R.string.Sensor_TYPE_HEART_RATE);
                break;
            case Sensor.TYPE_HEART_BEAT:
                result = pContext.getString(R.string.Sensor_TYPE_HEART_BEAT);
                break;
            case Sensor.TYPE_POSE_6DOF:
                result = pContext.getString(R.string.Sensor_TYPE_POSE_6DOF);
                break;
            case Sensor.TYPE_STATIONARY_DETECT:
                result = pContext.getString(R.string.Sensor_TYPE_STATIONARY_DETECT);
                break;
            case Sensor.TYPE_MOTION_DETECT:
                result = pContext.getString(R.string.Sensor_TYPE_MOTION_DETECT);
                break;
            case Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT:
                result = pContext.getString(R.string.Sensor_TYPE_LOW_LATENCY_OFFBODY_DETECT);
                break;
            case Sensor.TYPE_ACCELEROMETER_UNCALIBRATED:
                result = pContext.getString(R.string.Sensor_TYPE_ACCELEROMETER_UNCALIBRATED);
                break;
            default:
                //TODO:做未匹配的传感器记录
                break;
        }
        return result;
    }


    /*内部静态类，辅助实现单例模式
    private static class SenseHelperCreater {
        private static SenseHelper instance = new SenseHelper();
    }
    public static SenseHelper getInstance(Context context){
        mContext = context;
        return SenseHelperCreater.instance;
    }
    */
}
