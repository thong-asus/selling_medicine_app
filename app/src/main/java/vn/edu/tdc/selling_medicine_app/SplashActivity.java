package vn.edu.tdc.selling_medicine_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.google.gson.Gson;

import vn.edu.tdc.selling_medicine_app.model.User;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DISPLAY_LENGTH = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkUserLoginStatus();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    private void checkUserLoginStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences("informationUser", Context.MODE_PRIVATE);
        String jsonUser = sharedPreferences.getString("informationUser", null);
        Intent intent;

        if (jsonUser != null) {
            Gson gson = new Gson();
            User user = gson.fromJson(jsonUser, User.class);
            if (user != null) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
        } else {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
