package io.github.yuzhiang.qxb.activity;

import static com.blankj.utilcode.util.AppUtils.launchAppDetailsSettings;
import static io.github.yuzhiang.qxb.common.FileConfig.PM_TC;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.RomUtils;
import com.blankj.utilcode.util.SPUtils;

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

        showMsg();


        List<Fragment> mFragments = Arrays.asList(
                LnmWeeklyFragment.newInstance(),
                LnmTJFragment.newInstance(),
                LnmMyFragment.newInstance(false)
        );

        binding.lnmVp.setAdapter(new Viewpager2Adapter(this, mFragments));

        binding.lnmVp.setOffscreenPageLimit(mFragments.size());
        binding.lnmVp.setUserInputEnabled(false);
        if (binding.tvRuleHint != null) {
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
        if (savedInstanceState == null) {
            binding.getRoot().post(this::showParentTodayDialog);
        }

    }

    private void menu() {

        binding.bottomNavigationLnm.addItems(Arrays.asList(
                new AHBottomNavigationItem(R.string.main_tab_lnm3, R.drawable.ic_suggestion, R.color.white),
                new AHBottomNavigationItem(R.string.main_tab_lnm6, R.drawable.ic_tongji_24dp, R.color.white),
                new AHBottomNavigationItem(R.string.main_tab_lnm5, R.drawable.ic_boy, R.color.white)
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

    public void switchToTab(int index) {
        if (binding == null) return;
        int safeIndex = Math.max(0, Math.min(index, binding.bottomNavigationLnm.getItemsCount() - 1));
        binding.lnmVp.setCurrentItem(safeIndex, false);
        binding.bottomNavigationLnm.setCurrentItem(safeIndex);
    }


    public void showMsg() {
        if (SPUtils.getInstance().getInt(PM_TC, 0) == 0 && (RomUtils.isXiaomi() || RomUtils.isVivo())) {

            String showCalculateMsgTitle
                    = "注意";
            String showCalculateMsg
                    = "小米手机需要\n" +
                    "    1. 后台弹出界面权限（小米手机权限设置中）\n" +
                    "    2. 后台保护（vivo手机，最近程序加上“锁”）\n" +
                    "    3. 自启权限（大多手机都需要，一般在设置里打开）" +
                    "请自行开启";

            new MessageDialog.Builder(LearnNoMobileActivity.this)
                    .setTitle(showCalculateMsgTitle)
                    .setMessage(showCalculateMsg)
                    .setCancelable(false)
                    .setConfirm("“后台弹出界面”权限")
                    .setCancel("取消")
                    .setListener(dialog1 -> {

                        if (RomUtils.isXiaomi()) {
                            Jump();
                        } else {
                            launchAppDetailsSettings();
                        }

                        SPUtils.getInstance().put(PM_TC, 1);
                    }).show();
        }
    }


    private void Jump() {
        try {
            // MIUI 8
            Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            localIntent.putExtra("extra_pkgname", getPackageName());
            startActivity(localIntent);

        } catch (Exception e) {
            try {
                // MIUI 5/6/7
                Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                localIntent.putExtra("extra_pkgname", getPackageName());
                startActivity(localIntent);
            } catch (Exception e1) {
                // 否则跳转到应用详情
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }
        toastSl("打开权限管理，找到“后台弹出界面”");

    }

    private void showParentTodayDialog() {
        ParentTodayReport.TodayStats stats = ParentTodayReport.buildTodayStats();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("今日快报")
                .setMessage(ParentTodayReport.buildDetailLines(stats))
                .setPositiveButton("知道了", null)
                .setNeutralButton("一键确认", (d, w) -> {
                    int confirmed = ParentTodayReport.confirmPendingTodos();
                    if (confirmed <= 0) {
                        SimToast.toastEL("暂无待确认作业");
                    } else {
                        SimToast.toastSe("已确认 " + confirmed + " 项作业");
                    }
                })
                .create();
        dialog.setOnShowListener(d -> {
            if (stats.pendingConfirm <= 0) {
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setAlpha(0.5f);
            }
        });
        dialog.show();
    }

}
