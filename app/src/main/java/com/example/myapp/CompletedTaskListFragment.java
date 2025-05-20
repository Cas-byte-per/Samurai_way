package com.example.myapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CompletedTaskListFragment extends Fragment {
    private ArrayList<Task> completedList = new ArrayList<>();
    private TaskAdapter adapter;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_completed_tasks, container, false);
        String uid = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();
        prefs = requireActivity()
                .getSharedPreferences("tasks_" + uid, Context.MODE_PRIVATE);

        RecyclerView rv = view.findViewById(R.id.completedTaskRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TaskAdapter(completedList, new TaskAdapter.OnTaskAction() {
            @Override
            public void onDelete(int position) {
                Task task = completedList.get(position);
                new AlertDialog.Builder(requireContext())
                        .setTitle("Удаление задачи")
                        .setMessage("Вы точно хотите удалить задачу?\n\nЕсли вы захотите вернуть задачу, зайдите на страницу профиля, затем в корзину. Удалённые задачи хранятся там в течение 5 дней.")
                        .setPositiveButton("Удалить", (d,w) -> {
                            task.setDeletedAt(System.currentTimeMillis());
                            updateTaskInPrefs(task);
                            loadCompletedTasks();
                            adapter.notifyDataSetChanged();
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            }

            @Override
            public void onClick(int position) {
                Task task = completedList.get(position);
                ((MainActivity) requireActivity())
                        .navigateTo(EditTaskFragment.newInstance(position, task));
            }

            @Override
            public void onToggle(int position) {
                // 1. Помечаем задачу как невыполненную
                Task task = completedList.get(position);
                task.setCompleted(false);
                task.setDeletedAt(0);

                // 2. Обновляем в SharedPreferences
                updateTaskInPrefs(task);
                if (task.isNotifyEnabled()) {
                    NotificationUtils.scheduleNotification(requireContext(), task);
                }
                // 3. Перезагружаем список выполненных заново
                loadCompletedTasks();
                adapter.notifyDataSetChanged();
            }

        },false);


        rv.setAdapter(adapter);

        loadCompletedTasks();

        return view;
    }
    private void saveTasks() {
        JSONArray jsonArray = new JSONArray();
        for (Task task : getAllTasksFromPrefs()) {
            try {
                jsonArray.put(task.toJson());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        prefs.edit().putString("task_list", jsonArray.toString()).apply();
    }

    private ArrayList<Task> getAllTasksFromPrefs() {
        ArrayList<Task> allTasks = new ArrayList<>();
        String json = prefs.getString("task_list", null);
        if (json != null) {
            try {
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    allTasks.add(Task.fromJson(obj));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return allTasks;
    }
    private void loadCompletedTasks() {
        completedList.clear();
        String json = prefs.getString("task_list", null);
        if (json == null) return;
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                Task t = Task.fromJson(array.getJSONObject(i));
                if (t.isCompleted() && !t.isDeleted()) {
                    completedList.add(t);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateTaskInPrefs(Task updated) {
        String json = prefs.getString("task_list", null);
        if (json == null) return;
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                if (o.getString("title").equals(updated.getTitle())
                        && o.getString("date").equals(updated.getDate())) {
                    o.put("isCompleted", updated.isCompleted());
                    o.put("deletedAt", updated.getDeletedAt());
                    break;
                }
            }
            prefs.edit().putString("task_list", array.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

