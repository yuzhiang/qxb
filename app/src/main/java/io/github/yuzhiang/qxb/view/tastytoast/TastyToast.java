package io.github.yuzhiang.qxb.view.tastytoast;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import io.github.yuzhiang.qxb.R;


/**
 * Created by rahul on 28/7/16.
 */
public class TastyToast {
    public static final int LENGTH_SHORT = 0;
    public static final int LENGTH_LONG = 1;


    public static final int SUCCESS = 1;
    //    public static final int WARNING = 2;
    public static final int ERROR = 3;
    public static final int INFO = 4;
    public static final int DEFAULT = 5;
    public static final int CONFUSING = 6;

    private static SuccessToastView successToastView;
    //    static WarningToastView warningToastView;
    private static ErrorToastView errorToastView;
    private static InfoToastView infoToastView;
    private static DefaultToastView defaultToastView;
    private static ConfusingToastView confusingToastView;

    public static Toast makeText(Context context, String msg, int length, int type) {

        Toast toast = new Toast(context);


        switch (type) {
            case 1: {
                View layout = LayoutInflater.from(context).inflate(R.layout.success_toast_layout, null, false);
                TextView text = layout.findViewById(R.id.toastMessage);
                text.setText(msg);
                successToastView = layout.findViewById(R.id.successView);
                successToastView.startAnim();
                text.setBackgroundResource(R.drawable.toast_success);
                text.setTextColor(Color.parseColor("#FFFFFF"));
                toast.setView(layout);
                break;
            }
//            case 2: {
//                View layout = LayoutInflater.from(context).inflate(R.layout.warning_toast_layout, null, false);
//
//                TextView text = (TextView) layout.findViewById(R.id.toastMessage);
//                text.setText(msg);
//
//                warningToastView = (WarningToastView) layout.findViewById(R.id.warningView);
//
//
////                SpringSystem springSystem = SpringSystem.create();
////                final Spring spring = springSystem.createSpring();
////                spring.setCurrentValue(1.8);
////                SpringConfig config = new SpringConfig(40, 5);
////                spring.setSpringConfig(config);
////                spring.addListener(new SimpleSpringListener() {
////
////                    @Override
////                    public void onSpringUpdate(Spring spring) {
////                        float value = (float) spring.getCurrentValue();
////                        float scale = (float) (0.9f - (value * 0.5f));
////
////                        warningToastView.setScaleX(scale);
////                        warningToastView.setScaleY(scale);
////                    }
////                });
////                Thread t = new Thread(new Runnable() {
////                    @Override
////                    public void run() {
////                        try {
////                            Thread.sleep(500);
////                        } catch (InterruptedException e) {
////                        }
////                        spring.setEndValue(0.4f);
////                    }
////                });
//
////                t.start();
//                text.setBackgroundResource(R.drawable.warning_toast);
//                text.setTextColor(Color.parseColor("#FFFFFF"));
//                toast.setView(layout);
//                break;
//            }
            case 3: {
                View layout = LayoutInflater.from(context).inflate(R.layout.error_toast_layout, null, false);

                TextView text = layout.findViewById(R.id.toastMessage);
                text.setText(msg);
                errorToastView = layout.findViewById(R.id.errorView);
                errorToastView.startAnim();
                text.setBackgroundResource(R.drawable.toast_error);
                text.setTextColor(Color.parseColor("#FFFFFF"));
                toast.setView(layout);
                break;
            }
            case 4: {
                View layout = LayoutInflater.from(context).inflate(R.layout.info_toast_layout, null, false);

                TextView text = layout.findViewById(R.id.toastMessage);
                text.setText(msg);
                infoToastView = layout.findViewById(R.id.infoView);
                infoToastView.startAnim();
                text.setBackgroundResource(R.drawable.toast_info);
                text.setTextColor(Color.parseColor("#FFFFFF"));
                toast.setView(layout);
                break;
            }
            case 5: {
                View layout = LayoutInflater.from(context).inflate(R.layout.default_toast_layout, null, false);

                TextView text = layout.findViewById(R.id.toastMessage);
                text.setText(msg);
                defaultToastView = layout.findViewById(R.id.defaultView);
                defaultToastView.startAnim();
                text.setBackgroundResource(R.drawable.toast_default);
                text.setTextColor(Color.parseColor("#FFFFFF"));
                toast.setView(layout);
                break;
            }
            case 6: {
                View layout = LayoutInflater.from(context).inflate(R.layout.confusing_toast_layout, null, false);

                TextView text = layout.findViewById(R.id.toastMessage);
                text.setText(msg);
                confusingToastView = layout.findViewById(R.id.confusingView);
                confusingToastView.startAnim();
                text.setBackgroundResource(R.drawable.toast_confusing);
                text.setTextColor(Color.parseColor("#FFFFFF"));
                toast.setView(layout);
                break;
            }
        }
        toast.setDuration(length);
        toast.show();
        return toast;
    }

}
