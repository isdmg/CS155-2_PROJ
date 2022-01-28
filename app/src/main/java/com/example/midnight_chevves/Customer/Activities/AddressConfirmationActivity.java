package com.example.midnight_chevves.Customer.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.midnight_chevves.OrderDetailsActivity;
import com.example.midnight_chevves.R;

public class AddressConfirmationActivity  extends AppCompatActivity {
    private Button btnAddress;

    protected void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation_address);
        btnAddress = findViewById(R.id.button_address);

        btnAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddressConfirmationActivity.this, AddressActivity.class);
                startActivity(intent);
            }
        });

    }
}
