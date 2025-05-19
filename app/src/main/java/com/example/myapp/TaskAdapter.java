package com.example.myapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.VH> {
    public interface OnTaskAction {
        void onDelete(int position);
        void onClick(int position);
        void onToggle(int position);
    }

    private final List<Task> tasks;
    private final OnTaskAction listener;
    private final boolean isTrashMode;

    public TaskAdapter(List<Task> tasks, OnTaskAction listener, boolean isTrashMode) {
        this.tasks = tasks;
        this.listener = listener;
        this.isTrashMode = isTrashMode;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new VH(item);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Task task = tasks.get(position);

        String title = task.getTitle();
        String date = task.getDate();
        holder.taskInfo.setText(title + " (" + date + ")");

        @DrawableRes int iconRes;
        if (isTrashMode) {
            iconRes = R.drawable.ic_deleted;
        } else {
            iconRes = task.isCompleted()
                    ? R.drawable.ic_checkbox_checked
                    : R.drawable.ic_checkbox_empty;
        }
        holder.checkbox.setImageResource(iconRes);

        holder.checkbox.setOnClickListener(v -> listener.onToggle(position));
        holder.itemView.setOnClickListener(v -> listener.onClick(position));
        holder.deleteBtn.setOnClickListener(v -> listener.onDelete(position));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView taskInfo;
        ImageView checkbox;
        ImageButton deleteBtn;

        VH(@NonNull View itemView) {
            super(itemView);
            taskInfo = itemView.findViewById(R.id.tvTitle);
            checkbox = itemView.findViewById(R.id.btnToggleComplete);
            deleteBtn = itemView.findViewById(R.id.btnDelete);
        }
    }

}
