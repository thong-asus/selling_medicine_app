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

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import vn.edu.tdc.selling_medicine_app.feature.CustomToast;
import vn.edu.tdc.selling_medicine_app.feature.HashUtil;
import vn.edu.tdc.selling_medicine_app.feature.ShowMessage;
import vn.edu.tdc.selling_medicine_app.model.User;

public class LoginActivity extends AppCompatActivity {

    private View vLogin;
    private TextInputEditText mobileNumber, password;
    private Button btnLogin, btnRegister;
    private TextView tvForgotPassword;
    private Context context;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //////////////////////////////////////////////
        FirebaseApp.initializeApp(this);
        //////////////////////////////////////////////
        context = this;
        setControl();
        setEvent();
    }

    private void setEvent() {
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputMobileNumber = mobileNumber.getText().toString().trim();
                String inputPassword = password.getText().toString().trim();

                if (inputMobileNumber.isEmpty() || inputPassword.isEmpty()) {
                    CustomToast.showToastFailed(context, "Vui lòng điền thông tin đăng nhập!");
                    return;
                }

                databaseReference = firebaseDatabase.getReference("User/" + inputMobileNumber);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);
                            String hashedInputPassword = HashUtil.hashPassword(inputPassword);

                            if (user != null && user.getPassword().equals(hashedInputPassword)) {
                                // Đăng nhập thành công
                                CustomToast.showToastSuccessful(context, "Đăng nhập thành công ^^");

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
                                CustomToast.showToastFailed(context, "Sai mật khẩu!");
                            }
                        } else {
                            CustomToast.showToastFailed(context, "Tài khoản không tồn tại!");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        CustomToast.showToastFailed(context, "Lỗi không thể truy vấn!");
                    }
                });
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, RegisterActivity.class);
                startActivity(intent);
                finish();
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