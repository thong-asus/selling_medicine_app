package vn.edu.tdc.selling_medicine_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;

import vn.edu.tdc.selling_medicine_app.feature.CustomToast;
import vn.edu.tdc.selling_medicine_app.feature.FormatNumber;
import vn.edu.tdc.selling_medicine_app.feature.ReceiveUserInfo;
import vn.edu.tdc.selling_medicine_app.model.Product;
import vn.edu.tdc.selling_medicine_app.model.User;

public class DetailProductActivity extends AppCompatActivity {

    private Toolbar toolbar_DetailProduct;
    private ImageView ivItemProduct;
    private TextView qtySelling, expiryDate, sideEffects, dosage, indications, strength, form, price, drugName;
    private Product product = new Product();
    private Context context;
    private String idDrug;
    private AlertDialog editProductDialog;
    private User user = new User();
    /////////////////////////////////////////////////////////////
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST_CODE = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_product);

        //Nhận thông tin User
        context = this;
        user = ReceiveUserInfo.getUserInfo(context);

        //Nhận dữ liệu thông tin sản phẩm
        Intent intent = getIntent();
        if (intent.hasExtra("productInfo")) {
            product = (Product) intent.getSerializableExtra("productInfo");
            idDrug = getIntent().getStringExtra("idDrug");
        }

        setControl();
        setEvent();
        getProductDetails(idDrug);
    }

    private void setEvent() {
        setSupportActionBar(toolbar_DetailProduct);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    private void getProductDetails(String idDrug) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Drugs/" + user.getMobileNumber()).child(idDrug);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Product product = dataSnapshot.getValue(Product.class);
                if (product != null) {

                    //////////////////tải hình ảnh
                    String imageUrl = product.getImageDrug();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(context)
                                .load(imageUrl)
                                .apply(new RequestOptions().placeholder(R.drawable.loading)
                                        .error(R.drawable.loadingerror))
                                .into(ivItemProduct);
                    } else {
                        ivItemProduct.setImageResource(R.drawable.loading);
                    }
                    ////////////////////////////////////////////////////////
                    drugName.setText(product.getDrugName());
                    indications.setText(product.getIndications());
                    expiryDate.setText(product.getExpiryDate());
                    price.setText(FormatNumber.formatNumber(product.getPrice()) + " VND");
                    form.setText(product.getForm());
                    strength.setText(product.getStrength());
                    dosage.setText(product.getDosage());
                    sideEffects.setText(product.getSideEffects());
                    qtySelling.setText(FormatNumber.formatNumber(product.getQtySelling()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error loading product details: " + databaseError.getMessage());
            }
        });
    }

    private void showEditProductDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog_edit_product, null);
        dialogBuilder.setView(dialogView);

        ImageView ivEditMedicine = dialogView.findViewById(R.id.ivEditMedicine);
        EditText drugNameEditText = dialogView.findViewById(R.id.drugName);
        EditText priceEditText = dialogView.findViewById(R.id.price);
        EditText formEditText = dialogView.findViewById(R.id.form);
        EditText strengthEditText = dialogView.findViewById(R.id.strength);
        EditText indicationsEditText = dialogView.findViewById(R.id.indications);
        EditText dosageEditText = dialogView.findViewById(R.id.dosage);
        EditText sideEffectsEditText = dialogView.findViewById(R.id.sideEffects);
        EditText expiryDateEditText = dialogView.findViewById(R.id.expiryDate);
        EditText qtySellingEditText = dialogView.findViewById(R.id.qtySelling);

        drugNameEditText.setText(product.getDrugName());
        priceEditText.setText(String.valueOf(product.getPrice()));
        formEditText.setText(product.getForm());
        strengthEditText.setText(product.getStrength());
        indicationsEditText.setText(product.getIndications());
        dosageEditText.setText(product.getDosage());
        sideEffectsEditText.setText(product.getSideEffects());
        expiryDateEditText.setText(product.getExpiryDate());
        qtySellingEditText.setText(String.valueOf(product.getQtySelling()));

        Glide.with(this)
                .load(product.getImageDrug())
                .placeholder(R.drawable.loading)
                .error(R.drawable.loadingerror)
                .into(ivEditMedicine);

        ivEditMedicine.setOnClickListener(v -> {
            showImagePickDialog();
        });

        ivEditMedicine.setOnClickListener(v -> showImagePickDialog());

        dialogBuilder.setTitle("Cập nhật thông tin sản phẩm");
        dialogBuilder.setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String _drugName = drugNameEditText.getText().toString().trim();
                String _price = priceEditText.getText().toString().trim();
                String _form = formEditText.getText().toString().trim();
                String _strength = strengthEditText.getText().toString().trim();
                String _indications = indicationsEditText.getText().toString().trim();
                String _dosage = dosageEditText.getText().toString().trim();
                String _sideEffects = sideEffectsEditText.getText().toString().trim();
                String _expiryDate = expiryDateEditText.getText().toString().trim();
                String _qtySelling = qtySellingEditText.getText().toString().trim();

                if (_drugName.isEmpty() || _price.isEmpty() || _form.isEmpty() || _strength.isEmpty() ||
                        _indications.isEmpty() || _dosage.isEmpty() || _sideEffects.isEmpty() || _expiryDate.isEmpty() || _qtySelling.isEmpty()) {
                    CustomToast.showToastFailed(context, "Vui lòng nhập đầy đủ thông tin sản phẩm");
                } else {
                    if (imageUri != null) {
                        // Nếu có hình ảnh mới được chọn, upload lên Firebase và cập nhật thông tin sản phẩm
                        uploadImageToFirebase(imageUri, _drugName, _form, _strength, _indications, _dosage, _sideEffects, _expiryDate, _qtySelling, _price);
                        CustomToast.showToastSuccessful(context, "Cập nhật thông tin sản phẩm thành công");
                    } else {
                        // Nếu không có hình ảnh mới được chọn, chỉ cập nhật thông tin sản phẩm
                        editInfoProduct(product.getImageDrug(), _drugName, _form, _strength, _indications, _dosage, _sideEffects, _expiryDate, _qtySelling, _price);
                        CustomToast.showToastSuccessful(context, "Cập nhật thông tin sản phẩm thành công");
                    }
                }
            }
        });
        dialogBuilder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        editProductDialog = dialogBuilder.create();
        editProductDialog.show();

    }

    private void editInfoProduct(String newDrugImage, String newDrugName, String newForm, String newStrength, String newIndications, String newDosage, String newSideEffects, String newExpiryDate, String newQtySelling, String newPrice) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Drugs/" + user.getMobileNumber());
        databaseReference.child(product.getIdDrug()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Product productToUpdate = snapshot.getValue(Product.class);
                    productToUpdate.setImageDrug(newDrugImage);
                    productToUpdate.setDrugName(newDrugName);
                    productToUpdate.setForm(newForm);
                    productToUpdate.setStrength(newStrength);
                    productToUpdate.setIndications(newIndications);
                    productToUpdate.setDosage(newDosage);
                    productToUpdate.setSideEffects(newSideEffects);
                    productToUpdate.setExpiryDate(newExpiryDate);
                    productToUpdate.setQtySelling(Integer.parseInt(newQtySelling));
                    productToUpdate.setPrice(Integer.parseInt(newPrice));


                    databaseReference.child(product.getIdDrug()).setValue(productToUpdate)
                            .addOnSuccessListener(aVoid -> {
                                //CustomToast.showToastSuccessful(context, "Cập nhật thông tin sản phẩm thành công");
                                showNewDataProduct(productToUpdate);
                            })
                            .addOnFailureListener(e -> CustomToast.showToastFailed(context, "Lỗi cập nhật thông tin sản phẩm"));
                } else {
                    CustomToast.showToastFailed(context, "Không tìm thấy sản phẩm để cập nhật");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                CustomToast.showToastFailed(context, "Lỗi cập nhật thông tin sản phẩm");
            }
        });
    }

    private void showNewDataProduct(Product productToUpdate) {
        drugName.setText(productToUpdate.getDrugName());
        price.setText(FormatNumber.formatNumber(productToUpdate.getPrice()) + " VND");
        form.setText(productToUpdate.getForm());
        strength.setText(productToUpdate.getStrength());
        indications.setText(productToUpdate.getIndications());
        dosage.setText(productToUpdate.getDosage());
        sideEffects.setText(productToUpdate.getSideEffects());
        expiryDate.setText(productToUpdate.getExpiryDate());
        qtySelling.setText(FormatNumber.formatNumber(productToUpdate.getQtySelling()));


        Glide.with(this).load(productToUpdate.getImageDrug()).into(ivItemProduct);
    }

    ////////////////////////////////UPLOAD IMAGE
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
                        ImageView ivEditMedicine = editProductDialog.findViewById(R.id.ivEditMedicine);
                        if (ivEditMedicine != null) {
                            ivEditMedicine.setImageBitmap(imageBitmap);
                            ivEditMedicine.setTag(imageUri.toString());
                        }
                    }
                }
            } else if (requestCode == PICK_IMAGE_REQUEST) {
                imageUri = data.getData();
                if (imageUri != null) {
                    ImageView ivEditMedicine = editProductDialog.findViewById(R.id.ivEditMedicine);
                    if (ivEditMedicine != null) {
                        ivEditMedicine.setImageURI(imageUri);
                        ivEditMedicine.setTag(imageUri.toString());
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

    private void uploadImageToFirebase(Uri imageUri, String nameDrug, String formDrug, String strengthDrug, String indicationsDrug, String dosageDrug, String sideEffectsDrug, String expiryDateDrug, String qtyInventoryDrug, String priceDrug) {
        if (imageUri != null) {
            String idDrug = product.getIdDrug(); // Sử dụng ID của sản phẩm hiện tại
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("Drugs/" + user.getMobileNumber() + "/" + idDrug);
            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + ".jpg");

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        // Chuyển đổi dữ liệu và thêm sản phẩm vào Firebase Database
                        editInfoProduct(imageUrl, nameDrug, formDrug, strengthDrug, indicationsDrug, dosageDrug, sideEffectsDrug, expiryDateDrug, qtyInventoryDrug, priceDrug);
                    }))
                    .addOnFailureListener(e -> CustomToast.showToastFailed(context, "Upload thất bại: " + e.getMessage()));
        } else {
            CustomToast.showToastFailed(context, "Không có hình ảnh để tải lên");
        }
    }


//    private void uploadImageToFirebase(Uri imageUri, String nameDrug, String formDrug, String strengthDrug, String indicationsDrug, String dosageDrug, String sideEffectsDrug, String expiryDateDrug, String qtyInventoryDrug, String priceDrug) {
//        if (imageUri != null) {
//            // Tạo ID tự động cho sản phẩm mới
//            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Drugs");
//            String idDrug = databaseReference.push().getKey();
//            if (idDrug == null) {
//                CustomToast.showToastFailed(context, "Lỗi tạo ID sản phẩm");
//                return;
//            }
//
//            StorageReference storageReference = FirebaseStorage.getInstance().getReference("Drugs/"+ user.getMobileNumber() +"/"+ idDrug);
//            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + ".jpg");
//
//            fileReference.putFile(imageUri)
//                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
//                        String imageUrl = uri.toString();
//                        // Chuyển đổi dữ liệu và thêm sản phẩm vào Firebase Database
//                        editInfoProduct(imageUrl, nameDrug, formDrug, strengthDrug, indicationsDrug, dosageDrug, sideEffectsDrug, expiryDateDrug, Integer.parseInt(qtyInventoryDrug), Integer.parseInt(priceDrug));
//                    }))
//                    .addOnFailureListener(e -> CustomToast.showToastFailed(context, "Upload thất bại: " + e.getMessage()));
//        } else {
//            CustomToast.showToastFailed(context, "Không có hình ảnh để tải lên");
//        }
//    }
    ///////////////////////////////////////////////

    private void setControl() {
        toolbar_DetailProduct = findViewById(R.id.toolbar_DetailProduct);
        ivItemProduct = findViewById(R.id.ivItemProduct);
        qtySelling = findViewById(R.id.qtySelling);
        expiryDate = findViewById(R.id.expiryDate);
        sideEffects = findViewById(R.id.sideEffects);
        dosage = findViewById(R.id.dosage);
        indications = findViewById(R.id.indications);
        strength = findViewById(R.id.strength);
        form = findViewById(R.id.form);
        price = findViewById(R.id.price);
        drugName = findViewById(R.id.drugName);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.menu_edit) {
            showEditProductDialog();
            //finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return super.onPrepareOptionsMenu(menu);
    }
}