package io.github.yuzhiang.qxb.base;

import static com.blankj.utilcode.util.LogUtils.getConfig;
import static io.github.yuzhiang.qxb.common.FileConfig.DIR_LDR_LOGS;

import android.app.Application;
import android.app.DownloadManager;
import android.content.IntentFilter;
import android.os.Build;
import android.webkit.WebView;

import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;

import io.github.yuzhiang.qxb.BuildConfig;
import io.github.yuzhiang.qxb.Service.DownloadCompleteReceiver;

import io.reactivex.rxjava3.exceptions.UndeliverableException;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import lombok.Getter;


public class MyApplication extends Application {

    @Getter
    public static MyApplication instances;


    @Override
    public void onCreate() {
        super.onCreate();

        //Android 9及以上必须设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = getProcessName();
            if (!getPackageName().equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
            }
        }

        instances = this;
        initSdk(this);

        // 使用
        DownloadCompleteReceiver receiver = new DownloadCompleteReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        ContextCompat.registerReceiver(this, receiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);

    }

    public void initSdk(Application application) {
//        用了autoDisposable，会出现这个问题UndeliverableException
        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof UndeliverableException) {
                // Merely log undeliverable exceptions
                LogUtils.e(e.toString());
                LogUtils.file("\n\n关闭rx错误0：" + e);
            } else {
                // Forward all others to current thread's uncaught exception handler
                Thread.currentThread().setUncaughtExceptionHandler((thread, e1) -> {
                    LogUtils.file("\n\n关闭rx错误1：" + e1);
                });
            }
        });

        Utils.init(application);
        if (FileUtils.createOrExistsDir(DIR_LDR_LOGS)) {
            getConfig().setSaveDays(10)
                    .setDir(DIR_LDR_LOGS)
                    .setConsoleSwitch(BuildConfig.DEBUG)
                    .setFilePrefix("yuh");
        }

    }


    @Override
    public String getPackageName() {
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (StackTraceElement element : stackTrace) {
                if ("org.chromium.base.BuildInfo".equalsIgnoreCase(element.getClassName())) {
                    if ("getAll".equalsIgnoreCase(element.getMethodName())) {
                        return "";
                    }
                    break;
                }
            }
        } catch (Exception e) {
            LogUtils.e(e);
        }

        return super.getPackageName();
    }

}
