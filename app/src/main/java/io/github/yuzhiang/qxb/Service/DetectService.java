package io.github.yuzhiang.qxb.Service;

import static io.github.yuzhiang.qxb.model.lnm2file.getLearning;
import static io.github.yuzhiang.qxb.model.lnm2file.getManualCancelAt;
import static io.github.yuzhiang.qxb.model.lnm2file.saveLnmLogs;
import static io.github.yuzhiang.qxb.common.Constant.Constant.lnmDetectUnbind;
import static io.github.yuzhiang.qxb.common.Constant.Constant.lnmStart;
import static io.github.yuzhiang.qxb.common.Constant.Constant.lnmState;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.Utils;

import io.github.yuzhiang.qxb.BuildConfig;
import io.github.yuzhiang.qxb.activity.StartLearnActivity;
import io.github.yuzhiang.qxb.activity.LearnNoMobileActivity;
import io.github.yuzhiang.qxb.model.lnm2file;
import io.github.yuzhiang.qxb.model.focus.FocusRulePrefs;
import io.github.yuzhiang.qxb.model.focus.SleepReportStore;
import io.github.yuzhiang.qxb.base.MyApplication;
import io.github.yuzhiang.qxb.db.room.dbUtils.lnmDBUtils;
import io.github.yuzhiang.qxb.view.tastytoast.SimToast;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by wenmingvs on 16/2/10.
 */
public class DetectService extends AccessibilityService {
    private static final long MANUAL_EXIT_SUPPRESS_MS = 30_000L;

    private static String mForegroundPackageName;
    private static DetectService mInstance = null;
    public List<String> lnmBai = new ArrayList<>();

    long last_data = -1;
    private Handler sleepHandler;
    private Runnable sleepCheck;

    public DetectService() {
        lnmBai.clear();
        lnmBai.addAll(new ArrayList<>(Arrays.asList(
//            "com.vivo.floatingball",//vivo悬浮球
//            "com.miui.securitycenter",//小米安全管家
//            "com.iqoo.secure",//vivo安安全管家
//            "com.coloros.safecenter",//oppo安全管家
//            "com.huawei.systemmanager",//华为全管家
//            "android",//华为手势返回
//            "com.android.systemui",//小米、华为通知栏，最近任务等
//            "com.vivo.upslide",//vivo控制中心
//            "com.huawei.android.FloatTasks",//华为悬浮球
//            "com.miui.touchassistant",//小米悬浮球
//            "com.miui.contentextension",//小米传送门
                BuildConfig.APPLICATION_ID,
                getDefaultInputMethodPkgName()
        )));
        try {
            String json = ResourceUtils.readAssets2String("lnmBaiMingDan");
            List<String> localWhiteList = GsonUtils.fromJson(json, new TypeToken<List<String>>() {
            }.getType());
            if (localWhiteList != null) {
                lnmBai.addAll(localWhiteList);
            }
        } catch (Exception e) {
            LogUtils.e(e);
        }
    }

    public static DetectService getInstance() {
        if (mInstance == null) {
            synchronized (DetectService.class) {
                if (mInstance == null) {
                    mInstance = new DetectService();
                }
            }
        }
        return mInstance;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        SimToast.toastEL("辅助权限被关闭，轻学伴模式无法使用");

        LogUtils.file("\n\n辅助权限被关闭" + TimeUtils.getNowString());

        if (isInterceptLearningActive()) {
            Intent intent0 = new Intent(getApplicationContext(), StartLearnActivity.class);
            intent0.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent0.putExtra(lnmState, lnmDetectUnbind);
            startActivity(intent0);
        }

        return super.onUnbind(intent);
    }

    @Override
    protected void onServiceConnected() {
        if (isInterceptLearningActive()) {
            Intent intent0 = new Intent(getApplicationContext(), LearnNoMobileActivity.class);
            intent0.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent0);
        }
        startSleepCheck();
        super.onServiceConnected();
    }

    @Override
    public void onDestroy() {
        stopSleepCheck();
        super.onDestroy();
    }

    /**
     * 监听窗口焦点,并且获取焦点窗口的包名
     *
     * @param event
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        try {

            if (System.currentTimeMillis() - last_data > 500) {
                last_data = System.currentTimeMillis();

                FocusRulePrefs.RuleConfig cfgRule = FocusRulePrefs.load();
                if (cfgRule != null && cfgRule.enabled) {
                    checkSleepAuto(cfgRule);
                }

                if (isInterceptLearningActive()) {

                    mForegroundPackageName = event.getPackageName().toString();
                    LogUtils.i(mForegroundPackageName);

                    if (!lnmBai.contains(mForegroundPackageName)) {


                        List<String> stringList = lnm2file.getEnableApp();
                        String s = mForegroundPackageName;

                        FocusRulePrefs.RuleConfig cfgStrict = FocusRulePrefs.load();
                        boolean sleepStrict = cfgStrict != null && cfgStrict.enabled
                                && FocusRulePrefs.isSleepAutoActive()
                                && isInSleepWindow(cfgStrict);

                        if (!stringList.contains(mForegroundPackageName) || sleepStrict) {
                            if (sleepStrict) {
                                SleepReportStore.recordAttempt(System.currentTimeMillis());
                            }

                            FocusRulePrefs.RuleConfig cfg = FocusRulePrefs.load();
                            if (!sleepStrict && cfg != null && cfg.enabled && System.currentTimeMillis() < cfg.tempPassUntil) {
                                return;
                            }
                            if (cfg != null && cfg.enabled) {
                                if (isInSleepWindow(cfg)) {
                                    try {
                                        Intent intent = new Intent(getApplicationContext(), StartLearnActivity.class);
                                        intent.putExtra(lnmState, lnmStart);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    } catch (Exception ignored) {
                                    }
                                    saveLnmLogs(TimeUtils.getNowString() + "：睡眠时段拦截\n    -->  " + AppUtils.getAppName(mForegroundPackageName) + "（" + mForegroundPackageName + "）");
                                    return;
                                }
                            }

                            try {


                                Intent intent = new Intent(getApplicationContext(), StartLearnActivity.class);
                                intent.putExtra(lnmState, lnmStart);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                LogUtils.i("=======" + mForegroundPackageName);

                                LogUtils.file("\n\n正在学习，拦截非白名单应用：" + s);

                                saveLnmLogs(TimeUtils.getNowString() + "：拦截成功\n    -->  " + AppUtils.getAppName(mForegroundPackageName) + "（" + mForegroundPackageName + "）");
                                long currentId = lnm2file.getThisId();
                                if (currentId > 0) {
                                    lnm2file.incrementScreenOnCount((int) currentId);
                                }

                                // Category quota feature removed.

                            } catch (Exception e) {
                                LogUtils.file("\n\n正在学习，拦截非白名单应用失败：" + s + "\n" + e.toString());

                                saveLnmLogs(TimeUtils.getNowString() + "：拦截失败\n    -->  " + AppUtils.getAppName(mForegroundPackageName) + "（" + mForegroundPackageName + "）\n" + e.toString());

                                e.printStackTrace();
                            }

                        } else {
                            saveLnmLogs(TimeUtils.getNowString() + "：未拦截(用户白名单应用)\n    -->  " + AppUtils.getAppName(mForegroundPackageName) + "（" + mForegroundPackageName + "）");

                            LogUtils.file("\n\n正在学习，白名单应用不进行拦截：" + s);
                        }
                    } else {
                        saveLnmLogs(TimeUtils.getNowString() + "：未拦截(内置白名单应用)\n    -->  " + AppUtils.getAppName(mForegroundPackageName) + "（" + mForegroundPackageName + "）");

                        LogUtils.file("\n\n正在学习，管家类应用不进行拦截：" + mForegroundPackageName);
                    }
                }
            }

        } catch (Exception e) {
            saveLnmLogs(TimeUtils.getNowString() + "：监听错误！\n    -->  " + e.toString());
            LogUtils.file("\n\n监听焦点出现问题：" + e.toString());
            e.printStackTrace();
        }

    }

    @Override
    public void onInterrupt() {
    }

    public String getForegroundPackage() {
        return mForegroundPackageName;
    }

    private boolean isInterceptLearningActive() {
        return !isManualExitSuppressed() && getLearning() && lnmDBUtils.countPending() > 0;
    }

    private boolean isManualExitSuppressed() {
        long at = getManualCancelAt();
        return at > 0 && System.currentTimeMillis() - at <= MANUAL_EXIT_SUPPRESS_MS;
    }

    private void startSleepCheck() {
        if (sleepHandler == null) {
            sleepHandler = new Handler(Looper.getMainLooper());
        }
        if (sleepCheck == null) {
            sleepCheck = new Runnable() {
                @Override
                public void run() {
                    try {
                        FocusRulePrefs.RuleConfig cfg = FocusRulePrefs.load();
                        if (cfg != null && cfg.enabled) {
                            checkSleepAuto(cfg);
                        }
                    } catch (Exception ignored) {
                    } finally {
                        if (sleepHandler != null) {
                            sleepHandler.postDelayed(this, 60_000L);
                        }
                    }
                }
            };
        }
        sleepHandler.removeCallbacks(sleepCheck);
        sleepHandler.postDelayed(sleepCheck, 5_000L);
    }

    private void stopSleepCheck() {
        if (sleepHandler != null && sleepCheck != null) {
            sleepHandler.removeCallbacks(sleepCheck);
        }
    }

    private void checkSleepAuto(FocusRulePrefs.RuleConfig cfgRule) {
        if (isManualExitSuppressed()) {
            return;
        }
        boolean inSleep = isInSleepWindow(cfgRule);
        if (inSleep && !getLearning() && FocusRulePrefs.isSleepAutoActive()) {
            FocusRulePrefs.setSleepAutoActive(false);
        }
        if (inSleep && !getLearning() && !FocusRulePrefs.isSleepAutoActive()) {
            try {
                long endAt = calcSleepEndAtMs(cfgRule);
                long sleepSpan = endAt > 0 ? Math.max(60_000L, endAt - System.currentTimeMillis()) : 0L;
                if (sleepSpan > 0) {
                    lnm2file.savePlanSpan(sleepSpan);
                }
                if (cfgRule != null) {
                    cfgRule.tempPassUntil = 0L;
                    FocusRulePrefs.save(cfgRule);
                }
                SleepReportStore.startSession(System.currentTimeMillis(), endAt);
                Intent intent = new Intent(getApplicationContext(), StartLearnActivity.class);
                intent.putExtra(lnmState, lnmStart);
                intent.putExtra(StartLearnActivity.EXTRA_SLEEP_AUTO, true);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                FocusRulePrefs.setSleepAutoActive(true);
                saveLnmLogs(TimeUtils.getNowString() + "：睡眠时段自动开启专注");
            } catch (Exception ignored) {
            }
        } else if (!inSleep && getLearning() && FocusRulePrefs.isSleepAutoActive()) {
            try {
                Intent intent = new Intent(getApplicationContext(), StartLearnActivity.class);
                intent.putExtra(lnmState, io.github.yuzhiang.qxb.common.Constant.Constant.lnmFinish);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                FocusRulePrefs.setSleepAutoActive(false);
                saveLnmLogs(TimeUtils.getNowString() + "：睡眠结束自动结束专注");
            } catch (Exception ignored) {
            }
        }
    }

    private boolean isInSleepWindow(FocusRulePrefs.RuleConfig cfg) {
        if (cfg == null) return false;
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_WEEK);
        boolean weekend = (day == Calendar.SATURDAY || day == Calendar.SUNDAY);
        int nowMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
        FocusRulePrefs.TimeWindow w = weekend ? cfg.weekendSleep : cfg.schoolSleep;
        return w != null && w.contains(nowMin);
    }

    private long calcSleepSpanMs(FocusRulePrefs.RuleConfig cfg) {
        long endAt = calcSleepEndAtMs(cfg);
        if (endAt <= 0) return 0L;
        long span = endAt - System.currentTimeMillis();
        return Math.max(60_000L, span);
    }

    private long calcSleepEndAtMs(FocusRulePrefs.RuleConfig cfg) {
        if (cfg == null) return 0L;
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

    // Category quota feature removed.


    /**
     * 此方法用来判断当前应用的辅助功能服务是否开启
     *
     * @param context
     * @return
     */
    public static boolean isAccessibilitySettingsOn(Context context) {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(MyApplication.getInstances().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            LogUtils.i("wenming", e.getMessage());
        }

        if (accessibilityEnabled == 1) {
            String services = Settings.Secure.getString(MyApplication.getInstances().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (services != null) {
                return services.toLowerCase().contains(MyApplication.getInstances().getPackageName().toLowerCase());
            }
        }
        return false;
    }


//    private boolean isSystemApp(String pkgName) {
//        boolean isSystemApp = false;
//        PackageInfo pi = null;
//        try {
//            PackageManager pm = getApplicationContext().getPackageManager();
//            pi = pm.getPackageInfo(pkgName, 0);
//        } catch (Throwable t) {
//            Log.w("==isSystemApp==", t.getMessage(), t);
//        }
//
//        // 是系统中已安装的应用
//        if (pi != null) {

    /// /            boolean isSysUpd = (pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1;
    /// /            isSystemApp = isSysApp || isSysUpd;
//            isSystemApp = (pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1;
//        }
//
//        if ( mForegroundPackageName.contains("browser") ||//不是浏览器
//                mForegroundPackageName.contains("game") ){
//            isSystemApp = false;
//        }else if (mForegroundPackageName.equals(getDefaultInputMethodPkgName())){
//            isSystemApp = true;
//
//        }
//
//        Log.w("==isSystemApp==", pkgName + "====" + isSystemApp);
//
//        return isSystemApp;
//    }
    public String getDefaultInputMethodPkgName() {
        String mDefaultInputMethodPkg = "";
        try {
            String mDefaultInputMethodCls = Settings.Secure.getString(
                    Utils.getApp().getContentResolver(),
                    Settings.Secure.DEFAULT_INPUT_METHOD);
            //输入法类名信息
            if (!TextUtils.isEmpty(mDefaultInputMethodCls)) {
                //输入法包名
                mDefaultInputMethodPkg = mDefaultInputMethodCls.split("/")[0];
            }
        } catch (Exception e) {
            LogUtils.file("\n\n获取默认输入法错误！" + e.toString());
        }


        return mDefaultInputMethodPkg;
    }


}
