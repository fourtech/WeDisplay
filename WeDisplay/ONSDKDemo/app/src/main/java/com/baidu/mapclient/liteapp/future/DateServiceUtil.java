package com.baidu.mapclient.liteapp.future;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateServiceUtil {
    public static final int SAME = 0;
    public static final int LEFT_BIG = 1;
    public static final int RIGHT_BIG = -1;
    public static final int INVALID = -2;

    public static String formatDate(Date date) {
        if (date == null) {
            return "empty";
        }
        try {
            SimpleDateFormat minuteF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return minuteF.format(date);
        } catch (Exception e) {
            return String.valueOf(date.getTime());
        }
    }

    public static int getDateDeltaV4(Date startDate, Date targetDate) {
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(startDate);

        Calendar toCalendar = Calendar.getInstance();
        toCalendar.setTime(targetDate);
        // 先获取年份
        int targetYear = toCalendar.get(Calendar.YEAR);
        // 获取一年中的第几天
        int targetDay = toCalendar.get(Calendar.DAY_OF_YEAR);
        // 获取当前年份 和 一年中的第几天
        int currentYear = fromCalendar.get(Calendar.YEAR);
        int currentDay = fromCalendar.get(Calendar.DAY_OF_YEAR);
        // 如果目标日期是明年的
        if (targetYear - currentYear == 1) {
            int yearDay;
            if (currentYear % 400 == 0) {
                yearDay = 366; // 世纪闰年
            } else if (currentYear % 4 == 0 && currentYear % 100 != 0) {
                yearDay = 366; // 普通闰年
            } else {
                yearDay = 365; // 平年
            }
            int offset = yearDay - currentDay + targetDay;
            return offset;
        } else {
            // 同一年
            int offset = targetDay - currentDay;
            return offset;
        }
    }

    public static boolean isPassedTime(Date date) {
        return date.getTime() - System.currentTimeMillis() < 0;
    }

    public static int getMinutes(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MINUTE);
    }

    /**
     * @return 当前TimeingView的时间的分钟数是不是15分钟的整数倍（0、15、30、45）
     */
    public static boolean isMultipleBlock(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.MINUTE) % 15 == 0;
    }

    /**
     * @param dateA
     * @param dateB
     *
     * @return 0 same ; 1: dateA > dateB ; -1: dateA < dateB
     */
    public static int compare(Date dateA, Date dateB) {
        if (dateA != null && dateB != null) {
            int formatAWithMinute = (int) (dateA.getTime() / 1000 / 60);
            int formatBWithMinute = (int) (dateB.getTime() / 1000 / 60);
            if (formatAWithMinute < formatBWithMinute) {
                return RIGHT_BIG;
            }
            if (formatAWithMinute == formatBWithMinute) {
                return SAME;
            }
            if (formatAWithMinute > formatBWithMinute) {
                return LEFT_BIG;
            }
        }
        return INVALID;
    }
}
