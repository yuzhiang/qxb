package io.github.yuzhiang.qxb.activity;

import static com.blankj.utilcode.util.AppUtils.launchAppDetailsSettings;
import static io.github.yuzhiang.qxb.common.FileConfig.PM_TC;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.RomUtils;
import com.blankj.utilcode.util.SPUtils;

import java.util.Arrays;
import java.util.List;

import io.github.yuzhiang.qxb.MyUtils.UsrMsgUtils;
import io.github.yuzhiang.qxb.R;
import io.github.yuzhiang.qxb.adapter.Viewpager2Adapter;
import io.github.yuzhiang.qxb.base.BaseActivity;
import io.github.yuzhiang.qxb.databinding.ActivityLearnNoMobileBinding;
import io.github.yuzhiang.qxb.fragment.LnmAllFragment;
import io.github.yuzhiang.qxb.fragment.LnmMainFragment;
import io.github.yuzhiang.qxb.fragment.LnmMyFragment;
import io.github.yuzhiang.qxb.fragment.LnmRewardFragment;
import io.github.yuzhiang.qxb.model.focus.FocusRulePrefs;
import io.github.yuzhiang.qxb.model.reward.RewardEngine;
import io.github.yuzhiang.qxb.view.dialog.MessageDialog;

public class StudentEntryActivity extends BaseActivity {

    private ActivityLearnNoMobileBinding binding;
    private Handler ruleHandler;
    private Runnable ruleTick;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLearnNoMobileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BarUtils.setNavBarLightMode(this, true);
        BarUtils.setNavBarColor(this, ContextCompat.getColor(this, R.color.navigation_color));

        List<Fragment> mFragments = Arrays.asList(
                LnmAllFragment.newInstance(),
                LnmMainFragment.newInstance(),
                LnmRewardFragment.newInstance(),
                LnmMyFragment.newInstance(true)
        );

        binding.lnmVp.setAdapter(new Viewpager2Adapter(this, mFragments));
        binding.lnmVp.setOffscreenPageLimit(mFragments.size());
        binding.lnmVp.setUserInputEnabled(false);
        binding.lnmVp.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                binding.bottomNavigationLnm.setCurrentItem(position);
            }
        });

        menu();
        applyBottomNavInset();
        initRuleHint();
        RewardEngine.settleDailyIfNeeded();
        RewardEngine.resetDailyUsageIfNeeded();
    }

    private void menu() {
        binding.bottomNavigationLnm.addItems(Arrays.asList(
                new AHBottomNavigationItem(R.string.main_tab_lnm1, R.drawable.ic_timeline, R.color.white),
                new AHBottomNavigationItem(R.string.main_tab_lnm2, R.drawable.ic_pets, R.color.white),
                new AHBottomNavigationItem(R.string.main_tab_lnm4, R.drawable.ic_timeline, R.color.white),
                new AHBottomNavigationItem(R.string.main_tab_lnm5, R.drawable.ic_boy, R.color.white)
        ));

        binding.bottomNavigationLnm.setDefaultBackgroundColor(ContextCompat.getColor(this, R.color.navigation_color));
        binding.bottomNavigationLnm.setAccentColor(ContextCompat.getColor(this, UsrMsgUtils.getThemeColor()));
        binding.bottomNavigationLnm.setInactiveColor(Color.parseColor("#747474"));
        binding.bottomNavigationLnm.setBehaviorTranslationEnabled(true);
        binding.bottomNavigationLnm.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        binding.bottomNavigationLnm.setOnTabSelectedListener((position, wasSelected) -> {
            binding.lnmVp.setCurrentItem(position, false);
            return true;
        });
        binding.bottomNavigationLnm.setCurrentItem(0);
    }

    private void applyBottomNavInset() {
        binding.bottomNavigationLnm.post(() -> {
            ViewGroup.LayoutParams lp = binding.lnmVp.getLayoutParams();
            if (!(lp instanceof ViewGroup.MarginLayoutParams)) return;
            int navHeight = binding.bottomNavigationLnm.getHeight();
            if (navHeight <= 0) return;
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
            if (mlp.bottomMargin == navHeight) return;
            mlp.bottomMargin = navHeight;
            binding.lnmVp.setLayoutParams(mlp);
        });
    }

    private void initRuleHint() {
        if (binding.tvRuleHint == null) return;
        binding.tvRuleHint.setVisibility(View.VISIBLE);
        updateRuleHint();
        if (ruleHandler == null) {
            ruleHandler = new Handler(Looper.getMainLooper());
        }
        if (ruleTick == null) {
            ruleTick = new Runnable() {
                @Override
                public void run() {
                    updateRuleHint();
                    if (ruleHandler != null) {
                        ruleHandler.postDelayed(this, 60_000L);
                    }
                }
            };
        }
        ruleHandler.removeCallbacks(ruleTick);
        ruleHandler.postDelayed(ruleTick, 60_000L);
    }

    private void updateRuleHint() {
        if (binding.tvRuleHint == null) return;
        FocusRulePrefs.RuleConfig cfg = FocusRulePrefs.load();
        if (cfg == null || !cfg.enabled) {
            binding.tvRuleHint.setText("当前时段：未开启规则");
            return;
        }
        String label = getCurrentRuleLabel(cfg);
        binding.tvRuleHint.setText(label);
    }

    private String getCurrentRuleLabel(FocusRulePrefs.RuleConfig cfg) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int day = cal.get(java.util.Calendar.DAY_OF_WEEK);
        boolean weekend = (day == java.util.Calendar.SATURDAY || day == java.util.Calendar.SUNDAY);
        int nowMin = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE);
        FocusRulePrefs.TimeWindow sleep = weekend ? cfg.weekendSleep : cfg.schoolSleep;
        FocusRulePrefs.TimeWindow homework = weekend ? cfg.weekendHomework : cfg.schoolHomework;
        FocusRulePrefs.TimeWindow free = weekend ? cfg.weekendFree : cfg.schoolFree;
        if (sleep != null && sleep.contains(nowMin)) {
            return "当前时段：睡眠时间 · 将限制使用手机";
        }
        if (homework != null && homework.contains(nowMin)) {
            return "当前时段：作业时间 · 建议专注完成作业";
        }
        if (free != null && free.contains(nowMin)) {
            return "当前时段：自由时间 · 可合理使用手机";
        }
        return "当前时段：未设置规则";
    }

    @Override
    protected void onDestroy() {
        if (ruleHandler != null && ruleTick != null) {
            ruleHandler.removeCallbacks(ruleTick);
        }
        super.onDestroy();
    }

}
