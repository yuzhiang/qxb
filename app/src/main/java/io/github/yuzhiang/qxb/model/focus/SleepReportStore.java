package io.github.yuzhiang.qxb.model.focus;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.SPUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.reflect.TypeToken;

public class SleepReportStore {
    private static final String KEY_SLEEP_CURRENT = "sleep_report_current";
    private static final String KEY_SLEEP_HISTORY = "sleep_report_history";
    private static final int MAX_HISTORY = 60;

    public static class SleepReport {
        public long startAt;
        public long endAt;
        public int attemptCount;
        public List<Long> attemptTimes = new ArrayList<>();
    }

    public static void startSession(long startAt, long plannedEndAt) {
        SleepReport current = loadCurrent();
        if (current != null && current.endAt <= 0 && current.startAt > 0) {
            return;
        }
        SleepReport report = new SleepReport();
        report.startAt = startAt;
        report.endAt = 0L;
        saveCurrent(report);
    }

    public static void recordAttempt(long ts) {
        SleepReport current = loadCurrent();
        if (current == null) {
            current = new SleepReport();
            current.startAt = ts;
        }
        current.attemptCount += 1;
        if (current.attemptTimes == null) {
            current.attemptTimes = new ArrayList<>();
        }
        current.attemptTimes.add(ts);
        if (current.attemptTimes.size() > 200) {
            current.attemptTimes = current.attemptTimes.subList(current.attemptTimes.size() - 200, current.attemptTimes.size());
        }
        saveCurrent(current);
    }

    public static void finishSession(long endAt) {
        SleepReport current = loadCurrent();
        if (current == null) return;
        current.endAt = endAt;
        saveCurrent(null);
        List<SleepReport> history = loadHistory();
        history.add(0, current);
        if (history.size() > MAX_HISTORY) {
            history = history.subList(0, MAX_HISTORY);
        }
        saveHistory(history);
    }

    public static SleepReport loadCurrent() {
        String json = SPUtils.getInstance().getString(KEY_SLEEP_CURRENT, "");
        if (json == null || json.trim().isEmpty()) return null;
        try {
            return GsonUtils.fromJson(json, SleepReport.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static List<SleepReport> loadHistory() {
        String json = SPUtils.getInstance().getString(KEY_SLEEP_HISTORY, "");
        if (json == null || json.trim().isEmpty()) return new ArrayList<>();
        try {
            List<SleepReport> list = GsonUtils.fromJson(json, new TypeToken<List<SleepReport>>() {
            }.getType());
            return list == null ? new ArrayList<>() : new ArrayList<>(list);
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    private static void saveCurrent(SleepReport report) {
        if (report == null) {
            SPUtils.getInstance().put(KEY_SLEEP_CURRENT, "");
            return;
        }
        SPUtils.getInstance().put(KEY_SLEEP_CURRENT, GsonUtils.toJson(report));
    }

    private static void saveHistory(List<SleepReport> history) {
        SPUtils.getInstance().put(KEY_SLEEP_HISTORY, GsonUtils.toJson(history));
    }

    public static void overwriteHistory(List<SleepReport> history) {
        saveHistory(history == null ? new ArrayList<>() : history);
    }

    public static void clearCurrent() {
        saveCurrent(null);
    }

    public static int calcAttemptMinutes(SleepReport report) {
        if (report == null || report.attemptTimes == null || report.attemptTimes.isEmpty()) {
            return 0;
        }
        Set<Long> minutes = new HashSet<>();
        for (Long ts : report.attemptTimes) {
            if (ts == null) continue;
            minutes.add(ts / 60000L);
        }
        return minutes.size();
    }

    public static String formatAttemptTimes(SleepReport report, int maxItems) {
        if (report == null || report.attemptTimes == null || report.attemptTimes.isEmpty()) {
            return "无";
        }
        List<Long> times = new ArrayList<>(report.attemptTimes);
        Collections.sort(times);
        int start = Math.max(0, times.size() - maxItems);
        List<Long> tail = times.subList(start, times.size());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tail.size(); i++) {
            sb.append(com.blankj.utilcode.util.TimeUtils.date2String(new java.util.Date(tail.get(i)), "HH:mm"));
            if (i < tail.size() - 1) {
                sb.append("、");
            }
        }
        if (start > 0) {
            sb.append(" ...");
        }
        return sb.toString();
    }
}
