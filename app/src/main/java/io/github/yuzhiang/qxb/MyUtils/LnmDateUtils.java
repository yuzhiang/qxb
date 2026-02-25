package io.github.yuzhiang.qxb.MyUtils;

import static io.github.yuzhiang.qxb.MyUtils.UsrMsgUtils.getWeekStartDay;

import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;

import java.util.Calendar;
import java.util.Date;

public class LnmDateUtils {

    private static final String COURSE_SP = "CourseMsg";

    private LnmDateUtils() {
    }

    public static int getNowDays() {
        return getNowDays(new Date());
    }

    public static int getNowDays(Date date) {
        double days = getNowMin(date) / (60.0 * 24.0);
        return (int) Math.ceil(days);
    }

    public static int getThisWeek(Date date) {
        double week = getNowMin(date) / (60.0 * 24.0 * 7);
        return (int) Math.ceil(week);
    }

    private static long getNowMin(Date date) {
        Date start = getCourseStartTime();
        long min = TimeUtils.getTimeSpan(date, start, TimeConstants.MIN);
        if (getWeekStartDay()) {
            min += 60L * 24L;
        }
        return min;
    }

    private static Date getCourseStartTime() {
        long saved = SPUtils.getInstance(COURSE_SP).getLong("courseStartTime", 0L);
        if (saved > 0) {
            return TimeUtils.millis2Date(saved);
        }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
