package vn.edu.tdc.selling_medicine_app.recycleview;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.List;

import vn.edu.tdc.selling_medicine_app.DetailCustomerActivity;
import vn.edu.tdc.selling_medicine_app.R;
import vn.edu.tdc.selling_medicine_app.feature.CustomToast;
import vn.edu.tdc.selling_medicine_app.model.Customer;
import vn.edu.tdc.selling_medicine_app.model.User;

public class ItemCustomerAdapter extends RecyclerView.Adapter<ItemCustomerViewHolder> {
    private List<Customer> customerList;
    private Context context;
    private RecyclerView recyclerView;

    private  User user = new User();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;

    public ItemCustomerAdapter(List<Customer> customerList, Context context) {
        this.customerList = customerList;
        this.context = context;

        SharedPreferences sharedPreferences = context.getSharedPreferences("informationUser", Context.MODE_PRIVATE);
        if (sharedPreferences.contains("informationUser")) {
            String jsonUser = sharedPreferences.getString("informationUser", "");
            Gson gson = new Gson();
            user = gson.fromJson(jsonUser, User.class);
        } else {
            // Dữ liệu không tồn tại, có thể là người dùng đã đăng xuất hoặc lần đầu sử dụng ứng dụng
        }
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

//        if (customerList.isEmpty()) {
//            holder.tvNoAvailableCustomer.setVisibility(View.VISIBLE);
//        } else {
//            holder.tvNoAvailableCustomer.setVisibility(View.GONE);
//        }
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

            AlertDialog alertDialog = new AlertDialog.Builder(context)
                    .setTitle("Thông báo")
                    .setMessage("Bạn có chắc chắn muốn xóa khách hàng này chứ?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Customers/"+user.getMobileNumber());
                        databaseReference.child(customer.getCustomerMobileNum()).removeValue();
                        customerList.remove(position);
                        notifyItemRemoved(position);
                        notifyDataSetChanged();
                       CustomToast.showToastSuccessful(context,"Xóa khách hàng thành công");
                    })
                    .setNegativeButton("Không", (dialog, which) -> {
                        notifyDataSetChanged();
                    })
                    .setIcon(R.drawable.ic_question)
                    .create();

            alertDialog.setOnDismissListener(dialog -> resetSwipe(position));
            alertDialog.show();
        }
    }
    public void resetSwipe(int position) {
        if (recyclerView != null) {
            recyclerView.post(() -> notifyItemChanged(position));
        } else {
            notifyItemChanged(position);
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
