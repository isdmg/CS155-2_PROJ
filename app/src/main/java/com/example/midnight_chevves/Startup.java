package com.example.midnight_chevves;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.midnight_chevves.Model.Products;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Startup extends Application {

    private CollectionReference collectionReference;

    @Override
    public void onCreate() {
        super.onCreate();
        collectionReference = FirebaseFirestore.getInstance().collection("Products");
        checkNeededRefill();
    }

    private void checkNeededRefill() {
//        ProductsRef
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.exists()) {
//                            for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
//                                Products products = productSnapshot.getValue(Products.class);
//                                Calendar cal = Calendar.getInstance();
//                                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
//
//                                try {
//                                    int weeks = calculateWeeks(convertToDate(products.getRDate()), cal);
//                                    if (weeks > 0) {
//                                        int refill = 4 * weeks;
//                                        Log.d("pid", products.getPid());
//                                        ProductsRef.child(products.getPid()).child("slots").setValue(products.getSlots() + refill);
//                                        ProductsRef.child(products.getPid()).child("rdate").setValue(sdf.format(cal.getTime()));
//                                    }
//                                } catch (ParseException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                    }
//                });

        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        Calendar cal = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

                        try {
                            int weeks = calculateWeeks(convertToDate(document.getString("RDate")), cal);
                            if (weeks > 0) {
                                int refill = 4 * weeks;
                                collectionReference.document(document.getId()).update("Slots", document.getLong("Slots") + refill);
                                collectionReference.document(document.getId()).update("RDate", sdf.format(cal.getTime()));
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public static Calendar convertToDate(String database_date) throws ParseException {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date date = sdf.parse(database_date);
        cal.setTime(date);
        return cal;
    }

    public static int calculateWeeks(Calendar lastrcal, Calendar thiscal) throws ParseException {

        Date lastdate = lastrcal.getTime();
        Date thisdate = thiscal.getTime();
        long diffInMillies = Math.abs(lastdate.getTime() - thisdate.getTime());
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        int weeks = 0;

        while (diff >= 0) {
            if (diff >= 7 && lastrcal.before(thiscal)) {
                if (lastrcal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                    diff = diff - 7;
                    weeks++;
                    lastrcal.add(Calendar.DATE, 7);
                } else if (lastrcal.get(Calendar.DAY_OF_WEEK) < Calendar.MONDAY && diff > 0) {
                    diff = diff - 7;
                    weeks++;
                    lastrcal.add(Calendar.DATE, 7);
                } else {
                    lastrcal.add(Calendar.DATE, 1);
                    diff = diff - 1;
                }
            } else if (lastrcal.before(thiscal)) {
                if (lastrcal.get(Calendar.DAY_OF_WEEK) < Calendar.MONDAY && diff > 0) {
                    diff = diff - 7;
                    weeks++;
                    lastrcal.add(Calendar.DATE, 7);
                } else {
                    lastrcal.add(Calendar.DATE, 1);
                    diff = diff - 1;
                }
            } else {
                diff = -1;
            }
        }
        return weeks;
    }
}
