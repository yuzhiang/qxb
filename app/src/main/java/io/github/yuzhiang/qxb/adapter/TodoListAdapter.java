package io.github.yuzhiang.qxb.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.github.yuzhiang.qxb.R;
import io.github.yuzhiang.qxb.model.todo.TodoGroup;
import io.github.yuzhiang.qxb.model.todo.TodoItem;
import io.github.yuzhiang.qxb.model.todo.TodoListEntry;
import io.github.yuzhiang.qxb.model.todo.TodoTimeUtils;

public class TodoListAdapter extends BaseQuickAdapter<TodoListEntry, QuickViewHolder> {
    public interface Listener {
        void onGroupClick(int position, TodoListEntry entry);

        void onGroupLongClick(int position, TodoListEntry entry);

        void onItemClick(int position, TodoListEntry entry);

        void onItemCheckedChanged(int position, TodoListEntry entry, boolean checked);
    }

    private static final int TYPE_GROUP = TodoListEntry.TYPE_GROUP;
    private static final int TYPE_ITEM = TodoListEntry.TYPE_ITEM;

    private static final DiffUtil.ItemCallback<TodoListEntry> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NotNull TodoListEntry oldItem, @NotNull TodoListEntry newItem) {
            return entryIdentity(oldItem).equals(entryIdentity(newItem));
        }

        @Override
        public boolean areContentsTheSame(@NotNull TodoListEntry oldItem, @NotNull TodoListEntry newItem) {
            // Todo data is mutable and shared by reference across rebuilds.
            // Always rebinding avoids stale UI when Diff compares two lists
            // that point to the same mutated model instance.
            return false;
        }
    };

    private Listener listener;

    public TodoListAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<TodoListEntry> list) {
        submitList(list == null ? Collections.emptyList() : new ArrayList<>(list));
    }


    public TodoListEntry getEntry(int position) {
        List<TodoListEntry> items = getItems();
        if (position < 0 || position >= items.size()) return null;
        return items.get(position);
    }

    @Override
    protected int getItemViewType(int position, @NotNull List<? extends TodoListEntry> list) {
        TodoListEntry entry = list.get(position);
        return entry == null ? TYPE_ITEM : entry.getType();
    }

    @Override
    protected @NotNull QuickViewHolder onCreateViewHolder(@NotNull Context context, @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_GROUP) {
            View view = inflater.inflate(R.layout.item_todo_group, parent, false);
            return new GroupHolder(view);
        }
        View view = inflater.inflate(R.layout.item_todo_task, parent, false);
        return new ItemHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NotNull QuickViewHolder holder, int position, TodoListEntry entry) {
        if (entry == null) return;
        if (holder instanceof GroupHolder) {
            bindGroup((GroupHolder) holder, entry);
        } else if (holder instanceof ItemHolder) {
            bindItem((ItemHolder) holder, entry, position);
        }
    }


    private boolean isGroupAllCompleted(TodoGroup group) {
        if (group == null || group.getItems() == null || group.getItems().isEmpty()) return false;
        for (TodoItem item : group.getItems()) {
            if (item == null) continue;
            if (!item.isCompleted()) return false;
        }
        return true;
    }

    private void bindGroup(GroupHolder holder, TodoListEntry entry) {
        if (entry.getGroup() == null) return;
        holder.tvName.setText(entry.getGroup().getName());
        int count = entry.getGroup().getItems() == null ? 0 : entry.getGroup().getItems().size();
        holder.tvCount.setText(count + " 项作业");
        holder.ivSticky.setVisibility(entry.isSticky() ? View.VISIBLE : View.INVISIBLE);
        holder.ivExpand.setImageResource(R.drawable.ic_expand);
        holder.ivExpand.setRotation(entry.isExpanded() ? 180f : 0f);

        boolean groupAllDone = isGroupAllCompleted(entry.getGroup());
        int groupText = groupAllDone ? 0xFF9E9E9E : 0xFF000000;
        int groupSub = groupAllDone ? 0xFFBDBDBD : 0xFF666666;
        holder.tvName.setTextColor(groupText);
        holder.tvCount.setTextColor(groupSub);
        holder.tvCountdown.setTextColor(groupSub);

        long nearest = -1;
        if (entry.getGroup().getItems() != null) {
            for (TodoItem item : entry.getGroup().getItems()) {
                if (item == null) continue;
                if (item.isCompleted()) continue;
                long due = item.getDueAt();
                if (nearest < 0 || due < nearest) {
                    nearest = due;
                }
            }
        }
        holder.tvCountdown.setVisibility(View.VISIBLE);
        if (nearest > 0) {
            holder.tvCountdown.setText(TodoTimeUtils.formatCountdown(nearest));
        } else {
            holder.tvCountdown.setText("暂无待到期任务");
        }

        holder.itemView.setOnClickListener(v -> {
            int adapterPos = holder.getBindingAdapterPosition();
            TodoListEntry clicked = getEntry(adapterPos);
            if (listener != null && clicked != null) listener.onGroupClick(adapterPos, clicked);
        });
        holder.itemView.setOnLongClickListener(v -> {
            int adapterPos = holder.getBindingAdapterPosition();
            TodoListEntry clicked = getEntry(adapterPos);
            if (listener != null && clicked != null) listener.onGroupLongClick(adapterPos, clicked);
            return true;
        });
    }

    private void bindItem(ItemHolder holder, TodoListEntry entry, int position) {
        TodoItem item = entry.getItem();
        if (item == null) return;
        holder.tvTitle.setText(item.getTitle());
        if (item.isRepeat()) {
            String unit = item.getRepeatUnit();
            String label = "重复";
            if (unit != null && !unit.isEmpty()) {
                label = "每" + unit;
            }
            holder.tvRepeat.setText(label);
        } else {
            holder.tvRepeat.setText("不重复");
        }
        holder.tvCountdown.setText(TodoTimeUtils.formatCountdown(item.getDueAt()));

        holder.cbDone.setOnCheckedChangeListener(null);
        holder.cbDone.setChecked(item.isCompleted());
        holder.cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int adapterPos = holder.getBindingAdapterPosition();
            TodoListEntry clicked = getEntry(adapterPos);
            if (listener != null && clicked != null) listener.onItemCheckedChanged(adapterPos, clicked, isChecked);
        });

        int textColor = item.isCompleted() ? 0xFF9E9E9E : 0xFF000000;
        int subColor = item.isCompleted() ? 0xFFBDBDBD : 0xFF666666;
        holder.tvTitle.setTextColor(textColor);
        holder.tvRepeat.setTextColor(subColor);
        holder.tvCountdown.setTextColor(subColor);
        if (holder.tvStatus != null) {
            if (!item.isCompleted()) {
                holder.tvStatus.setText("未打卡");
                holder.tvStatus.setTextColor(subColor);
            } else if (item.getStudentCheckedAt() <= 0) {
                holder.tvStatus.setText("已完成");
                holder.tvStatus.setTextColor(subColor);
            } else if (item.isParentConfirmed()) {
                holder.tvStatus.setText("家长已确认");
                holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green));
            } else {
                holder.tvStatus.setText("已打卡，待家长确认");
                holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorWarning));
            }
        }

        holder.itemView.setOnClickListener(v -> {
            int adapterPos = holder.getBindingAdapterPosition();
            TodoListEntry clicked = getEntry(adapterPos);
            if (listener != null && clicked != null) listener.onItemClick(adapterPos, clicked);
        });

        boolean isLastInGroup = true;
        TodoListEntry next = getEntry(position + 1);
        if (next != null && next.getType() == TYPE_ITEM) {
            isLastInGroup = false;
        }
        if (holder.divider != null) {
            holder.divider.setVisibility(isLastInGroup ? View.INVISIBLE : View.VISIBLE);
        }
    }

    private static String entryIdentity(TodoListEntry entry) {
        if (entry.getType() == TYPE_GROUP) {
            TodoGroup group = entry.getGroup();
            return "G:" + (group == null ? "null" : safe(group.getId()));
        }
        TodoGroup group = entry.getGroup();
        TodoItem item = entry.getItem();
        String groupId = group == null ? "null" : safe(group.getId());
        String itemId = item == null ? "null" : safe(item.getId());
        if ("null".equals(itemId)) {
            long due = item == null ? 0L : item.getDueAt();
            String title = item == null ? "" : safe(item.getTitle());
            itemId = title + "@" + due;
        }
        return "I:" + groupId + ":" + itemId;
    }

    private static String safe(@Nullable String s) {
        return Objects.toString(s, "");
    }

    static class GroupHolder extends QuickViewHolder {
        TextView tvName;
        TextView tvCount;
        TextView tvCountdown;
        ImageView ivSticky;
        ImageView ivExpand;

        GroupHolder(@NotNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_group_name);
            tvCount = itemView.findViewById(R.id.tv_group_count);
            tvCountdown = itemView.findViewById(R.id.tv_group_countdown);
            ivSticky = itemView.findViewById(R.id.iv_group_sticky);
            ivExpand = itemView.findViewById(R.id.iv_group_expand);
        }
    }

    static class ItemHolder extends QuickViewHolder {
        TextView tvTitle;
        CheckBox cbDone;
        TextView tvRepeat;
        TextView tvCountdown;
        TextView tvStatus;
        View divider;

        ItemHolder(@NotNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_todo_title);
            cbDone = itemView.findViewById(R.id.cb_todo_done);
            tvRepeat = itemView.findViewById(R.id.tv_todo_repeat);
            tvCountdown = itemView.findViewById(R.id.tv_todo_countdown);
            tvStatus = itemView.findViewById(R.id.tv_todo_status);
            divider = itemView.findViewById(R.id.v_todo_divider);
        }
    }
}
