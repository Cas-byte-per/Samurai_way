package com.example.myapp;

import android.app.AlertDialog; // NEW
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import android.app.DatePickerDialog;
import android.text.InputType;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import java.util.Arrays;



public class EditTaskFragment extends Fragment {

    private static final String ARG_POSITION    = "position";    // NEW
    private static final String ARG_TITLE       = "title";
    private static final String ARG_DATE        = "date";
    private static final String ARG_PRIORITY    = "priority";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_TIME = "time";

    private EditText titleInput, dateInput, descriptionInput, timeInput;
    private Spinner prioritySpinner;
    private ImageView ivToggleDesc;
    private boolean isDescExpanded = false;
    private int position = -1; // NEW

    // NEW: теперь принимаем позицию
    public static EditTaskFragment newInstance(int position, Task task) {
        EditTaskFragment fragment = new EditTaskFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        args.putString(ARG_TITLE, task.getTitle());
        args.putString(ARG_DATE, task.getDate());
        args.putString(ARG_TIME, task.getTime());
        args.putInt(ARG_PRIORITY, task.getPriority());
        args.putString(ARG_DESCRIPTION, task.getDescription());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_task, container, false);

        // findViewById
        titleInput       = view.findViewById(R.id.etTitle);
        dateInput        = view.findViewById(R.id.etDate);
        timeInput = view.findViewById(R.id.etTime);

        // Отключаем ручной ввод и скрываем курсор
        dateInput.setInputType(InputType.TYPE_NULL);
        dateInput.setCursorVisible(false);

        timeInput.setInputType(InputType.TYPE_NULL);
        timeInput.setCursorVisible(false);

        // По клику показываем DatePickerDialog
        dateInput.setOnClickListener(v -> {
            // Текущая дата для инициализации диалога
            final Calendar cal = Calendar.getInstance();
            int year  = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day   = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog picker = new DatePickerDialog(
                    requireContext(),
                    (view1, year1, month1, dayOfMonth) -> {
                        // Форматируем в dd.MM.yy, год %100
                        String formatted = String.format(
                                Locale.getDefault(),
                                "%02d.%02d.%02d",
                                dayOfMonth,
                                month1 + 1,
                                year1 % 100
                        );
                        dateInput.setText(formatted);
                    },
                    year, month, day
            );
            picker.show();
        });
        timeInput.setOnClickListener(v -> {
            final Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            new TimePickerDialog(requireContext(),
                    (view1, h, m) -> {
                        String fmt = String.format(Locale.getDefault(), "%02d:%02d", h, m);
                        timeInput.setText(fmt);
                    },
                    hour, minute, true
            ).show();
        });

        // 1) Находим Spinner
        prioritySpinner = view.findViewById(R.id.spinnerPriority);

        // 2) Готовим список опций
        String[] priorities = {"Высокий", "Средний", "Низкий"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                Arrays.asList(priorities)
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(adapter);

        descriptionInput = view.findViewById(R.id.etDescription);
        ivToggleDesc     = view.findViewById(R.id.ivToggleDesc);
        Button saveButton   = view.findViewById(R.id.btnSave);

        // раскладка описания
        descriptionInput.setMaxLines(1);
        ivToggleDesc.setImageResource(R.drawable.ic_down);
        ivToggleDesc.setOnClickListener(v -> {
            if (isDescExpanded) {
                descriptionInput.setMaxLines(1);
                ivToggleDesc.setImageResource(R.drawable.ic_down);
            } else {
                descriptionInput.setMaxLines(Integer.MAX_VALUE);
                ivToggleDesc.setImageResource(R.drawable.ic_up);
            }
            isDescExpanded = !isDescExpanded;
        });

        // получаем аргументы
        if (getArguments() != null) {
            position = getArguments().getInt(ARG_POSITION, -1); // NEW
            titleInput.setText(getArguments().getString(ARG_TITLE));
            dateInput.setText(getArguments().getString(ARG_DATE));
            timeInput.setText(getArguments().getString(ARG_TIME, ""));
            int prio = getArguments().getInt(ARG_PRIORITY, 1);   // 1–3
            prioritySpinner.setSelection(prio - 1);
            descriptionInput.setText(getArguments().getString(ARG_DESCRIPTION));
        }

        // SAVE логика (как было)
        saveButton.setOnClickListener(v -> {
            String title       = titleInput.getText().toString().trim();
            String date        = dateInput.getText().toString().trim();
            String time = timeInput.getText().toString().trim();
            int prio = prioritySpinner.getSelectedItemPosition() + 1;
            String description = descriptionInput.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Введите название задачи", Toast.LENGTH_SHORT).show();
                return;
            }
            // валидируем дату...
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());
            sdf.setLenient(false);
            Date enteredDate;
            try { enteredDate = sdf.parse(date); }
            catch (ParseException e) {
                Toast.makeText(getContext(), "Дата некорректна. Проверьте день и месяц.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (enteredDate.before(removeTime(new Date()))) {
                Toast.makeText(getContext(), "Нельзя ставить задачу на прошедшую дату", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle result = new Bundle();
            result.putString("title", title);
            result.putString("date", date);
            result.putInt(ARG_PRIORITY, prio);
            result.putString("description", description);
            result.putString("time",time);

            if (getArguments() != null && getArguments().containsKey(ARG_POSITION)) {
                int pos = getArguments().getInt(ARG_POSITION);
                result.putInt("position", pos);
                getParentFragmentManager()
                        .setFragmentResult("task_update_result", result);
            } else {
                getParentFragmentManager()
                        .setFragmentResult("task_add_result", result);
            }

            requireActivity().onBackPressed();
        });

        return view;
    }

    private Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
