package com.example.midnight_chevves.Customer.Activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.midnight_chevves.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

public class UserAddressActivity extends AppCompatActivity {

    private TextView userAddress;
    private Button btnAddress;
    private FirebaseAuth auth;
    private FirebaseFirestore store;

    protected void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_address);


        auth = FirebaseAuth.getInstance();
        store = FirebaseFirestore.getInstance();
        btnAddress = findViewById(R.id.button_address);
        userAddress = findViewById(R.id.text_user_address);


        btnAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserAddressActivity.this, AddressActivity.class);
                startActivity(intent);
            }
        });

        final DocumentReference docRef = store.collection("Addresses").document(auth.getUid());
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
                    String mapAddress = snapshot.getString("MapAddress");
                    String addressDetails = snapshot.getString("AddressDetails");
                    String addressType = snapshot.getString("AddressType");
                    userAddress.setText(mapAddress + "\n\n" + addressDetails +"\n\n" + addressType);
                } else {
                    userAddress.setText("No Address...");
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }
}
