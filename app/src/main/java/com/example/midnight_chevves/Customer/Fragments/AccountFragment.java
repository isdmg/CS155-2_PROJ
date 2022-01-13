package com.example.midnight_chevves.Customer.Fragments;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.midnight_chevves.Customer.Activities.AddressActivity;
import com.example.midnight_chevves.Customer.Activities.CustomerOrdersActivity;
import com.example.midnight_chevves.Customer.Activities.LoginSecurityActivity;
import com.example.midnight_chevves.LoginActivity;
import com.example.midnight_chevves.R;
import com.example.midnight_chevves.SignUpActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import android.os.Handler;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountFragment extends Fragment implements View.OnClickListener {

    private Button btnLoginSecurity, btnOrders, btnAddressInformation, btnLogOut;
    private CircleImageView accountImage;
    private TextView username;

    private FirebaseAuth auth;
    private FirebaseFirestore store;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_customer_account, container, false);

        // TODO: Add image.
        btnLoginSecurity = v.findViewById(R.id.button_login_security);
        btnAddressInformation = v.findViewById(R.id.button_address_info);
        btnLogOut = v.findViewById(R.id.button_customer_logout);
        btnOrders = v.findViewById(R.id.button_orders);
        accountImage = v.findViewById(R.id.account_image);
        username = v.findViewById(R.id.text_username);
        auth = FirebaseAuth.getInstance();
        store = FirebaseFirestore.getInstance();
        btnLoginSecurity.setOnClickListener(this);
        btnOrders.setOnClickListener(this);
        btnAddressInformation.setOnClickListener(this);
        btnLogOut.setOnClickListener(this);

        getAccountDetails();

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
            case R.id.button_orders:
                Log.d("test0", "wtf");
                intent = new Intent(getActivity(), CustomerOrdersActivity.class);
                startActivity(intent);
                break;
            case R.id.button_address_info:
                intent = new Intent(getActivity(), AddressActivity.class);
                startActivity(intent);
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

    private void getAccountDetails() {
//        store.collection("Users").document(auth.getUid())
//                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                username.setText(documentSnapshot.getString("Username"));
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });

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
                    username.setText(snapshot.getString("Username"));
                    Picasso.get().load(snapshot.getString("imageRef")).into(accountImage);
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                getAccountDetails();
//            }
//        }, 2000);
//    }
}
