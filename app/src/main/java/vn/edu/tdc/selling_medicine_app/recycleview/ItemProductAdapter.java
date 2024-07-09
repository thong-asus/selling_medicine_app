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

import vn.edu.tdc.selling_medicine_app.DetailProductActivity;
import vn.edu.tdc.selling_medicine_app.R;
import vn.edu.tdc.selling_medicine_app.feature.CustomToast;
import vn.edu.tdc.selling_medicine_app.model.Product;
import vn.edu.tdc.selling_medicine_app.model.User;

public class ItemProductAdapter extends RecyclerView.Adapter<ItemProductViewHolder> {
    private List<Product> productList;
    private Context context;
    private RecyclerView recyclerView;
    private  User user = new User();

    public ItemProductAdapter(List<Product> productList, Context context) {
        this.productList = productList;
        this.context = context;
        ////////////////////nhận dữ liệu///////////////////////
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
    public ItemProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.custom_item_product, parent, false);
        return new ItemProductViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.drugName.setText(product.getDrugName());
        holder.indications.setText(product.getIndications());
        holder.expiryDate.setText(product.getExpiryDate());
        holder.itemView.setBackgroundResource(R.drawable.bg_item1);

//        if (productList.isEmpty()) {
//            holder.tvNoAvailableProduct.setVisibility(View.VISIBLE);
//        } else {
//            holder.tvNoAvailableProduct.setVisibility(View.GONE);
//        }

        final int finalPosition = position;
        holder.linear_item_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Product product1 = productList.get(finalPosition);
                Intent intent = new Intent(context, DetailProductActivity.class);

                intent.putExtra("idDrug", product1.getIdDrug());
                intent.putExtra("productInfo", product1);
                context.startActivity(intent);
            }
        });
    }

public void deleteAProduct(int position) {
    if (productList.size() > 0) {
        Product product = productList.get(position);

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle("Thông báo")
                .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này chứ?")
                .setPositiveButton("Có", (dialog, which) -> {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Drugs/"+user.getMobileNumber());
                    databaseReference.child(product.getIdDrug()).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                productList.remove(position);
                                notifyItemRemoved(position);
                                notifyDataSetChanged();
                                CustomToast.showToastSuccessful(context,"Xóa sản phẩm thành công");
                            })
                            .addOnFailureListener(e -> {
                                CustomToast.showToastFailed(context,"Xóa sản phẩm thất bại");
                                //Toast.makeText(context, "Xóa sản phẩm thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
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

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    public void updateData(List<Product> newData) {
        productList.clear();
        productList.addAll(newData);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}
