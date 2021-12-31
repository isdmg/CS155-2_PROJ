package com.example.midnight_chevves.Customer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.midnight_chevves.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ProductDetailsActivity extends AppCompatActivity {

    private String ID;
    private TextView productName, productPrice, productSlots;
    private ImageButton btnBack;
    private ImageView productImage;

    private FirebaseFirestore store;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        store = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        ID = getIntent().getStringExtra("ID");
        productName = findViewById(R.id.details_name);
        productPrice = findViewById(R.id.details_price);
        productSlots = findViewById(R.id.details_slots);
        productImage = findViewById(R.id.details_image);
        getProductDetails();

        btnBack = findViewById(R.id.details_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void getProductDetails() {
        store.collection("Products").document(ID)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                productName.setText(documentSnapshot.getString("Name"));
                productPrice.setText(documentSnapshot.getString("Price"));
                productSlots.setText(documentSnapshot.get("Slots").toString());

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProductDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        storageReference.child("Product Images/"+ID+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(productImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProductDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}