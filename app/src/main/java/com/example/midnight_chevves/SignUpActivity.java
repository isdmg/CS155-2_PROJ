package com.example.midnight_chevves;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseFirestore store;
    private TextInputLayout layoutName, layoutUsername, layoutEmail, layoutPhoneNumber;
    private TextInputEditText inputName, inputUsername, inputEmail, inputPhoneNumber;
    private Button btnProceed;
    private ImageButton btnBack;
    private boolean usernameDuplicate;

    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        store = FirebaseFirestore.getInstance();
        usernameDuplicate = true;
        inputName = findViewById(R.id.signup_name);
        inputUsername = findViewById(R.id.signup_username);
        inputEmail = findViewById(R.id.signup_email);
        inputPhoneNumber = findViewById(R.id.signup_number);
        btnBack = findViewById(R.id.signup_back);
        btnProceed = findViewById(R.id.button_signup2_redirect);
        bundle = new Bundle();
        layoutName = findViewById(R.id.layout_signup_name);
        layoutUsername = findViewById(R.id.layout_signup_username);
        layoutEmail = findViewById(R.id.layout_signup_email);
        layoutPhoneNumber = findViewById(R.id.layout_signup_number);

        // TODO: Implement View Listener?
        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUsernameDuplicate();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void checkUsernameDuplicate() {
        String username = inputUsername.getText().toString();
        Query query = store.collection("Users").whereEqualTo("Username", username);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                boolean match = false;
                for (DocumentSnapshot snapshot : value) {
                    if (snapshot != null) {
                        match = true;
                    }
                }
                if (!match) {
                    usernameDuplicate = false;
                }
                PerformAuth();
            }
        });
    }

    private void PerformAuth() {
        String name = inputName.getText().toString();
        String username = inputUsername.getText().toString();
        String email = inputEmail.getText().toString();
        String phoneNumber = inputPhoneNumber.getText().toString();

        if (phoneNumber.isEmpty()) {
            layoutPhoneNumber.setErrorEnabled(true);
            layoutPhoneNumber.setError("Phone number field is required!");
            inputPhoneNumber.requestFocus();
        } else if (phoneNumber.length() != 10) {
            layoutPhoneNumber.setError("Invalid phone number format!");
            inputPhoneNumber.requestFocus();
        } else if (phoneNumber.charAt(0) != '9') {
            layoutPhoneNumber.setError("Mobile area code is not accepted!");
            inputPhoneNumber.requestFocus();
        } else {
            clearError(4);
        }

        if (!isEmailValid(email)) {
            layoutEmail.setErrorEnabled(true);
            layoutEmail.setError("Invalid email address!");
            inputEmail.requestFocus();
        } else {
            clearError(3);
        }

        if (username.isEmpty()) {
            layoutUsername.setErrorEnabled(true);
            layoutUsername.setError("Username field is required!");
            inputUsername.requestFocus();
        } else if (username.length() > 20) {
            layoutUsername.setError("Maximum length exceeded!");
            inputUsername.requestFocus();
        } else if (usernameDuplicate) {
            layoutUsername.setError("Username is taken!");
            inputUsername.requestFocus();
        } else {
            clearError(2);
        }

        if (name.isEmpty()) {
            layoutName.setErrorEnabled(true);
            layoutName.setError("Name field is required!");
            inputName.requestFocus();
        } else if (!name.contains(" ")) {
            layoutName.setErrorEnabled(true);
            layoutName.setError("Last name is required!");
            inputName.requestFocus();
        } else {
            clearError(1);
        }

        if (withoutErrors()) {
            bundle.putString("name", name);
            bundle.putString("username", username);
            bundle.putString("email", email);
            bundle.putString("phoneNumber", phoneNumber);
            sendUserToNextActivity();
        }
    }

    private void sendUserToNextActivity() {
        Intent intent = new Intent(SignUpActivity.this, SignUpStep2Activity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void clearError(int field) {
        if (field == 1) {
            layoutName.setError(null);
            layoutName.setErrorEnabled(false);
        } else if (field == 2) {
            layoutUsername.setError(null);
            layoutUsername.setErrorEnabled(false);
        } else if (field == 3) {
            layoutEmail.setError(null);
            layoutEmail.setErrorEnabled(false);
        } else {
            layoutPhoneNumber.setError(null);
            layoutPhoneNumber.setErrorEnabled(false);
        }
    }

    private boolean withoutErrors() {
        if (!layoutName.isErrorEnabled() && !layoutUsername.isErrorEnabled() && !layoutEmail.isErrorEnabled() && !layoutPhoneNumber.isErrorEnabled()) {
            return true;
        } else {
            return false;
        }
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                .matches();
    }
}
