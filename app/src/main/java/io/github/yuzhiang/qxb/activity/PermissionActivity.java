package io.github.yuzhiang.qxb.activity;

import static com.blankj.utilcode.util.AppUtils.launchAppDetailsSettings;
import static io.github.yuzhiang.qxb.model.lnm2file.clearLnmLogs;
import static io.github.yuzhiang.qxb.model.lnm2file.errorSpan;
import static io.github.yuzhiang.qxb.model.lnm2file.getLnmLogs;
import static io.github.yuzhiang.qxb.model.lnm2file.okSpan;
import static io.github.yuzhiang.qxb.common.LdrConfig.lnmLogsTitle;
import static autodispose2.AutoDispose.autoDisposable;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.RomUtils;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;
import com.hjq.permissions.permission.base.IPermission;
import com.hjq.permissions.permission.special.NotificationServicePermission;
import com.hjq.permissions.permission.special.RequestIgnoreBatteryOptimizationsPermission;
import com.hjq.permissions.permission.special.SystemAlertWindowPermission;

import io.github.yuzhiang.qxb.model.LnmPermission;
import io.github.yuzhiang.qxb.adapter.RVALnmPm;
import io.github.yuzhiang.qxb.R;
import io.github.yuzhiang.qxb.Service.DetectService;
import io.github.yuzhiang.qxb.base.BaseActivity;
import io.github.yuzhiang.qxb.base.BaseDialog;
import io.github.yuzhiang.qxb.databinding.ActivityPermissionBinding;
import io.github.yuzhiang.qxb.view.dialog.MessageDialog;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PermissionActivity extends BaseActivity {


    RVALnmPm rvaLnmPm;

    long spanTime = 0;
    //    无障碍
    private final int WZA = 0;
    //    关闭电池优化
    private final int DC_YH = 1;
    //    通知
    private final int TZ = 2;
    //    自启
    private final int ZQ = 3;
    //    悬浮窗
    private final int XFC = 4;

    //    后台弹出
    private final int HT_TC = 5;
    //    后台保护
    private final int HT_BH = 6;
    //    核对时间
    private final int HD_SJ = 7;
    //    悬浮按钮
    private final int XF_AN = 8;


    ActivityPermissionBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPermissionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setImmersiveView(binding.rlLnmPm);

        binding.srlLnmPm.setOnRefreshListener(refreshLayout -> {
            rvaLnmPm.submitList(getPm());
            binding.srlLnmPm.finishRefresh();

        });

        binding.tvLnmShowLogs.setOnClickListener(v -> {
            if (binding.tvLnmLogs.getVisibility() == View.GONE) {
                binding.tvLnmShowLogs.setText("隐藏拦截日志");
                binding.tvLnmLogs.setVisibility(View.VISIBLE);
                String logs = getLnmLogs();
                if (logs.isEmpty()) {
                    logs = "还没有拦截日志 ~";
                }
                binding.tvLnmLogs.setText(lnmLogsTitle + logs);

            } else if (binding.tvLnmLogs.getVisibility() == View.VISIBLE) {
                binding.tvLnmShowLogs.setText("查看拦截日志");
                binding.tvLnmLogs.setVisibility(View.GONE);

            }
        });

        binding.tvLnmLogs.setOnLongClickListener(v -> {
            new MessageDialog.Builder(PermissionActivity.this)
                    .setTitle("注意！")
                    .setMessage("是否删除兰朵模式的拦截日志！")
                    .setTextGravity(Gravity.CENTER)
                    .setConfirm("删除")
                    .setCancel("取消")
                    .setCancelable(false)
                    .setListener(dialog -> {
                        clearLnmLogs();
                        String logs = getLnmLogs();
                        if (logs.isEmpty()) {
                            logs = "还没有拦截日志 ~";
                        }
                        binding.tvLnmLogs.setText(lnmLogsTitle + logs);
                    }).show();

            return true;
        });

        binding.rvPm.setLayoutManager(new LinearLayoutManager(this));
        rvaLnmPm = new RVALnmPm(new ArrayList<>());

        View header = LayoutInflater.from(this).inflate(R.layout.rv_empty_view, binding.rvPm, false);
        TextView tv = header.findViewById(R.id.tv_empty_msg);
        tv.setText("加载中……");
        rvaLnmPm.setStateView(header);
        rvaLnmPm.setStateViewEnable(true);
        rvaLnmPm.setOnItemClickListener((adapter, view1, position) -> {
            LnmPermission pm = (LnmPermission) adapter.getItems().get(position);
            switch (pm.getId()) {
                case WZA:
                    toastIL("点击“已下载应用”，找到轻学伴并开启");
                    getAS();

                    break;

                case DC_YH:
                    checkPM(new RequestIgnoreBatteryOptimizationsPermission());

                    break;

                case TZ:
                    checkPM(new NotificationServicePermission());
                    break;

                case ZQ:
                    launchAppDetailsSettings();
                    break;
                case HT_TC:
                    Jump();
                    break;

                case XFC:
                    checkPM(new SystemAlertWindowPermission());
                    break;
                case HT_BH:
                    new MessageDialog.Builder(PermissionActivity.this).setTitle("后台保护")
                            .setMessage("小米、华为：点菜单键（或者全面屏手势 底部上滑、中间停住）；然后，长按软件，将小锁加上\n\n" +
                                    "vivo：打开控制中心，然后把应用图标下滑，点击加锁")
                            .setConfirm("明白了")
                            .setCancel("没听懂")
                            .setListener(new MessageDialog.OnListener() {
                                @Override
                                public void onConfirm(BaseDialog dialog) {
                                }

                                @Override
                                public void onCancel(BaseDialog dialog) {
                                    toastSl("请自行百度，如：华为手机 如何软件后台保护的小锁，防止被一键清理");
                                }
                            }).show();
                    break;

                case HD_SJ:
                    toastSl("关闭、再重新打开“自动获取网络时间”");
                    startActivity(new Intent().setAction("android.settings.DATE_SETTINGS"));
                    break;

                case XF_AN:
                    toastSl("比如系统菜单悬浮按钮、360清理等悬浮窗");

                    break;

                default:
                    break;
            }


        });
        binding.rvPm.setAdapter(rvaLnmPm);

        binding.tvClosePm.setOnClickListener(v -> finish());

//        getWebsiteDatetime();
    }

    private void checkPM(IPermission permission) {

        XXPermissions.with(this)
                .permission(permission)
                .request(new OnPermissionCallback() {

                    @Override
                    public void onResult(List<IPermission> allGranted, List<IPermission> deniedList) {
                        if (deniedList == null || deniedList.isEmpty()) {
                            toastSs("获取权限成功");
                        } else {
                            toastEs("获取失败");
                        }
                        rvaLnmPm.submitList(getPm());
                    }
                });
    }

    @Override
    protected void onStart() {
        getWebsiteDatetime();
        super.onStart();
    }

    private List<LnmPermission> getPm() {
        List<LnmPermission> permissions = new ArrayList<>();

        LnmPermission pm = new LnmPermission(
                WZA, "无障碍权限",
                "保证学习期间自动跳转回软件(偶尔需要重启手机)",
                R.drawable.logo, 0, "");
        if (isAS()) {
            pm.setPmOk(1);
        } else {
            pm.setPmOk(0);
        }
        permissions.add(pm);

        permissions.add(new LnmPermission(
                HT_TC, "后台界面弹出权限",
                "部分手机必须！如小米、vivo",
                R.drawable.logo, 2, "点我开启"));

        permissions.add(new LnmPermission(HT_BH, "开启后台保护", "部分手机必须！如vivo", R.drawable.logo, 2, "自行开启"));

        LnmPermission pm7 = new LnmPermission(HD_SJ, "核对手机时间", "", R.drawable.logo, 2, "点我开启");

        String s = "";
        if (Math.abs(spanTime) > okSpan) {
            pm7.setPmOk(0);
            if (spanTime == errorSpan) {
                s = "手机时间误差过大，请重新设置";
            } else {
                s = "手机时间与网络时间相差" + spanTime / 1000 + "秒，请重新设置";
            }

        } else {
            s = "手机时间正确";
            pm7.setPmOk(1);
        }
        pm7.setPmOkSum(s);
        pm7.setPmSummary(s);

        permissions.add(pm7);

        permissions.add(new LnmPermission(
                XF_AN, "关闭悬浮按钮",
                "如果白名单app仍然不可用，请关闭所有悬浮按钮",
                R.drawable.logo, 2, ""));

        LnmPermission pm1 = new LnmPermission(DC_YH, "关闭电池优化", "保证软件不被系统异常回收", R.drawable.logo, 2, "点我开启");

        if (XXPermissions.isGrantedPermission(this, new RequestIgnoreBatteryOptimizationsPermission())) {
            pm1.setPmOk(1);
        } else {
            pm1.setPmOk(0);
        }

        permissions.add(pm1);


        LnmPermission pm2 = new LnmPermission(TZ, "通知权限", "保证消息稳定", R.drawable.logo, 2, "点我开启");
        if (XXPermissions.isGrantedPermission(this, new NotificationServicePermission())) {
            pm2.setPmOk(1);
        } else {
            pm2.setPmOk(0);
        }
        permissions.add(pm2);


        permissions.add(new LnmPermission(ZQ, "自启权限", "软件无法得知用户是否开启", R.drawable.logo, 2, "尽可能设置"));


//        LnmPermission pm4 = new LnmPermission();
//        pm4.setId(XFC);
//        pm4.setPmIcon(R.drawable.logo);
//        pm4.setPmName("悬浮窗");
//        pm4.setPmSummary("保证稳定运行");
//        if (isGrantedDrawOverlays()){
//            pm4.setPmOk(1);
//        }else {
//            pm4.setPmOk(0);
//        }
//        permissions.add(pm4);

        LogUtils.i(permissions);

        return permissions;
    }

    public boolean isAS() {
        return DetectService.isAccessibilitySettingsOn(this);
    }


    public void getAS() {

        if (isAS()) {
            toastSl("无障碍权限已经获取\n有时需要手机重启生效");
        } else {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            toastSl("系统设置 - 无障碍 - 更多已下载服务");

        }

    }


    private void Jump() {
        toastSl("打开权限管理，找到“后台弹出界面”");
        if (RomUtils.isXiaomi()) {
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
                    launchAppDetailsSettings();

                }
            }
        } else {
            launchAppDetailsSettings();

        }
    }


    /**
     * 获取指定网站的日期时间
     *
     * @return
     * @author SHANHY
     * @date 2015年11月27日
     */
    private void getWebsiteDatetime() {

        Observable.create((ObservableOnSubscribe<Long>) emitter -> {
                    try {

                        String s = "https://baidu.com";
                        URLConnection uc = new URL(s).openConnection();// 生成连接对象
                        uc.connect();// 发出连接
                        spanTime = uc.getDate() - System.currentTimeMillis();
                        emitter.onNext(spanTime);
                        emitter.onComplete();

                    } catch (IOException e) {
                        spanTime = errorSpan;
                        LogUtils.file("\n\n 时间核对失败\n" + e.toString());
                        LogUtils.i(e.toString());
                        emitter.onError(e);
                        e.printStackTrace();
                    }
                })
                .subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
                .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Long span) {
                    }

                    @Override
                    public void onError(Throwable e) {
                        toastEe("网络有点问题 ~ ");
                        rvaLnmPm.submitList(getPm());
                        binding.srlLnmPm.finishRefresh();
                    }

                    @Override
                    public void onComplete() {
                        rvaLnmPm.submitList(getPm());
                        binding.srlLnmPm.finishRefresh();
                    }
                });

    }

    public static Date getNetTime() {
        //醉了，各个地方的时间都不一样
        String webUrl = "http://www.ntsc.ac.cn";//中国科学院国家授时中心
        try {
            URL url = new URL(webUrl);
            URLConnection uc = url.openConnection();
            uc.setReadTimeout(5000);
            uc.setConnectTimeout(5000);
            uc.connect();
            long correctTime = uc.getDate();
            Date date = new Date(correctTime);
            long my = System.currentTimeMillis();
            Date myDate = new Date(my);

            LogUtils.i(date.toString() + "\n" + myDate.toString() + "===" + (correctTime - my));
            return date;
        } catch (Exception e) {
            return new Date();
        }
    }

    public static Date getNetTime2() {
        String webUrl = "https://www.iconfont.cn/";//中国科学院国家授时中心
        try {
            URL url = new URL(webUrl);
            URLConnection uc = url.openConnection();
            uc.setReadTimeout(5000);
            uc.setConnectTimeout(5000);
            uc.connect();
            long correctTime = uc.getDate();
            Date date = new Date(correctTime);
            long my = System.currentTimeMillis();
            Date myDate = new Date(my);

            LogUtils.i(date.toString() + "\n" + myDate.toString() + "===" + (correctTime - my));

            return date;
        } catch (Exception e) {
            return new Date();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 666) rvaLnmPm.submitList(getPm());

        LogUtils.i(requestCode + "=====" + resultCode);
    }
}
