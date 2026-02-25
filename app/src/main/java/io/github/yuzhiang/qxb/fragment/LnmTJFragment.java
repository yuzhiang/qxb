package io.github.yuzhiang.qxb.fragment;

import static io.github.yuzhiang.qxb.common.LzuUrl.lnmMsg;
import static io.github.yuzhiang.qxb.MyUtils.LnmRetrofitUtils.schedulersTransformer;
import static io.github.yuzhiang.qxb.db.room.dbUtils.lnmDBUtils.findBetween;
import static io.github.yuzhiang.qxb.view.tastytoast.SimToast.toastEL;
import static io.github.yuzhiang.qxb.view.tastytoast.SimToast.toastSL;
import static io.github.yuzhiang.qxb.view.tastytoast.SimToast.toastSe;
import static autodispose2.AutoDispose.autoDisposable;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

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
import io.github.yuzhiang.qxb.view.dialog.MessageDialog;
import io.github.yuzhiang.qxb.view.tastytoast.SimToast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

        binding.lnmTitleQa.setOnClickListener(v -> showMsg(false));

        binding.SwipeRefreshLayoutLnmTg.setOnRefreshListener(refreshLayout -> getMyAll());

        binding.tvLnmTjMore.setOnClickListener(v -> {
            startActivity(new Intent(mContext, LnmRecordActivity.class));

        });


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
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MeLnmShowChart meLnmShowChart) {
        if (meLnmShowChart.isLearnFinish()) {
            getMyAll();
        }
    }

}
