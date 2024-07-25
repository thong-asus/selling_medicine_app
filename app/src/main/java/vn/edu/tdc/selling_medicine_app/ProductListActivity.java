package vn.edu.tdc.selling_medicine_app;

import static android.content.ContentValues.TAG;
import static android.view.View.VISIBLE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import vn.edu.tdc.selling_medicine_app.feature.CustomToast;
import vn.edu.tdc.selling_medicine_app.feature.GetCurrentDate;
import vn.edu.tdc.selling_medicine_app.feature.ReceiveUserInfo;
import vn.edu.tdc.selling_medicine_app.model.Product;
import vn.edu.tdc.selling_medicine_app.feature.ReloadSound;
import vn.edu.tdc.selling_medicine_app.feature.SwipeToDelete;
import vn.edu.tdc.selling_medicine_app.model.User;
import vn.edu.tdc.selling_medicine_app.recycleview.Adapter_ItemProduct;

public class ProductListActivity extends AppCompatActivity {
    private AlertDialog addProductDialog;

    private Toolbar toolbar_productList;
    private TextInputEditText search_productList;
    private LinearLayout noDataAvailable;
    private ImageView btn_filter_productList;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recycleview_productList;
    private Context context;
    private List<Product> productList = new ArrayList<>();
    private List<Product> originalProductList = new ArrayList<>();
    private Adapter_ItemProduct itemProductAdapter;
    private ReloadSound reloadSound;

    private User user = new User();
    /////////////////////////////////////////////
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST_CODE = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private ImageView ivMedicine;
    private Uri imageUri = null;


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

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog_filter_products, null);
        builder.setView(dialogView);

        Spinner spinnerSortByProductName = dialogView.findViewById(R.id.spinnerSortByProductName);
        Spinner spinnerSortByDate = dialogView.findViewById(R.id.spinnerSortByDate);
        Spinner spinnerSortByProductExpiry = dialogView.findViewById(R.id.spinnerSortByProductExpiry);
        Spinner spinnerSortByQtySelling = dialogView.findViewById(R.id.spinnerSortByQtySelling);
        Button btnApplyFilter = dialogView.findViewById(R.id.btnApplyFilter);

        ArrayAdapter<CharSequence> adapterName = ArrayAdapter.createFromResource(this,
                R.array.sort_by_name_customer, android.R.layout.simple_spinner_item);
        adapterName.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortByProductName.setAdapter(adapterName);

        ArrayAdapter<CharSequence> adapterDate = ArrayAdapter.createFromResource(this,
                R.array.sort_customer_by_date, android.R.layout.simple_spinner_item);
        adapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortByDate.setAdapter(adapterDate);

        ArrayAdapter<CharSequence> adapterExpiry = ArrayAdapter.createFromResource(this,
                R.array.sort_by_product_expiry_date, android.R.layout.simple_spinner_item);
        adapterExpiry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortByProductExpiry.setAdapter(adapterExpiry);

        ArrayAdapter<CharSequence> adapterQtySelling = ArrayAdapter.createFromResource(this,
                R.array.sort_by_product_selling, android.R.layout.simple_spinner_item);
        adapterQtySelling.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortByQtySelling.setAdapter(adapterQtySelling);

        final AlertDialog dialog = builder.create();

        btnApplyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedNameOption = spinnerSortByProductName.getSelectedItem().toString();
                String selectedDateOption = spinnerSortByDate.getSelectedItem().toString();
                String selectedProductExpiryOption = spinnerSortByProductExpiry.getSelectedItem().toString();
                String selectedQtySellingOption = spinnerSortByQtySelling.getSelectedItem().toString();
                handleFilterSelection(selectedNameOption, selectedDateOption, selectedProductExpiryOption, selectedQtySellingOption);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void handleFilterSelection(String nameOption, String dateOption, String expiryOption, String qtySellingOption) {
        List<Product> filteredProducts = new ArrayList<>(originalProductList);

        // Lọc theo tên sản phẩm
        if (!nameOption.equals("Không chọn")) {
            if (nameOption.equals("Từ A-Z")) {
                Collections.sort(filteredProducts, new Comparator<Product>() {
                    @Override
                    public int compare(Product p1, Product p2) {
                        return p1.getDrugName().compareTo(p2.getDrugName());
                    }
                });
            } else if (nameOption.equals("Từ Z-A")) {
                Collections.sort(filteredProducts, new Comparator<Product>() {
                    @Override
                    public int compare(Product p1, Product p2) {
                        return p2.getDrugName().compareTo(p1.getDrugName());
                    }
                });
            }
        }

        // Lọc theo ngày tạo
        if (!dateOption.equals("Không chọn")) {
            switch (dateOption) {
                case "Mới nhất":
                    Collections.sort(filteredProducts, new Comparator<Product>() {
                        @Override
                        public int compare(Product p1, Product p2) {
                            return p2.getDateCreated().compareTo(p1.getDateCreated());
                        }
                    });
                    break;
                case "Cũ nhất":
                    Collections.sort(filteredProducts, new Comparator<Product>() {
                        @Override
                        public int compare(Product p1, Product p2) {
                            return p1.getDateCreated().compareTo(p2.getDateCreated());
                        }
                    });
                    break;
                case "Hôm nay":
                    filterByDateRange(filteredProducts, "today");
                    break;
                case "Hôm qua":
                    filterByDateRange(filteredProducts, "yesterday");
                    break;
                case "Tuần này":
                    filterByDateRange(filteredProducts, "this_week");
                    break;
                case "Tháng này":
                    filterByDateRange(filteredProducts, "this_month");
                    break;
            }
        }

        // Lọc theo hạn sử dụng sản phẩm
        if (!expiryOption.equals("Không chọn")) {
            filterByProductExpiry(filteredProducts, expiryOption);
        }

        // Lọc theo số lượng bán
        if (!qtySellingOption.equals("Không chọn")) {
            filterByQtySelling(filteredProducts, qtySellingOption);
        }
        if (!filteredProducts.isEmpty()) {
            itemProductAdapter.updateData(filteredProducts);
        } else {
            CustomToast.showToastFailed(context, "Không tìm thấy sản phẩm nào");
        }
    }

    private void filterByDateRange(List<Product> products, String dateRangeType) {
        List<Product> filteredList = new ArrayList<>();
        String currentDate = GetCurrentDate.getCurrentDateTime();
        String[] parts = currentDate.split(" ")[0].split("/");

        int currentDay = Integer.parseInt(parts[0]);
        int currentMonth = Integer.parseInt(parts[1]);
        int currentYear = Integer.parseInt(parts[2]);

        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.DAY_OF_MONTH, currentDay);
        todayCalendar.set(Calendar.MONTH, currentMonth - 1);
        todayCalendar.set(Calendar.YEAR, currentYear);

        Calendar productCalendar = Calendar.getInstance();

        for (Product product : products) {
            String productDate = product.getDateCreated();
            String[] productParts = productDate.split(" ")[0].split("/");

            int productDay = Integer.parseInt(productParts[0]);
            int productMonth = Integer.parseInt(productParts[1]);
            int productYear = Integer.parseInt(productParts[2]);

            productCalendar.set(Calendar.DAY_OF_MONTH, productDay);
            productCalendar.set(Calendar.MONTH, productMonth - 1);
            productCalendar.set(Calendar.YEAR, productYear);

            boolean isInRange = false;

            switch (dateRangeType) {
                case "today":
                    isInRange = isSameDay(todayCalendar, productCalendar);
                    break;
                case "yesterday":
                    isInRange = isYesterday(todayCalendar, productCalendar);
                    break;
                case "this_week":
                    isInRange = isThisWeek(todayCalendar, productCalendar);
                    break;
                case "this_month":
                    isInRange = isThisMonth(todayCalendar, productCalendar);
                    break;
                default:
                    break;
            }

            if (isInRange) {
                filteredList.add(product);
            }
        }

        products.clear();
        products.addAll(filteredList);
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    private boolean isYesterday(Calendar today, Calendar product) {
        Calendar yesterday = (Calendar) today.clone();
        yesterday.add(Calendar.DATE, -1);
        return isSameDay(yesterday, product);
    }

    private boolean isThisWeek(Calendar today, Calendar product) {
        int currentWeek = today.get(Calendar.WEEK_OF_YEAR);
        int productWeek = product.get(Calendar.WEEK_OF_YEAR);
        int currentYear = today.get(Calendar.YEAR);
        int productYear = product.get(Calendar.YEAR);

        return currentYear == productYear && currentWeek == productWeek;
    }

    private boolean isThisMonth(Calendar today, Calendar product) {
        return today.get(Calendar.YEAR) == product.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == product.get(Calendar.MONTH);
    }

    private void filterByProductExpiry(List<Product> products, String expiryOption) {
        List<Product> filteredList = new ArrayList<>();
        Calendar now = Calendar.getInstance();

        for (Product product : products) {
            String expiryDateStr = product.getExpiryDate();
            String[] parts = expiryDateStr.split("/");
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1; // Calendar month is 0-based
            int year = Integer.parseInt(parts[2]);

            Calendar expiryDate = Calendar.getInstance();
            expiryDate.set(year, month, day);

            long diff = expiryDate.getTimeInMillis() - now.getTimeInMillis();
            long days = TimeUnit.MILLISECONDS.toDays(diff);

            boolean isInRange = false;
            switch (expiryOption) {
                case "<1 Tháng":
                    isInRange = days < 30;
                    break;
                case "<3 Tháng":
                    isInRange = days < 90;
                    break;
                case "<6 Tháng":
                    isInRange = days < 180;
                    break;
                case "<1 Năm":
                    isInRange = days < 365;
                    break;
                case ">1 Năm":
                    isInRange = days > 365;
                    break;
            }

            if (isInRange) {
                filteredList.add(product);
            }
        }

        products.clear();
        products.addAll(filteredList);
    }

    private void filterByQtySelling(List<Product> products, String qtySellingOption) {
        List<Product> filteredList = new ArrayList<>(products); // Clone the original list

        switch (qtySellingOption) {
            case "Cao nhất":
                Collections.sort(filteredList, new Comparator<Product>() {
                    @Override
                    public int compare(Product p1, Product p2) {
                        return Integer.compare(p2.getQtySelling(), p1.getQtySelling());
                    }
                });
                break;
            case "Thấp nhất":
                Collections.sort(filteredList, new Comparator<Product>() {
                    @Override
                    public int compare(Product p1, Product p2) {
                        return Integer.compare(p1.getQtySelling(), p2.getQtySelling());
                    }
                });
                break;
            case "<5":
                filteredList.clear();
                for (Product product : products) {
                    if (product.getQtySelling() < 5) {
                        filteredList.add(product);
                    }
                }
                break;
            case "5-10":
                filteredList.clear();
                for (Product product : products) {
                    if (product.getQtySelling() >= 5 && product.getQtySelling() <= 10) {
                        filteredList.add(product);
                    }
                }
                break;
            case ">10":
                filteredList.clear();
                for (Product product : products) {
                    if (product.getQtySelling() > 10) {
                        filteredList.add(product);
                    }
                }
                break;
        }

        products.clear();
        products.addAll(filteredList);
    }


    private void deleteAProduct() {
        SwipeToDelete swipeToDeleteCallback = new SwipeToDelete(itemProductAdapter, this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recycleview_productList);
    }

    private void setEvent() {
        btn_filter_productList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();
            }
        });
        //đổ dữ liệu vào recycleview
        recycleview_productList.setLayoutManager(new LinearLayoutManager(this));
        itemProductAdapter = new Adapter_ItemProduct(productList, this);
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
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Drugs/" + user.getMobileNumber());

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

                if (productList.isEmpty()) {
                    noDataAvailable.setVisibility(VISIBLE);
                } else {
                    noDataAvailable.setVisibility(View.GONE);
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
                }
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

        // Khởi tạo ImageView và các EditText từ dialogView
        ImageView ivAddMedicine = dialogView.findViewById(R.id.ivAddMedicine);
        EditText drugName = dialogView.findViewById(R.id.drugName);
        EditText price = dialogView.findViewById(R.id.price);
        EditText form = dialogView.findViewById(R.id.form);
        EditText strength = dialogView.findViewById(R.id.strength);
        EditText indications = dialogView.findViewById(R.id.indications);
        EditText dosage = dialogView.findViewById(R.id.dosage);
        EditText sideEffects = dialogView.findViewById(R.id.sideEffects);
        EditText expiryDate = dialogView.findViewById(R.id.expiryDate);
        EditText qtyInventory = dialogView.findViewById(R.id.qtySelling);

        ivAddMedicine.setOnClickListener(v -> showImagePickDialog());

        setEditorActionListener(drugName, price, "Vui lòng nhập tên sản phẩm");
        setEditorActionListener(price, form, "Vui lòng nhập giá");
        setEditorActionListener(form, strength, "Vui lòng nhập dạng thuốc");
        setEditorActionListener(strength, indications, "Vui lòng nhập nồng độ");
        setEditorActionListener(indications, dosage, "Vui lòng nhập chỉ định");
        setEditorActionListener(dosage, sideEffects, "Vui lòng nhập liều dùng");
        setEditorActionListener(sideEffects, expiryDate, "Vui lòng nhập tác dụng phụ");

        qtyInventory.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (qtyInventory.getText().toString().isEmpty()) {
                    qtyInventory.setError("Vui lòng nhập số lượng đang bán");
                    qtyInventory.requestFocus();
                } else {
                    hideKeyboard(qtyInventory);
                }
                return true;
            }
            return false;
        });

        dialogBuilder.setTitle("Thêm sản phẩm");
        dialogBuilder.setPositiveButton("Thêm", (dialog, whichButton) -> {
            String nameDrug = drugName.getText().toString().trim();
            String priceDrug = price.getText().toString().trim();
            String formDrug = form.getText().toString().trim();
            String strengthDrug = strength.getText().toString().trim();
            String indicationsDrug = indications.getText().toString().trim();
            String dosageDrug = dosage.getText().toString().trim();
            String sideEffectsDrug = sideEffects.getText().toString().trim();
            String expiryDateDrug = expiryDate.getText().toString().trim();
            String qtyInventoryDrug = qtyInventory.getText().toString().trim();

            String imageDrugUrl = ivAddMedicine.getTag() != null ? ivAddMedicine.getTag().toString() : "";

            if (nameDrug.isEmpty() || priceDrug.isEmpty() || formDrug.isEmpty() || strengthDrug.isEmpty() || indicationsDrug.isEmpty()
                    || dosageDrug.isEmpty() || sideEffectsDrug.isEmpty() || expiryDateDrug.isEmpty() || qtyInventoryDrug.isEmpty()) {
                CustomToast.showToastFailed(context, "Vui lòng nhập đầy đủ thông tin sản phẩm");
            } else {
                if (imageDrugUrl.isEmpty()) {
                    imageDrugUrl = "https://example.com/default_image.jpg";
                }

                String dateCreated = GetCurrentDate.getCurrentDateTime();
                String drudID = FirebaseDatabase.getInstance().getReference("Drugs").push().getKey();

                if (imageUri != null) {
                    uploadImageToFirebase(imageUri, nameDrug, formDrug, strengthDrug, indicationsDrug, dosageDrug, sideEffectsDrug, expiryDateDrug, qtyInventoryDrug, priceDrug, dateCreated);
                } else {
                    addNewProduct(drudID, imageDrugUrl, nameDrug, formDrug, strengthDrug, indicationsDrug, dosageDrug, sideEffectsDrug, expiryDateDrug, Integer.parseInt(qtyInventoryDrug), Integer.parseInt(priceDrug), dateCreated);
                }

                dialog.dismiss();
            }
        });

        dialogBuilder.setNegativeButton("Hủy", (dialog, whichButton) -> dialog.dismiss());

        addProductDialog = dialogBuilder.create();
        addProductDialog.show();
    }

    private void showImagePickDialog() {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn ảnh từ");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    openCamera();
                } else if (which == 1) {
                    openFileChooser();
                }
            }
        });
        builder.show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            // Nếu đã có quyền, mở camera
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            } else {
                CustomToast.showToastFailed(context, "Không thể mở camera");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                CustomToast.showToastFailed(context, "Quyền truy cập camera bị từ chối");
            }
        }
    }


    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if (imageBitmap != null) {
                        imageUri = getImageUri(this, imageBitmap);
                        ImageView ivAddMedicine = addProductDialog.findViewById(R.id.ivAddMedicine);
                        if (ivAddMedicine != null) {
                            ivAddMedicine.setImageBitmap(imageBitmap);
                            ivAddMedicine.setTag(imageUri.toString());
                        }
                    }
                }
            } else if (requestCode == PICK_IMAGE_REQUEST) {
                imageUri = data.getData();
                if (imageUri != null) {
                    ImageView ivAddMedicine = addProductDialog.findViewById(R.id.ivAddMedicine);
                    if (ivAddMedicine != null) {
                        ivAddMedicine.setImageURI(imageUri);
                        ivAddMedicine.setTag(imageUri.toString());
                    }
                }
            }
        }
    }

    private Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    private void uploadImageToFirebase(Uri imageUri, String nameDrug, String formDrug, String strengthDrug, String indicationsDrug, String dosageDrug, String sideEffectsDrug, String expiryDateDrug, String qtyInventoryDrug, String priceDrug, String dateCreated) {
        if (imageUri != null) {
            // Tạo ID tự động cho sản phẩm mới
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Drugs");
            String idDrug = databaseReference.push().getKey();
            if (idDrug == null) {
                CustomToast.showToastFailed(context, "Lỗi tạo ID sản phẩm");
                return;
            }

            StorageReference storageReference = FirebaseStorage.getInstance().getReference("Drugs/" + user.getMobileNumber() + "/" + idDrug);
            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + ".jpg");

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        // Chuyển đổi dữ liệu và thêm sản phẩm vào Firebase Database
                        addNewProduct(idDrug, imageUrl, nameDrug, formDrug, strengthDrug, indicationsDrug, dosageDrug, sideEffectsDrug, expiryDateDrug, Integer.parseInt(qtyInventoryDrug), Integer.parseInt(priceDrug), dateCreated);
                    }))
                    .addOnFailureListener(e -> CustomToast.showToastFailed(context, "Upload thất bại: " + e.getMessage()));
        } else {
            CustomToast.showToastFailed(context, "Không có hình ảnh để tải lên");
        }
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

    private void addNewProduct(String idDrug, String imageDrug, String drugName, String form, String strength, String indications, String dosage, String sideEffects, String expiryDate, int qtyInventory, int price, String dateCreated) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Drugs/" + user.getMobileNumber());

        if (idDrug.isEmpty()) {
            idDrug = databaseReference.push().getKey();
        }

        Product newProduct = new Product(idDrug, imageDrug, drugName, form, strength, indications, dosage, sideEffects, expiryDate, qtyInventory, price, dateCreated);

        databaseReference.child(idDrug).setValue(newProduct)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Sản phẩm đã được thêm vào Firebase thành công");
                    CustomToast.showToastSuccessful(context, "Thêm sản phẩm thành công");
                    getAllProduct();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi thêm sản phẩm vào Firebase", e);
                    //CustomToast.showToastFailed(context, "Thêm sản phẩm thất bại: " + e.getMessage());
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
        noDataAvailable = findViewById(R.id.noDataAvailable);
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