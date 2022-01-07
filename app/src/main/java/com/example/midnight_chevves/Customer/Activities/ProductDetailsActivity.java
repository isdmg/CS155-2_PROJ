package com.example.midnight_chevves.Customer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.midnight_chevves.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ProductDetailsActivity extends AppCompatActivity {

    private String ID;
    private TextView productName, productPrice, productSlots;
    private Button btnAddToCart;
    private ImageButton btnBack;
    private ImageView productImage;
    private ElegantNumberButton btnQuantity;

    private FirebaseAuth auth;
    private FirebaseFirestore store;
    private CollectionReference collectionReference;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        auth = FirebaseAuth.getInstance();
        store = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        collectionReference = store.collection("Cart").document(auth.getUid()).collection("List");
        ID = getIntent().getStringExtra("ID");
        productName = findViewById(R.id.details_name);
        productPrice = findViewById(R.id.details_price);
        productSlots = findViewById(R.id.details_slots);
        productImage = findViewById(R.id.details_image);
        btnQuantity = findViewById(R.id.button_quantity);
        getProductDetails();

        btnBack = findViewById(R.id.details_back);
        btnAddToCart = findViewById(R.id.button_add_to_cart);

        // TODO: Add View.OnClickListener?
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        btnAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToCart();
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

        storageReference.child("Product Images/" + ID + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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

    private void addToCart() {
        String date;
        Calendar calendarDate = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        date = dateFormat.format(calendarDate.getTime());

        Map<String, Object> cartInfo = new HashMap<>();
        cartInfo.put("ProductID", ID);
        cartInfo.put("ProductName", productName.getText().toString());
        cartInfo.put("ProductPrice", productPrice.getText().toString());
        cartInfo.put("PurchaseDate", date);
        cartInfo.put("Quantity", btnQuantity.getNumber());

        collectionReference
                .whereEqualTo("ProductID", ID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                String documentID = null;
                boolean documentExists = false;
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        documentID = document.getId();
                        documentExists = document.exists();
                    }
                    Log.d("test0", Boolean.toString(documentExists));
                    if (documentExists) {
                        collectionReference.document(documentID).update("Quantity", btnQuantity.getNumber());
                        onBackPressed();
                    } else {
                        collectionReference
                                .add(cartInfo)
                                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        Toast.makeText(ProductDetailsActivity.this, "Added to Cart", Toast.LENGTH_SHORT).show();
                                        onBackPressed();
                                    }
                                });
                    }
                } else {
                    Log.d(ProductDetailsActivity.class.getSimpleName(), "Error getting documents: ", task.getException());
                }
            }
        });
    }
}