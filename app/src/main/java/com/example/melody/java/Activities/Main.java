package com.example.melody.java.Activities;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.melody.R;
import com.example.melody.java.fragments.FavoriteFragment;
import com.example.melody.java.fragments.HomeFragment;
import com.example.melody.java.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Main extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    Fragment currentPage;
    private Fragment homeFragment, favoritFragment, profileFragment ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.main_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;});

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        homeFragment = new HomeFragment();
        profileFragment = new ProfileFragment();
        favoritFragment=new FavoriteFragment();


        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_home) {
                    currentPage = homeFragment;
                } else if (item.getItemId() == R.id.nav_fav) {
                    currentPage = favoritFragment;
                } else if (item.getItemId() == R.id.nav_profile) {
                    currentPage = profileFragment;
                }
                updateFragment();
                return true;
            }
        });

        String page = getIntent().getStringExtra("page");
        if (page != null) {
            if (page.equals("PROFILE")) {
                currentPage = profileFragment;
                bottomNavigationView.setSelectedItemId(R.id.nav_profile);
                updateFragment();
            }
        } else {
            currentPage = homeFragment;
            updateFragment();
        }


    }

    @Override
    public void onBackPressed() {
        if (currentPage.equals(homeFragment)) {
            super.onBackPressed();
        } else {
            currentPage = homeFragment;
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }
    void updateFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (currentPage == homeFragment) {
            // If the current page is HomeFragment, just show it
            transaction.show(homeFragment);
        } else {
            // If the current page is not HomeFragment, hide it
            transaction.hide(homeFragment);
        }
        // Show the current page fragment
        transaction
                .setCustomAnimations(R.anim.slide_in, R.anim.slide_out, R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.frame_container, currentPage)
                .setReorderingAllowed(true)
                .commit();
    }

}
