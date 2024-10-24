package vn.edu.tdc.selling_medicine_app.feature;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import vn.edu.tdc.selling_medicine_app.model.User;

public class UpdateRevenueWorker extends Worker {
    private User user = new User();
    private Context context;

    public UpdateRevenueWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        checkAndUpdateRevenueForToday();
        return Result.success();
    }

    private void checkAndUpdateRevenueForToday() {
        user = ReceiveUserInfo.getUserInfo(context);
        String userMobileNumber = user.getMobileNumber();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Revenue").child(userMobileNumber);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd_MM_yyyy", Locale.getDefault());
        String todayStr = format.format(calendar.getTime());

        databaseReference.child(todayStr).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || snapshot.child("totalDailyMoney").getValue(Double.class) == null) {
                    databaseReference.child(todayStr).child("totalDailyMoney").setValue(0.0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
