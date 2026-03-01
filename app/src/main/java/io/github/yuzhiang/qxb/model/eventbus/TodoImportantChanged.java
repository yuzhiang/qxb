package io.github.yuzhiang.qxb.model.eventbus;

import io.github.yuzhiang.qxb.model.todo.TodoItem;

public class TodoImportantChanged {
    private final TodoItem item;

    public TodoImportantChanged(TodoItem item) {
        this.item = item;
    }

    public TodoItem getItem() {
        return item;
    }
}
