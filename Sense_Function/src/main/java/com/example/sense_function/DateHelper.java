package com.example.sense_function;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelper {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static SimpleDateFormat getSimpleDateFormat(){
        return simpleDateFormat;
    }

    /*
     * s must be yyyy-MM-dd HH:mm:ss
     */
    public static Date string2Date (String s) throws ParseException {
        return simpleDateFormat.parse(s);
    }

    /*
     * s must be yyyy-MM-dd HH:mm:ss
     */
    public static long string2TimeStamp(String s) throws ParseException {
        return simpleDateFormat.parse(s).getTime();
    }
}
