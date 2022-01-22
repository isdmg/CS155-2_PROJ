package com.example.midnight_chevves.Customer.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.lang.reflect.Array;
import java.util.Properties;

import com.example.midnight_chevves.Admin.AdminActivity;
import com.example.midnight_chevves.Interface.JavaMailAPI;
import com.example.midnight_chevves.LoginActivity;
import com.example.midnight_chevves.R;
import com.google.firebase.auth.FirebaseAuth;

public class PaymentFormEmail extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public EditText txtName, txtMobileNo, txtLocation;
    Spinner paymentmethod;
    Button SendEmail_btn;

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
}