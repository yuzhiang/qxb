package io.github.yuzhiang.qxb.model.reward;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.SPUtils;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RewardPrefs {
    private static final String KEY_REWARD_CONFIG = "reward_config";
    private static final String KEY_REWARD_STATE = "reward_state";
    private static final String KEY_REWARD_SUMMARY = "reward_summary_list";
    private static final String KEY_REWARD_MILESTONE = "reward_milestones";
    private static final String KEY_REWARD_USAGE = "reward_usage_list";

    public static class RewardConfig {
        public boolean enabled = true;
        public int exchangeBaseMinutes = 45;
        public int exchangeRewardMinutes = 10;
        public int dailyMaxMinutes = 30;
        public int violationLimit = 5;

        public int focusStreak3Bonus = 10;
        public int focusStreak7Bonus = 30;
        public int sleepStreak3Bonus = 10;
        public int sleepStreak7Bonus = 30;
    }

    public static class RewardState {
        public int balanceMinutes = 0;
        public int todayUsedMinutes = 0;
        public int focusStreakDays = 0;
        public int sleepStreakDays = 0;
        public String lastSettledDate = "";
    }

    public static class DailySummary {
        public String date; // yyyy-MM-dd
        public int homeworkMinutes;
        public int totalFocusMinutes;
        public int blockedAttempts;
        public int sleepAttempts;
        public boolean ruleOk;
        public boolean focusOk;
        public boolean sleepOk;
        public int rewardMinutes;
    }

    public static class RewardUsage {
        public String date; // yyyy-MM-dd
        public int usedMinutes;
    }

    public static RewardConfig loadConfig() {
        String json = SPUtils.getInstance().getString(KEY_REWARD_CONFIG, "");
        if (json == null || json.trim().isEmpty()) return new RewardConfig();
        RewardConfig cfg = GsonUtils.fromJson(json, RewardConfig.class);
        return cfg == null ? new RewardConfig() : cfg;
    }

    public static void saveConfig(RewardConfig cfg) {
        if (cfg == null) return;
        SPUtils.getInstance().put(KEY_REWARD_CONFIG, GsonUtils.toJson(cfg));
    }

    public static RewardState loadState() {
        String json = SPUtils.getInstance().getString(KEY_REWARD_STATE, "");
        if (json == null || json.trim().isEmpty()) return new RewardState();
        RewardState st = GsonUtils.fromJson(json, RewardState.class);
        return st == null ? new RewardState() : st;
    }

    public static void saveState(RewardState st) {
        if (st == null) return;
        SPUtils.getInstance().put(KEY_REWARD_STATE, GsonUtils.toJson(st));
    }

    public static List<DailySummary> loadSummaries() {
        String json = SPUtils.getInstance().getString(KEY_REWARD_SUMMARY, "");
        if (json == null || json.trim().isEmpty()) return new ArrayList<>();
        try {
            List<DailySummary> list = GsonUtils.fromJson(json, new TypeToken<List<DailySummary>>() {
            }.getType());
            return list == null ? new ArrayList<>() : new ArrayList<>(list);
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    public static void saveSummaries(List<DailySummary> list) {
        SPUtils.getInstance().put(KEY_REWARD_SUMMARY, GsonUtils.toJson(list));
    }

    public static Set<String> loadMilestones() {
        String json = SPUtils.getInstance().getString(KEY_REWARD_MILESTONE, "");
        if (json == null || json.trim().isEmpty()) return new HashSet<>();
        try {
            List<String> list = GsonUtils.fromJson(json, new TypeToken<List<String>>() {
            }.getType());
            return list == null ? new HashSet<>() : new HashSet<>(list);
        } catch (Exception ignored) {
            return new HashSet<>();
        }
    }

    public static List<RewardUsage> loadUsage() {
        String json = SPUtils.getInstance().getString(KEY_REWARD_USAGE, "");
        if (json == null || json.trim().isEmpty()) return new ArrayList<>();
        try {
            List<RewardUsage> list = GsonUtils.fromJson(json, new TypeToken<List<RewardUsage>>() {
            }.getType());
            return list == null ? new ArrayList<>() : new ArrayList<>(list);
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    public static void saveUsage(List<RewardUsage> list) {
        SPUtils.getInstance().put(KEY_REWARD_USAGE, GsonUtils.toJson(list));
    }

    public static void addUsageForDate(String date, int minutes) {
        if (date == null || date.trim().isEmpty() || minutes <= 0) return;
        List<RewardUsage> list = loadUsage();
        RewardUsage target = null;
        for (RewardUsage usage : list) {
            if (usage == null) continue;
            if (date.equals(usage.date)) {
                target = usage;
                break;
            }
        }
        if (target == null) {
            target = new RewardUsage();
            target.date = date;
            target.usedMinutes = minutes;
            list.add(0, target);
        } else {
            target.usedMinutes += minutes;
        }
        if (list.size() > 90) {
            list = list.subList(0, 90);
        }
        saveUsage(list);
    }

    public static void saveMilestones(Set<String> set) {
        SPUtils.getInstance().put(KEY_REWARD_MILESTONE, GsonUtils.toJson(new ArrayList<>(set)));
    }
}
