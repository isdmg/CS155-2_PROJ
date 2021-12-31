package com.example.midnight_chevves.Customer.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midnight_chevves.Customer.Activities.LoginSecurityActivity;
import com.example.midnight_chevves.Customer.Activities.ProductDetailsActivity;
import com.example.midnight_chevves.Model.Products;
import com.example.midnight_chevves.R;
import com.example.midnight_chevves.ViewHolder.ProductViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import com.google.firebase.firestore.Query;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class HomeFragment extends Fragment {

    private TextView txtCake, txtBox, txtWine;
    private RecyclerView recyclerView1, recyclerView2, recyclerView3;
    private FirebaseFirestore store;
    private StorageReference storageReference;
    private CollectionReference collectionReference;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_customer_home, container, false);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);
        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);
        RecyclerView.LayoutManager layoutManager3 = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);

        // TODO: Refactor variables within the activity and xml.
        recyclerView1 = (RecyclerView) v.findViewById(R.id.recycler_view1);
        recyclerView1.setLayoutManager(layoutManager);

        recyclerView2 = (RecyclerView) v.findViewById(R.id.recycler_view2);
        recyclerView2.setLayoutManager(layoutManager2);

        recyclerView3 = (RecyclerView) v.findViewById(R.id.recycler_view3);
        recyclerView3.setLayoutManager(layoutManager3);

        txtCake = v.findViewById(R.id.text_cake);
        txtBox = v.findViewById(R.id.text_char_cute_rie_boxes);
        txtWine = v.findViewById(R.id.text_wines);

        store = FirebaseFirestore.getInstance();
        collectionReference = store.collection("Products");
        storageReference = FirebaseStorage.getInstance().getReference();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirestoreRecyclerAdapter<Products, ProductViewHolder> adapter;
        adapter = populateRecyclerView("cake");
        recyclerView1.setAdapter(adapter);
        adapter.startListening();
        adapter = populateRecyclerView("box");
        recyclerView2.setAdapter(adapter);
        adapter.startListening();
        adapter = populateRecyclerView("wine");
        recyclerView3.setAdapter(adapter);
        adapter.startListening();
        // TODO: Use Global...
    }

    private FirestoreRecyclerAdapter<Products, ProductViewHolder> populateRecyclerView(String category) {
        if (category.equals(category)) {
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
                                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                Intent intent = new Intent(getActivity(), ProductDetailsActivity.class);
                                intent.putExtra("ID", model.getID());
                                startActivity(intent);
                                }
                            });
                        }

                        @NonNull
                        @Override
                        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_items_layout, parent, false);
                            ProductViewHolder holder = new ProductViewHolder(view);
                            return holder;
                        }
                    };
            return adapter;
        } else {
            return null;
        }
    }
}
