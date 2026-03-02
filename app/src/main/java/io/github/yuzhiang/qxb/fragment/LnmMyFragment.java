package io.github.yuzhiang.qxb.fragment;

import static io.github.yuzhiang.qxb.view.pickpic.PicUtils.picSel;

import android.view.View;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import io.github.yuzhiang.qxb.MyUtils.ImageLoaderUtils;
import io.github.yuzhiang.qxb.MyUtils.StatusBarUtil;
import io.github.yuzhiang.qxb.MyUtils.UsrMsgUtils;
import io.github.yuzhiang.qxb.R;
import io.github.yuzhiang.qxb.activity.LnmRecordActivity;
import io.github.yuzhiang.qxb.base.LazyFragment;
import io.github.yuzhiang.qxb.databinding.LnmFragmentMyBinding;
import io.github.yuzhiang.qxb.db.room.bean.Lnm;
import io.github.yuzhiang.qxb.db.room.dbUtils.lnmDBUtils;
import io.github.yuzhiang.qxb.model.StudyProjectRecord;
import io.github.yuzhiang.qxb.view.pickpic.ImageCropEngine;
import io.github.yuzhiang.qxb.view.pickpic.ImageFileCompressEngine;
import io.github.yuzhiang.qxb.view.pickpic.PicUtils;
import io.github.yuzhiang.qxb.view.tastytoast.SimToast;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;

public class LnmMyFragment extends LazyFragment {

    private static final String KEY_PW = "localPw";

    private LnmFragmentMyBinding binding;
    private CalendarAdapter calendarAdapter;
    private Calendar displayMonth;

    public static LnmMyFragment newInstance() {
        return new LnmMyFragment();
    }

    @Override
    protected int getContentViewId() {
        return R.layout.lnm_fragment_my;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        binding = LnmFragmentMyBinding.bind(view);
        StatusBarUtil.setPaddingSmart(getContext(), binding.ablMy);
        calendarAdapter = new CalendarAdapter();
        binding.rvMyCalendar.setLayoutManager(new GridLayoutManager(getContext(), 7));
        binding.rvMyCalendar.setAdapter(calendarAdapter);

        displayMonth = Calendar.getInstance();
        displayMonth.set(Calendar.DAY_OF_MONTH, 1);
        displayMonth.set(Calendar.HOUR_OF_DAY, 0);
        displayMonth.set(Calendar.MINUTE, 0);
        displayMonth.set(Calendar.SECOND, 0);
        displayMonth.set(Calendar.MILLISECOND, 0);

        loadProfile();
        updateStats();
        updateCalendar();
        updateVow();
        updateNick();

        binding.tvMySettings.setOnClickListener(v -> showSettingsDialog());
        binding.ivMyAvatar.setOnClickListener(v -> changeAvatar());
        if (binding.tvMyAvatarEdit != null) {
            binding.tvMyAvatarEdit.setOnClickListener(v -> changeAvatar());
        }
        if (binding.tvMyVow != null) {
            binding.tvMyVow.setOnClickListener(v -> showVowDialog());
        }
        binding.tvMyHistory.setOnClickListener(v -> showHistoryPopup());
        binding.tvMyWeekly.setOnClickListener(v -> showWeeklyPopup());
        if (binding.tvMyCalendarPrev != null) {
            binding.tvMyCalendarPrev.setOnClickListener(v -> shiftMonth(-1));
        }
        if (binding.tvMyCalendarNext != null) {
            binding.tvMyCalendarNext.setOnClickListener(v -> shiftMonth(1));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfile();
        updateStats();
        updateCalendar();
        updateVow();
        updateNick();
    }

    private void loadProfile() {
        String avatar = UsrMsgUtils.getLocalAvatar();
        ImageLoaderUtils.displayRoundHead(binding.ivMyAvatar, avatar, 88);
    }

    private void updateNick() {
        if (binding == null || binding.tvMyNick == null) return;
        binding.tvMyNick.setText(UsrMsgUtils.getNickName());
    }

    private void updateVow() {
        if (binding == null || binding.tvMyVow == null) return;
        String vow = UsrMsgUtils.getSignature();
        if (vow == null || vow.trim().isEmpty()) {
            vow = "写一句誓言吧";
        }
        binding.tvMyVow.setText(vow);
    }

    private void updateStats() {
        List<Lnm> all = lnmDBUtils.findByTimeAsc();
        long totalMs = 0;
        int count = 0;
        for (Lnm l : all) {
            if (l == null || l.createdDate == null) continue;
            long endMs = l.endTime != null ? l.endTime.getTime() : l.schedule.getTime();
            totalMs += Math.max(0, endMs - l.createdDate.getTime());
            count++;
        }

        Calendar cal = Calendar.getInstance();
        Date end = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        Date start = cal.getTime();
        List<Lnm> last7 = lnmDBUtils.findBetween(start, end);
        long weekMs = 0;
        int weekCount = 0;
        for (Lnm l : last7) {
            if (l == null || l.createdDate == null) continue;
            long endMs = l.endTime != null ? l.endTime.getTime() : l.schedule.getTime();
            weekMs += Math.max(0, endMs - l.createdDate.getTime());
            weekCount++;
        }

        binding.tvMyTotalTime.setText("累计时长：" + formatDuration(totalMs));
        binding.tvMyTotalCount.setText("累计次数：" + count + "次");
        binding.tvMyWeekTime.setText("近7天时长：" + formatDuration(weekMs));
        binding.tvMyWeekCount.setText("近7天次数：" + weekCount + "次");
    }

    private void updateCalendar() {
        Calendar now = Calendar.getInstance();
        Calendar month = displayMonth == null ? Calendar.getInstance() : (Calendar) displayMonth.clone();
        month.set(Calendar.DAY_OF_MONTH, 1);
        month.set(Calendar.HOUR_OF_DAY, 0);
        month.set(Calendar.MINUTE, 0);
        month.set(Calendar.SECOND, 0);
        month.set(Calendar.MILLISECOND, 0);

        binding.tvMyCalendarMonth.setText(String.format(Locale.CHINA, "%d年%02d月", month.get(Calendar.YEAR), month.get(Calendar.MONTH) + 1));

        Calendar start = (Calendar) month.clone();

        Calendar end = (Calendar) start.clone();
        end.add(Calendar.MONTH, 1);

        List<Lnm> list = lnmDBUtils.findBetween(start.getTime(), end.getTime());
        Map<String, Long> daily = new HashMap<>();
        long max = 0;
        for (Lnm l : list) {
            if (l == null || l.createdDate == null) continue;
            long endMs = l.endTime != null ? l.endTime.getTime() : l.schedule.getTime();
            long dur = Math.max(0, endMs - l.createdDate.getTime());
            String key = TimeUtils.date2String(l.createdDate, "yyyy-MM-dd");
            long sum = daily.getOrDefault(key, 0L) + dur;
            daily.put(key, sum);
            if (sum > max) max = sum;
        }

        List<DayCell> cells = new ArrayList<>();
        Calendar cursor = (Calendar) start.clone();
        int firstDay = cursor.get(Calendar.DAY_OF_WEEK);
        int offset = (firstDay == Calendar.SUNDAY ? 6 : firstDay - Calendar.MONDAY);
        for (int i = 0; i < offset; i++) {
            cells.add(DayCell.empty());
        }
        int maxDay = start.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int d = 1; d <= maxDay; d++) {
            Calendar c = (Calendar) start.clone();
            c.set(Calendar.DAY_OF_MONTH, d);
            String key = TimeUtils.date2String(c.getTime(), "yyyy-MM-dd");
            long dur = daily.getOrDefault(key, 0L);
            float ratio = max <= 0 ? 0f : (float) dur / (float) max;
            boolean isToday = isSameDay(c, now);
            cells.add(new DayCell(d, ratio, isToday, false));
        }
        while (cells.size() % 7 != 0) {
            cells.add(DayCell.empty());
        }
        calendarAdapter.submit(cells);
    }

    private void shiftMonth(int delta) {
        if (displayMonth == null) {
            displayMonth = Calendar.getInstance();
        }
        displayMonth.add(Calendar.MONTH, delta);
        displayMonth.set(Calendar.DAY_OF_MONTH, 1);
        displayMonth.set(Calendar.HOUR_OF_DAY, 0);
        displayMonth.set(Calendar.MINUTE, 0);
        displayMonth.set(Calendar.SECOND, 0);
        displayMonth.set(Calendar.MILLISECOND, 0);
        updateCalendar();
    }

    private boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
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

    private static class DayCell {
        final int day;
        final float ratio;
        final boolean today;
        final boolean empty;

        DayCell(int day, float ratio, boolean today, boolean empty) {
            this.day = day;
            this.ratio = ratio;
            this.today = today;
            this.empty = empty;
        }

        static DayCell empty() {
            return new DayCell(0, 0f, false, true);
        }
    }

    private class CalendarAdapter extends RecyclerView.Adapter<CalendarHolder> {
        private final List<DayCell> cells = new ArrayList<>();

        void submit(List<DayCell> data) {
            cells.clear();
            if (data != null) cells.addAll(data);
            notifyDataSetChanged();
        }

        @Override
        public CalendarHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
            return new CalendarHolder(view);
        }

        @Override
        public void onBindViewHolder(CalendarHolder holder, int position) {
            DayCell cell = cells.get(position);
            if (cell.empty) {
                holder.day.setText("");
                holder.day.setBackground(null);
                return;
            }
            holder.day.setText(String.valueOf(cell.day));
            int base = ContextCompat.getColor(getContext(), R.color.colorPrimary);
            int alpha = (int) (40 + 180 * cell.ratio);
            int color = Color.argb(alpha, Color.red(base), Color.green(base), Color.blue(base));
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(20f);
            bg.setColor(color);
            if (cell.today) {
                bg.setStroke(2, ContextCompat.getColor(getContext(), R.color.colorPrimary));
                holder.day.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            } else {
                holder.day.setTextColor(cell.ratio > 0.4f ? Color.WHITE : ContextCompat.getColor(getContext(), R.color.colorTextContent));
            }
            holder.day.setBackground(bg);
        }

        @Override
        public int getItemCount() {
            return cells.size();
        }
    }

    private static class CalendarHolder extends RecyclerView.ViewHolder {
        final android.widget.TextView day;
        CalendarHolder(View itemView) {
            super(itemView);
            day = itemView.findViewById(R.id.tv_day);
        }
    }

    private void saveProfile(String nick, String pw) {
        if (nick.isEmpty()) {
            SimToast.toastEL("昵称不能为空");
            return;
        }
        String avatar = UsrMsgUtils.getLocalAvatar();
        UsrMsgUtils.saveLocalProfile(nick, avatar);
        SPUtils.getInstance(UsrMsgUtils.fileName).put(KEY_PW, pw);
        SimToast.toastSe("保存成功");
    }

    private void changeAvatar() {
        int size = 480;
        picSel().setCropEngine(new ImageCropEngine(size, size))
                .setCompressEngine(new ImageFileCompressEngine())
                .forResult(new OnResultCallbackListener<>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        if (result == null || result.isEmpty()) return;
                        String path = PicUtils.getRealLocalPath(result.get(0));
                        if (path == null || path.trim().isEmpty()) return;
                        UsrMsgUtils.saveLocalProfile(UsrMsgUtils.getNickName(), path);
                        ImageLoaderUtils.displayRoundHead(binding.ivMyAvatar, path, 88);
                    }

                    @Override
                    public void onCancel() {
                    }
                });
    }

    private void showSettingsDialog() {
        View view = android.view.LayoutInflater.from(getContext()).inflate(R.layout.dialog_my_settings, null);
        android.widget.EditText etNick = view.findViewById(R.id.et_setting_nick);
        android.widget.EditText etPw = view.findViewById(R.id.et_setting_password);
        etNick.setText(UsrMsgUtils.getNickName());
        etPw.setText(SPUtils.getInstance(UsrMsgUtils.fileName).getString(KEY_PW, ""));

        new AlertDialog.Builder(getContext())
                .setTitle("设置")
                .setView(view)
                .setPositiveButton("保存", (d, w) -> {
                    String nick = etNick.getText().toString().trim();
                    String pw = etPw.getText().toString().trim();
                    saveProfile(nick, pw);
                })
                .setNeutralButton("更换背景", (d, w) -> showBackgroundDialog())
                .setNegativeButton("关闭", null)
                .show();
    }

    private void showVowDialog() {
        View view = android.view.LayoutInflater.from(getContext()).inflate(R.layout.dialog_my_settings, null);
        android.widget.EditText et = view.findViewById(R.id.et_setting_nick);
        android.widget.EditText etPw = view.findViewById(R.id.et_setting_password);
        etPw.setVisibility(View.GONE);
        et.setHint("写一句誓言");
        et.setText(UsrMsgUtils.getLocalSignature());
        new AlertDialog.Builder(getContext())
                .setTitle("我的誓言")
                .setView(view)
                .setPositiveButton("保存", (d, w) -> {
                    String vow = et.getText().toString().trim();
                    UsrMsgUtils.saveLocalSignature(vow);
                    updateVow();
                })
                .setNegativeButton("关闭", null)
                .show();
    }

    private void showBackgroundDialog() {
        String[] items = new String[]{"纯白（默认）", "红色渐变", "暖橙渐变", "柔和蓝"};
        int style = UsrMsgUtils.getPageBgStyle();
        int current = 0;
        if (style == UsrMsgUtils.BG_STYLE_RED) current = 1;
        else if (style == UsrMsgUtils.BG_STYLE_WARM) current = 2;
        else if (style == UsrMsgUtils.BG_STYLE_SOFT) current = 3;
        new AlertDialog.Builder(getContext())
                .setTitle("更换背景")
                .setSingleChoiceItems(items, current, (d, which) -> {
                    int target;
                    if (which == 1) target = UsrMsgUtils.BG_STYLE_RED;
                    else if (which == 2) target = UsrMsgUtils.BG_STYLE_WARM;
                    else if (which == 3) target = UsrMsgUtils.BG_STYLE_SOFT;
                    else target = UsrMsgUtils.BG_STYLE_WHITE;
                    UsrMsgUtils.setPageBgStyle(target);
                    applyBackgroundToAllPages();
                    d.dismiss();
                })
                .setNeutralButton("选择图片", (d, w) -> selectBackgroundImage())
                .setNegativeButton("取消", null)
                .show();
    }

    private void selectBackgroundImage() {
        int size = 1280;
        picSel().setCropEngine(new ImageCropEngine(size, size))
                .setCompressEngine(new ImageFileCompressEngine())
                .forResult(new OnResultCallbackListener<>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        if (result == null || result.isEmpty()) return;
                        String path = PicUtils.getRealLocalPath(result.get(0));
                        if (path == null || path.trim().isEmpty()) return;
                        UsrMsgUtils.setPageBgImage(path);
                        UsrMsgUtils.setPageBgStyle(UsrMsgUtils.BG_STYLE_IMAGE);
                        applyBackgroundToAllPages();
                    }

                    @Override
                    public void onCancel() {
                    }
                });
    }

    private void applyBackgroundToAllPages() {
        if (getActivity() != null) {
            UsrMsgUtils.applyPageBackground(getActivity().findViewById(android.R.id.content));
            if (getActivity() instanceof androidx.fragment.app.FragmentActivity) {
                List<androidx.fragment.app.Fragment> fragments =
                        ((androidx.fragment.app.FragmentActivity) getActivity())
                                .getSupportFragmentManager().getFragments();
                for (androidx.fragment.app.Fragment fragment : fragments) {
                    if (fragment == null) continue;
                    View v = fragment.getView();
                    if (v != null) {
                        UsrMsgUtils.applyPageBackground(v);
                    }
                }
            }
        } else {
            View root = getView();
            if (root != null) {
                UsrMsgUtils.applyPageBackground(root);
            }
        }
    }

    private void showHistoryPopup() {
        List<Lnm> list = lnmDBUtils.findByTimeAsc();
        if (list == null || list.isEmpty()) {
            SimToast.toastEL("暂无历史记录");
            return;
        }
        int size = Math.min(list.size(), 30);
        String[] items = new String[size];
        for (int i = 0; i < size; i++) {
            Lnm l = list.get(list.size() - 1 - i);
            if (l == null || l.createdDate == null) {
                items[i] = "未知记录";
                continue;
            }
            String start = TimeUtils.date2String(l.createdDate, "MM-dd HH:mm");
            String end = TimeUtils.date2String(l.endTime != null ? l.endTime : l.schedule, "HH:mm");
            long endMs = l.endTime != null ? l.endTime.getTime() : l.schedule.getTime();
            long durMin = Math.max(0, (endMs - l.createdDate.getTime()) / 60000);
            int screenOn = io.github.yuzhiang.qxb.model.lnm2file.getScreenOnCount(l.id);
            items[i] = start + " ~ " + end + " · " + durMin + "分钟 · 亮屏" + screenOn + "次";
        }
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("锁机历史（最近30次）")
                .setItems(items, null)
                .setPositiveButton("查看全部", (d, w) -> startActivity(new android.content.Intent(getContext(), LnmRecordActivity.class)))
                .setNegativeButton("关闭", null)
                .show();
    }

    private void showWeeklyPopup() {
        List<Lnm> list = lnmDBUtils.findByTimeAsc();
        if (list == null || list.isEmpty()) {
            SimToast.toastEL("暂无周报");
            return;
        }
        TreeSet<Long> weekStarts = new TreeSet<>();
        for (Lnm item : list) {
            if (item == null || item.createdDate == null) continue;
            Calendar base = Calendar.getInstance();
            base.setTime(item.createdDate);
            Calendar start = getWeekStart(base, 0);
            weekStarts.add(start.getTimeInMillis());
        }
        if (weekStarts.isEmpty()) {
            SimToast.toastEL("暂无周报");
            return;
        }
        List<Long> starts = new ArrayList<>(weekStarts);
        String[] items = new String[starts.size()];
        for (int i = 0; i < starts.size(); i++) {
            Calendar start = Calendar.getInstance();
            start.setTimeInMillis(starts.get(i));
            Calendar end = getWeekStart(start, 1);
            items[i] = formatDate(start) + " ~ " + formatDate(end);
        }
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("历史周报")
                .setItems(items, (d, which) -> {
                    Calendar start = Calendar.getInstance();
                    start.setTimeInMillis(starts.get(which));
                    Calendar end = getWeekStart(start, 1);
                    String report = buildWeeklyReport(start, end);
                    new androidx.appcompat.app.AlertDialog.Builder(getContext())
                            .setTitle("锁机周报（" + formatDate(start) + " ~ " + formatDate(end) + "）")
                            .setMessage(report)
                            .setPositiveButton("确定", null)
                            .show();
                })
                .setNegativeButton("关闭", null)
                .show();
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
        float finishRate = count == 0 ? 0f : (success * 100f / count);
        StringBuilder sb = new StringBuilder();
        sb.append("总时长：").append(formatDuration(totalMs)).append("\n");
        sb.append("总次数：").append(count).append(" 次\n");
        sb.append("完成率：").append(String.format(Locale.CHINA, "%.0f", finishRate)).append("%\n");
        sb.append("项目偏向：").append(buildProjectSummary(start, end)).append("\n");
        sb.append("建议：保持每周至少2次深度学习。");
        return sb.toString();
    }

    private String buildProjectSummary(Calendar start, Calendar end) {
        List<StudyProjectRecord> records = io.github.yuzhiang.qxb.model.lnm2file.getStudyProjectRecords();
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
}
