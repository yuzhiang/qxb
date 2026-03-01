package io.github.yuzhiang.qxb.model.todo;

import java.util.ArrayList;
import java.util.List;

public class TodoGroup {
    private String id;
    private String name;
    private List<TodoItem> items = new ArrayList<>();

    public TodoGroup() {
    }

    public TodoGroup(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TodoItem> getItems() {
        return items;
    }

    public void setItems(List<TodoItem> items) {
        this.items = items;
    }
}
