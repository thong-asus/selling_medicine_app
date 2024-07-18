package vn.edu.tdc.selling_medicine_app.recycleview;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.tdc.selling_medicine_app.R;

public class ItemDetailInvoiceViewHolder extends RecyclerView.ViewHolder {
    TextView detailInvoiceDrugName, detailInvoiceDrugQty;
    View detailViewLine;

    public ItemDetailInvoiceViewHolder(@NonNull View itemView) {
        super(itemView);
        detailInvoiceDrugName = itemView.findViewById(R.id.detailInvoiceDrugName);
        detailInvoiceDrugQty = itemView.findViewById(R.id.detailInvoiceDrugQty);
        detailViewLine = itemView.findViewById(R.id.detailViewLine);
    }
}
