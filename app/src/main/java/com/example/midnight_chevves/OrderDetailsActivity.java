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

import com.example.midnight_chevves.Model.Cart;
import com.example.midnight_chevves.ViewHolder.CartViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

public class OrderDetailsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView txtShippingAddress,txtPaymentMethod, txtTotalAmount;
    private FirebaseFirestore store;
    private RecyclerView recyclerViewProducts;
    private CollectionReference collectionReference, extraReference, productReference;
    private FirestoreRecyclerAdapter<Cart, CartViewHolder> adapter;


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
        txtTotalAmount = findViewById(R.id.order_details_total_amount_data);

        productReference = store.collection("Products");
        collectionReference = store.collection("Orders");
        extraReference = collectionReference.document(getIntent().getStringExtra("orderId")).collection("Extras");
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
                        .setQuery(collectionReference.document(getIntent().getStringExtra("orderId")).collection("Products").orderBy("ProductName", Query.Direction.ASCENDING), Cart.class)
                        .build();

        adapter =
                new FirestoreRecyclerAdapter<Cart, CartViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull CartViewHolder holder, int position, @NonNull final Cart model) {
                        holder.txtProductQuantity.setText("Quantity: " + model.getQuantity());
                        holder.txtProductName.setText(model.getProductName());

                        long productPrice = model.getProductPrice() * model.getQuantity();

                        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                Intent intent = new Intent(OrderDetailsActivity.this, ItemDetailsActivity.class);
                                intent.putExtra("ListID", model.getListID());
                                intent.putExtra("ProductName", model.getProductName());
                                intent.putExtra("Quantity", model.getQuantity());
                                intent.putExtra("orderId",getIntent().getStringExtra("orderId"));
                                startActivity(intent);
                                return true;
                            }
                        });

                        extraReference.whereEqualTo("parentRef", model.getListID()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                long extraTotal = 0;
                                if (task.isSuccessful()) {
                                    for (DocumentSnapshot document : task.getResult()) {
                                        extraTotal += document.getLong("Quantity") * document.getLong("ProductPrice");
                                        Log.d("extraTotal", String.valueOf(extraTotal));
                                    }
                                }
                                long productTotalWithExtra = productPrice + extraTotal;
                                holder.txtProductPrice.setText("Price: ₱" + productTotalWithExtra);
                            }
                        });

                        productReference.whereEqualTo("ID", model.getProductID()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (DocumentSnapshot document : task.getResult()) {
                                        Picasso.get().load(document.getString("imageRef")).into(holder.imgProduct);
                                    }
                                }
                            }
                        });
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
                    txtTotalAmount.setText(String.valueOf(l));

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