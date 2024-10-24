package vn.edu.tdc.selling_medicine_app.feature;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private boolean isConnected = false;
    private boolean isFirstCheck = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean currentState = NetworkUtil.isNetworkAvailable(context);

        if (!isFirstCheck && currentState && !isConnected) {
            CustomToast.showToastSuccessful(context, "Đã khôi phục kết nối internet");
        }

        isConnected = currentState;
        isFirstCheck = false;
    }
}
