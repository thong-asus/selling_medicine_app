package vn.edu.tdc.selling_medicine_app;

import static android.widget.Toast.LENGTH_SHORT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import vn.edu.tdc.selling_medicine_app.feature.CustomToast;
import vn.edu.tdc.selling_medicine_app.feature.HashUtil;
import vn.edu.tdc.selling_medicine_app.model.User;

public class RegisterActivity extends AppCompatActivity {
    private View vRegister;
    private LinearLayout linear_timeResend;
    private TextView tvAlreadyHaveAnAccount, tvTimeResend;
    private TextInputEditText mobileNumber, fullname, password, rePassword, otpCode;
    private Button btnSendOTP, btnRegister;
    private Context context;
    ///////////////////////////////////////////
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private Long timeoutSeconds = 60L;
    private String verificationCode = "", codesms = "";
    private User user = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        context = this;
        setControl();
        setEvent();
    }

    private void setEvent() {
        btnSendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mobileNumber.getText().toString().isEmpty()) {
                    mobileNumber.setError("Vui lòng nhập số điện thoại!");
                } else {
                    sentOTP();
                    CustomToast.showToastSuccessful(context,"Đã gửi mã OTP");
                    resendOTP();
                    linear_timeResend.setVisibility(View.VISIBLE);
                }
            }
        });

        mobileNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnSendOTP.setEnabled(!s.toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobile = mobileNumber.getText().toString().trim();
                String name = fullname.getText().toString().trim();
                String password1 = password.getText().toString().trim();
                String rePassword1 = rePassword.getText().toString().trim();
                String otp = otpCode.getText().toString().trim();

                if (mobile.isEmpty() || name.isEmpty() || password1.isEmpty() || rePassword1.isEmpty() || otp.isEmpty()) {
                    CustomToast.showToastFailed(context, "Vui lòng điền đầy đủ thông tin đăng ký!!!");
                    return;
                }

                if (!password1.equals(rePassword1)) {
                    CustomToast.showToastFailed(context, "Mật khẩu và xác nhận mật khẩu không khớp!");
                    return;
                }

                try {
                    PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(codesms, otp);
                    if (phoneAuthCredential != null) {
                        registrationSuccessful(phoneAuthCredential);
                    } else {
                        CustomToast.showToastFailed(context, "Sai mã OTP. Vui lòng thử lại!");
                    }
                } catch (Exception e) {
                    Log.d("Lỗi xác minh OTP:", e.getMessage());
                    //CustomToast.showToastFailed(context, "Lỗi xác minh OTP: " + e.getMessage());
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
                        fullname.requestFocus();
                    }
                }
                return false;
            }
        });

        fullname.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (fullname.getText().toString().isEmpty()) {
                        fullname.requestFocus();
                        fullname.setError("Vui lòng nhập họ tên!");
                    } else {
                        password.requestFocus();
                    }
                }
                return false;
            }
        });

        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (password.getText().toString().isEmpty()) {
                        password.requestFocus();
                        password.setError("Vui lòng nhập mật khẩu!");
                    } else {
                        rePassword.requestFocus();
                    }
                }
                return false;
            }
        });

        rePassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (rePassword.getText().toString().isEmpty()) {
                        rePassword.requestFocus();
                        rePassword.setError("Vui lòng xác nhận mật khẩu!");
                    } else {
                        otpCode.requestFocus();
                    }
                }
                return false;
            }
        });

        tvAlreadyHaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        vRegister.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });

    }

    void registration() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("User");

        String mobileNumberText = mobileNumber.getText().toString();
        String fullnameText = fullname.getText().toString();
        String passwordText = password.getText().toString();
        String fcmTokenNull = "";


        String hashedPassword = HashUtil.hashPassword(passwordText);

        User user = new User();
        user.setMobileNumber(mobileNumberText);
        user.setFullname(fullnameText);
        user.setPassword(hashedPassword);
        user.setFcmToken(fcmTokenNull);

        databaseReference.child(mobileNumber.getText().toString()).setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Thành công","Đăng ký thành công");
                        } else {
                            Log.d("Lỗi","Đăng ký thất bại");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Lỗi đăng ký", e.getMessage());
                    }
                });
    }

    void registrationSuccessful(PhoneAuthCredential phoneAuthCredential) {
        mAuth.signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            CustomToast.showToastSuccessful(context,"Xác minh tài khoản thành công ^^");
                            registration();
                            //////////////////////// CHUYỂN SANG MÀN HÌNH LOGIN ////////////////////////
                            Intent intent = new Intent(context, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            CustomToast.showToastFailed(context,"Sai mã OTP!!!");
                        }
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
        vRegister = findViewById(R.id.vRegister);
        tvAlreadyHaveAnAccount = findViewById(R.id.tvAlreadyHaveAnAccount);
        tvTimeResend = findViewById(R.id.tvTimeResend);
        linear_timeResend = findViewById(R.id.linear_timeResend);
        btnRegister = findViewById(R.id.btnRegister);
        btnSendOTP = findViewById(R.id.btnSendOTP);
        otpCode = findViewById(R.id.otpCode);
        rePassword = findViewById(R.id.rePassword);
        password = findViewById(R.id.password);
        fullname = findViewById(R.id.fullname);
        mobileNumber = findViewById(R.id.mobileNumber);
    }
}