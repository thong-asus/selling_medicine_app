package vn.edu.tdc.selling_medicine_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.units.qual.C;

import vn.edu.tdc.selling_medicine_app.model.Customer;
import vn.edu.tdc.selling_medicine_app.recycleview.ItemCustomerAdapter;

public class DetailCustomerActivity extends AppCompatActivity {

    private Context context;
    private AlertDialog editCustomerDialog;
    TextView customerName, customerMobileNum, qtyBought, totalCash;
    RecyclerView recycleview_customerBought;
    Toolbar toolbar_DetailCustomer;

    Customer customer = new Customer();
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_customer);

        context = this;
        //Nhận dữ liệu thông tin khách hàng
        Intent intent = getIntent();
        if (intent.hasExtra("customerInfo")) {
            customer = (Customer) intent.getSerializableExtra("customerInfo");
            //System.out.println("Dữ liệu nhận được tại OrderDetailActivity: " + customer);
        }

        setControl();
        setEvent();
        setInitialization();
    }

    private void setEvent() {
        customerName.setText(customer.getCustomerName());
        customerMobileNum.setText(customer.getCustomerMobileNum());
        qtyBought.setText(String.valueOf(customer.getQtyBought()));
        totalCash.setText(String.valueOf(customer.getTotalCash()) + " VND");
    }

    private void setControl() {
        customerName = findViewById(R.id.customerName);
        customerMobileNum = findViewById(R.id.customerMobileNum);
        qtyBought = findViewById(R.id.qtyBought);
        totalCash = findViewById(R.id.totalCash);
        recycleview_customerBought = findViewById(R.id.recycleview_customerBought);
        toolbar_DetailCustomer = findViewById(R.id.toolbar_DetailCustomer);
    }

    private void setInitialization() {
        setSupportActionBar(toolbar_DetailCustomer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_customer, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.menu_add) {
            showEditCustomerDialog();
            //finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showEditCustomerDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog_add_customer, null);
        dialogBuilder.setView(dialogView);

        EditText customerMobileNum = dialogView.findViewById(R.id.customerMobileNum);
        EditText customerName = dialogView.findViewById(R.id.customerName);

        // Set current customer details in the dialog
        customerMobileNum.setText(customer.getCustomerMobileNum());
        customerName.setText(customer.getCustomerName());

        dialogBuilder.setTitle("Cập nhật thông tin");
        dialogBuilder.setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String mobileNumber = customerMobileNum.getText().toString().trim();
                String fullName = customerName.getText().toString().trim();

                if (mobileNumber.isEmpty() || fullName.isEmpty()) {
                    Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin khách hàng", Toast.LENGTH_SHORT).show();
                } else {
                    editInfoCustomer(mobileNumber, fullName);
                    customer.setCustomerMobileNum(mobileNumber); // Update local customer object
                    customer.setCustomerName(fullName); // Update local customer object
                    setEvent(); // Refresh customer details on the screen
                    Toast.makeText(context, "Cập nhật thông tin khách hàng thành công", Toast.LENGTH_SHORT).show();
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
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Customers");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    databaseReference.child(customer.getCustomerMobileNum()).removeValue();
                    Customer updatedCustomer = new Customer(newMobileNumber, newFullName, customer.getQtyBought(), customer.getTotalCash());
                    databaseReference.child(newMobileNumber).setValue(updatedCustomer);
                    customer.setCustomerMobileNum(newMobileNumber);
                    customer.setCustomerName(newFullName);
                    setEvent();
                    Toast.makeText(context, "Cập nhật thông tin khách hàng thành công", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Lỗi cập nhật thông tin khách hàng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}