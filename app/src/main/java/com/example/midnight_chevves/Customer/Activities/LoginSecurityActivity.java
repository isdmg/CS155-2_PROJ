package com.example.midnight_chevves.Customer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.midnight_chevves.LoginActivity;
import com.example.midnight_chevves.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginSecurityActivity extends AppCompatActivity {

    private TextInputLayout layoutName, layoutUsername, layoutPhoneNumber;
    private TextInputEditText inputName, inputUsername, inputPhoneNumber;
    private ImageButton btnBack;
    private Button btnSave;

    private FirebaseAuth auth;
    private FirebaseFirestore store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_security);

        layoutName = findViewById(R.id.layout_edit_name);
        layoutUsername = findViewById(R.id.layout_edit_username);
        layoutPhoneNumber = findViewById(R.id.layout_edit_number);
        inputName = findViewById(R.id.edit_name);
        inputUsername = findViewById(R.id.edit_username);
        inputPhoneNumber = findViewById(R.id.edit_number);
        btnBack = findViewById(R.id.edit_back);
        btnSave = findViewById(R.id.button_save_edit);
        auth = FirebaseAuth.getInstance();
        store = FirebaseFirestore.getInstance();
        getUserInfo();

        // TODO: Implement View Listener?
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

    private void getUserInfo() {
        DocumentReference documentReference = store.collection("Users").document(auth.getUid());
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                inputName.setText(documentSnapshot.getString("Name"));
                inputUsername.setText(documentSnapshot.getString("Username"));
                inputPhoneNumber.setText(documentSnapshot.getString("Phone"));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginSecurityActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void save() {
        DocumentReference documentReference = store.collection("Users").document(auth.getUid());
        documentReference.update("Name", inputName.getText().toString());
        documentReference.update("Username", inputUsername.getText().toString());
        documentReference.update("Phone", inputPhoneNumber.getText().toString());
        onBackPressed();
    }
}