package io.github.yuzhiang.qxb.activity;

import static io.github.yuzhiang.qxb.view.pickpic.PicUtils.picSel;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.github.yuzhiang.qxb.MyUtils.StatusBarUtil;
import io.github.yuzhiang.qxb.MyUtils.UsrMsgUtils;
import io.github.yuzhiang.qxb.R;
import io.github.yuzhiang.qxb.databinding.ActivitySettingsBinding;
import io.github.yuzhiang.qxb.model.focus.FocusRulePrefs;
import io.github.yuzhiang.qxb.model.reward.RewardPrefs;
import io.github.yuzhiang.qxb.base.BaseDialog;
import io.github.yuzhiang.qxb.view.dialog.InputDialog;
import io.github.yuzhiang.qxb.view.dialog.SelectDialog;
import io.github.yuzhiang.qxb.view.pickpic.ImageCropEngine;
import io.github.yuzhiang.qxb.view.pickpic.ImageFileCompressEngine;
import io.github.yuzhiang.qxb.view.pickpic.PicUtils;
import io.github.yuzhiang.qxb.view.tastytoast.SimToast;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private int pwFailCount = 0;
    private long pwLockUntil = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtil.setPaddingSmart(this, binding.tvSettingsTitle);
        UsrMsgUtils.applyPageBackground(binding.getRoot());

        setupActions();
        refreshUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUi();
    }

    private void setupActions() {
        binding.layoutSettingNick.setOnClickListener(v -> showNickDialog());
        binding.layoutSettingPassword.setOnClickListener(v -> showPasswordDialog());
        binding.layoutSettingQuestion.setOnClickListener(v -> showSecurityQuestionDialog());
        binding.layoutSettingClearPw.setOnClickListener(v -> clearPassword());
        binding.layoutSettingResetPw.setOnClickListener(v -> showResetPasswordDialog());
        binding.layoutSettingRules.setOnClickListener(v -> showRuleSettingsDialog());
        binding.layoutSettingRewards.setOnClickListener(v -> showRewardSettingsDialog());
        binding.layoutSettingBackground.setOnClickListener(v -> showBackgroundDialog());
    }

    private void refreshUi() {
        binding.tvSettingNickValue.setText(UsrMsgUtils.getNickName());
        boolean hasPw = !UsrMsgUtils.getFocusExitPassword().trim().isEmpty();
        binding.tvSettingPasswordValue.setText(hasPw ? "已设置" : "未设置");
        boolean hasQa = !UsrMsgUtils.getFocusExitQuestion().trim().isEmpty()
                && !UsrMsgUtils.getFocusExitAnswer().trim().isEmpty();
        binding.tvSettingQuestionValue.setText(hasQa ? "已设置" : "未设置");
    }

    private void showNickDialog() {
        new InputDialog.Builder(this)
                .setTitle("设置昵称")
                .setHint("请输入昵称")
                .setContent(UsrMsgUtils.getNickName())
                .setCancel("取消")
                .setConfirm("保存")
                .setListener(new InputDialog.OnListener() {
                    @Override
                    public void onConfirm(BaseDialog dialog, String content) {
                        String nick = content == null ? "" : content.trim();
                        if (nick.isEmpty()) {
                            SimToast.toastEL("昵称不能为空");
                            return;
                        }
                        String avatar = UsrMsgUtils.getLocalAvatar();
                        UsrMsgUtils.saveLocalProfile(nick, avatar);
                        refreshUi();
                    }
                })
                .show();
    }

    private void showPasswordDialog() {
        String pw = UsrMsgUtils.getFocusExitPassword();
        if (pw == null || pw.trim().isEmpty()) {
            showSetPasswordDialog(false);
            return;
        }
        verifyFocusPassword(() -> showSetPasswordDialog(true));
    }

    private void showSetPasswordDialog(boolean hadOld) {
        LinearLayout layout = buildTwoInputLayout();
        EditText et1 = (EditText) layout.getChildAt(0);
        EditText et2 = (EditText) layout.getChildAt(1);
        et1.setHint("新密码");
        et2.setHint("确认密码");
        et1.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        et2.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        new AlertDialog.Builder(this)
                .setTitle(hadOld ? "修改家长密码" : "设置家长密码")
                .setView(layout)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (d, w) -> {
                    String p1 = et1.getText().toString().trim();
                    String p2 = et2.getText().toString().trim();
                    if (p1.isEmpty()) {
                        SimToast.toastEL("密码不能为空");
                        return;
                    }
                    if (!p1.equals(p2)) {
                        SimToast.toastEL("两次密码不一致");
                        return;
                    }
                    UsrMsgUtils.setFocusExitPassword(p1);
                    SimToast.toastSe("密码已保存");
                    refreshUi();
                })
                .show();
    }

    private void showSecurityQuestionDialog() {
        String pw = UsrMsgUtils.getFocusExitPassword();
        if (pw != null && !pw.trim().isEmpty()) {
            verifyFocusPassword(this::showSecurityQuestionEdit);
        } else {
            showSecurityQuestionEdit();
        }
    }

    private void showSecurityQuestionEdit() {
        LinearLayout layout = buildTwoInputLayout();
        EditText etQ = (EditText) layout.getChildAt(0);
        EditText etA = (EditText) layout.getChildAt(1);
        etQ.setHint("推荐：身份证后四位");
        etA.setHint("答案");
        etQ.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        etA.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etQ.setText(UsrMsgUtils.getFocusExitQuestion());
        etA.setText(UsrMsgUtils.getFocusExitAnswer());
        new AlertDialog.Builder(this)
                .setTitle("设置安全问题")
                .setView(layout)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (d, w) -> {
                    String q = etQ.getText().toString().trim();
                    String a = etA.getText().toString().trim();
                    if (q.isEmpty() || a.isEmpty()) {
                        SimToast.toastEL("安全问题与答案都需要填写");
                        return;
                    }
                    UsrMsgUtils.setFocusExitQuestion(q);
                    UsrMsgUtils.setFocusExitAnswer(a);
                    SimToast.toastSe("已保存");
                    refreshUi();
                })
                .show();
    }

    private void clearPassword() {
        verifyFocusPassword(() -> new AlertDialog.Builder(this)
                .setTitle("清除密码")
                .setMessage("清除后退出专注将不再需要密码，同时清除安全问题。")
                .setNegativeButton("取消", null)
                .setPositiveButton("清除", (d, w) -> {
                    UsrMsgUtils.setFocusExitPassword("");
                    UsrMsgUtils.setFocusExitQuestion("");
                    UsrMsgUtils.setFocusExitAnswer("");
                    SimToast.toastSe("已清除");
                    refreshUi();
                })
                .show());
    }

    private void showResetPasswordDialog() {
        String q = UsrMsgUtils.getFocusExitQuestion();
        String a = UsrMsgUtils.getFocusExitAnswer();
        if (q == null || q.trim().isEmpty() || a == null || a.trim().isEmpty()) {
            SimToast.toastEL("请先设置安全问题和答案");
            return;
        }
        new InputDialog.Builder(this)
                .setTitle(q)
                .setHint("请输入答案")
                .setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .setCancel("取消")
                .setConfirm("验证")
                .setListener(new InputDialog.OnListener() {
                    @Override
                    public void onConfirm(BaseDialog dialog, String content) {
                        String in = content == null ? "" : content.trim();
                        if (!a.equals(in)) {
                            SimToast.toastEL("答案错误");
                            return;
                        }
                        showSetPasswordDialog(false);
                    }
                })
                .show();
    }

    private void verifyFocusPassword(Runnable onPass) {
        String pw = UsrMsgUtils.getFocusExitPassword();
        if (pw == null || pw.trim().isEmpty()) {
            onPass.run();
            return;
        }
        long now = System.currentTimeMillis();
        if (now < pwLockUntil) {
            long left = Math.max(0, (pwLockUntil - now) / 1000);
            SimToast.toastEL("输入错误次数过多，请稍后再试（" + left + "s）");
            return;
        }
        new InputDialog.Builder(this)
                .setTitle("验证家长密码")
                .setHint("请输入密码")
                .setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .setCancel("取消")
                .setConfirm("确认")
                .setListener(new InputDialog.OnListener() {
                    @Override
                    public void onConfirm(BaseDialog dialog, String content) {
                        String in = content == null ? "" : content.trim();
                        if (pw.equals(in)) {
                            pwFailCount = 0;
                            onPass.run();
                        } else {
                            pwFailCount++;
                            if (pwFailCount >= 3) {
                                pwLockUntil = System.currentTimeMillis() + 30_000L;
                                pwFailCount = 0;
                                SimToast.toastEL("错误次数过多，已锁定30秒");
                            } else {
                                SimToast.toastEL("密码错误（还可尝试 " + (3 - pwFailCount) + " 次）");
                            }
                        }
                    }
                })
                .show();
    }

    private void showRuleSettingsDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_focus_rules, null);
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
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, templates);
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
            public void onItemSelected(android.widget.AdapterView<?> parent, View view1, int position, long id) {
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

        new AlertDialog.Builder(this)
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
        View view = getLayoutInflater().inflate(R.layout.dialog_reward_settings, null);
        EditText etBase = view.findViewById(R.id.et_reward_base);
        EditText etGain = view.findViewById(R.id.et_reward_gain);
        EditText etDaily = view.findViewById(R.id.et_reward_daily_max);
        EditText etLimit = view.findViewById(R.id.et_reward_violation);

        RewardPrefs.RewardConfig cfg = RewardPrefs.loadConfig();
        etBase.setText(String.valueOf(cfg.exchangeBaseMinutes));
        etGain.setText(String.valueOf(cfg.exchangeRewardMinutes));
        etDaily.setText(String.valueOf(cfg.dailyMaxMinutes));
        etLimit.setText(String.valueOf(cfg.violationLimit));

        new AlertDialog.Builder(this)
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

    private void showBackgroundDialog() {
        String[] items = new String[]{"纯白（默认）", "红色渐变", "暖橙渐变", "柔和蓝", "选择图片"};
        int style = UsrMsgUtils.getPageBgStyle();
        int current = 0;
        if (style == UsrMsgUtils.BG_STYLE_RED) current = 1;
        else if (style == UsrMsgUtils.BG_STYLE_WARM) current = 2;
        else if (style == UsrMsgUtils.BG_STYLE_SOFT) current = 3;
        else if (style == UsrMsgUtils.BG_STYLE_IMAGE) current = 4;
        new SelectDialog.Builder(this)
                .setTitle("更换背景")
                .setList(items)
                .setSingleSelect()
                .setSelect(current)
                .setListener((dialog, data) -> {
                    if (data == null || data.isEmpty()) return;
                    Object selectedKey = data.keySet().iterator().next();
                    int which = selectedKey instanceof Integer ? (Integer) selectedKey : Integer.parseInt(String.valueOf(selectedKey));
                    if (which == 4) {
                        selectBackgroundImage();
                        return;
                    }
                    int target;
                    if (which == 1) target = UsrMsgUtils.BG_STYLE_RED;
                    else if (which == 2) target = UsrMsgUtils.BG_STYLE_WARM;
                    else if (which == 3) target = UsrMsgUtils.BG_STYLE_SOFT;
                    else target = UsrMsgUtils.BG_STYLE_WHITE;
                    UsrMsgUtils.setPageBgStyle(target);
                    applyBackground();
                })
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
                        applyBackground();
                    }

                    @Override
                    public void onCancel() {
                    }
                });
    }

    private void applyBackground() {
        UsrMsgUtils.applyPageBackground(findViewById(android.R.id.content));
    }

    private LinearLayout buildTwoInputLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (getResources().getDisplayMetrics().density * 12);
        layout.setPadding(pad, pad, pad, 0);

        EditText et1 = new EditText(this);
        et1.setBackgroundResource(R.drawable.bg_todo_item);
        et1.setPadding(pad, pad, pad, pad);
        layout.addView(et1, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        EditText et2 = new EditText(this);
        et2.setBackgroundResource(R.drawable.bg_todo_item);
        et2.setPadding(pad, pad, pad, pad);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = pad;
        layout.addView(et2, lp);
        return layout;
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
        android.app.TimePickerDialog startPicker = new android.app.TimePickerDialog(this, R.style.TimePickerDialogTheme,
                (v, h, m) -> {
                    android.app.TimePickerDialog endPicker = new android.app.TimePickerDialog(this, R.style.TimePickerDialogTheme,
                            (v1, h1, m1) -> {
                                FocusRulePrefs.TimeWindow w = new FocusRulePrefs.TimeWindow(h * 60 + m, h1 * 60 + m1);
                                cb.onPicked(w);
                            }, endH, endM, true);
                    endPicker.setTitle("选择结束时间");
                    endPicker.show();
                }, startH, startM, true);
        startPicker.setTitle("选择开始时间");
        startPicker.show();
    }

    private void updateRuleTimeLabels(FocusRulePrefs.RuleConfig cfg,
                                      TextView schoolHomework,
                                      TextView schoolSleep,
                                      TextView schoolFree,
                                      TextView weekendHomework,
                                      TextView weekendSleep,
                                      TextView weekendFree) {
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
}
