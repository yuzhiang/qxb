package io.github.yuzhiang.qxb.fragment;

import static io.github.yuzhiang.qxb.MyUtils.UsrMsgUtils.getAccentThemeColor;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import io.github.yuzhiang.qxb.MyUtils.LnmDateUtils;
import io.github.yuzhiang.qxb.MyUtils.UsrMsgUtils;
import io.github.yuzhiang.qxb.R;
import io.github.yuzhiang.qxb.base.LazyFragment;
import io.github.yuzhiang.qxb.databinding.VpLnmRecordFragmentBinding;
import io.github.yuzhiang.qxb.db.room.bean.Lnm;
import io.github.yuzhiang.qxb.db.room.dbUtils.lnmDBUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PlaceholderLnmRecordFragment extends LazyFragment {


    private static final String LnmType = "lnmType";

    private int lnmType = 0;

    private int index = 0;


    private VpLnmRecordFragmentBinding binding;


    private Activity mContext;

    public static PlaceholderLnmRecordFragment newInstance(int index) {
        PlaceholderLnmRecordFragment fragment = new PlaceholderLnmRecordFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(LnmType, index);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    protected int getContentViewId() {
        return R.layout.vp_lnm_record_fragment;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        mContext = getActivity();
        binding = VpLnmRecordFragmentBinding.bind(view);

        int textColor = ContextCompat.getColor(mContext, R.color.colorTextContent);

        XAxis xAxis = binding.lcLnmRecord.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(textColor);

        YAxis yl = binding.lcLnmRecord.getAxisLeft();
        YAxis yr = binding.lcLnmRecord.getAxisRight();
        yl.setTextColor(textColor);
        yr.setTextColor(textColor);
        yl.enableGridDashedLine(10f, 10f, 0f);
        yr.setDrawGridLines(false);
        yl.setAxisMinimum(0f);

        binding.lcLnmRecord.getDescription().setTextColor(textColor);

    }

    @Override
    protected void initData() {
        super.initData();
        if (getArguments() != null) lnmType = getArguments().getInt(LnmType);
        index = 0;

        getData();
    }

    @Override
    protected void initEvent() {
        super.initEvent();

        binding.ivLnmNext.setOnClickListener(v -> {
            index++;
            getData();
        });

        binding.ivLnmLast.setOnClickListener(v -> {
            index--;
            getData();
        });

    }

    private void getData() {
        List<Date> xn = new ArrayList<>();
        List<String> xTime = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        int thisYear = calendar.get(Calendar.YEAR);

        LogUtils.e(index);

        switch (lnmType) {
            case 0:
                //周
                int w = calendar.get(Calendar.DAY_OF_WEEK) - 2;
                if (w == -1) w = 6;

                Calendar c_week = Calendar.getInstance();
                c_week.set(thisYear, calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
                Date today = c_week.getTime();

                for (int d = -w; d <= 7 - w; d++) {

                    Date date = TimeUtils.getDate(today, (d + index * 7), TimeConstants.DAY);

                    xTime.add(TimeUtils.getChineseWeek(date));

                    LogUtils.i(TimeUtils.date2String(date));

                    xn.add(date);
                }
                binding.lcLnmRecord.getDescription().setText("每周学习记录");

                break;

            case 1:
                int thisMonth = calendar.get(Calendar.MONTH) + index;

                Calendar c0 = Calendar.getInstance();
                c0.set(thisYear, thisMonth, 1, 0, 0, 0);

                int maxDay = c0.getActualMaximum(Calendar.DAY_OF_MONTH);

                LogUtils.i(index + "=====" + thisMonth + "=====" + maxDay + "===" + TimeUtils.date2String(c0.getTime()));

                for (int d = 0; d < maxDay; d++) {
                    Calendar c = Calendar.getInstance();
                    c.set(thisYear, thisMonth, d + 1, 0, 0, 0);

                    Date date = c.getTime();

                    xTime.add(TimeUtils.date2String(date, "dd"));

                    LogUtils.i(TimeUtils.date2String(date));

                    xn.add(date);
                }
                Calendar c1 = Calendar.getInstance();
                c1.set(thisYear, thisMonth + 1, 1, 0, 0, 0);
                xn.add(c1.getTime());
                xTime.add(TimeUtils.date2String(c1.getTime(), "dd"));

                binding.lcLnmRecord.getDescription().setText("每月学习记录");

                break;

            case 2:

                for (int m = 0; m < 12; m++) {
                    Calendar c = Calendar.getInstance();
                    c.set(thisYear + index, m, 1, 0, 0, 0);
                    Date date = c.getTime();
                    xTime.add((m + 1) + "月");

                    xn.add(date);
                }
                Calendar c = Calendar.getInstance();

                c.set(thisYear + index + 1, 1, 0, 0, 0, 0);

                Date date = c.getTime();
                LogUtils.i(TimeUtils.date2String(date));
                xTime.add(12 + "月");

                xn.add(date);
                binding.lcLnmRecord.getDescription().setText("每年学习记录");

                break;

            case 3:

                binding.ivLnmLast.setEnabled(false);
                binding.ivLnmNext.setEnabled(false);

                for (int m = 0; m < 7; m++) {
                    Calendar c3 = Calendar.getInstance();
                    c3.set(thisYear - 7 + m + 2, 0, 1, 0, 0, 0);
                    xn.add(c3.getTime());

                    if (m < 7 - 1) xTime.add(String.valueOf(thisYear - 7 + m + 2));
                }

                binding.lcLnmRecord.getDescription().setText("总学习记录");

                break;

            default: //可选
                break;
        }

        showChart(xn, xTime);
    }


    private void showChart(List<Date> dateList, List<String> xTime) {
        xTime.add("");
        xTime.add("");
        xTime.add("");

        if (!dateList.isEmpty()) {
//            LogUtils.i(dateList.toString());
            String s0 = TimeUtils.date2String(dateList.get(0), "yyyy-MM-dd");
            String s1 = TimeUtils.date2String(dateList.get(dateList.size() - 2), "yyyy-MM-dd");
            binding.tvLnmRecordStart.setText(s0);
            binding.tvLnmRecordEnd.setText(s1);

            binding.tvLnmRecordStart0.setText("第" + LnmDateUtils.getThisWeek(dateList.get(0)) + "周");
            binding.tvLnmRecordEnd0.setText("第" + LnmDateUtils.getThisWeek(dateList.get(dateList.size() - 1)) + "周");

        }

        double allTime = 0;

        List<BarEntry> entries = new ArrayList<>();
        List<String> yTime = new ArrayList<>();
        List<Float> yTimeF = new ArrayList<>();

        for (int i = 0; i < dateList.size() - 1; i++) {

            float time = 0;
            List<Lnm> lnmList = lnmDBUtils.findBetween(dateList.get(i), dateList.get(i + 1));

            LogUtils.i(TimeUtils.date2String(dateList.get(i)) + "===" + TimeUtils.date2String(dateList.get(i + 1)));


            for (Lnm lnm : lnmList) {
                time = TimeUtils.getTimeSpan(lnm.endTime, lnm.createdDate, TimeConstants.MIN) + time;
            }

            yTimeF.add(time);
            if (time == 0) {
                yTime.add("懒");
            } else {
                yTime.add(new BigDecimal(time / 60).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + "h");

            }


            allTime = allTime + time;

            entries.add(new BarEntry((float) i, time));

        }

//注意，2月29天，但是因为太多，所以显示的是1，6，11，16，21，26，31
        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {

                try {//加上 xAxisDay.setAxisMaximum(xTime.size()-1);，其实就好了，不用try
                    return xTime.get((int) value);
//                    LogUtils.i(value + "====" + xTime.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    return String.valueOf(value);
                }
            }
        };

        XAxis xAxisDay = binding.lcLnmRecord.getXAxis();
//        xAxisDay.setAxisMaximum(xTime.size()-1);
//        xAxisDay.setAxisMinimum(-1);
        xAxisDay.setValueFormatter(formatter);

        BarData lineDataDay = new BarData();

        BarDataSet lineDataSetDay = new BarDataSet(entries, "每日学习时间"); // add entries to dataset
        lineDataSetDay.setColor(ContextCompat.getColor(mContext, UsrMsgUtils.getThemeColor()));
        lineDataSetDay.setValueTextSize(10f);
        lineDataSetDay.setValueTextColor(ContextCompat.getColor(mContext, UsrMsgUtils.getThemeColor())); // styling, ...
        lineDataSetDay.setDrawValues(true);

        lineDataSetDay.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (yTimeF.contains(value) && yTime.size() > yTimeF.indexOf(value)) {
                    return yTime.get(yTimeF.indexOf(value));
                } else {
                    return super.getFormattedValue(value);
                }
            }
        });

        lineDataDay.addDataSet(lineDataSetDay);

        YAxis yAxis = binding.lcLnmRecord.getAxisRight();

        double dayTime = allTime / ((dateList.size() - 1) * 60);

        dayTime = new BigDecimal(dayTime).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        String s = "平均" + dayTime + "h";


        allTime = new BigDecimal(allTime / 60).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();


        String s2 = "共" + allTime + "h";
        binding.tvLnmRecordTime.setText(String.valueOf(allTime));


        LimitLine ll2 = new LimitLine((float) dayTime, s + "，" + s2);
        ll2.setLabel(s);
        ll2.setTextColor(ContextCompat.getColor(mContext, getAccentThemeColor()));
        ll2.setLineWidth(0.6f);
        ll2.setEnabled(true);
        ll2.setLineColor(ContextCompat.getColor(mContext, getAccentThemeColor()));
        ll2.enableDashedLine(10f, 10f, 0f);//三个参数，第一个线宽长度，第二个线段之间宽度，第三个一般为0，是个补偿
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);//标签位置
        ll2.setTextSize(8f);
        yAxis.removeAllLimitLines();
        yAxis.addLimitLine(ll2);


        binding.lcLnmRecord.setData(lineDataDay);
        binding.lcLnmRecord.invalidate(); // refresh
        binding.lcLnmRecord.animateY(800);


    }


}
