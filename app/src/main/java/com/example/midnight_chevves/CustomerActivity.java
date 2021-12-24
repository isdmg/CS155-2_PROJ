package com.example.midnight_chevves;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CustomerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        BottomNavigationView bottomNavigationView = findViewById(R.id.customer_nav);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();
//
//        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment());
//        getSupportFragmentManager().beginTransaction().addToBackStack(null);//add the transaction to the back stack so the user can navigate back
//        getSupportFragmentManager().beginTransaction().commit();
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            int id = item.getItemId();
            switch(item.getItemId()) {
                case R.id.home:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.profile:
                        selectedFragment = new ProfileFragment();
                    break;
                case R.id.about_us:
                        selectedFragment = new AboutUsFragment();
                    break;
                case R.id.cart:
                        selectedFragment = new CartFragment();
                    break;
            }
//            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment);
//            getSupportFragmentManager().beginTransaction().addToBackStack(null);//add the transaction to the back stack so the user can navigate back
//            getSupportFragmentManager().beginTransaction().commit();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFragment).commit();
            return true;
        }
    };


}