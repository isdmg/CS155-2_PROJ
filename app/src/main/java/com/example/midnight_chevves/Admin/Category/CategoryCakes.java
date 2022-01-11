package com.example.midnight_chevves.Admin.Category;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.midnight_chevves.Admin.AddCakesActivity;
import com.example.midnight_chevves.Customer.Activities.LoginSecurityActivity;
import com.example.midnight_chevves.Customer.Activities.ProductDetailsActivity;
import com.example.midnight_chevves.Model.Products;
import com.example.midnight_chevves.R;
import com.example.midnight_chevves.ViewHolder.ProductViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class CategoryCakes extends AppCompatActivity {


    private TextView txtCake;
    private RecyclerView recyclerView_ADMIN_cakes;
    private FirebaseFirestore store;
    private StorageReference storageReference;
    private CollectionReference collectionReference;

    private Button btnAddCakes;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_cakes);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(CategoryCakes.this, 2);

        // TODO: Refactor variables within the activity and xml.
        recyclerView_ADMIN_cakes = (RecyclerView) findViewById(R.id.recycler_view_ADMIN_cake);
        recyclerView_ADMIN_cakes.setLayoutManager(gridLayoutManager);

        txtCake =findViewById(R.id.text_cake_ADMIN);


        store = FirebaseFirestore.getInstance();
        collectionReference = store.collection("Products");
        storageReference = FirebaseStorage.getInstance().getReference();

        btnAddCakes = findViewById(R.id.add_btn_cake);

        btnAddCakes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CategoryCakes.this, AddCakesActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirestoreRecyclerAdapter<Products, ProductViewHolder> adapter;
        adapter = populateRecyclerView("cake");
        recyclerView_ADMIN_cakes.setAdapter(adapter);
        adapter.startListening();

    }

    private FirestoreRecyclerAdapter<Products, ProductViewHolder> populateRecyclerView(String category) {
        FirestoreRecyclerOptions<Products> options =
                new FirestoreRecyclerOptions.Builder<Products>()
                        .setQuery(collectionReference.whereEqualTo("Category", category), Products.class)
                        .build();

        FirestoreRecyclerAdapter<Products, ProductViewHolder> adapter =
                new FirestoreRecyclerAdapter<Products, ProductViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ProductViewHolder holder, int position, @NonNull final Products model) {
                        holder.txtProductName.setText(model.getName());
                        holder.txtProductDescription.setText(model.getDescription());
                        holder.txtProductPrice.setText("₱" + model.getPrice());
                        Log.d("ProductHolder", model.getName());
                        // Shows that no slots are left by changing the LinearLayout transparency to 25/100.
                        if (model.getSlots() == 0) {
                            holder.linearLayout.setAlpha(0.25f);
                        } else {
                            holder.linearLayout.setAlpha(1f);
                        }

                        storageReference.child("Product Images/"+model.getID()+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get().load(uri).into(holder.imageView);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(CategoryCakes.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(CategoryCakes.this, ProductDetailsActivity.class);
                                intent.putExtra("ID", model.getID());
                                startActivity(intent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_items_layout_admin, parent, false);
                        ProductViewHolder holder = new ProductViewHolder(view);
                        return holder;
                    }
                };
        return adapter;
    }
}
