package vn.edu.tdc.selling_medicine_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.Format;
import java.util.ArrayList;
import java.util.List;
import java.util.PrimitiveIterator;

import vn.edu.tdc.selling_medicine_app.feature.FormatNumber;
import vn.edu.tdc.selling_medicine_app.feature.ReceiveUserInfo;
import vn.edu.tdc.selling_medicine_app.model.Customer;
import vn.edu.tdc.selling_medicine_app.model.MyBill;
import vn.edu.tdc.selling_medicine_app.model.Product;
import vn.edu.tdc.selling_medicine_app.model.User;
import vn.edu.tdc.selling_medicine_app.recycleview.Adapter_ItemDetailInvoice;
import vn.edu.tdc.selling_medicine_app.recycleview.Adapter_ItemInvoice;
import vn.edu.tdc.selling_medicine_app.recycleview.Adapter_ItemMedicineAddedPrePayment;
import vn.edu.tdc.selling_medicine_app.recycleview.Adapter_ItemPayment;
import vn.edu.tdc.selling_medicine_app.recycleview.ItemInvoiceViewHolder;

public class DetailHistorySalesActivity extends AppCompatActivity {

    private Toolbar toolbarInvoiceDetail;
    private ImageView ivMedicineDetail;
    private TextView customerMobileNum, customerName, dateCreated, noted, totalQtyDrug, totalCash, customerPaid, changeOfCustomer;
    private RecyclerView rec_itemInvoiceDetail;
    ////////////////////////////////////////////////
    private User user = new User();
    private Context context;
    private MyBill invoice = new MyBill();
    private DatabaseReference databaseReference;
    private List<MyBill.Item> itemsList = new ArrayList<>();
    private Adapter_ItemDetailInvoice adapterItemDetailInvoice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_history_sales);
        context = this;
        user = ReceiveUserInfo.getUserInfo(context);

        setControl();
        setEvent();
        loadInvoiceDetails();
    }

    private void loadInvoiceDetails() {
        itemsList = new ArrayList<>();
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("invoiceInfo")) {
            invoice = (MyBill) intent.getSerializableExtra("invoiceInfo");

            if (invoice != null) {
                itemsList = invoice.getItems();
                adapterItemDetailInvoice = new Adapter_ItemDetailInvoice(itemsList, this);
                rec_itemInvoiceDetail.setAdapter(adapterItemDetailInvoice);
                rec_itemInvoiceDetail.setLayoutManager(new LinearLayoutManager(this));

                customerMobileNum.setText(invoice.getCustomerMobileNum());
                customerName.setText(invoice.getCustomerName());
                dateCreated.setText(invoice.getDateCreated());
                noted.setText(invoice.getNote());
                totalCash.setText(FormatNumber.formatNumber(invoice.getTotalCash()) + " VND");
                totalQtyDrug.setText(FormatNumber.formatNumber(invoice.getTotalQty()));
                customerPaid.setText(FormatNumber.formatNumber(invoice.getCustomerPaid()) + " VND");
                changeOfCustomer.setText(FormatNumber.formatNumber(invoice.getChangeOfCustomer()) + " VND");

                //////////////////tải hình ảnh
                String imageUrl = invoice.getImageInvoice();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(context)
                            .load(imageUrl)
                            .apply(new RequestOptions().placeholder(R.drawable.loading)
                                    .error(R.drawable.loadingerror))
                            .into(ivMedicineDetail);
                } else {
                    ivMedicineDetail.setImageResource(R.drawable.loading);
                }


            } else {
                Log.e("DetailHistorySalesActivity", "Invoice object is null");
            }
        } else {
            Log.e("DetailHistorySalesActivity", "Intent does not contain invoiceInfo");
        }
    }


    private void setControl() {
        toolbarInvoiceDetail = findViewById(R.id.toolbarInvoiceDetail);
        ivMedicineDetail = findViewById(R.id.ivMedicineDetail);
        customerMobileNum = findViewById(R.id.customerMobileNum);
        customerName = findViewById(R.id.customerName);
        dateCreated = findViewById(R.id.dateCreated);
        noted = findViewById(R.id.noted);
        totalQtyDrug = findViewById(R.id.totalQtyDrug);
        totalCash = findViewById(R.id.totalCash);
        customerPaid = findViewById(R.id.customerPaid);
        changeOfCustomer = findViewById(R.id.changeOfCustomer);
        rec_itemInvoiceDetail = findViewById(R.id.rec_itemInvoiceDetail);
    }

    private void setEvent() {
        setSupportActionBar(toolbarInvoiceDetail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}