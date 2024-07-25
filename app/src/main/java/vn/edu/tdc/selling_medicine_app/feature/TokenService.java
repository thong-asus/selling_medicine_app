package vn.edu.tdc.selling_medicine_app.feature;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class TokenService extends IntentService {
    public TokenService() {
        super("TokenService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.getIdToken(true).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String idToken = task.getResult().getToken();
                    // Sử dụng token này để xác thực
                    authenticateWithToken(idToken);
                } else {
                    // Xử lý lỗi
                    Log.e("TokenService", "Lỗi khi lấy mã token: " + task.getException().getMessage());
                }
            });
        }
    }

    private void authenticateWithToken(String idToken) {
        // Thực hiện xác thực với token ở đây
        Log.d("TokenService", "Token nhận được: " + idToken);
        // Ví dụ: Gửi token đến máy chủ của bạn hoặc Firebase Storage
    }
}
