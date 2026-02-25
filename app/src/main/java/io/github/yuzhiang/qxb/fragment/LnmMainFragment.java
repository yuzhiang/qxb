package io.github.yuzhiang.qxb.fragment;

import static com.blankj.utilcode.util.ScreenUtils.getScreenHeight;
import static com.blankj.utilcode.util.ScreenUtils.getScreenWidth;
import static io.github.yuzhiang.qxb.model.lnm2file.clearManualCancelMark;
import static io.github.yuzhiang.qxb.model.lnm2file.errorSpan;
import static io.github.yuzhiang.qxb.model.lnm2file.finishLearn;
import static io.github.yuzhiang.qxb.model.lnm2file.getLastTime;
import static io.github.yuzhiang.qxb.model.lnm2file.getManualCancelAt;
import static io.github.yuzhiang.qxb.model.lnm2file.getThisId;
import static io.github.yuzhiang.qxb.model.lnm2file.okSpan;
import static io.github.yuzhiang.qxb.common.Constant.Constant.lnmBg;
import static io.github.yuzhiang.qxb.common.Constant.Constant.lnmStart;
import static io.github.yuzhiang.qxb.common.Constant.Constant.lnmState;
import static io.github.yuzhiang.qxb.MyUtils.LnmRetrofitUtils.schedulersTransformer;
import static io.github.yuzhiang.qxb.view.pickpic.PicUtils.picSel;
import static io.github.yuzhiang.qxb.view.tastytoast.SimToast.toastEL;
import static io.github.yuzhiang.qxb.view.tastytoast.SimToast.toastSe;
import static autodispose2.AutoDispose.autoDisposable;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;

import io.github.yuzhiang.qxb.MyUtils.StatusBarUtil;
import io.github.yuzhiang.qxb.MyUtils.UsrMsgUtils;
import io.github.yuzhiang.qxb.R;
import io.github.yuzhiang.qxb.Service.DetectService;
import io.github.yuzhiang.qxb.activity.BaiMingDanActivity;
import io.github.yuzhiang.qxb.activity.PermissionActivity;
import io.github.yuzhiang.qxb.activity.StartLearnActivity;
import io.github.yuzhiang.qxb.adapter.EnableAppAdapter;
import io.github.yuzhiang.qxb.base.BaseDialog;
import io.github.yuzhiang.qxb.base.LazyFragment;
import io.github.yuzhiang.qxb.databinding.LnmFragmentMainBinding;
import io.github.yuzhiang.qxb.db.room.bean.Lnm;
import io.github.yuzhiang.qxb.db.room.dbUtils.lnmDBUtils;
import io.github.yuzhiang.qxb.model.LnmApp;
import io.github.yuzhiang.qxb.model.eventbus.EBLnmBai;
import io.github.yuzhiang.qxb.model.eventbus.MeLnmShowChart;
import io.github.yuzhiang.qxb.model.lnm2file;
import io.github.yuzhiang.qxb.view.dialog.MessageDialog;
import io.github.yuzhiang.qxb.view.pickpic.ImageCropEngine;
import io.github.yuzhiang.qxb.view.pickpic.ImageFileCompressEngine;
import io.github.yuzhiang.qxb.view.pickpic.PicUtils;
import io.github.yuzhiang.qxb.view.tastytoast.SimToast;
import io.github.yuzhiang.qxb.view.tastytoast.TastyToast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;


public class LnmMainFragment extends LazyFragment {
    private static final long RESUME_PROMPT_WINDOW_MS = 30 * 1000L;
    private static final long MANUAL_CANCEL_SUPPRESS_RESUME_MS = 10 * 1000L;

    private Context mContext;

    private LnmFragmentMainBinding binding;
    private EnableAppAdapter enableAppAdapter;
    private long spanTime = 0;

    public static LnmMainFragment newInstance() {
        return new LnmMainFragment();
    }

    @Override
    protected int getContentViewId() {
        return R.layout.lnm_fragment_main;
    }


    /**
     * 初始化视图
     *
     * @param view
     */
    @Override
    protected void initView(View view) {
        super.initView(view);

        LogUtils.i("lnmMain======initView");

        mContext = getContext();

        binding = LnmFragmentMainBinding.bind(view);
        StatusBarUtil.setPaddingSmart(mContext, binding.lnmTitle);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        binding.rvLnmMain.setLayoutManager(layoutManager);

        enableAppAdapter = new EnableAppAdapter(new ArrayList<>());
        binding.rvLnmMain.setAdapter(enableAppAdapter);
        //设置监听

    }

    @Override
    protected void initEvent() {
        super.initEvent();


        enableAppAdapter.setOnItemClickListener((adapter, view, position) -> {

            try {
                String appPackageName = enableAppAdapter.getItems().get(position).getAppPackageName();
                LogUtils.i(appPackageName);
                Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(appPackageName);
                if (intent != null) {
                    startActivity(intent);
                } else {
                    SimToast.toastEe("打开错误，软件不存在：" + AppUtils.getAppName(appPackageName));
                }
            } catch (Exception e) {
                LogUtils.i(e.toString());
                e.printStackTrace();
            }

        });

        binding.tvCheckPm.setOnClickListener(v -> startActivity(new Intent(mContext, PermissionActivity.class)));

        binding.tvLnmPic.setOnClickListener(v -> {


            Bitmap bitmap = ImageUtils.getBitmap(lnmBg);

            if (bitmap != null) {

                new MessageDialog.Builder(mContext)
                        .setTitle("注意！")
                        .setMessage("发现用户已经更换过背景，你是准备？")
                        .setTextGravity(Gravity.CENTER)
                        .setConfirm("更换背景图")
                        .setCancel("恢复默认背景图")
                        .setListener(new MessageDialog.OnListener() {
                            @Override
                            public void onConfirm(BaseDialog dialog) {
                                getBackGround();
                            }

                            @Override
                            public void onCancel(BaseDialog dialog) {
                                binding.clLnmMain.setBackground(ContextCompat.getDrawable(mContext, R.mipmap.table_bg));
                            }
                        })
                        .show();

            } else {
                getBackGround();
            }

        });

        binding.lnmTitle.setOnClickListener(view -> SimToast.toastEe("请在我的界面修改个性签名"));


        binding.tvStartLnm.setOnClickListener(v -> {
//            if (noNickName.equals(UsrMsgUtils.getNickName())) {
//                SimToast.toastEL("未设置“昵称”，也可能是注册失败，请前往“我的”查看");
//                startActivity(new Intent(mContext, MangeUserActivity.class));
//                return;
//            }

            long id = getThisId();
            if (id > 0) {
                if (lnmDBUtils.countById((int) id) <= 0) {
                    finishLearn();
                    clearManualCancelMark();
                    id = -1;
                } else if (shouldResumeLearn()) {
                    showResumeLearnDialog();
                    return;
                } else {
                    findLastLearn(id);
                    return;
                }
            }

            //辅助权限，手机时间都没问题
            LogUtils.i(DetectService.isAccessibilitySettingsOn(mContext), Math.abs(spanTime) <= okSpan);
            if (DetectService.isAccessibilitySettingsOn(mContext) && Math.abs(spanTime) <= okSpan) {

                new TimePickerDialog(mContext, (view, hourOfDay, minute) -> {

                    long inputTime = hourOfDay * 60 * 60 * 1000 + minute * 60 * 1000;
                    if (inputTime > 4 * 60 * 60 * 1000) {

                        new MessageDialog.Builder(mContext)
                                .setTitle("警告！")
                                .setMessage("你确定要一动不动的学习，超过4个小时？")
                                .setConfirm("确定")
                                .setCancel("点错了")
                                .setCancelable(false)
                                .setListener(new MessageDialog.OnListener() {
                                    @Override
                                    public void onConfirm(BaseDialog dialog) {
                                        SimToast.toastEe("确定也不行…");

                                    }

                                    @Override
                                    public void onCancel(BaseDialog dialog) {
                                        SimToast.toastSe("重新选择吧…");
                                    }
                                }).show();

                    } else {

                        if (UsrMsgUtils.getSignature().isEmpty()) {
                            TastyToast.makeText(mContext, "记得在“我的”界面修改个人签名", TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                        }

                        lnm2file.saveTime(hourOfDay, minute);
                        lnm2file.savePlanSpan(inputTime);


                        Intent intent = new Intent(mContext, StartLearnActivity.class);
                        intent.putExtra(lnmState, lnmStart);
                        startActivity(intent);

                    }

                }, lnm2file.getHourOfDay(), lnm2file.getMinute(), true)
                        .show();

            } else {
                SimToast.toastSL("请先授予相关的权限");
                startActivity(new Intent(mContext, PermissionActivity.class));

            }
        });

        binding.tvBai.setOnClickListener(v -> startActivity(new Intent(mContext, BaiMingDanActivity.class)));


    }

    /**
     * 初始化数据
     */
    @Override
    protected void initData() {
        super.initData();
        EventBus.getDefault().register(this);

        LogUtils.i("lnm==========register");

        setBackGround();

        binding.lnmTitle.setText(UsrMsgUtils.getSignature());
        updateApps();
    }

    private void setBackGround() {

        Observable.create((ObservableOnSubscribe<Drawable>) emitter -> {
                    if (FileUtils.isFile(lnmBg)) {
                        try {
                            emitter.onNext(new BitmapDrawable(getResources(), ImageUtils.getBitmap(lnmBg)));
                        } catch (Exception e) {
                            emitter.onNext(ContextCompat.getDrawable(mContext, R.mipmap.table_bg));
                            e.printStackTrace();
                        }
                    } else {
                        emitter.onNext(ContextCompat.getDrawable(mContext, R.mipmap.table_bg));
                    }
                    emitter.onComplete();

                })
                .compose(schedulersTransformer())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(new Observer<Drawable>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Drawable drawable) {
                        binding.clLnmMain.setBackground(drawable);
                    }

                    @Override
                    public void onError(Throwable e) {
                        SimToast.toastEe("背景设置错误！");
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }


    private void getBackGround() {

        final int width = getScreenWidth();//宽度
        int height = getScreenHeight();//高度

        picSel().setCropEngine(new ImageCropEngine(width, height))
                .setCompressEngine(new ImageFileCompressEngine())
                .forResult(new OnResultCallbackListener<>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        if (result.size() == 1) {
                            FileUtils.copy(PicUtils.getRealLocalPath(result.get(0)), lnmBg);
                            setBackGround();
                        } else {
                            SimToast.toastEe("请重新选择：图片数目错误" + result.size());
                        }
                    }

                    @Override
                    public void onCancel() {
                        // 取消
                    }
                });

    }


    private void updateApps() {

        List<String> stringList = lnm2file.getEnableApp();

        LogUtils.i("===========" + stringList.toString());
        List<LnmApp> lnmApps = new ArrayList<>();

        for (String packageName : stringList) {
            lnmApps.add(new LnmApp(AppUtils.getAppName(packageName), AppUtils.getAppIcon(packageName), packageName));
        }
        enableAppAdapter.submitList(lnmApps);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EBLnmBai ebLnmBai) {

        if (ebLnmBai.isOk()) updateApps();
    }

    @Override
    public void onStart() {
        super.onStart();
        getWebsiteDatetime();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        LogUtils.i("lnm==========unregister");
    }


    private void getWebsiteDatetime() {

        new Thread() {
            @Override
            public void run() {//不进行异步，卡顿厉害
                try {
//                    getNetTime();
//                    getNetTime2();
                    String s = "https://baidu.com";
                    URLConnection uc = new URL(s).openConnection();// 生成连接对象
                    uc.connect();// 发出连接
                    spanTime = uc.getDate() - System.currentTimeMillis();

                    LogUtils.file("轻学伴时间 - 手机时间 = " + spanTime);

                } catch (IOException e) {
                    spanTime = errorSpan;
                    LogUtils.file("\n\n 时间核对失败\n" + e.toString());
                    LogUtils.i(e.toString());
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private void findLastLearn(long id) {

        Date plan = lnm2file.getPlanTime();
        Date start = lnm2file.getStartTime();

        if (plan == null || start == null) {
            new AlertDialog.Builder(mContext, R.style.MyAlertDialog)
                    .setMessage("检测到残留学习状态，已无法恢复，本次记录将清除。")
                    .setCancelable(false)
                    .setTitle("注意！")
                    .setNegativeButton("清除", (d, i) -> {
                        cancelLastLearn(id);
                    }).create().show();

            return;
        }

        String s = "发现有以前的本地学习数据未处理！\n\n";
        double time = Math.ceil(TimeUtils.getTimeSpan(plan, start, TimeConstants.SEC) / 60.0);
        s = s + "上次计划学习约" + time + "分钟，未在中断后及时返回。\n将记为学习失败（用于统计完成率）。";

        new AlertDialog.Builder(mContext, R.style.MyAlertDialog)
                .setMessage(s)
                .setCancelable(false)
                .setTitle("注意！")
                .setNegativeButton("记为失败", (d, i) -> {

                    LogUtils.file("\n\n处理上次本地学习记录 ~ ");
                    LogUtils.i("处理上次本地学习记录 ~ ");
                    failLastLearn(id);
                }).create().show();

    }

    private boolean shouldResumeLearn() {
        long id = getThisId();
        Date last = getLastTime();
        long manualCancelAt = getManualCancelAt();
        boolean recentInterrupted = last != null
                && System.currentTimeMillis() - last.getTime() <= RESUME_PROMPT_WINDOW_MS;
        boolean justManuallyCancelled = manualCancelAt > 0
                && System.currentTimeMillis() - manualCancelAt <= MANUAL_CANCEL_SUPPRESS_RESUME_MS;
        return id > 0
                && recentInterrupted
                && !justManuallyCancelled
                && lnmDBUtils.countPendingById((int) id) > 0;
    }

    private void showResumeLearnDialog() {
        new AlertDialog.Builder(mContext, R.style.MyAlertDialog)
                .setTitle("继续学习")
                .setMessage("检测到上次学习仍在进行中，是否继续？")
                .setCancelable(false)
                .setPositiveButton("继续", (d, i) -> {
                    Intent intent = new Intent(mContext, StartLearnActivity.class);
                    intent.putExtra(lnmState, lnmStart);
                    startActivity(intent);
                })
                .setNegativeButton("放弃本次", (d, i) -> {
                    long id = getThisId();
                    if (id > 0) failLastLearn(id);
                })
                .create().show();
    }

    private void finishLastLearn(long id) {
        Date start = lnm2file.getStartTime();
        Date plan = lnm2file.getPlanTime();
        if (start == null || plan == null) {
            toastEL("本地记录缺失，无法恢复");
            cancelLastLearn(id);
            return;
        }

        Lnm lnm = new Lnm();
        lnm.id = (int) id;
        lnm.createdDate = start;
        lnm.schedule = plan;
        lnm.endTime = new Date();
        lnm.finish = true;
        lnmDBUtils.insert(lnm);
        EventBus.getDefault().post(new MeLnmShowChart(true));

        LogUtils.file("\n\n上次学习记录本地保存成功！");
        toastSe("上次学习记录已保存到本地！\n可以开始学习");
        finishLearn();

    }

    private void cancelLastLearn(long id) {
        lnmDBUtils.deleteById((int) id);
        toastSe("成功！\n可以开始学习");
        finishLearn();

        try {
            LogUtils.file("\n\n上次学习取消(本地)：id-" + id);
        } catch (Exception e) {
            toastSe("错误！请重试");
            LogUtils.file("\n\n上次学习取消(本地)：记录错误" + e);
            e.printStackTrace();
        }
    }

    private void failLastLearn(long id) {
        Date start = lnm2file.getStartTime();
        Date plan = lnm2file.getPlanTime();
        if (start == null || plan == null) {
            toastEL("本地记录缺失，无法标记失败");
            cancelLastLearn(id);
            return;
        }

        Date last = lnm2file.getLastTime();
        Date end = last == null ? new Date() : last;
        if (end.before(start)) end = start;

        Lnm lnm = new Lnm();
        lnm.id = (int) id;
        lnm.createdDate = start;
        lnm.schedule = plan;
        lnm.endTime = end;
        lnm.finish = false;
        lnmDBUtils.insert(lnm);
        EventBus.getDefault().post(new MeLnmShowChart(true));

        LogUtils.file("\n\n上次学习记录已记为失败：id-" + id);
        toastSe("上次中断学习已记为失败\n可以开始学习");
        finishLearn();
    }

}
