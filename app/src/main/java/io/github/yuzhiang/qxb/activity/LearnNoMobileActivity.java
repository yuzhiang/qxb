package io.github.yuzhiang.qxb.activity;

import static com.blankj.utilcode.util.AppUtils.launchAppDetailsSettings;
import static io.github.yuzhiang.qxb.common.FileConfig.PM_TC;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.RomUtils;
import com.blankj.utilcode.util.SPUtils;

import io.github.yuzhiang.qxb.base.BaseDialog;
import io.github.yuzhiang.qxb.fragment.LnmMyFragment;
import io.github.yuzhiang.qxb.fragment.LnmTJFragment;
import io.github.yuzhiang.qxb.fragment.LnmWeeklyFragment;
import io.github.yuzhiang.qxb.MyUtils.UsrMsgUtils;
import io.github.yuzhiang.qxb.R;
import io.github.yuzhiang.qxb.adapter.Viewpager2Adapter;
import io.github.yuzhiang.qxb.base.BaseActivity;
import io.github.yuzhiang.qxb.databinding.ActivityLearnNoMobileBinding;
import io.github.yuzhiang.qxb.view.dialog.MessageDialog;
import io.github.yuzhiang.qxb.model.ParentTodayReport;
import io.github.yuzhiang.qxb.view.tastytoast.SimToast;

import java.util.Arrays;
import java.util.List;

public class LearnNoMobileActivity extends BaseActivity {

    private ActivityLearnNoMobileBinding binding;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLearnNoMobileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BarUtils.setNavBarLightMode(this, true);
        BarUtils.setNavBarColor(this, ContextCompat.getColor(this, R.color.navigation_color));


        List<Fragment> mFragments = Arrays.asList(
                LnmWeeklyFragment.newInstance(),
                LnmTJFragment.newInstance(),
                LnmMyFragment.newInstance(false)
        );

        binding.lnmVp.setAdapter(new Viewpager2Adapter(this, mFragments));

        binding.lnmVp.setOffscreenPageLimit(mFragments.size());
        binding.lnmVp.setUserInputEnabled(false);
        {
            binding.tvRuleHint.setVisibility(View.GONE);
        }

//        //ViewPager的监听事件
        binding.lnmVp.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                binding.bottomNavigationLnm.setCurrentItem(position);
            }
        });

        menu();
        applyBottomNavInset();
        if (savedInstanceState == null) {
            binding.getRoot().post(this::showParentTodayDialog);
        }

    }

    private void menu() {

        binding.bottomNavigationLnm.addItems(Arrays.asList(
                new AHBottomNavigationItem(R.string.main_tab_lnm3, R.drawable.ic_suggestion, R.color.white),
                new AHBottomNavigationItem(R.string.main_tab_lnm6, R.drawable.ic_tongji_24dp, R.color.white),
                new AHBottomNavigationItem(R.string.main_tab_lnm5, R.drawable.ic_my, R.color.white)
        ));

        // Set background color
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

    public void switchToTab(int index) {
        if (binding == null) return;
        int safeIndex = Math.max(0, Math.min(index, binding.bottomNavigationLnm.getItemsCount() - 1));
        binding.lnmVp.setCurrentItem(safeIndex, false);
        binding.bottomNavigationLnm.setCurrentItem(safeIndex);
    }


    private void showParentTodayDialog() {
        ParentTodayReport.TodayStats stats = ParentTodayReport.buildTodayStats();
        new MessageDialog.Builder(this)
                .setTitle("今日快报")
                .setMessage(ParentTodayReport.buildDetailLines(stats))
                .setConfirm("知道了")
                .setCancel("一键确认")
                .setListener(new MessageDialog.OnListener() {
                    @Override
                    public void onConfirm(BaseDialog dialog) {
                    }

                    @Override
                    public void onCancel(BaseDialog dialog) {
                        int confirmed = ParentTodayReport.confirmPendingTodos();
                        if (confirmed <= 0) {
                            SimToast.toastEL("暂无待确认作业");
                        } else {
                            SimToast.toastSe("已确认 " + confirmed + " 项作业");
                        }
                    }
                })
                .show();
    }

}
