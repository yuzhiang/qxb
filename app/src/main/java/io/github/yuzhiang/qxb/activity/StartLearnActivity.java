package io.github.yuzhiang.qxb.activity;

import static io.github.yuzhiang.qxb.model.lnm2file.finishLearn;
import static io.github.yuzhiang.qxb.model.lnm2file.clearManualCancelMark;
import static io.github.yuzhiang.qxb.model.lnm2file.getLearning;
import static io.github.yuzhiang.qxb.model.lnm2file.getSpanTime;
import static io.github.yuzhiang.qxb.model.lnm2file.getThisId;
import static io.github.yuzhiang.qxb.model.lnm2file.markManualCancelNow;
import static io.github.yuzhiang.qxb.model.lnm2file.okSpan;
import static io.github.yuzhiang.qxb.model.lnm2file.saveLnmTime;
import static io.github.yuzhiang.qxb.model.lnm2file.saveNowTime;
import static io.github.yuzhiang.qxb.model.lnm2file.saveThisId;
import static io.github.yuzhiang.qxb.common.Constant.Constant.lnmBg;
import static io.github.yuzhiang.qxb.common.Constant.Constant.lnmStart;
import static io.github.yuzhiang.qxb.common.Constant.Constant.lnmState;
import static io.github.yuzhiang.qxb.view.tastytoast.SimToast.toastEL;
import static io.github.yuzhiang.qxb.view.tastytoast.SimToast.toastEe;
import static io.github.yuzhiang.qxb.view.tastytoast.SimToast.toastSL;
import static autodispose2.AutoDispose.autoDisposable;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.VibrateUtils;
import com.google.android.material.snackbar.Snackbar;

import io.github.yuzhiang.qxb.model.lnm2file;
import io.github.yuzhiang.qxb.MyUtils.StatusBarUtil;
import io.github.yuzhiang.qxb.MyUtils.UsrMsgUtils;
import io.github.yuzhiang.qxb.R;
import io.github.yuzhiang.qxb.Service.DetectService;
import io.github.yuzhiang.qxb.adapter.EnableAppAdapter;
import io.github.yuzhiang.qxb.base.BaseDialog;
import io.github.yuzhiang.qxb.common.Constant.Constant;
import io.github.yuzhiang.qxb.databinding.ActivityStartLearnBinding;
import io.github.yuzhiang.qxb.db.room.bean.Lnm;
import io.github.yuzhiang.qxb.db.room.dbUtils.lnmDBUtils;
import io.github.yuzhiang.qxb.model.StudyProjectRecord;
import io.github.yuzhiang.qxb.model.LnmApp;
import io.github.yuzhiang.qxb.model.eventbus.MeLnmShowChart;
import io.github.yuzhiang.qxb.view.dialog.MessageDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class StartLearnActivity extends AppCompatActivity {

    private static final long START_CANCEL_GRACE_MS = 10_000L;
    private static final int CAL_ACTION_NONE = 0;
    private static final int CAL_ACTION_CANCEL = 1;
    private static final int CAL_ACTION_RECORD = 2;

    Disposable rxTimer;

    private Date planTime;
    private Date startTime;

    int re = 0;

    private ActivityStartLearnBinding binding;
    EnableAppAdapter enableAppAdapter;

    boolean showCal = true;
    private int calAction = CAL_ACTION_NONE;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        LogUtils.i("恢复");

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStartLearnBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.tvStartLearnCal.setOnClickListener(v -> onCalButtonClick());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getLearning()) {
                    ShowDialog();
                    return;
                }
                setEnabled(false);
                StartLearnActivity.this.getOnBackPressedDispatcher().onBackPressed();
            }
        });
        StatusBarUtil.immersive(this);

        hideBottomUIMenu();

        LogUtils.i("=====开始");

/*      与预期行为不同
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Snackbar snackbar = Snackbar
                    .make(binding.rlStartLearn, "请在10秒内退出分屏模式否则将视为学习失败", Snackbar.LENGTH_INDEFINITE )
                    .setAction("立即结束学习", v1 -> postFinishLearn());

            if (isInMultiWindowMode() && getLearning()) {
                snackbar.show();
                MultiWindowModeWarning();
                if(!isInMultiWindowMode()){
                    snackbar.dismiss();
                }
            }
        }*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (isInMultiWindowMode() && getLearning()) {
                Snackbar
                        .make(binding.rlStartLearn, "请在10秒内退出分屏模式否则将视为学习失败", Snackbar.LENGTH_INDEFINITE)
                        .setAction("结束学习", v1 -> postUserStopLearn())
                        .setDuration((int) TimeUnit.SECONDS.toMillis(10))
                        .show();
                MultiWindowModeWarning();
            }
        }

        switch (getIntent().getIntExtra(lnmState, lnmStart)) {
            case Constant.lnmFinish:
                LogUtils.i("删除");
                postFinishLearn();
                break;

            case Constant.lnmCancel:
                LogUtils.i("取消");
                postCancelLearn();
                break;

            case lnmStart:
                LogUtils.i("开始");
                postStartLearn();

                break;
            case Constant.lnmDetectUnbind:

                openDetect();

                LogUtils.i("无障碍权限被关闭");

                break;

            default:
                allFinish();
                LogUtils.i("莫名其妙");

                break;

        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        binding.rvLnmStartLearn.setLayoutManager(layoutManager);
        enableAppAdapter = new EnableAppAdapter(new ArrayList<>());
        binding.rvLnmStartLearn.setAdapter(enableAppAdapter);

        enableAppAdapter.setOnItemClickListener((adapter, view, position) -> {
            try {
                String appPackageName = enableAppAdapter.getItems().get(position).getAppPackageName();
                LogUtils.i(appPackageName);
                Intent intent = getPackageManager().getLaunchIntentForPackage(appPackageName);
                if (intent != null) {
                    startActivity(intent);
                } else {
                    toastEe("打开错误，软件不存在：" + AppUtils.getAppName(appPackageName));
                }
            } catch (Exception e) {
                LogUtils.i(e.toString());
                e.printStackTrace();
            }
        });
        setBackGround();

        updateApp();

        re = 0;

    }

    private void MultiWindowModeWarning() {

        CountDownTimer timer = new CountDownTimer(10 * 1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (isInMultiWindowMode()) {
                        postFinishLearn();
                    }
                }
            }
        }.start();
    }


    private void setBackGround() {

        Observable.create((ObservableOnSubscribe<Drawable>) emitter -> {
                    if (FileUtils.isFile(lnmBg)) {
                        try {
                            emitter.onNext(new BitmapDrawable(getResources(), ImageUtils.getBitmap(lnmBg)));
                        } catch (Exception e) {
                            emitter.onNext(ContextCompat.getDrawable(this, R.mipmap.table_bg));
                            e.printStackTrace();
                        }
                    } else {
                        emitter.onNext(ContextCompat.getDrawable(this, R.mipmap.table_bg));
                    }
                    emitter.onComplete();

                })
                .subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
                .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(new Observer<Drawable>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Drawable drawable) {
                        binding.rlStartLearn.setBackground(drawable);
                    }

                    @Override
                    public void onError(Throwable e) {
                        toastEe("背景设置错误！");
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if ((keyCode == KeyEvent.KEYCODE_BACK || isInMultiWindowMode() || keyCode == KeyEvent.KEYCODE_HOME) && getLearning()) {

                ShowDialog();
                return true;

            }
        } else {
            if ((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) && getLearning()) {

                ShowDialog();
                return true;

            }
        }
        return super.onKeyDown(keyCode, event);

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && getLearning()) {
            int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_APP_SWITCH) {
                ShowDialog();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }


    public void ShowDialog() {
        new MessageDialog.Builder(StartLearnActivity.this)
                .setTitle("学习去！")
                .setMessage("好好学习去,不准玩手机")
                .setTextGravity(Gravity.CENTER)
                .setConfirm("默默去学习")
                .setCancel("投降了")
                .setCancelable(false)
                .setListener(new MessageDialog.OnListener() {
                    @Override
                    public void onConfirm(BaseDialog dialog) {
                    }

                    @Override
                    public void onCancel(BaseDialog dialog) {
                        Snackbar.make(binding.rlStartLearn,
                                        canCancelAsNoRecord() ? "将取消本次学习" : "将视为失败！！",
                                        Snackbar.LENGTH_LONG)
                                .setAction("确定", v1 -> postUserStopLearn())
                                .show();
                    }
                }).show();

    }

    private void WaveLoadingPaintColor(int top, int center, int bottom, int wave, int border) {

        binding.wlStartLearn.setTopTitleColor(ContextCompat.getColor(StartLearnActivity.this, top));
        binding.wlStartLearn.setCenterTitleColor(ContextCompat.getColor(StartLearnActivity.this, center));
        binding.wlStartLearn.setBottomTitleColor(ContextCompat.getColor(StartLearnActivity.this, bottom));
        binding.wlStartLearn.setWaveColor(ContextCompat.getColor(StartLearnActivity.this, wave));
        binding.wlStartLearn.setBorderColor(ContextCompat.getColor(StartLearnActivity.this, border));

    }

    private void openDetect() {
        runOnUiThread(() -> {
            VibrateUtils.vibrate(500);

            String s = "无障碍权限被关闭！无法继续拦截\n\n请在30s内重新开启，否则此次学习失败，学习时间将会被扣除！" +
                    "\n\n如果一次学习中不断出现该问题，可能是因为没有给予“自启权限、后台保护防止清理权限”";

            new AlertDialog.Builder(StartLearnActivity.this, R.style.MyAlertDialog)
                    .setTitle("权限缺失！")
                    .setMessage(s)
                    .setCancelable(false)
                    .setPositiveButton("重新开启", (dialog1, which) -> {

                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                        AppUtils.exitApp();
                    })
                    .setNegativeButton("不学了", (dialog1, which) -> {
                        postCancelLearn();
                    }).create().show();

            saveNowTime(new Date());

        });

    }

    private void allFinish() {

        if (enableAppAdapter != null)
            enableAppAdapter.submitList(new ArrayList<>());

        binding.tvStartLearnCal.setVisibility(View.VISIBLE);
        binding.tvStartLearnCalMsg.setVisibility(View.VISIBLE);
        binding.tvStartLearnCal.setClickable(true);

        binding.tvStartLearnCal.setText("查看努力记录");
        binding.tvStartLearnCalMsg.setText("让努力不只以成绩衡量，还有付出的精力与时间");
        calAction = CAL_ACTION_RECORD;
    }

    private void postStartLearn() {

        long plan_span = lnm2file.getPlanSpan();
        if (plan_span < 50 * 1000) {//有可能是界面重新加载了
            allFinish();
            return;
        }

        long lastId = getThisId();

        if (lastId > 0) {
            LogUtils.i("继续上次的");
            showCal = false;
            planTime = lnm2file.getPlanTime();
            startTime = lnm2file.getStartTime();

            LearnStart();

        } else {
            LogUtils.i("本地开始");
            planTime = new Date(System.currentTimeMillis() + plan_span);
            startTime = new Date();

            int localId = newLocalLnmId();
            Lnm lnm = buildLocalLnm(localId, startTime, planTime, startTime, false);
            lnmDBUtils.insert(lnm);
            clearManualCancelMark();

            showCal = true;

            saveThisId(localId);

            try {
                String s = "\n\n学习开始(本地)：id-" + lnm.id + lnm.finish +
                        "\n    开始-" + TimeUtils.date2String(lnm.createdDate) +
                        "\n    计划-" + TimeUtils.date2String(lnm.schedule);
                LogUtils.file(s);
                LogUtils.i(s);
            } catch (Exception e) {
                LogUtils.file("\n\n学习开始(本地)：记录错误" + e);
                e.printStackTrace();
            }

            toastSL("开始学习");
            LearnStart();
        }
    }

    private void postFinishLearn() {
        timeOff();
        long id = getThisId();

        LogUtils.file("\n\n尝试学习结束 ~ " + id);
        LogUtils.i("尝试学习结束 ~ " + id);
        binding.wlStartLearn.setCenterTitle("学习结束");
        binding.wlStartLearn.setBottomTitle("数据保存中");

        if (id > 0) {
            Date localStart = startTime != null ? startTime : lnm2file.getStartTime();
            Date localPlan = planTime != null ? planTime : lnm2file.getPlanTime();
            Date localEnd = new Date();

            if (localStart == null || localPlan == null) {
                toastEL("本地学习记录异常，无法结束");
                cancel();
                finish();
                return;
            }

            if (TimeUtils.getTimeSpan(localEnd, localPlan, TimeConstants.MIN) > 20) {
                toastEL("目前与计划结束时间相差超过20分钟，此次学习被删除！");
                postCancelLearn(id);
                return;
            }

            boolean success = localEnd.getTime() + okSpan >= localPlan.getTime();
            Lnm lnm = buildLocalLnm((int) id, localStart, localPlan, localEnd, success);
            lnmDBUtils.insert(lnm);
            String project = lnm2file.getSelectedStudyProject();
            if (project == null || project.trim().isEmpty()) {
                project = "未设置";
            }
            long durationMs = Math.max(0, localEnd.getTime() - localStart.getTime());
            lnm2file.addStudyProjectRecord(new StudyProjectRecord(
                    project,
                    localStart.getTime(),
                    localEnd.getTime(),
                    durationMs,
                    success
            ));

            try {
                LogUtils.file("\n\n学习结束(本地)：id-" + lnm.id + lnm.finish +
                        "\n    开始-" + TimeUtils.date2String(lnm.createdDate) +
                        "\n    结束-" + TimeUtils.date2String(lnm.endTime) +
                        "\n    计划-" + TimeUtils.date2String(lnm.schedule));
                LogUtils.i("\n\n学习结束(本地)：id-" + lnm.id + lnm.finish +
                        "\n    开始-" + TimeUtils.date2String(lnm.createdDate) +
                        "\n    结束-" + TimeUtils.date2String(lnm.endTime) +
                        "\n    计划-" + TimeUtils.date2String(lnm.schedule));
            } catch (Exception e) {
                LogUtils.file("\n\n学习结束(本地)：记录错误" + e);
                e.printStackTrace();
            }

            binding.wlStartLearn.setBottomTitle("数据保存成功");
            LearnFinish(lnm.finish);
            allFinish();
        }
    }


    private void postCancelLearn() {
        long id = getThisId();
        postCancelLearn(id);

    }

    private void postCancelLearn(long id) {

        binding.wlStartLearn.setCenterTitle("学习取消");
        binding.wlStartLearn.setBottomTitle("数据保存中");

        LogUtils.i("取消：" + id);
        markManualCancelNow();
        finishLearn(); // 先清理进行中状态，避免返回主页时误判为“继续学习”

        if (id > 0) {
            lnmDBUtils.deleteById((int) id);
            lnmDBUtils.deletePendingAll();
            timeOff();

            try {
                LogUtils.file("\n\n学习取消(本地)：id-" + id);
            } catch (Exception e) {
                LogUtils.file("\n\n学习取消(本地)：记录错误" + e);
                e.printStackTrace();
            }

            toastSL("取消学习！");
            binding.wlStartLearn.setBottomTitle("数据保存成功");
            finish();
        } else {
            lnmDBUtils.deletePendingAll();
            timeOff();
            finish();
        }

    }

    private void LearnFinish(boolean success) {

        if (success) {

            binding.tvStartLearnMsg.setText("真棒！学习结束");

            toastSL("学习成功！");

        } else {
            toastEL("学习失败！");

            binding.tvStartLearnMsg.setText("学习失败");

        }


        binding.wlStartLearn.setCenterTitle("学习结束");
        binding.wlStartLearn.setProgressValue(0);
        WaveLoadingPaintColor(UsrMsgUtils.getThemeColor(), UsrMsgUtils.getThemeColor(), UsrMsgUtils.getThemeColor(), UsrMsgUtils.getThemeColor(), UsrMsgUtils.getThemeColor());

        //振动
        VibrateUtils.vibrate(200);


        binding.tvStartLearnCal.setVisibility(View.GONE);
        binding.tvStartLearnCalMsg.setVisibility(View.GONE);


        EventBus.getDefault().post(new MeLnmShowChart(true));


        cancel();

    }

    //    不然会出现，锁屏停止
    @SuppressLint("AutoDispose")
    private void LearnStart() {

        saveLnmTime(startTime, planTime, new Date());

        if (showCal) {
            binding.tvStartLearnCal.setClickable(true);
            calAction = CAL_ACTION_CANCEL;
        } else {
            // Clear any previous "view record" click handler while learning is active.
            binding.tvStartLearnCal.setClickable(false);
            calAction = CAL_ACTION_NONE;
        }
        binding.tvStartLearnMsg.setText(UsrMsgUtils.getSignature());

        binding.wlStartLearn.setAnimDuration(3000);
        binding.wlStartLearn.pauseAnimation();
        binding.wlStartLearn.resumeAnimation();
        binding.wlStartLearn.cancelAnimation();
        binding.wlStartLearn.startAnimation();

        binding.tvStartLearnCal.setVisibility(View.VISIBLE);
        binding.tvStartLearnCalMsg.setVisibility(View.VISIBLE);

        timeOff();

        Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable disposable) {
                        rxTimer = disposable;
                    }

                    @Override
                    public void onNext(@NonNull Long number) {
                        updateUI(number);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });


    }

    private void updateUI(long number) {

        try {

            if (number < 10 && showCal) {
                binding.tvStartLearnCalMsg.setText((10 - number) + "s");
                binding.tvStartLearnCal.setText("取消");
                calAction = CAL_ACTION_CANCEL;
            } else {
                binding.tvStartLearnCalMsg.setVisibility(View.GONE);
                binding.tvStartLearnCal.setVisibility(View.GONE);
                if (calAction == CAL_ACTION_CANCEL) calAction = CAL_ACTION_NONE;
            }

            Date date = new Date();

            // 倒计时
            long mHour = TimeUtils.getTimeSpan(planTime, date, TimeConstants.HOUR);
            long mMin = TimeUtils.getTimeSpan(planTime, date, TimeConstants.MIN);
            // 天 ,小时,分钟,秒
            long mSecond = TimeUtils.getTimeSpan(planTime, date, TimeConstants.SEC);

            if (mSecond > 60) mSecond = mSecond % 60;
            if (mMin > 60) mMin = mMin % 60;

//                LogUtils.i(mSecond + "=====");

            long spanTime = getSpanTime();

//            LogUtils.i("剩余时间" + spanTime);

            if (spanTime <= 0) {
                if (getLearning()) {
                    binding.wlStartLearn.setCenterTitle("稍等" + (okSpan + spanTime) / 1000 + "秒钟");
                } else {
                    LogUtils.i("==== 调用" + number);
                    postFinishLearn();
                }
            } else if (DetectService.isAccessibilitySettingsOn(this)) {

//                LogUtils.i("======学习");

                if (number % 10 == 0) {//减少性能消耗
                    saveNowTime(new Date());
//                        LogUtils.file("\n\n学习中 -- " + TimeUtils.date2String(date));
                }

                long fenZi = TimeUtils.getTimeSpan(planTime, date, TimeConstants.SEC);
                long fenMu = TimeUtils.getTimeSpan(planTime, startTime, TimeConstants.SEC) + okSpan / 1000;

                int aa = (int) (fenZi * 100 / fenMu);

//                LogUtils.i("计/**/算分数" + aa);

                if (aa >= 80) {
                    WaveLoadingPaintColor(R.color.white, R.color.white, R.color.white, R.color.color_8, R.color.color_8);
                } else if (aa >= 50) {
                    WaveLoadingPaintColor(UsrMsgUtils.getThemeColor(), R.color.white, R.color.white, UsrMsgUtils.getThemeColor(), UsrMsgUtils.getThemeColor());
                } else if (aa >= 20) {
                    WaveLoadingPaintColor(R.color.orange, R.color.orange, R.color.white, R.color.orange, R.color.orange);
                } else if (mSecond >= 10L) {
                    WaveLoadingPaintColor(R.color.orange, R.color.orange, R.color.orange, R.color.orange, R.color.orange);
                } else {
                    WaveLoadingPaintColor(R.color.colorAccent, R.color.colorAccent, R.color.colorAccent, R.color.colorAccent, R.color.colorAccent);
                }
                String s = timeOO(mHour) + " : " + timeOO(mMin) + " : " + timeOO(mSecond);


                binding.wlStartLearn.setProgressValue(aa);
                binding.wlStartLearn.setCenterTitle(s);

//                LogUtils.i(s);

            } else {
                LogUtils.i("权限缺失");
                timeOff();
                openDetect();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void timeOff() {
        LogUtils.i("取消");
        if (rxTimer != null && !rxTimer.isDisposed()) rxTimer.dispose();

    }

    private String timeOO(long timeO) {
        String timeOO = String.valueOf(timeO);
        if (timeOO.length() == 1) timeOO = "0" + timeOO;
        return timeOO;
    }

    private void updateApp() {

        List<String> stringList = lnm2file.getEnableApp();

        LogUtils.i("===========" + stringList.toString());
        List<LnmApp> lnmApps = new ArrayList<>();

        for (String packageName : stringList) {
            lnmApps.add(new LnmApp(AppUtils.getAppName(packageName), AppUtils.getAppIcon(packageName), packageName));
        }
        enableAppAdapter.submitList(lnmApps);
    }

    private void cancel() {
        finishLearn();
    }

    private void onCalButtonClick() {
        if (calAction == CAL_ACTION_CANCEL) {
            binding.tvStartLearnCalMsg.setText("取消中……");
            binding.tvStartLearnCal.setClickable(false);
            postCancelLearn();
            return;
        }
        if (calAction == CAL_ACTION_RECORD) {
            startActivity(new Intent(StartLearnActivity.this, LnmRecordActivity.class));
            finish();
        }
    }

    private void postUserStopLearn() {
        if (canCancelAsNoRecord()) {
            postCancelLearn();
        } else {
            postFinishLearn();
        }
    }

    private boolean canCancelAsNoRecord() {
        if (!showCal) return false;
        Date s = startTime != null ? startTime : lnm2file.getStartTime();
        if (s == null) return false;
        return System.currentTimeMillis() - s.getTime() <= START_CANCEL_GRACE_MS;
    }

    private int newLocalLnmId() {
        int id = (int) (System.currentTimeMillis() & 0x7fffffff);
        if (id <= 0) id = (int) (System.nanoTime() & 0x7fffffff);
        return id;
    }

    private Lnm buildLocalLnm(int id, Date createdDate, Date schedule, Date endTime, boolean finish) {
        Lnm lnm = new Lnm();
        lnm.id = id;
        lnm.createdDate = createdDate;
        lnm.schedule = schedule;
        lnm.endTime = endTime;
        lnm.finish = finish;
        return lnm;
    }

//    @Override
//    protected void onStop() {
//        LogUtils.i("=====onStop");
//

    /// /            notice("请在20s内返回！");
//        super.onStop();
//    }
//
//    @Override
//    protected void onStart() {
//        LogUtils.i("=====onStart");
//
//        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        if (notificationManager != null) notificationManager.cancel(ncLnmId);
//        super.onStart();
//    }
    @Override
    protected void onDestroy() {
        LogUtils.i("=====onDestroy");

        timeOff();
        super.onDestroy();

    }

//    private void notice(String msg){
//        saveNowTime(new Date());
//
//        String title = "学习中……";
//
//        Intent intent = new Intent(this, MainActivity.class);
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,
//                0,   //请求码
//                intent, //意图对象
//                PendingIntent.FLAG_UPDATE_CURRENT);
//
//        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        if (notificationManager != null) {
//            Notification notification = null;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                notification = new NotificationCompat.Builder(this, ncLnmIds)
//                        .setChannelId(ncLnmName)
//                        .setContentTitle(title)
//                        .setAutoCancel(true)
//                        .setContentText(msg)
//                        .setSmallIcon(R.drawable.logo)
//                        .setContentIntent(pendingIntent)
//                        .build();
//            } else {
//                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ncLnmIds)
//                        .setChannelId(ncLnmName)
//                        .setContentTitle(title)
//                        .setContentText(msg)
//                        .setAutoCancel(true)
//                        .setContentIntent(pendingIntent)
//                        .setSmallIcon(R.drawable.logo)
//                        .setOngoing(true);
//                notification = notificationBuilder.build();
//            }
//            notificationManager.notify(ncLnmId, notification);
//        }
//
//    }


    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        //for new api versions.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
}
