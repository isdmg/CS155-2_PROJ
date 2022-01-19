package com.example.midnight_chevves.Admin;



import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.midnight_chevves.Admin.Category.CategoryBoxes;
import com.example.midnight_chevves.Admin.Category.CategoryCakes;
import com.example.midnight_chevves.Admin.Category.CategoryExtras;
import com.example.midnight_chevves.Admin.Category.CategoryWines;
import com.example.midnight_chevves.LoginActivity;
import com.example.midnight_chevves.R;
import com.google.firebase.auth.FirebaseAuth;

public class AdminActivity extends AppCompatActivity {
    private ImageView AdminButton_Cake,AdminButton_Char_CUTE_rie_boxes, AdminButton_Wines;

    private Button button_ManageOrder;
    private Button btnLogout;
    private Button AdminButton_Extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);


        AdminButton_Cake = (ImageView) findViewById(R.id.AdminButton_Cake);
        AdminButton_Cake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCategoryCake();
            }
        });

        AdminButton_Char_CUTE_rie_boxes = (ImageView) findViewById(R.id.AdminButton_Char_CUTE_rie_boxes);
        AdminButton_Char_CUTE_rie_boxes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCategoryBoxes();
            }
        });

        AdminButton_Wines = (ImageView) findViewById(R.id.AdminButton_Wines);
        AdminButton_Wines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCategoryWines();
            }
        });

        button_ManageOrder = findViewById(R.id.button_ManageOrder);
        button_ManageOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminActivity.this, ManageOrdersActivity.class);
                startActivity(intent);
            }
        });

        btnLogout = findViewById(R.id.button_admin_logout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Toast.makeText(AdminActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
            }
        });

        AdminButton_Extras = findViewById(R.id.AdminButton_Extras);
        AdminButton_Extras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminActivity.this, CategoryExtras.class);
                startActivity(intent);
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