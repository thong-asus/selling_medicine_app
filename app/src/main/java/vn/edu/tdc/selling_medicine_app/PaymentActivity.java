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
import android.view.MotionEvent;
import android.view.View;
import android.view.contentcapture.ContentCaptureCondition;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.units.qual.C;

import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.edu.tdc.selling_medicine_app.feature.CustomToast;
import vn.edu.tdc.selling_medicine_app.feature.GetCurrentDate;
import vn.edu.tdc.selling_medicine_app.feature.ReceiveUserInfo;
import vn.edu.tdc.selling_medicine_app.model.BillSingleton;
import vn.edu.tdc.selling_medicine_app.model.Customer;
import vn.edu.tdc.selling_medicine_app.model.MyBill;
import vn.edu.tdc.selling_medicine_app.model.User;
import vn.edu.tdc.selling_medicine_app.recycleview.Adapter_ItemPayment;

public class PaymentActivity extends AppCompatActivity {

    Toolbar toolbar_Payment;
    Button btnSaveBill;
    View viewPayment;
    private RecyclerView rec_itemPayment;
    private Adapter_ItemPayment adapterItemPayment;
    private List<MyBill.Item> itemList;
    TextView customerMobileNum, customerName, dateCreated, noted, changeOfCustomer, totalQtyDrug;
    TextInputEditText edtTotalCash, edtCustomerPaid;
    private Context context;
    private User user = new User();
    private Customer customer;
    private String customerMobileNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        context = this;
        user = ReceiveUserInfo.getUserInfo(context);
        customer = new Customer();
        setControl();
        getChangeOfCustomer();
        // setEvent();
        ArrayList<String> selectedDrugIds = getIntent().getStringArrayListExtra("selectedDrugIds");
        if (selectedDrugIds != null) {

        }

        Intent intent = getIntent();
        if (intent != null) {
            customerMobileNumber = intent.getStringExtra("customerMobileNum");
            //Log.d("PaymentActivity", "Received Customer Mobile Number: " + customerMobileNumber);

            // Lấy customerName từ Intent và hiển thị
            String customerNameFromIntent = intent.getStringExtra("customerName");
            if (customerNameFromIntent != null) {
                TextView textViewCustomerName = findViewById(R.id.customerName);
                textViewCustomerName.setText(customerNameFromIntent);
            }

            if (intent.hasExtra("myBill")) {
                MyBill myBill = (MyBill) intent.getSerializableExtra("myBill");
                if (myBill != null) {
                    itemList = myBill.getItems();

                    adapterItemPayment = new Adapter_ItemPayment(itemList, this);
                    rec_itemPayment.setAdapter(adapterItemPayment);
                    rec_itemPayment.setLayoutManager(new LinearLayoutManager(this));
/////////////////////////////////////////HIỂN THỊ SỐ LƯỢNG LÊN TEXTVIEW/////////////////////////////
                    int totalQuantity = 0;
                    for (MyBill.Item item : itemList) {
                        totalQuantity += item.getQtyDrug();
                    }
                    totalQtyDrug.setText(String.valueOf(totalQuantity));
////////////////////////////////////////////////////////////////////////////////////////////////////
                    customerMobileNum.setText(myBill.getCustomerMobileNum());
                    dateCreated.setText(myBill.getDateCreated());
                    noted.setText(myBill.getNote());
                    customerInfo(myBill.getCustomerMobileNum());

                } else {
                    //Toast.makeText(this, "Không có hóa đơn để hiển thị", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                //Toast.makeText(this, "Không tìm thấy dữ liệu hóa đơn", Toast.LENGTH_SHORT).show();
            }
        }

        btnSaveBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edtTotalCash.getText().toString().isEmpty() || edtCustomerPaid.getText().toString().isEmpty()){
                    CustomToast.showToastFailed(context,"Vui lòng nhập số tiền");
                } else {
                    saveBill();
                }
            }
        });
    }


    private void saveBill() {
        String userMobileNumber = user.getMobileNumber();
        Log.d("PaymentActivity", "User Mobile Number: " + userMobileNumber);
        Log.d("PaymentActivity", "Customer Mobile Number: " + customerMobileNumber);

        if (userMobileNumber == null || customerMobileNumber == null) {
            Toast.makeText(context, "Số điện thoại người dùng hoặc khách hàng không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Invoices");
        String invoiceID = databaseReference.push().getKey(); // Tạo ID hóa đơn tự động
        String purchaseDate = GetCurrentDate.getCurrentDate(); // Lấy ngày hiện tại
        String note = noted.getText().toString().trim(); // Lấy ghi chú
        String totalCashStr = edtTotalCash.getText().toString().trim();
        String customerPayStr = edtCustomerPaid.getText().toString().trim();

        if (!totalCashStr.isEmpty() && !customerPayStr.isEmpty() && itemList != null && !itemList.isEmpty()) {
            int totalCash = Integer.parseInt(totalCashStr);
            int customerPay = Integer.parseInt(customerPayStr);
            int changeOfCustomer = customerPay - totalCash;

            MyBill myBill = new MyBill();
            myBill.setInvoiceID(invoiceID);
            myBill.setDateCreated(purchaseDate);
            myBill.setNote(note);
            myBill.setCustomerMobileNum(customerMobileNumber);
            myBill.setTotalCash(totalCash);
            myBill.setCustomerPaid(customerPay);
            myBill.setChangeOfCustomer(changeOfCustomer);

            int totalQuantity = 0;
            for (MyBill.Item item : itemList) {
                totalQuantity += item.getQtyDrug();
            }
            totalQtyDrug.setText(String.valueOf(totalQuantity));
            myBill.setTotalQty(totalQuantity);

            DatabaseReference billRef = databaseReference.child(userMobileNumber).child(customerMobileNumber).child(invoiceID);
            billRef.setValue(myBill)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Lưu từng mục thuốc với khóa là idDrug
                            DatabaseReference itemsRef = billRef.child("items");
                            for (MyBill.Item item : itemList) {
                                if (item.getIdDrug() != null && !item.getIdDrug().isEmpty()) {
                                    itemsRef.child(item.getIdDrug()).setValue(item)
                                            .addOnCompleteListener(itemTask -> {
                                                if (!itemTask.isSuccessful()) {
                                                    Log.e("PaymentActivity", "Lỗi khi lưu mục thuốc: " + item.getIdDrug());
                                                }
                                            });
                                } else {
                                    Log.e("PaymentActivity", "idDrug không hợp lệ cho mục thuốc: " + item.getDrugName());
                                }
                            }
                            CustomToast.showToastSuccessful(context, "Hóa đơn đã được lưu thành công");
                            updateCustomerStats(totalCash);
                            Intent intent = new Intent(PaymentActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            //CustomToast.showToastFailed(context, "Lỗi khi lưu hóa đơn");
                        }
                    });
        } else {
            //CustomToast.showToastFailed(context, "Vui lòng nhập đầy đủ thông tin");
        }
    }

    private void updateCustomerStats(int totalCashToAdd) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Customers/" + user.getMobileNumber() + "/" + customerMobileNumber);
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    // Lấy thông tin hiện tại
                    int currentQtyBought = snapshot.child("qtyBought").getValue(Integer.class);
                    int currentTotalCash = snapshot.child("totalCash").getValue(Integer.class);

                    // Cập nhật giá trị mới
                    int newQtyBought = currentQtyBought + 1;
                    int newTotalCash = currentTotalCash + totalCashToAdd;

                    // Cập nhật đồng thời cả hai trường
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("qtyBought", newQtyBought);
                    updates.put("totalCash", newTotalCash);

                    databaseReference.updateChildren(updates)
                            .addOnCompleteListener(updateTask -> {
                                if (updateTask.isSuccessful()) {
                                    Log.d("UpdateCustomer", "Cập nhật số lượng đơn hàng và tổng tiền thành công.");
                                } else {
                                    Log.e("UpdateCustomer", "Lỗi khi cập nhật tổng tiền: " + updateTask.getException().getMessage());
                                }
                            });
                } else {
                    Log.e("UpdateCustomer", "Khách hàng không tồn tại trong cơ sở dữ liệu.");
                }
            } else {
                Log.e("UpdateCustomer", "Lỗi khi truy xuất thông tin khách hàng: " + task.getException().getMessage());
            }
        });
    }


    private void getChangeOfCustomer() {
        String customerPayStr = edtCustomerPaid.getText().toString().trim();
        String totalCashStr = edtTotalCash.getText().toString().trim();

        if (!customerPayStr.isEmpty() && !totalCashStr.isEmpty()) {
            try {
                double customerPay = Double.parseDouble(customerPayStr);
                double totalCash = Double.parseDouble(totalCashStr);

                double changeOfCustomer = customerPay - totalCash;

                TextView textViewChange = findViewById(R.id.changeOfCustomer);
                textViewChange.setText(String.valueOf(changeOfCustomer));

            } catch (NumberFormatException e) {
                CustomToast.showToastFailed(context, "Vui lòng nhập số hợp lệ");
            }
        } else {
            CustomToast.showToastFailed(context, "Vui lòng nhập đầy đủ thông tin");
        }
    }


    private void customerInfo(String customerMobileNum) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Customers/" + user.getMobileNumber() + "/" + customerMobileNum);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Customer customer = snapshot.getValue(Customer.class);
                    if (customer != null) {
                        customerName.setText(customer.getCustomerName());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setControl() {
        rec_itemPayment = findViewById(R.id.rec_itemPayment);
        customerMobileNum = findViewById(R.id.customerMobileNum);
        customerName = findViewById(R.id.customerName);
        dateCreated = findViewById(R.id.dateCreated);
        noted = findViewById(R.id.noted);
        toolbar_Payment = findViewById(R.id.toolbar_Payment);
        edtCustomerPaid = findViewById(R.id.edtCustomerPaid);
        edtTotalCash = findViewById(R.id.edtTotalCash);
        changeOfCustomer = findViewById(R.id.changeOfCustomer);
        btnSaveBill = findViewById(R.id.btnSaveBill);
        viewPayment = findViewById(R.id.viewPayment);
        totalQtyDrug = findViewById(R.id.totalQtyDrug);

        viewPayment.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });

        setSupportActionBar(toolbar_Payment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}

