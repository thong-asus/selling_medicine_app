package vn.edu.tdc.selling_medicine_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import vn.edu.tdc.selling_medicine_app.model.User;

public class HomeActivity extends AppCompatActivity {

    Toolbar toolbar_home;
    View layout_home;
    TextInputEditText search_home;
    RecyclerView recycleview_recentInvoice;
    Button btn_history, btn_payment,btn_statistic,btn_inventory,btn_product,btn_customer;
    BottomNavigationView bottom_navigation;
    Context context;
    FirebaseFirestore firestore;
    private String mobileNumber;
    private  User user = new User();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //nhận dữ liệu
        SharedPreferences sharedPreferences = getSharedPreferences("informationUser", Context.MODE_PRIVATE);
        if (sharedPreferences.contains("informationUser")) {
            String jsonShop = sharedPreferences.getString("informationUser", "");
            Gson gson = new Gson();
            user = gson.fromJson(jsonShop, User.class);
        } else {
            // Dữ liệu không tồn tại, có thể là người dùng đã đăng xuất hoặc lần đầu sử dụng ứng dụng
        }
        ////////////////////////////////////////////////
        FirebaseApp.initializeApp(this);
        //Khởi tạo db
        firestore = FirebaseFirestore.getInstance();
        context = this;
        setControl();
        setEvent();


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
                Toast.makeText(context, "Chuyển sang màn hình thanh toán", Toast.LENGTH_SHORT).show();
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

//        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                Fragment selectedFragment = null;
//                switch (item.getItemId()) {
//////                    case R.id.navigation_home:
//////                        selectedFragment = new HomeFragment();
//////                        break;
//////                    case R.id.navi:
//////                        selectedFragment = new DashboardFragment();
//////                        break;
//////                    case R.id.navigation_notifications:
//////                        selectedFragment = new NotificationsFragment();
////                        break;
//                }
//                if (selectedFragment != null) {
//                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//                    transaction.replace(R.id.frame_home, selectedFragment);
//                    transaction.commit();
//                }
//                return true;
//            }
//        });
//
//        // Đặt fragment mặc định
//        if (savedInstanceState == null) {
//            //getSupportFragmentManager().beginTransaction().replace(R.id.container, new HomeFragment()).commit();
//        }
    }
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