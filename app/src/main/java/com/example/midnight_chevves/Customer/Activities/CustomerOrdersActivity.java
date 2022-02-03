package com.example.midnight_chevves.Customer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.midnight_chevves.Admin.ManageOrdersActivity;
import com.example.midnight_chevves.Customer.Fragments.CartFragment;
import com.example.midnight_chevves.Model.Cart;
import com.example.midnight_chevves.Model.Orders;
import com.example.midnight_chevves.OrderDetailsActivity;
import com.example.midnight_chevves.R;
import com.example.midnight_chevves.ViewHolder.OrderViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class CustomerOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerViewOrders;

    private FirebaseAuth auth;
    private FirebaseFirestore store;
    private CollectionReference collectionReference;
    private FirestoreRecyclerAdapter<Orders, OrderViewHolder> adapter;
    private ImageButton btnBack;
    private ImageView noItemsImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_orders);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(CustomerOrdersActivity.this, RecyclerView.VERTICAL, false);

        recyclerViewOrders = (RecyclerView) findViewById(R.id.recycler_view_orders);
        recyclerViewOrders.setLayoutManager(layoutManager);
        auth = FirebaseAuth.getInstance();
        store = FirebaseFirestore.getInstance();

        collectionReference = store.collection("Orders");

        noItemsImage = findViewById(R.id.no_orders);

        btnBack = findViewById(R.id.orders_back);
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
        FirestoreRecyclerOptions<Orders> options =
                new FirestoreRecyclerOptions.Builder<Orders>()
                        .setQuery(collectionReference.whereEqualTo("accountRef", auth.getUid()).orderBy("Timestamp", Query.Direction.DESCENDING), Orders.class)
                        .build();

        adapter =
                new FirestoreRecyclerAdapter<Orders, OrderViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull OrderViewHolder holder, int position, @NonNull final Orders model) {
                        holder.txtOrderId.setText("Order # " + "\n" + model.getOrderId());

                        SimpleDateFormat currentDate = new SimpleDateFormat("MM/dd/yyyy");
                        String saveCurrentDate = currentDate.format(model.getTimestamp().toDate());

                        holder.txtProductDate.setText("Order Placed: " + saveCurrentDate);

                        holder.txtProductStatus.setText(model.getOrderStatus());

                        holder.orderDetail.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(CustomerOrdersActivity.this, OrderDetailsActivity.class);
                                intent.putExtra("orderId", model.getOrderId());
                                startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public int getItemCount() {
                        return getSnapshots().size();
                    }

                    @Override
                    public void onDataChanged() {
                        adapter.notifyDataSetChanged();
                        if (getItemCount() == 0) {
                            noItemsImage.setVisibility(View.VISIBLE);
                        }

                        else {
                            noItemsImage.setVisibility(View.GONE);
                        }
                    }

                    @NonNull
                    @Override
                    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_items_layout, parent, false);
                        OrderViewHolder holder = new OrderViewHolder(view);
                        return holder;
                    }
                };
        recyclerViewOrders.setAdapter(adapter);
        adapter.startListening();
    }
}