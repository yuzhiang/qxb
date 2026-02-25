package io.github.yuzhiang.qxb.MyUtils;

import static com.blankj.utilcode.util.ConvertUtils.dp2px;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.widget.ImageView;

import com.blankj.utilcode.util.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import io.github.yuzhiang.qxb.R;

/**
 * Description : 图片加载工具类 使用glide框架封装
 */
public class ImageLoaderUtils {

    private static int dp24 = dp2px(24);


    public static void display(Context context, ImageView imageView, String url) {
        display(context, imageView, url, 120);
    }

    public static void display(Context context, ImageView imageView, String url, int dp) {
        if (imageView == null) {
            throw new IllegalArgumentException("argument error");
        }
        if (url != null && !url.trim().isEmpty() && !"https://api.ldr.cool#".equals(url)) {

            RoundedCorners roundedCorners = new RoundedCorners(12);
            //通过RequestOptions扩展功能,override:采样率,因为ImageView就这么大,可以压缩图片,降低内存消耗
            RequestOptions options = RequestOptions.bitmapTransform(roundedCorners).override(dp, dp);

            Glide.with(context).load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .placeholder(R.drawable.ic_default_img)
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);
        }
    }


    public static void displayRoundHead(ImageView imageView, String url, int dp) {
        if (imageView == null) {
            throw new IllegalArgumentException("argument error");
        }
        if (url != null && !url.trim().isEmpty() && !"https://api.ldr.cool#".equals(url)) {

            Glide.with(Utils.getApp()).load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .placeholder(R.drawable.ic_empty_head)
                    .error(R.drawable.ic_empty_head)
                    .centerCrop()
                    .transform(new GlideRoundTransformUtil())
                    .override(dp2px(dp), dp2px(dp))
                    .into(imageView);

        } else {
            imageView.setImageResource(R.drawable.ic_empty_head);
        }
    }

    public static boolean assertValidRequest(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return !isDestroy(activity);
        } else if (context instanceof ContextWrapper) {
            ContextWrapper contextWrapper = (ContextWrapper) context;
            if (contextWrapper.getBaseContext() instanceof Activity) {
                Activity activity = (Activity) contextWrapper.getBaseContext();
                return !isDestroy(activity);
            }
        }
        return true;
    }

    private static boolean isDestroy(Activity activity) {
        if (activity == null) {
            return true;
        }
        return activity.isFinishing() || activity.isDestroyed();
    }


}
