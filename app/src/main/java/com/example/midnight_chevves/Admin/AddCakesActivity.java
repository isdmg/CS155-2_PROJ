package com.example.midnight_chevves.Admin;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.example.midnight_chevves.R;
import com.example.midnight_chevves.SignUpStep3Activity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class AddCakesActivity extends AppCompatActivity {

    private TextInputLayout layoutName, layoutPrice, layoutSlots;
    private TextInputEditText inputName, inputPrice, inputSlots;
    private Button btnAddCake;

    private FirebaseFirestore store;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;

    private ImageView cakeImage;
    private Uri imageUri;
    private String downloadImageUrl;

    private String randomKey = UUID.randomUUID().toString();
    private ActivityResultLauncher<Intent> someActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cakes);

        store = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        layoutName = findViewById(R.id.layout_add_cake_name);
        layoutPrice = findViewById(R.id.layout_add_cake_price);
        layoutSlots = findViewById(R.id.layout_add_cake_slots);
        inputName = findViewById(R.id.add_cake_name);
        inputPrice = findViewById(R.id.add_cake_price);
        inputSlots = findViewById(R.id.add_cake_slots);
        cakeImage = findViewById(R.id.add_cake_image);

        progressDialog = new ProgressDialog(this);


        btnAddCake = findViewById(R.id.button_add_cake);

        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            imageUri = result.getData().getData();
                            Picasso.get().load(imageUri).into(cakeImage);
                        }
                    }
                });

        cakeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGalleryForResult();
            }
        });

        btnAddCake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUri != null) {
                    addCake();
                } else {
                    Toast.makeText(AddCakesActivity.this, "Please select a product image!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openGalleryForResult() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        someActivityResultLauncher.launch(intent);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            imageUri = result.getUri();
//            Picasso.get().load(imageUri).into(cakeImage);
//        }
//    }

    private void addCake() {
        progressDialog.setMessage("Adding Cake...");
        progressDialog.setTitle("Adding Product");
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
                Toast.makeText(AddCakesActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void writeDocument() {
        String name = inputName.getText().toString();
        String price = inputPrice.getText().toString();
        String slots = inputSlots.getText().toString();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MM/dd/yyyy");
        String saveCurrentDate = currentDate.format(calendar.getTime());

        Map<String, Object> cakeInfo = new HashMap<>();
        cakeInfo.put("Category", "cake");
        cakeInfo.put("Description", "");
        cakeInfo.put("ID", randomKey);
        cakeInfo.put("Name", name);
        cakeInfo.put("Price", price);
        cakeInfo.put("Date", saveCurrentDate);
        cakeInfo.put("RDate", saveCurrentDate);
        cakeInfo.put("imageRef", downloadImageUrl);
        cakeInfo.put("Slots", Integer.parseInt(slots));

        store.collection("Products").document(randomKey)
                .set(cakeInfo)
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
                        Toast.makeText(AddCakesActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        Toast.makeText(AddCakesActivity.this, "Adding Product Successful", Toast.LENGTH_SHORT).show();
        onBackPressed();
    }
}