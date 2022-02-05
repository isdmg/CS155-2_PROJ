package com.example.midnight_chevves.Admin;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.midnight_chevves.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

// TODO: Prevent changes when in Cart.

public class EditProductsActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private String ID, description, category;

    private TextInputLayout layoutName, layoutPrice;
    private TextInputEditText inputName, inputPrice;

    private ImageButton btnBack;
    private Button btnSave;
    private ImageView productImage;
    private Uri imageUri;

    private ElegantNumberButton btnSlots;

    private FirebaseFirestore store;
    private StorageReference storageReference;

    private ActivityResultLauncher<Intent> someActivityResultLauncher;

    private long slots;
    private Bundle bundle;

    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat currentDate = new SimpleDateFormat("MM/dd/yyyy");
    private String saveCurrentDate = currentDate.format(calendar.getTime());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_products);

        progressDialog = new ProgressDialog(this);

        store = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        ID = getIntent().getStringExtra("ID");
        layoutName = findViewById(R.id.layout_edit_product_name);
        layoutPrice = findViewById(R.id.layout_edit_product_price);
        inputName = findViewById(R.id.edit_product_name);
        inputPrice = findViewById(R.id.edit_product_price);
        productImage = findViewById(R.id.manage_image);
        btnSlots = findViewById(R.id.button_edit_slots);
        bundle = new Bundle();
        getProductDetails();

        btnBack = findViewById(R.id.edit_products_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnSave = findViewById(R.id.button_save_changes);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PerformAuth();
            }
        });

        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            imageUri = result.getData().getData();
                            Picasso.get().load(imageUri).into(productImage);
                        }
                    }
                });

        productImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGalleryForResult();
            }
        });
    }

    private void openGalleryForResult() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        someActivityResultLauncher.launch(intent);
    }

    private void getProductDetails() {

        final DocumentReference docRef = store.collection("Products").document(ID);
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
                    inputPrice.setText(Long.toString((Long) snapshot.get("Price")));
                    btnSlots.setNumber(snapshot.get("Slots").toString());
                    description = snapshot.getString("Description");
                    category = snapshot.getString("Category");
                    bundle.putString("slots", String.valueOf(snapshot.get("Slots")));
                    slots = snapshot.getLong("Slots");
                    bundle.putString("description", description);
                    bundle.putString("product", category);
                    Picasso.get().load(snapshot.getString("imageRef")).into(productImage);
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    private void PerformAuth() {
        String name = inputName.getText().toString();
        String price = inputPrice.getText().toString();
        int newSlots = Integer.parseInt(btnSlots.getNumber());

        if (price.isEmpty()) {
            layoutPrice.setErrorEnabled(true);
            layoutPrice.setError("Price field is required!");
            inputPrice.requestFocus();
        } else {
            clearError(2);
        }

        if (name.isEmpty()) {
            layoutName.setErrorEnabled(true);
            layoutName.setError("Name field is required!");
            inputName.requestFocus();
        } else {
            clearError(1);
        }

        if (withoutErrors()) {
//            DocumentReference documentReference = store.collection("Products").document(ID);
//            documentReference.update("Name", name);
//            documentReference.update("Price", Integer.parseInt(price));
//            documentReference.update("Slots", newSlots);

            bundle.putString("ID", ID);
            bundle.putString("name", name);
            bundle.putString("price", price);
            bundle.putString("description", description);
            bundle.putInt("newSlots", newSlots);
            bundle.putParcelable("uri", imageUri);
            bundle.putBoolean("isEdit", true);

            if (description == null) {
                progressDialog.setMessage("Saving changes...");
                progressDialog.setTitle("Updating Product");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                DocumentReference documentReference = store.collection("Products").document(ID);
                documentReference.update("Name", name);
                documentReference.update("Price", Integer.parseInt(price));

                if (slots != newSlots) {
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
                                    Toast.makeText(EditProductsActivity.this, "Selected product was updated!", Toast.LENGTH_SHORT).show();
                                    sendUserToNextActivity();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Log.d("updateStatus", "fail");
                            Toast.makeText(EditProductsActivity.this, "Uploading Image Failed!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(EditProductsActivity.this, "Selected product was updated!", Toast.LENGTH_SHORT).show();
                    sendUserToNextActivity();
                }
            } else {
                sendUserToNextActivity();
            }

//            if (imageUri != null) {
//                storageReference.child("Product Images/" + ID + ".jpg")
//                        .putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        storageReference.child("Product Images/" + ID + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                            @Override
//                            public void onSuccess(Uri uri) {
//                                documentReference.update("imageRef", uri.toString());
//                            }
//                        });
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(EditProductsActivity.this, "Uploading Image Failed!", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
        }
    }

    private void sendUserToNextActivity() {
        if (description == null) {
            onBackPressed();
        } else {
            Intent intent = new Intent(EditProductsActivity.this, AddDescriptionActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    private void clearError(int field) {
        if (field == 1) {
            layoutName.setError(null);
            layoutName.setErrorEnabled(false);
        } else {
            layoutPrice.setError(null);
            layoutPrice.setErrorEnabled(false);
        }
    }

    private boolean withoutErrors() {
        if (!layoutName.isErrorEnabled() && !layoutPrice.isErrorEnabled()) {
            return true;
        } else {
            return false;
        }
    }
}