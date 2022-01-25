package com.example.midnight_chevves.Customer.Activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.midnight_chevves.Interface.JavaMailAPI;
import com.example.midnight_chevves.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class PaymentFormEmail extends AppCompatActivity implements AdapterView.OnItemSelectedListener, OnMapReadyCallback {

    public EditText txtName, txtMobileNo, txtLocation;
    Spinner paymentmethod;
    Button SendEmail_btn, showMap;
    AlertDialog dialog;
    AlertDialog.Builder dialogBuilder;


    ImageButton btnBack, btnLocation;
    Button btnSave, btnResi, btnOffi;
    FusedLocationProviderClient client;
    SupportMapFragment supportMapFragment;
    GoogleMap gMap;
    TextInputEditText addressInput;
    TextView addressInside;
    String addressString, addressType;
    Boolean popup = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_form_email);

        txtName = (EditText)findViewById(R.id.editTextNAME_paymentform);
        txtMobileNo = (EditText)findViewById(R.id.editTextMobileNo_paymentform);
        txtLocation = (EditText)findViewById(R.id.editTextLocation_paymentform);

        paymentmethod = findViewById(R.id.orderform_paymentmethod);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.payment_method_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentmethod.setAdapter(adapter);
        paymentmethod.setOnItemSelectedListener(this);


        SendEmail_btn = findViewById(R.id.orderFormSubmit_btn);
        SendEmail_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMail();

            }
        });

        showMap = findViewById(R.id.button_show_map);

        /*supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map2);
        client = LocationServices.getFusedLocationProviderClient(this);
        supportMapFragment.getMapAsync(this);*/

        showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup();
            }
        });



    }
    private void popup() {
        dialogBuilder = new AlertDialog.Builder(this);
        final View contactPopupView = getLayoutInflater().inflate(R.layout.activity_payment_form_map, null);

        btnLocation = (ImageButton) contactPopupView.findViewById(R.id.address_location_popup);
        addressInput = (TextInputEditText) contactPopupView.findViewById(R.id.address_text_popup);

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map_popup);
        client = LocationServices.getFusedLocationProviderClient(this);
        supportMapFragment.getMapAsync(this);


        btnSave = (Button) contactPopupView.findViewById(R.id.button_save_changes2_popup);
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(PaymentFormEmail.this
                        , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    ActivityCompat.requestPermissions(PaymentFormEmail.this
                            , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);

                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtLocation.setText(addressInput.getText());
                dialog.dismiss();
            }
        });

        dialogBuilder.setView(contactPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

    }


    private void sendMail() {

        String mail = "cs155.midnight.payment@gmail.com";
        String subject = txtName.getText().toString().trim() + " Payment Form";
        String message =    "Customer Name: " + txtName.getText().toString() + "\n" +
                "Phone Number: " + txtMobileNo.getText().toString() + "\n" +


                //gerome pls add user's profile location here_ tnx
                "Shipping Address: " + txtLocation.getText().toString() + "\n" +
                "Order Number: " + "\n" +
                "Payment Method: " + paymentmethod.getSelectedItem().toString();;

        //Send Mail
        JavaMailAPI javaMailAPI = new JavaMailAPI(this,mail,subject,message);
        javaMailAPI.execute();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
        String PaymentMethodString = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(),PaymentMethodString, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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
                        Geocoder geocoder = new Geocoder(PaymentFormEmail.this,
                                Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 4);
                        txtLocation.setText(addresses.get(0).getAddressLine(0));
                        addressInput.setText(addresses.get(0).getAddressLine(0));
                        addressString = addresses.get(0).getAddressLine(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


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

                Geocoder geocoder = new Geocoder(PaymentFormEmail.this,
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
                txtLocation.setText(addresses.get(0).getAddressLine(0));
                addressInput.setText(addresses.get(0).getAddressLine(0));
                addressString = addresses.get(0).getAddressLine(0);

            }
        });
    }


}