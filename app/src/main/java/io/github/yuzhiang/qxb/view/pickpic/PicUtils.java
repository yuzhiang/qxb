package io.github.yuzhiang.qxb.view.pickpic;

import static io.github.yuzhiang.qxb.view.tastytoast.SimToast.toastEs;

import android.app.Activity;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;
import com.hjq.permissions.permission.base.IPermission;
import com.hjq.permissions.permission.dangerous.ReadMediaImagesPermission;
import com.luck.picture.lib.basic.PictureSelectionModel;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.permissions.PermissionChecker;

import io.github.yuzhiang.qxb.MyUtils.GlideEngine;
import io.github.yuzhiang.qxb.view.tastytoast.SimToast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PicUtils {

    public static String getRealLocalPath(LocalMedia media) {
        String path = media.getAvailablePath();
        LogUtils.i(path + "\n" + FileUtils.getSize(new File(path)));
        return path;
    }


    public static void checkPrm() {
        IPermission prm = new ReadMediaImagesPermission();
        Activity activity = ActivityUtils.getTopActivity();
        LogUtils.i(activity);

        LogUtils.i("权限2");
        LogUtils.i(XXPermissions.isGrantedPermission(activity, prm));
//        LogUtils.i(XXPermissions.isPermanentDenied(activity, prm));
        LogUtils.e(PermissionChecker.isCheckWriteExternalStorage(activity));

        if (XXPermissions.isGrantedPermission(activity, prm)) {
            LogUtils.e("权限被赋予");
            LogUtils.e(PermissionChecker.isCheckWriteExternalStorage(activity));

            return;
        }


        XXPermissions.with(activity)
                .permission(prm)
                .request((allGranted, deniedList) -> {
                    if (deniedList.isEmpty()) {
                        SimToast.toastSs("获取权限成功，请重新选图片");
                        return;
                    }
                    if (XXPermissions.isDoNotAskAgainPermissions(activity, deniedList)) {
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(activity, deniedList);
                        SimToast.toastEs("请手动授予读取文件的权限");
                    } else {
                        SimToast.toastEs("获取失败");
                    }
                });
        LogUtils.i("权限3");

    }

    public static PictureSelectionModel picSel() {
        checkPrm();
        LogUtils.i("权限4");

        return PictureSelector.create(ActivityUtils.getTopActivity())
                .openGallery(SelectMimeType.ofImage())
                .setImageEngine(GlideEngine.createGlideEngine())
                .setCropEngine(new ImageCropEngine())
                .setCompressEngine(new ImageFileCompressEngine());
    }


}
