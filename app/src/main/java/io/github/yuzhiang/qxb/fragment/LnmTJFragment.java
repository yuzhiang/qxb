package io.github.yuzhiang.qxb.fragment;

import static io.github.yuzhiang.qxb.common.LzuUrl.lnmMsg;
import static io.github.yuzhiang.qxb.MyUtils.LnmRetrofitUtils.schedulersTransformer;
import static io.github.yuzhiang.qxb.db.room.dbUtils.lnmDBUtils.findBetween;
import static io.github.yuzhiang.qxb.view.tastytoast.SimToast.toastEL;
import static io.github.yuzhiang.qxb.view.tastytoast.SimToast.toastSL;
import static io.github.yuzhiang.qxb.view.tastytoast.SimToast.toastSe;
import static autodispose2.AutoDispose.autoDisposable;

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
import com.blankj.utilcode.util.ThreadUtils;
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
import com.github.mikephil.charting.data.CombinedData;
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
import io.github.yuzhiang.qxb.view.dialog.MessageDialog;
import io.github.yuzhiang.qxb.view.tastytoast.SimToast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.Collections;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;


public class LnmTJFragment extends LazyFragment {

    public static LnmTJFragment newInstance() {
        return new LnmTJFragment();
    }

    private Context mContext;
    private static final String WEEKLY_REPORT_KEY = "weekly_report_last_shown";
    private static final String WEEKLY_REPORT_ENABLED = "weekly_report_enabled";
    private static final String WEEKLY_REPORT_REPEAT = "weekly_report_allow_repeat";
    private static final String STATS_CARD_ORDER_KEY = "stats_card_order";
    private boolean projectUnitInMinutes = true;
    private List<String> overviewLabels = new ArrayList<>();
    private List<String> weeksLabels = new ArrayList<>();
    private List<String> recordLabels = new ArrayList<>();


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

        int textColor = ContextCompat.getColor(mContext, R.color.colorTextContent);

        XAxis xAxis = binding.chartLnm.getXAxis();
        xAxis.setTextColor(textColor);
        xAxis.setAxisMinimum(0f);

        YAxis ylAxis = binding.chartLnm.getAxisLeft();
        ylAxis.setTextColor(textColor);
        ylAxis.enableGridDashedLine(10f, 10f, 0f);
        YAxis yrAxis = binding.chartLnm.getAxisRight();
        yrAxis.setTextColor(textColor);
        yrAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.TOP);

        binding.chartLnm.getDescription().setText("学习历程（分钟，滑动查看）");
        binding.chartLnm.getDescription().setTextColor(textColor);
        binding.chartLnm.setNoDataText("快去学习吧，还没有数据");
        binding.tvLnmCount.setText("学习历程（分钟）");
        binding.tvLnmCount3.setText("最近三周对比（分钟）");

        XAxis xAxisWeek = binding.chartLnmWeeks.getXAxis();
        xAxisWeek.setTextColor(textColor);
        xAxisWeek.setPosition(XAxis.XAxisPosition.TOP);

        YAxis ylAxisWeek = binding.chartLnmWeeks.getAxisLeft();
        ylAxisWeek.setTextColor(textColor);
        ylAxisWeek.enableGridDashedLine(10f, 10f, 0f);

        YAxis yrAxisWeek = binding.chartLnmWeeks.getAxisRight();
        yrAxisWeek.setTextColor(textColor);
        yrAxisWeek.setDrawGridLines(false);

        setupProjectUnitToggle();
        setupChartClickDetails();
        updateProjectCharts();
        updateRecordTrendChart();
        binding.chartLnmWeeks.getDescription().setText("最近三周对比（分钟）");
        binding.chartLnmWeeks.getDescription().setTextColor(textColor);
        binding.chartLnmWeeks.setNoDataText("快去学习吧，还没有数据");
        binding.chartLnm.getXAxis().setGranularity(1f); // minimum axis-step (interval) is 1
        binding.chartLnmWeeks.getXAxis().setGranularity(1f); // minimum axis-step (interval) is 1

        setupStatsCardReorder();


    }

    private void setupStatsCardReorder() {
        if (binding == null || binding.layoutStatsCards == null) return;
        applyStatsCardOrder();
        setCardLongPress(binding.cardStatSummary);
        setCardLongPress(binding.cardStatOverview);
        setCardLongPress(binding.cardStatWeeks);
        setCardLongPress(binding.cardStatProject);
        setCardLongPress(binding.cardStatRecord);
    }

    private void setCardLongPress(View card) {
        if (card == null) return;
        card.setOnLongClickListener(v -> {
            showCardOrderDialog(v);
            return true;
        });
    }

    private void showCardOrderDialog(View card) {
        if (binding == null || binding.layoutStatsCards == null || card == null) return;
        String[] items = new String[]{"上移", "下移", "置顶", "置底"};
        new AlertDialog.Builder(mContext)
                .setTitle("调整顺序")
                .setItems(items, (d, which) -> {
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
        if (binding == null || binding.layoutStatsCards == null) return;
        int index = binding.layoutStatsCards.indexOfChild(card);
        if (index < 0) return;
        int newIndex = Math.max(0, Math.min(binding.layoutStatsCards.getChildCount() - 1, index + delta));
        if (newIndex == index) return;
        moveCardTo(card, newIndex);
    }

    private void moveCardTo(View card, int index) {
        if (binding == null || binding.layoutStatsCards == null) return;
        int count = binding.layoutStatsCards.getChildCount();
        if (index < 0 || index >= count) return;
        binding.layoutStatsCards.removeView(card);
        binding.layoutStatsCards.addView(card, index);
    }

    private void saveStatsCardOrder() {
        if (binding == null || binding.layoutStatsCards == null) return;
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
        if (binding == null || binding.layoutStatsCards == null) return;
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


        if (!SPUtils.getInstance("enterNum").getBoolean("lnmTj", false)) showMsg(true);

    }

    @Override
    protected void initEvent() {
        super.initEvent();

        binding.lnmTitleQa.setOnClickListener(v -> showWeeklyReportSettings());
        binding.lnmTitleQa.setOnLongClickListener(v -> {
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

    private void showMsg(boolean auto) {
        String msg = "    请仔细看轻学伴模式使用说明，并给予相关权限，否则会出现诸多问题！";

        new MessageDialog.Builder(mContext)
                .setTitle("权限说明")
                .setMessage(msg)
                .setConfirm("查看")
                .setCancel("就不看")
                .setCancelable(false)
                .setListener(new MessageDialog.OnListener() {
                    @Override
                    public void onConfirm(BaseDialog dialog) {
                        if (auto) {
                            toastSL("随时可以在统计界面右上角问号查看 ～");
                            SPUtils.getInstance("enterNum").put("lnmTj", true);
                        }
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(lnmMsg)));
                    }

                    @Override
                    public void onCancel(BaseDialog dialog) {
                        if (auto) {
                            toastSL("随时可以在统计界面右上角问号查看 ～");
                        }

                    }
                }).show();
    }


    /**
     * onDestroyView中进行解绑操作
     */


    private void setupProjectUnitToggle() {
        if (binding == null || binding.tvProjectUnitMin == null || binding.tvProjectUnitSec == null) return;
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
        if (binding == null || binding.tvProjectUnitMin == null || binding.tvProjectUnitSec == null) return;
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
        if (binding.tvProjectTitle != null) {
            binding.tvProjectTitle.setText("学习项目分布（" + unit + "）");
        }
        if (binding.tvProjectBarTitle != null) {
            binding.tvProjectBarTitle.setText("学习项目时长（" + unit + "）");
        }
    }

    private void setupChartClickDetails() {
        if (binding == null) return;
        View.OnClickListener overviewListener = v -> showOverviewChartDetail();
        View.OnClickListener weeksListener = v -> showWeeksChartDetail();
        View.OnClickListener projectListener = v -> showProjectChartDetail();
        View.OnClickListener recordListener = v -> showRecordTrendDetail();

        // Chart clicks show point detail; avoid duplicate dialogs from chart click + point selection.
        if (binding.tvLnmCount != null) binding.tvLnmCount.setOnClickListener(overviewListener);

        // Chart clicks show point detail; avoid duplicate dialogs from chart click + point selection.
        if (binding.tvLnmCount3 != null) binding.tvLnmCount3.setOnClickListener(weeksListener);

        if (binding.chartProjectPie != null) binding.chartProjectPie.setOnClickListener(projectListener);
        if (binding.chartProjectBar != null) binding.chartProjectBar.setOnClickListener(projectListener);
        if (binding.tvProjectTitle != null) binding.tvProjectTitle.setOnClickListener(projectListener);
        if (binding.tvProjectBarTitle != null) binding.tvProjectBarTitle.setOnClickListener(projectListener);

        // Chart clicks show point detail; avoid duplicate dialogs from chart click + point selection.
        if (binding.tvRecordTitle != null) binding.tvRecordTitle.setOnClickListener(recordListener);

        setupChartValueListeners();
    }

    private void setupChartValueListeners() {
        if (binding == null) return;
        if (binding.chartLnm != null) {
            binding.chartLnm.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    String label = getLabelByIndex(overviewLabels, e.getX());
                    String value = formatMinutesValue(e.getY());
                    showPointDetailDialog("学习历程", label, value, "");
                }

                @Override
                public void onNothingSelected() {
                }
            });
        }
        if (binding.chartLnmWeeks != null) {
            binding.chartLnmWeeks.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    int dataSetIndex = h != null ? h.getDataSetIndex() : 0;
                    String series = (dataSetIndex >= 0 && binding.chartLnmWeeks.getData() != null)
                            ? binding.chartLnmWeeks.getData().getDataSetByIndex(dataSetIndex).getLabel()
                            : "";
                    String label = buildWeekDateLabel(dataSetIndex, Math.round(e.getX()), series);
                    String value = formatMinutesValue(e.getY());
                    showPointDetailDialog("最近三周对比", label, value, series.isEmpty() ? "" : ("周别：" + series));
                }

                @Override
                public void onNothingSelected() {
                }
            });
        }
        if (binding.chartRecordTrend != null) {
            binding.chartRecordTrend.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    String label = getLabelByIndex(recordLabels, e.getX());
                    String value = formatMinutesValue(e.getY());
                    showPointDetailDialog("学习记录趋势", label, value, "");
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
        return mins + "分钟";
    }

    private void showPointDetailDialog(String title, String dateLabel, String value, String extra) {
        StringBuilder sb = new StringBuilder();
        sb.append("日期：").append(dateLabel).append("\n");
        sb.append("时长：").append(value);
        if (extra != null && !extra.trim().isEmpty()) {
            sb.append("\n").append(extra);
        }
        showStatsDetailDialog(title, sb.toString());
    }


    private void updateStatsHeader() {
        Calendar now = Calendar.getInstance();

        Range day = buildDayRange(now);
        Range week = buildWeekRange(now, 0);
        Range month = buildMonthRange(now, 0);
        Range year = buildYearRange(now, 0);

        Stat dayStat = calcStat(day.start, day.end);
        Stat weekStat = calcStat(week.start, week.end);
        Stat monthStat = calcStat(month.start, month.end);
        Stat yearStat = calcStat(year.start, year.end);

        Stat weekAvg = calcAvgOfPrevious(week, 2, "week");
        Stat monthAvg = calcAvgOfPrevious(month, 2, "month");
        Stat yearAvg = calcAvgOfPrevious(year, 2, "year");

        if (binding.tvStatTodayTime != null) {
            binding.tvStatTodayTime.setText("时长 " + formatDuration(dayStat.totalMs));
        }
        if (binding.tvStatTodayCount != null) {
            binding.tvStatTodayCount.setText("次数 " + dayStat.count);
        }
        if (binding.tvStatWeek != null) {
            binding.tvStatWeek.setText("本周：" + formatDuration(weekStat.totalMs) + " · 平均 " + formatDuration(weekAvg.totalMs) + " · 差 " + formatDiff(weekStat.totalMs - weekAvg.totalMs));
        }
        if (binding.tvStatMonth != null) {
            binding.tvStatMonth.setText("本月：" + formatDuration(monthStat.totalMs) + " · 平均 " + formatDuration(monthAvg.totalMs) + " · 差 " + formatDiff(monthStat.totalMs - monthAvg.totalMs));
        }
        if (binding.tvStatYear != null) {
            binding.tvStatYear.setText("本年：" + formatDuration(yearStat.totalMs) + " · 平均 " + formatDuration(yearAvg.totalMs) + " · 差 " + formatDiff(yearStat.totalMs - yearAvg.totalMs));
        }
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

    private void showOverviewChartDetail() {
        List<Lnm> list = lnmDBUtils.findByTimeAsc();
        if (list == null || list.isEmpty()) {
            showStatsDetailDialog("学习历程（分钟）", "暂无学习记录");
            return;
        }
        long totalMs = 0;
        int count = 0;
        int finished = 0;
        long longest = 0;
        for (Lnm l : list) {
            if (l == null || l.createdDate == null) continue;
            long endMs = l.endTime != null ? l.endTime.getTime() : l.schedule.getTime();
            long dur = Math.max(0, endMs - l.createdDate.getTime());
            totalMs += dur;
            count++;
            if (l.finish) finished++;
            if (dur > longest) longest = dur;
        }
        long avg = count == 0 ? 0 : totalMs / count;
        float rate = count == 0 ? 0f : (finished * 100f / count);

        Range last7 = buildRecentDaysRange(7);
        Range last30 = buildRecentDaysRange(30);
        Stat stat7 = calcStat(last7.start, last7.end);
        Stat stat30 = calcStat(last30.start, last30.end);

        StringBuilder sb = new StringBuilder();
        sb.append("总时长：").append(formatDuration(totalMs)).append("\n");
        sb.append("总次数：").append(count).append("\n");
        sb.append("完成率：").append(String.format(Locale.CHINA, "%.0f", rate)).append("%\n");
        sb.append("单次均值：").append(formatDuration(avg)).append("\n");
        sb.append("最长单次：").append(formatDuration(longest)).append("\n");
        sb.append("近7天：").append(formatDuration(stat7.totalMs)).append("（").append(stat7.count).append("次）\n");
        sb.append("近30天：").append(formatDuration(stat30.totalMs)).append("（").append(stat30.count).append("次）");

        showStatsDetailDialog("学习历程（分钟）", sb.toString());
    }

    private void showWeeksChartDetail() {
        Calendar now = Calendar.getInstance();
        Calendar w0 = getWeekStart(now, 0);
        Calendar w1 = getWeekStart(now, -1);
        Calendar w2 = getWeekStart(now, -2);
        Calendar w3 = getWeekStart(now, -3);

        Stat s0 = calcStat(w0, getWeekStart(now, 1));
        Stat s1 = calcStat(w1, w0);
        Stat s2 = calcStat(w2, w1);
        Stat s3 = calcStat(w3, w2);

        StringBuilder sb = new StringBuilder();
        sb.append("本周（").append(formatDate(w0)).append(" ~ ").append(formatDate(getWeekStart(now, 1))).append("）：")
                .append(formatDuration(s0.totalMs)).append("（").append(s0.count).append("次）\n");
        sb.append("上周（").append(formatDate(w1)).append(" ~ ").append(formatDate(w0)).append("）：")
                .append(formatDuration(s1.totalMs)).append("（").append(s1.count).append("次）\n");
        sb.append("上上周（").append(formatDate(w2)).append(" ~ ").append(formatDate(w1)).append("）：")
                .append(formatDuration(s2.totalMs)).append("（").append(s2.count).append("次）\n");
        sb.append("再上一周（").append(formatDate(w3)).append(" ~ ").append(formatDate(w2)).append("）：")
                .append(formatDuration(s3.totalMs)).append("（").append(s3.count).append("次）");

        showStatsDetailDialog("最近三周对比（分钟）", sb.toString());
    }

    private void showProjectChartDetail() {
        List<StudyProjectRecord> records = lnm2file.getStudyProjectRecords();
        String unit = projectUnitInMinutes ? "分钟" : "秒";
        if (records == null || records.isEmpty()) {
            showStatsDetailDialog("学习项目分布（" + unit + "）", "暂无学习项目统计");
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
            showStatsDetailDialog("学习项目分布（" + unit + "）", "暂无学习项目统计");
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

        showStatsDetailDialog("学习项目分布（" + unit + "）", sb.toString());
    }

    private void showRecordTrendDetail() {
        List<Lnm> list = lnmDBUtils.findByTimeAsc();
        if (list == null || list.isEmpty()) {
            showStatsDetailDialog("学习记录趋势（分钟）", "暂无学习记录");
            return;
        }
        Map<String, Long> daily = new HashMap<>();
        for (Lnm l : list) {
            if (l == null || l.createdDate == null) continue;
            long endMs = l.endTime != null ? l.endTime.getTime() : l.schedule.getTime();
            long dur = Math.max(0, endMs - l.createdDate.getTime());
            String key = TimeUtils.date2String(l.createdDate, "M月d日");
            daily.put(key, daily.getOrDefault(key, 0L) + dur);
        }
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_YEAR, -29);

        long totalMs = 0;
        String maxDay = "";
        long maxMs = 0;
        for (int i = 0; i < 30; i++) {
            String key = TimeUtils.date2String(cal.getTime(), "M月d日");
            long ms = daily.getOrDefault(key, 0L);
            totalMs += ms;
            if (ms > maxMs) {
                maxMs = ms;
                maxDay = key;
            }
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        long avg = totalMs / 30;

        StringBuilder sb = new StringBuilder();
        sb.append("统计单位：分钟\n");
        sb.append("近30天总时长：").append(formatDuration(totalMs)).append("\n");
        sb.append("日均时长：").append(formatDuration(avg)).append("\n");
        if (!TextUtils.isEmpty(maxDay)) {
            sb.append("最高日：").append(maxDay).append("（").append(formatDuration(maxMs)).append("）");
        }

        showStatsDetailDialog("学习记录趋势（分钟）", sb.toString());
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
        new AlertDialog.Builder(mContext)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("知道了", null)
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

    private void updateProjectCharts() {
        if (binding == null || binding.chartProjectPie == null || binding.chartProjectBar == null) return;
        List<StudyProjectRecord> records = lnm2file.getStudyProjectRecords();
        if (records == null || records.isEmpty()) {
            binding.chartProjectPie.setNoDataText("暂无学习项目统计");
            binding.chartProjectBar.setNoDataText("暂无学习项目统计");
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
            binding.chartProjectPie.setNoDataText("暂无学习项目统计");
            binding.chartProjectBar.setNoDataText("暂无学习项目统计");
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
        binding.chartProjectPie.setCenterText(projectUnitInMinutes ? "学习项目(分钟)" : "学习项目(秒钟)");
        binding.chartProjectPie.setUsePercentValues(false);
        binding.chartProjectPie.setExtraOffsets(16f, 16f, 16f, 16f);
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
        if (binding == null || binding.chartRecordTrend == null) return;
        List<Lnm> list = lnmDBUtils.findByTimeAsc();
        if (list == null || list.isEmpty()) {
            binding.chartRecordTrend.setNoDataText("暂无学习记录");
            return;
        }
        Map<String, Long> daily = new HashMap<>();
        for (Lnm l : list) {
            if (l == null || l.createdDate == null) continue;
            long endMs = l.endTime != null ? l.endTime.getTime() : l.schedule.getTime();
            long dur = Math.max(0, endMs - l.createdDate.getTime());
            String key = TimeUtils.date2String(l.createdDate, "M月d日");
            daily.put(key, daily.getOrDefault(key, 0L) + dur);
        }

        List<Entry> entries = new ArrayList<>();
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
            labels.add(key);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        LineDataSet dataSet = new LineDataSet(entries, "近30天(分钟)");
        dataSet.setColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        dataSet.setLineWidth(1.6f);
        dataSet.setCircleRadius(2.5f);
        dataSet.setDrawValues(false);
        LineData data = new LineData(dataSet);
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
        updateProjectCharts();
        updateRecordTrendChart();
        maybeShowWeeklyReport();
    }

    private void showChart() {
//        Long aLong = System.currentTimeMillis();

        Observable.create((ObservableOnSubscribe<CombinedData>) emitter -> {
                    emitter.onNext(setBarData());
                    emitter.onComplete();

                }).compose(schedulersTransformer())
                // 指定 Subscriber 的回调发生在主线程
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(new Observer<>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(CombinedData data) {
                        binding.chartLnm.setData(data);//这个不要放在子线程，会内存泄漏
                        binding.chartLnm.setVisibleXRangeMaximum(12);
                        int num = data.getBarData().getEntryCount();
                        LogUtils.i(num + "=================");
                        if (num > 12) binding.chartLnm.moveViewToX(num - 6);
                        binding.chartLnm.invalidate(); // refresh
//                        LogUtils.i("====" + (System.currentTimeMillis()-aLong));
                    }

                    @Override
                    public void onError(Throwable e) {
                        SimToast.toastEe("错误！");
                    }

                    @Override
                    public void onComplete() {
                    }
                });

        Observable.create((ObservableOnSubscribe<LineData>) emitter -> {
                    emitter.onNext(getLcData());
                    emitter.onComplete();
                }).compose(schedulersTransformer())
                // 指定 Subscriber 的回调发生在主线程
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(new Observer<LineData>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(LineData lineData) {
                        binding.chartLnmWeeks.setData(lineData);
                        binding.chartLnmWeeks.invalidate(); // refresh
//                        LogUtils.i("====" + (System.currentTimeMillis()-aLong));
                    }

                    @Override
                    public void onError(Throwable e) {
                        SimToast.toastEe("错误！");
                    }

                    @Override
                    public void onComplete() {
                    }
                });

        toastSe("不显示计划时间小于30秒的 ~");

    }

    private void maybeShowWeeklyReport() {
        if (!SPUtils.getInstance().getBoolean(WEEKLY_REPORT_ENABLED, true)) return;
        Calendar now = Calendar.getInstance();
        int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek != Calendar.MONDAY) return;

        Calendar lastWeekStart = getWeekStart(now, -1);
        Calendar lastWeekEnd = getWeekStart(now, 0);
        String weekKey = formatDate(lastWeekStart);

        String lastShown = SPUtils.getInstance().getString(WEEKLY_REPORT_KEY, "");
        boolean allowRepeat = SPUtils.getInstance().getBoolean(WEEKLY_REPORT_REPEAT, false);
        if (!allowRepeat && weekKey.equals(lastShown)) return;

        String report = buildWeeklyReport(lastWeekStart, lastWeekEnd);
        showWeeklyReportDialog(weekKey, report, lastWeekStart, lastWeekEnd);
    }

    private void showWeeklyReportSettings() {
        boolean enabled = SPUtils.getInstance().getBoolean(WEEKLY_REPORT_ENABLED, true);
        boolean repeat = SPUtils.getInstance().getBoolean(WEEKLY_REPORT_REPEAT, false);
        String[] items = new String[]{
                "开启周报提醒",
                "允许重复查看"
        };
        boolean[] checks = new boolean[]{enabled, repeat};

        new AlertDialog.Builder(mContext)
                .setTitle("周报设置")
                .setMultiChoiceItems(items, checks, (d, which, isChecked) -> checks[which] = isChecked)
                .setPositiveButton("保存", (d, w) -> {
                    SPUtils.getInstance().put(WEEKLY_REPORT_ENABLED, checks[0]);
                    SPUtils.getInstance().put(WEEKLY_REPORT_REPEAT, checks[1]);
                })
                .setNeutralButton("查看历史周报", (d, w) -> showHistoryWeeklyReports())
                .setNegativeButton("使用说明", (d, w) -> showMsg(false))
                .setCancelable(true)
                .show();
    }

    private void showLastWeekReportManual() {
        Calendar now = Calendar.getInstance();
        Calendar lastWeekStart = getWeekStart(now, -1);
        Calendar lastWeekEnd = getWeekStart(now, 0);
        String weekKey = formatDate(lastWeekStart);
        String report = buildWeeklyReport(lastWeekStart, lastWeekEnd);
        showWeeklyReportDialog(weekKey, report, lastWeekStart, lastWeekEnd);
    }

    private void showHistoryWeeklyReports() {
        List<Lnm> list = lnmDBUtils.findByTimeAsc();
        if (list == null || list.isEmpty()) {
            toastEL("暂无历史记录");
            return;
        }
        TreeSet<Long> weekStarts = new TreeSet<>(Collections.reverseOrder());
        for (Lnm item : list) {
            if (item == null || item.createdDate == null) continue;
            Calendar base = Calendar.getInstance();
            base.setTime(item.createdDate);
            Calendar start = getWeekStart(base, 0);
            weekStarts.add(start.getTimeInMillis());
        }
        if (weekStarts.isEmpty()) {
            toastEL("暂无历史周报");
            return;
        }
        List<Long> starts = new ArrayList<>(weekStarts);
        List<String> labels = new ArrayList<>();
        for (Long startMs : starts) {
            Calendar start = Calendar.getInstance();
            start.setTimeInMillis(startMs);
            Calendar end = getWeekStart(start, 1);
            labels.add(formatDate(start) + " ~ " + formatDate(end));
        }
        String[] items = labels.toArray(new String[0]);
        new AlertDialog.Builder(mContext)
                .setTitle("历史周报")
                .setItems(items, (d, which) -> {
                    if (which < 0 || which >= starts.size()) return;
                    Calendar start = Calendar.getInstance();
                    start.setTimeInMillis(starts.get(which));
                    Calendar end = getWeekStart(start, 1);
                    String weekKey = formatDate(start);
                    String report = buildWeeklyReport(start, end);
                    showWeeklyReportDialog(weekKey, report, start, end);
                })
                .setNegativeButton("关闭", null)
                .show();
    }

    private void showSeedDataDialog() {
        new AlertDialog.Builder(mContext)
                .setTitle("生成三周测试数据")
                .setMessage("将写入近三周的锁机记录与学习项目统计数据，是否继续？")
                .setNegativeButton("取消", null)
                .setPositiveButton("生成", (d, w) -> {
                    seedThreeWeeks();
                    getMyAll();
                    updateProjectCharts();
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
            int sessions = 1 + random.nextInt(3);
            for (int s = 0; s < sessions; s++) {
                Calendar begin = (Calendar) day.clone();
                begin.set(Calendar.HOUR_OF_DAY, 7 + random.nextInt(13));
                begin.set(Calendar.MINUTE, random.nextInt(60));
                long durationMin = 20 + random.nextInt(71);
                Calendar plan = (Calendar) begin.clone();
                plan.add(Calendar.MINUTE, (int) durationMin);

                boolean success = random.nextInt(100) < 70;
                Calendar end = (Calendar) plan.clone();
                if (success) {
                    end.add(Calendar.MINUTE, random.nextInt(3));
                } else {
                    end.add(Calendar.MINUTE, -5 - random.nextInt(20));
                }

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
    }

    private void seedTodoAndScreenOn(List<Lnm> list) {
        if (list != null && !list.isEmpty()) {
            java.util.Random random = new java.util.Random();
            for (Lnm l : list) {
                int count = random.nextInt(5);
                lnm2file.saveScreenOnCount(l.id, count);
            }
        }

        List<TodoGroup> groups = new ArrayList<>();
        TodoGroup g1 = new TodoGroup("g_study", "学习");
        TodoGroup g2 = new TodoGroup("g_life", "生活");
        TodoGroup g3 = new TodoGroup("g_other", "其他");

        long now = System.currentTimeMillis();
        g1.getItems().add(buildSeedTodo("t1", "背单词 30 分钟", "学习", now + TimeConstants.HOUR * 6, false, null));
        g1.getItems().add(buildSeedTodo("t2", "刷题 20 道", "学习", now + TimeConstants.DAY * 1, false, null));
        g1.getItems().add(buildSeedTodo("t3", "整理错题", "学习", now + TimeConstants.DAY * 2, true, "天"));

        g2.getItems().add(buildSeedTodo("t4", "喝水打卡", "生活", now + TimeConstants.HOUR * 2, true, "天"));
        g2.getItems().add(buildSeedTodo("t5", "整理书桌", "生活", now + TimeConstants.DAY * 3, false, null));

        g3.getItems().add(buildSeedTodo("t6", "阅读 30 分钟", "其他", now + TimeConstants.DAY * 4, false, null));

        groups.add(g1);
        groups.add(g2);
        groups.add(g3);
        TodoPrefs.saveGroups(groups);

        TodoItem important = buildSeedTodo("t_imp", "四级倒计时", "学习", now + TimeConstants.DAY * 15, false, null);
        important.setImportant(true);
        TodoPrefs.saveImportant(important);
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
        TextView avg = view.findViewById(R.id.tv_weekly_avg);
        TextView deep = view.findViewById(R.id.tv_weekly_deep);
        TextView streak = view.findViewById(R.id.tv_weekly_streak);
        TextView peak = view.findViewById(R.id.tv_weekly_peak);
        TextView low = view.findViewById(R.id.tv_weekly_low);
        TextView project = view.findViewById(R.id.tv_weekly_project);
        TextView body = view.findViewById(R.id.tv_weekly_report);
        RadarChart radar = view.findViewById(R.id.chart_weekly_radar);

        title.setText("锁机周报（" + formatDate(start) + " ~ " + formatDate(end) + "）");
        range.setText(formatDate(start) + " ~ " + formatDate(end));

        WeeklyMetrics metrics = buildWeeklyMetrics(start, end);
        total.setText("总时长 " + formatDuration(metrics.totalMs));
        count.setText("次数 " + metrics.count);
        rate.setText("完成率 " + String.format(Locale.CHINA, "%.0f", metrics.finishRate) + "%");
        compare.setText("较前两周：时长" + metrics.diffTime + "，次数" + metrics.diffCount + "，完成率" + metrics.diffRate);
        avg.setText("日均 " + formatDuration(metrics.avgPerDay) + " · 单次均值 " + formatDuration(metrics.avgPerSession));
        deep.setText("深度学习块 ≥45分钟：" + metrics.deepCount + "次（" + String.format(Locale.CHINA, "%.0f", metrics.deepShare) + "%）");
        streak.setText("最长连续学习：" + metrics.streak + "天");
        peak.setText("高峰日：" + metrics.peakDay);
        low.setText("低谷日：" + metrics.lowDay);
        project.setText("项目偏向：" + metrics.projectSummary);
        body.setText(report);
        setupWeeklyRadar(radar, start, end);

        new AlertDialog.Builder(mContext)
                .setView(view)
                .setPositiveButton("知道了", (d, w) -> SPUtils.getInstance().put(WEEKLY_REPORT_KEY, weekKey))
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
                    "专注总量", "节奏稳定", "计划完成", "项目聚焦", "坚持频次", "效率倾向"
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

        return new float[]{totalScore, stability, planScore, focus, consistency, efficiency};
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
        for (Lnm l : list) {
            if (l == null || l.createdDate == null) continue;
            long endMs = l.endTime != null ? l.endTime.getTime() : l.schedule.getTime();
            long dur = Math.max(0, endMs - l.createdDate.getTime());
            totalMs += dur;
            if (l.finish) success++;
            if (dur > longestMs) longestMs = dur;

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
        sb.append("锁机周报（").append(range).append(")\n");
        sb.append("总时长：").append(formatDuration(totalMs)).append("（较前两周均值 ").append(diffStr).append("）\n");
        sb.append("总次数：").append(count).append(" 次（较前两周 ").append(countDiff).append("）\n");
        sb.append("完成率：").append(String.format(Locale.CHINA, "%.0f", finishRate)).append("%（较前两周 ").append(rateDiff).append("）\n");
        sb.append("活跃天数：").append(activeDays).append("/7，日均 ").append(formatDuration(avgPerDay)).append("，单次均值 ").append(formatDuration(avgPerSession)).append("\n");
        sb.append("最长单次：").append(formatDuration(longestMs)).append("，深度块 ≥45min：").append(deepCount).append(" 次（")
                .append(String.format(Locale.CHINA, "%.0f", deepShare)).append("%）\n\n");

        sb.append("节奏分布：高峰日 ").append(peakDay).append("，低谷日 ").append(lowDay).append("，最长连续学习 ")
                .append(streak).append(" 天\n");
        sb.append("项目偏向：").append(buildProjectSummary(start, end)).append("\n\n");

        sb.append("结论：").append(diffMs >= 0 ? "本周投入上升，强度更稳。" : "本周投入下降，强度波动偏大。").append("\n");
        sb.append("建议：").append(deepCount >= 2 ? "保持每周≥2次深度学习块，优化低谷日安排。" : "补足深度块（每周≥2次），固定两个高质量学习时段。");
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

private CombinedData setBarData() {

        List<Lnm> learnNoMobiles = lnmDBUtils.findByTimeAsc();
        List<Entry> entries = new ArrayList<>();
        List<BarEntry> barEntriesS = new ArrayList<>();//成功的
        List<BarEntry> barEntries = new ArrayList<>();

        int mm = 0;

        int allNum = learnNoMobiles.size();

        int success = 0;
        long star = 0;
        List<String> xTime = new ArrayList<>();
        xTime.add("0");
        for (Lnm learnNoMobile : learnNoMobiles) {

            if (learnNoMobile.finish) {
                success = success + 1;
                long min = TimeUtils.getTimeSpan(learnNoMobile.endTime, learnNoMobile.createdDate, TimeConstants.MIN);
                star = star + min;
            } else {
                long min = TimeUtils.getTimeSpan(learnNoMobile.schedule, learnNoMobile.endTime, TimeConstants.MIN);
                star = star - min;
            }


            float span2 = (float) (TimeUtils.getTimeSpan(learnNoMobile.endTime, learnNoMobile.createdDate, TimeConstants.SEC) / 60.0);
            float span = (float) Math.round(TimeUtils.getTimeSpan(learnNoMobile.schedule, learnNoMobile.createdDate, TimeConstants.SEC) / 60.0);

            if (span > 60 * 4) continue;

            mm = mm + 1;

            entries.add(new Entry((float) mm, span2));
            if (learnNoMobile.finish) {
                barEntriesS.add(new BarEntry((float) mm, span2));
            } else {
                barEntries.add(new BarEntry((float) mm, span));
            }
            xTime.add(TimeUtils.date2String(learnNoMobile.createdDate, "M月d日"));

        }

        String ss = ((float) success / (float) allNum) * 100.0 + "";
        if (ss.length() > 5) ss = ss.substring(0, 5);

        String finalSs = ss;
        long finalStar = star;
        ThreadUtils.runOnUiThread(() -> {
            binding.tvLnmCount.setText("完成率：" + finalSs + "%    积分：" + finalStar);
        });

        LineData lineData = new LineData();

        LineDataSet lineDataSet = new LineDataSet(entries, "实际"); // add entries to dataset
        lineDataSet.setColor(ContextCompat.getColor(mContext, R.color.yellow));
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setLineWidth(1.5f);
        lineDataSet.setValueTextColor(ContextCompat.getColor(mContext, R.color.yellow)); // styling, ...
        lineDataSet.setDrawValues(true);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        lineData.addDataSet(lineDataSet);

        BarDataSet barDataSetS = new BarDataSet(barEntriesS, "计划（成功）"); // add entries to dataset
        barDataSetS.setColor(ContextCompat.getColor(mContext, R.color.spring_green));
        barDataSetS.setValueTextSize(10f);
        barDataSetS.setValueTextColor(ContextCompat.getColor(mContext, R.color.spring_green)); // styling, ...

        BarDataSet barDataSet = new BarDataSet(barEntries, "计划（失败）"); // add entries to dataset
        barDataSet.setColor(ContextCompat.getColor(mContext, R.color.pink));
        barDataSet.setValueTextSize(10f);
        barDataSet.setValueTextColor(ContextCompat.getColor(mContext, R.color.pink)); // styling, ...

        BarData barData = new BarData(barDataSetS, barDataSet);

        CombinedData data = new CombinedData();
        data.setData(lineData);
        data.setData(barData);


        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                try {
                    return xTime.get((int) value);
                } catch (Exception e) {
                    e.printStackTrace();
                    return String.valueOf(value);
                }
            }
        };


        binding.chartLnm.getXAxis().setValueFormatter(formatter);
        overviewLabels = new ArrayList<>(xTime);


        return data;


    }

    //
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
            toastEL("本地还没有学习记录……");
            return;
        }
        updateStatsHeader();
        showChart();
        updateProjectCharts();
        updateRecordTrendChart();
        toastSe("已按本地记录刷新统计");

    }



    private void showImportantActionDialog(TodoItem important) {
        if (important == null) return;
        String[] items = new String[]{"编辑", "删除"};
        new AlertDialog.Builder(mContext)
                .setTitle("重要待办")
                .setItems(items, (d, which) -> {
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
            DatePickerDialog dialog = new DatePickerDialog(mContext,
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
            TimePickerDialog dialog = new TimePickerDialog(mContext,
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
                .setTitle("编辑重要待办")
                .setView(view)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (TextUtils.isEmpty(title)) {
                SimToast.toastEL("请输入待办名称");
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
