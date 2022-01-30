package com.example.midnight_chevves.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.midnight_chevves.Interface.ItemClickListner;
import com.example.midnight_chevves.R;

public class ExtraViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView txtProductName,txtProductPrice,txtProductQuantity;
    private ItemClickListner itemClickListner;

    public ExtraViewHolder(View itemView) {
        super(itemView);
        txtProductName = itemView.findViewById(R.id.extra_product_name);
        txtProductPrice = itemView.findViewById(R.id.extra_product_price);
        txtProductQuantity = itemView.findViewById(R.id.extra_product_quantity);
    }

    @Override
    public void onClick(View view) {
        itemClickListner.onClick(view,getAdapterPosition(),false);
    }

    public void setItemClickListner(ItemClickListner itemClickListner) {
        this.itemClickListner = itemClickListner;
    }
}
