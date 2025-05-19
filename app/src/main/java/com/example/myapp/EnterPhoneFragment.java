package com.example.myapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class EnterPhoneFragment extends Fragment {

    private EditText etPhoneOrEmail;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_enter_phone, container, false);

        etPhoneOrEmail = view.findViewById(R.id.etPhoneOrEmail);
        ImageButton btnNext = view.findViewById(R.id.btnNext);

        auth = FirebaseAuth.getInstance();

        btnNext.setOnClickListener(v -> {
            String input = etPhoneOrEmail.getText().toString().trim();

            if (TextUtils.isEmpty(input)) {
                etPhoneOrEmail.setError("Введите номер телефона");
                return;
            }

            if (!input.startsWith("+7") && !input.startsWith("7")) {
                input = "+7" + input.replaceAll("[^\\d]", "");
            }

            sendCode(input);
        });

        return view;
    }

    private void sendCode(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull com.google.firebase.auth.PhoneAuthCredential credential) {
                        // Автоматически подтвердилось — можно сразу войти
                        Toast.makeText(getContext(), "Код подтвержден автоматически", Toast.LENGTH_SHORT).show();
                        ((MainActivity) requireActivity()).navigateTo(new TaskListFragment());
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        Toast.makeText(getContext(), "Код отправлен", Toast.LENGTH_SHORT).show();
                        // Переход во фрагмент ввода кода
                        ((MainActivity) requireActivity()).navigateTo(
                                EnterCodeFragment.newInstance(phoneNumber, verificationId));
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    @Override
    public void onResume() {
        super.onResume();
        // Скрываем нижнюю навигацию, если нужно
        ((MainActivity) requireActivity()).setBottomNavVisible(false);
    }
}
