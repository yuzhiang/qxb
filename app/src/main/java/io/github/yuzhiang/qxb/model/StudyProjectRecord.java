package io.github.yuzhiang.qxb.model;

public class StudyProjectRecord {
    private String project;
    private long startAt;
    private long endAt;
    private long durationMs;
    private boolean success;

    public StudyProjectRecord() {
    }

    public StudyProjectRecord(String project, long startAt, long endAt, long durationMs, boolean success) {
        this.project = project;
        this.startAt = startAt;
        this.endAt = endAt;
        this.durationMs = durationMs;
        this.success = success;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public long getStartAt() {
        return startAt;
    }

    public void setStartAt(long startAt) {
        this.startAt = startAt;
    }

    public long getEndAt() {
        return endAt;
    }

    public void setEndAt(long endAt) {
        this.endAt = endAt;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
