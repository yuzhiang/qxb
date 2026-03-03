package io.github.yuzhiang.qxb.model.reward;

import com.blankj.utilcode.util.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.text.SimpleDateFormat;
import java.util.Locale;

import io.github.yuzhiang.qxb.db.room.bean.Lnm;
import io.github.yuzhiang.qxb.db.room.dbUtils.lnmDBUtils;
import io.github.yuzhiang.qxb.model.focus.FocusRulePrefs;
import io.github.yuzhiang.qxb.model.focus.SleepReportStore;
import io.github.yuzhiang.qxb.model.lnm2file;

public class RewardEngine {
    private static final String DATE_FMT = "yyyy-MM-dd";

    public static RewardPrefs.DailySummary settleDailyIfNeeded() {
        RewardPrefs.RewardState state = RewardPrefs.loadState();
        String today = TimeUtils.date2String(new Date(), DATE_FMT);
        if (today.equals(state.lastSettledDate)) {
            return null;
        }
        String targetDate = getYesterdayString();
        if (targetDate.equals(state.lastSettledDate)) {
            state.lastSettledDate = today;
            state.todayUsedMinutes = 0;
            RewardPrefs.saveState(state);
            return null;
        }

        RewardPrefs.RewardConfig cfg = RewardPrefs.loadConfig();
        RewardPrefs.DailySummary summary = buildDailySummary(targetDate, cfg);
        int rewardMinutes = calcRewardMinutes(summary, cfg);
        summary.rewardMinutes = rewardMinutes;

        if (summary.focusOk) {
            state.focusStreakDays += 1;
        } else {
            state.focusStreakDays = 0;
        }
        if (summary.sleepOk) {
            state.sleepStreakDays += 1;
        } else {
            state.sleepStreakDays = 0;
        }
        int bonus = calcStreakBonus(state, cfg);
        state.balanceMinutes += rewardMinutes + bonus;
        state.lastSettledDate = targetDate;
        RewardPrefs.saveState(state);

        List<RewardPrefs.DailySummary> list = RewardPrefs.loadSummaries();
        list.add(0, summary);
        if (list.size() > 60) {
            list = list.subList(0, 60);
        }
        RewardPrefs.saveSummaries(list);

        updateMilestones(state);
        return summary;
    }

    public static void resetDailyUsageIfNeeded() {
        RewardPrefs.RewardState state = RewardPrefs.loadState();
        String today = TimeUtils.date2String(new Date(), DATE_FMT);
        if (!today.equals(state.lastSettledDate)) {
            state.todayUsedMinutes = 0;
            RewardPrefs.saveState(state);
        }
    }

    private static RewardPrefs.DailySummary buildDailySummary(String date, RewardPrefs.RewardConfig cfg) {
        RewardPrefs.DailySummary s = new RewardPrefs.DailySummary();
        s.date = date;
        s.homeworkMinutes = 0;
        s.totalFocusMinutes = 0;
        s.blockedAttempts = 0;
        s.sleepAttempts = findSleepAttemptsForDate(date);

        Date[] range = getDayRange(date);
        List<Lnm> list = lnmDBUtils.findBetween(range[0], range[1]);
        for (Lnm l : list) {
            if (l == null || l.createdDate == null) continue;
            long endMs = l.endTime != null ? l.endTime.getTime() : l.schedule.getTime();
            long durMin = Math.max(0, (endMs - l.createdDate.getTime()) / 60000L);
            s.totalFocusMinutes += (int) durMin;
            s.blockedAttempts += lnm2file.getScreenOnCount(l.id);
            if (isHomeworkTime(l.createdDate, cfg)) {
                s.homeworkMinutes += (int) durMin;
            }
        }

        s.ruleOk = s.blockedAttempts <= cfg.violationLimit;
        s.focusOk = s.homeworkMinutes >= cfg.exchangeBaseMinutes;
        s.sleepOk = s.sleepAttempts == 0;
        return s;
    }

    private static int calcRewardMinutes(RewardPrefs.DailySummary s, RewardPrefs.RewardConfig cfg) {
        if (!cfg.enabled) return 0;
        if (!s.focusOk || !s.ruleOk) return 0;
        int base = cfg.exchangeBaseMinutes;
        if (base <= 0) return 0;
        int blocks = s.homeworkMinutes / base;
        return blocks * Math.max(0, cfg.exchangeRewardMinutes);
    }

    private static int calcStreakBonus(RewardPrefs.RewardState state, RewardPrefs.RewardConfig cfg) {
        int bonus = 0;
        if (state.focusStreakDays == 3) bonus += cfg.focusStreak3Bonus;
        if (state.focusStreakDays == 7) bonus += cfg.focusStreak7Bonus;
        if (state.sleepStreakDays == 3) bonus += cfg.sleepStreak3Bonus;
        if (state.sleepStreakDays == 7) bonus += cfg.sleepStreak7Bonus;
        return bonus;
    }

    private static int findSleepAttemptsForDate(String date) {
        List<SleepReportStore.SleepReport> history = SleepReportStore.loadHistory();
        for (SleepReportStore.SleepReport r : history) {
            if (r == null) continue;
            String d = TimeUtils.date2String(new Date(r.startAt), DATE_FMT);
            if (date.equals(d)) {
                return r.attemptCount;
            }
        }
        return 0;
    }

    private static void updateMilestones(RewardPrefs.RewardState state) {
        Set<String> milestones = RewardPrefs.loadMilestones();
        int totalFocusMin = calcTotalFocusMinutes();
        maybeAddMilestone(milestones, "FOCUS_600", totalFocusMin >= 600);
        maybeAddMilestone(milestones, "FOCUS_1800", totalFocusMin >= 1800);
        maybeAddMilestone(milestones, "FOCUS_3000", totalFocusMin >= 3000);
        maybeAddMilestone(milestones, "STREAK_FOCUS_3", state.focusStreakDays >= 3);
        maybeAddMilestone(milestones, "STREAK_FOCUS_7", state.focusStreakDays >= 7);
        maybeAddMilestone(milestones, "STREAK_SLEEP_3", state.sleepStreakDays >= 3);
        maybeAddMilestone(milestones, "STREAK_SLEEP_7", state.sleepStreakDays >= 7);
        RewardPrefs.saveMilestones(milestones);
    }

    private static int calcTotalFocusMinutes() {
        List<Lnm> list = lnmDBUtils.findByTimeAsc();
        int total = 0;
        for (Lnm l : list) {
            if (l == null || l.createdDate == null) continue;
            long endMs = l.endTime != null ? l.endTime.getTime() : l.schedule.getTime();
            total += (int) Math.max(0, (endMs - l.createdDate.getTime()) / 60000L);
        }
        return total;
    }

    private static void maybeAddMilestone(Set<String> set, String key, boolean ok) {
        if (ok) set.add(key);
    }

    private static boolean isHomeworkTime(Date date, RewardPrefs.RewardConfig cfg) {
        FocusRulePrefs.RuleConfig rule = FocusRulePrefs.load();
        if (rule == null || !rule.enabled) return false;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int day = cal.get(Calendar.DAY_OF_WEEK);
        boolean weekend = (day == Calendar.SATURDAY || day == Calendar.SUNDAY);
        int nowMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
        FocusRulePrefs.TimeWindow w = weekend ? rule.weekendHomework : rule.schoolHomework;
        return w != null && w.contains(nowMin);
    }

    private static Date[] getDayRange(String date) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FMT, Locale.getDefault());
        cal.setTime(TimeUtils.string2Date(date, sdf));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date start = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        Date end = cal.getTime();
        return new Date[]{start, end};
    }

    private static String getYesterdayString() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        return TimeUtils.date2String(cal.getTime(), DATE_FMT);
    }
}
