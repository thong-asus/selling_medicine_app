package vn.edu.tdc.selling_medicine_app.recycleview;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import vn.edu.tdc.selling_medicine_app.R;
import vn.edu.tdc.selling_medicine_app.model.Product;

public class ItemMedicineAddedPrePaymentViewHolder extends RecyclerView.ViewHolder {
    TextView drugNameAdded, qtyDrugAdded, idDrug;
    LinearLayout linear_item_medicine_added;
    View viewLine;
    public ItemMedicineAddedPrePaymentViewHolder(@NonNull View itemView) {
        super(itemView);
        drugNameAdded = itemView.findViewById(R.id.drugNameAdded);
        qtyDrugAdded = itemView.findViewById(R.id.qtyDrugAdded);
        idDrug = itemView.findViewById(R.id.idDrug);
        linear_item_medicine_added = itemView.findViewById(R.id.linear_item_medicine_added);
        viewLine = itemView.findViewById(R.id.viewLine);
    }
    public void bind(Product product) {
        idDrug.setText(product.getIdDrug());
        drugNameAdded.setText(product.getDrugName());
        qtyDrugAdded.setText(String.valueOf(product.getQtySelling()));
    }
}
