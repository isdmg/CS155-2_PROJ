package com.example.midnight_chevves.Customer.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.midnight_chevves.R;

public class ProductDetailsActivity extends AppCompatActivity {

    private String ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        ID = getIntent().getStringExtra("ID");
    }
}