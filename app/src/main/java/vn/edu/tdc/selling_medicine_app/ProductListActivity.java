package vn.edu.tdc.selling_medicine_app;

import static android.content.ContentValues.TAG;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.tdc.selling_medicine_app.feature.CustomToast;
import vn.edu.tdc.selling_medicine_app.feature.GetCurrentDate;
import vn.edu.tdc.selling_medicine_app.feature.ReceiveUserInfo;
import vn.edu.tdc.selling_medicine_app.model.Product;
import vn.edu.tdc.selling_medicine_app.feature.ReloadSound;
import vn.edu.tdc.selling_medicine_app.feature.SwipeToDelete;
import vn.edu.tdc.selling_medicine_app.model.User;
import vn.edu.tdc.selling_medicine_app.recycleview.ItemProductAdapter;

public class ProductListActivity extends AppCompatActivity {
    private AlertDialog addProductDialog;

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
    private ReloadSound reloadSound;

    private  User user = new User();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        ////////////////////////////////////////////////
        context = this;
        reloadSound = new ReloadSound(this);
        user = ReceiveUserInfo.getUserInfo(context);
        ////////////////////////////////////////////////
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
                reloadSound.playReloadSound();
                getAllProduct();
                swipeRefresh.setRefreshing(false);
            }
        });
        search_productList.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();
                }
                return false;
            }
        });
        setSupportActionBar(toolbar_productList);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void getAllProduct() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Drugs/"+user.getMobileNumber());

        Query query = databaseReference.orderByChild("dateCreated");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear();
                originalProductList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String idDrug = snapshot.getKey();
                    if (idDrug != null) {
                        Product product = snapshot.getValue(Product.class);
                        if (product != null) {
                            product.setIdDrug(idDrug);
                            productList.add(product);
                            originalProductList.add(product);
                        }
                    }
                }

                Collections.sort(productList, new Comparator<Product>() {
                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                    @Override
                    public int compare(Product p1, Product p2) {
                        try {
                            Date date1 = dateFormat.parse(p1.getDateCreated());
                            Date date2 = dateFormat.parse(p2.getDateCreated());
                            // Sắp xếp giảm dần theo ngày
                            return date2.compareTo(date1);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return 0;
                        }
                    }
                });
                itemProductAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error loading data from Firebase: " + databaseError.getMessage());
            }
        });
    }


    private void showAddProductDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog_add_product, null);
        dialogBuilder.setView(dialogView);

        ImageView ivAddMedicine = dialogView.findViewById(R.id.ivAddMedicine);
        EditText drugName = dialogView.findViewById(R.id.drugName);
        EditText price = dialogView.findViewById(R.id.price);
        EditText form = dialogView.findViewById(R.id.form);
        EditText strength = dialogView.findViewById(R.id.strength);
        EditText indications = dialogView.findViewById(R.id.indications);
        EditText dosage = dialogView.findViewById(R.id.dosage);
        EditText sideEffects = dialogView.findViewById(R.id.sideEffects);
        EditText expiryDate = dialogView.findViewById(R.id.expiryDate);
        EditText qtyInventory = dialogView.findViewById(R.id.qtyInventory);

        // Set EditorActionListeners for each EditText
        setEditorActionListener(drugName, price, "Vui lòng nhập tên sản phẩm");
        setEditorActionListener(price, form, "Vui lòng nhập giá");
        setEditorActionListener(form, strength, "Vui lòng nhập dạng thuốc");
        setEditorActionListener(strength, indications, "Vui lòng nhập nồng độ");
        setEditorActionListener(indications, dosage, "Vui lòng nhập chỉ định");
        setEditorActionListener(dosage, sideEffects, "Vui lòng nhập liều dùng");
        setEditorActionListener(sideEffects, expiryDate, "Vui lòng nhập tác dụng phụ");
        setEditorActionListener(expiryDate, qtyInventory, "Vui lòng nhập ngày hết hạn");

        qtyInventory.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (qtyInventory.getText().toString().isEmpty()) {
                    qtyInventory.setError("Vui lòng nhập số lượng tồn kho");
                    qtyInventory.requestFocus();

                } else {
                    hideKeyboard(qtyInventory);
                }
                return true;
            }
            return false;
        });

        dialogBuilder.setTitle("Thêm sản phẩm");
        dialogBuilder.setPositiveButton("Thêm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String nameDrug = drugName.getText().toString().trim();
                String priceDrug = price.getText().toString().trim();
                String formDrug = form.getText().toString().trim();
                String strengthDrug = strength.getText().toString().trim();
                String indicationsDrug = indications.getText().toString().trim();
                String dosageDrug = dosage.getText().toString().trim();
                String sideEffectsDrug = sideEffects.getText().toString().trim();
                String expiryDateDrug = expiryDate.getText().toString().trim();
                String qtyInventoryDrug = qtyInventory.getText().toString().trim();

                // Lấy đường dẫn hình ảnh từ Tag của ImageView
                String imageDrugUrl = ivAddMedicine.getTag() != null ? ivAddMedicine.getTag().toString() : "";

                if (nameDrug.isEmpty() || priceDrug.isEmpty() || formDrug.isEmpty() || strengthDrug.isEmpty() || indicationsDrug.isEmpty()
                        || dosageDrug.isEmpty() || sideEffectsDrug.isEmpty() || expiryDateDrug.isEmpty() || qtyInventoryDrug.isEmpty()) {
                    CustomToast.showToastFailed(context,"Vui lòng nhập đầy đủ thông tin sản phẩm");
                } else {
                    if (imageDrugUrl.isEmpty()) {
                        imageDrugUrl = "https://example.com/default_image.jpg";
                    }

                    String dateCreated = GetCurrentDate.getCurrentDate();

                    addNewProduct(imageDrugUrl, nameDrug, formDrug, strengthDrug,
                            indicationsDrug, dosageDrug, sideEffectsDrug, expiryDateDrug,
                            Integer.parseInt(qtyInventoryDrug), Integer.parseInt(priceDrug), dateCreated);

                    dialog.dismiss();
                    CustomToast.showToastSuccessful(context,"Thêm sản phẩm thành công");
                }
            }
        });
        dialogBuilder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        addProductDialog = dialogBuilder.create();
        addProductDialog.show();
    }

    private void setEditorActionListener(EditText currentEditText, EditText nextEditText, String errorMessage) {
        currentEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                if (currentEditText.getText().toString().isEmpty()) {
                    currentEditText.setError(errorMessage);
                    currentEditText.requestFocus();
                    return true;
                } else {
                    nextEditText.requestFocus();
                    return true;
                }
            }
            return false;
        });
    }


    private void addNewProduct(String imageDrug, String drugName, String form,
                               String strength, String indications, String dosage, String sideEffects,
                               String expiryDate, int qtyInventory, int price, String dateCreated) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Drugs/"+user.getMobileNumber());

        String idDrug = databaseReference.push().getKey();

        if (imageDrug == null || imageDrug.isEmpty()) {
            imageDrug = "https://example.com/default_image.jpg";
        }

        Product newProduct = new Product(idDrug, imageDrug, drugName, form, strength,
                indications, dosage, sideEffects, expiryDate, qtyInventory, price, dateCreated);

        databaseReference.child(idDrug).setValue(newProduct)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Sản phẩm đã được thêm vào Firebase thành công");
                        // Cập nhật danh sách sản phẩm sau khi thêm thành công (nếu cần)
                        getAllProduct();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Lỗi khi thêm sản phẩm vào Firebase", e);
                    }
                });
    }


    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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
        getAllProduct();
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
            showAddProductDialog();
            //finish();
        }
        return super.onOptionsItemSelected(item);
    }
}