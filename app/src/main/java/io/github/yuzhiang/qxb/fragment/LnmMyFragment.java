package io.github.yuzhiang.qxb.fragment;

import static io.github.yuzhiang.qxb.view.pickpic.PicUtils.picSel;

import android.view.View;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

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

import io.github.yuzhiang.qxb.MyUtils.ImageLoaderUtils;
import io.github.yuzhiang.qxb.MyUtils.StatusBarUtil;
import io.github.yuzhiang.qxb.MyUtils.UsrMsgUtils;
import io.github.yuzhiang.qxb.R;
import io.github.yuzhiang.qxb.activity.LnmRecordActivity;
import io.github.yuzhiang.qxb.activity.LearnNoMobileActivity;
import io.github.yuzhiang.qxb.activity.StudentEntryActivity;
import io.github.yuzhiang.qxb.activity.SettingsActivity;
import io.github.yuzhiang.qxb.base.LazyFragment;
import io.github.yuzhiang.qxb.databinding.LnmFragmentMyBinding;
import io.github.yuzhiang.qxb.db.room.bean.Lnm;
import io.github.yuzhiang.qxb.db.room.dbUtils.lnmDBUtils;
import io.github.yuzhiang.qxb.model.focus.FocusRulePrefs;
import io.github.yuzhiang.qxb.model.lnm2file;
import io.github.yuzhiang.qxb.model.reward.RewardPrefs;
import io.github.yuzhiang.qxb.view.dialog.InputDialog;
import io.github.yuzhiang.qxb.view.dialog.MessageDialog;
import io.github.yuzhiang.qxb.view.dialog.SelectDialog;
import io.github.yuzhiang.qxb.view.pickpic.ImageCropEngine;
import io.github.yuzhiang.qxb.view.pickpic.ImageFileCompressEngine;
import io.github.yuzhiang.qxb.view.pickpic.PicUtils;
import io.github.yuzhiang.qxb.view.tastytoast.SimToast;
import io.github.yuzhiang.qxb.base.BaseDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import io.github.yuzhiang.qxb.view.dialog.UIDialog;

public class LnmMyFragment extends LazyFragment {

    private static final String ARG_STUDENT_MODE = "arg_student_mode";

    private LnmFragmentMyBinding binding;
    private CalendarAdapter calendarAdapter;
    private Calendar displayMonth;
    private boolean studentMode = false;
    private int parentPwFailCount = 0;
    private long parentPwLockUntil = 0L;

    public static LnmMyFragment newInstance() {
        return newInstance(false);
    }

    public static LnmMyFragment newInstance(boolean studentMode) {
        LnmMyFragment fragment = new LnmMyFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_STUDENT_MODE, studentMode);
        fragment.setArguments(args);
        return fragment;
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
        Bundle args = getArguments();
        studentMode = args != null && args.getBoolean(ARG_STUDENT_MODE, false);
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
        updateModeEntry();
        applyStudentModeUi();

        binding.tvMySettings.setOnClickListener(v -> openSettingsActivity());

            binding.tvParentEntryAction.setOnClickListener(v -> openModeEntry());

        binding.ivMyAvatar.setOnClickListener(v -> changeAvatar());

        binding.tvMyHistory.setOnClickListener(v ->
                startActivity(new android.content.Intent(getContext(), LnmRecordActivity.class)));

        binding.tvMyWeekly.setOnClickListener(v -> {
                if (getActivity() instanceof LearnNoMobileActivity) {
                    ((LearnNoMobileActivity) getActivity()).switchToTab(0);
                }
            });

        binding.tvMyCalendarPrev.setOnClickListener(v -> shiftMonth(-1));

        binding.tvMyCalendarNext.setOnClickListener(v -> shiftMonth(1));

    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfile();
        updateStats();
        updateCalendar();
        updateVow();
        updateNick();
        updateModeEntry();
        applyStudentModeUi();
    }

    private void loadProfile() {
        String avatar = UsrMsgUtils.getLocalAvatar();
        ImageLoaderUtils.displayRoundHead(binding.ivMyAvatar, avatar, 88);
    }

    private void updateNick() {
        if (binding == null) return;
        binding.tvMyNick.setText(UsrMsgUtils.getNickName());
    }

    private void updateModeEntry() {
        if (binding == null) return;
        if (!studentMode) {
            binding.tvParentEntryAction.setText("学生入口");
            return;
        }
        binding.tvParentEntryAction.setText("家长入口");
    }

    private void openModeEntry() {
        if (studentMode) {
            openParentEntry();
        } else {
            openStudentEntry();
        }
    }

    private void openParentEntry() {
        String pw = UsrMsgUtils.getFocusExitPassword();
        if (pw == null || pw.trim().isEmpty()) {
            SimToast.toastEL("请先设置家长密码");
            openSettingsActivity();
            return;
        }
        if (studentMode) {
            verifyFocusPassword(this::openParentMode);
        } else {
            verifyFocusPassword(this::openSettingsActivity);
        }
    }

    private void openSettingsActivity() {
        if (getContext() == null) return;
        startActivity(new Intent(getContext(), SettingsActivity.class));
    }

    private void openSettingsAndResetPassword() {
        if (getContext() == null) return;
        Intent intent = new Intent(getContext(), SettingsActivity.class);
        intent.putExtra(SettingsActivity.EXTRA_OPEN_RESET_PASSWORD, true);
        startActivity(intent);
    }

    private void openParentMode() {
        if (getContext() == null) return;
        Intent intent = new Intent(getContext(), LearnNoMobileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void openStudentEntry() {
        if (getContext() == null) return;
        Intent intent = new Intent(getContext(), StudentEntryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void applyStudentModeUi() {
        if (binding == null) return;
        if (studentMode) {
            binding.tvMySettings.setVisibility(View.GONE);
            binding.layoutMyShortcuts.setVisibility(View.GONE);
            binding.tvParentEntryAction.setVisibility(View.VISIBLE);
        } else {
            binding.tvMySettings.setVisibility(View.VISIBLE);
            binding.layoutMyShortcuts.setVisibility(View.VISIBLE);
            binding.tvParentEntryAction.setVisibility(View.VISIBLE);
        }
    }

    private void updateVow() {
        if (binding == null) return;
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
            cells.add(new DayCell(d, ratio, isToday, false, c.getTimeInMillis()));
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
        final long dateMillis;

        DayCell(int day, float ratio, boolean today, boolean empty, long dateMillis) {
            this.day = day;
            this.ratio = ratio;
            this.today = today;
            this.empty = empty;
            this.dateMillis = dateMillis;
        }

        static DayCell empty() {
            return new DayCell(0, 0f, false, true, 0L);
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
                holder.itemView.setOnClickListener(null);
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
            holder.itemView.setOnClickListener(v -> showDayRecords(cell.dateMillis));
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

    private void showDayRecords(long dayMillis) {
        if (dayMillis <= 0) return;
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(dayMillis);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.DAY_OF_YEAR, 1);

        List<Lnm> list = lnmDBUtils.findBetween(start.getTime(), end.getTime());
        if (list == null || list.isEmpty()) {
            SimToast.toastEL("当天没有专注记录");
            return;
        }
        List<String> rows = new ArrayList<>();
        for (Lnm l : list) {
            if (l == null || l.createdDate == null) continue;
            String s = TimeUtils.date2String(l.createdDate, "HH:mm");
            String e = TimeUtils.date2String(l.endTime != null ? l.endTime : l.schedule, "HH:mm");
            long endMs = l.endTime != null ? l.endTime.getTime() : l.schedule.getTime();
            long durMin = Math.max(0, TimeUtils.getTimeSpan(endMs, l.createdDate.getTime(), com.blankj.utilcode.constant.TimeConstants.MIN));
            String status = l.finish ? "成功" : "失败";
            int blocked = lnm2file.getScreenOnCount(l.id);
            rows.add(s + " ~ " + e + "(" + durMin + "分钟)\n" + status + " · 未允许应用" + blocked + "次\n");
        }
        String title = String.format(Locale.CHINA, "%d年%02d月%02d日",
                start.get(Calendar.YEAR),
                start.get(Calendar.MONTH) + 1,
                start.get(Calendar.DAY_OF_MONTH));
        if (rows.isEmpty()) {
            SimToast.toastEL("当天没有专注记录");
            return;
        }
        String message = String.join("\n", rows);
        new MessageDialog.Builder(getContext())
                .setTitle(title)
                .setTextGravity(android.view.Gravity.START)
                .setMessage(message)
                .show();
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

    private void verifyFocusPassword(Runnable onPass) {
        String pw = UsrMsgUtils.getFocusExitPassword();
        if (pw == null || pw.trim().isEmpty()) {
            onPass.run();
            return;
        }
        long now = System.currentTimeMillis();
        if (now < parentPwLockUntil) {
            long left = Math.max(0, (parentPwLockUntil - now) / 1000);
            SimToast.toastEL("输入错误次数过多，请稍后再试（" + left + "s）");
            return;
        }
        new InputDialog.Builder(getContext())
                .setTitle("验证专注退出密码")
                .setHint("请输入密码")
                .setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .setCancel("找回密码")
                .setConfirm("确认")
                .setListener(new InputDialog.OnListener() {
                    @Override
                    public void onConfirm(BaseDialog dialog, String content) {
                        String in = content == null ? "" : content.trim();
                        if (pw.equals(in)) {
                            parentPwFailCount = 0;
                            onPass.run();
                        } else {
                            parentPwFailCount++;
                            if (parentPwFailCount >= 3) {
                                parentPwLockUntil = System.currentTimeMillis() + 30_000L;
                                parentPwFailCount = 0;
                                SimToast.toastEL("错误次数过多，已锁定30秒");
                            } else {
                                SimToast.toastEL("密码错误（还可尝试 " + (3 - parentPwFailCount) + " 次）");
                            }
                        }
                    }

                    @Override
                    public void onCancel(BaseDialog dialog) {
                        openSettingsAndResetPassword();
                    }
                })
                .show();
    }

}
