package com.example.myapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapp.NotificationUtils;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskListFragment extends Fragment {

    private List<Task> taskList = new ArrayList<>();
    private TaskAdapter adapter;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);
        RecyclerView taskRecyclerView = view.findViewById(R.id.taskRecyclerView);
        ImageButton btnFilter = view.findViewById(R.id.btnFilter);
        Button btnAddTask = view.findViewById(R.id.btnAddTask);
        ImageButton btnCompleted = view.findViewById(R.id.btnCompletedTasks);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Авторизуйтесь, чтобы продолжить", Toast.LENGTH_SHORT).show();
            ((MainActivity) requireActivity()).navigateTo(new EnterPhoneFragment());
            return view;
        }
        String uid = user.getUid();


        prefs = requireActivity()
                .getSharedPreferences("tasks_" + uid, Context.MODE_PRIVATE);

        loadTasks();

        taskRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TaskAdapter(taskList, new TaskAdapter.OnTaskAction() {
            @Override
            public void onDelete(int position) {
                Task task = taskList.get(position);
                new AlertDialog.Builder(requireContext())
                        .setTitle("Удаление задачи")
                        .setMessage("Вы точно хотите удалить задачу?\n\nЕсли вы захотите вернуть задачу, зайдите на страницу профиля, затем в корзину. Удалённые задачи хранятся там в течение 5 дней.")
                        .setPositiveButton("Удалить", (d,w) -> {
                            task.setDeletedAt(System.currentTimeMillis());
                            updateTaskInPrefs(task);
                            loadTasks();
                            NotificationUtils.cancelNotification(requireContext(), task);
                            adapter.notifyDataSetChanged();
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            }

            @Override
            public void onClick(int position) {
                Task task = taskList.get(position);
                ((MainActivity) requireActivity())
                        .navigateTo(EditTaskFragment.newInstance(position, task));
            }

            @Override
            public void onToggle(int position) {
                Task task = taskList.get(position);
                task.setCompleted(true);
                saveTasks();
                NotificationUtils.cancelNotification(requireContext(), task);
                taskList.remove(position);
                adapter.notifyItemRemoved(position);
            }
        },false);

        taskRecyclerView.setAdapter(adapter);

        btnAddTask.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).navigateTo(new EditTaskFragment());
        });

        btnCompleted.setOnClickListener(v -> {
            ((MainActivity) requireActivity())
                    .navigateTo(new CompletedTaskListFragment());
        });

        btnFilter.setOnClickListener(v -> {
            Collections.sort(taskList, Comparator
                    .comparing(Task::getDate)
                    .thenComparing(Task::getPriority));
            adapter.notifyDataSetChanged();
        });
        getParentFragmentManager().setFragmentResultListener(
                "task_add_result", this, (key, bundle) -> {
                    String title = bundle.getString("title");
                    String date = bundle.getString("date");
                    String time = bundle.getString("time");
                    int priority = bundle.getInt("priority");
                    String description = bundle.getString("description");
                    boolean notifyOn   = bundle.getBoolean("notifyEnabled", false);

                    taskList.add(new Task(title, date, time, priority, description));
                    saveTasks();
                    adapter.notifyItemInserted(taskList.size() - 1);
                    Task newTask = taskList.get(taskList.size()-1);
                    newTask.setNotifyEnabled(notifyOn);
                    if (newTask.isNotifyEnabled()) {
                        NotificationUtils.scheduleNotification(requireContext(), newTask);
                    }
                }
        );

        getParentFragmentManager().setFragmentResultListener(
                "task_update_result", this, (key, bundle) -> {
                    int pos = bundle.getInt("position");
                    String title = bundle.getString("title");
                    String date = bundle.getString("date");
                    String time = bundle.getString("time");
                    int priority = bundle.getInt("priority");
                    String description = bundle.getString("description");
                    boolean notifyOn   = bundle.getBoolean("notifyEnabled", false);

                    Task updated = new Task(title, date, time, priority, description);
                    updated.setNotifyEnabled(notifyOn);
                    taskList.set(pos, updated);
                    saveTasks();
                    adapter.notifyItemChanged(pos);
                    Task updatedTask = taskList.get(pos);
                    if (updatedTask.isNotifyEnabled()) {
                        NotificationUtils.scheduleNotification(requireContext(), updatedTask);
                    } else {
                        NotificationUtils.cancelNotification(requireContext(), updatedTask);
                    }
                }
        );

        getParentFragmentManager().setFragmentResultListener(
                "task_delete_result", this, (key, bundle) -> {
                    int pos = bundle.getInt("position");
                    taskList.remove(pos);
                    saveTasks();
                    adapter.notifyItemRemoved(pos);
                }
        );

        return view;
    }
    private void saveTasks() {
        JSONArray jsonArray = new JSONArray();
        for (Task task : taskList) {
            try {
                jsonArray.put(task.toJson());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // + дополнительно сохраняем все задачи, включая выполненные
        List<Task> allTasks = getAllTasksFromPrefs();
        for (Task task : allTasks) {
            if (task.isCompleted()) {
                try {
                    jsonArray.put(task.toJson());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        prefs.edit()
                .putString("task_list", jsonArray.toString())
                .apply();
    }

    private void loadTasks() {
        taskList.clear();
        String json = prefs.getString("task_list", null);
        if (json != null) {
            try {
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    Task task = Task.fromJson(obj);
                    if (!task.isCompleted() && !task.isDeleted()) {
                        taskList.add(task);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(taskList, Comparator
                .comparing(Task::getDate)
                .thenComparing(Task::getPriority));
    }

    private List<Task> getAllTasksFromPrefs() {
        List<Task> result = new ArrayList<>();
        String json = prefs.getString("task_list", null);
        if (json != null) {
            try {
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    result.add(Task.fromJson(obj));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    private void updateTaskInPrefs(Task updatedTask) {
        String json = prefs.getString("task_list", null);
        if (json == null) return;

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                if (o.getString("title").equals(updatedTask.getTitle())
                        && o.getString("date").equals(updatedTask.getDate())) {
                    o.put("isCompleted", updatedTask.isCompleted());
                    o.put("deletedAt", updatedTask.getDeletedAt());
                    break;
                }
            }
            prefs.edit()
                    .putString("task_list", array.toString())
                    .apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).setBottomNavVisible(true);
    }
}
