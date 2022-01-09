package com.example.midnight_chevves.Admin;



import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.midnight_chevves.Admin.Category.CategoryCakes;
import com.example.midnight_chevves.R;

public class AdminActivity extends AppCompatActivity {

    private Button AdminButton_Cake;
    private Button AdminButton_Char_CUTE_rie_boxes;
    private Button AdminButton_Wines;
    private Button button_ManageOrder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);


        AdminButton_Cake = (Button) findViewById(R.id.AdminButton_Cake);
        AdminButton_Cake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCategoryCake();
            }
        });
    }
    public void openCategoryCake(){
        Intent intent = new Intent(this, CategoryCakes.class);
        startActivity(intent);
    }
}