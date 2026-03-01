package io.github.yuzhiang.qxb.model.todo;

import java.util.concurrent.TimeUnit;

public class TodoTimeUtils {
    public static String formatCountdown(long dueAt) {
        long now = System.currentTimeMillis();
        long diff = dueAt - now;
        if (diff <= 0) {
            return "已到期";
        }
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        diff -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        diff -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        if (days > 0) {
            return "剩余 " + days + "天 " + hours + "小时 " + minutes + "分钟";
        }
        if (hours > 0) {
            return "剩余 " + hours + "小时 " + minutes + "分钟";
        }
        return "剩余 " + minutes + "分钟";
    }

    public static String formatImportantDays(long dueAt) {
        long now = System.currentTimeMillis();
        long diff = dueAt - now;
        long days = (long) Math.ceil(diff / (double) TimeUnit.DAYS.toMillis(1));
        if (days < 0) {
            days = 0;
        }
        return "D-" + days + "天";
    }
}
