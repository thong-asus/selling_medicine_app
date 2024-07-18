package vn.edu.tdc.selling_medicine_app;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.edu.tdc.selling_medicine_app.feature.ReceiveUserInfo;
import vn.edu.tdc.selling_medicine_app.model.MyBill;
import vn.edu.tdc.selling_medicine_app.model.User;
import vn.edu.tdc.selling_medicine_app.recycleview.Adapter_ItemRecentInvoiceHome;

public class HomeActivity extends AppCompatActivity {
    private Toolbar toolbar_home;
    private View layout_home;
    private TextInputEditText search_home;
    private Button btn_history, btn_payment, btn_statistic, btn_inventory, btn_product, btn_customer;
    private BottomNavigationView bottom_navigation;
    private Context context;
    private User user = new User();
    private Adapter_ItemRecentInvoiceHome adapterItemRecentInvoice;
    private RecyclerView recycleview_recentInvoice;

    private ArrayList<MyBill> recentInvoiceList;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //nhận dữ liệu
        context = this;
        user = ReceiveUserInfo.getUserInfo(context);
        setControl();
        ////////////////////////////////////////////////
        FirebaseApp.initializeApp(this);
        //Khởi tạo db
        databaseReference = FirebaseDatabase.getInstance().getReference();
        setEvent();
        /////////////////////////set data cho recycleview 5 hóa đơn gần nhất////////////////////////////
        showItemInvoicesRecent();
        getCustomerMobiles();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCustomerMobiles();
    }
    private void showItemInvoicesRecent() {
        //thiết lập mỗi lần lướt tới 1 item
        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(recycleview_recentInvoice);
        // Khởi tạo RecyclerView
        recycleview_recentInvoice.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recentInvoiceList = new ArrayList<>();
        adapterItemRecentInvoice = new Adapter_ItemRecentInvoiceHome(recentInvoiceList, this);
        recycleview_recentInvoice.setAdapter(adapterItemRecentInvoice);
    }

    private void getFiveInvoicesRecent(String userMobile, List<String> customerMobiles) {
        databaseReference.child("Invoices").child(userMobile).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<MyBill> allInvoices = new ArrayList<>();

                for (String customerMobile : customerMobiles) {
                    DataSnapshot customerSnapshot = dataSnapshot.child(customerMobile);

                    if (customerSnapshot.exists()) {
                        for (DataSnapshot invoiceSnapshot : customerSnapshot.getChildren()) {
                            HashMap<String, Object> invoiceMap = (HashMap<String, Object>) invoiceSnapshot.getValue();

                            if (invoiceMap != null) {
                                MyBill invoice = new MyBill();
                                invoice.setInvoiceID((String) invoiceMap.get("invoiceID"));
                                invoice.setCustomerMobileNum((String) invoiceMap.get("customerMobileNum"));
                                invoice.setNote((String) invoiceMap.get("note"));
                                invoice.setCustomerName((String) invoiceMap.get("customerName"));
                                invoice.setCustomerPaid(((Long) invoiceMap.get("customerPaid")).intValue());
                                invoice.setDateCreated((String) invoiceMap.get("dateCreated"));
                                invoice.setTotalCash(((Long) invoiceMap.get("totalCash")).intValue());
                                invoice.setTotalQty(((Long) invoiceMap.get("totalQty")).intValue());

                                List<MyBill.Item> itemsList = new ArrayList<>();
                                HashMap<String, HashMap<String, Object>> itemsMap = (HashMap<String, HashMap<String, Object>>) invoiceMap.get("items");
                                if (itemsMap != null) {
                                    for (Map.Entry<String, HashMap<String, Object>> entry : itemsMap.entrySet()) {
                                        HashMap<String, Object> itemMap = entry.getValue();
                                        if (itemMap != null) {
                                            MyBill.Item item = new MyBill.Item();
                                            item.setDrugName((String) itemMap.get("drugName"));
                                            item.setIdDrug((String) itemMap.get("idDrug"));
                                            item.setPrice(((Long) itemMap.get("price")).intValue());
                                            item.setQtyDrug(((Long) itemMap.get("qtyDrug")).intValue());

                                            itemsList.add(item);
                                        }
                                    }
                                }
                                invoice.setItems(itemsList);
                                allInvoices.add(invoice);
                            }
                        }
                    }
                }

                Collections.sort(allInvoices, new Comparator<MyBill>() {
                    @Override
                    public int compare(MyBill o1, MyBill o2) {
                        return o2.getDateCreated().compareTo(o1.getDateCreated());
                    }
                });

                List<MyBill> latestInvoices = allInvoices.subList(0, Math.min(5, allInvoices.size()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapterItemRecentInvoice.setInvoiceList(latestInvoices);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read Invoices data.", databaseError.toException());
            }
        });
    }


    private void getCustomerMobiles() {
        databaseReference.child("InvoiceCustomer/" + user.getMobileNumber()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> customerMobiles = new ArrayList<>();

                for (DataSnapshot customerSnapshot : dataSnapshot.getChildren()) {
                    String customerMobileNum = customerSnapshot.getKey();
                    customerMobiles.add(customerMobileNum);
                }
                getFiveInvoicesRecent(user.getMobileNumber(), customerMobiles);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read InvoiceCustomer data.", databaseError.toException());
            }
        });
    }





    private void setEvent() {
        btn_statistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Chuyển sang màn hình thống kê", Toast.LENGTH_SHORT).show();
            }
        });
        btn_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PrePaymentActivity.class);
                startActivity(intent);
            }
        });
        btn_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProductListActivity.class);
                startActivity(intent);
            }
        });
        btn_inventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        btn_customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CustomerListActivity.class);
                startActivity(intent);
            }
        });
        btn_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, HistorySalesListActivity.class);
                startActivity(intent);
            }
        });
        toolbar_home.setTitle("Xin chào, " + user.getFullname());
        layout_home.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });

        bottom_navigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case 0:

                    return true;
                case 1:

                    return true;
                case 2:

                    return true;
                default:
                    return false;
            }
        });

    }
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//        if (result != null) {
//            if (result.getContents() == null) {
//                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
//            } else {
//                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
//                // Xử lý kết quả quét mã tại đây, ví dụ: hiển thị hoặc sử dụng cho xử lý tiếp theo
//            }
//        } else {
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//    }

    private void setControl() {
        toolbar_home = findViewById(R.id.toolbar_home);
        layout_home = findViewById(R.id.layout_home);
        search_home = findViewById(R.id.search_home);
        recycleview_recentInvoice = findViewById(R.id.recycleview_recentInvoice);
        btn_history = findViewById(R.id.btn_history);
        btn_payment = findViewById(R.id.btn_payment);
        btn_statistic = findViewById(R.id.btn_statistic);
        btn_inventory = findViewById(R.id.btn_inventory);
        btn_product = findViewById(R.id.btn_product);
        btn_customer = findViewById(R.id.btn_customer);
        bottom_navigation = findViewById(R.id.bottom_navigation);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}