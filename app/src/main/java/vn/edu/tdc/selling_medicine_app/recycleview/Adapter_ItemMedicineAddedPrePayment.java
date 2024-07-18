package vn.edu.tdc.selling_medicine_app.recycleview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.tdc.selling_medicine_app.R;
import vn.edu.tdc.selling_medicine_app.feature.CustomToast;
import vn.edu.tdc.selling_medicine_app.model.MyBill;
import vn.edu.tdc.selling_medicine_app.model.Product;

public class Adapter_ItemMedicineAddedPrePayment extends RecyclerView.Adapter<ItemMedicineAddedPrePaymentViewHolder> {

    private List<Product> productList;
    private Context context;

    public Adapter_ItemMedicineAddedPrePayment(List<Product> productList, Context context) {
        this.productList = productList;
        this.context = context;
    }
    @NonNull
    @Override
    public ItemMedicineAddedPrePaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_item_drug_added_invoice, parent, false);
        return new ItemMedicineAddedPrePaymentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemMedicineAddedPrePaymentViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);

        holder.itemView.setOnClickListener(view -> {
            showDeleteConfirmationDialog(position);
        });

        if (productList.size() > 1 && position < productList.size() - 1) {
            holder.viewLine.setVisibility(View.VISIBLE);
        } else {
            holder.viewLine.setVisibility(View.GONE);
        }

    }

    private void showDeleteConfirmationDialog(int position) {
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle("Thông báo")
                .setMessage("Bạn có chắc chắn muốn xóa thuốc này khỏi đơn?")
                .setPositiveButton("Có", (dialog, which) -> {
                    productList.remove(position);
                    notifyItemRemoved(position);
                    CustomToast.showToastSuccessful(context, "Xóa thuốc khỏi đơn thành công");
                })
                .setNegativeButton("Không", (dialog, which) -> {
                    notifyDataSetChanged();
                })
                .setIcon(R.drawable.ic_question)
                .create();

        alertDialog.show();
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

}