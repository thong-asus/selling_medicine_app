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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdc.selling_medicine_app.model.Customer;
import vn.edu.tdc.selling_medicine_app.model.SwipeToDelete;
import vn.edu.tdc.selling_medicine_app.recycleview.ItemCustomerAdapter;

public class CustomerListActivity extends AppCompatActivity {
    private AlertDialog addCustomerDialog;
    private Toolbar toolbar_customerList;
    TextInputEditText search_customerList;
    private RecyclerView recyclerView_customerList;
    private ItemCustomerAdapter itemCustomerAdapter;
    private List<Customer> customerList = new ArrayList<>();
    private List<Customer> originalCustomerList = new ArrayList<>();
    private Context context;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list);
        context = this;
        setInitialization();
        getAllCustomer();
        deleteACustomer();
        setEvent();
    }

    private void setEvent() {

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
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
    }

    private void setInitialization() {
        toolbar_customerList = findViewById(R.id.toolbar_customerList);
        search_customerList = findViewById(R.id.search_customerList);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        setSupportActionBar(toolbar_customerList);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView_customerList = findViewById(R.id.recycleview_customerList);
        recyclerView_customerList.setLayoutManager(new LinearLayoutManager(this));
        itemCustomerAdapter = new ItemCustomerAdapter(customerList, this);
        recyclerView_customerList.setAdapter(itemCustomerAdapter);
        itemCustomerAdapter.notifyDataSetChanged();
    }

    private void getAllCustomer() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Customers");
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
        getMenuInflater().inflate(R.menu.menu_add_customer, menu);
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
                    Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin khách hàng", Toast.LENGTH_SHORT).show();
                } else {
                    addNewCustomer(mobileNumber, fullName);
                    getAllCustomer();
                    Toast.makeText(context, "Thêm khách hàng thành công", Toast.LENGTH_SHORT).show();
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
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Customers");
        Customer newCustomer = new Customer(mobileNumber, fullName, 0, 0);
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
