package com.example.myapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditTaskFragment extends Fragment {

    private static final String ARG_POSITION         = "position";
    private static final String ARG_TITLE            = "title";
    private static final String ARG_DATE             = "date";
    private static final String ARG_TIME             = "time";
    private static final String ARG_PRIORITY         = "priority";
    private static final String ARG_DESCRIPTION      = "description";
    private static final String ARG_NOTIFY_ENABLED   = "notifyEnabled";

    private EditText titleInput, dateInput, timeInput, descriptionInput;
    private Spinner prioritySpinner;
    private ImageView ivToggleDesc;
    private Button saveButton;
    private boolean isDescExpanded = false;
    private int position = -1;

    private Switch switchNotify;

    public static EditTaskFragment newInstance(int position, Task task) {
        EditTaskFragment fragment = new EditTaskFragment();
        Bundle args = new Bundle();
        args.putInt   (ARG_POSITION,       position);
        args.putString(ARG_TITLE,          task.getTitle());
        args.putString(ARG_DATE,           task.getDate());
        args.putString(ARG_TIME,           task.getTime());
        args.putInt   (ARG_PRIORITY,       task.getPriority());
        args.putString(ARG_DESCRIPTION,    task.getDescription());
        args.putBoolean(ARG_NOTIFY_ENABLED,   task.isNotifyEnabled());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_task, container, false);

        // findViewById
        titleInput          = view.findViewById(R.id.etTitle);
        dateInput           = view.findViewById(R.id.etDate);
        timeInput           = view.findViewById(R.id.etTime);
        descriptionInput    = view.findViewById(R.id.etDescription);
        ivToggleDesc        = view.findViewById(R.id.ivToggleDesc);
        saveButton          = view.findViewById(R.id.btnSave);
        prioritySpinner     = view.findViewById(R.id.spinnerPriority);
        switchNotify        = view.findViewById(R.id.switchNotify);

        // отключаем ручной ввод для даты и времени
        dateInput.setInputType(InputType.TYPE_NULL);
        dateInput.setCursorVisible(false);
        timeInput.setInputType(InputType.TYPE_NULL);
        timeInput.setCursorVisible(false);

        // DatePickerDialog
        dateInput.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(),
                    (DatePicker dp, int y, int m, int d) -> {
                        String formatted = String.format(
                                Locale.getDefault(), "%02d.%02d.%02d", d, m + 1, y % 100);
                        dateInput.setText(formatted);
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // TimePickerDialog
        timeInput.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new TimePickerDialog(requireContext(),
                    (tp, h, m) -> {
                        String fmt = String.format(Locale.getDefault(), "%02d:%02d", h, m);
                        timeInput.setText(fmt);
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
            ).show();
        });

        // приоритеты
        String[] priorities = {"Высокий", "Средний", "Низкий"};
        ArrayAdapter<String> priAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                Arrays.asList(priorities)
        );
        priAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(priAdapter);

        // разворот описания
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



        // заполнение полей при редактировании
        if (getArguments() != null) {
            position = getArguments().getInt(ARG_POSITION, -1);
            titleInput.setText(getArguments().getString(ARG_TITLE));
            dateInput.setText(getArguments().getString(ARG_DATE));
            timeInput.setText(getArguments().getString(ARG_TIME, ""));
            int prio = getArguments().getInt(ARG_PRIORITY, 1);
            prioritySpinner.setSelection(prio - 1);
            descriptionInput.setText(getArguments().getString(ARG_DESCRIPTION));

            boolean notifyOn = getArguments().getBoolean(ARG_NOTIFY_ENABLED, false);
            switchNotify.setChecked(notifyOn);
        }

        // сохранение
        saveButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String date  = dateInput.getText().toString().trim();
            String time  = timeInput.getText().toString().trim();
            int prio     = prioritySpinner.getSelectedItemPosition() + 1;
            String desc  = descriptionInput.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Введите название задачи", Toast.LENGTH_SHORT).show();
                return;
            }
            // проверка даты
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());
            sdf.setLenient(false);
            Date enteredDate;
            try {
                enteredDate = sdf.parse(date);
            } catch (ParseException e) {
                Toast.makeText(getContext(), "Дата некорректна. Проверьте день и месяц.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (enteredDate.before(removeTime(new Date()))) {
                Toast.makeText(getContext(), "Нельзя ставить задачу на прошедшую дату", Toast.LENGTH_SHORT).show();
                return;
            }
            // проверка времени
            if (!time.isEmpty()) {
                String dt = date + " " + time;
                SimpleDateFormat sdfDT = new SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault());
                sdfDT.setLenient(false);
                Date enteredDateTime;
                try {
                    enteredDateTime = sdfDT.parse(dt);
                } catch (ParseException e) {
                    Toast.makeText(getContext(), "Время некорректно. Проверьте часы и минуты.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (enteredDateTime.before(new Date())) {
                    Toast.makeText(getContext(), "Нельзя ставить дедлайн в прошлом", Toast.LENGTH_SHORT).show();
                    return;
                }

            }

            // параметры уведомления
            boolean notifyOn = switchNotify.isChecked();

            Bundle result = new Bundle();
            result.putString("title",       title);
            result.putString("date",        date);
            result.putString("time",        time);
            result.putInt   (ARG_PRIORITY,  prio);
            result.putString("description", desc);
            result.putBoolean(ARG_NOTIFY_ENABLED, notifyOn);

            if (getArguments() != null && getArguments().containsKey(ARG_POSITION)) {
                result.putInt("position", getArguments().getInt(ARG_POSITION));
                getParentFragmentManager().setFragmentResult("task_update_result", result);
            } else {
                getParentFragmentManager().setFragmentResult("task_add_result", result);
            }

            requireActivity().onBackPressed();
        });

        return view;
    }

    private Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY,   0);
        cal.set(Calendar.MINUTE,        0);
        cal.set(Calendar.SECOND,        0);
        cal.set(Calendar.MILLISECOND,   0);
        return cal.getTime();
    }
}
