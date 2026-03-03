package io.github.yuzhiang.qxb.fragment;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.TimeUtils;

import java.util.List;
import java.util.Set;

import io.github.yuzhiang.qxb.R;
import io.github.yuzhiang.qxb.base.LazyFragment;
import io.github.yuzhiang.qxb.MyUtils.StatusBarUtil;
import io.github.yuzhiang.qxb.model.focus.FocusRulePrefs;
import io.github.yuzhiang.qxb.model.reward.RewardEngine;
import io.github.yuzhiang.qxb.model.reward.RewardPrefs;
import io.github.yuzhiang.qxb.view.tastytoast.SimToast;

public class LnmRewardFragment extends LazyFragment {

    public static LnmRewardFragment newInstance() {
        return new LnmRewardFragment();
    }

    private TextView tvBalance;
    private TextView tvRatio;
    private TextView tvToday;
    private TextView tvStreak;
    private TextView tvYesterday;
    private TextView tvMilestone;
    private TextView tvMilestoneDetail;
    private Button btnUse;

    @Override
    protected int getContentViewId() {
        return R.layout.lnm_fragment_reward;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        TextView title = view.findViewById(R.id.tv_reward_title);
        StatusBarUtil.setPaddingSmart(getContext(), title);
        tvBalance = view.findViewById(R.id.tv_reward_balance);
        tvRatio = view.findViewById(R.id.tv_reward_ratio);
        tvToday = view.findViewById(R.id.tv_reward_today);
        tvStreak = view.findViewById(R.id.tv_reward_streak);
        tvYesterday = view.findViewById(R.id.tv_reward_yesterday);
        tvMilestone = view.findViewById(R.id.tv_reward_milestone);
        tvMilestoneDetail = view.findViewById(R.id.tv_reward_milestone_detail);
        btnUse = view.findViewById(R.id.btn_reward_use);

        if (btnUse != null) {
            btnUse.setOnClickListener(v -> useRewardMinutes(10));
        }

        refreshUi();
    }

    @Override
    public void onResume() {
        super.onResume();
        RewardEngine.settleDailyIfNeeded();
        RewardEngine.resetDailyUsageIfNeeded();
        refreshUi();
    }

    private void refreshUi() {
        RewardPrefs.RewardState st = RewardPrefs.loadState();
        RewardPrefs.RewardConfig cfg = RewardPrefs.loadConfig();
        tvBalance.setText("奖励时长：" + st.balanceMinutes + " 分钟");
        if (tvRatio != null) {
            tvRatio.setText("兑换规则：作业 " + cfg.exchangeBaseMinutes + " 分钟 → 奖励 "
                    + cfg.exchangeRewardMinutes + " 分钟 · 每日上限 " + cfg.dailyMaxMinutes + " 分钟");
        }
        tvToday.setText("今日已使用：" + st.todayUsedMinutes + " 分钟（上限 " + cfg.dailyMaxMinutes + "）");
        tvStreak.setText("连续专注：" + st.focusStreakDays + " 天 · 连续早睡：" + st.sleepStreakDays + " 天");

        List<RewardPrefs.DailySummary> list = RewardPrefs.loadSummaries();
        if (list.isEmpty()) {
            tvYesterday.setText("昨日达标：暂无");
        } else {
            RewardPrefs.DailySummary s = list.get(0);
            String ok = (s.focusOk && s.sleepOk && s.ruleOk) ? "达标" : "未达标";
            tvYesterday.setText("昨日达标：" + ok + " · 奖励 " + s.rewardMinutes + " 分钟");
        }

        Set<String> milestones = RewardPrefs.loadMilestones();
        tvMilestone.setText("阶段里程碑：" + milestones.size() + " 个");
        if (milestones.isEmpty()) {
            tvMilestoneDetail.setText("最近达成：暂无");
        } else {
            String last = milestones.iterator().next();
            tvMilestoneDetail.setText("最近达成：" + mapMilestoneLabel(last));
        }

        if (!cfg.enabled) {
            btnUse.setEnabled(false);
        } else {
            btnUse.setEnabled(true);
        }
    }

    private void useRewardMinutes(int minutes) {
        RewardPrefs.RewardState st = RewardPrefs.loadState();
        RewardPrefs.RewardConfig cfg = RewardPrefs.loadConfig();
        if (!cfg.enabled) {
            SimToast.toastEL("激励功能未开启");
            return;
        }
        if (minutes <= 0) return;
        if (st.balanceMinutes < minutes) {
            SimToast.toastEL("奖励时长不足");
            return;
        }
        if (st.todayUsedMinutes + minutes > cfg.dailyMaxMinutes) {
            SimToast.toastEL("已超过今日奖励使用上限");
            return;
        }
        if (!isFreeTime()) {
            SimToast.toastEL("当前不在自由时间段");
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("使用奖励")
                .setMessage("确认使用 " + minutes + " 分钟奖励时长？")
                .setNegativeButton("取消", null)
                .setPositiveButton("确认", (d, w) -> {
                    st.balanceMinutes -= minutes;
                    st.todayUsedMinutes += minutes;
                    RewardPrefs.saveState(st);
                    String today = TimeUtils.date2String(new java.util.Date(), "yyyy-MM-dd");
                    RewardPrefs.addUsageForDate(today, minutes);
                    FocusRulePrefs.setTempPassUntil(System.currentTimeMillis() + minutes * 60_000L);
                    SimToast.toastSe("已开启 " + minutes + " 分钟奖励通行");
                    refreshUi();
                })
                .show();
    }

    private boolean isFreeTime() {
        FocusRulePrefs.RuleConfig rule = FocusRulePrefs.load();
        if (rule == null || !rule.enabled) return false;
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int day = cal.get(java.util.Calendar.DAY_OF_WEEK);
        boolean weekend = (day == java.util.Calendar.SATURDAY || day == java.util.Calendar.SUNDAY);
        int nowMin = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE);
        FocusRulePrefs.TimeWindow w = weekend ? rule.weekendFree : rule.schoolFree;
        return w != null && w.contains(nowMin);
    }

    private String mapMilestoneLabel(String key) {
        if ("FOCUS_600".equals(key)) return "累计专注 10 小时";
        if ("FOCUS_1800".equals(key)) return "累计专注 30 小时";
        if ("FOCUS_3000".equals(key)) return "累计专注 50 小时";
        if ("STREAK_FOCUS_3".equals(key)) return "连续专注 3 天";
        if ("STREAK_FOCUS_7".equals(key)) return "连续专注 7 天";
        if ("STREAK_SLEEP_3".equals(key)) return "连续早睡 3 天";
        if ("STREAK_SLEEP_7".equals(key)) return "连续早睡 7 天";
        return key;
    }
}
