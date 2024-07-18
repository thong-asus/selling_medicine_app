package vn.edu.tdc.selling_medicine_app.recycleview;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdc.selling_medicine_app.DetailHistorySalesActivity;
import vn.edu.tdc.selling_medicine_app.R;
import vn.edu.tdc.selling_medicine_app.feature.FormatNumber;
import vn.edu.tdc.selling_medicine_app.feature.ReceiveUserInfo;
import vn.edu.tdc.selling_medicine_app.model.Customer;
import vn.edu.tdc.selling_medicine_app.model.MyBill;
import vn.edu.tdc.selling_medicine_app.model.User;

public class Adapter_ItemRecentInvoiceHome extends RecyclerView.Adapter<IntemRecentInvoiceHomeViewHolder> {

    private Context context;
    private List<MyBill> invoiceList;
    private DatabaseReference invoicesRef; // Tham chiếu đến node "Invoices" trong Firebase
    private DatabaseReference customersRef; // Tham chiếu đến node "Customers" trong Firebase
    private User user = new User();
    private List<MyBill> invoices;

    private List<MyBill.Item> items;

    public Adapter_ItemRecentInvoiceHome(List<MyBill> invoiceList, Context context) {
        this.context = context;
        this.invoiceList = invoiceList;


        user = ReceiveUserInfo.getUserInfo(context);
        // Khởi tạo database reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        invoicesRef = database.getReference("Invoices").child(user.getMobileNumber());
        customersRef = database.getReference("Customers").child(user.getMobileNumber());
    }

    public void setInvoiceList(List<MyBill> invoiceList) {
        this.invoiceList = invoiceList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IntemRecentInvoiceHomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.custom_item_home_recent_invoice, parent, false);
        return new IntemRecentInvoiceHomeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull IntemRecentInvoiceHomeViewHolder holder, int position) {
        MyBill invoice = invoiceList.get(position);

        holder.customer_name.setText("");
        holder.customer_mobile.setText("");
        holder.total_cash.setText("");
        holder.dateCreated.setText("");

        // Set customer information from MyBill object
        holder.customer_mobile.setText(invoice.getCustomerMobileNum());
        holder.customer_name.setText(invoice.getCustomerName());
        holder.total_cash.setText(FormatNumber.formatNumber(invoice.getTotalCash()) + " VND");
        holder.dateCreated.setText(invoice.getDateCreated());

        holder.itemView.setBackgroundResource(R.drawable.bg_item1);

//        StringBuilder itemsText = new StringBuilder();
//        for (MyBill.Item item : invoice.getItems()) {
//            itemsText.append(item.getDrugName()).append(": ").append(item.getQtyDrug()).append("\n");
//
//        }
        //holder.items.setText(itemsText.toString().trim());
        holder.linear_item_invoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailHistorySalesActivity.class);
                intent.putExtra("invoiceInfo", invoice);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return invoiceList != null ? invoiceList.size() : 0;
    }

    // Phương thức để load thông tin khách hàng từ Firebase
    private void loadCustomerInfo(String customerMobileNum, LoadCustomerInfoCallback callback) {
        DatabaseReference customerRef = customersRef.child(customerMobileNum);
        customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Customer customer = dataSnapshot.getValue(Customer.class);
                    if (customer != null) {
                        callback.onCustomerInfoLoaded(customer.getCustomerName());
                    } else {
                        callback.onError("Customer data is null");
                    }
                } else {
                    callback.onError("Customer not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    interface LoadCustomerInfoCallback {
        void onCustomerInfoLoaded(String customerName);

        void onError(String errorMessage);
    }

}
