package io.github.yuzhiang.qxb.model.focus;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.SPUtils;


public class FocusRulePrefs {
    private static final String KEY_RULE_CONFIG = "focus_rule_config";
    private static final String KEY_SLEEP_AUTO_ACTIVE = "focus_sleep_auto_active";

    public static class TimeWindow {
        public int startMin;
        public int endMin;

        public TimeWindow() {
        }

        public TimeWindow(int startMin, int endMin) {
            this.startMin = startMin;
            this.endMin = endMin;
        }

        public boolean contains(int nowMin) {
            if (startMin == endMin) return false;
            if (startMin < endMin) {
                return nowMin >= startMin && nowMin < endMin;
            }
            return nowMin >= startMin || nowMin < endMin;
        }
    }

    public static class RuleConfig {
        public boolean enabled = true;

        public TimeWindow schoolHomework = new TimeWindow(18 * 60, 20 * 60);
        public TimeWindow schoolSleep = new TimeWindow(21 * 60, 6 * 60 + 30);
        public TimeWindow schoolFree = new TimeWindow(16 * 60, 18 * 60);

        public TimeWindow weekendHomework = new TimeWindow(9 * 60, 11 * 60);
        public TimeWindow weekendSleep = new TimeWindow(21 * 60, 7 * 60);
        public TimeWindow weekendFree = new TimeWindow(14 * 60, 18 * 60);

        public long tempPassUntil = 0L;
    }

    public static RuleConfig load() {
        String json = SPUtils.getInstance().getString(KEY_RULE_CONFIG, "");
        if (json == null || json.trim().isEmpty()) {
            return new RuleConfig();
        }
        RuleConfig cfg = GsonUtils.fromJson(json, RuleConfig.class);
        return cfg == null ? new RuleConfig() : cfg;
    }

    public static void save(RuleConfig cfg) {
        if (cfg == null) return;
        SPUtils.getInstance().put(KEY_RULE_CONFIG, GsonUtils.toJson(cfg));
    }

    public static void setTempPassUntil(long ts) {
        RuleConfig cfg = load();
        cfg.tempPassUntil = ts;
        save(cfg);
    }

    public static boolean isSleepAutoActive() {
        return SPUtils.getInstance().getBoolean(KEY_SLEEP_AUTO_ACTIVE, false);
    }

    public static void setSleepAutoActive(boolean active) {
        SPUtils.getInstance().put(KEY_SLEEP_AUTO_ACTIVE, active);
    }

    // Category quota feature removed.
}
