package com.example.myapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);


        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container,
                            isLoggedIn
                                    ? new TaskListFragment()
                                    : new EnterPhoneFragment()
                    )
                    .commit();
        }

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setOnItemSelectedListener(item -> {
            Fragment f = item.getItemId() == R.id.nav_profile
                    ? new ProfileFragment()
                    : new TaskListFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, f)
                    .commit();
            return true;
        });
    }

    public void navigateTo(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void logout() {
        // Сбрасываем флаг и возвращаем на экран выбора входа
        getSharedPreferences("auth", MODE_PRIVATE)
                .edit()
                .remove("is_logged_in")
                .apply();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new AuthChoiceFragment())
                .commit();
    }
    public void setBottomNavVisible(boolean visible) {
        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setVisibility(visible ? View.VISIBLE : View.GONE);
    }


}
