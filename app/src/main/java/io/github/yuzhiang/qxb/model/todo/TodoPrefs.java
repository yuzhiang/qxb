package io.github.yuzhiang.qxb.model.todo;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.SPUtils;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TodoPrefs {
    private static final String KEY_TODO_GROUPS = "todo_groups_json";
    private static final String KEY_TODO_IMPORTANT = "todo_important_json";
    private static final String KEY_TODO_STICKY = "todo_group_sticky_json";

    public static List<TodoGroup> loadGroups() {
        String json = SPUtils.getInstance().getString(KEY_TODO_GROUPS, "");
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<TodoGroup> groups = GsonUtils.fromJson(json, new TypeToken<List<TodoGroup>>() {
        }.getType());
        return groups == null ? new ArrayList<>() : groups;
    }

    public static void saveGroups(List<TodoGroup> groups) {
        SPUtils.getInstance().put(KEY_TODO_GROUPS, GsonUtils.toJson(groups));
    }

    public static TodoItem loadImportant() {
        String json = SPUtils.getInstance().getString(KEY_TODO_IMPORTANT, "");
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        return GsonUtils.fromJson(json, TodoItem.class);
    }

    public static void saveImportant(TodoItem item) {
        if (item == null) {
            SPUtils.getInstance().remove(KEY_TODO_IMPORTANT);
            return;
        }
        SPUtils.getInstance().put(KEY_TODO_IMPORTANT, GsonUtils.toJson(item));
    }

    public static Set<String> loadStickyGroups() {
        String json = SPUtils.getInstance().getString(KEY_TODO_STICKY, "");
        if (json == null || json.trim().isEmpty()) {
            return new HashSet<>();
        }
        List<String> list = GsonUtils.fromJson(json, new TypeToken<List<String>>() {
        }.getType());
        return list == null ? new HashSet<>() : new HashSet<>(list);
    }

    public static void saveStickyGroups(Set<String> ids) {
        SPUtils.getInstance().put(KEY_TODO_STICKY, GsonUtils.toJson(new ArrayList<>(ids)));
    }
}
