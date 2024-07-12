package vn.edu.tdc.selling_medicine_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.edu.tdc.selling_medicine_app.feature.CustomToast;
import vn.edu.tdc.selling_medicine_app.feature.GetCurrentDate;
import vn.edu.tdc.selling_medicine_app.feature.ReceiveUserInfo;
import vn.edu.tdc.selling_medicine_app.feature.SwipeToDelete;
import vn.edu.tdc.selling_medicine_app.model.Customer;
import vn.edu.tdc.selling_medicine_app.model.MyBill;
import vn.edu.tdc.selling_medicine_app.model.Product;
import vn.edu.tdc.selling_medicine_app.model.User;
import vn.edu.tdc.selling_medicine_app.recycleview.Adapter_ItemMedicineAddedPrePayment;

public class PrePaymentActivity extends AppCompatActivity {

    ImageView ivMedicinePrePayment;
    TextInputEditText edtCustomerMobileNum, edtCustomerName, edtNote;
    TextView dateCreated;
    Button nextPayment,btnAddMedicine;
    Toolbar toolbar_prePayment;
    View viewPrePayment;
    ////////////////////////////////////////////////////////
    private Context context;
    private RecyclerView rec_medicine_added_list;
    private Adapter_ItemMedicineAddedPrePayment adapterItemMedicineAddedPrePayment;
    private ArrayList<Product> productArrayList;
    private List<Customer> customerList = new ArrayList<>();
    private List<Customer> originalCustomerList = new ArrayList<>();
    private User user = new User();
    private Customer customer = new Customer();

    //////////////////////
    private List<String> selectedDrugIds = new ArrayList<>();
    private Map<String, String> drugIdMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_payment);
//        context = this;
//        user = ReceiveUserInfo.getUserInfo(context);

//        Intent intent = getIntent();
//        if (intent.hasExtra("customerInfo")) {
//            customer = (Customer) intent.getSerializableExtra("customerInfo");
//        }
//        setControl();
//        setEvent();
//
//        productArrayList = new ArrayList<>();
//        adapterItemMedicineAddedPrePayment = new Adapter_ItemMedicineAddedPrePayment(productArrayList, context);
//        rec_medicine_added_list.setAdapter(adapterItemMedicineAddedPrePayment);
//        rec_medicine_added_list.setLayoutManager(new LinearLayoutManager(this));
//
//        loadCustomerList();
        context = this;
        user = ReceiveUserInfo.getUserInfo(context);

        setControl();
        setEvent();

        productArrayList = new ArrayList<>();
        adapterItemMedicineAddedPrePayment = new Adapter_ItemMedicineAddedPrePayment(productArrayList, context);
        rec_medicine_added_list.setAdapter(adapterItemMedicineAddedPrePayment);
        rec_medicine_added_list.setLayoutManager(new LinearLayoutManager(this));

        loadCustomerList();
        //deleteDrugInvoicePrePayment();
        //getCustomerInfo();
        //sendInvoiceToPayment();
    }


    private void setEvent() {
        setSupportActionBar(toolbar_prePayment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtCustomerMobileNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String mobileNum = s.toString().trim();
                if (!mobileNum.isEmpty()) {
                    checkAndSetCustomerInfo(mobileNum);
                } else {
                    edtCustomerName.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edtNote.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //edtNote.clearFocus();
                    hideKeyboard();
                    return true;
                }
                return false;
            }
        });

        edtNote.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard();
                }
            }
        });
        dateCreated.setText(GetCurrentDate.getCurrentDate());
        btnAddMedicine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddMedicineDialog();
            }
        });
        nextPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edtCustomerMobileNum.getText().toString().isEmpty() || edtCustomerName.getText().toString().isEmpty() || adapterItemMedicineAddedPrePayment.getItemCount() == 0) {
                    CustomToast.showToastFailed(context,"Vui lòng nhập đầy đủ thông tin đơn thuốc");
                } else {
                    addNewCustomer(edtCustomerMobileNum.getText().toString(),edtCustomerName.getText().toString());
                    sendToPaymentActivity();
                }
            }
        });
    }


        private void sendToPaymentActivity() {
        String customerMobileNum = edtCustomerMobileNum.getText().toString().trim();
        String customerName = edtCustomerName.getText().toString().trim();
        String dateCreatedText = dateCreated.getText().toString().trim();
        String note = edtNote.getText().toString().trim();

        MyBill myBill = new MyBill();
        myBill.setUserMobileNum(user.getMobileNumber());
        myBill.setCustomerMobileNum(customerMobileNum);
        myBill.setDateCreated(dateCreatedText);
        myBill.setNote(note);

        for (Product product : productArrayList) {
            MyBill.Item item = new MyBill.Item();
            item.setIdDrug(product.getIdDrug()); ///////////set id để add firebase
            item.setDrugName(product.getDrugName());
            item.setQtyDrug(product.getQtyInventory());
            // Nếu có giá thuốc thì cập nhật vào item.setPrice(product.getPrice());
            myBill.addItem(item);
        }

        // Gửi MyBill qua Intent
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("customerMobileNum", customerMobileNum);
        intent.putExtra("myBill", myBill);
        intent.putExtra("customerName", customerName);
        intent.putStringArrayListExtra("selectedDrugIds", new ArrayList<>(selectedDrugIds));
        startActivity(intent);
    }
    private void addNewCustomer(String mobileNumber, String fullName) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Customers/" + user.getMobileNumber());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.child(mobileNumber).exists()) {
                    // Khách hàng không tồn tại trong database, thêm mới
                    Customer newCustomer = new Customer(mobileNumber, fullName, GetCurrentDate.getCurrentDate(), 0, 0);
                    databaseReference.child(mobileNumber).setValue(newCustomer)
                            .addOnSuccessListener(aVoid -> {
                                CustomToast.showToastSuccessful(context,"Đã thêm khách hàng mới");
                            })
                            .addOnFailureListener(e -> {

                            });
                } else {
                    //đã tồn tại
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //lỗi truy vấn
            }
        });
    }

    private void fetchDrugNames(final AutoCompleteTextView autoCompleteDrugName) {
        DatabaseReference drugsRef = FirebaseDatabase.getInstance().getReference("Drugs/" + user.getMobileNumber());
        drugsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> drugNames = new ArrayList<>();
                for (DataSnapshot drugSnapshot : snapshot.getChildren()) {
                    String idDrug = drugSnapshot.getKey(); // Lấy ID thuốc
                    String drugName = drugSnapshot.child("drugName").getValue(String.class);
                    if (drugName != null) {
                        drugNames.add(drugName);
                        drugIdMap.put(drugName, idDrug); // Lưu ánh xạ tên thuốc với idDrug
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, drugNames);
                autoCompleteDrugName.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                CustomToast.showToastFailed(context, "Đã xảy ra lỗi khi tải danh sách thuốc");
            }
        });
    }

    public void openAddMedicineDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog_add_medicine_to_invoice, null);
        builder.setView(dialogView);

        final AutoCompleteTextView autoCompleteDrugName = dialogView.findViewById(R.id.drugNameInvoice);
        final TextInputEditText editTextQty = dialogView.findViewById(R.id.qtyDrugInvoice);

        fetchDrugNames(autoCompleteDrugName);

        builder.setPositiveButton("Thêm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String drugName = autoCompleteDrugName.getText().toString().trim();
                String qtyStr = editTextQty.getText().toString().trim();

                if (!drugName.isEmpty() && !qtyStr.isEmpty()) {
                    try {
                        int qty = Integer.parseInt(qtyStr);
                        String idDrug = drugIdMap.get(drugName); // Lấy idDrug từ drugIdMap
                        if (idDrug != null) {
                            Product newProduct = new Product(idDrug, drugName, qty);
                            productArrayList.add(newProduct);
                            selectedDrugIds.add(idDrug);
                            adapterItemMedicineAddedPrePayment.notifyDataSetChanged();


                            CustomToast.showToastSuccessful(context,"Thêm thuốc vào đơn thành công");
                        } else {
                            CustomToast.showToastFailed(context, "Không tìm thấy id thuốc");
                        }
                    } catch (NumberFormatException e) {
                        CustomToast.showToastFailed(context, "Số lượng không hợp lệ");
                    }
                } else {
                    CustomToast.showToastFailed(context, "Vui lòng nhập đầy đủ thông tin");
                }
            }
        });


        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void checkAndSetCustomerInfo(String customerMobileNum) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Customers/" + user.getMobileNumber() + "/" + customerMobileNum);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Customer customer = snapshot.getValue(Customer.class);
                    if (customer != null) {
                        edtCustomerName.setText(customer.getCustomerName());
                        edtCustomerName.setEnabled(false);
                        edtCustomerName.setTextColor(getColor(R.color.loading));
                    }
                } else {
                    edtCustomerName.setText("");
                    edtCustomerName.setEnabled(true);
                    edtCustomerName.setTextColor(getColor(R.color.black));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                CustomToast.showToastFailed(context, "Đã xảy ra lỗi khi tải thông tin khách hàng");
            }
        });
    }

    private void loadCustomerList() {
        DatabaseReference customersRef = FirebaseDatabase.getInstance().getReference().child("Customers");

        customersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                originalCustomerList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Customer customer = snapshot.getValue(Customer.class);
                    if (customer != null) {
                        originalCustomerList.add(customer);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                CustomToast.showToastFailed(context, "Đã xảy ra lỗi khi tải danh sách khách hàng");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setControl() {
        nextPayment = findViewById(R.id.nextPayment);
        btnAddMedicine = findViewById(R.id.btnAddMedicine);
        ivMedicinePrePayment = findViewById(R.id.ivMedicinePrePayment);
        edtCustomerMobileNum = findViewById(R.id.edtCustomerMobileNum);
        edtCustomerName = findViewById(R.id.edtCustomerName);
        edtNote = findViewById(R.id.edtNote);
        dateCreated = findViewById(R.id.dateCreated);
        rec_medicine_added_list = findViewById(R.id.rec_medicine_added_list);
        toolbar_prePayment = findViewById(R.id.toolbar_prePayment);
        viewPrePayment = findViewById(R.id.viewPrePayment);

        viewPrePayment.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });
    }
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}