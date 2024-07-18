package vn.edu.tdc.selling_medicine_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import vn.edu.tdc.selling_medicine_app.feature.CustomToast;
import vn.edu.tdc.selling_medicine_app.feature.ReceiveUserInfo;
import vn.edu.tdc.selling_medicine_app.feature.ReloadSound;
import vn.edu.tdc.selling_medicine_app.feature.SwipeToDelete;
import vn.edu.tdc.selling_medicine_app.model.Customer;
import vn.edu.tdc.selling_medicine_app.feature.FormatNumber;
import vn.edu.tdc.selling_medicine_app.feature.MakePhoneCall;
import vn.edu.tdc.selling_medicine_app.model.MyBill;
import vn.edu.tdc.selling_medicine_app.model.User;
import vn.edu.tdc.selling_medicine_app.recycleview.Adapter_ItemInvoice;
import vn.edu.tdc.selling_medicine_app.recycleview.Adapter_ItemInvoiceCustomerBought;

public class DetailCustomerActivity extends AppCompatActivity {

    private Context context;
    private AlertDialog editCustomerDialog;
    private TextView customerName, customerMobileNum, qtyBought, totalCash, dateCreated;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recycleview_customerBought;
    private Toolbar toolbar_DetailCustomer;
    private TextView btnCall;
    private LinearLayout noDataAvailable;
    private Adapter_ItemInvoiceCustomerBought adapterItemInvoiceCustomerBought;
    private Customer customer = new Customer();
    private MakePhoneCall makePhoneCall;
    private String phoneNumberToCall;
    private DatabaseReference databaseReference;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private User user = new User();
    private ReloadSound reloadSound;
    private ArrayList<MyBill> invoiceList = new ArrayList<>();
    private ArrayList<MyBill> originalInvoiceList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_customer);

        //nhận dữ liệu user
        context = this;
        user = ReceiveUserInfo.getUserInfo(context);
        reloadSound = new ReloadSound(this);
        makePhoneCall = new MakePhoneCall(this);
        //Nhận dữ liệu thông tin khách hàng
        Intent intent = getIntent();
        if (intent.hasExtra("customerInfo")) {
            customer = (Customer) intent.getSerializableExtra("customerInfo");
        }
        phoneNumberToCall = customer.getCustomerMobileNum();

        setControl();
        setEvent();
        setInitialization();
        getAllInvoiceCustomerBought(customer.getCustomerMobileNum());
        deleteAInvoiceOfCustomer();
    }

    private void setEvent() {
        customerName.setText(customer.getCustomerName());
        customerMobileNum.setText(customer.getCustomerMobileNum());
        qtyBought.setText(FormatNumber.formatNumber(customer.getQtyBought()));
        totalCash.setText(FormatNumber.formatNumber(customer.getTotalCash()) + " VND");
        dateCreated.setText(customer.getDateCreated());

        recycleview_customerBought.setLayoutManager(new LinearLayoutManager(this));
        adapterItemInvoiceCustomerBought = new Adapter_ItemInvoiceCustomerBought(invoiceList, this);
        recycleview_customerBought.setAdapter(adapterItemInvoiceCustomerBought);
        adapterItemInvoiceCustomerBought.notifyDataSetChanged();

        //load lại
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadSound.playReloadSound();
                getAllInvoiceCustomerBought(customer.getCustomerMobileNum());
                swipeRefresh.setRefreshing(false);
            }
        });
        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoneCall.openDialer(phoneNumberToCall);
            }
        });
    }
    private void getAllInvoiceCustomerBought(String customerMobileNum) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Invoices/" + user.getMobileNumber() + "/" + customerMobileNum);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    invoiceList.clear();

                    for (DataSnapshot invoiceSnapshot : snapshot.getChildren()) {
                        MyBill invoice = new MyBill();
                        invoice.setCustomerMobileNum(invoiceSnapshot.child("customerMobileNum").getValue(String.class));
                        invoice.setCustomerName(invoiceSnapshot.child("customerName").getValue(String.class));
                        invoice.setInvoiceID(invoiceSnapshot.child("invoiceID").getValue(String.class));
                        invoice.setDateCreated(invoiceSnapshot.child("dateCreated").getValue(String.class));
                        invoice.setNote(invoiceSnapshot.child("note").getValue(String.class));
                        invoice.setTotalCash(getIntegerValue(invoiceSnapshot.child("totalCash")));
                        invoice.setCustomerPaid(getIntegerValue(invoiceSnapshot.child("customerPaid")));
                        invoice.setChangeOfCustomer(getIntegerValue(invoiceSnapshot.child("changeOfCustomer")));
                        invoice.setTotalQty(getIntegerValue(invoiceSnapshot.child("totalQty")));

                        List<MyBill.Item> items = new ArrayList<>();
                        for (DataSnapshot itemSnapshot : invoiceSnapshot.child("items").getChildren()) {
                            MyBill.Item item = new MyBill.Item();
                            item.setIdDrug(itemSnapshot.child("idDrug").getValue(String.class));
                            item.setDrugName(itemSnapshot.child("drugName").getValue(String.class));
                            item.setQtyDrug(getIntegerValue(itemSnapshot.child("qtyDrug")));
                            item.setPrice(getIntegerValue(itemSnapshot.child("price")));
                            items.add(item);
                        }
                        invoice.setItems(items);

                        invoiceList.add(invoice);
                    }

                    Collections.sort(invoiceList, new Comparator<MyBill>() {
                        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                        @Override
                        public int compare(MyBill o1, MyBill o2) {
                            try {
                                if (o1.getDateCreated() == null || o2.getDateCreated() == null) {
                                    return 0;
                                }
                                Date date1 = dateFormat.parse(o1.getDateCreated());
                                Date date2 = dateFormat.parse(o2.getDateCreated());
                                return date2.compareTo(date1);
                            } catch (ParseException | NullPointerException e) {
                                e.printStackTrace();
                                return 0;
                            }
                        }
                    });

                    noDataAvailable.setVisibility(View.GONE);
                    originalInvoiceList.clear();
                    originalInvoiceList.addAll(invoiceList);

                    adapterItemInvoiceCustomerBought.notifyDataSetChanged();
                } else {
                    noDataAvailable.setVisibility(View.VISIBLE);
                    Log.d("InvoiceData", "không có hóa đơn: " + customerMobileNum);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("InvoiceData", "Không thể truy vấn: " + error.getMessage());
            }
        });
    }
    private void deleteAInvoiceOfCustomer() {
        SwipeToDelete swipeToDeleteCallback = new SwipeToDelete(adapterItemInvoiceCustomerBought, this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recycleview_customerBought);
    }
    private Integer getIntegerValue(DataSnapshot dataSnapshot) {
        return dataSnapshot.getValue(Integer.class) != null ? dataSnapshot.getValue(Integer.class) : 0;
    }

    private void setControl() {
        customerName = findViewById(R.id.customerName);
        customerMobileNum = findViewById(R.id.customerMobileNum);
        qtyBought = findViewById(R.id.qtyBought);
        totalCash = findViewById(R.id.totalCash);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        recycleview_customerBought = findViewById(R.id.recycleview_customerBought);
        toolbar_DetailCustomer = findViewById(R.id.toolbar_DetailCustomer);
        noDataAvailable = findViewById(R.id.noDataAvailable);
        dateCreated = findViewById(R.id.dateCreated);
        btnCall = findViewById(R.id.btnCall);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        makePhoneCall.onRequestPermissionsResult(requestCode, permissions, grantResults, phoneNumberToCall);
    }

    private void setInitialization() {
        setSupportActionBar(toolbar_DetailCustomer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.menu_edit) {
            showEditCustomerDialog();
            //finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showEditCustomerDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog_edit_customer, null);
        dialogBuilder.setView(dialogView);

        EditText customerMobileNum = dialogView.findViewById(R.id.customerMobileNum);
        EditText customerName = dialogView.findViewById(R.id.customerName);

        customerMobileNum.setText(customer.getCustomerMobileNum());
        customerName.setText(customer.getCustomerName());

        customerMobileNum.setEnabled(false);

        dialogBuilder.setTitle("Cập nhật thông tin khách hàng");
        dialogBuilder.setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Chỉ lấy và cập nhật tên khách hàng
                String fullName = customerName.getText().toString().trim();

                if (fullName.isEmpty()) {
                    CustomToast.showToastFailed(context,"Vui lòng nhập đầy đủ thông tin khách hàng");
                } else {
                    editInfoCustomer(customer.getCustomerMobileNum(), fullName);
                }
            }
        });
        dialogBuilder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        editCustomerDialog = dialogBuilder.create();
        editCustomerDialog.show();
    }


    private void editInfoCustomer(String newMobileNumber, String newFullName) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Customers/"+user.getMobileNumber());
        databaseReference.child(customer.getCustomerMobileNum()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Customer customerToUpdate = snapshot.getValue(Customer.class);
                    customerToUpdate.setCustomerName(newFullName);

                    databaseReference.child(customer.getCustomerMobileNum()).setValue(customerToUpdate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    CustomToast.showToastSuccessful(context,"Cập nhật thông tin khách hàng thành công");
                                    showNewData(newFullName);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    CustomToast.showToastFailed(context,"Lỗi cập nhật thông tin khách hàng");
                                }
                            });
                } else {
                    CustomToast.showToastFailed(context,"Không tìm thấy khách hàng để cập nhật");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                CustomToast.showToastFailed(context,"Lỗi cập nhật thông tin khách hàng");
            }
        });
    }

    private void showNewData(String fullName) {
        customerName.setText(fullName);
        customer.setCustomerName(fullName);
    }
}