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
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import vn.edu.tdc.selling_medicine_app.feature.CustomToast;
import vn.edu.tdc.selling_medicine_app.feature.ReceiveUserInfo;
import vn.edu.tdc.selling_medicine_app.feature.SwipeToDelete;
import vn.edu.tdc.selling_medicine_app.model.Customer;
import vn.edu.tdc.selling_medicine_app.feature.GetCurrentDate;
import vn.edu.tdc.selling_medicine_app.feature.ReloadSound;
import vn.edu.tdc.selling_medicine_app.model.User;
import vn.edu.tdc.selling_medicine_app.recycleview.Adapter_ItemCustomer;

public class CustomerListActivity extends AppCompatActivity {
    private AlertDialog addCustomerDialog;
    private Toolbar toolbar_customerList;
    TextInputEditText search_customerList;
    private RecyclerView recyclerView_customerList;
    private Adapter_ItemCustomer itemCustomerAdapter;
    private List<Customer> customerList = new ArrayList<>();
    private List<Customer> originalCustomerList = new ArrayList<>();
    private Context context;
    private SwipeRefreshLayout swipeRefresh;
    private ReloadSound reloadSound;
    private  User user = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list);
        ////////////////////////////////////////////////////////
        context = this;
        user = ReceiveUserInfo.getUserInfo(context);

        /////////////////////////////
        reloadSound = new ReloadSound(this);
        setInitialization();
        getAllCustomer();
        deleteACustomer();
        setEvent();
    }

    private void setEvent() {

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadSound.playReloadSound();
                getAllCustomer();
                swipeRefresh.setRefreshing(false);
            }
        });
        search_customerList.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchCustomer(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        search_customerList.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();
                }
                return false;
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reloadSound != null) {
            reloadSound.release();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        getAllCustomer();
    }
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private void setInitialization() {
        toolbar_customerList = findViewById(R.id.toolbar_customerList);
        search_customerList = findViewById(R.id.search_customerList);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        setSupportActionBar(toolbar_customerList);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView_customerList = findViewById(R.id.recycleview_customerList);
        recyclerView_customerList.setLayoutManager(new LinearLayoutManager(this));
        itemCustomerAdapter = new Adapter_ItemCustomer(customerList, this);
        recyclerView_customerList.setAdapter(itemCustomerAdapter);
        itemCustomerAdapter.notifyDataSetChanged();
    }

    private void getAllCustomer() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Customers/"+user.getMobileNumber());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                customerList.clear();
                originalCustomerList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Customer customer = snapshot.getValue(Customer.class);
                    if (customer != null) {
                        customerList.add(customer);
                        originalCustomerList.add(customer);
                    }
                }
                Collections.sort(customerList, new Comparator<Customer>() {
                    @Override
                    public int compare(Customer c1, Customer c2) {
                        return c2.getDateCreated().compareTo(c1.getDateCreated());
                    }
                });

                Collections.sort(originalCustomerList, new Comparator<Customer>() {
                    @Override
                    public int compare(Customer c1, Customer c2) {
                        return c2.getDateCreated().compareTo(c1.getDateCreated());
                    }
                });

                itemCustomerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error loading data from Firebase: " + databaseError.getMessage());
            }
        });
    }

    private void deleteACustomer() {
        SwipeToDelete swipeToDeleteCallback = new SwipeToDelete(itemCustomerAdapter, this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView_customerList);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.menu_add) {
            showAddCustomerDialog();
            //finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddCustomerDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog_add_customer, null);
        dialogBuilder.setView(dialogView);

        EditText customerMobileNum = dialogView.findViewById(R.id.customerMobileNum);
        EditText customerName = dialogView.findViewById(R.id.customerName);

        dialogBuilder.setTitle("Thêm khách hàng");
        dialogBuilder.setPositiveButton("Thêm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String mobileNumber = customerMobileNum.getText().toString().trim();
                String fullName = customerName.getText().toString().trim();

                if (mobileNumber.isEmpty() || fullName.isEmpty()) {
                    CustomToast.showToastFailed(context,"Vui lòng nhập đầy đủ thông tin khách hàng");
                } else {
                    addNewCustomer(mobileNumber, fullName);
                    getAllCustomer();
                    CustomToast.showToastSuccessful(context,"Thêm khách hàng thành công");
                }
            }
        });
        dialogBuilder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        addCustomerDialog = dialogBuilder.create();
        addCustomerDialog.show();
    }

    private void addNewCustomer(String mobileNumber, String fullName) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Customers/"+user.getMobileNumber());
        Customer newCustomer = new Customer(mobileNumber, fullName, GetCurrentDate.getCurrentDate(), 0, 0);
        databaseReference.child(newCustomer.getCustomerMobileNum()).setValue(newCustomer);
    }

    private void searchCustomer(String searchText) {
        ArrayList<Customer> filteredCustomerList = new ArrayList<>();
        if (searchText.isEmpty()) {
            filteredCustomerList.addAll(originalCustomerList);
        } else {
            for (Customer customer : originalCustomerList) {
                if (customer.getCustomerMobileNum().toLowerCase().contains(searchText.toLowerCase()) ||
                        customer.getCustomerName().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredCustomerList.add(customer);
                }
            }
        }
        itemCustomerAdapter.updateData(filteredCustomerList);
    }
}
