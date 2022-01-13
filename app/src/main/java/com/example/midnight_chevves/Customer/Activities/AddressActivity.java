package com.example.midnight_chevves.Customer.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.midnight_chevves.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddressActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private MaterialButton btnSave;
    private TextInputEditText add1, add2, add3, add4, add5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);
        btnBack = findViewById(R.id.edit_back);
        btnSave = findViewById(R.id.button_save_edit2);
        add1 = findViewById(R.id.house_number);
        add2 = findViewById(R.id.street);
        add3 = findViewById(R.id.area);
        add4 = findViewById(R.id.province);
        add5 = findViewById(R.id.city);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
    }
    private void save() {
        String address1 = add1.getText().toString();
        String address2 = add2.getText().toString();
        String address3 = add3.getText().toString();
        String address4 = add4.getText().toString();
        String address5 = add5.getText().toString();


        if (address1.isEmpty() || address2.isEmpty() ||address3.isEmpty() ||address4.isEmpty() ||address5.isEmpty()) {
            Toast.makeText(this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
        } else {
            //address updated
            Toast.makeText(this, "Address Updated", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    }


}
