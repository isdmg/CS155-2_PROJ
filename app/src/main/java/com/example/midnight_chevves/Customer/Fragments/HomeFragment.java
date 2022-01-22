package com.example.midnight_chevves.Customer.Fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.models.SlideModel;
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

import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.paulrybitskyi.persistentsearchview.PersistentSearchView;
import com.paulrybitskyi.persistentsearchview.listeners.OnSearchConfirmedListener;
import com.paulrybitskyi.persistentsearchview.listeners.OnSearchQueryChangeListener;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView txtCake, txtBox, txtWine;
    private RecyclerView recyclerView1, recyclerView2, recyclerView3;
    private FirebaseFirestore store;
    private StorageReference storageReference;
    private CollectionReference collectionReference;
    private ImageSlider imageSlider;
    private PersistentSearchView persistentSearchView;
    private String searchInput = "";
    private ScrollView scrollView;




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


        //image gallery banner segment
        imageSlider = v.findViewById(R.id.slider);
        ArrayList<SlideModel> images = new ArrayList<>();
        images.add(new SlideModel(R.drawable.temp_banner, null));
        images.add(new SlideModel("https://cdn.discordapp.com/attachments/856045907409764393/931438077820411924/B8005DAC-6D53-4164-8501-C17846067A0D-FA7D8431-40AC-425C-89CF-F2866EBB0931.JPG", null));
        images.add(new SlideModel("https://cdn.discordapp.com/attachments/856045907409764393/931438380938575882/IMG_6556.JPG", null));
        images.add(new SlideModel("https://cdn.discordapp.com/attachments/856045907409764393/931438478351282226/IMG_6601.jpg", null));


        imageSlider.setImageList(images);
        //


        scrollView = v.findViewById(R.id.scrollView_home);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @RequiresApi(api = Build.VERSION_CODES.P)
                @Override
                public void onScrollChange(View view, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if(scrollY + 75<=oldScrollY || scrollY == 0) {
//                        Log.d("scrollY", String.valueOf(scrollY));
//                        Log.d("oldScrollY", String.valueOf(oldScrollY));
                        persistentSearchView.setVisibility(View.VISIBLE);
                    } else if (scrollY > oldScrollY){
//                        Log.d("scrollY", String.valueOf(scrollY));
//                        Log.d("oldScrollY", String.valueOf(oldScrollY));
                        persistentSearchView.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }

        persistentSearchView = v.findViewById(R.id.persistentSearchView);

        persistentSearchView.setOnClearInputBtnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchInput = "";
                onStart();
            }
        });

        persistentSearchView.setOnLeftBtnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchInput = "";
                onStart();
            }
        });

        persistentSearchView.setOnSearchConfirmedListener(new OnSearchConfirmedListener() {
            @Override
            public void onSearchConfirmed(PersistentSearchView searchView, String query) {
                // Handle a search confirmation. This is the place where you'd
                // want to perform a search against your data provider.

                searchView.collapse();
                searchInput = query;
                Log.d("searchQuery", searchInput);
                onStart();
            }

        });


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
    }

    private FirestoreRecyclerAdapter<Products, ProductViewHolder> populateRecyclerView(String category) {
            FirestoreRecyclerOptions<Products> options;

            if (searchInput.isEmpty()) {
                options =
                        new FirestoreRecyclerOptions.Builder<Products>()
                                .setQuery(collectionReference.whereEqualTo("Category", category), Products.class)
                                .build();
            } else {
                options =
                        new FirestoreRecyclerOptions.Builder<Products>()
                                .setQuery(collectionReference.whereEqualTo("Category", category).orderBy("Name").startAt(searchInput).endAt(searchInput+ "\uf8ff"), Products.class)
                                .build();
            }


            FirestoreRecyclerAdapter<Products, ProductViewHolder> adapter =
                    new FirestoreRecyclerAdapter<Products, ProductViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull ProductViewHolder holder, int position, @NonNull final Products model) {
                            holder.txtProductName.setText(model.getName());
                            holder.txtProductDescription.setText(model.getDescription());
                            holder.txtProductPrice.setText("₱" + model.getPrice());
                            Log.d("ProductHolder", model.getName());
                            // Shows that no slots are left by changing the LinearLayout transparency to 25/100.
                            if (model.getSlots() == 0) {
                                holder.linearLayout.setAlpha(0.25f);
                            } else {
                                holder.linearLayout.setAlpha(1f);
                            }

                            storageReference.child("Product Images/" + model.getID() + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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
                                    Bundle bundle = new Bundle();
                                    bundle.putString("ProductID", model.getID());
                                    intent.putExtras(bundle);
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
    }
}
