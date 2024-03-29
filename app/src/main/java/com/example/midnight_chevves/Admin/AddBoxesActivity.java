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
import com.example.midnight_chevves.SignUpActivity;
import com.example.midnight_chevves.SignUpStep2Activity;
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


public class AddBoxesActivity extends AppCompatActivity {

    private TextInputLayout layoutName, layoutPrice, layoutSlots;
    private TextInputEditText inputName, inputPrice, inputSlots;
    private Button btnAddBox;

    private FirebaseFirestore store;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;

    private ImageView boxImage;
    private Uri imageUri;
    private String downloadImageUrl;

    private ActivityResultLauncher<Intent> someActivityResultLauncher;

    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_boxes);

        store = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        layoutName = findViewById(R.id.layout_add_box_name);
        layoutPrice = findViewById(R.id.layout_add_box_price);
        layoutSlots = findViewById(R.id.layout_add_box_slots);
        inputName = findViewById(R.id.add_box_name);
        inputPrice = findViewById(R.id.add_box_price);
        inputSlots = findViewById(R.id.add_box_slots);
        boxImage = findViewById(R.id.add_boxes_image);
        bundle = new Bundle();

        btnAddBox = findViewById(R.id.button_add_box);

        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            imageUri = result.getData().getData();
                            Picasso.get().load(imageUri).into(boxImage);
                        }
                    }
                });

        boxImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGalleryForResult();
            }
        });

        btnAddBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUri != null) {
                    PerformAuth();
                } else {
                    Toast.makeText(AddBoxesActivity.this, "Please select a product image!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openGalleryForResult() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        someActivityResultLauncher.launch(intent);
    }

    private void PerformAuth() {
        String name = inputName.getText().toString();
        String price = inputPrice.getText().toString();
        String slots = inputSlots.getText().toString();

        if (slots.isEmpty()) {
            layoutSlots.setErrorEnabled(true);
            layoutSlots.setError("Slots field is required!");
            inputSlots.requestFocus();
        } else {
            clearError(3);
        }

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
            bundle.putString("name", name);
            bundle.putString("price", price);
            bundle.putString("slots", slots);
            bundle.putParcelable("uri", imageUri);
            bundle.putString("product", "box");
            sendUserToNextActivity();
        }
    }

    private void sendUserToNextActivity() {
        Intent intent = new Intent(AddBoxesActivity.this, AddDescriptionActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void clearError(int field) {
        if (field == 1) {
            layoutName.setError(null);
            layoutName.setErrorEnabled(false);
        } else if (field == 2) {
            layoutPrice.setError(null);
            layoutPrice.setErrorEnabled(false);
        } else {
            layoutSlots.setError(null);
            layoutSlots.setErrorEnabled(false);
        }
    }

    private boolean withoutErrors() {
        if (!layoutName.isErrorEnabled() && !layoutPrice.isErrorEnabled() && !layoutSlots.isErrorEnabled()) {
            return true;
        } else {
            return false;
        }
    }
}