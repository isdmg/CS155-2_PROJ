package com.example.midnight_chevves.Customer.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.midnight_chevves.Customer.Activities.LoginSecurityActivity;
import com.example.midnight_chevves.LoginActivity;
import com.example.midnight_chevves.R;
import com.example.midnight_chevves.SignUpActivity;
import com.google.firebase.auth.FirebaseAuth;

public class AccountFragment extends Fragment implements View.OnClickListener {

    private Button btnLoginSecurity, btnAddressInformation, btnLogOut;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_customer_account, container, false);

        // TODO: Add image.
        btnLoginSecurity = v.findViewById(R.id.button_login_security);
        btnAddressInformation = v.findViewById(R.id.button_address_info);
        btnLogOut = v.findViewById(R.id.button_customer_logout);

        btnLoginSecurity.setOnClickListener(this);
        btnAddressInformation.setOnClickListener(this);
        btnLogOut.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        int id = view.getId();
        switch (id) {
            case R.id.button_login_security:
                intent = new Intent(getActivity(), LoginSecurityActivity.class);
                startActivity(intent);
                break;
            case R.id.button_address_info:
                //
                break;
            case R.id.button_customer_logout:
                FirebaseAuth.getInstance().signOut();
                intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Toast.makeText(getActivity(), "Logged Out", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}