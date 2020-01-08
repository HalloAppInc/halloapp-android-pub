package com.halloapp;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.halloapp.ui.RegistrationRequestActivity;
import com.halloapp.ui.RegistrationVerificationActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!HalloApp.instance.isRegistered()) {
            startActivity(new Intent(this, RegistrationRequestActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        final BottomNavigationView navView = findViewById(R.id.nav_view);
        final AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_messages,
                R.id.navigation_profile).build();
        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }
}
