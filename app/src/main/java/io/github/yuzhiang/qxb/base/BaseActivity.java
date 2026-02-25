package io.github.yuzhiang.qxb.base;

import static io.github.yuzhiang.qxb.common.Constant.ThemeConstant.getThemeArray;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.github.yuzhiang.qxb.MyUtils.StatusBarUtil;
import io.github.yuzhiang.qxb.view.tastytoast.SimToast;

public class BaseActivity extends AppCompatActivity {
    protected String TAG = getClass().getSimpleName();
    private boolean isImmersive = true;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        initTheme();
        if (isImmersive) {
            StatusBarUtil.immersive(this);
//        StatusBarUtil.immersive(this, 0xff000000, 0.1f);
        }
//        BarUtils.setNavBarLightMode(this, true);
//        BarUtils.setNavBarColor(this, ContextCompat.getColor(this, R.color.navigation_color));

        super.onCreate(savedInstanceState);

    }

    //    状态栏沉浸
    public void setImmersive(boolean isImmersive) {
        this.isImmersive = isImmersive;
    }

    //    chen沉浸以后，设置一段距离，防止哪天这个换了其他库，需要大量修改
    public void setImmersiveView(View immersiveView) {
        StatusBarUtil.setPaddingSmart(this, immersiveView);
    }


    protected void initTheme() {

        setTheme(getThemeArray());

    }

    public void toastSl(String msg) {
        SimToast.toastSL(msg);
    }

    public void toastSe(String msg) {
        SimToast.toastSe(msg);
    }

    public void toastSs(String msg) {
        SimToast.toastSs(msg);
    }

    public void toastEL(String msg) {
        SimToast.toastEL(msg);
    }

    public void toastEe(String msg) {
        SimToast.toastEe(msg);
    }

    public void toastEs(String msg) {
        SimToast.toastEs(msg);
    }

    public void toastIL(String msg) {
        SimToast.toastIL(msg);
    }

    public void toastIe(String msg) {
        SimToast.toastIe(msg);
    }

    public void toastIs(String msg) {
        SimToast.toastIs(msg);
    }


    @Override
    public Resources getResources() {
        if (isNeedSystemResConfig()) {
            return super.getResources();
        } else {
            Resources res = super.getResources();
            Configuration config = res.getConfiguration();
            if (config != null && config.fontScale != 1.0f) {
                config.fontScale = 1.0f;
                res.updateConfiguration(config, res.getDisplayMetrics());
            }
            return res;
        }
    }

    // 默认返回true，使用系统资源，如果个别界面不需要，在这些activity中Override this method ，then return false;
    protected boolean isNeedSystemResConfig() {
        return false;
    }


}