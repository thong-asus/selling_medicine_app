package vn.edu.tdc.selling_medicine_app.recycleview;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.tdc.selling_medicine_app.R;

public class ItemInvoiceViewHolder extends RecyclerView.ViewHolder {
    ImageView ivMedicine;
    LinearLayout linear_item_invoice;
    TextView customer_name, customer_mobile, total_cash, dateCreated;
    public ItemInvoiceViewHolder(@NonNull View itemView) {
        super(itemView);
        ivMedicine = itemView.findViewById(R.id.ivMedicine);
        linear_item_invoice = itemView.findViewById(R.id.linear_item_invoice);
        customer_name = itemView.findViewById(R.id.customer_name);
        customer_mobile = itemView.findViewById(R.id.customer_mobile);
        total_cash = itemView.findViewById(R.id.total_cash);
        dateCreated = itemView.findViewById(R.id.dateCreated);
    }
}