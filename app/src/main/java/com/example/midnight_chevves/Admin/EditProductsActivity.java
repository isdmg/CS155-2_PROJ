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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.midnight_chevves.Customer.Activities.LoginSecurityActivity;
import com.example.midnight_chevves.Customer.Activities.ProductDetailsActivity;
import com.example.midnight_chevves.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
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

// TODO: Prevent changes when in Cart.

public class EditProductsActivity extends AppCompatActivity {

    private String ID;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_products);

        store = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        ID = getIntent().getStringExtra("ID");
        layoutName = findViewById(R.id.layout_edit_product_name);
        layoutPrice = findViewById(R.id.layout_edit_product_price);
        inputName = findViewById(R.id.edit_product_name);
        inputPrice = findViewById(R.id.edit_product_price);
        productImage = findViewById(R.id.manage_image);
        btnSlots = findViewById(R.id.button_edit_slots);
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
                save();
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
//        store.collection("Products").document(ID)
//                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                inputName.setText(documentSnapshot.getString("Name"));
//                inputPrice.setText(documentSnapshot.getString("Price"));
//                btnSlots.setNumber(documentSnapshot.get("Slots").toString());
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(EditProductsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        storageReference.child("Product Images/" + ID + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//                Picasso.get().load(uri).into(productImage);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(EditProductsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });

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
                    Picasso.get().load(snapshot.getString("imageRef")).into(productImage);
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    private void save() {
        String name = inputName.getText().toString();
        String price = inputPrice.getText().toString();
        int slots = Integer.parseInt(btnSlots.getNumber());

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
            DocumentReference documentReference = store.collection("Products").document(ID);
            documentReference.update("Name", name);
            documentReference.update("Price", Integer.parseInt(price));
            documentReference.update("Slots", slots);

            if (imageUri != null) {
                storageReference.child("Product Images/" + ID + ".jpg")
                        .putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.child("Product Images/" + ID + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                documentReference.update("imageRef", uri.toString());
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProductsActivity.this, "Uploading Image Failed!", Toast.LENGTH_SHORT).show();
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