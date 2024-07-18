package vn.edu.tdc.selling_medicine_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import vn.edu.tdc.selling_medicine_app.feature.CustomToast;
import vn.edu.tdc.selling_medicine_app.feature.FormatNumber;
import vn.edu.tdc.selling_medicine_app.feature.ReceiveUserInfo;
import vn.edu.tdc.selling_medicine_app.model.Product;
import vn.edu.tdc.selling_medicine_app.model.User;

public class DetailProductActivity extends AppCompatActivity {

    private Toolbar toolbar_DetailProduct;
    private ImageView ivMedicine;
    private TextView qtySelling,expiryDate,sideEffects,dosage,indications,strength,form,price,drugName;
    private Product product = new Product();
    private Context context;
    private String idDrug;
    private AlertDialog editProductDialog;
    private  User user = new User();
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
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Drugs/"+user.getMobileNumber()).child(idDrug);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Product product = dataSnapshot.getValue(Product.class);
                if (product != null) {
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

                String _imageDrugUrl = product.getImageDrug();

                if (_drugName.isEmpty() || _price.isEmpty() || _form.isEmpty() || _strength.isEmpty() ||
                        _indications.isEmpty() || _dosage.isEmpty() || _sideEffects.isEmpty() || _expiryDate.isEmpty() || _qtySelling.isEmpty()) {
                    CustomToast.showToastFailed(context,"Vui lòng nhập đầy đủ thông tin sản phẩm");
                } else {
                    editInfoProduct(_imageDrugUrl,_drugName,_price,_form,_strength,_indications,_dosage,_sideEffects,_expiryDate,_qtySelling);
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
    private void editInfoProduct(String newDrugImage, String newDrugName, String newPrice, String newForm, String newStrength, String newIndications, String newDosage, String newSideEffects, String newExpiryDate, String newQtySelling) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Drugs/"+user.getMobileNumber());
        databaseReference.child(product.getIdDrug()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Product productToUpdate = snapshot.getValue(Product.class);
                    productToUpdate.setImageDrug(newDrugImage);
                    productToUpdate.setDrugName(newDrugName);
                    productToUpdate.setPrice(Integer.parseInt(newPrice));
                    productToUpdate.setForm(newForm);
                    productToUpdate.setStrength(newStrength);
                    productToUpdate.setIndications(newIndications);
                    productToUpdate.setDosage(newDosage);
                    productToUpdate.setSideEffects(newSideEffects);
                    productToUpdate.setExpiryDate(newExpiryDate);
                    productToUpdate.setQtySelling(Integer.parseInt(newQtySelling));

                    databaseReference.child(product.getIdDrug()).setValue(productToUpdate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    CustomToast.showToastSuccessful(context,"Cập nhật thông tin sản phẩm thành công");
                                    showNewData(newDrugName, newPrice, newForm, newStrength, newIndications, newDosage, newSideEffects, newExpiryDate, newQtySelling);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    CustomToast.showToastFailed(context,"Lỗi cập nhật thông tin sản phẩm");
                                }
                            });
                } else {
                    CustomToast.showToastFailed(context,"Không tìm thấy sản phẩm để cập nhật");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                CustomToast.showToastFailed(context,"Lỗi cập nhật thông tin sản phẩm");
            }
        });
    }

    private void showNewData(String drugNameText, String priceText, String formText, String strengthText,
                             String indicationsText, String dosageText, String sideEffectsText,
                             String expiryDateText, String qtySellingText) {
        drugName.setText(drugNameText);
        form.setText(formText);
        strength.setText(strengthText);
        indications.setText(indicationsText);
        dosage.setText(dosageText);
        sideEffects.setText(sideEffectsText);
        expiryDate.setText(expiryDateText);
        qtySelling.setText(qtySellingText);

        // Format và hiển thị giá sản phẩm
        int priceValue = Integer.parseInt(priceText);
        price.setText(FormatNumber.formatNumber(priceValue) + " VND");
        int qtySellingValue = Integer.parseInt(qtySellingText);
        qtySelling.setText(FormatNumber.formatNumber(qtySellingValue));

        product.setDrugName(drugNameText);
        product.setPrice(priceValue);
        product.setForm(formText);
        product.setStrength(strengthText);
        product.setIndications(indicationsText);
        product.setDosage(dosageText);
        product.setSideEffects(sideEffectsText);
        product.setExpiryDate(expiryDateText);
        product.setQtySelling(Integer.parseInt(qtySellingText));
    }

    private void setControl() {
        toolbar_DetailProduct = findViewById(R.id.toolbar_DetailProduct);
        ivMedicine = findViewById(R.id.ivMedicine);
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