package io.github.yuzhiang.qxb.model;

import android.graphics.drawable.Drawable;

import lombok.Data;

@Data
public class LnmApp {

    public String appName;
    public String appPackageName;
    public Drawable appIcon;
    public boolean appSelect;
    public float appSearchSimilarity;

    public LnmApp(String appName, String appPackageName, Drawable appIcon, Boolean appSelect, Float appSearchSimilarity) {
        super(); //step 1

        this.appName = appName;
        this.appPackageName = appPackageName;
        this.appIcon = appIcon;
        this.appSelect = appSelect;
        this.appSearchSimilarity = appSearchSimilarity;

    }

    public LnmApp(String appName, String appPackageName) {
        super(); //step 1

        this.appName = appName;
        this.appPackageName = appPackageName;

    }

    public LnmApp(String appName, Drawable appIcon, String appPackageName) {
        super(); //step 1

        this.appName = appName;
        this.appIcon = appIcon;
        this.appPackageName = appPackageName;

    }

}
