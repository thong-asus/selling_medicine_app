package vn.edu.tdc.selling_medicine_app;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.components.XAxis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import vn.edu.tdc.selling_medicine_app.feature.ReceiveUserInfo;
import vn.edu.tdc.selling_medicine_app.model.User;

public class StatisticActivity extends AppCompatActivity {

    private Toolbar toolbar_statistic;
    private TextView title_statistic;
    private View viewLine;
    private Button btn_revenue;
    private BarChart barChart;
    private Context context;
    private User user = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);
        context = this;
        user = ReceiveUserInfo.getUserInfo(context);
        setControl();
        setEvent();
        //checkAndUpdateRevenueForToday();
    }

    private void setEvent() {
        setSupportActionBar(toolbar_statistic);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        btn_revenue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadRevenueData();
                title_statistic.setText("Doanh thu 7 ngày gần nhất");
                title_statistic.setVisibility(View.VISIBLE);
                viewLine.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setControl() {
        toolbar_statistic = findViewById(R.id.toolbar_statistic);
        title_statistic = findViewById(R.id.title_statistic);
        btn_revenue = findViewById(R.id.btn_revenue);
        barChart = findViewById(R.id.barChart);
        viewLine = findViewById(R.id.viewLine);
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
    private void checkAndUpdateRevenueForToday() {
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
                    Log.d("StatisticActivity", "Revenue for " + todayStr + " was set to 0.");
                } else {
                    Log.d("StatisticActivity", "Revenue for " + todayStr + " already exists.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("StatisticActivity", "DatabaseError: " + error.getMessage());
            }
        });
    }

    private void loadRevenueData() {
        String userMobileNumber = user.getMobileNumber();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Revenue").child(userMobileNumber);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("dd_MM_yyyy", Locale.getDefault());
                String todayStr = format.format(calendar.getTime());

                if (snapshot.exists()) {
                    Map<String, Double> revenueMap = new HashMap<>();
                    boolean todayExists = false;

                    for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                        String dateKey = dateSnapshot.getKey();
                        Double totalDailyMoney = dateSnapshot.child("totalDailyMoney").getValue(Double.class);
                        if (totalDailyMoney != null) {
                            revenueMap.put(dateKey, totalDailyMoney);
                        }
                        if (dateKey.equals(todayStr)) {
                            todayExists = true;
                        }
                    }

                    if (!todayExists) {
                        revenueMap.put(todayStr, 0.0);
                        // Cập nhật dữ liệu vào cơ sở dữ liệu
                        databaseReference.child(todayStr).child("totalDailyMoney").setValue(0.0);
                    }

                    getData7Day(revenueMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("StatisticActivity", "DatabaseError: " + error.getMessage());
            }
        });
    }

    private void getData7Day(Map<String, Double> revenueMap) {
        SimpleDateFormat format = new SimpleDateFormat("dd_MM_yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        String todayStr = format.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, -6); // Bắt đầu từ 7 ngày trước
        String sevenDaysAgoStr = format.format(calendar.getTime());

        Date today = null;
        Date sevenDaysAgo = null;
        try {
            today = format.parse(todayStr);
            sevenDaysAgo = format.parse(sevenDaysAgoStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Sắp xếp các ngày theo thứ tự tăng dần
        List<String> sortedDates = new ArrayList<>(revenueMap.keySet());
        Collections.sort(sortedDates, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                try {
                    return format.parse(o1).compareTo(format.parse(o2));
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });

        LinkedHashMap<String, Double> filteredMap = new LinkedHashMap<>();
        for (String date : sortedDates) {
            try {
                Date dateObj = format.parse(date);
                if (dateObj != null && !dateObj.before(sevenDaysAgo) && !dateObj.after(today)) {
                    filteredMap.put(date, revenueMap.get(date));
                    if (filteredMap.size() >= 7) {
                        break;
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, Double> entry : filteredMap.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue().floatValue()));
            labels.add(formatDate(entry.getKey()));
            colors.add(getRandomColor());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu hàng ngày");
        dataSet.setColors(colors);
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        barChart.setData(barData);

        // Tắt chức năng zoom và cuộn
        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setDragEnabled(false); // Ngăn kéo trượt

        // Định dạng trục X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Đặt tiêu đề của cột ở phía dưới
        xAxis.setLabelRotationAngle(45); // Xoay nhãn để dễ đọc
        xAxis.setLabelCount(entries.size(), true); // Đảm bảo có đủ số lượng nhãn trên trục X
        xAxis.setGranularity(1f); // Đặt độ chính xác của các nhãn
        xAxis.setDrawGridLines(false); // Tắt các đường lưới nếu cần
        xAxis.setDrawAxisLine(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int idx = (int) value;
                if (idx >= 0 && idx < labels.size()) {
                    return labels.get(idx);
                }
                return "";
            }
        });

        barChart.setPadding(0, 0, 0, 50); // Tăng padding dưới cùng nếu cần
        barChart.invalidate(); // refresh chart
    }

    private String formatDate(String date) {
        SimpleDateFormat originalFormat = new SimpleDateFormat("dd_MM_yyyy", Locale.getDefault());
        SimpleDateFormat targetFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date parsedDate = originalFormat.parse(date);
            if (parsedDate != null) {
                return targetFormat.format(parsedDate);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }


    private int getRandomColor() {
        Random random = new Random();
        return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }
}
