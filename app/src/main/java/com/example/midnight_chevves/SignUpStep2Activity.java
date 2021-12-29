package com.example.midnight_chevves;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignUpStep2Activity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnProceed;
    private TextInputLayout layoutPassword, layoutConfirmPassword;
    private TextInputEditText inputPassword, inputConfirmPassword;

    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_step2);

        btnBack = findViewById(R.id.signup_step2_back);
        btnProceed = findViewById(R.id.button_signup3_redirect);
        layoutPassword = findViewById(R.id.layout_signup_password);
        layoutConfirmPassword = findViewById(R.id.layout_signup_confirm_password);
        inputPassword = findViewById(R.id.signup_password);
        inputConfirmPassword = findViewById(R.id.signup_confirm_password);

        bundle = getIntent().getExtras();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PerformAuth();
            }
        });

    }

    private void PerformAuth() {
        String password = inputPassword.getText().toString();
        String confirmPassword = inputConfirmPassword.getText().toString();

        if (!password.equals(confirmPassword)) {
            layoutConfirmPassword.setErrorEnabled(true);
            layoutConfirmPassword.setError("Passwords do not match!");
            inputConfirmPassword.requestFocus();
        } else {
            clearError(2);
        }

        if (password.isEmpty()) {
            layoutPassword.setErrorEnabled(true);
            layoutPassword.setError("Password field is required!");
            inputPassword.requestFocus();
        } else if (password.length() < 6) {
            layoutPassword.setError("Password should be at least six characters!");
            inputPassword.requestFocus();
        } else {
            clearError(1);
        }

        if (withoutErrors()) {
            bundle.putString("password", password);
            sendUserToNextActivity();
        }


    }

    private void sendUserToNextActivity() {
        Intent intent = new Intent(SignUpStep2Activity.this, SignUpStep3Activity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void clearError(int field) {
        if (field == 1) {
            layoutPassword.setError(null);
            layoutPassword.setErrorEnabled(false);
        } else {
            layoutConfirmPassword.setError(null);
            layoutConfirmPassword.setErrorEnabled(false);
        }
    }

    private boolean withoutErrors() {
        if (!layoutPassword.isErrorEnabled() && !layoutConfirmPassword.isErrorEnabled()) {
            return true;
        } else {
            return false;
        }
    }
}