package vn.edu.tdc.selling_medicine_app.recycleview;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import vn.edu.tdc.selling_medicine_app.DetailCustomerActivity;
import vn.edu.tdc.selling_medicine_app.HomeActivity;
import vn.edu.tdc.selling_medicine_app.R;
import vn.edu.tdc.selling_medicine_app.model.Customer;

public class ItemCustomerAdapter extends RecyclerView.Adapter<ItemCustomerViewHolder> {
    private List<Customer> customerList;
    private Context context;

    FirebaseDatabase firebaseDatabase =  FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;
    public ItemCustomerAdapter(List<Customer> customerList, Context context) {
        this.customerList = customerList;
        this.context = context;
    }

    @NonNull
    @Override
    public ItemCustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.custom_item_customer, parent, false);
        return new ItemCustomerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemCustomerViewHolder holder, int position) {
        Customer customer = customerList.get(position);
        holder.fullname.setText(customer.getCustomerName());
        holder.mobileNumber.setText(customer.getCustomerMobileNum());
        holder.qtyBought.setText(String.valueOf(customer.getQtyBought()));
        holder.totalCash.setText(String.valueOf(customer.getTotalCash()) + " VND");
        holder.bind(customer);


        holder.itemView.setBackgroundResource(R.drawable.bg_item1);

        //Chuyển sang màn hình Chi tiết khách hàng
        final int finalPosition = position;
        holder.linear_item_customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailCustomerActivity.class);
                Customer customer1 = customerList.get(finalPosition);
                intent.putExtra("customerInfo", customer1);
                context.startActivity(intent);
            }
        });

//        if (position % 2 == 0) {
//            holder.itemView.setBackgroundResource(R.drawable.bg_item1);
//        } else {
//            holder.itemView.setBackgroundResource(R.drawable.bg_item2);
//        }
    }
    public void deleteACustomer(int position) {
        if (customerList.size() > 0) {
            Customer customer = customerList.get(position);

            new AlertDialog.Builder(context)
                    .setTitle("Thông báo")
                    .setMessage("Bạn có chắc chắn muốn xóa khách hàng này chứ?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Customers");
                        databaseReference.child(customer.getCustomerMobileNum()).removeValue();
                        customerList.remove(position);
                        notifyItemRemoved(position);
                        notifyDataSetChanged();
                    })
                    .setNegativeButton("Không", (dialog, which) -> {
                        notifyItemChanged(position);
                    })
                    .setIcon(R.drawable.ic_question)
                    .show();
        }
    }

    public void updateData(List<Customer> newData) {
        customerList.clear();
        customerList.addAll(newData);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }
}
