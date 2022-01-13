package com.example.midnight_chevves;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.midnight_chevves.Admin.ManageOrdersActivity;
import com.example.midnight_chevves.Model.Cart;
import com.example.midnight_chevves.Model.Orders;
import com.example.midnight_chevves.ViewHolder.CartViewHolder;
import com.example.midnight_chevves.ViewHolder.OrderViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

public class OrderDetailsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView txtShippingAddress,txtPaymentMethod, txtGrandTotal;
    private FirebaseFirestore store;
    private RecyclerView recyclerViewProducts;
    private CollectionReference collectionReference;
    private FirestoreRecyclerAdapter<Cart, CartViewHolder> adapter;
    private int overTotalPrice = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(OrderDetailsActivity.this, RecyclerView.VERTICAL, false);

        recyclerViewProducts = (RecyclerView) findViewById(R.id.recycler_view_order_details);
        recyclerViewProducts.setLayoutManager(layoutManager);

        store = FirebaseFirestore.getInstance();
        txtShippingAddress = findViewById(R.id.order_details_shipping_address_data);
        txtPaymentMethod = findViewById(R.id.order_details_payment_method_data);
        txtGrandTotal = findViewById(R.id.order_details_grand_total_data);

        collectionReference = store.collection("Orders");
        getOrderDetails();

        btnBack = findViewById(R.id.order_details_back);
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
        FirestoreRecyclerOptions<Cart> options =
                new FirestoreRecyclerOptions.Builder<Cart>()
                        .setQuery(collectionReference.document(getIntent().getStringExtra("orderId")).collection("Products"), Cart.class)
                        .build();

        adapter =
                new FirestoreRecyclerAdapter<Cart, CartViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull CartViewHolder holder, int position, @NonNull final Cart model) {
                        holder.txtProductQuantity.setText("Quantity = " + model.getQuantity());
                        holder.txtProductPrice.setText("Price = " + "₱" + ((Integer.valueOf(model.getProductPrice()))) * Integer.valueOf(model.getQuantity()));
                        holder.txtProductName.setText(model.getProductName());
                        Log.d("WineHolder", model.getProductName());
                        int oneTypeProductTPrice = ((Integer.valueOf(model.getProductPrice()))) * Integer.valueOf(model.getQuantity());
                        overTotalPrice = overTotalPrice + oneTypeProductTPrice;
                    }

                    @NonNull
                    @Override
                    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items_layout, parent, false);
                        CartViewHolder holder = new CartViewHolder(view);
                        return holder;
                    }
                };
        recyclerViewProducts.setAdapter(adapter);
        adapter.startListening();
    }

    private void getOrderDetails() {
        String docId = getIntent().getStringExtra("orderId");

        final DocumentReference docRef = store.collection("Orders").document(docId);
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
                    txtPaymentMethod.setText(snapshot.getString("PaymentMethod"));
                    long l = (Long) snapshot.get("TotalAmount");
                    txtGrandTotal.setText(String.valueOf(l));

                    String accountRef = snapshot.getString("accountRef");


                    final DocumentReference docRef = store.collection("Orders").
                            document(docId).collection("Details").document(accountRef);
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
                                txtShippingAddress.setText(snapshot.getString("Address"));
                            } else {
                                Log.d(TAG, "Current data: null");
                            }
                        }
                    });
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

    }
}