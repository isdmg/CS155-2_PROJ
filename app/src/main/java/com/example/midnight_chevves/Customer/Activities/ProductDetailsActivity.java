package com.example.midnight_chevves.Customer.Activities;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.midnight_chevves.Model.AddOns;
import com.example.midnight_chevves.R;
import com.example.midnight_chevves.ViewHolder.AddOnsViewHolder;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ProductDetailsActivity extends AppCompatActivity {

    private String ID, ListID, category;
    private Bundle bundle;
    private TextView productName, productDescription, productPrice, productSlots, extrasHeader, customizeHeader, scrollDown;
    private Button btnAddToCart;
    private ImageButton btnBack;
    private ImageView productImage, horizontalSep;
    private ElegantNumberButton btnQuantity;

    private FirebaseAuth auth;
    private FirebaseFirestore store;
    private CollectionReference listReference, productReference, extraReference;
    private StorageReference storageReference;

    private RecyclerView recyclerViewExtra;
    private List<Integer> quantityList;

    private ArrayList<HashMap<String, Object>> extraInfo;
    private ArrayList<HashMap<String, Object>> updateInfo;
    private ArrayList<HashMap<String, Object>> deleteInfo;
    private ArrayList<String> extraProductID;

    private boolean isEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        auth = FirebaseAuth.getInstance();
        store = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        listReference = store.collection("Carts").document(auth.getUid()).collection("List");
        productReference = store.collection("Products");
        extraReference = store.collection("Carts").document(auth.getUid()).collection("Extras");
        btnAddToCart = findViewById(R.id.button_add_to_cart);

        bundle = getIntent().getExtras();
        ListID = bundle.getString("ListID");
        ID = bundle.getString("ProductID");
        extraProductID = bundle.getStringArrayList("ExtraProductID");

        if (bundle.getBoolean("Edit")) {
            isEdit = true;
            btnAddToCart.setText("Save");
        } else {
            isEdit = false;
        }

        productName = findViewById(R.id.details_name);
        productDescription = findViewById(R.id.details_description);
        productPrice = findViewById(R.id.details_price);
        productSlots = findViewById(R.id.details_slots);
        productImage = findViewById(R.id.details_image);
        btnQuantity = findViewById(R.id.button_quantity);
        customizeHeader = findViewById(R.id.textview_customize);
        extrasHeader = findViewById(R.id.textview_extras);
        horizontalSep = findViewById(R.id.line_prod_details);
        scrollDown = findViewById(R.id.scrolldownformore);
        getProductDetails();

        btnBack = findViewById(R.id.details_back);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ProductDetailsActivity.this, RecyclerView.HORIZONTAL, false);
        recyclerViewExtra = (RecyclerView) findViewById(R.id.recycler_view_add_ons2);
        recyclerViewExtra.setLayoutManager(layoutManager);

        quantityList = new ArrayList<>();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
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


                    listReference.whereEqualTo("ProductID", snapshot.getString("ID")).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            int cumulativeSlots = 0;
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot document : task.getResult()) {
                                    if (ListID == null) {
                                        cumulativeSlots += document.getLong("Quantity");
                                    } else {
                                        if (!document.getId().equals(ListID)) {
                                            cumulativeSlots += document.getLong("Quantity");
                                        }
                                    }
                                }
                                if (snapshot.getLong("Slots") - cumulativeSlots < 1) {
                                    if (!isEdit) {
                                        btnQuantity.setVisibility(View.GONE);
                                        btnAddToCart.setVisibility(View.GONE);
                                        recyclerViewExtra.setVisibility(View.GONE);
                                        customizeHeader.setVisibility(View.GONE);
                                        extrasHeader.setVisibility(View.GONE);
                                        horizontalSep.setVisibility(View.GONE);
                                        scrollDown.setVisibility(View.GONE);
                                    }
                                } else {
                                    btnQuantity.setVisibility(View.VISIBLE);
                                    btnAddToCart.setVisibility(View.VISIBLE);
                                    recyclerViewExtra.setVisibility(View.VISIBLE);
                                    customizeHeader.setVisibility(View.VISIBLE);
                                    extrasHeader.setVisibility(View.VISIBLE);
                                    horizontalSep.setVisibility(View.VISIBLE);
                                    scrollDown.setVisibility(View.VISIBLE);
                                }

                                productSlots.setText("Available Slots: " + (snapshot.getLong("Slots") - cumulativeSlots));
                                btnQuantity.setRange(1, snapshot.getLong("Slots").intValue() - cumulativeSlots);
                            }
                        }
                    });

                    productName.setText(snapshot.getString("Name"));
                    productDescription.setText(snapshot.getString("Description"));
                    productPrice.setText("₱" + String.valueOf(snapshot.get("Price")));

                    int quantity = bundle.getInt("Quantity");
                    if (quantity != 0) {
                        btnQuantity.setNumber(String.valueOf(quantity));
                    }

                    Picasso.get().load(snapshot.getString("imageRef")).into(productImage);

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

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

                    category = snapshot.getString("Category");
                    Log.d("Category", category);

                    FirestoreRecyclerOptions<AddOns> options;
                    FirestoreRecyclerAdapter<AddOns, AddOnsViewHolder> adapter = null;

                    if (category.equals("cake")) {
                        options =
                                new FirestoreRecyclerOptions.Builder<AddOns>()
                                        .setQuery(productReference.whereEqualTo("Category", "extra"), AddOns.class)
                                        .build();

                        adapter =
                                new FirestoreRecyclerAdapter<AddOns, AddOnsViewHolder>(options) {
                                    @NonNull
                                    @Override
                                    public AddOns getItem(int position) {
                                        return getSnapshots().get(position);
                                    }


                                    @Override
                                    protected void onBindViewHolder(@NonNull AddOnsViewHolder holder, int position, @NonNull final AddOns model) {
                                        holder.txtProductName.setText(model.getName());
                                        holder.txtProductPrice.setText("₱" + model.getPrice());

                                        // Shows that no slots are left by changing the LinearLayout transparency to 25/100.
                                        extraReference.whereEqualTo("ProductID", model.getID()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                int cumulativeSlots = 0;
                                                if (task.isSuccessful()) {
                                                    for (DocumentSnapshot document : task.getResult()) {
                                                        if (ListID == null) {
                                                            cumulativeSlots += document.getLong("Quantity");
                                                        } else {
                                                            if (!document.getString("parentRef").equals(ListID)) {
                                                                Log.d("parentRefCum", document.getString("parentRef"));
                                                                cumulativeSlots += document.getLong("Quantity");
                                                            }
                                                        }
                                                    }
                                                    if (model.getSlots() - cumulativeSlots < 1) {
                                                        if (!isEdit) {
                                                            holder.linearLayout.setAlpha(0.25f);
                                                        }
                                                    } else {
                                                        holder.linearLayout.setAlpha(1f);
                                                    }
                                                    holder.btnQuantity.setRange(0, model.getSlots() - cumulativeSlots);
                                                }
                                            }
                                        });

                                        quantityList.add(0);

                                        if (ListID == null) {
                                            holder.btnQuantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
                                                @Override
                                                public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                                                    quantityList.set(holder.getAbsoluteAdapterPosition(), newValue);
                                                }
                                            });
                                        } else {
                                            if (!extraProductID.isEmpty()) {
                                                extraReference.whereEqualTo("parentRef", ListID).get()
                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                                                        if (Objects.equals(model.getID(), documentSnapshot.getString("ProductID"))) {
                                                                            holder.btnQuantity.setNumber(documentSnapshot.getLong("Quantity").toString());
                                                                            quantityList.set(holder.getAbsoluteAdapterPosition(), documentSnapshot.getLong("Quantity").intValue());
                                                                            holder.btnQuantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
                                                                                @Override
                                                                                public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                                                                                    quantityList.set(holder.getAbsoluteAdapterPosition(), newValue);
                                                                                }
                                                                            });
                                                                        } else {
                                                                            holder.btnQuantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
                                                                                @Override
                                                                                public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                                                                                    quantityList.set(holder.getAbsoluteAdapterPosition(), newValue);
                                                                                }
                                                                            });
                                                                        }
                                                                    }
                                                                } else {
                                                                    Log.d("test0", "Error getting documents: ", task.getException());
                                                                }
                                                            }
                                                        });
                                            }
                                            else {
                                                holder.btnQuantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
                                                    @Override
                                                    public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                                                        quantityList.set(holder.getAbsoluteAdapterPosition(), newValue);
                                                    }
                                                });
                                            }
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
                    } else {
                        options =
                                new FirestoreRecyclerOptions.Builder<AddOns>()
                                        .setQuery(productReference.whereEqualTo("Name", "Christmas Packing Upgrade"), AddOns.class)
                                        .build();

                        adapter =
                                new FirestoreRecyclerAdapter<AddOns, AddOnsViewHolder>(options) {
                                    @NonNull
                                    @Override
                                    public AddOns getItem(int position) {
                                        return getSnapshots().get(position);
                                    }

                                    @Override
                                    protected void onBindViewHolder(@NonNull AddOnsViewHolder holder, int position, @NonNull final AddOns model) {
                                        holder.txtProductName.setText(model.getName());
                                        holder.txtProductPrice.setText("₱" + model.getPrice());
                                        holder.btnQuantity.setRange(0, model.getSlots());


                                        // Shows that no slots are left by changing the LinearLayout transparency to 25/100.
                                        extraReference.whereEqualTo("ProductID", model.getID()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                int cumulativeSlots = 0;
                                                if (task.isSuccessful()) {
                                                    for (DocumentSnapshot document : task.getResult()) {
                                                        cumulativeSlots += document.getLong("Quantity");
                                                    }
                                                    if (model.getSlots() - cumulativeSlots == 0) {
                                                        holder.linearLayout.setAlpha(0.25f);
                                                    }
                                                    else {
                                                        holder.linearLayout.setAlpha(1f);
                                                    }
                                                    holder.btnQuantity.setRange(0, model.getSlots() - cumulativeSlots);
                                                }
                                            }
                                        });

                                        quantityList.add(0);

                                        if (ListID == null) {
                                            holder.btnQuantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
                                                @Override
                                                public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                                                    quantityList.set(holder.getAbsoluteAdapterPosition(), newValue);
                                                }
                                            });
                                        } else {
                                            if (!extraProductID.isEmpty()) {
                                                extraReference.whereEqualTo("parentRef", ListID).get()
                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                                                        if (Objects.equals(model.getID(), documentSnapshot.getString("ProductID"))) {
                                                                            holder.btnQuantity.setNumber(documentSnapshot.getLong("Quantity").toString());
                                                                            quantityList.set(holder.getAbsoluteAdapterPosition(), documentSnapshot.getLong("Quantity").intValue());
                                                                            holder.btnQuantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
                                                                                @Override
                                                                                public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                                                                                    quantityList.set(holder.getAbsoluteAdapterPosition(), newValue);
                                                                                }
                                                                            });
                                                                        } else {
                                                                            holder.btnQuantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
                                                                                @Override
                                                                                public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                                                                                    quantityList.set(holder.getAbsoluteAdapterPosition(), newValue);
                                                                                }
                                                                            });
                                                                        }
                                                                    }
                                                                } else {
                                                                    Log.d("test0", "Error getting documents: ", task.getException());
                                                                }
                                                            }
                                                        });
                                            }
                                            else {
                                                holder.btnQuantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
                                                    @Override
                                                    public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                                                        quantityList.set(holder.getAbsoluteAdapterPosition(), newValue);
                                                    }
                                                });
                                            }
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
                    }
                    recyclerViewExtra.setAdapter(adapter);
                    adapter.startListening();
                    FirestoreRecyclerAdapter<AddOns, AddOnsViewHolder> finalAdapter = adapter;
                    btnAddToCart.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d("Get Size of List", String.valueOf(quantityList.size()));
                            addToCart(finalAdapter);
                        }
                    });
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    private void addToCart(FirestoreRecyclerAdapter<AddOns, AddOnsViewHolder> adapter) {
        String randomKey;
        randomKey = UUID.randomUUID().toString();

        Map<String, Object> cartInfo = new HashMap<>();
        cartInfo.put("ListID", randomKey);
        cartInfo.put("ProductID", ID);
        cartInfo.put("ProductName", productName.getText().toString());
        cartInfo.put("ProductPrice", Integer.parseInt(productPrice.getText().toString().substring(1)));
        cartInfo.put("Quantity", Integer.parseInt(btnQuantity.getNumber()));

        extraInfo = new ArrayList<>();
        updateInfo = new ArrayList<>();
        deleteInfo = new ArrayList<>();

        Log.d("QuantitySize", String.valueOf(quantityList.size()));


        for (int i = 0; i < quantityList.size(); i++) {
            String id = adapter.getItem(i).getID();
            String name = adapter.getItem(i).getName();
            int price = adapter.getItem(i).getPrice();
            int quantity = quantityList.get(i);


            if (quantityList.get(i) != 0) {

                HashMap<String, Object> hMap = new HashMap<>();


                if (extraProductID != null && extraProductID.contains(id)) {
                    Log.d("NameFound", adapter.getItem(i).getName());
                    Log.d("IntegerFound", String.valueOf(quantityList.get(i)));
                    HashMap<String, Object> uMap = new HashMap<>();
                    uMap.put("ProductID", id);
                    uMap.put("Quantity", quantity);
                    updateInfo.add(uMap);
                } else {
                    hMap.put("ExtraID", UUID.randomUUID().toString());
                    hMap.put("ProductName", name);
                    hMap.put("Quantity", quantity);
                    hMap.put("ProductID", id);
                    hMap.put("ProductPrice", price);

                    if (ListID == null) {
                        hMap.put("parentRef", randomKey);
                    } else {
                        hMap.put("parentRef", ListID);
                    }

                    extraInfo.add(hMap);
                }
            } else {
                if (extraProductID != null && extraProductID.contains(id)) {
                    HashMap<String, Object> dMap = new HashMap<>();
                    Log.d("NameB", adapter.getItem(i).getName());
                    Log.d("IntegerB", String.valueOf(quantityList.get(i)));
                    dMap.put("ProductID", id);
                    deleteInfo.add(dMap);
                }
            }
        }

        Log.d("ExtraInfoSize", String.valueOf(extraInfo.size()));
        for (HashMap<String, Object> e : extraInfo) {
            Log.d("testMap", e.get("ProductID").toString());
        }

        listReference
                .whereEqualTo("ListID", ListID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        String documentID = null;
                        boolean documentExists = false;
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                documentID = document.getId();
                                documentExists = document.exists();
                            }
                            Log.d("testDocExists", Boolean.toString(documentExists));
                            if (documentExists) {
                                listReference.document(documentID).update("Quantity", Integer.parseInt(btnQuantity.getNumber()));

                                if (!updateInfo.isEmpty()) {
                                    for (HashMap<String, Object> u : updateInfo) {
                                        extraReference.whereEqualTo("parentRef", ListID).whereEqualTo("ProductID", u.get("ProductID")).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (DocumentSnapshot document : task.getResult()) {
                                                        extraReference.document(document.getId()).update("Quantity", u.get("Quantity"));
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }

                                if (!deleteInfo.isEmpty()) {
                                    for (HashMap<String, Object> d : deleteInfo) {
                                        extraReference.whereEqualTo("parentRef", ListID).whereEqualTo("ProductID", d.get("ProductID")).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (DocumentSnapshot document : task.getResult()) {
                                                        extraReference.document(document.getId()).delete();
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }
                                onBackPressed();
                            } else {
                                listReference.document(randomKey)
                                        .set(cartInfo)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(ProductDetailsActivity.this, "Added to Cart", Toast.LENGTH_SHORT).show();
                                                onBackPressed();
                                            }
                                        });
                            }
                            if (!extraInfo.isEmpty()) {
                                for (HashMap<String, Object> e : extraInfo) {
                                    extraReference.document(e.get("ExtraID").toString()).set(e);
                                }
                            }
                        }
                    }
                });
    }
}