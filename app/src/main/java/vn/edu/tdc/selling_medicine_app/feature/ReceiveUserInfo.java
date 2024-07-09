package vn.edu.tdc.selling_medicine_app.feature;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import vn.edu.tdc.selling_medicine_app.model.User;

public class ReceiveUserInfo {
    private static final String PREF_NAME = "informationUser";
    private static final String KEY_USER_INFO = "informationUser";

    public static void saveUserInfo(Context context, User user) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String jsonUser = gson.toJson(user);
        editor.putString(KEY_USER_INFO, jsonUser);
        editor.apply();
    }

    public static User getUserInfo(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences.contains(KEY_USER_INFO)) {
            String jsonUser = sharedPreferences.getString(KEY_USER_INFO, "");
            Gson gson = new Gson();
            return gson.fromJson(jsonUser, User.class);
        }
        return null;
    }

    public static void clearUserInfo(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_USER_INFO);
        editor.apply();
    }
}
