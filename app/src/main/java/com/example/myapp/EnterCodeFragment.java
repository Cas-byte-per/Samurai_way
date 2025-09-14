package com.example.myapp;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class EnterCodeFragment extends Fragment {

    private static final String ARG_PHONE = "phone";
    private static final long COUNTDOWN_TIME = 60000;

    private String phoneNumber;
    private String verificationId;
    private String resendPhoneNumber;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    private CountDownTimer timer;
    private FirebaseAuth auth;

    private TextView tvInfo, tvTimer;
    private EditText etCode;
    private Button btnConfirm;

    public static EnterCodeFragment newInstance(String phoneNumber, String verificationId) {
        EnterCodeFragment fragment = new EnterCodeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PHONE, phoneNumber);
        args.putString("verificationId", verificationId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        phoneNumber = getArguments() != null ? getArguments().getString(ARG_PHONE) : "";
        verificationId = getArguments() != null ? getArguments().getString("verificationId") : null;
        resendPhoneNumber = phoneNumber;
        auth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_enter_code, container, false);

        tvInfo = view.findViewById(R.id.tvInfo);
        tvTimer = view.findViewById(R.id.tvTimer);
        etCode = view.findViewById(R.id.etCode);
        btnConfirm = view.findViewById(R.id.btnConfirm);// неправильный id кнопки - ошибка 3

        tvInfo.setText("Сейчас вам придет смс на номер " + maskPhone(phoneNumber));

        btnConfirm.setOnClickListener(v -> verifyCode());

        tvTimer.setOnClickListener(v -> {
            if (tvTimer.getVisibility() == View.VISIBLE) {
                resendCode();
            }
        });

        startCountdown();

        return view;
    }

    private void verifyCode() {
        String code = etCode.getText().toString().trim();

        if (TextUtils.isEmpty(code)) {
            etCode.setError("Введите код");
            return;
        }

        if (verificationId == null) {
            Toast.makeText(getContext(), "Ошибка. Попробуйте войти снова", Toast.LENGTH_LONG).show();
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        requireActivity()
                                .getSharedPreferences("auth", Context.MODE_PRIVATE)
                                .edit()
                                .putBoolean("is_logged_in", true)
                                .apply();

                        Toast.makeText(getContext(), "Успешный вход", Toast.LENGTH_SHORT).show();
                        ((MainActivity) requireActivity()).navigateTo(new TaskListFragment());
                    } else {
                        Toast.makeText(getContext(),
                                "Ошибка подтверждения: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void resendCode() {
        if (TextUtils.isEmpty(resendPhoneNumber)) return;

        PhoneAuthOptions.Builder builder = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(resendPhoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        Toast.makeText(getContext(), "Код подтвержден автоматически", Toast.LENGTH_SHORT).show();
                        ((MainActivity) requireActivity()).navigateTo(new TaskListFragment());
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String newVerificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = newVerificationId;
                        resendToken = token;
                        Toast.makeText(getContext(), "Код отправлен повторно", Toast.LENGTH_SHORT).show();
                        tvTimer.setVisibility(View.GONE);
                        startCountdown();
                    }
                });

        if (resendToken != null) {
            builder.setForceResendingToken(resendToken);
        }

        PhoneAuthProvider.verifyPhoneNumber(builder.build());
    }

    private String maskPhone(String phone) {
        if (phone.length() >= 11) {
            return "+7********" + phone.substring(phone.length() - 2);
        } else {
            return phone;
        }
    }

    private void startCountdown() {
        tvTimer.setVisibility(View.VISIBLE);
        timer = new CountDownTimer(COUNTDOWN_TIME, 1000) {
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvTimer.setText("Не пришел код? 00:" + (seconds < 10 ? "0" + seconds : seconds));
            }

            public void onFinish() {
                tvTimer.setText("Отправить код повторно");
            }
        }.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timer != null) timer.cancel();
    }
    @Override
    public void onResume() {
        super.onResume();
        // Скрываем нижнюю навигацию, если нужно
        ((MainActivity) requireActivity()).setBottomNavVisible(false);
    }
}
