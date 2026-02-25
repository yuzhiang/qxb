package io.github.yuzhiang.qxb.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;

import com.bumptech.glide.Glide;
import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;

import io.github.yuzhiang.qxb.R;
import io.github.yuzhiang.qxb.model.LnmTime;
import io.github.yuzhiang.qxb.model.lnm2file;
import io.github.yuzhiang.qxb.view.headview.IdentityImageView;

import java.math.BigDecimal;
import java.util.List;

public class LnmRecyclerViewAdapter extends BaseQuickAdapter<LnmTime, QuickViewHolder> {

    private static final DiffUtil.ItemCallback<LnmTime> DIFF_CALLBACK = new DiffUtil.ItemCallback<LnmTime>() {
        @Override
        public boolean areItemsTheSame(LnmTime oldItem, LnmTime newItem) {
            if (oldItem.getId() == null || newItem.getId() == null) {
                return oldItem == newItem;
            }
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(LnmTime oldItem, LnmTime newItem) {
            return oldItem.equals(newItem);
        }
    };


    public LnmRecyclerViewAdapter(@Nullable List<LnmTime> data) {

        super(DIFF_CALLBACK);
        submitList(data);

    }

    @Override
    protected QuickViewHolder onCreateViewHolder(Context context, ViewGroup parent, int viewType) {
        return new QuickViewHolder(R.layout.item_lnm, parent);
    }

    @Override
    protected void onBindViewHolder(QuickViewHolder viewHolder, int position, LnmTime lnmTime) {
        Context mContext = getContext();

        TextView nick_name = viewHolder.getView(R.id.item_lnm_nick_name);

        TextView tv_item_lnm_num = viewHolder.getView(R.id.tv_item_lnm_num);
        ImageView iv_item_lnm_num = viewHolder.getView(R.id.iv_item_lnm_num);
        IdentityImageView lnm_all_head = viewHolder.getView(R.id.lnm_all_head);
        lnm_all_head.setUser(lnmTime.getXh(), lnmTime.getAvatar());

        if (position == 0) {
            tv_item_lnm_num.setVisibility(View.INVISIBLE);
            iv_item_lnm_num.setVisibility(View.VISIBLE);

            Glide.with(mContext)
                    .load(R.drawable.ic_prize1)
                    .into(iv_item_lnm_num);

            nick_name.setTextColor(ContextCompat.getColor(mContext, R.color.prize1));
            nick_name.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
        } else if (position == 1) {
            tv_item_lnm_num.setVisibility(View.INVISIBLE);
            iv_item_lnm_num.setVisibility(View.VISIBLE);

            Glide.with(mContext)
                    .load(R.drawable.ic_prize2)
                    .into(iv_item_lnm_num);

            nick_name.setTextColor(ContextCompat.getColor(mContext, R.color.prize2));
            nick_name.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);

        } else if (position == 2) {
            tv_item_lnm_num.setVisibility(View.INVISIBLE);
            iv_item_lnm_num.setVisibility(View.VISIBLE);

            Glide.with(mContext)
                    .load(R.drawable.ic_prize3)
                    .into(iv_item_lnm_num);

            nick_name.setTextColor(ContextCompat.getColor(mContext, R.color.prize3));
            nick_name.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);

        } else {
            iv_item_lnm_num.setVisibility(View.INVISIBLE);
            tv_item_lnm_num.setVisibility(View.VISIBLE);

            tv_item_lnm_num.setText(position + 1 + "");
            nick_name.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextContent));
            nick_name.setTypeface(null, Typeface.NORMAL);

        }

        String time;
        int sum = lnmTime.getSum();
        if (sum < 60) {
            time = sum + "分钟";
        } else {

            BigDecimal b = new BigDecimal(sum / 60.0);
            double f = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            time = f + "小时";
        }

        viewHolder.setText(R.id.item_lnm_mood, lnmTime.getSignature());
        viewHolder.setText(R.id.item_lnm_nick_name, lnmTime.getNickName());

        viewHolder.setText(R.id.item_lnm_rate_time, time);

        if (lnmTime.isUpShow()) {
            viewHolder.getView(R.id.ll_lnm_up).setVisibility(View.VISIBLE);
            viewHolder.setText(R.id.tv_lnm_up, String.valueOf(lnmTime.getLike()));

            if (lnm2file.getStudyModeUps().contains(lnmTime.getId())) {
                viewHolder.setImageResource(R.id.iv_lnm_up, R.drawable.up);
            } else {
                viewHolder.setImageResource(R.id.iv_lnm_up, R.drawable.ybf_thumb_up);
            }
        } else {
            viewHolder.getView(R.id.ll_lnm_up).setVisibility(View.GONE);
        }
    }


}
