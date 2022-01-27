package com.example.midnight_chevves.Customer.Fragments;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midnight_chevves.Customer.Activities.CheckoutActivity;
import com.example.midnight_chevves.Customer.Activities.ProductDetailsActivity;
import com.example.midnight_chevves.LoginActivity;
import com.example.midnight_chevves.Model.Cart;
import com.example.midnight_chevves.R;
import com.example.midnight_chevves.ViewHolder.CartViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CartFragment extends Fragment {

    private RecyclerView recyclerViewCart;

    private FirebaseAuth auth;
    private FirebaseFirestore store;
    private long overTotalPrice = 0;
    private long overTotalExtraPrice = 0;
    private long overTotalProductPrice = 0;
    private CollectionReference listReference, extraReference;

    private FirestoreRecyclerAdapter<Cart, CartViewHolder> adapter;
    private TextView subtotal;

    private Button btnCheckout;
    private HashMap<String, Object> updateListInfo;
    private HashMap<String, Object> updateExtraInfo;

    private boolean productPriceQueryFinish = true;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_customer_cart, container, false);
//        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);

        updateListInfo = new HashMap<>();
        updateExtraInfo = new HashMap<>();

        recyclerViewCart = (RecyclerView) v.findViewById(R.id.recycler_view_cart);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false) {
                                              @Override
                                              public void onLayoutCompleted(RecyclerView.State state) {
                                                  super.onLayoutCompleted(state);
                                                  validateCart();
                                              }
                                          }
        );


        auth = FirebaseAuth.getInstance();
        store = FirebaseFirestore.getInstance();
        listReference = store.collection("Carts").document(auth.getUid()).collection("List");
        extraReference = store.collection("Carts").document(auth.getUid()).collection("Extras");
        subtotal = v.findViewById(R.id.text_subtotal);
        btnCheckout = v.findViewById(R.id.button_checkout);
        btnCheckout.setVisibility(View.GONE);

        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkout();
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getTotal();
    }

    public void onStart() {
        super.onStart();
        FirestoreRecyclerOptions<Cart> options =
                new FirestoreRecyclerOptions.Builder<Cart>()
                        .setQuery(listReference, Cart.class)
                        .build();

        adapter =
                new FirestoreRecyclerAdapter<Cart, CartViewHolder>(options) {
                    @Override
                    public int getItemCount() {
                        return getSnapshots().size();
                    }

                    @Override
                    public void onDataChanged() {
                        adapter.notifyDataSetChanged();
                        validateCart();
                        getTotal();
                        if (getItemCount() == 0) {
                            btnCheckout.setVisibility(View.GONE);
                            subtotal.setVisibility(View.GONE);
                        } else {
                            btnCheckout.setVisibility(View.VISIBLE);
                            subtotal.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull CartViewHolder holder, int position, @NonNull final Cart model) {
                        holder.txtProductQuantity.setText("Quantity: " + Integer.toString(model.getQuantity()));
                        holder.txtProductName.setText(model.getProductName());
                        updateListInfo.put(model.getProductID(), model.getListID());
                        long productPrice = model.getProductPrice() * model.getQuantity();

                        extraReference.whereEqualTo("parentRef", model.getListID()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                long extraTotal = 0;
                                if (task.isSuccessful()) {
                                    for (DocumentSnapshot document : task.getResult()) {
                                        Log.d("parentRefTest", String.valueOf(document.getLong("ProductPrice")));
                                        updateExtraInfo.put(document.getString("ProductID"), document.getString("ExtraID"));
                                        extraTotal = document.getLong("Quantity") * document.getLong("ProductPrice");
                                        overTotalExtraPrice += extraTotal;
                                    }
                                }
                                long productTotalWithExtra = productPrice + extraTotal;
                                holder.txtProductPrice.setText("Price: ₱" + productTotalWithExtra);
                            }
                        });


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
                                            Bundle bundle = new Bundle();
                                            ArrayList<String> extraID = new ArrayList<>();

                                            bundle.putString("ProductID", model.getProductID());
                                            bundle.putString("ListID", model.getListID());
                                            bundle.putBoolean("Edit", true);
                                            bundle.putInt("Quantity", model.getQuantity());

                                            extraReference.whereEqualTo("parentRef", model.getListID()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        for (DocumentSnapshot document : task.getResult()) {
                                                            extraID.add(document.getString("ProductID"));
                                                            Log.d("check here", document.getString("ExtraID"));
                                                        }
                                                        bundle.putStringArrayList("ExtraProductID", extraID);
                                                        sendUserToNextActivity(bundle, intent);
                                                    } else {
                                                        Log.d(CartFragment.class.getSimpleName(), "Error getting documents: ", task.getException());
                                                    }
                                                }
                                            });
                                        } else {
                                            listReference.whereEqualTo("ListID", model.getListID()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        for (DocumentSnapshot document : task.getResult()) {
                                                            listReference.document(document.getId()).delete();
                                                        }
                                                    } else {
                                                        Log.d(CartFragment.class.getSimpleName(), "Error getting documents: ", task.getException());
                                                    }
                                                }
                                            });

                                            extraReference.whereEqualTo("parentRef", model.getListID()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        for (DocumentSnapshot document : task.getResult()) {
                                                            extraReference.document(document.getId()).delete();
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

    private void sendUserToNextActivity(Bundle bundle, Intent intent) {
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void checkout() {
        Intent intent = new Intent(getActivity(), CheckoutActivity.class);
        intent.putExtra("totalAmount", overTotalPrice);
        startActivity(intent);
    }

    private void validateCart() {
        for (Map.Entry<String, Object> entry : updateListInfo.entrySet()) {
            final DocumentReference docRef = store.collection("Products").document(entry.getKey());
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


                        listReference.whereEqualTo("ProductID", snapshot.getString("ID")).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                int cumulativeSlots = 0;
                                if (task.isSuccessful()) {
                                    for (DocumentSnapshot document : task.getResult()) {
                                        cumulativeSlots += document.getLong("Quantity");
                                    }

                                    long productSlots = snapshot.getLong("Slots");

                                    if (productSlots == 0) {
                                        listReference.document(String.valueOf(entry.getValue())).delete();

                                        extraReference.whereEqualTo("parentRef", String.valueOf(entry.getValue())).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (DocumentSnapshot document : task.getResult()) {
                                                        extraReference.document(document.getId()).delete();
                                                    }
                                                } else {
                                                    Log.d(CartFragment.class.getSimpleName(), "Error getting documents: ", task.getException());
                                                }
                                            }
                                        });
                                    } else if (productSlots < cumulativeSlots) {
                                        listReference.document(String.valueOf(entry.getValue())).update("Quantity", snapshot.getLong("Slots"));
                                    }
                                }
                            }
                        });
                    } else {
                        Log.d(TAG, "Current data: null");
                    }
                }
            });
        }

        for (Map.Entry<String, Object> entry : updateExtraInfo.entrySet()) {
            final DocumentReference docRef = store.collection("Products").document(entry.getKey());
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


                        extraReference.whereEqualTo("ProductID", snapshot.getString("ID")).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                int cumulativeSlots = 0;
                                if (task.isSuccessful()) {
                                    for (DocumentSnapshot document : task.getResult()) {
                                        cumulativeSlots += document.getLong("Quantity");
                                    }

                                    long productSlots = snapshot.getLong("Slots");

                                    if (productSlots == 0) {
                                        extraReference.document(String.valueOf(entry.getValue())).delete();;
                                    } else if (productSlots < cumulativeSlots) {
                                        extraReference.document(String.valueOf(entry.getValue())).update("Quantity", snapshot.getLong("Slots"));
                                    }
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

    private void getTotal() {
        overTotalPrice = 0;
        overTotalProductPrice = 0;
        overTotalExtraPrice = 0;

        if (productPriceQueryFinish) {
            productPriceQueryFinish = false;
            listReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    for (DocumentSnapshot document : task.getResult()) {
                        listReference.document(document.getId()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                                @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Log.w(TAG, "Listen failed.", e);
                                    return;
                                }

                                if (snapshot != null && snapshot.exists()) {
                                    Log.d(TAG, "Current data: " + snapshot.getData());
                                    long productTotal = snapshot.getLong("ProductPrice") * document.getLong("Quantity");
                                    overTotalProductPrice += productTotal;
                                    Log.d(TAG, String.valueOf(productTotal));
                                } else {
                                    Log.d(TAG, "Current data: null");
                                }
                            }
                        });
                    }
                    extraReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            for (DocumentSnapshot document : task.getResult()) {
                                overTotalExtraPrice += document.getLong("ProductPrice") * document.getLong("Quantity");
                                Log.d("overTotalExtra", String.valueOf(overTotalExtraPrice));
                            }
                            overTotalPrice = overTotalProductPrice + overTotalExtraPrice;
                            subtotal.setText("Subtotal: ₱" + overTotalPrice);
                        }
                    });
                    productPriceQueryFinish = true;
                }
            });
        }
    }
}
