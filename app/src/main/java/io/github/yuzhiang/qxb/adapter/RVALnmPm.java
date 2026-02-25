package io.github.yuzhiang.qxb.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;

import io.github.yuzhiang.qxb.R;
import io.github.yuzhiang.qxb.model.LnmPermission;

import java.util.List;

public class RVALnmPm extends BaseQuickAdapter<LnmPermission, QuickViewHolder> {

    private static final DiffUtil.ItemCallback<LnmPermission> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(LnmPermission oldItem, LnmPermission newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(LnmPermission oldItem, LnmPermission newItem) {
            return oldItem.getId() == newItem.getId()
                    && oldItem.getPmIcon() == newItem.getPmIcon()
                    && oldItem.getPmOk() == newItem.getPmOk()
                    && String.valueOf(oldItem.getPmName()).equals(String.valueOf(newItem.getPmName()))
                    && String.valueOf(oldItem.getPmSummary()).equals(String.valueOf(newItem.getPmSummary()))
                    && String.valueOf(oldItem.getPmOkSum()).equals(String.valueOf(newItem.getPmOkSum()));
        }
    };


    public RVALnmPm(@Nullable List<LnmPermission> permissions) {

        super(DIFF_CALLBACK);
        submitList(permissions);

    }


    @Override
    protected QuickViewHolder onCreateViewHolder(Context context, ViewGroup parent, int viewType) {
        return new QuickViewHolder(R.layout.item_permission, parent);
    }

    @Override
    protected void onBindViewHolder(QuickViewHolder viewHolder, int position, LnmPermission pm) {


        TextView tv = viewHolder.getView(R.id.tv_item_pm_ok);
        ImageView iv = viewHolder.getView(R.id.item_pm_ok);
        viewHolder.setImageResource(R.id.item_pm_icon, pm.getPmIcon());
        viewHolder.setText(R.id.item_pm_name, pm.getPmName());
        viewHolder.setText(R.id.item_pm_summary, pm.getPmSummary());
        if (pm.getPmOk() == 1) {
            tv.setVisibility(View.GONE);
            iv.setVisibility(View.VISIBLE);
            viewHolder.setImageResource(R.id.item_pm_ok, R.drawable.ic_right);
            viewHolder.setTextColorRes(R.id.item_pm_summary, R.color.normal);
        } else if (pm.getPmOk() == 0) {
            tv.setVisibility(View.GONE);
            iv.setVisibility(View.VISIBLE);
            viewHolder.setImageResource(R.id.item_pm_ok, R.drawable.ic_error);
            viewHolder.setTextColorRes(R.id.item_pm_summary, R.color.colorAccent);

        } else {
            iv.setVisibility(View.GONE);
            tv.setVisibility(View.VISIBLE);
            viewHolder.setText(R.id.tv_item_pm_ok, pm.getPmOkSum());
            viewHolder.setTextColorRes(R.id.item_pm_summary, R.color.normal);

        }


    }


}
