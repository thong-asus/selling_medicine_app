package vn.edu.tdc.selling_medicine_app.recycleview;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.tdc.selling_medicine_app.R;
import vn.edu.tdc.selling_medicine_app.model.Customer;

public class ItemCustomerViewHolder extends RecyclerView.ViewHolder {
    TextView fullname, mobileNumber, qtyBought, totalCash, tvNoAvailableCustomer;
    LinearLayout linear_item_customer;
    public ItemCustomerViewHolder(@NonNull View itemView) {
        super(itemView);
        fullname = itemView.findViewById(R.id.fullname);
        mobileNumber = itemView.findViewById(R.id.mobileNumber);
        qtyBought = itemView.findViewById(R.id.qtyBought);
        totalCash = itemView.findViewById(R.id.totalCash);
        linear_item_customer = itemView.findViewById(R.id.linear_item_customer);
        tvNoAvailableCustomer = itemView.findViewById(R.id.tvNoAvailableCustomer);
    }
    public void bind(Customer customer) {
        fullname.setText(customer.getCustomerName());
        mobileNumber.setText(customer.getCustomerMobileNum());
    }
}
