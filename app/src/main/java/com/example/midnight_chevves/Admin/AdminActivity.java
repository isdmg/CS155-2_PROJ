package com.example.midnight_chevves.Admin;



import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.midnight_chevves.Admin.Category.CategoryBoxes;
import com.example.midnight_chevves.Admin.Category.CategoryCakes;
import com.example.midnight_chevves.Admin.Category.CategoryWines;
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

        AdminButton_Char_CUTE_rie_boxes = (Button) findViewById(R.id.AdminButton_Char_CUTE_rie_boxes);
        AdminButton_Char_CUTE_rie_boxes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCategoryBoxes();
            }
        });

        AdminButton_Wines = (Button) findViewById(R.id.AdminButton_Wines);
        AdminButton_Wines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCategoryWines();
            }
        });

    }


    public void openCategoryCake(){
        Intent intent = new Intent(this, CategoryCakes.class);
        startActivity(intent);
    }

    public void openCategoryBoxes(){
        Intent intent = new Intent(this, CategoryBoxes.class);
        startActivity(intent);
    }

    public void openCategoryWines(){
        Intent intent = new Intent(this, CategoryWines.class);
        startActivity(intent);
    }

}