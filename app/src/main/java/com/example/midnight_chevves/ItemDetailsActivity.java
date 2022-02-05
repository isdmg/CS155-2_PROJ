package com.example.midnight_chevves;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.midnight_chevves.Model.Extras;
import com.example.midnight_chevves.ViewHolder.ExtraViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ItemDetailsActivity extends AppCompatActivity {

    private String listID;
    private ImageButton btnBack;
    private FirebaseFirestore store;
    private TextView txtProductDetail;
    private RecyclerView recyclerViewExtras;
    private CollectionReference extraReference;
    private FirestoreRecyclerAdapter<Extras, ExtraViewHolder> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ItemDetailsActivity.this, RecyclerView.VERTICAL, false);

        recyclerViewExtras = (RecyclerView) findViewById(R.id.recycler_view_order_details);
        recyclerViewExtras.setLayoutManager(layoutManager);

        store = FirebaseFirestore.getInstance();
        extraReference = store.collection("Orders").document(getIntent().getStringExtra("orderId")).collection("Extras");

        txtProductDetail = findViewById(R.id.item_details_product);
        txtProductDetail.setText(getIntent().getStringExtra("ProductName") + " x" + getIntent().getIntExtra("Quantity", 0));

        listID = getIntent().getStringExtra("ListID");

        btnBack = findViewById(R.id.item_details_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirestoreRecyclerOptions<Extras> options =
                new FirestoreRecyclerOptions.Builder<Extras>()
                        .setQuery(extraReference.whereEqualTo("parentRef", listID).orderBy("ProductName", Query.Direction.ASCENDING), Extras.class)
                        .build();

        adapter =
                new FirestoreRecyclerAdapter<Extras, ExtraViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ExtraViewHolder holder, int position, @NonNull final Extras model) {
                        holder.txtProductQuantity.setText("Quantity: " + model.getQuantity());
                        holder.txtProductName.setText(model.getProductName());

                        long productPrice = model.getProductPrice() * model.getQuantity();
                        holder.txtProductPrice.setText("Price: ₱ " + productPrice);
                    }

                    @NonNull
                    @Override
                    public ExtraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.extra_items_layout, parent, false);
                        ExtraViewHolder holder = new ExtraViewHolder(view);
                        return holder;
                    }
                };
        recyclerViewExtras.setAdapter(adapter);
        adapter.startListening();
    }
}