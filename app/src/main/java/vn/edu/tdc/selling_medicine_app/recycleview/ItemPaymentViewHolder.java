package vn.edu.tdc.selling_medicine_app.recycleview;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.tdc.selling_medicine_app.R;

public class ItemPaymentViewHolder extends RecyclerView.ViewHolder{
    TextView drugNameAdded, qtyDrugAdded,idDrug;
    View viewLine;
    public ItemPaymentViewHolder(@NonNull View itemView) {
        super(itemView);
        idDrug = itemView.findViewById(R.id.idDrug);
        drugNameAdded = itemView.findViewById(R.id.drugNameAdded);
        qtyDrugAdded = itemView.findViewById(R.id.qtyDrugAdded);
        viewLine = itemView.findViewById(R.id.viewLine);

    }
}
