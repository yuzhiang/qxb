package io.github.yuzhiang.qxb.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.github.yuzhiang.qxb.R;
import io.github.yuzhiang.qxb.model.todo.TodoListEntry;
import io.github.yuzhiang.qxb.model.todo.TodoItem;
import io.github.yuzhiang.qxb.model.todo.TodoGroup;
import io.github.yuzhiang.qxb.model.todo.TodoTimeUtils;

public class TodoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface Listener {
        void onGroupClick(int position, TodoListEntry entry);

        void onGroupLongClick(int position, TodoListEntry entry);

        void onItemClick(int position, TodoListEntry entry);

        void onItemCheckedChanged(int position, TodoListEntry entry, boolean checked);
    }

    private static final int TYPE_GROUP = TodoListEntry.TYPE_GROUP;
    private static final int TYPE_ITEM = TodoListEntry.TYPE_ITEM;

    private final List<TodoListEntry> entries = new ArrayList<>();
    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<TodoListEntry> list) {
        entries.clear();
        if (list != null) {
            entries.addAll(list);
        }
        notifyDataSetChanged();
    }


    public TodoListEntry getEntry(int position) {
        if (position < 0 || position >= entries.size()) return null;
        return entries.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return entries.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_GROUP) {
            View view = inflater.inflate(R.layout.item_todo_group, parent, false);
            return new GroupHolder(view);
        }
        View view = inflater.inflate(R.layout.item_todo_task, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TodoListEntry entry = entries.get(position);
        if (holder instanceof GroupHolder) {
            bindGroup((GroupHolder) holder, entry, position);
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

    private void bindGroup(GroupHolder holder, TodoListEntry entry, int position) {
        holder.tvName.setText(entry.getGroup().getName());
        int count = entry.getGroup().getItems() == null ? 0 : entry.getGroup().getItems().size();
        holder.tvCount.setText(count + " 条待办");
        holder.ivSticky.setVisibility(entry.isSticky() ? View.VISIBLE : View.GONE);
        holder.ivExpand.setImageResource(entry.isExpanded() ? android.R.drawable.arrow_down_float
                : android.R.drawable.arrow_up_float);

        boolean groupAllDone = isGroupAllCompleted(entry.getGroup());
        int groupText = groupAllDone ? 0xFF9E9E9E : 0xFF000000;
        int groupSub = groupAllDone ? 0xFFBDBDBD : 0xFF666666;
        holder.tvName.setTextColor(groupText);
        holder.tvCount.setTextColor(groupSub);
        holder.tvCountdown.setTextColor(groupSub);

        if (entry.isExpanded()) {
            holder.tvCountdown.setVisibility(View.GONE);
        } else {
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
            if (nearest > 0) {
                holder.tvCountdown.setVisibility(View.VISIBLE);
                holder.tvCountdown.setText(TodoTimeUtils.formatCountdown(nearest));
            } else {
                holder.tvCountdown.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGroupClick(position, entry);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onGroupLongClick(position, entry);
            }
            return true;
        });
    }

    private void bindItem(ItemHolder holder, TodoListEntry entry, int position) {
        holder.tvTitle.setText(entry.getItem().getTitle());
        if (entry.getItem().isRepeat()) {
            String unit = entry.getItem().getRepeatUnit();
            String label = "重复";
            if (unit != null && !unit.isEmpty()) {
                label = "每" + unit;
            }
            holder.tvRepeat.setText(label);
        } else {
            holder.tvRepeat.setText("不重复");
        }
        holder.tvCountdown.setText(TodoTimeUtils.formatCountdown(entry.getItem().getDueAt()));

        holder.cbDone.setOnCheckedChangeListener(null);
        holder.cbDone.setChecked(entry.getItem().isCompleted());
        holder.cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onItemCheckedChanged(position, entry, isChecked);
            }
        });

        int textColor = entry.getItem().isCompleted() ? 0xFF9E9E9E : 0xFF000000;
        int subColor = entry.getItem().isCompleted() ? 0xFFBDBDBD : 0xFF666666;
        holder.tvTitle.setTextColor(textColor);
        holder.tvRepeat.setTextColor(subColor);
        holder.tvCountdown.setTextColor(subColor);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position, entry);
            }
        });
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class GroupHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvCount;
        TextView tvCountdown;
        ImageView ivSticky;
        ImageView ivExpand;

        GroupHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_group_name);
            tvCount = itemView.findViewById(R.id.tv_group_count);
            tvCountdown = itemView.findViewById(R.id.tv_group_countdown);
            ivSticky = itemView.findViewById(R.id.iv_group_sticky);
            ivExpand = itemView.findViewById(R.id.iv_group_expand);
        }
    }

    static class ItemHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        CheckBox cbDone;
        TextView tvRepeat;
        TextView tvCountdown;

        ItemHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_todo_title);
            cbDone = itemView.findViewById(R.id.cb_todo_done);
            tvRepeat = itemView.findViewById(R.id.tv_todo_repeat);
            tvCountdown = itemView.findViewById(R.id.tv_todo_countdown);
        }
    }
}
