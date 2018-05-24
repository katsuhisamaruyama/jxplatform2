/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.Calendar;

/**
 * Manages time information.
 * @author Katsuhisa Maruyama
 */
public class TimeInfo {
    
    public static ZonedDateTime getCurrentTime() {
        return ZonedDateTime.now();
    }
    
    public static long getCurrentTimeAsLong() {
        return getTimeAsLong(ZonedDateTime.now());
    }
    
    public static long getTimeAsLong(ZonedDateTime time) {
        return time.toInstant().toEpochMilli();
    }
    
    public static String getTimeAsISOString(ZonedDateTime time) {
        return time.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }
    
    public static String getFormatedTime(ZonedDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
        return time.format(formatter);
    }
    
    public static String getFormatedDate(ZonedDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        return time.format(formatter);
    }
    
    public static ZonedDateTime getTime(String str) {
        return ZonedDateTime.parse(str, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }
    
    public static ZonedDateTime getTime(long utime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(utime);
        return ZonedDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
    }
}
