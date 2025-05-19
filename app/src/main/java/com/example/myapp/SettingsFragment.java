package com.example.myapp;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SettingsFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private ActivityResultLauncher<String> pickImageLauncher;

    private EditText etName;
    private ImageView ivAvatar;
    private Button btnClose;
    private Button btnSave;
    private Button btnChangeAvatar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Лаунчер для выбора изображения из галереи
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;
                    ivAvatar.setImageURI(uri);
                    if (user != null) {
                        // Обновляем фото в профиле Firebase
                        UserProfileChangeRequest photoReq = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();
                        user.updateProfile(photoReq);
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Подключаем разметку fragment_settings.xml (без поля телефона)
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Привязываем элементы UI (без etPhone)
        etName          = view.findViewById(R.id.etName);
        ivAvatar        = view.findViewById(R.id.ivAvatar);
        btnClose        = view.findViewById(R.id.btnClose);
        btnSave         = view.findViewById(R.id.btnSave);
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);

        // Заполняем текущее имя и аватар
        if (user != null) {
            if (user.getDisplayName() != null) etName.setText(user.getDisplayName());
            if (user.getPhotoUrl() != null) ivAvatar.setImageURI(user.getPhotoUrl());
        }

        // Открыть галерею для выбора аватара
        btnChangeAvatar.setOnClickListener(v ->
                pickImageLauncher.launch("image/*")
        );

        // Закрыть без сохранения
        btnClose.setOnClickListener(v ->
                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack()
        );
        // Сохранить изменения (только имя и аватар)
        btnSave.setOnClickListener(v -> {
            if (user == null) {
                Toast.makeText(getContext(), "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
                return;
            }

            String newName = etName.getText().toString().trim();

            UserProfileChangeRequest profileReq = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();

            user.updateProfile(profileReq)
                    .addOnSuccessListener(aVoid -> {
                        // Перезагружаем данные пользователя
                        user.reload()
                                .addOnSuccessListener(aVoid2 -> {
                                    Toast.makeText(getContext(), "Профиль сохранён", Toast.LENGTH_SHORT).show();
                                    // Возвращаемся назад
                                    requireActivity()
                                            .getSupportFragmentManager()
                                            .popBackStack();
                                })
                                .addOnFailureListener(err ->
                                        Toast.makeText(getContext(),
                                                "Не удалось перезагрузить профиль: " + err.getMessage(),
                                                Toast.LENGTH_SHORT).show()
                                );
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(),
                                    "Ошибка сохранения: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show()
                    );
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).setBottomNavVisible(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity) requireActivity()).setBottomNavVisible(true);
    }
}
