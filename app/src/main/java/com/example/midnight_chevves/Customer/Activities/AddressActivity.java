package com.example.midnight_chevves.Customer.Activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class AddressActivity extends AppCompatActivity implements OnMapReadyCallback {
    ImageButton btnBack, btnLocation;
    Button btnSave, btnResi, btnOffi;
    FusedLocationProviderClient client;
    SupportMapFragment supportMapFragment;
    GoogleMap gMap;
    TextInputEditText addressInput;
    AlertDialog dialog;
    AlertDialog.Builder dialogBuilder;
    TextView addressInside;
    String addressString, addressType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);
        addressInput = findViewById(R.id.address_text);
        btnBack = findViewById(R.id.address_back);
        btnLocation = findViewById(R.id.address_location);
        btnSave = findViewById(R.id.button_save_changes2);
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);

        client = LocationServices.getFusedLocationProviderClient(this);

        supportMapFragment.getMapAsync(this);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(AddressActivity.this
                        , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    ActivityCompat.requestPermissions(AddressActivity.this
                            , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);

                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addressString == null){
                    Toast.makeText(AddressActivity.this, "Address is Blank", Toast.LENGTH_SHORT).show();
                } else {
                    popup();
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

        addressInside = (TextView) contactPopupView.findViewById(R.id.textAddressForm);
        addressInside.setText(addressString);
        btnOffi = (Button) contactPopupView.findViewById(R.id.button_residential);
        btnResi = (Button) contactPopupView.findViewById(R.id.button_office);

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


        dialogBuilder.setView(contactPopupView);
        dialog = dialogBuilder.create();
        dialog.show();
    }
}
