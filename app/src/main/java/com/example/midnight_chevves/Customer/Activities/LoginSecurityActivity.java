package com.example.midnight_chevves.Customer.Activities;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.midnight_chevves.Customer.Fragments.AccountFragment;
import com.example.midnight_chevves.LoginActivity;
import com.example.midnight_chevves.R;
import com.example.midnight_chevves.SignUpStep3Activity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import de.hdodenhof.circleimageview.CircleImageView;

public class LoginSecurityActivity extends AppCompatActivity {

    private TextInputLayout layoutName, layoutUsername, layoutPhoneNumber;
    private TextInputEditText inputName, inputUsername, inputPhoneNumber;
    private ImageButton btnBack;
    private Button btnSave;
    private CircleImageView accountImage;
    private Uri imageUri;

    private FirebaseAuth auth;
    private FirebaseFirestore store;
    private StorageReference storageReference;

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
        accountImage = findViewById(R.id.edit_image);
        auth = FirebaseAuth.getInstance();
        store = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
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

        accountImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity().setAspectRatio(1, 1).start(LoginSecurityActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            Picasso.get().load(imageUri).into(accountImage);
        }
    }

    private void getUserInfo() {
//        store.collection("Users").document(auth.getUid())
//                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                inputName.setText(documentSnapshot.getString("Name"));
//                inputUsername.setText(documentSnapshot.getString("Username"));
//                inputPhoneNumber.setText(documentSnapshot.getString("Phone").substring(3));
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(LoginSecurityActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        storageReference.child("Users/" + auth.getUid() + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//                Picasso.get().load(uri).into(accountImage);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(LoginSecurityActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    inputName.setText(snapshot.getString("Name"));
                    inputUsername.setText(snapshot.getString("Username"));
                    inputPhoneNumber.setText(snapshot.getString("Phone").substring(3));
                    Picasso.get().load(snapshot.getString("imageRef")).into(accountImage);
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    private void save() {
        String name = inputName.getText().toString();
        String username = inputUsername.getText().toString();
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
            clearError(3);
        }

        if (username.isEmpty()) {
            layoutUsername.setErrorEnabled(true);
            layoutUsername.setError("Username field is required!");
            inputUsername.requestFocus();
        } else if (username.length() > 20) {
            layoutUsername.setError("Maximum length exceeded!");
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
            DocumentReference documentReference = store.collection("Users").document(auth.getUid());
            documentReference.update("Name", name);
            documentReference.update("Username", username);
            documentReference.update("Phone", "+63" + phoneNumber);

            if (imageUri != null) {
                storageReference.child("Users/" + auth.getUid() + ".jpg")
                        .putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.child("Users/" + auth.getUid() + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                documentReference.update("imageRef", uri.toString());
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginSecurityActivity.this, "Uploading Image Failed!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        onBackPressed();
    }

    private void clearError(int field) {
        if (field == 1) {
            layoutName.setError(null);
            layoutName.setErrorEnabled(false);
        } else if (field == 2) {
            layoutUsername.setError(null);
            layoutUsername.setErrorEnabled(false);
        } else {
            layoutPhoneNumber.setError(null);
            layoutPhoneNumber.setErrorEnabled(false);
        }
    }

    private boolean withoutErrors() {
        if (!layoutName.isErrorEnabled() && !layoutUsername.isErrorEnabled() && !layoutPhoneNumber.isErrorEnabled()) {
            return true;
        } else {
            return false;
        }
    }
}