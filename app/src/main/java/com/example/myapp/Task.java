package com.example.myapp;

import org.json.JSONException;
import org.json.JSONObject;

public class Task {
    private String title;
    private String date;
    private String time;
    private int priority;
    private String description;   // новое поле
    private boolean isCompleted;    // ← новое поле
    private long deletedAt = 0; // 0 — не удалено
    private boolean notifyEnabled;
    private int notifyBeforeHours;
    private long id;              // уникальный идентификатор задачи

    public Task(String title, String date, String time, int priority, String description, boolean isCompleted,
                boolean notifyEnabled, int notifyBeforeHours) {
        this.title = title;
        this.date = date;
        this.time = time;
        this.priority = priority;
        this.description = description;
        this.isCompleted = isCompleted;
        this.notifyEnabled     = notifyEnabled;
        this.notifyBeforeHours = notifyBeforeHours;
        this.id = System.currentTimeMillis();
    }
    public Task(String title, String date, String time, int priority, String description) {
        this(title, date, time, priority, description, false, false, 1);
    }
    public long getDeletedAt() { return deletedAt; }
    public void setDeletedAt(long timestamp) { this.deletedAt = timestamp; }
    public boolean isDeleted() { return deletedAt > 0; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public int getPriority() { return priority; }
    public String getDescription() { return description; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public boolean isNotifyEnabled() { return notifyEnabled; }
    public void setNotifyEnabled(boolean notifyEnabled) { this.notifyEnabled = notifyEnabled; }
    public int getNotifyBeforeHours() { return notifyBeforeHours; }
    public void setNotifyBeforeHours(int notifyBeforeHours) { this.notifyBeforeHours = notifyBeforeHours; }



    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("title", title);
        obj.put("date", date);
        obj.put("time", time);
        obj.put("priority", priority);
        obj.put("description", description);
        obj.put("isCompleted", isCompleted);
        obj.put("deletedAt", deletedAt);// ← сохраняем флаг
        obj.put("notifyEnabled", notifyEnabled);
        obj.put("notifyBeforeHours", notifyBeforeHours);
        return obj;
    }

    public static Task fromJson(JSONObject obj) throws JSONException {
        Task task = new Task(
                obj.getString("title"),
                obj.getString("date"),
                obj.optString("time", ""),
                obj.getInt("priority"),
                obj.getString("description"),
                obj.optBoolean("isCompleted", false),
                obj.optBoolean("notifyEnabled", false),
                obj.optInt("notifyBeforeHours", 1)
        );
        task.setDeletedAt(obj.optLong("deletedAt", 0));
        return task;
    }
}


