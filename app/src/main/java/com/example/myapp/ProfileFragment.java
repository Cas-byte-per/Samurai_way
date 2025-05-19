package com.example.myapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private TextView tvTitle, textPhone, tvLogout;
    private ImageView ivAvatar;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        tvTitle    = view.findViewById(R.id.tvTitle);
        ivAvatar   = view.findViewById(R.id.ivAvatar);
        textPhone  = view.findViewById(R.id.textPhone);
        tvLogout   = view.findViewById(R.id.tvLogout);
        Button btnTrash    = view.findViewById(R.id.btnTrash);
        ImageView btnSettings = view.findViewById(R.id.btnSettings);

        // Заголовок
        String name = (user != null && user.getDisplayName() != null)
                ? user.getDisplayName()
                : "User";
        tvTitle.setText("Ты справишься, " + name + "!");

        // Аватар: сначала «берём» persistable permission, потом отображаем
        if (user != null && user.getPhotoUrl() != null) {
            Uri photoUrl = user.getPhotoUrl();
            if (photoUrl != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .circleCrop()
                        .into(ivAvatar);
            }
        }

        // Телефон
        if (user != null) {
            textPhone.setText(
                    user.getPhoneNumber() != null
                            ? user.getPhoneNumber()
                            : "—"
            );
        }

        // Переход в настройки
        btnSettings.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).setBottomNavVisible(false);
            ((MainActivity) requireActivity()).navigateTo(new SettingsFragment());
        });

        // Удалённые таски
        btnTrash.setOnClickListener(v ->
                ((MainActivity) requireActivity()).navigateTo(new DeletedTaskListFragment())
        );

        // Выход
        tvLogout.setOnClickListener(v -> {
            auth.signOut();
            ((MainActivity) requireActivity()).navigateTo(new AuthChoiceFragment());
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).setBottomNavVisible(true);
    }
}
