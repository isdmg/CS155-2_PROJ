package com.example.midnight_chevves.Customer.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midnight_chevves.Model.Products;
import com.example.midnight_chevves.R;
import com.example.midnight_chevves.ViewHolder.ProductViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class HomeFragment extends Fragment {

    private TextView txtCake, txtBox, txtWine;
    private RecyclerView recyclerView1, recyclerView2, recyclerView3;
    private DatabaseReference ProductsRef;

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

        ProductsRef = FirebaseDatabase.getInstance().getReference().child("Products");
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Products, ProductViewHolder> adapter;
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

    private FirebaseRecyclerAdapter<Products, ProductViewHolder> populateRecyclerView(String category) {
        if (category.equals(category)) {
            FirebaseRecyclerOptions<Products> options =
                    new FirebaseRecyclerOptions.Builder<Products>()
                            .setQuery(ProductsRef.orderByChild("category").equalTo(category), Products.class)
                            .build();

            FirebaseRecyclerAdapter<Products, ProductViewHolder> adapter =
                    new FirebaseRecyclerAdapter<Products, ProductViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull ProductViewHolder holder, int position, @NonNull final Products model) {
                            holder.txtProductName.setText(model.getPname());
                            holder.txtProductDescription.setText(model.getDescription());
                            holder.txtProductPrice.setText("₱" + model.getPrice());
                            // Shows that no slots are left by changing the LinearLayout transparency to 25/100.
                            if (model.getSlots() == 0) {
                                holder.linearLayout.setAlpha(0.25f);
                            } else {
                                holder.linearLayout.setAlpha(1f);
                            }

                            Picasso.get().load(model.getImage()).into(holder.imageView);
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
//                                Intent intent = new Intent(HomeActivity.this, ProductDetailsActivity.class);
//                                intent.putExtra("pid", model.getPid());
//                                startActivity(intent);
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
