package io.github.yuzhiang.qxb.view.pickpic;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.luck.picture.lib.engine.CropFileEngine;

import io.github.yuzhiang.qxb.MyUtils.ImageLoaderUtils;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropImageEngine;
import com.yalantis.ucrop.model.AspectRatio;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义裁剪
 */
public class ImageCropEngine implements CropFileEngine {

    List<Integer> xs = new ArrayList<>();
    List<Integer> ys = new ArrayList<>();

    public ImageCropEngine(List<Integer> xs, List<Integer> ys) {
        this.xs = xs;
        this.ys = ys;
    }

    public ImageCropEngine(Integer x, Integer y) {
        this.xs.clear();
        this.ys.clear();

        this.xs.add(x);
        this.ys.add(y);
    }

    public ImageCropEngine() {
    }

    @Override
    public void onStartCrop(Fragment fragment, Uri srcUri, Uri destinationUri, ArrayList<String> dataSource, int requestCode) {
        UCrop.Options options = buildOptions(dataSource);
        UCrop uCrop = UCrop.of(srcUri, destinationUri, dataSource);
        uCrop.withOptions(options);
        uCrop.setImageEngine(new UCropImageEngine() {
            @Override
            public void loadImage(Context context, String url, ImageView imageView) {
                if (!ImageLoaderUtils.assertValidRequest(context)) {
                    return;
                }
                Glide.with(context).load(url).override(180, 180).into(imageView);
            }

            @Override
            public void loadImage(Context context, Uri url, int maxWidth, int maxHeight, OnCallbackListener<Bitmap> call) {
                if (!ImageLoaderUtils.assertValidRequest(context)) {
                    return;
                }
                Glide.with(context).asBitmap().override(maxWidth, maxHeight).load(url).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        if (call != null) {
                            call.onCall(resource);
                        }
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        if (call != null) {
                            call.onCall(null);
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
            }
        });
        uCrop.start(fragment.getActivity(), fragment, requestCode);
    }

    /**
     * 创建自定义输出目录
     *
     * @return
     */
    private String getSandboxPath() {
        File externalFilesDir = Utils.getApp().getExternalFilesDir("");
        File customFile = new File(externalFilesDir.getAbsolutePath(), "Sandbox");
        if (!customFile.exists()) {
            customFile.mkdirs();
        }
        return customFile.getAbsolutePath() + File.separator;
    }

    /**
     * 配制UCrop，可根据需求自我扩展
     *
     * @return
     */
    private UCrop.Options buildOptions(ArrayList<String> dataSource) {
        UCrop.Options options = new UCrop.Options();
//        options.setHideBottomControls(!cb_hide.isChecked());
//        options.setFreeStyleCropEnabled(cb_styleCrop.isChecked());
//        options.setShowCropFrame(cb_showCropFrame.isChecked());
//        options.setShowCropGrid(cb_showCropGrid.isChecked());
//        options.setCircleDimmedLayer(cb_crop_circular.isChecked());
//        options.withAspectRatio(aspect_ratio_x, aspect_ratio_y);
        options.setCropOutputPathDir(getSandboxPath());
        options.isCropDragSmoothToCenter(false);
//        options.isUseCustomLoaderBitmap(cb_crop_use_bitmap.isChecked());
        options.isForbidSkipMultipleCrop(false);
//        options.setStatusBarColor(ContextCompat.getColor(getContext(), R.color.ps_color_grey));
//        options.setToolbarColor(ContextCompat.getColor(getContext(), R.color.ps_color_grey));
//        options.setToolbarWidgetColor(ContextCompat.getColor(getContext(), R.color.ps_color_white));

        options.setMultipleCropAspectRatio(buildAspectRatios(dataSource.size()));
        return options;
    }


    /**
     * 多图裁剪时每张对应的裁剪比例
     *
     * @param dataSourceCount
     * @return
     */
    private AspectRatio[] buildAspectRatios(int dataSourceCount) {
        AspectRatio[] aspectRatios = new AspectRatio[dataSourceCount];
        for (int i = 0; i < dataSourceCount; i++) {
            if (i < xs.size()) {
                aspectRatios[i] = new AspectRatio(xs.get(i) + ":" + ys.get(i), xs.get(i), ys.get(i));
            } else {
                aspectRatios[i] = new AspectRatio("原始比例", 0, 0);
            }
        }
        return aspectRatios;
    }

}
