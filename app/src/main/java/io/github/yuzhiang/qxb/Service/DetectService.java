package io.github.yuzhiang.qxb.Service;

import static io.github.yuzhiang.qxb.model.lnm2file.getLearning;
import static io.github.yuzhiang.qxb.model.lnm2file.saveLnmLogs;
import static io.github.yuzhiang.qxb.common.Constant.Constant.lnmDetectUnbind;
import static io.github.yuzhiang.qxb.common.Constant.Constant.lnmStart;
import static io.github.yuzhiang.qxb.common.Constant.Constant.lnmState;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
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
import io.github.yuzhiang.qxb.base.MyApplication;
import io.github.yuzhiang.qxb.db.room.dbUtils.lnmDBUtils;
import io.github.yuzhiang.qxb.view.tastytoast.SimToast;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wenmingvs on 16/2/10.
 */
public class DetectService extends AccessibilityService {

    private static String mForegroundPackageName;
    private static DetectService mInstance = null;
    public List<String> lnmBai = new ArrayList<>();

    long last_data = -1;

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
        super.onServiceConnected();
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

                if (isInterceptLearningActive()) {

                    mForegroundPackageName = event.getPackageName().toString();
                    LogUtils.i(mForegroundPackageName);

                    if (!lnmBai.contains(mForegroundPackageName)) {


                        List<String> stringList = lnm2file.getEnableApp();
                        String s = mForegroundPackageName;


                        if (!stringList.contains(mForegroundPackageName)) {

                            try {


                                Intent intent = new Intent(getApplicationContext(), StartLearnActivity.class);
                                intent.putExtra(lnmState, lnmStart);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                LogUtils.i("=======" + mForegroundPackageName);

                                LogUtils.file("\n\n正在学习，拦截非白名单应用：" + s);

                                saveLnmLogs(TimeUtils.getNowString() + "：拦截成功\n    -->  " + AppUtils.getAppName(mForegroundPackageName) + "（" + mForegroundPackageName + "）");

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
        return getLearning() && lnmDBUtils.countPending() > 0;
    }


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
