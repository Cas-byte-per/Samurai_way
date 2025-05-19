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

public class DeletedTaskListFragment extends Fragment {

    private ArrayList<Task> deletedList = new ArrayList<>();
    private TaskAdapter adapter;
    private SharedPreferences prefs;
    private static final long MAX_AGE = 5L * 24 * 60 * 60 * 1000;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deleted_tasks, container, false);
        String uid = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();
        prefs = requireActivity()
                .getSharedPreferences("tasks_" + uid, Context.MODE_PRIVATE);

        RecyclerView rv = view.findViewById(R.id.completedTaskRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // 👉 Используем удалённый layout при создании ViewHolder
        adapter = new TaskAdapter(deletedList, new TaskAdapter.OnTaskAction() {

            public void onDelete(int position) {
                Task taskToRemove = deletedList.get(position);
                new AlertDialog.Builder(requireActivity())
                        .setTitle("Безвозвратное удаление")
                        .setMessage("Вы точно хотите безвозвратно удалить задачу?")
                        .setPositiveButton("Удалить", (d,w) -> permanentlyRemoveTask(taskToRemove))
                        .setNegativeButton("Отмена", null)
                        .show();
            }

            @Override
            public void onToggle(int position) {
                // восстановление
                confirmRestore(position);
            }

            @Override
            public void onClick(int position) {
                // нет действий
            }
        }, /* isTrashMode=*/ true) {
            @NonNull @Override
            public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View item = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_task, parent, false);
                return new VH(item);
            }
        };

        rv.setAdapter(adapter);
        loadDeletedTasks();
        return view;
    }

    private void loadDeletedTasks() {
        deletedList.clear();
        long now = System.currentTimeMillis();
        String json = prefs.getString("task_list", null);
        if (json == null) return;
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                Task t = Task.fromJson(array.getJSONObject(i));
                if (t.isDeleted() && now - t.getDeletedAt() <= MAX_AGE) {
                    deletedList.add(t);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
    }

    private void permanentlyRemoveTask(Task toRemove) {
        String json = prefs.getString("task_list", null);
        if (json == null) return;
        try {
            JSONArray old = new JSONArray(json);
            JSONArray newArr = new JSONArray();
            for (int i = 0; i < old.length(); i++) {
                JSONObject o = old.getJSONObject(i);
                if (!(o.getString("title").equals(toRemove.getTitle())
                        && o.getString("date").equals(toRemove.getDate()))) {
                    newArr.put(o);
                }
            }
            prefs.edit()
                    .putString("task_list", newArr.toString())
                    .apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        loadDeletedTasks();
        adapter.notifyDataSetChanged();
    }


    private void confirmRestore(int position) {
        Task task = deletedList.get(position);
        new AlertDialog.Builder(requireContext())
                .setTitle("Восстановить задачу?")
                .setMessage("Вы хотите восстановить задачу?\n\nОна отобразится в списке всех задач.")
                .setPositiveButton("Да", (d, w) -> {
                    task.setDeletedAt(0);
                    task.setCompleted(false);
                    updateTaskInPrefs(task);
                    loadDeletedTasks();
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    private void updateTaskInPrefs(Task updated) {
        String json = prefs.getString("task_list", null);
        if (json == null) return;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                if (o.getString("title").equals(updated.getTitle())
                        && o.getString("date").equals(updated.getDate())) {
                    o.put("deletedAt", updated.getDeletedAt());
                    o.put("isCompleted", updated.isCompleted());
                    break;
                }
            }
            prefs.edit().putString("task_list", arr.toString()).apply();
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

