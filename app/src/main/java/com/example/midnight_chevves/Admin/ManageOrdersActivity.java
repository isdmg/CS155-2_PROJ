package com.example.midnight_chevves.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.midnight_chevves.Model.Orders;
import com.example.midnight_chevves.OrderDetailsActivity;
import com.example.midnight_chevves.R;
import com.example.midnight_chevves.ViewHolder.OrderViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Arrays;

public class ManageOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerViewOrders;

    private FirebaseFirestore store;
    private CollectionReference collectionReference;
    private FirestoreRecyclerAdapter<Orders, OrderViewHolder> adapter;
    private FirestoreRecyclerOptions<Orders> options;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_orders);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ManageOrdersActivity.this, RecyclerView.VERTICAL, false);

        recyclerViewOrders = (RecyclerView) findViewById(R.id.recycler_view_manage_orders);
        recyclerViewOrders.setLayoutManager(layoutManager);
        store = FirebaseFirestore.getInstance();

        collectionReference = store.collection("Orders");


        btnBack = findViewById(R.id.manage_orders_back);
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
                        .setQuery(collectionReference.whereIn("OrderStatus", Arrays.asList("Out for delivery", "Ordered")), Orders.class)
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
                                Intent intent = new Intent(ManageOrdersActivity.this, OrderDetailsActivity.class);
                                intent.putExtra("orderId", model.getOrderId());
                                startActivity(intent);
                            }
                        });

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence options[] = new CharSequence[]
                                        {
                                                "Out for delivery",
                                                "Delivered"
                                        };
                                AlertDialog.Builder builder = new AlertDialog.Builder(ManageOrdersActivity.this);
                                builder.setTitle("Set Order Status: ");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        DocumentReference df = store.collection("Orders").document(model.getOrderId());
                                        if (i == 0) {
                                            df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    df.update("OrderStatus", "Out for delivery");
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(ManageOrdersActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else {
                                            df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    df.update("OrderStatus", "Delivered");
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(ManageOrdersActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });
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