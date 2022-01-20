package com.example.midnight_chevves.ViewHolder;



import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.midnight_chevves.Interface.ItemClickListner;
import com.example.midnight_chevves.R;


public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView txtOrderId,txtProductStatus,txtProductDate;
    private ItemClickListner itemClickListner;
    public ImageView orderDetail;

    public OrderViewHolder(View itemView) {
        super(itemView);
        txtOrderId = itemView.findViewById(R.id.order_id);
        txtProductStatus = itemView.findViewById(R.id.order_status);
        txtProductDate = itemView.findViewById(R.id.order_date);
        orderDetail = itemView.findViewById(R.id.order_detail);
    }

    @Override
    public void onClick(View view) {
        itemClickListner.onClick(view,getAdapterPosition(),false);
    }

    public void setItemClickListner(ItemClickListner itemClickListner) {
        this.itemClickListner = itemClickListner;
    }
}


