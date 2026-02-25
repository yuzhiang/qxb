package io.github.yuzhiang.qxb.view.tastytoast;

import static com.blankj.utilcode.util.Utils.getApp;

public class SimToast {

    public static void toastSL(String msg) {
        TastyToast.makeText(getApp(), msg, TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
    }

    public static void toastSe(String msg) {
        TastyToast.makeText(getApp(), msg, TastyToast.DEFAULT, TastyToast.SUCCESS);
    }

    public static void toastSs(String msg) {
        TastyToast.makeText(getApp(), msg, TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
    }

    public static void toastEL(String msg) {
        TastyToast.makeText(getApp(), msg, TastyToast.LENGTH_LONG, TastyToast.ERROR);
    }

    public static void toastEe(String msg) {
        TastyToast.makeText(getApp(), msg, TastyToast.DEFAULT, TastyToast.ERROR);
    }

    public static void toastEs(String msg) {
        TastyToast.makeText(getApp(), msg, TastyToast.LENGTH_SHORT, TastyToast.ERROR);
    }

    public static void toastIL(String msg) {
        TastyToast.makeText(getApp(), msg, TastyToast.LENGTH_LONG, TastyToast.INFO);
    }

    public static void toastIe(String msg) {
        TastyToast.makeText(getApp(), msg, TastyToast.DEFAULT, TastyToast.INFO);
    }

    public static void toastIs(String msg) {
        TastyToast.makeText(getApp(), msg, TastyToast.LENGTH_SHORT, TastyToast.INFO);
    }

}
