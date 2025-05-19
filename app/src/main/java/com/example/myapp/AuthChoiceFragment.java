package com.example.myapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AuthChoiceFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_auth_choice, container, false);

        Button btnStart = view.findViewById(R.id.btnStart);
        btnStart.setOnClickListener(v ->
                // Переходим на ваш фрагмент авторизации по телефону
                ((MainActivity) requireActivity()).navigateTo(new EnterPhoneFragment())
        );

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Скрываем нижнюю навигацию, если нужно
        ((MainActivity) requireActivity()).setBottomNavVisible(false);
    }
}
