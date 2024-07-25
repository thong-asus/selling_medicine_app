package vn.edu.tdc.selling_medicine_app.feature;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FcmTokenManager {
    private static final String PREF_NAME = "fcmTokenPrefs";
    private static final String KEY_FCM_TOKEN = "fcmToken";
    private static final String TOKENS_PATH = "User/";

    public static void saveFcmToken(Context context, String fcmToken) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_FCM_TOKEN, fcmToken);
        editor.apply();
    }

    public static String getFcmToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_FCM_TOKEN, null);
    }

    public static void clearFcmToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_FCM_TOKEN);
        editor.apply();
    }

    ///////////////////////////////////////////////////////FIREBASE////////////////////////////////////////////////////////////
    public static void saveTokenOnFireBase(Context context, String userId, String fcmToken) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(TOKENS_PATH + userId);
        databaseReference.child("fcmToken").setValue(fcmToken);
    }

    public static void deleteTokenOnFireBase(Context context, String userId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(TOKENS_PATH + userId);
        databaseReference.child("fcmToken").removeValue();
    }

    public static void deleteAllTokensOnFireBase(Context context, String userId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(TOKENS_PATH + userId);
        databaseReference.removeValue();
    }


}
