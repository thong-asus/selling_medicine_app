package vn.edu.tdc.selling_medicine_app.feature;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MakePhoneCall {
    private static final int REQUEST_CALL_PHONE_PERMISSION = 1;
    private Context context;

    public MakePhoneCall(Context context) {
        this.context = context;
    }

    // Mở màn hình quay số và gắn số điện thoại vào
    public void openDialer(String phoneNumber) {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:" + phoneNumber));
        context.startActivity(dialIntent);
    }

    // Kiểm tra và yêu cầu cấp quyền nếu cần
    public void makePhoneCallWithPermission(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            openDialer(phoneNumber);
        } else {
            if (context instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE_PERMISSION);
            }
        }
    }

    // Xử lý kết quả yêu cầu cấp quyền
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults, String phoneNumber) {
        if (requestCode == REQUEST_CALL_PHONE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openDialer(phoneNumber);
            } else {
                Toast.makeText(context, "Cần cấp quyền cuộc gọi!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
