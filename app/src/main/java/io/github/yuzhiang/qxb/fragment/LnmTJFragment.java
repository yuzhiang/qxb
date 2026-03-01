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
    private boolean projectUnitInMinutes = true;


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

        binding.chartLnm.getDescription().setText("学习历程（滑动查看）");
        binding.chartLnm.getDescription().setTextColor(textColor);
        binding.chartLnm.setNoDataText("快去学习吧，还没有数据");
        binding.tvLnmCount3.setText("最近三周对比图");

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
        updateProjectCharts();
        binding.chartLnmWeeks.getDescription().setText("最近三周对比图");
        binding.chartLnmWeeks.getDescription().setTextColor(textColor);
        binding.chartLnmWeeks.setNoDataText("快去学习吧，还没有数据");
        binding.chartLnm.getXAxis().setGranularity(1f); // minimum axis-step (interval) is 1
        binding.chartLnmWeeks.getXAxis().setGranularity(1f); // minimum axis-step (interval) is 1


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
        for (StudyProjectRecord record : records) {
            if (record == null) continue;
            String name = record.getProject();
            if (name == null || name.trim().isEmpty()) name = "未设置";
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
        binding.chartProjectPie.setCenterText(projectUnitInMinutes ? "学习项目(分钟)" : "学习项目(秒)");
        binding.chartProjectPie.setUsePercentValues(false);
        binding.chartProjectPie.setExtraOffsets(16f, 16f, 16f, 16f);
        binding.chartProjectPie.setDrawHoleEnabled(true);
        binding.chartProjectPie.setHoleRadius(45f);
        binding.chartProjectPie.setTransparentCircleRadius(50f);
        binding.chartProjectPie.invalidate();

        BarDataSet barSet = new BarDataSet(barEntries, projectUnitInMinutes ? "分钟" : "秒");
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
                .setNeutralButton("查看上周周报", (d, w) -> showLastWeekReportManual())
                .setNegativeButton("使用说明", (d, w) -> showMsg(false))
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
        TextView body = view.findViewById(R.id.tv_weekly_report);
        RadarChart radar = view.findViewById(R.id.chart_weekly_radar);

        title.setText("上周锁机周报（" + formatDate(start) + " ~ " + formatDate(end) + "）");
        body.setText(report);
        setupWeeklyRadar(radar, start, end);

        new AlertDialog.Builder(mContext)
                .setView(view)
                .setPositiveButton("知道了", (d, w) -> SPUtils.getInstance().put(WEEKLY_REPORT_KEY, weekKey))
                .show();
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
        for (Lnm l : list) {
            if (l == null || l.createdDate == null) continue;
            long endMs = l.endTime != null ? l.endTime.getTime() : l.schedule.getTime();
            totalMs += Math.max(0, endMs - l.createdDate.getTime());
            if (l.finish) success++;
        }
        long avgPrevMs = calcPrevTwoWeeksAvgMs(start);
        long diffMs = totalMs - avgPrevMs;
        String diffStr = diffMs == 0 ? "持平" : (diffMs > 0 ? "↑" : "↓") + formatDuration(Math.abs(diffMs));

        float finishRate = count == 0 ? 0f : (success * 100f / count);
        String range = formatDate(start) + " ~ " + formatDate(end);

        StringBuilder sb = new StringBuilder();
        sb.append("上周锁机周报（").append(range).append(")\n");
        sb.append("总时长：").append(formatDuration(totalMs)).append("（较前两周均值 ").append(diffStr).append(")\n");
        sb.append("总次数：").append(count).append(" 次\n");
        sb.append("完成率：").append(String.format(Locale.CHINA, "%.0f", finishRate)).append("%\n\n");
        sb.append("项目偏向：").append(buildProjectSummary(start, end)).append("\n");
        sb.append("结论：").append(diffMs >= 0 ? "本周投入上升，保持节奏。" : "本周投入下降，建议固定学习时段。").append("\n");
        sb.append("建议：").append("安排 2 次 ≥45 分钟深度学习块。");
        return sb.toString();
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
            return hours + "h" + mins + "m";
        }
        return mins + "m";
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
            xTime.add(TimeUtils.date2String(learnNoMobile.createdDate, "MM.dd"));

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


        return lineData_week;

    }


    private void getMyAll() {
        binding.SwipeRefreshLayoutLnmTg.finishRefresh();
        long count = lnmDBUtils.count();
        if (count <= 0) {
            toastEL("本地还没有学习记录……");
            return;
        }
        showChart();
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
