package com.example.myapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingsFragment extends Fragment {

    private static final String IMGBB_API_KEY = "3c3e9cc26aeb95b60797a71f1ab00396";
    private ActivityResultLauncher<String[]> pickImageLauncher;
    private ImageView ivAvatar;
    private FirebaseUser user;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = FirebaseAuth.getInstance().getCurrentUser();

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null || user == null) return;
                    ivAvatar.setImageURI(uri);

                    // Запускаем загрузку в imgbb в фоновом потоке
                    new Thread(() -> uploadToImgbb(uri)).start();
                }
        );
    }

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        ivAvatar        = view.findViewById(R.id.ivAvatar);
        EditText etName = view.findViewById(R.id.etName);
        Button btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);
        Button btnSave         = view.findViewById(R.id.btnSave);
        Button btnClose        = view.findViewById(R.id.btnClose);

        // Подставляем текущее фото и имя
        if (user.getPhotoUrl() != null) {
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .circleCrop()
                        .into(ivAvatar);
            }
        }
        if (user.getDisplayName() != null) {
            etName.setText(user.getDisplayName());
        }

        btnChangeAvatar.setOnClickListener(v ->
                pickImageLauncher.launch(new String[]{"image/*"})
        );

        btnSave.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(getContext(), "Имя не может быть пустым", Toast.LENGTH_SHORT).show();
                return;
            }
            // Обновляем только имя; фото обновится, когда uploadToImgbb вернёт URL
            UserProfileChangeRequest req = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();
            user.updateProfile(req)
                    .addOnSuccessListener(_a ->
                            Toast.makeText(getContext(), "Профиль сохранён", Toast.LENGTH_SHORT).show()
                    );
        });

        btnClose.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );
    }

    private void uploadToImgbb(Uri uri) {
        try {
            // 1) Прочитаем байты картинки и закодируем в Base64
            InputStream is = requireContext()
                    .getContentResolver()
                    .openInputStream(uri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int len;
            while ((len = is.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            is.close();
            String base64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            // 2) Собираем POST-запрос к imgbb
            OkHttpClient client = new OkHttpClient();
            RequestBody form = new FormBody.Builder()
                    .add("key", IMGBB_API_KEY)
                    .add("image", base64)
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgbb.com/1/upload")
                    .post(form)
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new Exception("HTTP " + response.code());

            // 3) Парсим JSON и берём data.url
            String body = response.body().string();
            JsonObject data = new Gson()
                    .fromJson(body, JsonObject.class)
                    .getAsJsonObject("data");
            String imageUrl = data.get("url").getAsString();

            // 4) Обновляем photoUrl у пользователя Firebase
            UserProfileChangeRequest photoReq =
                    new UserProfileChangeRequest.Builder()
                            .setPhotoUri(Uri.parse(imageUrl))
                            .build();
            user.updateProfile(photoReq)
                    .addOnSuccessListener(_a -> {
                        // возвращаемся в UI-поток, чтобы показать Toast
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(
                                        getContext(),
                                        "Аватар загружен в облако!",
                                        Toast.LENGTH_SHORT
                                ).show()
                        );
                    });

        } catch (Exception e) {
            e.printStackTrace();
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(),
                            "Ошибка загрузки: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show()
            );
        }
    }
}
