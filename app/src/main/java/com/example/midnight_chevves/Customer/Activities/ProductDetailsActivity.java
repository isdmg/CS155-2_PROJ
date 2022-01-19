package com.example.midnight_chevves.Customer.Activities;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.midnight_chevves.Admin.Category.CategoryCakes;
import com.example.midnight_chevves.Admin.Category.CategoryExtras;
import com.example.midnight_chevves.Admin.EditProductsActivity;
import com.example.midnight_chevves.Model.AddOns;
import com.example.midnight_chevves.Model.Products;
import com.example.midnight_chevves.R;
import com.example.midnight_chevves.ViewHolder.AddOnsViewHolder;
import com.example.midnight_chevves.ViewHolder.ProductViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProductDetailsActivity extends AppCompatActivity {

    private String ID, category;
    private TextView productName, productPrice, productSlots;
    private Button btnAddToCart, btnAddOns;
    private ImageButton btnBack, btnBackAddOns;
    private ImageView productImage;
    private ElegantNumberButton btnQuantity;

    private FirebaseAuth auth;
    private FirebaseFirestore store;
    private CollectionReference collectionReference, collectionReference2;
    private StorageReference storageReference;

    private AlertDialog.Builder addOnsBuilder;
    private AlertDialog addOns;
    private Button btnAddOnsSave, btnAddOnsCancel;
    private RecyclerView recyclerViewExtra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        auth = FirebaseAuth.getInstance();
        store = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        collectionReference = store.collection("Carts").document(auth.getUid()).collection("List");
        collectionReference2 = store.collection("Products");
        ID = getIntent().getStringExtra("ID");
        productName = findViewById(R.id.details_name);
        productPrice = findViewById(R.id.details_price);
        productSlots = findViewById(R.id.details_slots);
        productImage = findViewById(R.id.details_image);
        btnQuantity = findViewById(R.id.button_quantity);
        getProductDetails();

        btnBack = findViewById(R.id.details_back);
        btnAddToCart = findViewById(R.id.button_add_to_cart);
        btnAddOns = findViewById(R.id.button_add_ons);

        // TODO: Add View.OnClickListener?
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        btnAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToCart();
            }
        });

        btnAddOns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAddOnsDialog();
            }
        });
    }

    private void getProductDetails() {
//        store.collection("Products").document(ID)
//                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                productName.setText(documentSnapshot.getString("Name"));
//                productPrice.setText(documentSnapshot.getString("Price"));
//                productSlots.setText(documentSnapshot.get("Slots").toString());
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(ProductDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
//                Toast.makeText(ProductDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    productName.setText(snapshot.getString("Name"));
                    productPrice.setText(String.valueOf(snapshot.get("Price")));
                    productSlots.setText(snapshot.get("Slots").toString());
                    category = snapshot.getString("Category");

                    btnQuantity.setRange(1, Integer.parseInt(productSlots.getText().toString()));
                    int quantity = getIntent().getIntExtra("Quantity", 0);
                    if (quantity != 0) {
                        btnQuantity.setNumber(String.valueOf(quantity));
                    }

                    Picasso.get().load(snapshot.getString("imageRef")).into(productImage);

                    if ((Long) snapshot.get("Slots") == 0) {
                        btnQuantity.setVisibility(View.INVISIBLE);
                        btnAddToCart.setEnabled(false);
                        btnAddToCart.setAlpha(0.25f);
                    } else {
                        btnQuantity.setVisibility(View.VISIBLE);
                        btnAddToCart.setEnabled(true);
                        btnAddToCart.setAlpha(1f);
                    }
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    private void addToCart() {
        String date, randomKey;
        randomKey = UUID.randomUUID().toString();
        Calendar calendarDate = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        date = dateFormat.format(calendarDate.getTime());

        Map<String, Object> cartInfo = new HashMap<>();
        cartInfo.put("ListID", randomKey);
        cartInfo.put("ProductID", ID);
        cartInfo.put("ProductName", productName.getText().toString());
        cartInfo.put("ProductPrice", Integer.parseInt(productPrice.getText().toString()));
        cartInfo.put("PurchaseDate", date);
        cartInfo.put("Quantity", Integer.parseInt(btnQuantity.getNumber()));

        collectionReference
                .whereEqualTo("ProductID", ID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                String documentID = null;
                boolean documentExists = false;
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        documentID = document.getId();
                        documentExists = document.exists();
                    }
                    Log.d("test0", Boolean.toString(documentExists));
                    if (documentExists) {
                        collectionReference.document(documentID).update("Quantity", Integer.parseInt(btnQuantity.getNumber()));
                        onBackPressed();
                    } else {
                        collectionReference.document(randomKey)
                                .set(cartInfo)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(ProductDetailsActivity.this, "Added to Cart", Toast.LENGTH_SHORT).show();
                                        onBackPressed();
                                    }
                                });
                    }
                } else {
                    Log.d(ProductDetailsActivity.class.getSimpleName(), "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void createAddOnsDialog() {
        addOnsBuilder = new AlertDialog.Builder(this);
        final View addOnsPopupView = getLayoutInflater().inflate(R.layout.add_ons, null);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ProductDetailsActivity.this, RecyclerView.HORIZONTAL, false);
        recyclerViewExtra = (RecyclerView) addOnsPopupView.findViewById(R.id.recycler_view_add_ons);
        recyclerViewExtra.setLayoutManager(layoutManager);
        btnBackAddOns = addOnsPopupView.findViewById(R.id.add_ons_back);
        btnBackAddOns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addOns.dismiss();
            }
        });

        ///
        if (category.equals("cake")) {
            FirestoreRecyclerOptions<AddOns> options;
            options =
                    new FirestoreRecyclerOptions.Builder<AddOns>()
                            .setQuery(collectionReference2.whereEqualTo("Category", "extra"), AddOns.class)
                            .build();

            FirestoreRecyclerAdapter<AddOns, AddOnsViewHolder> adapter =
                    new FirestoreRecyclerAdapter<AddOns, AddOnsViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull AddOnsViewHolder holder, int position, @NonNull final AddOns model) {
                            holder.txtProductName.setText(model.getName());
                            holder.txtProductPrice.setText("₱" + model.getPrice());

                            holder.btnQuantity.setRange(0, model.getSlots());

                            // Shows that no slots are left by changing the LinearLayout transparency to 25/100.
                            if (model.getSlots() == 0) {
                                holder.linearLayout.setAlpha(0.25f);
                            } else {
                                holder.linearLayout.setAlpha(1f);
                            }

                            storageReference.child("Product Images/" + model.getID() + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Picasso.get().load(uri).into(holder.imageView);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ProductDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                        @NonNull
                        @Override
                        public AddOnsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_ons_items_layout, parent, false);
                            AddOnsViewHolder holder = new AddOnsViewHolder(view);
                            return holder;
                        }
                    };
            recyclerViewExtra.setAdapter(adapter);
            adapter.startListening();
        } else {
            FirestoreRecyclerOptions<AddOns> options;
            options =
                    new FirestoreRecyclerOptions.Builder<AddOns>()
                            .setQuery(collectionReference2.whereEqualTo("Name", "Christmas Packing Upgrade"), AddOns.class)
                            .build();

            FirestoreRecyclerAdapter<AddOns, AddOnsViewHolder> adapter =
                    new FirestoreRecyclerAdapter<AddOns, AddOnsViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull AddOnsViewHolder holder, int position, @NonNull final AddOns model) {
                            holder.txtProductName.setText(model.getName());
                            holder.txtProductPrice.setText("₱" + model.getPrice());

                            holder.btnQuantity.setRange(0, model.getSlots());

                            // Shows that no slots are left by changing the LinearLayout transparency to 25/100.
                            if (model.getSlots() == 0) {
                                holder.linearLayout.setAlpha(0.25f);
                            } else {
                                holder.linearLayout.setAlpha(1f);
                            }

                            storageReference.child("Product Images/" + model.getID() + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Picasso.get().load(uri).into(holder.imageView);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ProductDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                        @NonNull
                        @Override
                        public AddOnsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_ons_items_layout, parent, false);
                            AddOnsViewHolder holder = new AddOnsViewHolder(view);
                            return holder;
                        }
                    };
            recyclerViewExtra.setAdapter(adapter);
            adapter.startListening();
        }

        ///

        addOnsBuilder.setView(addOnsPopupView);
        addOns = addOnsBuilder.create();
        addOns.show();


    }
}