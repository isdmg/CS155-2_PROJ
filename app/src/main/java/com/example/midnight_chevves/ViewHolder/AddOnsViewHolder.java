package com.example.midnight_chevves.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.midnight_chevves.Interface.ItemClickListner;
import com.example.midnight_chevves.R;

public class AddOnsViewHolder extends RecyclerView.ViewHolder {
    public LinearLayout linearLayout; // TODO: Change variable name?
    public TextView txtProductName, txtProductPrice;
    public ImageView imageView;
    public ElegantNumberButton btnQuantity;


    public AddOnsViewHolder(@NonNull View itemView) {
        super(itemView);
        linearLayout = (LinearLayout) itemView.findViewById(R.id.add_ons_product_frame);
        imageView = (ImageView) itemView.findViewById(R.id.add_ons_product_image);
        txtProductName = (TextView) itemView.findViewById(R.id.add_ons_product_name);
        txtProductPrice = (TextView) itemView.findViewById(R.id.add_ons_product_price);
        btnQuantity = itemView.findViewById(R.id.add_ons_button_quantity);
    }
}
