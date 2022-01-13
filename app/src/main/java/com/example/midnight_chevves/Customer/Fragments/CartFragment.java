package com.example.midnight_chevves.Customer.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midnight_chevves.Customer.Activities.CheckoutActivity;
import com.example.midnight_chevves.Customer.Activities.ProductDetailsActivity;
import com.example.midnight_chevves.Model.Cart;
import com.example.midnight_chevves.Model.Products;
import com.example.midnight_chevves.R;
import com.example.midnight_chevves.ViewHolder.CartViewHolder;
import com.example.midnight_chevves.ViewHolder.ProductViewHolder;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

public class CartFragment extends Fragment {

    private RecyclerView recyclerViewCart;

    private FirebaseAuth auth;
    private FirebaseFirestore store;
    private int overTotalPrice = 0;
    private CollectionReference collectionReference;
    private FirestoreRecyclerAdapter<Cart, CartViewHolder> adapter;

    private Button btnCheckout;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_customer_cart, container, false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);

        recyclerViewCart = (RecyclerView) v.findViewById(R.id.recycler_view_cart);
        recyclerViewCart.setLayoutManager(layoutManager);

        auth = FirebaseAuth.getInstance();
        store = FirebaseFirestore.getInstance();
        collectionReference = store.collection("Carts").document(auth.getUid()).collection("List");
        btnCheckout = v.findViewById(R.id.button_checkout);

        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkout();
            }
        });

        return v;
    }

    public void onStart() {
        super.onStart();
        FirestoreRecyclerOptions<Cart> options =
                new FirestoreRecyclerOptions.Builder<Cart>()
                        .setQuery(collectionReference, Cart.class)
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

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence options[] = new CharSequence[]
                                        {
                                                "Edit",
                                                "Remove"
                                        };
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Cart Options: ");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (i == 0) {
                                            Intent intent = new Intent(getActivity(), ProductDetailsActivity.class);
                                            intent.putExtra("ID", model.getProductID());
                                            startActivity(intent);
                                        } else {
//                                            cartListRef.child("User view")
//                                                    .child(Prevalent.currentOnlineUser.getPhone())
//                                                    .child("Products")
//                                                    .child(model.getPid())
//                                                    .removeValue()
//                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                        @Override
//                                                        public void onComplete(@NonNull Task<Void> task) {
//                                                            if (task.isSuccessful()){
//                                                                Toast.makeText(CartActivity.this,"Item Removed",Toast.LENGTH_SHORT).show();
//                                                                Intent intent = new Intent(CartActivity.this,HomeActivity.class);
//                                                                startActivity(intent);
//                                                            }
//                                                        }
//                                                    });

                                            collectionReference.whereEqualTo("ProductID", model.getProductID()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        for (DocumentSnapshot document : task.getResult()) {
                                                            collectionReference.document(document.getId()).delete();
                                                        }
                                                    } else {
                                                        Log.d(CartFragment.class.getSimpleName(), "Error getting documents: ", task.getException());
                                                    }
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
                    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items_layout, parent, false);
                        CartViewHolder holder = new CartViewHolder(view);
                        return holder;
                    }
                };
        recyclerViewCart.setAdapter(adapter);
        adapter.startListening();
    }

    private void checkout() {
        if (adapter.getItemCount() != 0) {
            Intent intent = new Intent(getActivity(), CheckoutActivity.class);
            intent.putExtra("totalAmount", overTotalPrice);
            Log.d("total", Integer.toString(overTotalPrice));
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), "Cart is empty!", Toast.LENGTH_SHORT).show();
        }
    }
}
