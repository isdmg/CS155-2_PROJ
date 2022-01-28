package com.example.midnight_chevves.Customer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.midnight_chevves.Customer.Fragments.AboutUsFragment;
import com.example.midnight_chevves.Customer.Fragments.CartFragment;
import com.example.midnight_chevves.Customer.Fragments.HomeFragment;
import com.example.midnight_chevves.Customer.Fragments.AccountFragment;
import com.example.midnight_chevves.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CustomerActivity extends AppCompatActivity {

    private FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);
        BottomNavigationView bottomNavigationView = findViewById(R.id.customer_nav);
        fragmentTransaction = getSupportFragmentManager().beginTransaction();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        fragmentTransaction.replace(R.id.fragment_container, new HomeFragment()).commit();
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            int id = item.getItemId();
            switch (item.getItemId()) {
                case R.id.home:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.profile:
                    selectedFragment = new AccountFragment();
                    break;
                case R.id.about_us:
                    selectedFragment = new AboutUsFragment();
                    break;
                case R.id.cart:
                    selectedFragment = new CartFragment();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            return true;
        }
    };


}