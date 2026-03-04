package io.github.yuzhiang.qxb.fragment;

import android.view.View;

import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.github.yuzhiang.qxb.MyUtils.StatusBarUtil;
import io.github.yuzhiang.qxb.R;
import io.github.yuzhiang.qxb.base.LazyFragment;
import io.github.yuzhiang.qxb.databinding.LnmFragmentWeeklyBinding;
import io.github.yuzhiang.qxb.db.room.bean.Lnm;
import io.github.yuzhiang.qxb.db.room.dbUtils.lnmDBUtils;
import io.github.yuzhiang.qxb.model.focus.FocusRulePrefs;
import io.github.yuzhiang.qxb.model.focus.SleepReportStore;
import io.github.yuzhiang.qxb.model.lnm2file;
import io.github.yuzhiang.qxb.model.ParentTodayReport;
import io.github.yuzhiang.qxb.model.reward.RewardEngine;
import io.github.yuzhiang.qxb.model.reward.RewardPrefs;
import io.github.yuzhiang.qxb.view.dialog.SelectDialog;
import io.github.yuzhiang.qxb.view.tastytoast.SimToast;

public class LnmWeeklyFragment extends LazyFragment {

    public static LnmWeeklyFragment newInstance() {
        return new LnmWeeklyFragment();
    }

    private LnmFragmentWeeklyBinding binding;
    private Calendar weekStart;
    private Calendar weekEnd;
    private boolean todayExpanded = false;

    @Override
    protected int getContentViewId() {
        return R.layout.lnm_fragment_weekly;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        binding = LnmFragmentWeeklyBinding.bind(view);
        StatusBarUtil.setPaddingSmart(getContext(), binding.ablWeekly);

        if (binding.tvWeeklyHistory != null) {
            binding.tvWeeklyHistory.setOnClickListener(v -> showWeekSelector());
        }
        setupTodayCard();

        Calendar now = Calendar.getInstance();
        Calendar lastWeek = getWeekStart(now, -1);
        selectWeek(lastWeek);
    }

    @Override
    public void onResume() {
        super.onResume();
        RewardEngine.settleDailyIfNeeded();
        if (weekStart == null) {
            Calendar now = Calendar.getInstance();
            selectWeek(getWeekStart(now, -1));
        } else {
            refreshUi();
        }
    }

    private void selectWeek(Calendar start) {
        if (start == null) return;
        weekStart = (Calendar) start.clone();
        weekEnd = getWeekStart(start, 1);
        refreshUi();
    }

    private void refreshUi() {
        if (binding == null || weekStart == null || weekEnd == null) return;
        updateTodayCard();
        WeeklyStats stats = buildWeeklyStats(weekStart, weekEnd);
        String range = buildRangeLabel(weekStart, weekEnd);
        binding.tvWeeklyRange.setText(range);

        binding.tvWeeklyScore.setText(stats.healthScore + " 分");
        binding.tvWeeklyLevel.setText("健康等级：" + stats.healthLevel);

        binding.tvWeeklyHomework.setText(formatMinutesText(stats.homeworkMinutes));
        binding.tvWeeklyBlocked.setText(stats.blockedAttempts + " 次");
        binding.tvWeeklySleep.setText(stats.sleepAttemptTotal + " 次");
        binding.tvWeeklyReward.setText(formatMinutesText(stats.rewardEarned));

        binding.tvWeeklyFocusDetail.setText(buildFocusDetail(stats));
        binding.tvWeeklySleepDetail.setText(buildSleepDetail(stats));
        binding.tvWeeklyRewardDetail.setText(buildRewardDetail(stats));
        binding.tvWeeklySuggest.setText(buildSuggestions(stats));

        if (getContext() != null) {
            int color = stats.healthScore >= 70
                    ? ContextCompat.getColor(getContext(), R.color.green)
                    : ContextCompat.getColor(getContext(), R.color.colorWarning);
            binding.tvWeeklyScore.setTextColor(color);
        }
    }

    private void setupTodayCard() {
        if (binding == null) return;
        View.OnClickListener toggle = v -> toggleTodayCard();
        if (binding.layoutWeeklyTodayHeader != null) {
            binding.layoutWeeklyTodayHeader.setOnClickListener(toggle);
        }
        if (binding.tvWeeklyTodayToggle != null) {
            binding.tvWeeklyTodayToggle.setOnClickListener(toggle);
        }
        if (binding.cardWeeklyToday != null) {
            binding.cardWeeklyToday.setOnClickListener(toggle);
        }
        applyTodayExpanded(false);
    }

    private void toggleTodayCard() {
        todayExpanded = !todayExpanded;
        applyTodayExpanded(todayExpanded);
    }

    private void applyTodayExpanded(boolean expanded) {
        if (binding == null) return;
        if (binding.layoutWeeklyTodayBody != null) {
            binding.layoutWeeklyTodayBody.setVisibility(expanded ? View.VISIBLE : View.GONE);
        }
        if (binding.tvWeeklyTodayToggle != null) {
            binding.tvWeeklyTodayToggle.setText(expanded ? "收起" : "展开");
        }
    }

    private void updateTodayCard() {
        if (binding == null) return;
        ParentTodayReport.TodayStats stats = ParentTodayReport.buildTodayStats();
        if (binding.tvWeeklyTodaySummary != null) {
            binding.tvWeeklyTodaySummary.setText(ParentTodayReport.buildSummaryLine(stats));
        }
        if (binding.tvWeeklyTodayFocus != null) {
            binding.tvWeeklyTodayFocus.setText("今日专注：" + ParentTodayReport.formatDurationMs(stats.focusMs));
        }
        if (binding.tvWeeklyTodayBlocked != null) {
            binding.tvWeeklyTodayBlocked.setText("未允许应用：" + stats.blockedAttempts + "次");
        }
        if (binding.tvWeeklyTodaySleep != null) {
            binding.tvWeeklyTodaySleep.setText("睡眠尝试：" + stats.sleepAttempts + "次");
        }
        if (binding.tvWeeklyTodayHomework != null) {
            binding.tvWeeklyTodayHomework.setText("作业打卡：" + stats.checkedToday + "项");
        }
        if (binding.tvWeeklyTodayPending != null) {
            binding.tvWeeklyTodayPending.setText("待确认：" + stats.pendingConfirm + "项");
        }
        if (binding.btnWeeklyTodayConfirm != null) {
            boolean enabled = stats.pendingConfirm > 0;
            binding.btnWeeklyTodayConfirm.setEnabled(enabled);
            binding.btnWeeklyTodayConfirm.setAlpha(enabled ? 1f : 0.5f);
            binding.btnWeeklyTodayConfirm.setOnClickListener(v -> {
                int confirmed = ParentTodayReport.confirmPendingTodos();
                if (confirmed <= 0) {
                    SimToast.toastEL("暂无待确认作业");
                } else {
                    SimToast.toastSe("已确认 " + confirmed + " 项作业");
                }
                updateTodayCard();
            });
        }
    }

    private void showWeekSelector() {
        Calendar now = Calendar.getInstance();
        List<Calendar> weeks = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Calendar start = getWeekStart(now, -i);
            weeks.add(start);
            labels.add(formatDate(start));
        }
        new SelectDialog.Builder(getContext())
                .setTitle("选择周报")
                .setList(labels)
                .setSingleSelect()
                .setListener((dialog, data) -> {
                    if (data == null || data.isEmpty()) return;
                    Object selectedKey = data.keySet().iterator().next();
                    int which = selectedKey instanceof Integer ? (Integer) selectedKey : Integer.parseInt(String.valueOf(selectedKey));
                    if (which < 0 || which >= weeks.size()) return;
                    selectWeek(weeks.get(which));
                })
                .show();
    }

    private WeeklyStats buildWeeklyStats(Calendar start, Calendar end) {
        WeeklyStats stats = new WeeklyStats();
        List<Lnm> list = lnmDBUtils.findBetween(start.getTime(), end.getTime());
        Map<String, Integer> focusMinutesMap = new HashMap<>();
        Map<String, Integer> homeworkMinutesMap = new HashMap<>();
        Map<String, Integer> blockedMap = new HashMap<>();

        int longest = 0;
        for (Lnm l : list) {
            if (l == null || l.createdDate == null) continue;
            long endMs = l.endTime != null ? l.endTime.getTime() : l.schedule.getTime();
            int mins = (int) Math.max(0, (endMs - l.createdDate.getTime()) / 60000L);
            stats.focusMinutes += mins;
            stats.focusCount += 1;
            if (mins > longest) longest = mins;

            String key = TimeUtils.date2String(l.createdDate, "yyyy-MM-dd");
            focusMinutesMap.put(key, focusMinutesMap.getOrDefault(key, 0) + mins);
            if (isHomeworkTime(l.createdDate)) {
                homeworkMinutesMap.put(key, homeworkMinutesMap.getOrDefault(key, 0) + mins);
            }
            int blocked = lnm2file.getScreenOnCount(l.id);
            blockedMap.put(key, blockedMap.getOrDefault(key, 0) + blocked);
            stats.blockedAttempts += blocked;
        }
        stats.longestFocusMinutes = longest;
        stats.focusDays = focusMinutesMap.size();
        stats.homeworkMinutes = sumMap(homeworkMinutesMap);

        List<DayBucket> buckets = buildWeekBuckets(start);
        Map<String, SleepReportStore.SleepReport> sleepMap = buildSleepReportMap(buckets);
        List<Long> attemptTimes = new ArrayList<>();
        for (DayBucket b : buckets) {
            SleepReportStore.SleepReport report = sleepMap.get(b.key);
            if (report == null) continue;
            stats.sleepReportDays += 1;
            if (report.attemptCount == 0) {
                stats.sleepOkDays += 1;
            }
            stats.sleepAttemptTotal += Math.max(0, report.attemptCount);
            if (report.attemptTimes != null) {
                attemptTimes.addAll(report.attemptTimes);
            }
        }
        stats.sleepAttemptTimes = formatAttemptTimes(attemptTimes, 10);

        RewardPrefs.RewardConfig cfg = RewardPrefs.loadConfig();
        stats.rewardEnabled = cfg != null && cfg.enabled;
        Map<String, Integer> usageMap = buildRewardUsageMap();
        for (DayBucket b : buckets) {
            int homework = homeworkMinutesMap.getOrDefault(b.key, 0);
            int blocked = blockedMap.getOrDefault(b.key, 0);
            int sleepAttempts = 0;
            SleepReportStore.SleepReport report = sleepMap.get(b.key);
            if (report != null) sleepAttempts = Math.max(0, report.attemptCount);
            stats.rewardEarned += calcRewardMinutes(cfg, homework, blocked, sleepAttempts);
            stats.rewardUsed += usageMap.getOrDefault(b.key, 0);
        }
        RewardPrefs.RewardState st = RewardPrefs.loadState();
        stats.rewardBalance = st.balanceMinutes;

        stats.healthScore = calcHealthScore(stats);
        stats.healthLevel = mapHealthLevel(stats.healthScore);
        return stats;
    }

    private String buildFocusDetail(WeeklyStats stats) {
        int avgPerDay = stats.focusMinutes / 7;
        StringBuilder sb = new StringBuilder();
        sb.append("作业专注：").append(formatMinutesText(stats.homeworkMinutes)).append("\n");
        sb.append("专注总时长：").append(formatMinutesText(stats.focusMinutes)).append(" · ");
        sb.append("日均 ").append(formatMinutesText(avgPerDay)).append("\n");
        sb.append("专注次数：").append(stats.focusCount).append(" 次 · ");
        sb.append("最长单次：").append(formatMinutesText(stats.longestFocusMinutes)).append("\n");
        sb.append("有专注天数：").append(stats.focusDays).append("/7");
        return sb.toString();
    }

    private String buildSleepDetail(WeeklyStats stats) {
        StringBuilder sb = new StringBuilder();
        if (stats.sleepReportDays == 0) {
            sb.append("暂无睡眠记录，建议开启睡眠时间段规则。");
            return sb.toString();
        }
        sb.append("有睡眠记录：").append(stats.sleepReportDays).append("/7 天\n");
        sb.append("早睡达标：").append(stats.sleepOkDays).append(" 天 · ");
        sb.append("尝试玩手机：").append(stats.sleepAttemptTotal).append(" 次\n");
        sb.append("最近尝试时间点：").append(stats.sleepAttemptTimes);
        return sb.toString();
    }

    private String buildRewardDetail(WeeklyStats stats) {
        StringBuilder sb = new StringBuilder();
        if (!stats.rewardEnabled) {
            sb.append("激励功能未开启。");
            return sb.toString();
        }
        sb.append("奖励获得：").append(formatMinutesText(stats.rewardEarned)).append("\n");
        sb.append("兑换使用：").append(formatMinutesText(stats.rewardUsed)).append("\n");
        sb.append("当前余额：").append(formatMinutesText(stats.rewardBalance));
        return sb.toString();
    }

    private String buildSuggestions(WeeklyStats stats) {
        List<String> tips = new ArrayList<>();
        if (stats.sleepReportDays == 0) {
            tips.add("尚未开启睡眠规则，建议设置睡眠时间段减少夜间使用。");
        }
        if (stats.sleepAttemptTotal > 0) {
            tips.add("睡眠时间仍有尝试玩手机，建议睡前将手机放远并开启睡眠专注。");
        }
        if (stats.blockedAttempts > 10) {
            tips.add("违规尝试较多，建议缩小白名单或在作业时间减少手机使用。");
        }
        if (stats.homeworkMinutes < 180) {
            tips.add("作业专注时长偏少，建议每日安排固定作业时段。");
        }
        if (stats.focusDays < 3) {
            tips.add("专注天数偏少，建议每天至少完成一次专注任务。");
        }
        if (stats.rewardUsed > stats.rewardEarned) {
            tips.add("奖励使用快于获得，建议把奖励与作业完成强绑定。");
        }
        if (tips.isEmpty()) {
            tips.add("本周习惯稳定，继续保持并逐步提升作业专注时长。");
        }
        int limit = Math.min(3, tips.size());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < limit; i++) {
            sb.append(i + 1).append(". ").append(tips.get(i));
            if (i < limit - 1) sb.append("\n");
        }
        return sb.toString();
    }

    private int calcHealthScore(WeeklyStats stats) {
        int score = 60;
        score += Math.min(20, stats.homeworkMinutes / 30);
        score += Math.min(10, stats.focusDays * 2);
        score += Math.min(10, stats.sleepOkDays * 2);
        score -= Math.min(20, stats.blockedAttempts * 2);
        score -= Math.min(20, stats.sleepAttemptTotal * 4);
        if (score < 0) score = 0;
        if (score > 100) score = 100;
        return score;
    }

    private String mapHealthLevel(int score) {
        if (score >= 85) return "优秀";
        if (score >= 70) return "良好";
        if (score >= 50) return "需改进";
        return "需要关注";
    }

    private Map<String, Integer> buildRewardUsageMap() {
        Map<String, Integer> map = new HashMap<>();
        List<RewardPrefs.RewardUsage> list = RewardPrefs.loadUsage();
        for (RewardPrefs.RewardUsage u : list) {
            if (u == null || u.date == null) continue;
            int prev = map.getOrDefault(u.date, 0);
            map.put(u.date, prev + Math.max(0, u.usedMinutes));
        }
        return map;
    }

    private int calcRewardMinutes(RewardPrefs.RewardConfig cfg, int homeworkMinutes, int blockedAttempts, int sleepAttempts) {
        if (cfg == null || !cfg.enabled) return 0;
        if (cfg.exchangeBaseMinutes <= 0 || cfg.exchangeRewardMinutes <= 0) return 0;
        boolean ruleOk = blockedAttempts <= cfg.violationLimit;
        boolean focusOk = homeworkMinutes >= cfg.exchangeBaseMinutes;
        boolean sleepOk = sleepAttempts == 0;
        if (!ruleOk || !focusOk || !sleepOk) return 0;
        int blocks = homeworkMinutes / cfg.exchangeBaseMinutes;
        return blocks * cfg.exchangeRewardMinutes;
    }

    private boolean isHomeworkTime(Date date) {
        FocusRulePrefs.RuleConfig rule = FocusRulePrefs.load();
        if (rule == null || !rule.enabled || date == null) return false;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int day = cal.get(Calendar.DAY_OF_WEEK);
        boolean weekend = (day == Calendar.SATURDAY || day == Calendar.SUNDAY);
        int nowMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
        FocusRulePrefs.TimeWindow w = weekend ? rule.weekendHomework : rule.schoolHomework;
        return w != null && w.contains(nowMin);
    }

    private List<DayBucket> buildWeekBuckets(Calendar start) {
        List<DayBucket> buckets = new ArrayList<>();
        Calendar cal = (Calendar) start.clone();
        for (int i = 0; i < 7; i++) {
            DayBucket b = new DayBucket();
            b.start = (Calendar) cal.clone();
            b.end = (Calendar) cal.clone();
            b.end.add(Calendar.DAY_OF_YEAR, 1);
            b.key = TimeUtils.date2String(b.start.getTime(), "yyyy-MM-dd");
            b.label = TimeUtils.date2String(b.start.getTime(), "M/d");
            buckets.add(b);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return buckets;
    }

    private Map<String, SleepReportStore.SleepReport> buildSleepReportMap(List<DayBucket> buckets) {
        Map<String, SleepReportStore.SleepReport> map = new HashMap<>();
        if (buckets == null || buckets.isEmpty()) return map;
        HashSet<String> keys = new HashSet<>();
        for (DayBucket b : buckets) {
            if (b != null && b.key != null) keys.add(b.key);
        }
        List<SleepReportStore.SleepReport> history = SleepReportStore.loadHistory();
        for (SleepReportStore.SleepReport report : history) {
            if (report == null || report.startAt <= 0) continue;
            String key = TimeUtils.date2String(new Date(report.startAt), "yyyy-MM-dd");
            if (!keys.contains(key)) continue;
            if (!map.containsKey(key)) map.put(key, report);
        }
        return map;
    }

    private String formatAttemptTimes(List<Long> times, int maxItems) {
        if (times == null || times.isEmpty()) return "无";
        List<Long> sorted = new ArrayList<>(times);
        Collections.sort(sorted);
        int start = Math.max(0, sorted.size() - maxItems);
        List<Long> tail = sorted.subList(start, sorted.size());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tail.size(); i++) {
            sb.append(TimeUtils.date2String(new Date(tail.get(i)), "HH:mm"));
            if (i < tail.size() - 1) sb.append("、");
        }
        if (start > 0) sb.append(" ...");
        return sb.toString();
    }

    private String buildRangeLabel(Calendar start, Calendar end) {
        Calendar endLabel = (Calendar) end.clone();
        endLabel.add(Calendar.DAY_OF_YEAR, -1);
        String range = formatDate(start) + " ~ " + formatDate(endLabel);
        Calendar now = Calendar.getInstance();
        Calendar thisWeek = getWeekStart(now, 0);
        Calendar lastWeek = getWeekStart(now, -1);
        if (sameDay(start, thisWeek)) {
            return "本周（" + range + "）";
        }
        if (sameDay(start, lastWeek)) {
            return "上周（" + range + "）";
        }
        return "历史周（" + range + "）";
    }

    private boolean sameDay(Calendar a, Calendar b) {
        if (a == null || b == null) return false;
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    private String formatDate(Calendar cal) {
        return String.format(Locale.CHINA, "%d-%02d-%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH));
    }

    private String formatMinutesText(int minutes) {
        int mins = Math.max(0, minutes);
        if (mins >= 60) {
            int hours = mins / 60;
            int rem = mins % 60;
            return hours + "小时" + rem + "分钟";
        }
        return mins + "分钟";
    }

    private Calendar getWeekStart(Calendar base, int weekOffset) {
        Calendar cal = (Calendar) base.clone();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.add(Calendar.WEEK_OF_YEAR, weekOffset);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    private int sumMap(Map<String, Integer> map) {
        int total = 0;
        if (map == null) return 0;
        for (Integer v : map.values()) {
            if (v != null) total += v;
        }
        return total;
    }

    private static class DayBucket {
        Calendar start;
        Calendar end;
        String key;
        String label;
    }

    private static class WeeklyStats {
        int focusMinutes;
        int focusCount;
        int homeworkMinutes;
        int blockedAttempts;
        int longestFocusMinutes;
        int focusDays;
        int sleepAttemptTotal;
        int sleepReportDays;
        int sleepOkDays;
        String sleepAttemptTimes = "无";
        int rewardEarned;
        int rewardUsed;
        int rewardBalance;
        boolean rewardEnabled;
        int healthScore;
        String healthLevel = "--";
    }
}
