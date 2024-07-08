package vn.edu.tdc.selling_medicine_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import vn.edu.tdc.selling_medicine_app.model.Product;
import vn.edu.tdc.selling_medicine_app.model.SwipeToDelete;
import vn.edu.tdc.selling_medicine_app.recycleview.ItemProductAdapter;

public class ProductListActivity extends AppCompatActivity {

    private Toolbar toolbar_productList;
    private TextInputEditText search_productList;
    private ImageView btn_filter_productList;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recycleview_productList;
    private TextView tvNoAvailableProduct;
    private Context context;
    private List<Product> productList = new ArrayList<>();
    private List<Product> originalProductList = new ArrayList<>();
    private ItemProductAdapter itemProductAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        context = this;
        setControl();
        setEvent();
        getAllProduct();
        deleteAProduct();


    }
    private void deleteAProduct() {
        SwipeToDelete swipeToDeleteCallback = new SwipeToDelete(itemProductAdapter, this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recycleview_productList);
    }
    private void setEvent() {
        //đổ dữ liệu vào recycleview
        recycleview_productList.setLayoutManager(new LinearLayoutManager(this));
        itemProductAdapter = new ItemProductAdapter(productList, this);
        recycleview_productList.setAdapter(itemProductAdapter);
        itemProductAdapter.notifyDataSetChanged();


        search_productList.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchProduct(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAllProduct();
                swipeRefresh.setRefreshing(false);
            }
        });
        setSupportActionBar(toolbar_productList);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void getAllProduct() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Drugs");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear();
                originalProductList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String idDrug = snapshot.getKey();
                    if (idDrug != null) {
                        Product product = snapshot.getValue(Product.class);
                        if (product != null) {
                            // Thiết lập idDrug từ key của snapshot
                            product.setIdDrug(idDrug);
                            productList.add(product);
                            originalProductList.add(product);
                        }
                    }
                }
                itemProductAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error loading data from Firebase: " + databaseError.getMessage());
            }
        });
    }


    private void searchProduct(String searchText) {
        ArrayList<Product> filteredProductList = new ArrayList<>();
        if (searchText.isEmpty()) {
            filteredProductList.addAll(originalProductList);
        } else {
            for (Product product : originalProductList) {
                if (product.getDrugName().toLowerCase().contains(searchText.toLowerCase()) ||
                        product.getIndications().toLowerCase().contains(searchText.toLowerCase())) {

                    filteredProductList.add(product);
                }
            }
        }
        itemProductAdapter.updateData(filteredProductList);
    }

    private void setControl() {
        toolbar_productList = findViewById(R.id.toolbar_productList);
        search_productList = findViewById(R.id.search_productList);
        btn_filter_productList = findViewById(R.id.btn_filter_productList);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        recycleview_productList = findViewById(R.id.recycleview_productList);
        tvNoAvailableProduct = findViewById(R.id.tvNoAvailableProduct);
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
            //showAddCustomerDialog();
            //finish();
        }
        return super.onOptionsItemSelected(item);
    }
}