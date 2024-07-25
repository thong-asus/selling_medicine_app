package vn.edu.tdc.selling_medicine_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import vn.edu.tdc.selling_medicine_app.feature.TokenService;
import vn.edu.tdc.selling_medicine_app.fragment.HomeFragment;
import vn.edu.tdc.selling_medicine_app.fragment.ScanFragment;
import vn.edu.tdc.selling_medicine_app.fragment.SettingsFragment;
import vn.edu.tdc.selling_medicine_app.model.User;

public class MainActivity extends AppCompatActivity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ////////////////////////get token/////////////////////////////////
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, TokenService.class);
        startService(intent);
        ////////////////////////get token/////////////////////////////////
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.navigation_barcode) {
                    selectedFragment = new ScanFragment();
                } else if (itemId == R.id.navigation_setting) {
                    selectedFragment = new SettingsFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                }

                return true;
            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }
    }
}