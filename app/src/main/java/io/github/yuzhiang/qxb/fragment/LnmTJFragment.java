package io.github.yuzhiang.qxb.fragment;

import static io.github.yuzhiang.qxb.common.LzuUrl.lnmMsg;
import static io.github.yuzhiang.qxb.db.room.dbUtils.lnmDBUtils.findBetween;
import static io.github.yuzhiang.qxb.view.tastytoast.SimToast.toastEL;
import static io.github.yuzhiang.qxb.view.tastytoast.SimToast.toastSL;
import static io.github.yuzhiang.qxb.view.tastytoast.SimToast.toastSe;

import android.content.Context;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.LayoutInflater;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.ColorTemplate;

import io.github.yuzhiang.qxb.MyUtils.StatusBarUtil;
import io.github.yuzhiang.qxb.MyUtils.UsrMsgUtils;
import io.github.yuzhiang.qxb.R;
import io.github.yuzhiang.qxb.activity.LnmRecordActivity;
import io.github.yuzhiang.qxb.base.BaseDialog;
import io.github.yuzhiang.qxb.base.LazyFragment;
import io.github.yuzhiang.qxb.databinding.LnmFragmentTgBinding;
import io.github.yuzhiang.qxb.db.room.bean.Lnm;
import io.github.yuzhiang.qxb.db.room.dbUtils.lnmDBUtils;
import io.github.yuzhiang.qxb.model.eventbus.MeLnmShowChart;

import io.github.yuzhiang.qxb.model.eventbus.TodoImportantChanged;
import io.github.yuzhiang.qxb.model.todo.TodoGroup;
import io.github.yuzhiang.qxb.model.todo.TodoItem;
import io.github.yuzhiang.qxb.model.todo.TodoPrefs;
import io.github.yuzhiang.qxb.model.todo.TodoTimeUtils;
import io.github.yuzhiang.qxb.model.StudyProjectRecord;
import io.github.yuzhiang.qxb.model.lnm2file;
import io.github.yuzhiang.qxb.model.focus.SleepReportStore;
import io.github.yuzhiang.qxb.model.focus.FocusRulePrefs;
import io.github.yuzhiang.qxb.model.reward.RewardEngine;
import io.github.yuzhiang.qxb.model.reward.RewardPrefs;
import io.github.yuzhiang.qxb.view.dialog.MessageDialog;
import io.github.yuzhiang.qxb.view.dialog.SelectDialog;
import io.github.yuzhiang.qxb.view.tastytoast.SimToast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.Collections;



public class LnmTJFragment extends LazyFragment {

    public static LnmTJFragment newInstance() {
        return new LnmTJFragment();
    }

    private Context mContext;
    private static final String WEEKLY_REPORT_KEY = "weekly_report_last_shown";
    private static final String WEEKLY_REPORT_ENABLED = "weekly_report_enabled";
    private static final String WEEKLY_REPORT_REPEAT = "weekly_report_allow_repeat";
    private static final String STATS_CARD_ORDER_KEY = "stats_card_order";
    private static final String STATS_ADVANCED_EXPANDED_KEY = "stats_advanced_expanded";
    private boolean projectUnitInMinutes = true;
    private List<String> weeksLabels = new ArrayList<>();
    private List<String> recordLabels = new ArrayList<>();
    private List<String> rewardLabels = new ArrayList<>();


    @Override
    protected int getContentViewId() {
        return R.layout.lnm_fragment_tg;
    }


    private LnmFragmentTgBinding binding;

    /**
     * 初始化视图
     *
     * @param view
     */
    @Override
    protected void initView(View view) {
        super.initView(view);

        LogUtils.i("lnmTj======initView");

        mContext = getContext();

        binding = LnmFragmentTgBinding.bind(view);
        StatusBarUtil.setPaddingSmart(mContext, binding.ablLtj);

        updateImportantBanner();
        updateSleepReportHint();
        binding.btnSleepReportHistory.setOnClickListener(v -> showSleepReportHistory());

        binding.tvSeedData.setOnClickListener(v -> showSeedDataDialog());


        int textColor = ContextCompat.getColor(mContext, R.color.colorTextContent);

        binding.tvLnmCount3.setText("作业专注趋势（近7天）");

        XAxis xAxisWeek = binding.chartLnmWeeks.getXAxis();
        xAxisWeek.setTextColor(textColor);
        xAxisWeek.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis ylAxisWeek = binding.chartLnmWeeks.getAxisLeft();
        ylAxisWeek.setTextColor(textColor);
        ylAxisWeek.enableGridDashedLine(10f, 10f, 0f);

        YAxis yrAxisWeek = binding.chartLnmWeeks.getAxisRight();
        yrAxisWeek.setTextColor(textColor);
        yrAxisWeek.setDrawGridLines(false);

        XAxis xAxisRecord = binding.chartRecordTrend.getXAxis();
        xAxisRecord.setTextColor(textColor);
        xAxisRecord.setPosition(XAxis.XAxisPosition.BOTTOM);
        YAxis ylAxisRecord = binding.chartRecordTrend.getAxisLeft();
        ylAxisRecord.setTextColor(textColor);
        ylAxisRecord.enableGridDashedLine(10f, 10f, 0f);
        YAxis yrAxisRecord = binding.chartRecordTrend.getAxisRight();
        yrAxisRecord.setTextColor(textColor);
        yrAxisRecord.setDrawGridLines(false);


        XAxis xAxisReward = binding.chartRewardTrend.getXAxis();
        xAxisReward.setTextColor(textColor);
        xAxisReward.setPosition(XAxis.XAxisPosition.BOTTOM);
        YAxis ylAxisReward = binding.chartRewardTrend.getAxisLeft();
        ylAxisReward.setTextColor(textColor);
        ylAxisReward.enableGridDashedLine(10f, 10f, 0f);
        YAxis yrAxisReward = binding.chartRewardTrend.getAxisRight();
        yrAxisReward.setTextColor(textColor);
        yrAxisReward.setDrawGridLines(false);

        setupProjectUnitToggle();
        setupChartClickDetails();
        updatePrimaryCharts();
        binding.chartLnmWeeks.getDescription().setText("作业专注趋势（近7天）");
        binding.chartLnmWeeks.getDescription().setTextColor(textColor);
        binding.chartLnmWeeks.setNoDataText("暂无作业专注数据");


        binding.chartRecordTrend.getDescription().setText("睡眠尝试玩手机（近7天）");
        binding.chartRecordTrend.getDescription().setTextColor(textColor);
        binding.chartRecordTrend.setNoDataText("暂无睡眠报告数据");

            binding.chartRewardTrend.getDescription().setText("奖励/兑换趋势（近7天）");
            binding.chartRewardTrend.getDescription().setTextColor(textColor);
            binding.chartRewardTrend.setNoDataText("暂无奖励数据");

        binding.chartLnmWeeks.getXAxis().setGranularity(1f); // minimum axis-step (interval) is 1

            binding.chartRecordTrend.getXAxis().setGranularity(1f);

            binding.chartRewardTrend.getXAxis().setGranularity(1f);


        initAdvancedToggle();
        setupStatsCardReorder();


    }

    private void setupStatsCardReorder() {
        if (binding == null) return;
        applyStatsCardOrder();
        setCardLongPress(binding.cardStatSummary);
//        setCardLongPress(binding.cardStatToggle);
        setCardLongPress(binding.cardStatWeeks);
        setCardLongPress(binding.cardStatProject);
        setCardLongPress(binding.cardStatRecord);
        setCardLongPress(binding.cardStatReward);
    }

    private void initAdvancedToggle() {
        if (binding == null) return;
        boolean expanded = SPUtils.getInstance().getBoolean(STATS_ADVANCED_EXPANDED_KEY, false);
        setAdvancedExpanded(expanded);
        binding.tvStatToggle.setOnClickListener(v -> {
            boolean nowExpanded = binding.cardStatProject.getVisibility() != View.VISIBLE;
            setAdvancedExpanded(nowExpanded);
            SPUtils.getInstance().put(STATS_ADVANCED_EXPANDED_KEY, nowExpanded);
        });
    }

    private void setAdvancedExpanded(boolean expanded) {

        binding.cardStatProject.setVisibility(expanded ? View.VISIBLE : View.GONE);

        binding.tvStatToggle.setText(expanded ? "隐藏学科统计" : "显示学科统计");

    }

    private void setCardLongPress(View card) {
        if (card == null) return;
        card.setOnLongClickListener(v -> {
            showCardOrderDialog(v);
            return true;
        });
    }

    private void showCardOrderDialog(View card) {
        if (binding == null || card == null) return;
        String[] items = new String[]{"上移", "下移", "置顶", "置底"};
        new SelectDialog.Builder(mContext)
                .setTitle("调整顺序")
                .setList(items)
                .setSingleSelect()
                .setListener((dialog, data) -> {
                    if (data == null || data.isEmpty()) return;
                    Object selectedKey = data.keySet().iterator().next();
                    int which = selectedKey instanceof Integer ? (Integer) selectedKey : Integer.parseInt(String.valueOf(selectedKey));
                    switch (which) {
                        case 0:
                            moveCard(card, -1);
                            break;
                        case 1:
                            moveCard(card, 1);
                            break;
                        case 2:
                            moveCardTo(card, 0);
                            break;
                        case 3:
                            moveCardTo(card, binding.layoutStatsCards.getChildCount() - 1);
                            break;
                        default:
                            break;
                    }
                    saveStatsCardOrder();
                })
                .show();
    }

    private void moveCard(View card, int delta) {
        if (binding == null) return;
        int index = binding.layoutStatsCards.indexOfChild(card);
        if (index < 0) return;
        int newIndex = Math.max(0, Math.min(binding.layoutStatsCards.getChildCount() - 1, index + delta));
        if (newIndex == index) return;
        moveCardTo(card, newIndex);
    }

    private void moveCardTo(View card, int index) {
        if (binding == null) return;
        int count = binding.layoutStatsCards.getChildCount();
        if (index < 0 || index >= count) return;
        binding.layoutStatsCards.removeView(card);
        binding.layoutStatsCards.addView(card, index);
    }

    private void saveStatsCardOrder() {
        if (binding == null) return;
        StringBuilder sb = new StringBuilder();
        int count = binding.layoutStatsCards.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = binding.layoutStatsCards.getChildAt(i);
            if (child == null) continue;
            try {
                String name = getResources().getResourceEntryName(child.getId());
                if (name == null || name.trim().isEmpty()) continue;
                if (sb.length() > 0) sb.append(",");
                sb.append(name);
            } catch (Exception ignored) {
            }
        }
        SPUtils.getInstance().put(STATS_CARD_ORDER_KEY, sb.toString());
    }

    private void applyStatsCardOrder() {
        if (binding == null) return;
        String saved = SPUtils.getInstance().getString(STATS_CARD_ORDER_KEY, "");
        if (saved == null || saved.trim().isEmpty()) return;
        String[] names = saved.split(",");
        if (names.length == 0) return;
        List<View> views = new ArrayList<>();
        for (String name : names) {
            int id = getResources().getIdentifier(name, "id", mContext.getPackageName());
            if (id == 0) continue;
            View v = binding.layoutStatsCards.findViewById(id);
            if (v != null) views.add(v);
        }
        if (views.isEmpty()) return;
        for (View v : views) {
            binding.layoutStatsCards.removeView(v);
        }
        for (View v : views) {
            binding.layoutStatsCards.addView(v);
        }
    }

    /**
     * 初始化数据
     */
    @Override
    protected void initData() {
        super.initData();
        LogUtils.i("lnmTj======initData");

        EventBus.getDefault().register(this);
    }

    @Override
    protected void initEvent() {
        super.initEvent();

        binding.tvLnmTjTitle.setOnLongClickListener(v -> {
            showSeedDataDialog();
            return true;
        });

        binding.SwipeRefreshLayoutLnmTg.setOnRefreshListener(refreshLayout -> getMyAll());

        long ll = lnmDBUtils.count();
        LogUtils.i("数据：" + ll);
        if (ll == 0L) {
            binding.SwipeRefreshLayoutLnmTg.autoRefresh();
        } else {
            showChart();
        }

    }

    /**
     * onDestroyView中进行解绑操作
     */


    private void setupProjectUnitToggle() {
        if (binding == null) return;
        binding.tvProjectUnitMin.setOnClickListener(v -> {
            projectUnitInMinutes = true;
            updateProjectUnitUI();
            updateProjectCharts();
        });
        binding.tvProjectUnitSec.setOnClickListener(v -> {
            projectUnitInMinutes = false;
            updateProjectUnitUI();
            updateProjectCharts();
        });
        updateProjectUnitUI();
    }

    private void updateProjectUnitUI() {
        if (binding == null) return;
        if (projectUnitInMinutes) {
            binding.tvProjectUnitMin.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            binding.tvProjectUnitSec.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextContent));
        } else {
            binding.tvProjectUnitMin.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextContent));
            binding.tvProjectUnitSec.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        }
        updateProjectUnitTitle();
    }

    private void updateProjectUnitTitle() {
        if (binding == null) return;
        String unit = projectUnitInMinutes ? "分钟" : "秒";
        {
            binding.tvProjectTitle.setText("学科分布（" + unit + "）");
        }
        {
            binding.tvProjectBarTitle.setText("学科时长（" + unit + "）");
        }
    }

    private void setupChartClickDetails() {
        if (binding == null) return;
        View.OnClickListener weeksListener = v -> showWeeksChartDetail();
        View.OnClickListener projectListener = v -> showProjectChartDetail();
        View.OnClickListener recordListener = v -> showRecordTrendDetail();
        View.OnClickListener rewardListener = v -> showRewardTrendDetail();

        // Chart clicks show point detail; avoid duplicate dialogs from chart click + point selection.
        binding.tvLnmCount3.setOnClickListener(weeksListener);

        binding.chartProjectPie.setOnClickListener(projectListener);
        binding.chartProjectBar.setOnClickListener(projectListener);
        binding.tvProjectTitle.setOnClickListener(projectListener);
        binding.tvProjectBarTitle.setOnClickListener(projectListener);

        // Chart clicks show point detail; avoid duplicate dialogs from chart click + point selection.
        binding.tvRecordTitle.setOnClickListener(recordListener);
        binding.tvRewardTrendTitle.setOnClickListener(rewardListener);

        setupChartValueListeners();
    }

    private void setupChartValueListeners() {
        if (binding == null) return;
        {
            binding.chartLnmWeeks.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    String label = getLabelByIndex(weeksLabels, e.getX());
                    String value = formatMinutesValue(e.getY());
                    showPointDetailDialog("作业专注趋势", label, "作业专注", value, "");
                }

                @Override
                public void onNothingSelected() {
                }
            });
        }
        {
            binding.chartRecordTrend.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    String label = getLabelByIndex(recordLabels, e.getX());
                    String value = formatCountValue(e.getY(), "次");
                    showPointDetailDialog("睡眠尝试玩手机", label, "尝试玩手机", value, "");
                }

                @Override
                public void onNothingSelected() {
                }
            });
        }
        {
            binding.chartRewardTrend.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    String label = getLabelByIndex(rewardLabels, e.getX());
                    int dataSetIndex = h != null ? h.getDataSetIndex() : 0;
                    String series = (binding.chartRewardTrend.getData() != null
                            && dataSetIndex >= 0
                            && binding.chartRewardTrend.getData().getDataSetByIndex(dataSetIndex) != null)
                            ? binding.chartRewardTrend.getData().getDataSetByIndex(dataSetIndex).getLabel()
                            : "";
                    String metric = series.contains("兑换") ? "兑换使用" : "奖励获得";
                    String value = formatMinutesValue(e.getY());
                    showPointDetailDialog("奖励/兑换趋势", label, metric, value, "");
                }

                @Override
                public void onNothingSelected() {
                }
            });
        }
    }

    private String getLabelByIndex(List<String> labels, float x) {
        int i = Math.round(x);
        if (labels == null || labels.isEmpty() || i < 0 || i >= labels.size()) {
            return "日期未知";
        }
        return labels.get(i);
    }

    private String buildWeekDateLabel(int dataSetIndex, int dayIndex, String series) {
        int offset;
        if ("上周".equals(series)) {
            offset = -1;
        } else if ("上上周".equals(series)) {
            offset = -2;
        } else if ("本周".equals(series)) {
            offset = 0;
        } else {
            offset = -dataSetIndex;
        }
        Calendar start = getWeekStart(Calendar.getInstance(), offset);
        start.add(Calendar.DAY_OF_YEAR, Math.max(0, dayIndex));
        String date = TimeUtils.date2String(start.getTime(), "M月d日");
        String weekDay = TimeUtils.getChineseWeek(start.getTime());
        return date + " " + weekDay;
    }

    private String formatMinutesValue(float value) {
        int mins = Math.max(0, Math.round(value));
        return formatMinutesText(mins);
    }

    private String formatCountValue(float value, String unit) {
        int count = Math.max(0, Math.round(value));
        return count + unit;
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

    private void showPointDetailDialog(String title, String dateLabel, String metricName, String value, String extra) {
        StringBuilder sb = new StringBuilder();
        sb.append("日期：").append(dateLabel).append("\n");
        sb.append(metricName).append("：").append(value);
        if (extra != null && !extra.trim().isEmpty()) {
            sb.append("\n").append(extra);
        }
        showStatsDetailDialog(title, sb.toString());
    }


    private void updateStatsHeader() {
        Calendar now = Calendar.getInstance();

        Range day = buildDayRange(now);
        Range last7 = buildRecentDaysRange(7);
        updateKeyStats(day, last7);
    }

    private void updateKeyStats(Range day, Range week) {
        int blockedToday = calcBlockedAttempts(day.start, day.end);
        int blockedWeek = calcBlockedAttempts(week.start, week.end);
        int homeworkToday = calcHomeworkMinutes(day.start, day.end);
        int homeworkWeek = calcHomeworkMinutes(week.start, week.end);
        int sleepOkDays = calcSleepOkDays(7);

        RewardEngine.settleDailyIfNeeded();
        RewardPrefs.RewardState st = RewardPrefs.loadState();

        {
            binding.tvStatTodayTime.setText("规则遵守：今日违规 " + blockedToday + " 次 · 近7天 " + blockedWeek + " 次");
        }
        {
            binding.tvStatTodayCount.setText("作业专注：今日 " + formatMinutesText(homeworkToday) + " · 近7天 " + formatMinutesText(homeworkWeek));
        }
        {
            binding.tvStatWeek.setText("早睡达标：" + sleepOkDays + "/7 天");
        }
        {
            binding.tvStatMonth.setText("连续打卡：专注 " + st.focusStreakDays + " 天 · 早睡 " + st.sleepStreakDays + " 天");
        }
    }

    private int calcBlockedAttempts(Calendar start, Calendar end) {
        List<Lnm> list = lnmDBUtils.findBetween(start.getTime(), end.getTime());
        if (list == null || list.isEmpty()) return 0;
        int total = 0;
        for (Lnm l : list) {
            if (l == null) continue;
            total += lnm2file.getScreenOnCount(l.id);
        }
        return total;
    }

    private int calcHomeworkMinutes(Calendar start, Calendar end) {
        List<Lnm> list = lnmDBUtils.findBetween(start.getTime(), end.getTime());
        if (list == null || list.isEmpty()) return 0;
        int total = 0;
        for (Lnm l : list) {
            if (l == null || l.createdDate == null) continue;
            if (!isHomeworkTime(l.createdDate)) continue;
            long endMs = l.endTime != null ? l.endTime.getTime() : l.schedule.getTime();
            total += (int) Math.max(0, (endMs - l.createdDate.getTime()) / 60000L);
        }
        return total;
    }

    private boolean isHomeworkTime(Date date) {
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

    private int calcSleepOkDays(int days) {
        List<DayBucket> buckets = buildRecentDayBuckets(days);
        if (buckets.isEmpty()) return 0;
        Map<String, SleepReportStore.SleepReport> map = buildSleepReportMap(buckets);
        int ok = 0;
        for (DayBucket b : buckets) {
            SleepReportStore.SleepReport r = map.get(b.key);
            if (r != null && r.attemptCount == 0) ok++;
        }
        return ok;
    }

    private static class Range {
        Calendar start;
        Calendar end;
        Range(Calendar s, Calendar e) { start = s; end = e; }
    }

    private static class Stat {
        long totalMs;
        int count;
        Stat(long totalMs, int count) { this.totalMs = totalMs; this.count = count; }
    }

    private static class DayBucket {
        Calendar start;
        Calendar end;
        String key;
        String label;
    }

    private Range buildDayRange(Calendar now) {
        Calendar s = (Calendar) now.clone();
        s.set(Calendar.HOUR_OF_DAY, 0);
        s.set(Calendar.MINUTE, 0);
        s.set(Calendar.SECOND, 0);
        s.set(Calendar.MILLISECOND, 0);
        Calendar e = (Calendar) s.clone();
        e.add(Calendar.DAY_OF_YEAR, 1);
        return new Range(s, e);
    }

    private Range buildWeekRange(Calendar now, int offset) {
        Calendar s = getWeekStart(now, offset);
        Calendar e = getWeekStart(now, offset + 1);
        return new Range(s, e);
    }

    private Range buildMonthRange(Calendar now, int offset) {
        Calendar s = (Calendar) now.clone();
        s.set(Calendar.DAY_OF_MONTH, 1);
        s.set(Calendar.HOUR_OF_DAY, 0);
        s.set(Calendar.MINUTE, 0);
        s.set(Calendar.SECOND, 0);
        s.set(Calendar.MILLISECOND, 0);
        s.add(Calendar.MONTH, offset);
        Calendar e = (Calendar) s.clone();
        e.add(Calendar.MONTH, 1);
        return new Range(s, e);
    }

    private Range buildYearRange(Calendar now, int offset) {
        Calendar s = (Calendar) now.clone();
        s.set(Calendar.MONTH, 0);
        s.set(Calendar.DAY_OF_MONTH, 1);
        s.set(Calendar.HOUR_OF_DAY, 0);
        s.set(Calendar.MINUTE, 0);
        s.set(Calendar.SECOND, 0);
        s.set(Calendar.MILLISECOND, 0);
        s.add(Calendar.YEAR, offset);
        Calendar e = (Calendar) s.clone();
        e.add(Calendar.YEAR, 1);
        return new Range(s, e);
    }

    private Stat calcStat(Calendar start, Calendar end) {
        List<Lnm> list = lnmDBUtils.findBetween(start.getTime(), end.getTime());
        long total = 0;
        int count = 0;
        for (Lnm l : list) {
            if (l == null || l.createdDate == null) continue;
            long endMs = l.endTime != null ? l.endTime.getTime() : l.schedule.getTime();
            total += Math.max(0, endMs - l.createdDate.getTime());
            count++;
        }
        return new Stat(total, count);
    }

    private Stat calcAvgOfPrevious(Range range, int periods, String type) {
        long total = 0;
        int count = 0;
        for (int i = 1; i <= periods; i++) {
            Range r;
            if ("week".equals(type)) {
                r = buildWeekRange(range.start, -i);
            } else if ("month".equals(type)) {
                r = buildMonthRange(range.start, -i);
            } else {
                r = buildYearRange(range.start, -i);
            }
            Stat s = calcStat(r.start, r.end);
            total += s.totalMs;
            count += s.count;
        }
        long avgMs = periods == 0 ? 0 : total / periods;
        int avgCount = periods == 0 ? 0 : count / periods;
        return new Stat(avgMs, avgCount);
    }

    private String formatDiff(long diffMs) {
        if (diffMs == 0) return "持平";
        String sign = diffMs > 0 ? "↑" : "↓";
        return sign + formatDuration(Math.abs(diffMs));
    }

    private Range buildRecentDaysRange(int days) {
        Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, 0);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);
        end.add(Calendar.DAY_OF_YEAR, 1);
        Calendar start = (Calendar) end.clone();
        start.add(Calendar.DAY_OF_YEAR, -days);
        return new Range(start, end);
    }

    private List<DayBucket> buildRecentDayBuckets(int days) {
        List<DayBucket> buckets = new ArrayList<>();
        if (days <= 0) return buckets;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_YEAR, -(days - 1));
        for (int i = 0; i < days; i++) {
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
        java.util.HashSet<String> keys = new java.util.HashSet<>();
        for (DayBucket b : buckets) {
            if (b != null && b.key != null) keys.add(b.key);
        }
        List<SleepReportStore.SleepReport> history = SleepReportStore.loadHistory();
        for (SleepReportStore.SleepReport report : history) {
            if (report == null || report.startAt <= 0) continue;
            String key = TimeUtils.date2String(new Date(report.startAt), "yyyy-MM-dd");
            if (!keys.contains(key)) continue;
            if (!map.containsKey(key)) {
                map.put(key, report);
            }
        }
        return map;
    }

    private void showWeeksChartDetail() {
        List<DayBucket> buckets = buildRecentDayBuckets(7);
        int total = 0;
        int daysWith = 0;
        int max = 0;
        String maxLabel = "无";
        for (DayBucket b : buckets) {
            int minutes = calcHomeworkMinutes(b.start, b.end);
            total += minutes;
            if (minutes > 0) daysWith++;
            if (minutes > max) {
                max = minutes;
                maxLabel = b.label;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("近7天作业专注：").append(total).append(" 分钟\n");
        sb.append("日均作业专注：").append(total / 7).append(" 分钟\n");
        sb.append("有作业专注的天数：").append(daysWith).append("/7\n");
        sb.append("最长单日：").append(maxLabel).append("（").append(max).append(" 分钟）");

        showStatsDetailDialog("作业专注趋势", sb.toString());
    }

    private void showProjectChartDetail() {
        List<StudyProjectRecord> records = lnm2file.getStudyProjectRecords();
        String unit = projectUnitInMinutes ? "分钟" : "秒";
        if (records == null || records.isEmpty()) {
            showStatsDetailDialog("学科分布（" + unit + "）", "暂无学科统计");
            return;
        }
        java.util.LinkedHashMap<String, Long> totals = new java.util.LinkedHashMap<>();
        String deletedName = lnm2file.getDeletedStudyProjectName();
        long totalMs = 0;
        for (StudyProjectRecord record : records) {
            if (record == null) continue;
            String name = record.getProject();
            if (name == null || name.trim().isEmpty()) name = "未设置";
            if (deletedName.equals(name)) {
                continue;
            }
            long duration = Math.max(0, record.getDurationMs());
            totalMs += duration;
            Long prev = totals.get(name);
            totals.put(name, (prev == null ? 0L : prev) + duration);
        }
        if (totals.isEmpty() || totalMs <= 0) {
            showStatsDetailDialog("学科分布（" + unit + "）", "暂无学科统计");
            return;
        }
        List<Map.Entry<String, Long>> list = new ArrayList<>(totals.entrySet());
        list.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        StringBuilder sb = new StringBuilder();
        sb.append("统计单位：").append(unit).append("\n");
        sb.append("总时长：").append(formatProjectValue(totalMs)).append("\n");
        sb.append("项目数量：").append(list.size()).append("\n");
        sb.append("Top 5：\n");
        int limit = Math.min(5, list.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Long> e = list.get(i);
            sb.append(i + 1).append(". ").append(e.getKey()).append("  ")
                    .append(formatProjectValue(e.getValue()));
            if (i < limit - 1) sb.append("\n");
        }

        showStatsDetailDialog("学科分布（" + unit + "）", sb.toString());
    }

    private void showRecordTrendDetail() {
        List<DayBucket> buckets = buildRecentDayBuckets(7);
        Map<String, SleepReportStore.SleepReport> map = buildSleepReportMap(buckets);
        int total = 0;
        int daysWith = 0;
        int max = 0;
        String maxLabel = "无";
        for (DayBucket b : buckets) {
            SleepReportStore.SleepReport report = map.get(b.key);
            int count = report == null ? 0 : Math.max(0, report.attemptCount);
            total += count;
            if (count > 0) daysWith++;
            if (count > max) {
                max = count;
                maxLabel = b.label;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("近7天尝试玩手机：").append(total).append(" 次\n");
        sb.append("有尝试的天数：").append(daysWith).append("/7\n");
        sb.append("最高尝试日：").append(maxLabel).append("（").append(max).append(" 次）");

        showStatsDetailDialog("睡眠尝试玩手机", sb.toString());
    }

    private void showRewardTrendDetail() {
        RewardPrefs.RewardConfig cfg = RewardPrefs.loadConfig();
        List<DayBucket> buckets = buildRecentDayBuckets(7);
        Map<String, SleepReportStore.SleepReport> sleepMap = buildSleepReportMap(buckets);
        Map<String, Integer> usageMap = buildRewardUsageMap();

        int earnedTotal = 0;
        int usedTotal = 0;
        int maxEarn = 0;
        String maxEarnLabel = "无";
        int maxUse = 0;
        String maxUseLabel = "无";

        for (DayBucket b : buckets) {
            int homework = calcHomeworkMinutes(b.start, b.end);
            int blocked = calcBlockedAttempts(b.start, b.end);
            int sleepAttempts = 0;
            SleepReportStore.SleepReport report = sleepMap.get(b.key);
            if (report != null) sleepAttempts = Math.max(0, report.attemptCount);

            int earned = calcRewardMinutes(cfg, homework, blocked, sleepAttempts);
            int used = usageMap.getOrDefault(b.key, 0);

            earnedTotal += earned;
            usedTotal += used;
            if (earned > maxEarn) {
                maxEarn = earned;
                maxEarnLabel = b.label;
            }
            if (used > maxUse) {
                maxUse = used;
                maxUseLabel = b.label;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("近7天奖励获得：").append(earnedTotal).append(" 分钟\n");
        sb.append("近7天兑换使用：").append(usedTotal).append(" 分钟\n");
        sb.append("最高奖励日：").append(maxEarnLabel).append("（").append(maxEarn).append(" 分钟）\n");
        sb.append("最高兑换日：").append(maxUseLabel).append("（").append(maxUse).append(" 分钟）");

        showStatsDetailDialog("奖励/兑换趋势", sb.toString());
    }

    private String formatProjectValue(long ms) {
        if (projectUnitInMinutes) {
            long mins = Math.max(0, ms / 60000);
            return mins + "分钟";
        }
        long secs = Math.max(0, ms / 1000);
        return secs + "秒";
    }

    private void showStatsDetailDialog(String title, String message) {
        if (mContext == null) return;
        new MessageDialog.Builder(mContext)
                .setTitle(title)
                .setMessage(message)
                .setConfirm("知道了")
                .show();
    }

    private void updateImportantBanner() {
        TodoItem important = TodoPrefs.loadImportant();
        if (important == null) {
            binding.includeImportantBanner.lnmImportantBanner.setVisibility(View.GONE);
            return;
        }
        binding.includeImportantBanner.lnmImportantBanner.setVisibility(View.VISIBLE);
        binding.includeImportantBanner.tvImportantTitle.setText(important.getTitle());
        binding.includeImportantBanner.tvImportantDays.setText(TodoTimeUtils.formatImportantDays(important.getDueAt()));
        binding.includeImportantBanner.getRoot().setOnClickListener(v -> showImportantActionDialog(important));
    }

    private void updatePrimaryCharts() {
        updateHomeworkChart();
        updateSleepAttemptChart();
        updateRewardTrendChart();
    }

    private void updateHomeworkChart() {
        if (binding == null) return;
        List<DayBucket> buckets = buildRecentDayBuckets(7);
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int idx = 0;
        for (DayBucket b : buckets) {
            int minutes = calcHomeworkMinutes(b.start, b.end);
            entries.add(new Entry(idx, minutes));
            labels.add(b.label);
            idx++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "作业专注(分钟)");
        dataSet.setColor(ContextCompat.getColor(mContext, R.color.spring_green));
        dataSet.setCircleColor(ContextCompat.getColor(mContext, R.color.spring_green));
        dataSet.setLineWidth(1.6f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false);
        LineData data = new LineData(dataSet);
        binding.chartLnmWeeks.setData(data);
        binding.chartLnmWeeks.getDescription().setEnabled(false);
        binding.chartLnmWeeks.getAxisRight().setEnabled(false);

        XAxis xAxis = binding.chartLnmWeeks.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int i = (int) value;
                if (i >= 0 && i < labels.size()) {
                    return labels.get(i);
                }
                return "";
            }
        });
        weeksLabels = new ArrayList<>(labels);
        binding.chartLnmWeeks.invalidate();
    }

    private void updateSleepAttemptChart() {
        if (binding == null) return;
        List<DayBucket> buckets = buildRecentDayBuckets(7);
        Map<String, SleepReportStore.SleepReport> map = buildSleepReportMap(buckets);
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int idx = 0;
        for (DayBucket b : buckets) {
            SleepReportStore.SleepReport report = map.get(b.key);
            int count = report == null ? 0 : Math.max(0, report.attemptCount);
            entries.add(new Entry(idx, count));
            labels.add(b.label);
            idx++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "尝试玩手机(次)");
        dataSet.setColor(ContextCompat.getColor(mContext, R.color.yellow));
        dataSet.setCircleColor(ContextCompat.getColor(mContext, R.color.yellow));
        dataSet.setLineWidth(1.6f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false);
        LineData data = new LineData(dataSet);
        binding.chartRecordTrend.setData(data);
        binding.chartRecordTrend.getDescription().setEnabled(false);
        binding.chartRecordTrend.getAxisRight().setEnabled(false);

        XAxis xAxis = binding.chartRecordTrend.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int i = (int) value;
                if (i >= 0 && i < labels.size()) {
                    return labels.get(i);
                }
                return "";
            }
        });
        recordLabels = new ArrayList<>(labels);
        binding.chartRecordTrend.invalidate();
    }

    private void updateRewardTrendChart() {
        if (binding == null) return;
        RewardEngine.settleDailyIfNeeded();
        RewardPrefs.RewardConfig cfg = RewardPrefs.loadConfig();

        List<DayBucket> buckets = buildRecentDayBuckets(7);
        Map<String, SleepReportStore.SleepReport> sleepMap = buildSleepReportMap(buckets);
        Map<String, Integer> usageMap = buildRewardUsageMap();

        List<Entry> earnEntries = new ArrayList<>();
        List<Entry> useEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        boolean hasUse = false;
        boolean hasEarn = false;

        int idx = 0;
        for (DayBucket b : buckets) {
            int homework = calcHomeworkMinutes(b.start, b.end);
            int blocked = calcBlockedAttempts(b.start, b.end);
            int sleepAttempts = 0;
            SleepReportStore.SleepReport report = sleepMap.get(b.key);
            if (report != null) sleepAttempts = Math.max(0, report.attemptCount);

            int earned = calcRewardMinutes(cfg, homework, blocked, sleepAttempts);
            int used = usageMap.getOrDefault(b.key, 0);

            earnEntries.add(new Entry(idx, earned));
            useEntries.add(new Entry(idx, used));
            if (earned > 0) hasEarn = true;
            if (used > 0) hasUse = true;
            labels.add(b.label);
            idx++;
        }

        LineData data = new LineData();
        LineDataSet earnSet = new LineDataSet(earnEntries, "奖励获得(分钟)");
        earnSet.setColor(ContextCompat.getColor(mContext, R.color.spring_green));
        earnSet.setCircleColor(ContextCompat.getColor(mContext, R.color.spring_green));
        earnSet.setLineWidth(1.6f);
        earnSet.setCircleRadius(3f);
        earnSet.setDrawValues(false);
        data.addDataSet(earnSet);

        if (hasUse) {
            LineDataSet useSet = new LineDataSet(useEntries, "兑换使用(分钟)");
            useSet.setColor(ContextCompat.getColor(mContext, R.color.colorAccent));
            useSet.setCircleColor(ContextCompat.getColor(mContext, R.color.colorAccent));
            useSet.setLineWidth(1.6f);
            useSet.setCircleRadius(3f);
            useSet.setDrawValues(false);
            data.addDataSet(useSet);
        }
        binding.chartRewardTrend.setData(data);
        binding.chartRewardTrend.getDescription().setEnabled(false);
        binding.chartRewardTrend.getAxisRight().setEnabled(false);

        XAxis xAxis = binding.chartRewardTrend.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int i = (int) value;
                if (i >= 0 && i < labels.size()) {
                    return labels.get(i);
                }
                return "";
            }
        });
        rewardLabels = new ArrayList<>(labels);
        binding.chartRewardTrend.invalidate();
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

    private void updateProjectCharts() {
        if (binding == null) return;
        List<StudyProjectRecord> records = lnm2file.getStudyProjectRecords();
        if (records == null || records.isEmpty()) {
            binding.chartProjectPie.setNoDataText("暂无学科统计");
            binding.chartProjectBar.setNoDataText("暂无学科统计");
            return;
        }
        java.util.LinkedHashMap<String, Long> totals = new java.util.LinkedHashMap<>();
        String deletedName = lnm2file.getDeletedStudyProjectName();
        for (StudyProjectRecord record : records) {
            if (record == null) continue;
            String name = record.getProject();
            if (name == null || name.trim().isEmpty()) name = "未设置";
            if (deletedName.equals(name)) {
                continue;
            }
            long duration = Math.max(0, record.getDurationMs());
            Long prev = totals.get(name);
            totals.put(name, (prev == null ? 0L : prev) + duration);
        }
        if (totals.isEmpty()) {
            binding.chartProjectPie.setNoDataText("暂无学科统计");
            binding.chartProjectBar.setNoDataText("暂无学科统计");
            binding.chartProjectPie.invalidate();
            binding.chartProjectBar.invalidate();
            return;
        }

        List<PieEntry> pieEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<BarEntry> barEntries = new ArrayList<>();
        int index = 0;
        for (String key : totals.keySet()) {
            long ms = totals.get(key);
            float value = projectUnitInMinutes ? (ms / 60000f) : (ms / 1000f);
            if (value <= 0f) {
                value = 0.1f; // ensure very short sessions are still visible
            }
            pieEntries.add(new PieEntry(value, key));
            labels.add(key);
            barEntries.add(new BarEntry(index, value));
            index++;
        }

        PieDataSet pieSet = new PieDataSet(pieEntries, "");
        pieSet.setColors(ColorTemplate.MATERIAL_COLORS);
        pieSet.setValueTextSize(11f);
        PieData pieData = new PieData(pieSet);
        binding.chartProjectPie.setData(pieData);
        binding.chartProjectPie.getDescription().setEnabled(false);
        binding.chartProjectPie.setCenterText(projectUnitInMinutes ? "学科(分钟)" : "学科(秒钟)");
        binding.chartProjectPie.setUsePercentValues(false);
//        binding.chartProjectPie.setExtraOffsets(16f, 16f, 16f, 16f);
        binding.chartProjectPie.setDrawHoleEnabled(true);
        binding.chartProjectPie.setHoleRadius(45f);
        binding.chartProjectPie.setTransparentCircleRadius(50f);
        binding.chartProjectPie.invalidate();

        BarDataSet barSet = new BarDataSet(barEntries, projectUnitInMinutes ? "分钟" : "秒钟");
        barSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barSet.setValueTextSize(10f);
        BarData barData = new BarData(barSet);
        barData.setBarWidth(0.6f);
        binding.chartProjectBar.setData(barData);
        binding.chartProjectBar.getDescription().setEnabled(false);
        binding.chartProjectBar.getAxisRight().setEnabled(false);
        XAxis xAxis = binding.chartProjectBar.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int i = (int) value;
                if (i >= 0 && i < labels.size()) {
                    return labels.get(i);
                }
                return "";
            }
        });
        binding.chartProjectBar.invalidate();
    }

    private void updateRecordTrendChart() {
        if (binding == null) return;
        List<Lnm> list = lnmDBUtils.findByTimeAsc();
        if (list == null || list.isEmpty()) {
            binding.chartRecordTrend.setNoDataText("暂无专注记录");
            return;
        }
        Map<String, Long> daily = new HashMap<>();
        Map<String, Integer> blockedDaily = new HashMap<>();
        for (Lnm l : list) {
            if (l == null || l.createdDate == null) continue;
            long endMs = l.endTime != null ? l.endTime.getTime() : l.schedule.getTime();
            long dur = Math.max(0, endMs - l.createdDate.getTime());
            String key = TimeUtils.date2String(l.createdDate, "M月d日");
            daily.put(key, daily.getOrDefault(key, 0L) + dur);
            blockedDaily.put(key, blockedDaily.getOrDefault(key, 0) + lnm2file.getScreenOnCount(l.id));
        }

        List<Entry> entries = new ArrayList<>();
        List<Entry> blockedEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_YEAR, -29);
        for (int i = 0; i < 30; i++) {
            String key = TimeUtils.date2String(cal.getTime(), "M月d日");
            long ms = daily.getOrDefault(key, 0L);
            float mins = ms / 60000f;
            entries.add(new Entry(i, mins));
            int blocked = blockedDaily.getOrDefault(key, 0);
            blockedEntries.add(new Entry(i, blocked));
            labels.add(key);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        LineDataSet dataSet = new LineDataSet(entries, "近30天(分钟)");
        dataSet.setColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        dataSet.setLineWidth(1.6f);
        dataSet.setCircleRadius(2.5f);
        dataSet.setDrawValues(false);
        LineDataSet blockedSet = new LineDataSet(blockedEntries, "未允许应用(次)");
        blockedSet.setColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        blockedSet.setLineWidth(1.6f);
        blockedSet.setCircleRadius(2.5f);
        blockedSet.setDrawValues(false);
        LineData data = new LineData(dataSet, blockedSet);
        binding.chartRecordTrend.setData(data);
        binding.chartRecordTrend.getDescription().setEnabled(false);
        binding.chartRecordTrend.getAxisRight().setEnabled(false);
        XAxis xAxis = binding.chartRecordTrend.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int i = (int) value;
                if (i >= 0 && i < labels.size()) {
                    return labels.get(i);
                }
                return "";
            }
        });
        recordLabels = new ArrayList<>(labels);
        binding.chartRecordTrend.invalidate();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTodoImportantChanged(TodoImportantChanged event) {
        updateImportantBanner();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupProjectUnitToggle();
        updateStatsHeader();
        updatePrimaryCharts();
        updateProjectCharts();
        updateSleepReportHint();
    }

    private void showChart() {
        updatePrimaryCharts();
        updateProjectCharts();
    }



    private void showSeedDataDialog() {
        new AlertDialog.Builder(mContext)
                .setTitle("生成三周测试数据")
                .setMessage("将写入近三周的专注记录、学科统计、睡眠报告与奖励使用数据，是否继续？")
                .setNegativeButton("取消", null)
                .setPositiveButton("生成", (d, w) -> {
                    seedThreeWeeks();
                    getMyAll();
                    updateProjectCharts();
                    updateSleepReportHint();
                    toastSe("已生成三周测试数据");
                })
                .show();
    }

    private void seedThreeWeeks() {
        Calendar now = Calendar.getInstance();
        Calendar start = (Calendar) now.clone();
        start.add(Calendar.DAY_OF_YEAR, -21);
        start.set(Calendar.HOUR_OF_DAY, 7);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        lnm2file.clearStudyProjectRecords();
        List<String> projects = lnm2file.getStudyProjects();
        if (projects.isEmpty()) {
            projects.add("英语");
            projects.add("数学");
            projects.add("专业课");
            projects.add("阅读");
            lnm2file.saveStudyProjects(projects);
        }

        java.util.Random random = new java.util.Random();
        List<Lnm> list = new ArrayList<>();
        int baseId = (int) (System.currentTimeMillis() / 1000);
        int idx = 0;

        for (int d = 0; d < 21; d++) {
            Calendar day = (Calendar) start.clone();
            day.add(Calendar.DAY_OF_YEAR, d);
            int dow = day.get(Calendar.DAY_OF_WEEK);
            boolean weekend = (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY);

            int dayTotalMin = 90 + random.nextInt(210); // 90-300 min
            int sessions = weekend ? (1 + random.nextInt(2)) : (2 + random.nextInt(2));
            sessions = Math.min(sessions, Math.max(1, dayTotalMin / 30));

            List<Integer> durations = new ArrayList<>();
            int remaining = dayTotalMin;
            for (int s = 0; s < sessions; s++) {
                int leftSessions = sessions - s;
                int minForThis = 30;
                int maxForThis = Math.min(120, remaining - (leftSessions - 1) * 30);
                if (maxForThis < minForThis) {
                    maxForThis = minForThis;
                }
                int dur = (leftSessions == 1) ? Math.max(30, remaining) : (minForThis + random.nextInt(maxForThis - minForThis + 1));
                durations.add(dur);
                remaining -= dur;
            }

            int startHour = weekend ? 9 : 17;
            int endHour = weekend ? 21 : 22;
            int hourWindow = Math.max(1, endHour - startHour);

            for (int s = 0; s < durations.size(); s++) {
                int durMin = durations.get(s);
                Calendar begin = (Calendar) day.clone();
                begin.set(Calendar.HOUR_OF_DAY, startHour + random.nextInt(hourWindow));
                begin.set(Calendar.MINUTE, random.nextInt(60));
                Calendar plan = (Calendar) begin.clone();
                plan.add(Calendar.MINUTE, durMin);

                boolean success = random.nextInt(100) < (weekend ? 75 : 80);
                Calendar end = (Calendar) plan.clone();
                if (success) {
                    end.add(Calendar.MINUTE, Math.min(2, random.nextInt(3)));
                } else {
                    end.add(Calendar.MINUTE, -1 * (2 + random.nextInt(8)));
                }

                long actualMin = Math.max(30, (end.getTimeInMillis() - begin.getTimeInMillis()) / 60000L);
                end.setTimeInMillis(begin.getTimeInMillis() + actualMin * 60000L);

                Lnm lnm = new Lnm();
                lnm.id = baseId + idx++;
                lnm.createdDate = begin.getTime();
                lnm.schedule = plan.getTime();
                lnm.endTime = end.getTime();
                lnm.finish = success;
                list.add(lnm);

                String project = projects.get(random.nextInt(projects.size()));
                long durMs = Math.max(0, end.getTimeInMillis() - begin.getTimeInMillis());
                lnm2file.addStudyProjectRecord(new StudyProjectRecord(
                        project,
                        begin.getTimeInMillis(),
                        end.getTimeInMillis(),
                        durMs,
                        success
                ));
            }
        }
        lnmDBUtils.insert(list);
        seedTodoAndScreenOn(list);
        seedSleepReports(now);
        seedRewardUsage(now);
    }

    private void seedTodoAndScreenOn(List<Lnm> list) {
        if (list != null && !list.isEmpty()) {
            java.util.Random random = new java.util.Random();
            for (Lnm l : list) {
                int base = l.finish ? 0 : 2;
                int count = base + random.nextInt(5);
                lnm2file.saveScreenOnCount(l.id, count);
            }
        }

        List<TodoGroup> groups = new ArrayList<>();
        TodoGroup g1 = new TodoGroup("g_study", "作业");
        TodoGroup g2 = new TodoGroup("g_life", "生活");
        TodoGroup g3 = new TodoGroup("g_other", "其他");

        long now = System.currentTimeMillis();
        g1.getItems().add(buildSeedTodo("t1", "背单词 30 分钟", "作业", now + TimeConstants.HOUR * 6, false, null));
        g1.getItems().add(buildSeedTodo("t2", "刷题 20 道", "作业", now + TimeConstants.DAY * 1, false, null));
        g1.getItems().add(buildSeedTodo("t3", "整理错题", "作业", now + TimeConstants.DAY * 2, true, "天"));

        g2.getItems().add(buildSeedTodo("t4", "喝水打卡", "生活", now + TimeConstants.HOUR * 2, true, "天"));
        g2.getItems().add(buildSeedTodo("t5", "整理书桌", "生活", now + TimeConstants.DAY * 3, false, null));

        g3.getItems().add(buildSeedTodo("t6", "阅读 30 分钟", "其他", now + TimeConstants.DAY * 4, false, null));

        groups.add(g1);
        groups.add(g2);
        groups.add(g3);
        TodoPrefs.saveGroups(groups);

        TodoItem important = buildSeedTodo("t_imp", "期末倒计时", "作业", now + TimeConstants.DAY * 15, false, null);
        important.setImportant(true);
        TodoPrefs.saveImportant(important);
    }

    private void seedSleepReports(Calendar now) {
        List<SleepReportStore.SleepReport> history = new ArrayList<>();
        Calendar day = (Calendar) now.clone();
        day.add(Calendar.DAY_OF_YEAR, -21);
        day.set(Calendar.HOUR_OF_DAY, 0);
        day.set(Calendar.MINUTE, 0);
        day.set(Calendar.SECOND, 0);
        day.set(Calendar.MILLISECOND, 0);

        java.util.Random random = new java.util.Random();
        for (int d = 0; d < 21; d++) {
            Calendar startSleep = (Calendar) day.clone();
            startSleep.add(Calendar.DAY_OF_YEAR, d);
            startSleep.set(Calendar.HOUR_OF_DAY, 22);
            startSleep.set(Calendar.MINUTE, 0);

            Calendar endSleep = (Calendar) startSleep.clone();
            endSleep.add(Calendar.DAY_OF_YEAR, 1);
            endSleep.set(Calendar.HOUR_OF_DAY, 6);
            endSleep.set(Calendar.MINUTE, 30);

            SleepReportStore.SleepReport report = new SleepReportStore.SleepReport();
            report.startAt = startSleep.getTimeInMillis();
            report.endAt = endSleep.getTimeInMillis();
            report.attemptTimes = new ArrayList<>();

            int dow = startSleep.get(Calendar.DAY_OF_WEEK);
            boolean weekend = (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY);
            int attempts = weekend ? random.nextInt(3) : random.nextInt(2);
            report.attemptCount = attempts;
            for (int i = 0; i < attempts; i++) {
                Calendar attempt = (Calendar) startSleep.clone();
                int minuteOffset = 30 + random.nextInt(7 * 60); // between 22:30 and ~05:30
                attempt.add(Calendar.MINUTE, minuteOffset);
                report.attemptTimes.add(attempt.getTimeInMillis());
            }
            history.add(0, report);
        }
        SleepReportStore.overwriteHistory(history);
        SleepReportStore.clearCurrent();
    }

    private void seedRewardUsage(Calendar now) {
        RewardPrefs.RewardConfig cfg = RewardPrefs.loadConfig();
        int maxDaily = Math.max(0, cfg.dailyMaxMinutes);
        List<RewardPrefs.RewardUsage> list = new ArrayList<>();
        Calendar day = (Calendar) now.clone();
        day.add(Calendar.DAY_OF_YEAR, -21);
        day.set(Calendar.HOUR_OF_DAY, 0);
        day.set(Calendar.MINUTE, 0);
        day.set(Calendar.SECOND, 0);
        day.set(Calendar.MILLISECOND, 0);

        java.util.Random random = new java.util.Random();
        for (int d = 0; d < 21; d++) {
            int dow = day.get(Calendar.DAY_OF_WEEK);
            boolean weekend = (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY);
            int cap = weekend ? maxDaily : Math.max(0, maxDaily / 2);
            int used = cap == 0 ? 0 : random.nextInt(cap + 1);

            RewardPrefs.RewardUsage usage = new RewardPrefs.RewardUsage();
            usage.date = TimeUtils.date2String(day.getTime(), "yyyy-MM-dd");
            usage.usedMinutes = used;
            list.add(0, usage);

            day.add(Calendar.DAY_OF_YEAR, 1);
        }
        RewardPrefs.saveUsage(list);
    }

    private TodoItem buildSeedTodo(String id, String title, String category, long dueAt, boolean repeat, String unit) {
        TodoItem item = new TodoItem(id, title);
        item.setCategory(category);
        item.setDueAt(dueAt);
        item.setRepeat(repeat);
        item.setRepeatUnit(unit);
        item.setCreatedAt(System.currentTimeMillis());
        item.setCompleted(false);
        item.setPinned(false);
        return item;
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

    private String formatDate(Calendar cal) {
        return String.format(Locale.CHINA, "%d-%02d-%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH));
    }

    private void showWeeklyReportDialog(String weekKey, String report, Calendar start, Calendar end) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_weekly_report, null);
        TextView title = view.findViewById(R.id.tv_weekly_title);
        TextView range = view.findViewById(R.id.tv_weekly_range);
        TextView total = view.findViewById(R.id.tv_weekly_total);
        TextView count = view.findViewById(R.id.tv_weekly_count);
        TextView rate = view.findViewById(R.id.tv_weekly_rate);
        TextView compare = view.findViewById(R.id.tv_weekly_compare);
        TextView blocked = view.findViewById(R.id.tv_weekly_blocked);
        TextView avg = view.findViewById(R.id.tv_weekly_avg);
        TextView deep = view.findViewById(R.id.tv_weekly_deep);
        TextView streak = view.findViewById(R.id.tv_weekly_streak);
        TextView peak = view.findViewById(R.id.tv_weekly_peak);
        TextView low = view.findViewById(R.id.tv_weekly_low);
        TextView project = view.findViewById(R.id.tv_weekly_project);
        TextView body = view.findViewById(R.id.tv_weekly_report);
        RadarChart radar = view.findViewById(R.id.chart_weekly_radar);

        title.setText("使用周报（" + formatDate(start) + " ~ " + formatDate(end) + "）");
        range.setText(formatDate(start) + " ~ " + formatDate(end));

        WeeklyMetrics metrics = buildWeeklyMetrics(start, end);
        total.setText("总时长 " + formatDuration(metrics.totalMs));
        count.setText("次数 " + metrics.count);
        rate.setText("完成率 " + String.format(Locale.CHINA, "%.0f", metrics.finishRate) + "%");
        compare.setText("较前两周：时长" + metrics.diffTime + "，次数" + metrics.diffCount + "，完成率" + metrics.diffRate);
        blocked.setText("未允许应用 " + metrics.blockedCount + " 次");
        avg.setText("日均 " + formatDuration(metrics.avgPerDay) + " · 单次均值 " + formatDuration(metrics.avgPerSession));
        deep.setText("深度专注块 ≥45分钟：" + metrics.deepCount + "次（" + String.format(Locale.CHINA, "%.0f", metrics.deepShare) + "%）");
        streak.setText("最长连续专注：" + metrics.streak + "天");
        peak.setText("高峰日：" + metrics.peakDay);
        low.setText("低谷日：" + metrics.lowDay);
        project.setText("学科偏向：" + metrics.projectSummary);
        body.setText(report);
        setupWeeklyRadar(radar, start, end);

        new AlertDialog.Builder(mContext)
                .setView(view)
                .setPositiveButton("知道了", (d, w) -> SPUtils.getInstance().put(WEEKLY_REPORT_KEY, weekKey))
                .show();
    }

    private void updateSleepReportHint() {
        if (binding == null) return;
        List<SleepReportStore.SleepReport> history = SleepReportStore.loadHistory();
        if (history.isEmpty()) {
            binding.tvSleepReportHint.setText("暂无睡眠报告");
            return;
        }
        int abnormal = 0;
        int count = Math.min(7, history.size());
        for (int i = 0; i < count; i++) {
            SleepReportStore.SleepReport r = history.get(i);
            if (r != null && r.attemptCount > 0) abnormal++;
        }
        SleepReportStore.SleepReport latest = history.get(0);
        String time = TimeUtils.date2String(new Date(latest.startAt), "MM-dd HH:mm");
        binding.tvSleepReportHint.setText("最近一次：" + time + " · 尝试 " + latest.attemptCount + " 次 · 近7天异常 " + abnormal + " 次");
    }

    private void showSleepReportHistory() {
        List<SleepReportStore.SleepReport> history = SleepReportStore.loadHistory();
        if (history.isEmpty()) {
            toastSe("暂无睡眠报告");
            return;
        }
        List<String> items = new ArrayList<>();
        for (SleepReportStore.SleepReport report : history) {
            String start = TimeUtils.date2String(new Date(report.startAt), "MM-dd HH:mm");
            String end = report.endAt > 0 ? TimeUtils.date2String(new Date(report.endAt), "HH:mm") : "未结束";
            items.add(start + " - " + end + " · 尝试 " + report.attemptCount + " 次");
        }
        String message = String.join("\n", items);
        new MessageDialog.Builder(mContext)
                .setTitle("历史睡眠报告")
                .setTextGravity(android.view.Gravity.START)
                .setMessage(message)
                .setConfirm("知道了")
                .show();
    }

    private void showSleepReportDetail(SleepReportStore.SleepReport report) {
        if (report == null) return;
        String start = TimeUtils.date2String(new Date(report.startAt), "yyyy-MM-dd HH:mm");
        String end = report.endAt > 0 ? TimeUtils.date2String(new Date(report.endAt), "yyyy-MM-dd HH:mm") : "未结束";
        long durationMin = report.endAt > 0 ? Math.max(0, (report.endAt - report.startAt) / 60000L) : 0;
        String times = SleepReportStore.formatAttemptTimes(report, 12);

        StringBuilder sb = new StringBuilder();
        sb.append("时间范围：").append(start).append(" - ").append(end).append("\n");
        if (report.endAt > 0) {
            sb.append("睡眠时长：").append(durationMin).append(" 分钟\n");
        }
        sb.append("尝试次数：").append(report.attemptCount).append(" 次\n");
        sb.append("尝试时间点：").append(times);

        new MessageDialog.Builder(mContext)
                .setTitle("睡眠报告详情")
                .setMessage(sb.toString())
                .setConfirm("知道了")
                .show();
    }

    private static class WeeklyMetrics {
        long totalMs;
        int count;
        float finishRate;
        String diffTime;
        String diffCount;
        String diffRate;
        long avgPerDay;
        long avgPerSession;
        int deepCount;
        float deepShare;
        int streak;
        String peakDay;
        String lowDay;
        String projectSummary;
        int blockedCount;
    }

    private WeeklyMetrics buildWeeklyMetrics(Calendar start, Calendar end) {
        WeeklyMetrics m = new WeeklyMetrics();
        List<Lnm> list = lnmDBUtils.findBetween(start.getTime(), end.getTime());
        long totalMs = 0;
        int count = list.size();
        int success = 0;
        Map<String, Long> dailyTotals = new HashMap<>();
        long longestMs = 0;
        for (Lnm l : list) {
            if (l == null || l.createdDate == null) continue;
            long endMs = l.endTime != null ? l.endTime.getTime() : l.schedule.getTime();
            long dur = Math.max(0, endMs - l.createdDate.getTime());
            totalMs += dur;
            if (l.finish) success++;
            if (dur > longestMs) longestMs = dur;
            String dayKey = TimeUtils.date2String(l.createdDate, "M月d日");
            dailyTotals.put(dayKey, dailyTotals.getOrDefault(dayKey, 0L) + dur);
            m.blockedCount += lnm2file.getScreenOnCount(l.id);
        }
        long avgPrevMs = calcPrevTwoWeeksAvgMs(start);
        PrevStats prev = calcPrevTwoWeeksStats(start);
        long diffMs = totalMs - avgPrevMs;

        m.totalMs = totalMs;
        m.count = count;
        m.finishRate = count == 0 ? 0f : (success * 100f / count);
        m.diffTime = formatDiff(diffMs);
        m.diffCount = formatCountDiff(count, prev.countAvg);
        m.diffRate = formatRateDiff(m.finishRate, prev.finishRate);
        m.avgPerDay = totalMs / 7;
        m.avgPerSession = count == 0 ? 0 : totalMs / count;
        m.deepCount = countDeepSessions(list);
        m.deepShare = count == 0 ? 0f : (m.deepCount * 100f / count);
        m.streak = calcMaxStreak(dailyTotals);
        m.peakDay = findPeakDay(dailyTotals);
        m.lowDay = findLowDay(dailyTotals);
        m.projectSummary = buildProjectSummary(start, end);
        return m;
    }

    private void setupWeeklyRadar(RadarChart radar, Calendar start, Calendar end) {
        float[] scores = buildWeeklyScores(start, end);
        List<RadarEntry> entries = new ArrayList<>();
        for (float s : scores) entries.add(new RadarEntry(s));
        RadarDataSet set = new RadarDataSet(entries, "维度");
        set.setColor(ColorTemplate.MATERIAL_COLORS[0]);
        set.setFillColor(ColorTemplate.MATERIAL_COLORS[0]);
        set.setDrawFilled(true);
        set.setFillAlpha(120);
        set.setLineWidth(2f);
        set.setValueTextSize(10f);

        RadarData data = new RadarData(set);
        radar.setData(data);
        radar.getDescription().setEnabled(false);
        radar.getLegend().setEnabled(false);
        radar.getXAxis().setValueFormatter(new ValueFormatter() {
            private final String[] labels = new String[]{
                    "专注总量", "节奏稳定", "计划完成", "项目聚焦", "坚持频次", "效率倾向", "规则遵守"
            };

            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int idx = (int) value % labels.length;
                return labels[idx];
            }
        });
        radar.getYAxis().setAxisMinimum(0f);
        radar.getYAxis().setAxisMaximum(100f);
        radar.invalidate();
    }

    private float[] buildWeeklyScores(Calendar start, Calendar end) {
        List<Lnm> list = lnmDBUtils.findBetween(start.getTime(), end.getTime());
        Map<String, Long> daily = new HashMap<>();
        int successCount = 0;
        long totalMs = 0;
        for (Lnm lnm : list) {
            if (lnm == null || lnm.createdDate == null) continue;
            long endMs = lnm.endTime != null ? lnm.endTime.getTime() : lnm.schedule.getTime();
            long dur = Math.max(0, endMs - lnm.createdDate.getTime());
            totalMs += dur;
            if (lnm.finish) successCount++;
            Calendar c = Calendar.getInstance();
            c.setTime(lnm.createdDate);
            String key = formatDate(c);
            daily.put(key, daily.getOrDefault(key, 0L) + dur);
        }
        float finishRate = list.isEmpty() ? 0f : (successCount * 100f / list.size());

        float stability = calcStability(daily);
        float focus = calcProjectFocus(start, end);
        float consistency = calcConsistency(daily, list.size());
        float efficiency = calcEfficiency(list);

        float totalScore = clampScore(calcVolumeScore(totalMs, start, end));
        float planScore = clampScore(finishRate);

        float compliance = clampScore(calcComplianceScore(list));

        return new float[]{totalScore, stability, planScore, focus, consistency, efficiency, compliance};
    }

    private float calcComplianceScore(List<Lnm> list) {
        if (list.isEmpty()) return 0f;
        int blockedTotal = 0;
        for (Lnm l : list) {
            if (l == null) continue;
            blockedTotal += lnm2file.getScreenOnCount(l.id);
        }
        float perSession = blockedTotal / (float) list.size();
        float score = 100f - perSession * 25f;
        return clampScore(score);
    }

    private float calcVolumeScore(long totalMs, Calendar start, Calendar end) {
        Calendar prev2Start = getWeekStart(start, -2);
        Calendar prev2End = getWeekStart(start, 0);
        List<Lnm> prevList = lnmDBUtils.findBetween(prev2Start.getTime(), prev2End.getTime());
        long prevTotal = 0;
        for (Lnm l : prevList) {
            if (l == null || l.createdDate == null) continue;
            long endMs = l.endTime != null ? l.endTime.getTime() : l.schedule.getTime();
            prevTotal += Math.max(0, endMs - l.createdDate.getTime());
        }
        if (prevList.isEmpty() || prevTotal <= 0) return 60f;
        float avg = prevTotal / 2f;
        float diff = (totalMs - avg) / avg;
        return 50f + diff * 50f;
    }

    private float calcStability(Map<String, Long> daily) {
        if (daily.isEmpty()) return 0f;
        float mean = 0f;
        for (long v : daily.values()) mean += v;
        mean /= daily.size();
        if (mean <= 0f) return 0f;
        float variance = 0f;
        for (long v : daily.values()) {
            float d = (float) v - mean;
            variance += d * d;
        }
        variance /= daily.size();
        float std = (float) Math.sqrt(variance);
        float cv = std / mean;
        float score = 100f - cv * 100f;
        return clampScore(score);
    }

    private float calcProjectFocus(Calendar start, Calendar end) {
        List<StudyProjectRecord> records = lnm2file.getStudyProjectRecords();
        long total = 0;
        Map<String, Long> totals = new HashMap<>();
        long startMs = start.getTimeInMillis();
        long endMs = end.getTimeInMillis();
        for (StudyProjectRecord r : records) {
            if (r == null) continue;
            if (r.getStartAt() < startMs || r.getStartAt() >= endMs) continue;
            long dur = Math.max(0, r.getDurationMs());
            total += dur;
            String name = r.getProject();
            if (name == null || name.trim().isEmpty()) name = "未设置";
            totals.put(name, totals.getOrDefault(name, 0L) + dur);
        }
        if (total <= 0 || totals.isEmpty()) return 0f;
        List<Long> vals = new ArrayList<>(totals.values());
        vals.sort((a, b) -> Long.compare(b, a));
        long top2 = vals.get(0) + (vals.size() > 1 ? vals.get(1) : 0);
        return clampScore(top2 * 100f / total);
    }

    private float calcConsistency(Map<String, Long> daily, int count) {
        int activeDays = daily.size();
        float dayScore = activeDays / 7f * 100f;
        float countScore = Math.min(count, 14) / 14f * 100f;
        return clampScore(dayScore * 0.7f + countScore * 0.3f);
    }

    private float calcEfficiency(List<Lnm> list) {
        if (list.isEmpty()) return 0f;
        int deep = 0;
        for (Lnm l : list) {
            long endMs = l.endTime != null ? l.endTime.getTime() : l.schedule.getTime();
            long dur = Math.max(0, endMs - l.createdDate.getTime());
            if (dur >= 45 * 60 * 1000) deep++;
        }
        return clampScore(deep * 100f / list.size());
    }

    private float clampScore(float v) {
        if (v < 0f) return 0f;
        if (v > 100f) return 100f;
        return v;
    }

        private String buildWeeklyReport(Calendar start, Calendar end) {
        List<Lnm> list = lnmDBUtils.findBetween(start.getTime(), end.getTime());
        long totalMs = 0;
        int count = list.size();
        int success = 0;
        Map<String, Long> dailyTotals = new HashMap<>();
        Map<String, Integer> dailyCounts = new HashMap<>();
        long longestMs = 0;
        int blockedTotal = 0;
        for (Lnm l : list) {
            if (l == null || l.createdDate == null) continue;
            long endMs = l.endTime != null ? l.endTime.getTime() : l.schedule.getTime();
            long dur = Math.max(0, endMs - l.createdDate.getTime());
            totalMs += dur;
            if (l.finish) success++;
            if (dur > longestMs) longestMs = dur;
            blockedTotal += lnm2file.getScreenOnCount(l.id);

            String dayKey = TimeUtils.date2String(l.createdDate, "M月d日");
            dailyTotals.put(dayKey, dailyTotals.getOrDefault(dayKey, 0L) + dur);
            dailyCounts.put(dayKey, dailyCounts.getOrDefault(dayKey, 0) + 1);
        }
        long avgPrevMs = calcPrevTwoWeeksAvgMs(start);
        long diffMs = totalMs - avgPrevMs;
        String diffStr = diffMs == 0 ? "持平" : (diffMs > 0 ? "↑" : "↓") + formatDuration(Math.abs(diffMs));

        float finishRate = count == 0 ? 0f : (success * 100f / count);
        String range = formatDate(start) + " ~ " + formatDate(end);
        int activeDays = dailyTotals.size();
        long avgPerDay = totalMs / 7;
        long avgPerSession = count == 0 ? 0 : totalMs / count;
        String peakDay = findPeakDay(dailyTotals);
        String lowDay = findLowDay(dailyTotals);
        int deepCount = countDeepSessions(list);
        float deepShare = count == 0 ? 0f : (deepCount * 100f / count);
        PrevStats prev = calcPrevTwoWeeksStats(start);
        String rateDiff = formatRateDiff(finishRate, prev.finishRate);
        String countDiff = formatCountDiff(count, prev.countAvg);
        int streak = calcMaxStreak(dailyTotals);

        StringBuilder sb = new StringBuilder();
        sb.append("使用周报（").append(range).append(")\n");
        sb.append("总时长：").append(formatDuration(totalMs)).append("（较前两周均值 ").append(diffStr).append("）\n");
        sb.append("总次数：").append(count).append(" 次（较前两周 ").append(countDiff).append("）\n");
        sb.append("完成率：").append(String.format(Locale.CHINA, "%.0f", finishRate)).append("%（较前两周 ").append(rateDiff).append("）\n");
        sb.append("活跃天数：").append(activeDays).append("/7，日均 ").append(formatDuration(avgPerDay)).append("，单次均值 ").append(formatDuration(avgPerSession)).append("\n");
        sb.append("未允许应用尝试：").append(blockedTotal).append(" 次\n");
        sb.append("最长单次：").append(formatDuration(longestMs)).append("，深度块 ≥45min：").append(deepCount).append(" 次（")
                .append(String.format(Locale.CHINA, "%.0f", deepShare)).append("%）\n\n");

        sb.append("节奏分布：高峰日 ").append(peakDay).append("，低谷日 ").append(lowDay).append("，最长连续专注 ")
                .append(streak).append(" 天\n");
        sb.append("学科偏向：").append(buildProjectSummary(start, end)).append("\n\n");

        sb.append("结论：").append(diffMs >= 0 ? "本周投入上升，强度更稳。" : "本周投入下降，强度波动偏大。").append("\n");
        sb.append("建议：").append(deepCount >= 2 ? "保持每周≥2次深度专注块，优化低谷日安排。" : "补足深度块（每周≥2次），固定两个高质量专注时段。");
        return sb.toString();
    }

    private static class PrevStats {
        long totalAvg;
        int countAvg;
        float finishRate;
    }

    private long calcPrevTwoWeeksAvgMs(Calendar start) {
        Calendar prev2Start = getWeekStart(start, -2);
        Calendar prev2End = getWeekStart(start, 0);
        List<Lnm> prevList = lnmDBUtils.findBetween(prev2Start.getTime(), prev2End.getTime());
        long total = 0;
        for (Lnm l : prevList) {
            if (l == null || l.createdDate == null) continue;
            long endMs = l.endTime != null ? l.endTime.getTime() : l.schedule.getTime();
            total += Math.max(0, endMs - l.createdDate.getTime());
        }
        return prevList.isEmpty() ? 0 : total / 2;
    }

    private PrevStats calcPrevTwoWeeksStats(Calendar start) {
        Calendar prev2Start = getWeekStart(start, -2);
        Calendar prev2End = getWeekStart(start, 0);
        List<Lnm> prevList = lnmDBUtils.findBetween(prev2Start.getTime(), prev2End.getTime());
        PrevStats stats = new PrevStats();
        if (prevList == null || prevList.isEmpty()) return stats;
        long total = 0;
        int count = 0;
        int success = 0;
        for (Lnm l : prevList) {
            if (l == null || l.createdDate == null) continue;
            long endMs = l.endTime != null ? l.endTime.getTime() : l.schedule.getTime();
            total += Math.max(0, endMs - l.createdDate.getTime());
            count++;
            if (l.finish) success++;
        }
        stats.totalAvg = total / 2;
        stats.countAvg = count / 2;
        stats.finishRate = count == 0 ? 0f : (success * 100f / count);
        return stats;
    }

    private String formatRateDiff(float current, float prev) {
        float diff = current - prev;
        if (Math.abs(diff) < 0.1f) return "持平";
        return (diff > 0 ? "↑" : "↓") + String.format(Locale.CHINA, "%.0f", Math.abs(diff)) + "%";
    }

    private String formatCountDiff(int current, int prevAvg) {
        int diff = current - prevAvg;
        if (diff == 0) return "持平";
        return (diff > 0 ? "↑" : "↓") + Math.abs(diff) + " 次";
    }

    private String findPeakDay(Map<String, Long> daily) {
        if (daily.isEmpty()) return "无";
        String day = "无";
        long max = -1;
        for (Map.Entry<String, Long> e : daily.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                day = e.getKey() + "（" + formatDuration(e.getValue()) + "）";
            }
        }
        return day;
    }

    private String findLowDay(Map<String, Long> daily) {
        if (daily.isEmpty()) return "无";
        String day = "无";
        long min = Long.MAX_VALUE;
        for (Map.Entry<String, Long> e : daily.entrySet()) {
            if (e.getValue() < min) {
                min = e.getValue();
                day = e.getKey() + "（" + formatDuration(e.getValue()) + "）";
            }
        }
        return day;
    }

    private int countDeepSessions(List<Lnm> list) {
        int deep = 0;
        for (Lnm l : list) {
            if (l == null || l.createdDate == null) continue;
            long endMs = l.endTime != null ? l.endTime.getTime() : l.schedule.getTime();
            long dur = Math.max(0, endMs - l.createdDate.getTime());
            if (dur >= 45 * 60 * 1000) deep++;
        }
        return deep;
    }

    private int calcMaxStreak(Map<String, Long> daily) {
        if (daily.isEmpty()) return 0;
        List<String> keys = new ArrayList<>(daily.keySet());
        keys.sort(String::compareTo);
        int max = 1;
        int cur = 1;
        Calendar prev = null;
        for (String k : keys) {
            Calendar c = Calendar.getInstance();
            try {
                String[] parts = k.split("\\.");
                if (parts.length == 2) {
                    int m = Integer.parseInt(parts[0]) - 1;
                    int d = Integer.parseInt(parts[1]);
                    c.set(Calendar.MONTH, m);
                    c.set(Calendar.DAY_OF_MONTH, d);
                }
            } catch (Exception e) {
                continue;
            }
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            if (prev != null) {
                long delta = c.getTimeInMillis() - prev.getTimeInMillis();
                if (delta <= 24L * 60 * 60 * 1000 + 1000L && delta >= 23L * 60 * 60 * 1000) {
                    cur++;
                } else {
                    cur = 1;
                }
                if (cur > max) max = cur;
            }
            prev = c;
        }
        return max;
    }

    private String buildProjectSummary(Calendar start, Calendar end) {
        List<StudyProjectRecord> records = lnm2file.getStudyProjectRecords();
        long total = 0;
        Map<String, Long> totals = new HashMap<>();
        long startMs = start.getTimeInMillis();
        long endMs = end.getTimeInMillis();
        for (StudyProjectRecord r : records) {
            if (r == null) continue;
            if (r.getStartAt() < startMs || r.getStartAt() >= endMs) continue;
            long dur = Math.max(0, r.getDurationMs());
            total += dur;
            String name = r.getProject();
            if (name == null || name.trim().isEmpty()) name = "未设置";
            totals.put(name, totals.getOrDefault(name, 0L) + dur);
        }
        if (totals.isEmpty() || total <= 0) return "暂无项目记录";
        List<Map.Entry<String, Long>> list = new ArrayList<>(totals.entrySet());
        list.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        Map.Entry<String, Long> top = list.get(0);
        float share = top.getValue() * 100f / total;
        return top.getKey() + " 占比 " + String.format(Locale.CHINA, "%.0f", share) + "%";
    }

    private String formatDuration(long ms) {
        long minutes = ms / 60000;
        long hours = minutes / 60;
        long mins = minutes % 60;
        if (hours > 0) {
            return hours + "小时" + mins + "分钟";
        }
        return mins + "分钟";
    }

    private LineData getLcData() {

        //*****最近三周对比

        int week = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        if (week == 0) week = 7;

        List<String> xTimeWeek = new ArrayList<>();
        String[] weekName = {"本周", "上周", "上上周"};
        Integer[] weekColor = {UsrMsgUtils.getThemeColor(), R.color.spring_green, R.color.yellow};
        LineData lineData_week = new LineData();

        for (int i = 0; i < 3; i++) {
            List<Float> yTimeWeekF = new ArrayList<>();
            List<String> yTimeWeek = new ArrayList<>();
            List<Entry> entrieWeek = new ArrayList<>();

            for (int j = 0; j < 7; j++) {

                Calendar calendar_start = Calendar.getInstance();
                calendar_start.setTimeInMillis(System.currentTimeMillis() - (i * 7 - 2 - j + week) * 24 * 60 * 60 * 1000);
                calendar_start.set(Calendar.HOUR_OF_DAY, 0);
                calendar_start.set(Calendar.MINUTE, 0);
                calendar_start.set(Calendar.SECOND, 0);

                if (i == 0) xTimeWeek.add(TimeUtils.getChineseWeek(calendar_start.getTime()));

                Calendar calendar_last = Calendar.getInstance();
                calendar_last.setTimeInMillis(System.currentTimeMillis() - (i * 7 - 3 - j + week) * 24 * 60 * 60 * 1000);
                calendar_last.set(Calendar.HOUR_OF_DAY, 0);
                calendar_last.set(Calendar.MINUTE, 0);
                calendar_last.set(Calendar.SECOND, 0);

                List<Lnm> learnNoMobileList = findBetween(calendar_start.getTime(), calendar_last.getTime());

                //没有继续，有停止
                long stopDay = 0;
                for (Lnm learnNoMobile : learnNoMobileList) {
                    stopDay = TimeUtils.getTimeSpan(learnNoMobile.endTime, learnNoMobile.createdDate, TimeConstants.MIN) + stopDay;
                }

                yTimeWeekF.add((float) stopDay);
                if (stopDay == 0) {
                    yTimeWeek.add("懒");
                } else {
                    yTimeWeek.add(stopDay + "");
                }

                entrieWeek.add(new Entry((float) j, (float) stopDay));

            }

            LineDataSet lineDataSet_week = new LineDataSet(entrieWeek, weekName[i]); // add entries to dataset
            lineDataSet_week.setColor(ContextCompat.getColor(mContext, weekColor[i]));
            lineDataSet_week.setValueTextSize(10f);
            if (i == 0) lineDataSet_week.setLineWidth(1.5f);
            lineDataSet_week.setValueTextColor(ContextCompat.getColor(mContext, weekColor[i])); // styling, ...
            lineDataSet_week.setDrawValues(true);
            lineDataSet_week.setMode(LineDataSet.Mode.CUBIC_BEZIER);

            lineDataSet_week.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    if (yTimeWeekF.contains(value) && yTimeWeek.size() > yTimeWeekF.indexOf(value)) {
//                        Log.d(finalI +"==yTimeWeekF==","==="+value);
                        return yTimeWeek.get(yTimeWeekF.indexOf(value));
                    } else {
                        return super.getFormattedValue(value);
                    }
                }
            });

            lineData_week.addDataSet(lineDataSet_week);
        }


        ValueFormatter formatter_week = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                try {
                    return xTimeWeek.get((int) value);
                } catch (Exception e) {
                    e.printStackTrace();
                    return String.valueOf(value);
                }
            }
        };
        binding.chartLnmWeeks.getXAxis().setValueFormatter(formatter_week);
        weeksLabels = new ArrayList<>(xTimeWeek);


        return lineData_week;

    }


    private void getMyAll() {
        binding.SwipeRefreshLayoutLnmTg.finishRefresh();
        long count = lnmDBUtils.count();
        if (count <= 0) {
            toastEL("本地还没有专注记录……");
            return;
        }
        updateStatsHeader();
        showChart();
        toastSe("已按本地记录刷新统计");

    }



    private void showImportantActionDialog(TodoItem important) {
        if (important == null) return;
        String[] items = new String[]{"编辑", "删除"};
        new SelectDialog.Builder(mContext)
                .setTitle("重要目标")
                .setList(items)
                .setSingleSelect()
                .setListener((dialog, data) -> {
                    if (data == null || data.isEmpty()) return;
                    Object selectedKey = data.keySet().iterator().next();
                    int which = selectedKey instanceof Integer ? (Integer) selectedKey : Integer.parseInt(String.valueOf(selectedKey));
                    if (which == 0) {
                        showEditImportantDialog(important);
                    } else {
                        TodoPrefs.saveImportant(null);
                        EventBus.getDefault().post(new TodoImportantChanged(null));
                        updateImportantBanner();
                    }
                })
                .show();
    }

    private void showEditImportantDialog(TodoItem item) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_add_todo, null);
        EditText etTitle = view.findViewById(R.id.et_todo_title);
        View categorySection = view.findViewById(R.id.rg_category_mode);
        TextView tvCategoryLabel = view.findViewById(R.id.tv_category_label);
        Spinner spCategory = view.findViewById(R.id.sp_category);
        EditText etCategoryNew = view.findViewById(R.id.et_category_new);
        RadioButton rbRepeatYes = view.findViewById(R.id.rb_repeat_yes);
        RadioButton rbRepeatNo = view.findViewById(R.id.rb_repeat_no);
        View rgRepeatUnit = view.findViewById(R.id.rg_repeat_unit);
        RadioButton rbRepeatDay = view.findViewById(R.id.rb_repeat_day);
        RadioButton rbRepeatMonth = view.findViewById(R.id.rb_repeat_month);
        RadioButton rbRepeatYear = view.findViewById(R.id.rb_repeat_year);
        RadioButton rbNonRepeatCountdown = view.findViewById(R.id.rb_non_repeat_countdown);
        RadioButton rbNonRepeatDate = view.findViewById(R.id.rb_non_repeat_date);
        View layoutCountdown = view.findViewById(R.id.layout_countdown);
        View layoutDate = view.findViewById(R.id.layout_date);
        TextView tvPickDate = view.findViewById(R.id.tv_pick_date);
        TextView tvPickTime = view.findViewById(R.id.tv_pick_time);
        android.widget.Switch swImportant = view.findViewById(R.id.sw_important);

        swImportant.setChecked(true);
        swImportant.setEnabled(false);
        categorySection.setVisibility(View.GONE);
        tvCategoryLabel.setVisibility(View.GONE);
        spCategory.setVisibility(View.GONE);
        etCategoryNew.setVisibility(View.GONE);

        etTitle.setText(item.getTitle());
        if (item.isRepeat()) {
            rbRepeatYes.setChecked(true);
            rgRepeatUnit.setVisibility(View.VISIBLE);
            rbRepeatNo.setChecked(false);
            String unit = item.getRepeatUnit();
            if ("月".equals(unit)) rbRepeatMonth.setChecked(true);
            else if ("年".equals(unit)) rbRepeatYear.setChecked(true);
            else rbRepeatDay.setChecked(true);
        } else {
            rbRepeatNo.setChecked(true);
            rgRepeatUnit.setVisibility(View.GONE);
        }

        rbRepeatYes.setOnCheckedChangeListener((buttonView, isChecked) -> rgRepeatUnit.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        Calendar selected = Calendar.getInstance();
        selected.setTimeInMillis(item.getDueAt());
        rbNonRepeatDate.setChecked(true);
        rbNonRepeatCountdown.setChecked(false);
        layoutCountdown.setVisibility(View.GONE);
        layoutDate.setVisibility(View.VISIBLE);
        tvPickDate.setText(String.format(Locale.CHINA, "%d-%02d-%02d", selected.get(Calendar.YEAR), selected.get(Calendar.MONTH) + 1, selected.get(Calendar.DAY_OF_MONTH)));
        tvPickTime.setText(String.format(Locale.CHINA, "%02d:%02d", selected.get(Calendar.HOUR_OF_DAY), selected.get(Calendar.MINUTE)));

        tvPickDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(mContext, R.style.DatePickerDialogTheme,
                    (view1, year, month, dayOfMonth) -> {
                        selected.set(Calendar.YEAR, year);
                        selected.set(Calendar.MONTH, month);
                        selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        tvPickDate.setText(String.format(Locale.CHINA, "%d-%02d-%02d", year, month + 1, dayOfMonth));
                    },
                    selected.get(Calendar.YEAR),
                    selected.get(Calendar.MONTH),
                    selected.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });
        tvPickTime.setOnClickListener(v -> {
            TimePickerDialog dialog = new TimePickerDialog(mContext, R.style.TimePickerDialogTheme,
                    (view12, hourOfDay, minute) -> {
                        selected.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selected.set(Calendar.MINUTE, minute);
                        selected.set(Calendar.SECOND, 0);
                        tvPickTime.setText(String.format(Locale.CHINA, "%02d:%02d", hourOfDay, minute));
                    },
                    selected.get(Calendar.HOUR_OF_DAY),
                    selected.get(Calendar.MINUTE),
                    true);
            dialog.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle("编辑重要目标")
                .setView(view)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (TextUtils.isEmpty(title)) {
                SimToast.toastEL("请输入作业名称");
                return;
            }
            boolean repeat = rbRepeatYes.isChecked();
            String repeatUnit = null;
            long dueAt;
            if (repeat) {
                Calendar repeatCal = Calendar.getInstance();
                if (rbRepeatDay.isChecked()) {
                    repeatUnit = "天";
                    repeatCal.add(Calendar.DAY_OF_YEAR, 1);
                } else if (rbRepeatMonth.isChecked()) {
                    repeatUnit = "月";
                    repeatCal.add(Calendar.MONTH, 1);
                } else {
                    repeatUnit = "年";
                    repeatCal.add(Calendar.YEAR, 1);
                }
                dueAt = repeatCal.getTimeInMillis();
            } else {
                dueAt = selected.getTimeInMillis();
                if (dueAt <= System.currentTimeMillis()) {
                    SimToast.toastEL("请选择未来的时间");
                    return;
                }
            }

            item.setTitle(title);
            item.setRepeat(repeat);
            item.setRepeatUnit(repeatUnit);
            item.setDueAt(dueAt);
            TodoPrefs.saveImportant(item);
            EventBus.getDefault().post(new TodoImportantChanged(item));
            updateImportantBanner();
            dialog.dismiss();
        }));

        dialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MeLnmShowChart meLnmShowChart) {
        if (meLnmShowChart.isLearnFinish()) {
            getMyAll();
        }
    }

}
