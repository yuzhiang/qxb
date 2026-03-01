package io.github.yuzhiang.qxb.model.todo;

public class TodoListEntry {
    public static final int TYPE_GROUP = 0;
    public static final int TYPE_ITEM = 1;

    private int type;
    private TodoGroup group;
    private TodoItem item;
    private boolean expanded;
    private boolean sticky;

    public static TodoListEntry group(TodoGroup group, boolean expanded, boolean sticky) {
        TodoListEntry entry = new TodoListEntry();
        entry.type = TYPE_GROUP;
        entry.group = group;
        entry.expanded = expanded;
        entry.sticky = sticky;
        return entry;
    }

    public static TodoListEntry item(TodoGroup group, TodoItem item) {
        TodoListEntry entry = new TodoListEntry();
        entry.type = TYPE_ITEM;
        entry.group = group;
        entry.item = item;
        return entry;
    }

    public int getType() {
        return type;
    }

    public TodoGroup getGroup() {
        return group;
    }

    public TodoItem getItem() {
        return item;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public boolean isSticky() {
        return sticky;
    }
}
