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

public class Adapter_ItemDetailInvoice extends RecyclerView.Adapter<ItemDetailInvoiceViewHolder> {

    private List<MyBill.Item> itemList;
    private Context context;

    public Adapter_ItemDetailInvoice(List<MyBill.Item> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    @NonNull
    @Override
    public ItemDetailInvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_item_drug_detail_invoice, parent, false);
        return new ItemDetailInvoiceViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemDetailInvoiceViewHolder holder, int position) {
        MyBill.Item item = itemList.get(position);
        holder.detailInvoiceDrugName.setText(item.getDrugName());
        holder.detailInvoiceDrugQty.setText(String.valueOf(item.getQtyDrug()));

        if (itemList.size() > 1 && position < itemList.size() - 1) {
            holder.detailViewLine.setVisibility(View.VISIBLE);
        } else {
            holder.detailViewLine.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
