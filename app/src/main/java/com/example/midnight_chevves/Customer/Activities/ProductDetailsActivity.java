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
    private TextView productName, productPrice, productSlots;
    private Button btnAddToCart;
    private ImageButton btnBack;
    private ImageView productImage;
    private ElegantNumberButton btnQuantity;

    private FirebaseAuth auth;
    private FirebaseFirestore store;
    private CollectionReference listReference, productReference, extraReference;
    private StorageReference storageReference;

    private RecyclerView recyclerViewExtra;
    private List<Integer> quantityList;

    ArrayList<HashMap<String, Object>> extraInfo;
    ArrayList<HashMap<String, Object>> updateInfo;
    ArrayList<HashMap<String, Object>> deleteInfo;
    ArrayList<String> extraProductID;

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


        bundle = getIntent().getExtras();
//        ListID = getIntent().getStringExtra("ListID");
//        ID = getIntent().getStringExtra("ID");
//        extraID = getIntent().getStringArrayListExtra("ExtraID");
        ListID = bundle.getString("ListID");
        ID = bundle.getString("ProductID");
        extraProductID = bundle.getStringArrayList("ExtraProductID");

        productName = findViewById(R.id.details_name);
        productPrice = findViewById(R.id.details_price);
        productSlots = findViewById(R.id.details_slots);
        productImage = findViewById(R.id.details_image);
        btnQuantity = findViewById(R.id.button_quantity);
        getProductDetails();

        btnBack = findViewById(R.id.details_back);
        btnAddToCart = findViewById(R.id.button_add_to_cart);
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
                    productName.setText(snapshot.getString("Name"));
                    productPrice.setText(String.valueOf(snapshot.get("Price")));
                    productSlots.setText(snapshot.get("Slots").toString());

                    btnQuantity.setRange(1, Integer.parseInt(productSlots.getText().toString()));
//                    int quantity = getIntent().getIntExtra("Quantity", 0);
                    int quantity = bundle.getInt("Quantity");
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
                                        holder.btnQuantity.setRange(0, model.getSlots());

                                        // Shows that no slots are left by changing the LinearLayout transparency to 25/100.
                                        if (model.getSlots() == 0) {
                                            holder.linearLayout.setAlpha(0.25f);
                                        } else {
                                            holder.linearLayout.setAlpha(1f);
                                        }
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
                                        if (model.getSlots() == 0) {
                                            holder.linearLayout.setAlpha(0.25f);
                                            holder.btnQuantity.setNumber("0");
                                        } else {
                                            holder.linearLayout.setAlpha(1f);
                                        }

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
                Log.d("NameA", adapter.getItem(i).getName());
                Log.d("IntegerA", String.valueOf(quantityList.get(i)));

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
                    hMap.put("PurchaseDate", date);
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