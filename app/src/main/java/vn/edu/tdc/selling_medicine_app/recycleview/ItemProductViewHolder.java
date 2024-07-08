package vn.edu.tdc.selling_medicine_app.recycleview;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.tdc.selling_medicine_app.R;

public class ItemProductViewHolder extends RecyclerView.ViewHolder {
    LinearLayout linear_item_product;
    TextView drugName, indications, expiryDate;
    ImageView ivMedicine;
    public ItemProductViewHolder(@NonNull View itemView) {
        super(itemView);
        linear_item_product = itemView.findViewById(R.id.linear_item_product);
        drugName = itemView.findViewById(R.id.drugName);
        indications = itemView.findViewById(R.id.indications);
        expiryDate = itemView.findViewById(R.id.expiryDate);
        ivMedicine = itemView.findViewById(R.id.ivMedicine);
    }
}
