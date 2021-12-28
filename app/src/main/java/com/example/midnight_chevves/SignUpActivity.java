package com.example.midnight_chevves;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private TextInputLayout layoutEmail, layoutPassword, layoutConfirmPassword;
    private TextInputEditText inputEmail, inputPassword, inputConfirmPassword;
    private Button btnSignUp;
    private ImageButton btnBack;
    private ProgressDialog progressDialog;

    private FirebaseAuth auth;
    private FirebaseFirestore store;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        inputEmail = findViewById(R.id.signup_email);
        inputPassword = findViewById(R.id.signup_password);
        inputConfirmPassword = findViewById(R.id.signup_confirm_password);
        btnSignUp = findViewById(R.id.button_signup);
        btnBack = findViewById(R.id.signup_back);
        progressDialog = new ProgressDialog(this);
        auth = FirebaseAuth.getInstance();
        store = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();
        layoutEmail = findViewById(R.id.layout_signup_email);
        layoutPassword = findViewById(R.id.layout_signup_password);
        layoutConfirmPassword = findViewById(R.id.layout_signup_confirm_password);

        // TODO: Implement View Listener?
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PerformAuth();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void PerformAuth() {
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();
        String confirmPassword = inputConfirmPassword.getText().toString();

        if (!password.equals(confirmPassword)) {
            layoutConfirmPassword.setErrorEnabled(true);
            layoutConfirmPassword.setError("Passwords do not match!");
            inputConfirmPassword.requestFocus();
        } else {
            clearError(3);
        }

        if (password.isEmpty()) {
            layoutPassword.setErrorEnabled(true);
            layoutPassword.setError("Password field is required!");
            inputPassword.requestFocus();
        } else if (password.length() < 6) {
            layoutPassword.setError("Password should be at least six characters!");
        } else {
            clearError(2);
        }

        if (!isEmailValid(email)) {
            layoutEmail.setErrorEnabled(true);
            layoutEmail.setError("Invalid email address!");
            inputEmail.requestFocus();
        } else {
            clearError(1);
        }

        if (withoutErrors()) {
            progressDialog.setMessage("Signing up...");
            progressDialog.setTitle("Registration");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        DocumentReference documentReference = store.collection("Users").document(auth.getUid());
                        Map<String,Object> userInfo = new HashMap<>();
                        // TODO: Change to variables.
                        userInfo.put("Name", "Juan Pedro");
                        userInfo.put("Username", "jpd123");
                        userInfo.put("Email", "tes2t@gmail.com");
                        userInfo.put("Phone", "09190791784");
//                        userInfo.put("Photo", );
                        userInfo.put("isAdmin", "0");
                        documentReference.set(userInfo);
                        sendUserToNextActivity();
                        Toast.makeText(SignUpActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }

    private void sendUserToNextActivity() {
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void clearError(int field) {
        if (field == 1) {
            layoutEmail.setError(null);
            layoutEmail.setErrorEnabled(false);
        } else if (field == 2) {
            layoutPassword.setError(null);
            layoutPassword.setErrorEnabled(false);
        } else {
            layoutConfirmPassword.setError(null);
            layoutConfirmPassword.setErrorEnabled(false);
        }
    }

    private boolean withoutErrors() {
        if (!layoutEmail.isErrorEnabled() && !layoutPassword.isErrorEnabled() && !layoutConfirmPassword.isErrorEnabled()) {
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
