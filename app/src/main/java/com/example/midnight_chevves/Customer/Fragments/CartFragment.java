package com.example.midnight_chevves.Customer.Fragments;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midnight_chevves.Customer.Activities.PaymentFormEmail;
import com.example.midnight_chevves.Customer.Activities.ProductDetailsActivity;
import com.example.midnight_chevves.Customer.Activities.UserAddressActivity;
import com.example.midnight_chevves.Model.Cart;
import com.example.midnight_chevves.R;
import com.example.midnight_chevves.ViewHolder.CartViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CartFragment extends Fragment {

    private RecyclerView recyclerViewCart;

    private FirebaseAuth auth;
    private FirebaseFirestore store;
    private long overTotalPrice = 0;
    private long overTotalExtraPrice = 0;
    private long overTotalProductPrice = 0;
    private CollectionReference listReference, extraReference, productReference;

    private FirestoreRecyclerAdapter<Cart, CartViewHolder> adapter;

    private Button btnCheckout;
    private ImageView noItemsImage;
    private HashMap<String, Object> updateListInfo;
    private HashMap<String, Object> updateExtraInfo;
    private CardView cardview_cart_total, cart_items;
    private TextView grandTotal, subtotal, subtext1, subtext2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_customer_cart, container, false);

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
        productReference = store.collection("Products");
        listReference = store.collection("Carts").document(auth.getUid()).collection("List");
        extraReference = store.collection("Carts").document(auth.getUid()).collection("Extras");
        btnCheckout = v.findViewById(R.id.button_checkout);
        noItemsImage = v.findViewById(R.id.no_items_cart);
        cardview_cart_total = v.findViewById(R.id.cardview_cart_total);
        cart_items = v.findViewById(R.id.cardview_cart_products);

        subtext1 = v.findViewById(R.id.cart_subtext);
        subtext2 = v.findViewById(R.id.cart_subtext2);
        subtotal = (TextView) v.findViewById(R.id.cart_total_amount);
        grandTotal = v.findViewById(R.id.cart_delivery_and_total_fee);


        btnCheckout.setVisibility(View.GONE);

        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAddress();
            }
        });

        getCart();
        getProductTotal();
        getExtraTotal();

        return v;
    }


    public void onStart() {
        super.onStart();
        FirestoreRecyclerOptions<Cart> options =
                new FirestoreRecyclerOptions.Builder<Cart>()
                        .setQuery(listReference.orderBy("ProductName", Query.Direction.ASCENDING), Cart.class)
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
                        if (getItemCount() == 0) {
                            btnCheckout.setVisibility(View.GONE);
                            cardview_cart_total.setVisibility(View.GONE);
                            cart_items.setVisibility(View.GONE);
                            noItemsImage.setVisibility(View.VISIBLE);

                            subtext1.setVisibility(View.GONE);
                            subtext2.setVisibility(View.VISIBLE);
                        } else {
                            btnCheckout.setVisibility(View.VISIBLE);
                            cardview_cart_total.setVisibility(View.VISIBLE);
                            cart_items.setVisibility(View.VISIBLE);
                            noItemsImage.setVisibility(View.GONE);

                            subtext1.setVisibility(View.VISIBLE);
                            subtext2.setVisibility(View.GONE);
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
                                        updateExtraInfo.put(document.getString("ProductID"), document.getString("ExtraID"));
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
                                            listReference.document(model.getListID()).delete();

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
        Intent intent = new Intent(getActivity(), PaymentFormEmail.class);
        intent.putExtra("totalAmount", Long.parseLong(grandTotal.getText().toString().substring(14)));
        startActivity(intent);
    }

    private void checkAddress() {
        store.collection("Addresses").document(auth.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                checkout();
                            } else {
                                Intent intent = new Intent(getActivity(), UserAddressActivity.class);
                                startActivity(intent);
                            }
                        }
                    }
                });
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
                                        extraReference.document(String.valueOf(entry.getValue())).delete();
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

    private void displayTotal() {
        long userCartTotal = overTotalProductPrice + overTotalExtraPrice;
        subtotal.setText("Subtotal: ₱" + String.valueOf(userCartTotal));

        long userCartTotalWithDeliveryFee = userCartTotal + 100;
        grandTotal.setText("Grand Total: ₱" + userCartTotalWithDeliveryFee);
    }

    private void getCart() {
        listReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "listen:error", e);
                    return;
                }

                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED || dc.getType() == DocumentChange.Type.MODIFIED || dc.getType() == DocumentChange.Type.REMOVED) {
                        Log.d("b4Reset", String.valueOf(overTotalProductPrice));
                        Log.d("a4Reset", String.valueOf(overTotalProductPrice));
                        overTotalProductPrice = 0;
                    }
                }
            }
        });

        extraReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "listen:error", e);
                    return;
                }

                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED || dc.getType() == DocumentChange.Type.MODIFIED || dc.getType() == DocumentChange.Type.REMOVED) {
                        overTotalExtraPrice = 0;
                    }
                }
            }
        });
    }

    private void getProductTotal() {
        listReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                for (QueryDocumentSnapshot doc : value) {
                    Log.d("sizeValue", String.valueOf(value.size()));
                    long productPrice = doc.getLong("ProductPrice") * doc.getLong("Quantity");
                    Log.d("b-Over", String.valueOf(overTotalProductPrice));
                    overTotalProductPrice += productPrice;
                    Log.d("pPrice", String.valueOf(doc.getLong("ProductPrice")));
                    Log.d("pQuant", String.valueOf(doc.getLong("Quantity")));
                    Log.d("pCombi", String.valueOf(productPrice));
                    Log.d("a-Over", String.valueOf(overTotalProductPrice));
                }
                displayTotal();
            }
        });
    }

    private void getExtraTotal() {
        extraReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                for (QueryDocumentSnapshot doc : value) {
                    long productPrice = doc.getLong("ProductPrice") * doc.getLong("Quantity");
                    overTotalExtraPrice += productPrice;
                }
                displayTotal();
            }
        });
    }
}
