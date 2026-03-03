package io.github.yuzhiang.qxb.model;

import java.util.Calendar;
import java.util.List;

import io.github.yuzhiang.qxb.db.room.bean.Lnm;
import io.github.yuzhiang.qxb.db.room.dbUtils.lnmDBUtils;
import io.github.yuzhiang.qxb.model.focus.SleepReportStore;
import io.github.yuzhiang.qxb.model.lnm2file;
import io.github.yuzhiang.qxb.model.todo.TodoGroup;
import io.github.yuzhiang.qxb.model.todo.TodoItem;
import io.github.yuzhiang.qxb.model.todo.TodoPrefs;

public final class ParentTodayReport {

    private ParentTodayReport() {
    }

    public static TodayStats buildTodayStats() {
        TodayStats stats = new TodayStats();
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.DAY_OF_YEAR, 1);

        List<Lnm> list = lnmDBUtils.findBetween(start.getTime(), end.getTime());
        for (Lnm l : list) {
            if (l == null || l.createdDate == null) continue;
            long endMs = l.endTime != null ? l.endTime.getTime() : l.schedule.getTime();
            stats.focusMs += Math.max(0, endMs - l.createdDate.getTime());
            stats.blockedAttempts += lnm2file.getScreenOnCount(l.id);
        }

        stats.sleepAttempts = findLatestSleepAttempts();

        List<TodoGroup> groups = TodoPrefs.loadGroups();
        if (groups != null) {
            long today = System.currentTimeMillis();
            for (TodoGroup group : groups) {
                if (group == null || group.getItems() == null) continue;
                for (TodoItem item : group.getItems()) {
                    if (item == null) continue;
                    if (item.isCompleted() && isSameDay(item.getStudentCheckedAt(), today)) {
                        stats.checkedToday++;
                    }
                    if (item.isCompleted() && item.getStudentCheckedAt() > 0 && !item.isParentConfirmed()) {
                        stats.pendingConfirm++;
                    }
                }
            }
        }
        return stats;
    }

    public static int confirmPendingTodos() {
        List<TodoGroup> groups = TodoPrefs.loadGroups();
        if (groups == null || groups.isEmpty()) return 0;
        int confirmed = 0;
        long now = System.currentTimeMillis();
        for (TodoGroup group : groups) {
            if (group == null || group.getItems() == null) continue;
            for (TodoItem item : group.getItems()) {
                if (item == null) continue;
                if (item.isCompleted() && item.getStudentCheckedAt() > 0 && !item.isParentConfirmed()) {
                    item.setParentConfirmed(true);
                    item.setParentConfirmedAt(now);
                    confirmed++;
                }
            }
        }
        if (confirmed > 0) {
            TodoPrefs.saveGroups(groups);
        }
        return confirmed;
    }

    public static String formatDurationMs(long ms) {
        long minutes = Math.max(0, ms) / 60000L;
        long hours = minutes / 60;
        long mins = minutes % 60;
        if (hours > 0) {
            return hours + "小时" + mins + "分钟";
        }
        return mins + "分钟";
    }

    public static String buildSummaryLine(TodayStats stats) {
        if (stats == null) return "--";
        return "专注 " + formatDurationMs(stats.focusMs)
                + " · 未允许应用 " + stats.blockedAttempts + "次"
                + " · 睡眠尝试 " + stats.sleepAttempts + "次"
                + " · 作业打卡 " + stats.checkedToday + "项"
                + " · 待确认 " + stats.pendingConfirm + "项";
    }

    public static String buildDetailLines(TodayStats stats) {
        if (stats == null) return "--";
        StringBuilder sb = new StringBuilder();
        sb.append("今日专注：").append(formatDurationMs(stats.focusMs)).append("\n");
        sb.append("未允许应用：").append(stats.blockedAttempts).append("次\n");
        sb.append("睡眠尝试：").append(stats.sleepAttempts).append("次\n");
        sb.append("作业打卡：").append(stats.checkedToday).append("项\n");
        sb.append("待确认：").append(stats.pendingConfirm).append("项");
        return sb.toString();
    }

    private static int findLatestSleepAttempts() {
        List<SleepReportStore.SleepReport> history = SleepReportStore.loadHistory();
        if (history == null || history.isEmpty()) return 0;
        SleepReportStore.SleepReport latest = history.get(0);
        if (latest == null) return 0;
        long now = System.currentTimeMillis();
        if (now - latest.startAt > 36L * 60 * 60 * 1000) {
            return 0;
        }
        return Math.max(0, latest.attemptCount);
    }

    private static boolean isSameDay(long timeMs, long nowMs) {
        if (timeMs <= 0) return false;
        Calendar a = Calendar.getInstance();
        a.setTimeInMillis(timeMs);
        Calendar b = Calendar.getInstance();
        b.setTimeInMillis(nowMs);
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    public static final class TodayStats {
        public long focusMs = 0L;
        public int blockedAttempts = 0;
        public int sleepAttempts = 0;
        public int checkedToday = 0;
        public int pendingConfirm = 0;
    }
}
