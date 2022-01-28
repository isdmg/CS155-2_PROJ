package com.example.midnight_chevves.Customer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.midnight_chevves.Admin.Category.CategoryCakes;
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
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CheckoutActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private Button btnCOD, btnForm;
    private FirebaseFirestore store;
    private String randomKey = UUID.randomUUID().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        auth = FirebaseAuth.getInstance();
        store = FirebaseFirestore.getInstance();

        btnCOD = findViewById(R.id.checkout_cod);
        btnForm = findViewById(R.id.checkout_form);


        btnCOD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkoutCOD();
            }
        });

        btnForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEmailForm();
            }
        });
    }

    public void openEmailForm(){
        Intent intent = new Intent(this, PaymentFormEmail.class);
        startActivity(intent);
    }

    private void checkoutCOD() {
        createOrderEntry(randomKey, "Cash on Delivery");
        Toast.makeText(CheckoutActivity.this, "Added to Orders.", Toast.LENGTH_SHORT).show();
    }

    private void createOrderEntry(String randomKey, String paymentMethod) {
        String orderId = randomKey;
        Map<String, Object> detailsInfo = new HashMap<>();

        store.collection("Users").document(auth.getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                detailsInfo.put("Email", auth.getCurrentUser().getEmail());
                detailsInfo.put("Name", documentSnapshot.getString("Name"));
                detailsInfo.put("Phone", documentSnapshot.getString("Phone").substring(3));

                store.collection("Addresses").document(auth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String mapAddress = documentSnapshot.getString("MapAddress");
                            String addressDetails = documentSnapshot.getString("AddressDetails");
                            detailsInfo.put("Address", mapAddress + "\n" + addressDetails);
                        }
                    }
                });


                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("MM/dd/yyyy");
                String saveCurrentDate = currentDate.format(calendar.getTime());

                Map<String, Object> extraInfo = new HashMap<>();
                extraInfo.put("accountRef", auth.getUid());
                extraInfo.put("OrderId", orderId);
                extraInfo.put("OrderStatus", "Ordered");
                extraInfo.put("OrderDate", saveCurrentDate);
                extraInfo.put("TotalAmount", getIntent().getIntExtra("totalAmount", 0));
                extraInfo.put("PaymentMethod", paymentMethod);

                DocumentReference df1 = store.collection("Orders").document(orderId);
                df1.set(extraInfo);

                df1.collection("Details").document(auth.getUid())
                        .set(detailsInfo);

                addProducts(orderId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CheckoutActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addProducts(String randomKey) {
        String orderId = randomKey;
        CollectionReference collectionReference = store.collection("Carts").document(auth.getUid()).collection("List");
        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        store.collection("Products").document(document.getString("ProductID"))
                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                int slots = documentSnapshot.getLong("Slots").intValue();
                                int newSlots = slots - document.getLong("Quantity").intValue();
                                store.collection("Products").document(document.getString("ProductID")).update("Slots", newSlots);
                            }
                        });
                        collectionReference.document(document.getId()).delete();

                        Map<String, Object> orderInfo = new HashMap<>();
                        orderInfo.put("ProductID", document.getString("ProductID"));
                        orderInfo.put("ProductName", document.getString("ProductName"));
                        orderInfo.put("ProductPrice", document.getLong("ProductPrice"));
                        orderInfo.put("PurchaseDate", document.getString("PurchaseDate"));
                        orderInfo.put("Quantity", document.getLong("Quantity"));

                        DocumentReference df1 = store.collection("Orders").document(orderId);
                        CollectionReference df2 = df1.collection("Products");
                        df2.add(orderInfo);

                    }
                } else {
                    Log.d(CheckoutActivity.class.getSimpleName(), "Error getting documents: ", task.getException());
                }
            }
        });
    }


}