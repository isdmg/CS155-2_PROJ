package com.example.midnight_chevves.Customer.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.midnight_chevves.R;

public class AfterOrderSplash extends AppCompatActivity {

    private Button returnHome;

    @Override
    public void onBackPressed() {
        // Do nothing...
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_order_spash);

        returnHome = (Button) findViewById(R.id.return_home_btn);
        returnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AfterOrderSplash.this, CustomerActivity.class);

                startActivity(intent);
            }
        });

    }

}


