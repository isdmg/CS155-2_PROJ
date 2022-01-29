package com.example.midnight_chevves.Customer.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.midnight_chevves.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class AddressActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ImageButton btnBack, btnLocation;
    private Button btnSave, btnResi, btnOffi, btnSaveForm;
    private FusedLocationProviderClient client;
    private SupportMapFragment supportMapFragment;
    private GoogleMap gMap;
    private TextInputEditText addressInput, addressTextForm;
    private AlertDialog dialog;
    private AlertDialog.Builder dialogBuilder;
    private TextView addressInside;
    private String addressString, addressType;
    private FirebaseAuth auth;
    private FirebaseFirestore store;
    HashMap<String, Object> addressInfo = new HashMap<>();


    private TextInputLayout textInputLayout, textInputLayoutForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        auth = FirebaseAuth.getInstance();
        store = FirebaseFirestore.getInstance();
        addressInput = findViewById(R.id.g_address);
        btnBack = findViewById(R.id.address_back);
        btnSave = findViewById(R.id.button_save_location);
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);

        client = LocationServices.getFusedLocationProviderClient(this);

        supportMapFragment.getMapAsync(this);

        ActivityCompat.requestPermissions(AddressActivity.this
                , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
//        btnLocation.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addressString == null){
                    Toast.makeText(AddressActivity.this, "Address is empty!", Toast.LENGTH_SHORT).show();
                } else {
                    popup();
                }
            }
        });

        textInputLayout = findViewById(R.id.layout_g_address);
        textInputLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(AddressActivity.this
                        , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                }
            }
        });

    }

    private void getLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {

                            LatLng latLng = new LatLng (location.getLatitude(),
                                    location.getLongitude());
                            MarkerOptions options = new MarkerOptions().position(latLng).title("My Address");
                            gMap.clear();
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                            googleMap.addMarker(options);

                        }
                    });
                }
            }
        });
        client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    try {
                        Geocoder geocoder = new Geocoder(AddressActivity.this,
                                Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 4);
                        addressInput.setText(addresses.get(0).getAddressLine(0));
                        addressString = addresses.get(0).getAddressLine(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //Marker
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("My Address");
                gMap.clear();
                gMap.addMarker(markerOptions);

                Geocoder geocoder = new Geocoder(AddressActivity.this,
                        Locale.getDefault());
                double lat = latLng.latitude;
                double lng = latLng.longitude;
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(
                            lat, lng, 4);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                addressInput.setText(addresses.get(0).getAddressLine(0));
                addressString = addresses.get(0).getAddressLine(0);

            }
        });
    }
    public void popup(){
        dialogBuilder = new AlertDialog.Builder(this);
        final View contactPopupView = getLayoutInflater().inflate(R.layout.activity_address_form, null);

        addressInside = contactPopupView.findViewById(R.id.textAddressForm);
        addressTextForm = contactPopupView.findViewById(R.id.address_text_form);
        btnOffi = contactPopupView.findViewById(R.id.button_office);
        btnResi = contactPopupView.findViewById(R.id.button_residential);
        btnSaveForm = contactPopupView.findViewById(R.id.button_save_form);
        textInputLayoutForm = contactPopupView.findViewById(R.id.layout_address_form);

        dialogBuilder.setView(contactPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        btnOffi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnResi.setBackgroundColor(Color.parseColor("#ffffff"));
                btnOffi.setBackgroundColor(Color.parseColor("#fff766"));
                addressType = "Office";
            }
        });
        btnResi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnResi.setBackgroundColor(Color.parseColor("#fff766"));
                btnOffi.setBackgroundColor(Color.parseColor("#ffffff"));
                addressType = "Residential";
            }
        });
        addressTextForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textInputLayoutForm.setError(null);
            }
        });
        btnSaveForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textInputLayoutForm.setError(null);
                if ((addressType == null) && (TextUtils.isEmpty(addressTextForm.getText().toString()))){
                    Toast.makeText(AddressActivity.this, "Please select an Address Type", Toast.LENGTH_SHORT).show();
                    textInputLayoutForm.setError("Please Fill Up Address Details");
                } else if (TextUtils.isEmpty(addressTextForm.getText().toString())){
                    textInputLayoutForm.setError("Please Fill Up Address Details");
                } else if (addressType == null){
                    Toast.makeText(AddressActivity.this, "Please select an Address Type", Toast.LENGTH_SHORT).show();
                } else{
                    dialog.dismiss();
                    addressInfo.put("MapAddress", addressString);
                    addressInfo.put("AddressDetails", addressTextForm.getText().toString());
                    addressInfo.put("AddressType", addressType);
                    addressInfo.put("userRef", auth.getUid());
                    writeDocument();
                }

            }
        });

        addressInside.setText(addressString);
    }

    private void writeDocument() {
        store.collection("Addresses").document(auth.getUid()).set(addressInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(AddressActivity.this, "Address has been saved!", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        });
    }
}
