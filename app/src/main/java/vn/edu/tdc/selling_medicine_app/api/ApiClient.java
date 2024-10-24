package vn.edu.tdc.selling_medicine_app.api;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.Response;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

public class ApiClient {
    private static final String BASE_URL = "https://api-social.icheck.com.vn";
    private static String token = "";

    private static OkHttpClient client = new OkHttpClient();
    private static Gson gson = new Gson();

    public static String initToken() throws IOException {
        String random16byte = "34a0d8bd5c6d1925081d390db177da3e";
        JsonObject json = new JsonObject();
        json.addProperty("os", 3);
        json.addProperty("deviceId", random16byte);

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(BASE_URL + "/login/anonymous")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            JsonObject responseObject = gson.fromJson(response.body().string(), JsonObject.class);
            if (responseObject != null && responseObject.has("statusCode") && responseObject.get("statusCode").getAsString().equals("200")) {
                token = responseObject.getAsJsonObject("data").get("token").getAsString();
                return token;
            }
        }

        return "";
    }

    public static JsonObject searchCode(String code) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/social/api/products/search?nameCode=" + code + "&limit=48&offset=0")
                .addHeader("authorization", "Bearer " + token)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            JsonObject responseObject = gson.fromJson(response.body().string(), JsonObject.class);
            if (responseObject != null && responseObject.has("statusCode")) {
                String statusCode = responseObject.get("statusCode").getAsString();
                if (statusCode.equals("U102")) {
                    token = initToken();
                    if (!token.isEmpty()) {
                        return searchCode(code);
                    }
                } else if (statusCode.equals("200")) {
                    return responseObject.getAsJsonObject("data");
                }
            }
        }

        return null;
    }
}