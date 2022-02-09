package com.example.midnight_chevves.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.midnight_chevves.Admin.Category.CategoryBoxes;
import com.example.midnight_chevves.Admin.Category.CategoryCakes;
import com.example.midnight_chevves.Admin.Category.CategoryWines;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddDescriptionActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private TextInputLayout layoutDescription;
    private TextInputEditText inputDescription;
    private Bundle bundle;
    private Button addProduct;
    private String name, alias, price, slots, product, ID;
    private int newSlots;
    private String randomKey = UUID.randomUUID().toString();
    private String downloadImageUrl, description;
    private boolean isEdit;
    private Uri imageUri;

    private FirebaseFirestore store;
    private StorageReference storageReference;

    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat currentDate = new SimpleDateFormat("MM/dd/yyyy");
    private String saveCurrentDate = currentDate.format(calendar.getTime());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_description);

        store = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();


        progressDialog = new ProgressDialog(this);
        addProduct = findViewById(R.id.button_add_description);

        layoutDescription = findViewById(R.id.layout_add_product_description);
        inputDescription = findViewById(R.id.add_product_description);

        bundle = getIntent().getExtras();

        name = bundle.getString("name");
        alias = bundle.getString("alias");
        price = bundle.getString("price");
        slots = bundle.getString("slots");
        product = bundle.getString("product");
        imageUri = bundle.getParcelable("uri");
        isEdit = bundle.getBoolean("isEdit");

        if (isEdit) {
            ID = bundle.getString("ID");
            description = bundle.getString("description");
            newSlots = bundle.getInt("newSlots");
            inputDescription.setText(description);
        }

        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PerformAuth();
            }
        });

    }

    private void PerformAuth() {
        if (inputDescription.getText().toString().isEmpty()) {
            layoutDescription.setErrorEnabled(true);
            layoutDescription.setError("Description field is required!");
            inputDescription.requestFocus();
        } else {
            clearError();
        }

        if (!layoutDescription.isErrorEnabled()) {
            if (isEdit) {
                updateProduct();
            } else {
                addProduct();
            }

        }
    }

    private void updateProduct() {
        progressDialog.setMessage("Saving changes...");
        progressDialog.setTitle("Updating Product");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        DocumentReference documentReference = store.collection("Products").document(ID);
        documentReference.update("Name", name);
        documentReference.update("Price", Integer.parseInt(price));
        documentReference.update("Description", inputDescription.getText().toString());

        if (Integer.parseInt(slots) != newSlots) {
            documentReference.update("RDate", saveCurrentDate);
        }
        documentReference.update("Slots", newSlots);

        if (imageUri != null) {
            storageReference.child("Product Images/" + ID + ".jpg")
                    .putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.child("Product Images/" + ID + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            documentReference.update("imageRef", uri.toString());
                            progressDialog.dismiss();
                            Toast.makeText(AddDescriptionActivity.this, "Selected product was updated!", Toast.LENGTH_SHORT).show();
                            sendUserToNextActivity();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Log.d("updateStatus", "fail");
                    Toast.makeText(AddDescriptionActivity.this, "Uploading Image Failed!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            progressDialog.dismiss();
            Toast.makeText(AddDescriptionActivity.this, "Selected product was updated!", Toast.LENGTH_SHORT).show();
            sendUserToNextActivity();
        }
    }

    private void addProduct() {
        if (product.equals("box")) {
            progressDialog.setMessage("Adding Char-CUTE-rie box...");
            progressDialog.setTitle("Adding Product");
        } else if (product.equals("cake")) {
            progressDialog.setMessage("Adding Cake...");
            progressDialog.setTitle("Adding Product");
        } else {
            progressDialog.setMessage("Adding Wine...");
            progressDialog.setTitle("Adding Product");
        }

        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        final StorageReference filepath = storageReference.child("Product Images/" + randomKey + ".jpg");
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
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AddDescriptionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void writeDocument() {
        String description = inputDescription.getText().toString();

        Map<String, Object> boxInfo = new HashMap<>();
        boxInfo.put("Category", product);
        boxInfo.put("Description", description);
        boxInfo.put("Alias", alias);
        boxInfo.put("ID", randomKey);
        boxInfo.put("Name", name);
        boxInfo.put("Price", Integer.parseInt(price));
        boxInfo.put("RDate", saveCurrentDate);
        boxInfo.put("imageRef", downloadImageUrl);
        boxInfo.put("Slots", Integer.parseInt(slots));

        store.collection("Products").document(randomKey)
                .set(boxInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("test0", "DocumentSnapshot successfully written!");
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("test0", "Error writing document", e);
                        progressDialog.dismiss();
                        Toast.makeText(AddDescriptionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        Toast.makeText(AddDescriptionActivity.this, "Adding Product Successful", Toast.LENGTH_SHORT).show();
        sendUserToNextActivity();
    }

    private void sendUserToNextActivity() {
        Intent intent;

        if (product.equals("box")) {
            intent = new Intent(AddDescriptionActivity.this, CategoryBoxes.class);
        } else if (product.equals("cake")) {
            intent = new Intent(AddDescriptionActivity.this, CategoryCakes.class);
        } else {
            intent = new Intent(AddDescriptionActivity.this, CategoryWines.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private void clearError() {
        layoutDescription.setError(null);
        layoutDescription.setErrorEnabled(false);
    }
}