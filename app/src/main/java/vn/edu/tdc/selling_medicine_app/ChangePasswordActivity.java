package vn.edu.tdc.selling_medicine_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import vn.edu.tdc.selling_medicine_app.feature.CustomToast;
import vn.edu.tdc.selling_medicine_app.feature.HashUtil;
import vn.edu.tdc.selling_medicine_app.feature.ReceiveUserInfo;
import vn.edu.tdc.selling_medicine_app.fragment.SettingsFragment;
import vn.edu.tdc.selling_medicine_app.model.User;

public class ChangePasswordActivity extends AppCompatActivity {
    private Toolbar toolbar_changepassword;
    private TextInputEditText newPassword, reNewPassword;
    private Button btnConfirm;
    private Context context;
    private View vChangePassword;

    private User user = new User();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        context = this;
        user = ReceiveUserInfo.getUserInfo(context);
        setControl();
        setEvent();
    }

    private void setEvent() {
        vChangePassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndChangePassword();
            }
        });
        setSupportActionBar(toolbar_changepassword);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void checkAndChangePassword() {
        String newPasswordStr = newPassword.getText().toString().trim();
        String reNewPasswordStr = reNewPassword.getText().toString().trim();

        if (TextUtils.isEmpty(newPasswordStr) || TextUtils.isEmpty(reNewPasswordStr)) {
            CustomToast.showToastFailed(context, "Vui lòng nhập mật khẩu");
            return;
        }
        if (!newPasswordStr.equals(reNewPasswordStr)) {
            CustomToast.showToastFailed(context, "Xác nhận mật khẩu mới không khớp!");
            return;
        }

        changePassword(newPasswordStr);
    }
    private void changePassword(String newPasswordstr) {
        DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference("User/"+user.getMobileNumber());

        String hashPassword = HashUtil.hashPassword(newPasswordstr);
        firebaseDatabase.child("password").setValue(hashPassword);
        CustomToast.showToastSuccessful(context,"Đổi mật khẩu thành công!");
        newPassword.setText("");
        reNewPassword.setText("");
//        Intent intent = new Intent(context, SettingsFragment.class);
//        startActivity(intent);
    }
    private void setControl() {
        toolbar_changepassword = findViewById(R.id.toolbar_changepassword);
        newPassword = findViewById(R.id.newPassword);
        reNewPassword = findViewById(R.id.reNewPassword);
        btnConfirm = findViewById(R.id.btnConfirm);
        vChangePassword = findViewById(R.id.vChangePassword);
    }
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}