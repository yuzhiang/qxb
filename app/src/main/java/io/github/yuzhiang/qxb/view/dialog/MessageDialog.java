package io.github.yuzhiang.qxb.view.dialog;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.StringRes;

import com.blankj.utilcode.util.LogUtils;

import io.github.yuzhiang.qxb.R;
import io.github.yuzhiang.qxb.base.BaseDialog;


/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2018/12/2
 * desc   : 消息对话框
 */
public final class MessageDialog {

    public static final class Builder
            extends UIDialog.Builder<Builder> {

        private OnListener mListener;

        private final TextView mMessageView;

        public Builder(Context context) {
            super(context);
            setCustomView(R.layout.message_dialog);
            setAnimStyle(BaseDialog.ANIM_IOS);
            mMessageView = findViewById(R.id.tv_message_message);
            mMessageView.setMovementMethod(ScrollingMovementMethod.getInstance());
        }

        public Builder setMessage(@StringRes int id) {
            return setMessage(getString(id));
        }

        public Builder setMessage(CharSequence text) {
            mMessageView.setText(text);
            return this;
        }

        public Builder setMessageHtml(String text) {
            Spanned spanned = Html.fromHtml(text);
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(spanned);
            URLSpan[] urls = spannableStringBuilder.getSpans(0, spanned.length(), URLSpan.class);
            for (URLSpan url : urls) {
                MyURLSpan myURLSpan = new MyURLSpan(url.getURL());
                int start = spannableStringBuilder.getSpanStart(url);
                int end = spannableStringBuilder.getSpanEnd(url);
                int flags = spannableStringBuilder.getSpanFlags(url);
                spannableStringBuilder.setSpan(myURLSpan, start, end, flags);
                //一定要加上这一句,看过很多网上的方法，都没加这一句，导致ClickableSpan的onClick方法没有回调，直接用浏览器打开了
//                spannableStringBuilder.removeSpan(url);
            }

            mMessageView.setTextIsSelectable(false);

            mMessageView.setText(spannableStringBuilder);
            mMessageView.setMovementMethod(LinkMovementMethod.getInstance());

            return this;
        }

        public Builder setListener(OnListener listener) {
            mListener = listener;
            return this;
        }

        public Builder setTextGravity(int textGravity) {
            mMessageView.setGravity(textGravity);
            return this;
        }

        @Override
        public BaseDialog create() {
            // 如果内容为空就抛出异常
//            if ("".equals(mMessageView.getText().toString())) {
//                throw new IllegalArgumentException("Dialog message not null");
//            }
            return super.create();
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_ui_confirm:
                    autoDismiss();
                    if (mListener != null) {
                        mListener.onConfirm(getDialog());
                    }
                    break;
                case R.id.tv_ui_cancel:
                    autoDismiss();
                    if (mListener != null) {
                        mListener.onCancel(getDialog());
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private static class MyURLSpan extends ClickableSpan {
        private String mUrl;

        MyURLSpan(String url) {
            mUrl = url;
        }

        @Override
        public void onClick(View widget) {
            LogUtils.i(mUrl);
            Context context = widget.getContext();
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl)));
        }
    }

    public interface OnListener {

        /**
         * 点击确定时回调
         */
        void onConfirm(BaseDialog dialog);

        /**
         * 点击取消时回调
         */
        default void onCancel(BaseDialog dialog) {
        }
    }
}
