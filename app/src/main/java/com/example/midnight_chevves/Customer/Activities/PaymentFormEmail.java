package com.example.midnight_chevves.Customer.Activities;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.midnight_chevves.Interface.JavaMailAPI;
import com.example.midnight_chevves.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class PaymentFormEmail extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private FirebaseAuth auth;
    private FirebaseFirestore store;

    public EditText txtName, txtMobileNo, txtLocation;

    Spinner paymentmethod;
    Button SendEmail_btn;

    long subtotalText;
    TextView subtotal;

    private String orderId = UUID.randomUUID().toString();
    Map<String, Object> detailsInfo = new HashMap<>();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_form_email);

        auth = FirebaseAuth.getInstance();
        store = FirebaseFirestore.getInstance();

        txtName = (EditText) findViewById(R.id.editTextNAME_paymentform);
        txtMobileNo = (EditText) findViewById(R.id.editTextMobileNo_paymentform);
        txtLocation = (EditText) findViewById(R.id.editTextLocation_paymentform);
        subtotal = (TextView) findViewById(R.id.text_subtotal);

        subtotalText = getIntent().getLongExtra("totalAmount", 0);
        subtotal.setText("Total Price: ₱" + String.valueOf(+ subtotalText));

        //locks EditText
        txtName.setFocusable(false);
        txtMobileNo.setFocusable(false);
        txtLocation.setFocusable(false);


        paymentmethod = findViewById(R.id.orderform_paymentmethod);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.payment_method_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentmethod.setAdapter(adapter);
        paymentmethod.setOnItemSelectedListener(this);

        SendEmail_btn = findViewById(R.id.orderFormSubmit_btn);
        SendEmail_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAddress();
                Toast.makeText(PaymentFormEmail.this, "Added to Orders.", Toast.LENGTH_SHORT).show();

            }
        });




        final DocumentReference docRef = store.collection("Users").document(auth.getUid());
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    txtName.setText(snapshot.getString("Name"));
                    txtMobileNo.setText(snapshot.getString("Phone").substring(3));

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

        final DocumentReference docRef2 = store.collection("Addresses").document(auth.getUid());
        docRef2.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    String mapAddress = snapshot.getString("MapAddress");
                    String addressDetails = snapshot.getString("AddressDetails");
                    String addressType = snapshot.getString("AddressType");
                    txtLocation.setText(mapAddress + ", " + addressDetails +", " + addressType);

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }





    private void getAddress() {
        store.collection("Addresses").document(auth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String mapAddress = documentSnapshot.getString("MapAddress");
                    String addressDetails = documentSnapshot.getString("AddressDetails");
                    String addressType = documentSnapshot.getString("AddressType");
                    detailsInfo.put("Address", mapAddress + " / " + addressDetails + " / " + addressType);
                }
                createOrderEntry(orderId, paymentmethod.getSelectedItem().toString());
            }
        });
    }


    private void createOrderEntry(String randomKey, String paymentMethod) {
        orderId = randomKey;

        store.collection("Users").document(auth.getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                detailsInfo.put("Email", auth.getCurrentUser().getEmail());
                detailsInfo.put("Name", documentSnapshot.getString("Name"));
                detailsInfo.put("Phone", "+63" + documentSnapshot.getString("Phone").substring(3));


                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("MM/dd/yyyy");
                String saveCurrentDate = currentDate.format(calendar.getTime());

                Map<String, Object> extraInfo = new HashMap<>();
                extraInfo.put("accountRef", auth.getUid());
                extraInfo.put("OrderId", orderId);
                extraInfo.put("OrderStatus", "Ordered");
                extraInfo.put("OrderDate", saveCurrentDate);
                extraInfo.put("TotalAmount", getIntent().getLongExtra("totalAmount", 0));
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
                Toast.makeText(PaymentFormEmail.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addProducts(String randomKey) {
        orderId = randomKey;
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
                        orderInfo.put("ListID", document.getString("ListID"));
                        orderInfo.put("ProductID", document.getString("ProductID"));
                        orderInfo.put("ProductName", document.getString("ProductName"));
                        orderInfo.put("ProductPrice", document.getLong("ProductPrice"));
                        orderInfo.put("PurchaseDate", document.getString("PurchaseDate"));
                        orderInfo.put("Quantity", document.getLong("Quantity"));

                        DocumentReference df1 = store.collection("Orders").document(orderId);
                        CollectionReference df2 = df1.collection("Products");
                        df2.add(orderInfo);

                        sendMail();
                    }
                } else {
                   Log.d(PaymentFormEmail.class.getSimpleName(), "Error getting documents: ", task.getException());
                }
            }
        });

        CollectionReference collectionReference2 = store.collection("Carts").document(auth.getUid()).collection("Extras");
        collectionReference2.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                                Log.d("newSlots", String.valueOf(newSlots));
                                store.collection("Products").document(document.getString("ProductID")).update("Slots", newSlots);
                            }
                        });
                        collectionReference2.document(document.getId()).delete();

                        Map<String, Object> orderInfo = new HashMap<>();
                        orderInfo.put("ExtraID", document.getString("ExtraID"));
                        orderInfo.put("ProductID", document.getString("ProductID"));
                        orderInfo.put("ProductName", document.getString("ProductName"));
                        orderInfo.put("ProductPrice", document.getLong("ProductPrice"));
                        orderInfo.put("PurchaseDate", document.getString("PurchaseDate"));
                        orderInfo.put("Quantity", document.getLong("Quantity"));
                        orderInfo.put("parentRef", document.getString("parentRef"));

                        DocumentReference df1 = store.collection("Orders").document(orderId);
                        CollectionReference df2 = df1.collection("Extras");
                        df2.add(orderInfo);


                    }
                } else {
                    Log.d(PaymentFormEmail.class.getSimpleName(), "Error getting documents: ", task.getException());
                }
            }
        });
    }



    private void sendMail() {

        String mail = "cs155.midnight.payment@gmail.com";
        String subject = txtName.getText().toString().trim() + " Payment Form";
        String message = "Customer Name: " + detailsInfo.get("Name") + "\n" +
                "Phone Number: " + detailsInfo.get("Phone") + "\n" +
                "Shipping Address: " + detailsInfo.get("Address") + "\n" +
                "Order Number: " + orderId + "\n" +
                "Payment Method: " + paymentmethod.getSelectedItem().toString();

        //Send Mail
        JavaMailAPI javaMailAPI = new JavaMailAPI(this, mail, subject, message);
        javaMailAPI.execute();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
        String PaymentMethodString = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), PaymentMethodString, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }




}