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
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.text.TextUtils;
import android.widget.EditText;

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
import io.github.yuzhiang.qxb.model.focus.FocusRulePrefs;
import io.github.yuzhiang.qxb.model.focus.SleepReportStore;
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
import io.github.yuzhiang.qxb.view.dialog.InputDialog;
import io.github.yuzhiang.qxb.view.dialog.MessageDialog;
import io.github.yuzhiang.qxb.receiver.StudyAdminReceiver;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
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

    public static final String EXTRA_SLEEP_AUTO = "extra_sleep_auto";
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
    private int blockedAppCount = 0;
    private long lastPromptAt = 0L;
    private int exitPwFailCount = 0;
    private long exitPwLockUntil = 0L;
    private boolean sleepAutoMode = false;
    private boolean sleepAutoSwitchGuard = false;
    private boolean leftByHome = false;
    private long lastRestEndAt = 0L;
    private long lastRestRemindAt = 0L;
    private boolean restActive = false;
    private CountDownTimer restTimer;
    private AlertDialog restDialog;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        LogUtils.i("恢复");

    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (getLearning()) {
            leftByHome = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!getLearning()) {
            leftByHome = false;
            return;
        }
        if (leftByHome) {
            leftByHome = false;
            String title = sleepAutoMode ? "睡眠专注" : "返回桌面";
            String msg = sleepAutoMode ? "已返回桌面，请回去睡觉" : "已返回桌面，请继续专注";
            new MessageDialog.Builder(StartLearnActivity.this)
                    .setTitle(title)
                    .setMessage(msg)
                    .setConfirm("继续专注")
                    .setCancelable(false)
                    .show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStartLearnBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sleepAutoMode = getIntent().getBooleanExtra(EXTRA_SLEEP_AUTO, false);
        io.github.yuzhiang.qxb.MyUtils.UsrMsgUtils.applyPageBackground(binding.getRoot());
        binding.tvStartLearnCal.setOnClickListener(v -> onCalButtonClick());
        binding.tvStartLearnHistory.setOnClickListener(v -> {
            startActivity(new Intent(StartLearnActivity.this, LnmRecordActivity.class));
        });
        if (binding.btnTempPass10 != null) {
            binding.btnTempPass10.setOnClickListener(v -> {
                new MessageDialog.Builder(StartLearnActivity.this)
                        .setTitle("临时通行")
                        .setMessage("确认开启10分钟临时通行？")
                        .setConfirm("开启")
                        .setCancel("取消")
                        .setListener(new MessageDialog.OnListener() {
                            @Override
                            public void onConfirm(BaseDialog dialog) {
                                FocusRulePrefs.setTempPassUntil(System.currentTimeMillis() + 10 * 60 * 1000L);
                                updateTempPassStatus();
                                toastSL("已开启10分钟临时通行");
                            }

                            @Override
                            public void onCancel(BaseDialog dialog) {
                            }
                        }).show();
            });
        }
        if (binding.btnTempPass30 != null) {
            binding.btnTempPass30.setOnClickListener(v -> {
                new MessageDialog.Builder(StartLearnActivity.this)
                        .setTitle("临时通行")
                        .setMessage("确认开启30分钟临时通行？")
                        .setConfirm("开启")
                        .setCancel("取消")
                        .setListener(new MessageDialog.OnListener() {
                            @Override
                            public void onConfirm(BaseDialog dialog) {
                                FocusRulePrefs.setTempPassUntil(System.currentTimeMillis() + 30 * 60 * 1000L);
                                updateTempPassStatus();
                                toastSL("已开启30分钟临时通行");
                            }

                            @Override
                            public void onCancel(BaseDialog dialog) {
                            }
                        }).show();
            });
        }
        if (binding.btnTempPassEnd != null) {
            binding.btnTempPassEnd.setOnClickListener(v -> {
                new MessageDialog.Builder(StartLearnActivity.this)
                        .setTitle("临时通行")
                        .setMessage("确认结束临时通行？")
                        .setConfirm("结束")
                        .setCancel("取消")
                        .setListener(new MessageDialog.OnListener() {
                            @Override
                            public void onConfirm(BaseDialog dialog) {
                                FocusRulePrefs.setTempPassUntil(0L);
                                updateTempPassStatus();
                                toastSL("已结束临时通行");
                            }

                            @Override
                            public void onCancel(BaseDialog dialog) {
                            }
                        }).show();
            });
        }
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
        updateTempPassStatus();
        initSleepAutoSwitch();
        applySleepAutoUi(sleepAutoMode);
        if (sleepAutoMode && SleepReportStore.loadCurrent() == null) {
            SleepReportStore.startSession(System.currentTimeMillis(), calcSleepEndAtMs());
        }

        LogUtils.i("=====开始");

/*      与预期行为不同
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Snackbar snackbar = Snackbar
                    .make(binding.rlStartLearn, "请在10秒内退出分屏模式否则将视为专注失败", Snackbar.LENGTH_INDEFINITE )
                    .setAction("立即结束专注", v1 -> postFinishLearn());

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
                        .make(binding.rlStartLearn, "请在10秒内退出分屏模式否则将视为专注失败", Snackbar.LENGTH_INDEFINITE)
                        .setAction("结束专注", v1 -> postUserStopLearn())
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
        if (restActive) {
            toastEL("休息中，暂不可操作");
            return;
        }
        if (sleepAutoMode) {
            new MessageDialog.Builder(StartLearnActivity.this)
                    .setTitle("睡眠专注")
                    .setMessage("现在是睡眠时间，家长可输入密码退出睡眠专注")
                    .setTextGravity(Gravity.CENTER)
                    .setConfirm("家长退出")
                    .setCancel("继续睡眠")
                    .setCancelable(false)
                    .setListener(new MessageDialog.OnListener() {
                        @Override
                        public void onConfirm(BaseDialog dialog) {
                            requestExitWithPassword(StartLearnActivity.this::finishSleepAutoSilently);
                        }

                        @Override
                        public void onCancel(BaseDialog dialog) {
                        }
                    }).show();
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastPromptAt < 5000L) {
            return;
        }
        lastPromptAt = now;
        new MessageDialog.Builder(StartLearnActivity.this)
                .setTitle("开始专注！")
                .setMessage("先完成作业任务，暂时不要使用手机")
                .setTextGravity(Gravity.CENTER)
                .setConfirm("开始专注")
                .setCancel("先休息")
                .setCancelable(false)
                .setListener(new MessageDialog.OnListener() {
                    @Override
                    public void onConfirm(BaseDialog dialog) {
                    }

                    @Override
                    public void onCancel(BaseDialog dialog) {
                        requestExitWithPassword(() -> Snackbar.make(binding.rlStartLearn,
                                        canCancelAsNoRecord() ? "将取消本次专注" : "将视为失败！！",
                                        Snackbar.LENGTH_LONG)
                                .setAction("确定", v1 -> postUserStopLearn())
                                .show());
                    }
                }).show();

    }

    private void requestExitWithPassword(Runnable onPass) {
        String pw = UsrMsgUtils.getFocusExitPassword();
        if (TextUtils.isEmpty(pw)) {
            onPass.run();
            return;
        }
        long now = System.currentTimeMillis();
        if (now < exitPwLockUntil) {
            long left = Math.max(0, (exitPwLockUntil - now) / 1000);
            toastEL("输入错误次数过多，请稍后再试（" + left + "s）");
            return;
        }
        new InputDialog.Builder(this)
                .setTitle("请输入专注退出密码")
                .setHint("请输入密码")
                .setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .setCancel("取消")
                .setConfirm("确认")
                .setListener(new InputDialog.OnListener() {
                    @Override
                    public void onConfirm(BaseDialog dialog, String content) {
                        String in = content == null ? "" : content.trim();
                        if (pw.equals(in)) {
                            exitPwFailCount = 0;
                            onPass.run();
                        } else {
                            exitPwFailCount++;
                            if (exitPwFailCount >= 3) {
                                exitPwLockUntil = System.currentTimeMillis() + 30_000L;
                                exitPwFailCount = 0;
                                toastEL("错误次数过多，已锁定30秒");
                            } else {
                                toastEL("密码错误（还可尝试 " + (3 - exitPwFailCount) + " 次）");
                            }
                        }
                    }
                })
                .show();
    }

    private void WaveLoadingPaintColor(int top, int center, int bottom, int wave, int border) {

        binding.wlStartLearn.setTopTitleColor(ContextCompat.getColor(StartLearnActivity.this, top));
        binding.wlStartLearn.setCenterTitleColor(ContextCompat.getColor(StartLearnActivity.this, center));
        binding.wlStartLearn.setBottomTitleColor(ContextCompat.getColor(StartLearnActivity.this, bottom));
        binding.wlStartLearn.setWaveColor(ContextCompat.getColor(StartLearnActivity.this, wave));
        binding.wlStartLearn.setBorderColor(ContextCompat.getColor(StartLearnActivity.this, border));

    }

    private void updateTempPassStatus() {
        if (binding == null || binding.tvTempPassStatus == null) return;
        FocusRulePrefs.RuleConfig cfg = FocusRulePrefs.load();
        if (cfg == null) {
            binding.tvTempPassStatus.setText("临时通行：未开启");
            return;
        }
        long now = System.currentTimeMillis();
        if (cfg.tempPassUntil <= now) {
            binding.tvTempPassStatus.setText("临时通行：未开启");
        } else {
            long leftMin = Math.max(0, (cfg.tempPassUntil - now) / 60000);
            binding.tvTempPassStatus.setText("临时通行：剩余" + leftMin + "分钟");
        }
    }

    private void openDetect() {
        runOnUiThread(() -> {
            VibrateUtils.vibrate(500);

            String s = "无障碍权限被关闭！无法继续拦截\n\n请在30s内重新开启，否则此次专注失败，专注时间将会被扣除！" +
                    "\n\n如果一次专注中不断出现该问题，可能是因为没有给予“自启权限、后台保护防止清理权限”";

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
            lnm2file.saveScreenOnCount(localId, 0);

            try {
                String s = "\n\n专注开始(本地)：id-" + lnm.id + lnm.finish +
                        "\n    开始-" + TimeUtils.date2String(lnm.createdDate) +
                        "\n    计划-" + TimeUtils.date2String(lnm.schedule);
                LogUtils.file(s);
                LogUtils.i(s);
            } catch (Exception e) {
                LogUtils.file("\n\n专注开始(本地)：记录错误" + e);
                e.printStackTrace();
            }

            toastSL("开始专注");
            LearnStart();
        }
    }

    private void postFinishLearn() {
        if (sleepAutoMode) {
            finishSleepAutoSilently();
            return;
        }
        timeOff();
        long id = getThisId();

        LogUtils.file("\n\n尝试专注结束 ~ " + id);
        LogUtils.i("尝试专注结束 ~ " + id);
        binding.wlStartLearn.setCenterTitle("专注结束");
        binding.wlStartLearn.setBottomTitle("数据保存中");

        if (id > 0) {
            Date localStart = startTime != null ? startTime : lnm2file.getStartTime();
            Date localPlan = planTime != null ? planTime : lnm2file.getPlanTime();
            Date localEnd = new Date();

            if (localStart == null || localPlan == null) {
                toastEL("本地专注记录异常，无法结束");
                cancel();
                finish();
                return;
            }

            if (TimeUtils.getTimeSpan(localEnd, localPlan, TimeConstants.MIN) > 20) {
                toastEL("目前与计划结束时间相差超过20分钟，此次专注被删除！");
                postCancelLearn(id);
                return;
            }

            boolean success = localEnd.getTime() + okSpan >= localPlan.getTime();
            Lnm lnm = buildLocalLnm((int) id, localStart, localPlan, localEnd, success);
            lnmDBUtils.insert(lnm);
            blockedAppCount = lnm2file.getScreenOnCount((int) id);
            lnm2file.saveScreenOnCount((int) id, blockedAppCount);
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
                LogUtils.file("\n\n专注结束(本地)：id-" + lnm.id + lnm.finish +
                        "\n    开始-" + TimeUtils.date2String(lnm.createdDate) +
                        "\n    结束-" + TimeUtils.date2String(lnm.endTime) +
                        "\n    计划-" + TimeUtils.date2String(lnm.schedule));
                LogUtils.i("\n\n专注结束(本地)：id-" + lnm.id + lnm.finish +
                        "\n    开始-" + TimeUtils.date2String(lnm.createdDate) +
                        "\n    结束-" + TimeUtils.date2String(lnm.endTime) +
                        "\n    计划-" + TimeUtils.date2String(lnm.schedule));
            } catch (Exception e) {
                LogUtils.file("\n\n专注结束(本地)：记录错误" + e);
                e.printStackTrace();
            }

            binding.wlStartLearn.setBottomTitle("数据保存成功");
            LearnFinish(lnm.finish);
            allFinish();
        }
        FocusRulePrefs.setSleepAutoActive(false);
    }


    private void postCancelLearn() {
        long id = getThisId();
        postCancelLearn(id);

    }

    private void postCancelLearn(long id) {
        if (sleepAutoMode) {
            finishSleepAutoSilently();
            return;
        }

        binding.wlStartLearn.setCenterTitle("专注取消");
        binding.wlStartLearn.setBottomTitle("数据保存中");

        LogUtils.i("取消：" + id);
        markManualCancelNow();
        finishLearn(); // 先清理进行中状态，避免返回主页时误判为“继续学习”

        if (id > 0) {
            lnmDBUtils.deleteById((int) id);
            lnmDBUtils.deletePendingAll();
            blockedAppCount = lnm2file.getScreenOnCount((int) id);
            lnm2file.saveScreenOnCount((int) id, blockedAppCount);
            timeOff();

            try {
                LogUtils.file("\n\n专注取消(本地)：id-" + id);
            } catch (Exception e) {
                LogUtils.file("\n\n专注取消(本地)：记录错误" + e);
                e.printStackTrace();
            }

            toastSL("取消专注！");
            binding.wlStartLearn.setBottomTitle("数据保存成功");
            finish();
        } else {
            lnmDBUtils.deletePendingAll();
            timeOff();
            finish();
        }
        FocusRulePrefs.setSleepAutoActive(false);

    }

    private void finishSleepAutoSilently() {
        timeOff();
        long id = getThisId();
        finishLearn();
        if (id > 0) {
            lnmDBUtils.deleteById((int) id);
        }
        lnmDBUtils.deletePendingAll();
        SleepReportStore.finishSession(System.currentTimeMillis());
        FocusRulePrefs.setSleepAutoActive(false);
        finish();
    }

    private void initSleepAutoSwitch() {
        if (binding.layoutSleepAuto == null || binding.switchSleepAuto == null) return;
        updateSleepAutoSwitchState();
        binding.switchSleepAuto.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (sleepAutoSwitchGuard) return;
            if (!isChecked && sleepAutoMode) {
                sleepAutoSwitchGuard = true;
                buttonView.setChecked(true);
                sleepAutoSwitchGuard = false;
                new MessageDialog.Builder(StartLearnActivity.this)
                        .setTitle("退出睡眠专注")
                        .setMessage("请输入家长密码退出睡眠专注")
                        .setConfirm("家长退出")
                        .setCancel("取消")
                        .setListener(new MessageDialog.OnListener() {
                            @Override
                            public void onConfirm(BaseDialog dialog) {
                                requestExitWithPassword(StartLearnActivity.this::finishSleepAutoSilently);
                            }

                            @Override
                            public void onCancel(BaseDialog dialog) {
                            }
                        }).show();
                return;
            }
            if (isChecked && !sleepAutoMode) {
                if (!isInSleepWindow()) {
                    toastEL("不在睡眠时间段，无法开启睡眠专注");
                    sleepAutoSwitchGuard = true;
                    buttonView.setChecked(false);
                    sleepAutoSwitchGuard = false;
                    return;
                }
                enableSleepAutoNow();
            }
        });
    }

    private void updateSleepAutoSwitchState() {
        if (binding.layoutSleepAuto == null || binding.switchSleepAuto == null) return;
        if (!isInSleepWindow()) {
            binding.layoutSleepAuto.setVisibility(View.GONE);
            return;
        }
        binding.layoutSleepAuto.setVisibility(View.VISIBLE);
        sleepAutoSwitchGuard = true;
        binding.switchSleepAuto.setChecked(sleepAutoMode || FocusRulePrefs.isSleepAutoActive());
        sleepAutoSwitchGuard = false;
    }

    private void enableSleepAutoNow() {
        long endAt = calcSleepEndAtMs();
        long now = System.currentTimeMillis();
        long span = endAt > 0 ? Math.max(60_000L, endAt - now) : 0L;
        if (span > 0) {
            lnm2file.savePlanSpan(span);
        }
        FocusRulePrefs.setSleepAutoActive(true);
        SleepReportStore.startSession(now, endAt);
        sleepAutoMode = true;
        applySleepAutoUi(true);
        if (getIntent() != null) {
            getIntent().putExtra(EXTRA_SLEEP_AUTO, true);
        }
        postStartLearn();
    }

    private void applySleepAutoUi(boolean enabled) {
        if (enabled) {
            if (binding.layoutTempPass != null) binding.layoutTempPass.setVisibility(View.GONE);
            if (binding.tvStartLearnHistory != null) binding.tvStartLearnHistory.setVisibility(View.GONE);
            if (binding.tvStartLearnCal != null) binding.tvStartLearnCal.setVisibility(View.GONE);
            if (binding.tvStartLearnCalMsg != null) {
                binding.tvStartLearnCalMsg.setVisibility(View.VISIBLE);
                binding.tvStartLearnCalMsg.setText("睡眠时间，请放下手机好好休息\n睡眠专注不计入统计");
            }
            if (binding.wlStartLearn != null) {
                binding.wlStartLearn.setTopTitle("睡眠中");
                binding.wlStartLearn.setCenterTitle("请休息");
                binding.wlStartLearn.setBottomTitle("自动睡眠专注");
            }
        } else {
            if (binding.layoutTempPass != null) binding.layoutTempPass.setVisibility(View.VISIBLE);
            if (binding.tvStartLearnHistory != null) binding.tvStartLearnHistory.setVisibility(View.VISIBLE);
            if (binding.tvStartLearnCal != null) binding.tvStartLearnCal.setVisibility(View.VISIBLE);
        }
        updateSleepAutoSwitchState();
    }

    private boolean isInSleepWindow() {
        FocusRulePrefs.RuleConfig cfg = FocusRulePrefs.load();
        if (cfg == null || !cfg.enabled) return false;
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_WEEK);
        boolean weekend = (day == Calendar.SATURDAY || day == Calendar.SUNDAY);
        int nowMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
        FocusRulePrefs.TimeWindow w = weekend ? cfg.weekendSleep : cfg.schoolSleep;
        return w != null && w.contains(nowMin);
    }

    private long calcSleepEndAtMs() {
        FocusRulePrefs.RuleConfig cfg = FocusRulePrefs.load();
        if (cfg == null || !cfg.enabled) return 0L;
        Calendar now = Calendar.getInstance();
        int day = now.get(Calendar.DAY_OF_WEEK);
        boolean weekend = (day == Calendar.SATURDAY || day == Calendar.SUNDAY);
        FocusRulePrefs.TimeWindow w = weekend ? cfg.weekendSleep : cfg.schoolSleep;
        if (w == null) return 0L;
        int nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        if (!w.contains(nowMin)) return 0L;
        int endMin = w.endMin;
        Calendar end = (Calendar) now.clone();
        if (w.startMin < w.endMin) {
            end.set(Calendar.HOUR_OF_DAY, endMin / 60);
            end.set(Calendar.MINUTE, endMin % 60);
        } else {
            if (nowMin >= w.startMin) {
                end.add(Calendar.DAY_OF_MONTH, 1);
            }
            end.set(Calendar.HOUR_OF_DAY, endMin / 60);
            end.set(Calendar.MINUTE, endMin % 60);
        }
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);
        return end.getTimeInMillis();
    }

    private void LearnFinish(boolean success) {

        if (success) {

            toastSL("真棒！专注结束，尝试打开未允许应用 " + blockedAppCount + " 次");

            toastSL("专注成功！");

        } else {
            toastEL("专注失败！");

            toastEL("专注失败，尝试打开未允许应用 " + blockedAppCount + " 次");

        }


        binding.wlStartLearn.setCenterTitle("专注结束");
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
        long currentId = getThisId();
        blockedAppCount = currentId > 0 ? lnm2file.getScreenOnCount((int) currentId) : 0;
        lockScreenIfPossible();
        lastRestEndAt = System.currentTimeMillis();
        lastRestRemindAt = 0L;

        if (showCal) {
            binding.tvStartLearnCal.setClickable(true);
            calAction = CAL_ACTION_CANCEL;
        } else {
            // Clear any previous "view record" click handler while learning is active.
            binding.tvStartLearnCal.setClickable(false);
            calAction = CAL_ACTION_NONE;
        }
        // 移除底部提示文本

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

                if (!sleepAutoMode) {
                    maybeHandleRest();
                    if (restActive) {
                        binding.wlStartLearn.setCenterTitle("休息中");
                        return;
                    }
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

    private void maybeHandleRest() {
        if (restActive) return;
        long now = System.currentTimeMillis();
        long continuous = now - lastRestEndAt;
        if (continuous >= 60 * 60 * 1000L) {
            startForcedRest(10 * 60 * 1000L);
            return;
        }
        if (continuous >= 30 * 60 * 1000L && (lastRestRemindAt == 0L || now - lastRestRemindAt >= 30 * 60 * 1000L)) {
            lastRestRemindAt = now;
            toastSL("已连续使用30分钟，建议休息一下");
        }
    }

    private void startForcedRest(long durationMs) {
        restActive = true;
        lastRestRemindAt = 0L;
        setRestUiEnabled(false);
        if (planTime != null) {
            planTime = new Date(planTime.getTime() + durationMs);
            lnm2file.savePlanTime(planTime);
            long span = lnm2file.getPlanSpan();
            if (span > 0) {
                lnm2file.savePlanSpan(span + durationMs);
            }
        }
        if (restDialog == null) {
            restDialog = new AlertDialog.Builder(this)
                    .setTitle("视力保护")
                    .setMessage("已连续使用60分钟，请休息10分钟")
                    .setCancelable(false)
                    .create();
        }
        restDialog.show();
        if (restTimer != null) {
            restTimer.cancel();
        }
        restTimer = new CountDownTimer(durationMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long min = Math.max(0, millisUntilFinished / 60000);
                long sec = Math.max(0, (millisUntilFinished / 1000) % 60);
                if (restDialog != null && restDialog.isShowing()) {
                    restDialog.setMessage("已连续使用60分钟，请休息 " + min + "分" + sec + "秒");
                }
            }

            @Override
            public void onFinish() {
                restActive = false;
                lastRestEndAt = System.currentTimeMillis();
                setRestUiEnabled(true);
                if (restDialog != null && restDialog.isShowing()) {
                    restDialog.dismiss();
                }
                toastSL("休息结束，可以继续专注");
            }
        }.start();
    }

    private void setRestUiEnabled(boolean enabled) {
        if (binding == null) return;
        if (binding.layoutTempPass != null) binding.layoutTempPass.setEnabled(enabled);
        if (binding.btnTempPass10 != null) binding.btnTempPass10.setEnabled(enabled);
        if (binding.btnTempPass30 != null) binding.btnTempPass30.setEnabled(enabled);
        if (binding.btnTempPassEnd != null) binding.btnTempPassEnd.setEnabled(enabled);
        if (binding.tvStartLearnHistory != null) binding.tvStartLearnHistory.setEnabled(enabled);
        if (binding.tvStartLearnCal != null) binding.tvStartLearnCal.setEnabled(enabled);
        if (binding.tvStartLearnCalMsg != null) binding.tvStartLearnCalMsg.setEnabled(enabled);
    }

    private void timeOff() {
        LogUtils.i("取消");
        if (rxTimer != null && !rxTimer.isDisposed()) rxTimer.dispose();

    }

    private void lockScreenIfPossible() {
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            if (dpm == null) return;
            ComponentName admin = new ComponentName(this, StudyAdminReceiver.class);
            if (dpm.isAdminActive(admin)) {
                dpm.lockNow();
            }
        } catch (Exception e) {
            LogUtils.i("lockNow failed: " + e);
        }
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
            if (restActive) {
                toastEL("休息中，暂不可操作");
                return;
            }
            binding.tvStartLearnCalMsg.setText("取消中……");
            binding.tvStartLearnCal.setClickable(false);
            requestExitWithPassword(this::postCancelLearn);
            return;
        }
        if (calAction == CAL_ACTION_RECORD) {
            if (restActive) {
                toastEL("休息中，暂不可操作");
                return;
            }
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
        if (restTimer != null) {
            restTimer.cancel();
            restTimer = null;
        }
        if (restDialog != null && restDialog.isShowing()) {
            restDialog.dismiss();
        }
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
