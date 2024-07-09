package vn.edu.tdc.selling_medicine_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import vn.edu.tdc.selling_medicine_app.feature.CustomToast;
import vn.edu.tdc.selling_medicine_app.feature.ReceiveUserInfo;
import vn.edu.tdc.selling_medicine_app.model.Customer;
import vn.edu.tdc.selling_medicine_app.feature.FormatNumber;
import vn.edu.tdc.selling_medicine_app.feature.MakePhoneCall;
import vn.edu.tdc.selling_medicine_app.model.User;

public class DetailCustomerActivity extends AppCompatActivity {

    private Context context;
    private AlertDialog editCustomerDialog;
    TextView customerName, customerMobileNum, qtyBought, totalCash;
    RecyclerView recycleview_customerBought;
    Toolbar toolbar_DetailCustomer;
    TextView btnCall;

    Customer customer = new Customer();
    private MakePhoneCall makePhoneCall;
    private String phoneNumberToCall;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private User user = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_customer);

        //nhận dữ liệu user
        context = this;
        user = ReceiveUserInfo.getUserInfo(context);
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
    }

    private void setEvent() {
        customerName.setText(customer.getCustomerName());
        customerMobileNum.setText(customer.getCustomerMobileNum());
        qtyBought.setText(FormatNumber.formatNumber(customer.getQtyBought()));
        totalCash.setText(FormatNumber.formatNumber(customer.getTotalCash()) + " VND");
        //gọi điện
        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoneCall.openDialer(phoneNumberToCall);
            }
        });
    }

    private void setControl() {
        customerName = findViewById(R.id.customerName);
        customerMobileNum = findViewById(R.id.customerMobileNum);
        qtyBought = findViewById(R.id.qtyBought);
        totalCash = findViewById(R.id.totalCash);
        recycleview_customerBought = findViewById(R.id.recycleview_customerBought);
        toolbar_DetailCustomer = findViewById(R.id.toolbar_DetailCustomer);
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