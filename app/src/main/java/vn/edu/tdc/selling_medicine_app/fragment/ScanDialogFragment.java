package vn.edu.tdc.selling_medicine_app.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.common.HybridBinarizer;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import vn.edu.tdc.selling_medicine_app.PrePaymentActivity;
import vn.edu.tdc.selling_medicine_app.R;
import vn.edu.tdc.selling_medicine_app.feature.CustomToast;
import vn.edu.tdc.selling_medicine_app.feature.NetworkChangeReceiver;

public class ScanDialogFragment extends DialogFragment {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private static final int PICK_IMAGE_REQUEST_CODE = 300;

    private DecoratedBarcodeView barcodeView;
    private Context context;
    private Button btnFlash;
    private Button btnGallery;
    private boolean isFlashOn = false;

    private String token = "";
    private String deviceId = "";
    private OkHttpClient client = new OkHttpClient();

    private String productName = "";
    private NetworkChangeReceiver networkChangeReceiver;

    public ScanDialogFragment() {
        super();

        byte[] buf = new byte[16];
        new Random().nextBytes(buf);
        this.deviceId = "";
        for (byte b : buf) {
            this.deviceId += String.format("%02x", b);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.custom_scan_fragment, container, false);

        barcodeView = view.findViewById(R.id.barcode_scanner);
        barcodeView.getBarcodeView().getCameraSettings().setRequestedCameraId(0);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory());
        barcodeView.getViewFinder().setVisibility(View.VISIBLE);

        btnFlash = view.findViewById(R.id.button_flash);
        btnGallery = view.findViewById(R.id.button_gallery);
        context = view.getContext();

        btnFlash.setOnClickListener(v -> toggleFlash());
        btnGallery.setOnClickListener(v -> openGallery());

        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            initBarcodeScanner();
        }

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initBarcodeScanner();
            } else {
                CustomToast.showToastFailed(context, "Cần cấp quyền truy cập Camera!");
            }
        }
    }

    private void initBarcodeScanner() {
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                barcodeView.pause();
                handleScanResult(result.getText());
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    private void handleScanResult(String scanResult) {
        getProductInfoFromBarcode(scanResult);
    }

    private void toggleFlash() {
        isFlashOn = !isFlashOn;
        if (isFlashOn) {
            barcodeView.setTorchOn();
        } else {
            barcodeView.setTorchOff();
        }
        btnFlash.setText(isFlashOn ? "Tắt đèn" : "Bật đèn");
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                decodeBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void decodeBitmap(Bitmap bitmap) {
        int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        com.google.zxing.RGBLuminanceSource source = new com.google.zxing.RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            Result result = new MultiFormatReader().decode(binaryBitmap);
            handleScanResult(result.getText());
        } catch (Exception e) {
            CustomToast.showToastFailed(context, "Ảnh không hợp lệ!");
        }
    }

    private void getProductInfoFromBarcode(String barcode) {
        searchProduct(barcode);
    }

    void initSession(String code) {
        this.token = "";
        JsonObject payload = new JsonObject();
        payload.addProperty("os", 3);
        payload.addProperty("deviceId", this.deviceId);

        RequestBody body = RequestBody.create(payload.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://api-social.icheck.com.vn/login/anonymous")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                //e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    return;
                }

                JsonObject json = new Gson().fromJson(response.body().string(), JsonObject.class);
                if (json == null) {
                    return;

                }

                if (json.has("statusCode") && json.get("statusCode").getAsString().equals("200")) {
                    token = json.getAsJsonObject("data").get("token").getAsString();
                    searchProduct(code);
                }
            }
        });
    }


    void searchProduct(String code) {
        if (this.token == "") {
            this.initSession(code);
            return;
        }
        Request request = new Request.Builder()
                .url("https://api-social.icheck.com.vn/social/api/products/search?nameCode=" + code + "&limit=48&offset=0")
                .header("Authorization", "Bearer " + token)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    return;
                }
                String text = response.body().string();

                try {
                    JsonObject jsonObject = new Gson().fromJson(text, JsonObject.class);
                    String statusCode = jsonObject.get("statusCode").getAsString();
                    if (statusCode.equals("U102")) {
                        initSession(code);
                        return;
                    }
                    JsonObject data = jsonObject.getAsJsonObject("data");
                    JsonArray rows = data.getAsJsonArray("rows");

                    if (rows == null || rows.size() < 1) {return;}

                    productName = "";
                    float rating = 0.0f;
                    for (int i = 0; i < rows.size(); i++) {
                        JsonObject row = rows.get(i).getAsJsonObject();
                        if (row != null) {
                            float curRating = row.get("rating").getAsFloat();
                            Log.d("curRating", ""+curRating);

                            if (curRating > rating) {
                                rating = curRating;
                                productName = row.get("name").getAsString();
                            }
                        }
                    }
                    if (!productName.isEmpty()) {

                        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                        if (vibrator != null && vibrator.hasVibrator()) {
                            vibrator.vibrate(100);
                        }

                        requireActivity().runOnUiThread(() -> {
                            dismiss();
                            ((PrePaymentActivity) requireActivity()).openAddMedicineToInvoiceDialog(productName);
                            Log.d("Tên sp: ",productName);
                        });
                    } else {
                        getActivity().runOnUiThread(() -> CustomToast.showToastFailed(context, "Không tìm thấy sản phẩm!"));
                        Log.d("Scan: ", "Không tìm thấy sp");
                    }
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    // Xử lý lỗi phân tích JSON
                    getActivity().runOnUiThread(() -> CustomToast.showToastFailed(context, "Lỗi phân tích dữ liệu!"));
                }
            }
        });
    }
}
