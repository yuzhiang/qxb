package io.github.yuzhiang.qxb.common.Constant;

import io.github.yuzhiang.qxb.MyUtils.UsrMsgUtils;
import io.github.yuzhiang.qxb.R;

public class ThemeConstant {


    public static int[] themeColorArray = {
            R.color.primary_light_blue,
            R.color.primary_ldr,
            R.color.primary_green,
            R.color.primary_red,
            R.color.pink,
            R.color.primary_purple,
            R.color.primary_purple_de,
            R.color.primary_indigo,
            R.color.orange,
            R.color.primary_blue_grey,
            R.color.primary_ldr_purple,
            R.color.coffee_purple,

    };
    public static int[] themeAccentColorArray = {
            R.color.accent_light_blue,
            R.color.accent_ldr,
            R.color.accent_green,
            R.color.accent_red,
            R.color.pink_trans,
            R.color.accent_purple,
            R.color.accent_purple_de,
            R.color.accent_indigo,
            R.color.orange_trans,
            R.color.accent_blue_grey,
            R.color.accent_ldr_purple,
            R.color.coffee_accent,
    };
    public static String[] themeNameArray = {
            "知乎蓝",//0
            "兰朵蓝",//1
            "护眼绿",//2
            "玫瑰红",//3
            "少女粉",//4
            "基佬紫",//5
            "同志紫",//6
            "上天蓝",//7
            "活力橙",//8
            "低调灰",//9
            "清淡紫",//10
            "咖啡咖",//11

    };
    static int[] themeArray = {
            R.style.light_blueTheme,
            R.style.ldrTheme,
            R.style.greenTheme,
            R.style.RedTheme,
            R.style.pinkTheme,
            R.style.purpleTheme,
            R.style.purple_deTheme,
            R.style.indigoTheme,
            R.style.limeTheme,
            R.style.blue_greyTheme,
            R.style.ldr_purpleTheme,
            R.style.coffeeTheme,

    };
    static int[] themeArray2 = {
            R.style.light_blueTheme2,
            R.style.ldrTheme2,
            R.style.greenTheme2,
            R.style.RedTheme2,
            R.style.pinkTheme2,
            R.style.purpleTheme2,
            R.style.purple_deTheme2,
            R.style.indigoTheme2,
            R.style.limeTheme2,
            R.style.blue_greyTheme2,
            R.style.ldr_purpleTheme2,
            R.style.coffeeTheme2,

    };

    public static int getThemeArray() {

        return themeArray[UsrMsgUtils.getThemeId()];

    }

    public static int getThemeArray2() {

        return themeArray2[UsrMsgUtils.getThemeId()];

    }
}
