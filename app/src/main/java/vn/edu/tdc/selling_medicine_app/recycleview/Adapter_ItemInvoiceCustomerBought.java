package vn.edu.tdc.selling_medicine_app.recycleview;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import vn.edu.tdc.selling_medicine_app.DetailHistorySalesActivity;
import vn.edu.tdc.selling_medicine_app.R;
import vn.edu.tdc.selling_medicine_app.feature.CustomToast;
import vn.edu.tdc.selling_medicine_app.feature.FormatNumber;
import vn.edu.tdc.selling_medicine_app.feature.ReceiveUserInfo;
import vn.edu.tdc.selling_medicine_app.model.Customer;
import vn.edu.tdc.selling_medicine_app.model.MyBill;
import vn.edu.tdc.selling_medicine_app.model.User;

public class Adapter_ItemInvoiceCustomerBought extends RecyclerView.Adapter<ItemInvoiceCustomerBoughtViewHolder>{
    private Context context;
    private ArrayList<MyBill> invoiceList;
    private DatabaseReference databaseReference;
    private User user = new User();
    private RecyclerView recyclerView;

    public Adapter_ItemInvoiceCustomerBought(ArrayList<MyBill> invoiceList, Context context) {
        this.context = context;
        this.invoiceList = invoiceList;
        this.user = ReceiveUserInfo.getUserInfo(context);
    }
    public void setInvoiceList(ArrayList<MyBill> invoiceList) {
        this.invoiceList = invoiceList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemInvoiceCustomerBoughtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.custom_item_invoice, parent, false);
        return new ItemInvoiceCustomerBoughtViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemInvoiceCustomerBoughtViewHolder holder, int position) {
        MyBill invoice = invoiceList.get(position);

        holder.customer_name.setText("");
        holder.customer_mobile.setText("");
        holder.total_cash.setText("");
        holder.dateCreated.setText("");

        holder.customer_mobile.setText(invoice.getCustomerMobileNum());
        holder.customer_name.setText(invoice.getCustomerName());
        holder.total_cash.setText(FormatNumber.formatNumber(invoice.getTotalCash()) + " VND");
        holder.dateCreated.setText(invoice.getDateCreated());

        holder.itemView.setBackgroundResource(R.drawable.bg_item1);
        holder.linear_item_invoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailHistorySalesActivity.class);
                intent.putExtra("invoiceInfo", invoice);
                context.startActivity(intent);
            }
        });
    }
    public void deleteAInvoiceOfCustomer(int position) {
        if (invoiceList.size() > 0) {
            MyBill invoice = invoiceList.get(position);

            AlertDialog alertDialog = new AlertDialog.Builder(context)
                    .setTitle("Thông báo")
                    .setMessage("Bạn có chắc chắn muốn xóa hóa đơn này chứ?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Invoices/" + user.getMobileNumber() + "/" + invoice.getCustomerMobileNum() + "/" + invoice.getInvoiceID());
                        databaseReference.removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    invoiceList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyDataSetChanged();
                                    CustomToast.showToastSuccessful(context, "Xóa hóa đơn thành công");
                                })
                                .addOnFailureListener(e -> CustomToast.showToastFailed(context, "Xóa hóa đơn thất bại"));
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
    public int getItemCount() {
        return invoiceList != null ? invoiceList.size() : 0;
    }
}
