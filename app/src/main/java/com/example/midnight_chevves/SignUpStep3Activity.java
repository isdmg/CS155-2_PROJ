package com.example.midnight_chevves;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpStep3Activity extends AppCompatActivity {

    private CircleImageView accountImage;
    private Button btnSignUp;
    private ImageButton btnBack;

    private FirebaseAuth auth;
    private FirebaseFirestore store;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;

    private Uri imageUri;
    private String downloadImageUrl;

    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_step3);

        auth = FirebaseAuth.getInstance();
        store = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        accountImage = findViewById(R.id.signup_account_image);
        btnSignUp = findViewById(R.id.button_signup);
        progressDialog = new ProgressDialog(this);
        btnBack = findViewById(R.id.signup_step3_back);

        bundle = getIntent().getExtras();


        // TODO: Implement OnClickListener?
        accountImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SignUpStep3Activity.this);
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUri != null) {
                    signUp();
                } else {
                    Toast.makeText(SignUpStep3Activity.this, "Please select an account image!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
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

    private void signUp() {
        progressDialog.setMessage("Signing up...");
        progressDialog.setTitle("Registration");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        auth.createUserWithEmailAndPassword(bundle.getString("email"), bundle.getString("password")).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                progressDialog.dismiss();

//                storageReference.child("Users/" + auth.getUid() + ".jpg")
//                        .putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        Log.d("imageRef-test", downloadImageUrl);
//                        downloadImageUrl = storageReference.child("Users/" + auth.getUid() + ".jpg").getDownloadUrl().toString();
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(SignUpStep3Activity.this, "Upload Image Failed!", Toast.LENGTH_SHORT).show();
//                    }
//                });

                final StorageReference filepath = storageReference.child("Users/" + auth.getUid() + ".jpg");
                final UploadTask uploadTask = filepath.putFile(imageUri);
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        downloadImageUrl = filepath.getDownloadUrl().toString();
                        return filepath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            downloadImageUrl = task.getResult().toString();
                            writeDocument();
                        }
                    }
                });

                sendUserToNextActivity();
                Toast.makeText(SignUpStep3Activity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SignUpStep3Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void writeDocument() {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("Name", bundle.getString("name"));
        userInfo.put("Username", bundle.getString("username"));
        userInfo.put("Phone", "+63" + bundle.getString("phoneNumber"));
        userInfo.put("isAdmin", false);
        userInfo.put("imageRef", downloadImageUrl);

        store.collection("Users").document(auth.getUid())
                .set(userInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("test0", "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("test0", "Error writing document", e);
                    }
                });
    }

    private void sendUserToNextActivity() {
        Intent intent = new Intent(SignUpStep3Activity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}