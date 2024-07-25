package vn.edu.tdc.selling_medicine_app.fragment;

import static android.content.ContentValues.TAG;
import static vn.edu.tdc.selling_medicine_app.feature.FcmTokenManager.deleteTokenOnFireBase;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import vn.edu.tdc.selling_medicine_app.LoginActivity;
import vn.edu.tdc.selling_medicine_app.R;
import vn.edu.tdc.selling_medicine_app.feature.FcmTokenManager;
import vn.edu.tdc.selling_medicine_app.feature.ReceiveUserInfo;
import vn.edu.tdc.selling_medicine_app.model.User;

public class SettingsFragment extends Fragment {
    private Toolbar toolbar_settings;
    private Button btnLogout;


    private User user = new User();
    private Context context;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        context = view.getContext();
        //////////////////nhận dữ liệu user//////////
        user = ReceiveUserInfo.getUserInfo(context);
        /////////////////////////////////////////////
        setControl(view);
        setEvent();
        return view;
    }

    private void setEvent() {
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Thông báo");
                builder.setMessage("Bạn có muốn đăng xuất tài khoản?");
                builder.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_question));
                builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ReceiveUserInfo.clearUserInfo(context);
                        FcmTokenManager.clearFcmToken(context);
                        FcmTokenManager.deleteTokenOnFireBase(context,user.getMobileNumber());
                        logoutSuccess();
                    }
                });
                builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    private void logoutSuccess() {
        //xóa token tại client
        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "FCM Token cleared successfully");
            } else {
                Log.e(TAG, "Failed to clear FCM Token", task.getException());
            }
        });
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    private void setControl(View view) {
        toolbar_settings = view.findViewById(R.id.toolbar_settings);
        btnLogout = view.findViewById(R.id.btnLogout);
    }

}