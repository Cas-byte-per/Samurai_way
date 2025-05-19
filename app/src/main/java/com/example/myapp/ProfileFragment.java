package com.example.myapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private TextView textEmail, textPhone, tvTitle;
    private ShapeableImageView ivAvatar;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        tvTitle = view.findViewById(R.id.tvTitle);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        textPhone = view.findViewById(R.id.textPhone);
        ImageView btnSettings = view.findViewById(R.id.btnSettings);
        Button btnTrash = view.findViewById(R.id.btnTrash);

        updateTitle(user);
        if (user != null && user.getPhotoUrl() != null) {
            ivAvatar.setImageURI(user.getPhotoUrl());
        }

        btnSettings.setOnClickListener(v -> {
                    ((MainActivity) requireActivity()).setBottomNavVisible(false);
                    ((MainActivity) requireActivity()).navigateTo(new SettingsFragment());
        });
        // Заполняем поля (если пользователь авторизован)
        if (user != null) {
            textPhone.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "—");
        }

        // Обработчик выхода
        TextView tvLogout = view.findViewById(R.id.tvLogout);
        tvLogout.setOnClickListener(v -> {
            auth.signOut();
            ((MainActivity) requireActivity()).navigateTo(new AuthChoiceFragment());
        });
        btnTrash.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).navigateTo(new DeletedTaskListFragment());
        });

        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).setBottomNavVisible(true);
    }
    private void updateTitle(FirebaseUser user) {
        String name = (user != null && user.getDisplayName() != null)
                ? user.getDisplayName()
                : "User";
        tvTitle.setText("Ты справишься, " + name + "!");
    }
}
