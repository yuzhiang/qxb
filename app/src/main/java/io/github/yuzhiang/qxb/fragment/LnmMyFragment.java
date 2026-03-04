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
import io.github.yuzhiang.qxb.view.pickpic.ImageCropEngine;
import io.github.yuzhiang.qxb.view.pickpic.ImageFileCompressEngine;
import io.github.yuzhiang.qxb.view.pickpic.PicUtils;
import io.github.yuzhiang.qxb.view.tastytoast.SimToast;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;

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
        if (binding.tvParentEntryAction != null) {
            binding.tvParentEntryAction.setOnClickListener(v -> openModeEntry());
        }
        binding.ivMyAvatar.setOnClickListener(v -> changeAvatar());
        if (binding.tvMyVow != null) {
            binding.tvMyVow.setOnClickListener(v -> showVowDialog());
        }
        binding.tvMyHistory.setOnClickListener(v -> showHistoryPopup());
        if (binding.tvMyWeekly != null) {
            binding.tvMyWeekly.setOnClickListener(v -> {
                if (getActivity() instanceof LearnNoMobileActivity) {
                    ((LearnNoMobileActivity) getActivity()).switchToTab(0);
                }
            });
        }
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
        updateModeEntry();
        applyStudentModeUi();
    }

    private void loadProfile() {
        String avatar = UsrMsgUtils.getLocalAvatar();
        ImageLoaderUtils.displayRoundHead(binding.ivMyAvatar, avatar, 88);
    }

    private void updateNick() {
        if (binding == null || binding.tvMyNick == null) return;
        binding.tvMyNick.setText(UsrMsgUtils.getNickName());
    }

    private void updateModeEntry() {
        if (binding == null || binding.tvParentEntryAction == null) return;
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
            if (binding.tvMySettings != null) binding.tvMySettings.setVisibility(View.GONE);
            if (binding.layoutMyShortcuts != null) binding.layoutMyShortcuts.setVisibility(View.GONE);
            if (binding.tvParentEntryAction != null) binding.tvParentEntryAction.setVisibility(View.VISIBLE);
        } else {
            if (binding.tvMySettings != null) binding.tvMySettings.setVisibility(View.VISIBLE);
            if (binding.layoutMyShortcuts != null) binding.layoutMyShortcuts.setVisibility(View.VISIBLE);
            if (binding.tvParentEntryAction != null) binding.tvParentEntryAction.setVisibility(View.VISIBLE);
        }
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
            rows.add(s + " ~ " + e + " · " + durMin + "分钟 · " + status + " · 未允许应用" + blocked + "次");
        }
        String title = String.format(Locale.CHINA, "%d年%02d月%02d日",
                start.get(Calendar.YEAR),
                start.get(Calendar.MONTH) + 1,
                start.get(Calendar.DAY_OF_MONTH));
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setItems(rows.toArray(new String[0]), null)
                .setPositiveButton("关闭", null)
                .show();
    }

    private void saveProfileNick(String nick) {
        if (nick.isEmpty()) {
            SimToast.toastEL("昵称不能为空");
            return;
        }
        String avatar = UsrMsgUtils.getLocalAvatar();
        UsrMsgUtils.saveLocalProfile(nick, avatar);
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
        android.widget.EditText etPwConfirm = view.findViewById(R.id.et_setting_password_confirm);
        android.widget.CheckBox cbShowPw = view.findViewById(R.id.cb_setting_show_password);
        android.widget.EditText etQuestion = view.findViewById(R.id.et_setting_pw_question);
        android.widget.EditText etAnswer = view.findViewById(R.id.et_setting_pw_answer);
        android.widget.Button btnClear = view.findViewById(R.id.btn_setting_clear_pw);
        android.widget.Button btnReset = view.findViewById(R.id.btn_setting_reset_pw);
        android.widget.Button btnRules = view.findViewById(R.id.btn_setting_rules);
        android.widget.Button btnRewards = view.findViewById(R.id.btn_setting_rewards);

        etNick.setText(UsrMsgUtils.getNickName());
        etPw.setText(UsrMsgUtils.getFocusExitPassword());
        etPwConfirm.setText(UsrMsgUtils.getFocusExitPassword());
        etQuestion.setText(UsrMsgUtils.getFocusExitQuestion());
        etAnswer.setText(UsrMsgUtils.getFocusExitAnswer());

        final boolean hasPw = !UsrMsgUtils.getFocusExitPassword().trim().isEmpty();
        final boolean[] pwVerified = new boolean[]{false};
        cbShowPw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && hasPw && !pwVerified[0]) {
                cbShowPw.setChecked(false);
                verifyFocusPassword(() -> {
                    pwVerified[0] = true;
                    cbShowPw.setChecked(true);
                });
                return;
            }
            int pwType = isChecked
                    ? android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;
            etPw.setInputType(pwType);
            etPwConfirm.setInputType(pwType);
            etAnswer.setInputType(pwType);
            etPw.setSelection(etPw.getText().length());
            etPwConfirm.setSelection(etPwConfirm.getText().length());
            etAnswer.setSelection(etAnswer.getText().length());
        });

        btnClear.setOnClickListener(v -> {
            Runnable doClear = () -> new AlertDialog.Builder(getContext())
                    .setTitle("清除专注退出密码")
                    .setMessage("清除后退出专注将不再需要密码，同时清除安全问题。")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("清除", (d, w) -> {
                        UsrMsgUtils.setFocusExitPassword("");
                        UsrMsgUtils.setFocusExitQuestion("");
                        UsrMsgUtils.setFocusExitAnswer("");
                        etPw.setText("");
                        etPwConfirm.setText("");
                        etQuestion.setText("");
                        etAnswer.setText("");
                        cbShowPw.setChecked(false);
                        SimToast.toastSe("已清除");
                    })
                    .show();
            if (hasPw && !pwVerified[0]) {
                verifyFocusPassword(() -> {
                    pwVerified[0] = true;
                    doClear.run();
                });
            } else {
                doClear.run();
            }
        });

        btnReset.setOnClickListener(v -> showResetPasswordDialog());
        btnRules.setOnClickListener(v -> showRuleSettingsDialog());
        if (btnRewards != null) {
            btnRewards.setOnClickListener(v -> showRewardSettingsDialog());
        }

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("设置")
                .setView(view)
                .setPositiveButton("保存", null)
                .setNeutralButton("更换背景", (d, w) -> showBackgroundDialog())
                .setNegativeButton("关闭", null)
                .create();
        dialog.setOnShowListener(dlg -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nick = etNick.getText().toString().trim();
            if (nick.isEmpty()) {
                SimToast.toastEL("昵称不能为空");
                return;
            }

            String pw = etPw.getText().toString().trim();
            String confirm = etPwConfirm.getText().toString().trim();
            boolean wantChangePw = !pw.isEmpty() || !confirm.isEmpty();
            if (wantChangePw && !pw.equals(confirm)) {
                SimToast.toastEL("两次密码不一致");
                return;
            }
            if (hasPw && wantChangePw && !pwVerified[0]) {
                verifyFocusPassword(() -> {
                    pwVerified[0] = true;
                    if (pw.isEmpty()) {
                        SimToast.toastEL("新密码不能为空");
                        return;
                    }
                    UsrMsgUtils.setFocusExitPassword(pw);
                    dialog.dismiss();
                });
                return;
            }

            String q = etQuestion.getText().toString().trim();
            String a = etAnswer.getText().toString().trim();
            boolean wantQa = !q.isEmpty() || !a.isEmpty();
            if (wantQa && (q.isEmpty() || a.isEmpty())) {
                SimToast.toastEL("安全问题与答案都需要填写");
                return;
            }

            saveProfileNick(nick);
            if (wantChangePw) {
                if (pw.isEmpty()) {
                    SimToast.toastEL("新密码不能为空");
                    return;
                }
                UsrMsgUtils.setFocusExitPassword(pw);
            }
            if (wantQa) {
                UsrMsgUtils.setFocusExitQuestion(q);
                UsrMsgUtils.setFocusExitAnswer(a);
            }
            dialog.dismiss();
        }));
        dialog.show();
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
        android.widget.EditText input = new android.widget.EditText(getContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        new AlertDialog.Builder(getContext())
                .setTitle("验证专注退出密码")
                .setView(input)
                .setNegativeButton("取消", null)
                .setPositiveButton("确认", (d, w) -> {
                    String in = input.getText().toString().trim();
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
                })
                .show();
    }

    private void showResetPasswordDialog() {
        String q = UsrMsgUtils.getFocusExitQuestion();
        String a = UsrMsgUtils.getFocusExitAnswer();
        if (q == null || q.trim().isEmpty() || a == null || a.trim().isEmpty()) {
            SimToast.toastEL("请先在设置中填写安全问题和答案");
            return;
        }
        android.widget.EditText input = new android.widget.EditText(getContext());
        input.setHint("请输入答案");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        new AlertDialog.Builder(getContext())
                .setTitle(q)
                .setView(input)
                .setNegativeButton("取消", null)
                .setPositiveButton("验证", (d, w) -> {
                    String in = input.getText().toString().trim();
                    if (!a.equals(in)) {
                        SimToast.toastEL("答案错误");
                        return;
                    }
                    showSetNewPasswordDialog();
                })
                .show();
    }

    private void showRuleSettingsDialog() {
        View view = android.view.LayoutInflater.from(getContext()).inflate(R.layout.dialog_focus_rules, null);
        android.widget.Switch swEnabled = view.findViewById(R.id.sw_rules_enabled);
        android.widget.Spinner spTemplate = view.findViewById(R.id.sp_rule_template);
        android.widget.TextView tvSchoolHomework = view.findViewById(R.id.tv_school_homework);
        android.widget.TextView tvSchoolSleep = view.findViewById(R.id.tv_school_sleep);
        android.widget.TextView tvSchoolFree = view.findViewById(R.id.tv_school_free);
        android.widget.TextView tvWeekendHomework = view.findViewById(R.id.tv_weekend_homework);
        android.widget.TextView tvWeekendSleep = view.findViewById(R.id.tv_weekend_sleep);
        android.widget.TextView tvWeekendFree = view.findViewById(R.id.tv_weekend_free);

        FocusRulePrefs.RuleConfig cfg = FocusRulePrefs.load();
        swEnabled.setChecked(cfg.enabled);

        String[] templates = new String[]{"小学生", "初中生", "高中生", "自定义"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, templates);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTemplate.setAdapter(adapter);
        spTemplate.setSelection(3);

        updateRuleTimeLabels(cfg, tvSchoolHomework, tvSchoolSleep, tvSchoolFree, tvWeekendHomework, tvWeekendSleep, tvWeekendFree);

        tvSchoolHomework.setOnClickListener(v -> pickWindowTime("作业时间", cfg.schoolHomework, w -> {
            cfg.schoolHomework = w;
            updateRuleTimeLabels(cfg, tvSchoolHomework, tvSchoolSleep, tvSchoolFree, tvWeekendHomework, tvWeekendSleep, tvWeekendFree);
        }));
        tvSchoolSleep.setOnClickListener(v -> pickWindowTime("睡眠时间", cfg.schoolSleep, w -> {
            cfg.schoolSleep = w;
            updateRuleTimeLabels(cfg, tvSchoolHomework, tvSchoolSleep, tvSchoolFree, tvWeekendHomework, tvWeekendSleep, tvWeekendFree);
        }));
        tvSchoolFree.setOnClickListener(v -> pickWindowTime("自由时间", cfg.schoolFree, w -> {
            cfg.schoolFree = w;
            updateRuleTimeLabels(cfg, tvSchoolHomework, tvSchoolSleep, tvSchoolFree, tvWeekendHomework, tvWeekendSleep, tvWeekendFree);
        }));
        tvWeekendHomework.setOnClickListener(v -> pickWindowTime("作业时间", cfg.weekendHomework, w -> {
            cfg.weekendHomework = w;
            updateRuleTimeLabels(cfg, tvSchoolHomework, tvSchoolSleep, tvSchoolFree, tvWeekendHomework, tvWeekendSleep, tvWeekendFree);
        }));
        tvWeekendSleep.setOnClickListener(v -> pickWindowTime("睡眠时间", cfg.weekendSleep, w -> {
            cfg.weekendSleep = w;
            updateRuleTimeLabels(cfg, tvSchoolHomework, tvSchoolSleep, tvSchoolFree, tvWeekendHomework, tvWeekendSleep, tvWeekendFree);
        }));
        tvWeekendFree.setOnClickListener(v -> pickWindowTime("自由时间", cfg.weekendFree, w -> {
            cfg.weekendFree = w;
            updateRuleTimeLabels(cfg, tvSchoolHomework, tvSchoolSleep, tvSchoolFree, tvWeekendHomework, tvWeekendSleep, tvWeekendFree);
        }));

        spTemplate.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view1, int position, long id) {
                if (position == 3) return;
                FocusRulePrefs.RuleConfig t = buildTemplate(position);
                cfg.schoolHomework = t.schoolHomework;
                cfg.schoolSleep = t.schoolSleep;
                cfg.schoolFree = t.schoolFree;
                cfg.weekendHomework = t.weekendHomework;
                cfg.weekendSleep = t.weekendSleep;
                cfg.weekendFree = t.weekendFree;
                updateRuleTimeLabels(cfg, tvSchoolHomework, tvSchoolSleep, tvSchoolFree, tvWeekendHomework, tvWeekendSleep, tvWeekendFree);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        new AlertDialog.Builder(getContext())
                .setTitle("规则设置")
                .setView(view)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (d, w) -> {
                    cfg.enabled = swEnabled.isChecked();
                    FocusRulePrefs.save(cfg);
                    SimToast.toastSe("规则已保存");
                })
                .show();
    }

    private void showRewardSettingsDialog() {
        View view = android.view.LayoutInflater.from(getContext()).inflate(R.layout.dialog_reward_settings, null);
        android.widget.EditText etBase = view.findViewById(R.id.et_reward_base);
        android.widget.EditText etGain = view.findViewById(R.id.et_reward_gain);
        android.widget.EditText etDaily = view.findViewById(R.id.et_reward_daily_max);
        android.widget.EditText etLimit = view.findViewById(R.id.et_reward_violation);

        RewardPrefs.RewardConfig cfg = RewardPrefs.loadConfig();
        etBase.setText(String.valueOf(cfg.exchangeBaseMinutes));
        etGain.setText(String.valueOf(cfg.exchangeRewardMinutes));
        etDaily.setText(String.valueOf(cfg.dailyMaxMinutes));
        etLimit.setText(String.valueOf(cfg.violationLimit));

        new AlertDialog.Builder(getContext())
                .setTitle("激励设置")
                .setView(view)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (d, w) -> {
                    cfg.exchangeBaseMinutes = parseInt(etBase.getText().toString(), cfg.exchangeBaseMinutes);
                    cfg.exchangeRewardMinutes = parseInt(etGain.getText().toString(), cfg.exchangeRewardMinutes);
                    cfg.dailyMaxMinutes = parseInt(etDaily.getText().toString(), cfg.dailyMaxMinutes);
                    cfg.violationLimit = parseInt(etLimit.getText().toString(), cfg.violationLimit);
                    RewardPrefs.saveConfig(cfg);
                    SimToast.toastSe("已保存激励设置");
                })
                .show();
    }

    private int parseInt(String text, int fallback) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private FocusRulePrefs.RuleConfig buildTemplate(int type) {
        FocusRulePrefs.RuleConfig t = new FocusRulePrefs.RuleConfig();
        if (type == 0) { // 小学生
            t.schoolHomework = new FocusRulePrefs.TimeWindow(17 * 60, 19 * 60);
            t.schoolSleep = new FocusRulePrefs.TimeWindow(20 * 60 + 30, 6 * 60 + 30);
            t.schoolFree = new FocusRulePrefs.TimeWindow(16 * 60, 17 * 60);
            t.weekendHomework = new FocusRulePrefs.TimeWindow(9 * 60, 10 * 60 + 30);
            t.weekendSleep = new FocusRulePrefs.TimeWindow(20 * 60 + 30, 7 * 60);
            t.weekendFree = new FocusRulePrefs.TimeWindow(14 * 60, 16 * 60);
        } else if (type == 1) { // 初中生
            t.schoolHomework = new FocusRulePrefs.TimeWindow(18 * 60, 20 * 60);
            t.schoolSleep = new FocusRulePrefs.TimeWindow(21 * 60, 6 * 60 + 30);
            t.schoolFree = new FocusRulePrefs.TimeWindow(16 * 60, 18 * 60);
            t.weekendHomework = new FocusRulePrefs.TimeWindow(9 * 60, 11 * 60);
            t.weekendSleep = new FocusRulePrefs.TimeWindow(21 * 60, 7 * 60);
            t.weekendFree = new FocusRulePrefs.TimeWindow(14 * 60, 18 * 60);
        } else { // 高中生
            t.schoolHomework = new FocusRulePrefs.TimeWindow(18 * 60, 21 * 60);
            t.schoolSleep = new FocusRulePrefs.TimeWindow(22 * 60, 6 * 60);
            t.schoolFree = new FocusRulePrefs.TimeWindow(16 * 60, 18 * 60);
            t.weekendHomework = new FocusRulePrefs.TimeWindow(9 * 60, 12 * 60);
            t.weekendSleep = new FocusRulePrefs.TimeWindow(22 * 60, 7 * 60);
            t.weekendFree = new FocusRulePrefs.TimeWindow(14 * 60, 19 * 60);
        }
        return t;
    }

    private interface WindowPicked {
        void onPicked(FocusRulePrefs.TimeWindow window);
    }

    private void pickWindowTime(String title, FocusRulePrefs.TimeWindow window, WindowPicked cb) {
        if (window == null) window = new FocusRulePrefs.TimeWindow(18 * 60, 20 * 60);
        int startH = (window.startMin / 60) % 24;
        int startM = window.startMin % 60;
        int endH = (window.endMin / 60) % 24;
        int endM = window.endMin % 60;
        android.app.TimePickerDialog startPicker = new android.app.TimePickerDialog(getContext(), R.style.TimePickerDialogTheme,
                (v, h, m) -> {
                    android.app.TimePickerDialog endPicker = new android.app.TimePickerDialog(getContext(), R.style.TimePickerDialogTheme,
                            (v2, h2, m2) -> {
                                FocusRulePrefs.TimeWindow w = new FocusRulePrefs.TimeWindow(h * 60 + m, h2 * 60 + m2);
                                cb.onPicked(w);
                            }, endH, endM, true);
                    endPicker.setTitle("选择结束时间");
                    endPicker.show();
                }, startH, startM, true);
        startPicker.setTitle("选择开始时间");
        startPicker.show();
    }

    private void updateRuleTimeLabels(FocusRulePrefs.RuleConfig cfg,
                                      android.widget.TextView schoolHomework,
                                      android.widget.TextView schoolSleep,
                                      android.widget.TextView schoolFree,
                                      android.widget.TextView weekendHomework,
                                      android.widget.TextView weekendSleep,
                                      android.widget.TextView weekendFree) {
        schoolHomework.setText("作业时间：" + formatWindow(cfg.schoolHomework));
        schoolSleep.setText("睡眠时间：" + formatWindow(cfg.schoolSleep));
        schoolFree.setText("自由时间：" + formatWindow(cfg.schoolFree));
        weekendHomework.setText("作业时间：" + formatWindow(cfg.weekendHomework));
        weekendSleep.setText("睡眠时间：" + formatWindow(cfg.weekendSleep));
        weekendFree.setText("自由时间：" + formatWindow(cfg.weekendFree));
    }

    private String formatWindow(FocusRulePrefs.TimeWindow w) {
        if (w == null) return "--";
        return formatMin(w.startMin) + " - " + formatMin(w.endMin);
    }

    private String formatMin(int min) {
        int h = (min / 60) % 24;
        int m = min % 60;
        return String.format(Locale.CHINA, "%02d:%02d", h, m);
    }

    // 临时通行入口已移动到专注页面

    private void showSetNewPasswordDialog() {
        View view = android.view.LayoutInflater.from(getContext()).inflate(R.layout.dialog_my_settings, null);
        android.widget.EditText etPw = view.findViewById(R.id.et_setting_password);
        android.widget.EditText etPwConfirm = view.findViewById(R.id.et_setting_password_confirm);
        view.findViewById(R.id.et_setting_nick).setVisibility(View.GONE);
        view.findViewById(R.id.et_setting_pw_question).setVisibility(View.GONE);
        view.findViewById(R.id.et_setting_pw_answer).setVisibility(View.GONE);
        view.findViewById(R.id.btn_setting_clear_pw).setVisibility(View.GONE);
        view.findViewById(R.id.btn_setting_reset_pw).setVisibility(View.GONE);
        view.findViewById(R.id.cb_setting_show_password).setVisibility(View.GONE);
        new AlertDialog.Builder(getContext())
                .setTitle("设置新密码")
                .setView(view)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (d, w) -> {
                    String pw = etPw.getText().toString().trim();
                    String confirm = etPwConfirm.getText().toString().trim();
                    if (pw.isEmpty()) {
                        SimToast.toastEL("密码不能为空");
                        return;
                    }
                    if (!pw.equals(confirm)) {
                        SimToast.toastEL("两次密码不一致");
                        return;
                    }
                    UsrMsgUtils.setFocusExitPassword(pw);
                    SimToast.toastSe("密码已重置");
                })
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
            int blocked = io.github.yuzhiang.qxb.model.lnm2file.getScreenOnCount(l.id);
            items[i] = start + " ~ " + end + " · " + durMin + "分钟 · 未允许应用" + blocked + "次";
        }
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("专注记录（最近30次）")
                .setItems(items, null)
                .setPositiveButton("查看全部", (d, w) -> startActivity(new android.content.Intent(getContext(), LnmRecordActivity.class)))
                .setNegativeButton("关闭", null)
                .show();
    }

}
