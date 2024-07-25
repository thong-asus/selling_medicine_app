package vn.edu.tdc.selling_medicine_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import vn.edu.tdc.selling_medicine_app.feature.CustomToast;
import vn.edu.tdc.selling_medicine_app.feature.HashUtil;
import vn.edu.tdc.selling_medicine_app.model.User;

public class ForgotPasswordActivity extends AppCompatActivity {

    private View vForgotPassword;
    private TextInputEditText mobileNumber, newPassword, reNewPassword, otpCode;
    private Button btnSendOTP, btnConfirm;
    private LinearLayout linear_timeResend;
    private TextView tvTimeResend;
    private Context context;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference;
    private Long timeoutSeconds = 60L;
    private String verificationCode = "", codesms = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        context = this;
        setControl();
        setEvent();
    }

    private void setEvent() {
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputMobileNumber = mobileNumber.getText().toString().trim();
                String inputNewPassword = newPassword.getText().toString().trim();
                String inputReNewPassword = reNewPassword.getText().toString().trim();
                String inputOTP = otpCode.getText().toString().trim();

                if (inputMobileNumber.isEmpty() || inputNewPassword.isEmpty() || inputReNewPassword.isEmpty() || inputOTP.isEmpty()) {
                    CustomToast.showToastFailed(context, "Vui lòng điền đầy đủ thông tin!");
                    return;
                }

                if (!inputNewPassword.equals(inputReNewPassword)) {
                    CustomToast.showToastFailed(context, "Mật khẩu mới và xác nhận mật khẩu không khớp!");
                    return;
                }

                String hashedNewPassword = HashUtil.hashPassword(inputNewPassword);

                // Xác thực OTP
                PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(codesms, inputOTP);
                mAuth.signInWithCredential(phoneAuthCredential)
                        .addOnCompleteListener(ForgotPasswordActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Xác thực OTP thành công
                                    // Cập nhật mật khẩu mới vào Firebase Database
                                    DatabaseReference userRef = firebaseDatabase.getReference("User/" + inputMobileNumber);
                                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                userRef.child("password").setValue(hashedNewPassword)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> updateTask) {
                                                                if (updateTask.isSuccessful()) {
                                                                    CustomToast.showToastSuccessful(context, "Cập nhật mật khẩu thành công!");
                                                                    Intent intent = new Intent(context, LoginActivity.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                } else {
                                                                    CustomToast.showToastFailed(context, "Lỗi: Không thể cập nhật mật khẩu!");
                                                                }
                                                            }
                                                        });
                                            } else {
                                                CustomToast.showToastFailed(context, "Tài khoản không tồn tại!");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            CustomToast.showToastFailed(context, "Lỗi: Không thể truy cập dữ liệu!");
                                        }
                                    });
                                } else {
                                    CustomToast.showToastFailed(context, "Sai mã OTP!");
                                }
                            }
                        });
            }
        });

        btnSendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mobileNumber.getText().toString().isEmpty()) {
                    mobileNumber.setError("Vui lòng nhập số điện thoại!");
                    CustomToast.showToastFailed(context,"Vui lòng nhập số điện thoại!");
                } else {
                    sentOTP();
                    CustomToast.showToastSuccessful(context, "Đã gửi mã OTP");
                    resendOTP();
                    linear_timeResend.setVisibility(View.VISIBLE);
                }
            }
        });
        mobileNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (mobileNumber.getText().toString().isEmpty()) {
                        mobileNumber.requestFocus();
                        mobileNumber.setError("Vui lòng nhập số điện thoại!");
                    } else {
                        newPassword.requestFocus();
                    }
                }
                return false;
            }
        });
        newPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (newPassword.getText().toString().isEmpty()) {
                        newPassword.requestFocus();
                        newPassword.setError("Vui lòng nhập mật khẩu mới!");
                    } else {
                        reNewPassword.requestFocus();
                    }
                }
                return false;
            }
        });
        reNewPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (reNewPassword.getText().toString().isEmpty()) {
                        reNewPassword.requestFocus();
                        reNewPassword.setError("Vui lòng xác nhận mật khẩu mới!");
                    } else {
                        hideKeyboard();
                    }
                }
                return false;
            }
        });
        vForgotPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });
    }
    void sentOTP() {
        //setInProgress(true);
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber("+84" + mobileNumber.getText().toString().substring(1))
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        verificationCode = phoneAuthCredential.getSmsCode();
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        System.out.println("loi: " + e.getMessage());
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Thông báo");
                        builder.setMessage("Xảy ra lỗi trong quá trình gửi OTP!");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        codesms = s;
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    //Đếm ngược thời gian gửi lại mã
    void resendOTP() {
        btnSendOTP.setEnabled(false);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeoutSeconds--;
                runOnUiThread(() -> {
                    tvTimeResend.setText(timeoutSeconds + " giây");
                    if (timeoutSeconds <= 0) {
                        timeoutSeconds = 60L;
                        timer.cancel();
                        btnSendOTP.setEnabled(true);
                    }
                });
            }
        }, 0, 1000);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void setControl() {
        vForgotPassword = findViewById(R.id.vForgotPassword);
        mobileNumber = findViewById(R.id.mobileNumber);
        newPassword = findViewById(R.id.newPassword);
        reNewPassword = findViewById(R.id.reNewPassword);
        otpCode = findViewById(R.id.otpCode);
        btnSendOTP = findViewById(R.id.btnSendOTP);
        btnConfirm = findViewById(R.id.btnConfirm);
        linear_timeResend = findViewById(R.id.linear_timeResend);
        tvTimeResend = findViewById(R.id.tvTimeResend);
    }
}