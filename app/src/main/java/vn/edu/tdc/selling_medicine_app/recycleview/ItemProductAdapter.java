package vn.edu.tdc.selling_medicine_app.recycleview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import vn.edu.tdc.selling_medicine_app.R;
import vn.edu.tdc.selling_medicine_app.model.Customer;
import vn.edu.tdc.selling_medicine_app.model.Product;

public class ItemProductAdapter extends RecyclerView.Adapter<ItemProductViewHolder>{
    private List<Product> productList;
    private Context context;

    public ItemProductAdapter(List<Product> productList, Context context) {
        this.productList = productList;
        this.context = context;
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

    }
    public void deleteAProduct(int position) {
        if (productList.size() > 0) {
            Product product = productList.get(position);
            new AlertDialog.Builder(context)
                    .setTitle("Thông báo")
                    .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này chứ?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Drugs");
                        databaseReference.child(product.getIdDrug()).removeValue();
                        productList.remove(position);
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
