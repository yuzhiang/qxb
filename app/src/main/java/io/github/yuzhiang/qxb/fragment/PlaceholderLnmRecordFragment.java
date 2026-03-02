package io.github.yuzhiang.qxb.fragment;

import static io.github.yuzhiang.qxb.MyUtils.UsrMsgUtils.getAccentThemeColor;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import io.github.yuzhiang.qxb.model.lnm2file;

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
    private RecordAdapter recordAdapter;


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
        recordAdapter = new RecordAdapter();
        binding.rvLnmRecordDetail.setLayoutManager(new LinearLayoutManager(mContext));
        binding.rvLnmRecordDetail.setAdapter(recordAdapter);

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

        if (!dateList.isEmpty()) {
            Date start = dateList.get(0);
            Date end = dateList.get(dateList.size() - 1);
            updateRecordList(start, end);
        }

    }

    private void updateRecordList(Date start, Date end) {
        List<Lnm> list = lnmDBUtils.findBetween(start, end);
        if (list == null) list = new ArrayList<>();
        list.sort((a, b) -> {
            long ta = a != null && a.createdDate != null ? a.createdDate.getTime() : 0;
            long tb = b != null && b.createdDate != null ? b.createdDate.getTime() : 0;
            return Long.compare(tb, ta);
        });
        if (list.size() > 50) {
            list = list.subList(0, 50);
        }
        recordAdapter.submit(list);
        binding.tvLnmRecordDetailLabel.setText(list.isEmpty() ? "历史记录（暂无）" : "历史记录（" + list.size() + "）");
    }

    private static class RecordAdapter extends RecyclerView.Adapter<RecordHolder> {
        private final List<Lnm> data = new ArrayList<>();

        void submit(List<Lnm> list) {
            data.clear();
            if (list != null) data.addAll(list);
            notifyDataSetChanged();
        }

        @Override
        public RecordHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lnm_record_detail, parent, false);
            return new RecordHolder(view);
        }

        @Override
        public void onBindViewHolder(RecordHolder holder, int position) {
            Lnm lnm = data.get(position);
            if (lnm == null || lnm.createdDate == null) return;
            String start = TimeUtils.date2String(lnm.createdDate, "yyyy-MM-dd HH:mm");
            String end = TimeUtils.date2String(lnm.endTime != null ? lnm.endTime : lnm.schedule, "HH:mm");
            long endMs = lnm.endTime != null ? lnm.endTime.getTime() : lnm.schedule.getTime();
            long durMin = Math.max(0, TimeUtils.getTimeSpan(endMs, lnm.createdDate.getTime(), TimeConstants.MIN));
            int screenOn = lnm2file.getScreenOnCount(lnm.id);
            String status = lnm.finish ? "成功" : "失败";
            holder.title.setText(start + " ~ " + end);
            holder.sub.setText("时长 " + durMin + "m · " + status + " · 亮屏 " + screenOn + " 次");
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    private static class RecordHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView sub;

        RecordHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_record_title);
            sub = itemView.findViewById(R.id.tv_record_sub);
        }
    }


}
