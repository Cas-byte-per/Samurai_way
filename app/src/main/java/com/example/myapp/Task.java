package com.example.myapp;

import org.json.JSONException;
import org.json.JSONObject;

public class Task {
    private String title;
    private String date;
    private int priority;
    private String description;   // новое поле
    private boolean isCompleted;    // ← новое поле
    private long deletedAt = 0; // 0 — не удалено

    public Task(String title, String date, int priority, String description, boolean isCompleted) {
        this.title = title;
        this.date = date;
        this.priority = priority;
        this.description = description;
        this.isCompleted = isCompleted;
    }
    public Task(String title, String date, int priority, String description) {
        this(title, date, priority, description, false);
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


    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("title", title);
        obj.put("date", date);
        obj.put("priority", priority);
        obj.put("description", description);
        obj.put("isCompleted", isCompleted);
        obj.put("deletedAt", deletedAt);// ← сохраняем флаг
        return obj;
    }

    public static Task fromJson(JSONObject obj) throws JSONException {
        Task task = new Task(
                obj.getString("title"),
                obj.getString("date"),
                obj.getInt("priority"),
                obj.getString("description"),
                obj.optBoolean("isCompleted", false)
        );
        task.setDeletedAt(obj.optLong("deletedAt", 0));
        return task;
    }
}


