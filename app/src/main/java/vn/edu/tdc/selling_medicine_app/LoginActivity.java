package vn.edu.tdc.selling_medicine_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import vn.edu.tdc.selling_medicine_app.model.Customer;
import vn.edu.tdc.selling_medicine_app.model.ShowMessage;
import vn.edu.tdc.selling_medicine_app.model.User;

public class LoginActivity extends AppCompatActivity {

    View vLogin;
    TextInputEditText mobileNumber, password;
    Button btnLogin, btnRegister;
    TextView tvForgotPassword;
    Context context;


    FirebaseFirestore firestore;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //////////////////////////////////////////////
        FirebaseApp.initializeApp(this);
        //Khởi tạo db
        firestore = FirebaseFirestore.getInstance();
        //////////////////////////////////////////////
        context = this;
        setControl();
        setEvent();
    }

    private void setEvent() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputMobileNumber = mobileNumber.getText().toString().trim();
                String inputPassword = password.getText().toString().trim();

                if (inputMobileNumber.isEmpty() || inputPassword.isEmpty()) {
                    ShowMessage.showMessage(context, "Vui lòng điền thông tin đăng nhập!");
                    return;
                }
                databaseReference = firebaseDatabase.getReference("User/" + inputMobileNumber);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            ///////////MỘT ĐỐI TƯỢNG USER///////////////
                            User user = snapshot.getValue(User.class);
                            if(user.getPassword().equals(inputPassword)){
                                //Đăng nhập thành công
                                //ShowMessage.showMessage(context,"Đăng nhập thành công");
                                SharedPreferences sharedPreferences = getSharedPreferences("informationUser", Context.MODE_PRIVATE);
                                Gson gsonUser = new Gson();
                                String jsonUser = gsonUser.toJson(user);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("informationUser", jsonUser);
                                editor.putString("mobileNumber", inputMobileNumber);
                                editor.apply();
                                Intent intent = new Intent(context, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                ShowMessage.showMessage(context, "Sai mật khẩu!");
                            }
                        } else  {
                            ShowMessage.showMessage(context, "Tài khoản không tồn tại!");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        ShowMessage.showMessage(context, "Lỗi không thể truy vấn!");
                    }
                });
                //////////////////////////////////////////////////////////////////////////////////////////

//                DocumentReference documentReference = firestore.collection("User").document(inputMobileNumber);
//                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if (task.isSuccessful()) {
//                            DocumentSnapshot document = task.getResult();
//                            if (document.exists()) {
//                                ///////////MỘT ĐỐI TƯỢNG USER///////////////
//                                User user = document.toObject(User.class);
//                                String storedPassword = document.getString("password");
//                                if (storedPassword != null && storedPassword.equals(inputPassword)) {
//                                    //Đăng nhập thành công
//                                    //ShowMessage.showMessage(context,"Đăng nhập thành công");
//                                    SharedPreferences sharedPreferences = getSharedPreferences("informationUser", Context.MODE_PRIVATE);
//                                    Gson gsonUser = new Gson();
//                                    String jsonUser = gsonUser.toJson(user);
//                                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                                    editor.putString("informationUser", jsonUser);
//                                    editor.putString("mobileNumber", inputMobileNumber);
//                                    editor.apply();
//                                    Intent intent = new Intent(context, HomeActivity.class);
//                                    startActivity(intent);
//                                    finish();
//                                } else {
//                                    ShowMessage.showMessage(context, "Sai mật khẩu!");
//                                }
//                            } else {
//                                ShowMessage.showMessage(context, "Tài khoản không tồn tại!");
//                            }
//                        } else {
//                            ShowMessage.showMessage(context, "Lỗi không thể truy vấn!");
//                        }
//                    }
//                });
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, RegisterActivity.class);
                startActivity(intent);
            }
        });
        mobileNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    if (mobileNumber.getText().toString().isEmpty()) {
                        mobileNumber.selectAll();
                        mobileNumber.requestFocus();
                        mobileNumber.setError("Vui lòng nhập số điện thoại!");
                    } else {
                        password.requestFocus();
                    }
                }
                return false;
            }
        });
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    if (password.getText().toString().isEmpty()) {
                        password.selectAll();
                        password.requestFocus();
                        password.setError("Vui lòng nhập mật khẩu!");
                    }
                }
                return false;
            }
        });
        vLogin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });
    }

    private void setControl() {
        vLogin = findViewById(R.id.vLogin);
        mobileNumber = findViewById(R.id.mobileNumber);
        password = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}