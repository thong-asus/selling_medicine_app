package vn.edu.tdc.selling_medicine_app;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.RotateDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.github.ybq.android.spinkit.style.Circle;
import com.github.ybq.android.spinkit.style.FadingCircle;
import com.github.ybq.android.spinkit.style.RotatingCircle;
import com.github.ybq.android.spinkit.style.WanderingCubes;
import com.github.ybq.android.spinkit.style.Wave;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import vn.edu.tdc.selling_medicine_app.feature.CustomToast;
import vn.edu.tdc.selling_medicine_app.feature.FcmTokenManager;
import vn.edu.tdc.selling_medicine_app.feature.HashUtil;
import vn.edu.tdc.selling_medicine_app.feature.NetworkChangeReceiver;
import vn.edu.tdc.selling_medicine_app.feature.NetworkUtil;
import vn.edu.tdc.selling_medicine_app.feature.ReceiveUserInfo;
import vn.edu.tdc.selling_medicine_app.model.User;

public class LoginActivity extends AppCompatActivity {

    private View vLogin;
    private TextInputEditText mobileNumber, password;
    private Button btnLogin, btnRegister;
    private TextView tvForgotPassword;
    private Context context;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference;

    private NetworkChangeReceiver networkChangeReceiver;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //////////////////////////////////////////////
        FirebaseApp.initializeApp(this);
        //////////////////////////////////////////////
        context = this;
        setControl();

        progressBar.setIndeterminateDrawable(new WanderingCubes());
        //progressBar.setDrawingCacheBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));


        networkChangeReceiver = new NetworkChangeReceiver();
        if (!NetworkUtil.isNetworkAvailable(context)) {
            CustomToast.showToastFailed(context, "Không có kết nối internet!!!");
        }
        setEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);
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
                progressBar.setVisibility(View.VISIBLE);
                String inputMobileNumber = mobileNumber.getText().toString().trim();
                String inputPassword = password.getText().toString().trim();

                if (inputMobileNumber.isEmpty() || inputPassword.isEmpty()) {
                    CustomToast.showToastFailed(context, "Vui lòng điền thông tin đăng nhập!");
                    progressBar.setVisibility(GONE);
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
                                progressBar.setVisibility(GONE);
                                SharedPreferences sharedPreferences = getSharedPreferences("informationUser", Context.MODE_PRIVATE);
                                Gson gsonUser = new Gson();
                                String jsonUser = gsonUser.toJson(user);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("informationUser", jsonUser);
                                editor.putString("mobileNumber", inputMobileNumber);
                                editor.apply();


                                ReceiveUserInfo.saveUserInfo(context, user);

                                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        String newFcmToken = task.getResult();
                                        if (newFcmToken != null) {

                                            FcmTokenManager.saveFcmToken(context, newFcmToken);

                                            FcmTokenManager.saveTokenOnFireBase(context, user.getMobileNumber(), newFcmToken);
                                            SharedPreferences sharedPreferencesFCM = context.getSharedPreferences("fcmTokenPrefs", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editorFCM = sharedPreferencesFCM.edit();
                                            editorFCM.putString("fcmToken", newFcmToken);
                                            editorFCM.apply();
                                        }
                                    } else {
                                        Log.w(TAG, "Fetching FCM token failed", task.getException());
                                    }
                                });


                                Intent intent = new Intent(context, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                CustomToast.showToastFailed(context, "Sai mật khẩu!");
                                progressBar.setVisibility(GONE);
                            }
                        } else {
                            CustomToast.showToastFailed(context, "Tài khoản không tồn tại!");
                            progressBar.setVisibility(GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        CustomToast.showToastFailed(context, "Lỗi không thể truy vấn!");
                        progressBar.setVisibility(GONE);
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
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}