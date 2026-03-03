package io.github.yuzhiang.qxb.model.todo;

public class TodoItem {
    private String id;
    private String title;
    private String category;
    private long dueAt;
    private boolean repeat;
    private String repeatUnit;
    private boolean pinned;
    private boolean important;
    private boolean completed;
    private boolean parentConfirmed;
    private long studentCheckedAt;
    private long parentConfirmedAt;
    private long createdAt;

    public TodoItem() {
    }

    public TodoItem(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getDueAt() {
        return dueAt;
    }

    public void setDueAt(long dueAt) {
        this.dueAt = dueAt;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public String getRepeatUnit() {
        return repeatUnit;
    }

    public void setRepeatUnit(String repeatUnit) {
        this.repeatUnit = repeatUnit;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isImportant() {
        return important;
    }

    public void setImportant(boolean important) {
        this.important = important;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isParentConfirmed() {
        return parentConfirmed;
    }

    public void setParentConfirmed(boolean parentConfirmed) {
        this.parentConfirmed = parentConfirmed;
    }

    public long getStudentCheckedAt() {
        return studentCheckedAt;
    }

    public void setStudentCheckedAt(long studentCheckedAt) {
        this.studentCheckedAt = studentCheckedAt;
    }

    public long getParentConfirmedAt() {
        return parentConfirmedAt;
    }

    public void setParentConfirmedAt(long parentConfirmedAt) {
        this.parentConfirmedAt = parentConfirmedAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
