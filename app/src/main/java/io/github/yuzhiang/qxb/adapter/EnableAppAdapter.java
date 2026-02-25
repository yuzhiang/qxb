package io.github.yuzhiang.qxb.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;

import io.github.yuzhiang.qxb.R;
import io.github.yuzhiang.qxb.model.LnmApp;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class EnableAppAdapter extends BaseQuickAdapter<LnmApp, QuickViewHolder> {

    private static final DiffUtil.ItemCallback<LnmApp> DIFF_CALLBACK = new DiffUtil.ItemCallback<LnmApp>() {
        @Override
        public boolean areItemsTheSame(@NotNull LnmApp oldItem, @NotNull LnmApp newItem) {
            return String.valueOf(oldItem.getAppPackageName()).equals(String.valueOf(newItem.getAppPackageName()));
        }

        @Override
        public boolean areContentsTheSame(@NotNull LnmApp oldItem, @NotNull LnmApp newItem) {
            return oldItem.equals(newItem);
        }
    };


    public EnableAppAdapter(@Nullable List<LnmApp> apps) {
        super(DIFF_CALLBACK);
        submitList(apps);
    }

    @Override
    protected @NotNull QuickViewHolder onCreateViewHolder(@NotNull Context context, @NotNull ViewGroup parent, int viewType) {
        return new QuickViewHolder(R.layout.item_image, parent);
    }

    @Override
    protected void onBindViewHolder(@NotNull QuickViewHolder viewHolder, int position, LnmApp app) {

        viewHolder.setImageDrawable(R.id.iv_item, app.getAppIcon());
    }

}
