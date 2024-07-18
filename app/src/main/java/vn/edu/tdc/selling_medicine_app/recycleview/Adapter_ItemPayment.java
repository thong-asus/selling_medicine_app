package vn.edu.tdc.selling_medicine_app.recycleview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.tdc.selling_medicine_app.R;
import vn.edu.tdc.selling_medicine_app.model.MyBill;

public class Adapter_ItemPayment extends RecyclerView.Adapter<ItemPaymentViewHolder> {

    private List<MyBill.Item> itemList;
    private Context context;

    public Adapter_ItemPayment(List<MyBill.Item> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    @NonNull
    @Override
    public ItemPaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_item_drug_payment, parent, false);
        return new ItemPaymentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemPaymentViewHolder holder, int position) {
        MyBill.Item item = itemList.get(position);
        holder.idDrug.setText(item.getIdDrug());
        holder.drugNameAdded.setText(item.getDrugName());
        holder.qtyDrugAdded.setText(String.valueOf(item.getQtyDrug()));

        if (itemList.size() > 1 && position < itemList.size() - 1) {
            holder.viewLine.setVisibility(View.VISIBLE);
        } else {
            holder.viewLine.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
