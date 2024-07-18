package vn.edu.tdc.selling_medicine_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import vn.edu.tdc.selling_medicine_app.feature.CustomToast;
import vn.edu.tdc.selling_medicine_app.feature.GetCurrentDate;
import vn.edu.tdc.selling_medicine_app.feature.ReceiveUserInfo;
import vn.edu.tdc.selling_medicine_app.feature.ReloadSound;
import vn.edu.tdc.selling_medicine_app.feature.SwipeToDelete;
import vn.edu.tdc.selling_medicine_app.model.MyBill;
import vn.edu.tdc.selling_medicine_app.model.User;
import vn.edu.tdc.selling_medicine_app.recycleview.Adapter_ItemInvoice;

public class HistorySalesListActivity extends AppCompatActivity {

    private Toolbar toolbar_historySales;
    private TextInputEditText search_history_salesList;
    private ImageView btn_filter_history_salesList;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout noDataAvailable;
    private RecyclerView recycleview_historySales;
    private TextView tvNoAvailableHistorySales;
    /////////////////////////////////////////////
    private ReloadSound reloadSound;
    private Context context;
    private User user;
    private Adapter_ItemInvoice adapterItemInvoice;
    private ArrayList<MyBill> invoiceList = new ArrayList<>();
    private ArrayList<MyBill> originalInvoiceList = new ArrayList<>();
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_sales_list);

        context = this;
        user = ReceiveUserInfo.getUserInfo(context);
        reloadSound = new ReloadSound(this);
        setControl();
        setEvent();
        getAllInvoice();
        deleteAInvoice();
    }

    private void setEvent() {
        setSupportActionBar(toolbar_historySales);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recycleview_historySales.setLayoutManager(new LinearLayoutManager(this));
        adapterItemInvoice = new Adapter_ItemInvoice(invoiceList, this);
        recycleview_historySales.setAdapter(adapterItemInvoice);
        adapterItemInvoice.notifyDataSetChanged();



        swipeRefresh.setOnRefreshListener(() -> {
            getAllInvoice();
            swipeRefresh.setRefreshing(false);
        });


        btn_filter_history_salesList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();
            }
        });
        search_history_salesList.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterInvoices(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadSound.playReloadSound();
                getAllInvoice();
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog_filter_invoices, null);
        builder.setView(dialogView);

        Spinner spinnerSortByCustomerName = dialogView.findViewById(R.id.spinnerSortByCustomerName);
        Spinner spinnerSortByDate = dialogView.findViewById(R.id.spinnerSortByDateCreated);
        Spinner spinnerSortByTotalCash = dialogView.findViewById(R.id.spinnerSortByTotalSpent);
        Button btnApplyFilter = dialogView.findViewById(R.id.btnApplyFilter);

        ArrayAdapter<CharSequence> adapterName = ArrayAdapter.createFromResource(this,
                R.array.sort_by_name_customer, android.R.layout.simple_spinner_item);
        adapterName.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortByCustomerName.setAdapter(adapterName);

        ArrayAdapter<CharSequence> adapterDate = ArrayAdapter.createFromResource(this,
                R.array.sort_customer_by_date, android.R.layout.simple_spinner_item);
        adapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortByDate.setAdapter(adapterDate);

        ArrayAdapter<CharSequence> adapterTotalCash = ArrayAdapter.createFromResource(this,
                R.array.sort_customer_by_total_cash, android.R.layout.simple_spinner_item);
        adapterTotalCash.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortByTotalCash.setAdapter(adapterTotalCash);

        final AlertDialog dialog = builder.create();

        btnApplyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedNameOption = spinnerSortByCustomerName.getSelectedItem().toString();
                String selectedDateOption = spinnerSortByDate.getSelectedItem().toString();
                String selectedPurchaseCountOption = spinnerSortByTotalCash.getSelectedItem().toString();
                handleFilterSelection(selectedNameOption, selectedDateOption, selectedPurchaseCountOption);
                dialog.dismiss();
            }
        });

        dialog.show();
    }
    private void handleFilterSelection(String nameOption, String dateOption, String totalCashOption) {
        List<MyBill> filteredInvoices = new ArrayList<>(originalInvoiceList);

        // Lọc theo tên khách hàng
        if (!nameOption.equals("Không chọn")) {
            if (nameOption.equals("Từ A-Z")) {
                Collections.sort(filteredInvoices, new Comparator<MyBill>() {
                    @Override
                    public int compare(MyBill i1, MyBill i2) {
                        return i1.getCustomerName().compareTo(i2.getCustomerName());
                    }
                });
            } else if (nameOption.equals("Từ Z-A")) {
                Collections.sort(filteredInvoices, new Comparator<MyBill>() {
                    @Override
                    public int compare(MyBill i1, MyBill i2) {
                        return i2.getCustomerName().compareTo(i1.getCustomerName());
                    }
                });
            }
        }

        // Lọc theo ngày tạo
        if (!dateOption.equals("Không chọn")) {
            switch (dateOption) {
                case "Mới nhất":
                    Collections.sort(filteredInvoices, new Comparator<MyBill>() {
                        @Override
                        public int compare(MyBill i1, MyBill i2) {
                            return i2.getDateCreated().compareTo(i1.getDateCreated());
                        }
                    });
                    break;
                case "Cũ nhất":
                    Collections.sort(filteredInvoices, new Comparator<MyBill>() {
                        @Override
                        public int compare(MyBill i1, MyBill i2) {
                            return i1.getDateCreated().compareTo(i2.getDateCreated());
                        }
                    });
                    break;
                case "Hôm nay":
                    filterByDateRange(filteredInvoices, "today");
                    break;
                case "Hôm qua":
                    filterByDateRange(filteredInvoices, "yesterday");
                    break;
                case "Tuần này":
                    filterByDateRange(filteredInvoices, "this_week");
                    break;
                case "Tháng này":
                    filterByDateRange(filteredInvoices, "this_month");
                    break;
            }
        }

        // Lọc theo tổng tiền
        if (!totalCashOption.equals("Không chọn")) {
            if (totalCashOption.equals("Cao nhất")) {
                Collections.sort(filteredInvoices, new Comparator<MyBill>() {
                    @Override
                    public int compare(MyBill i1, MyBill i2) {
                        return Double.compare(i2.getTotalCash(), i1.getTotalCash());
                    }
                });
            } else if (totalCashOption.equals("Thấp nhất")) {
                Collections.sort(filteredInvoices, new Comparator<MyBill>() {
                    @Override
                    public int compare(MyBill i1, MyBill i2) {
                        return Double.compare(i1.getTotalCash(), i2.getTotalCash());
                    }
                });
            } else if (totalCashOption.equals("<5000")) {
                filteredInvoices = filterByTotalCash(filteredInvoices, 0, 5000);
            } else if (totalCashOption.equals("5000-10000")) {
                filteredInvoices = filterByTotalCash(filteredInvoices, 5000, 10000);
            } else if (totalCashOption.equals("10000-20000")) {
                filteredInvoices = filterByTotalCash(filteredInvoices, 10000, 20000);
            } else if (totalCashOption.equals("20000-50000")) {
                filteredInvoices = filterByTotalCash(filteredInvoices, 20000, 50000);
            } else if (totalCashOption.equals(">50000")) {
                filteredInvoices = filterByTotalCash(filteredInvoices, 50000, Double.MAX_VALUE);
            }
        }

        if(!filteredInvoices.isEmpty()) {
            adapterItemInvoice.updateData(filteredInvoices);
        } else {
            CustomToast.showToastFailed(context,"Không tìm thấy hóa đơn nào");
        }
    }

    private void filterByDateRange(List<MyBill> invoices, String dateRangeType) {
        List<MyBill> filteredList = new ArrayList<>();
        String currentDate = GetCurrentDate.getCurrentDate();
        String[] parts = currentDate.split(" ")[0].split("/");

        int currentDay = Integer.parseInt(parts[0]);
        int currentMonth = Integer.parseInt(parts[1]);
        int currentYear = Integer.parseInt(parts[2]);

        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.DAY_OF_MONTH, currentDay);
        todayCalendar.set(Calendar.MONTH, currentMonth - 1);
        todayCalendar.set(Calendar.YEAR, currentYear);

        Calendar invoiceCalendar = Calendar.getInstance();

        for (MyBill invoice : invoices) {
            String invoiceDate = invoice.getDateCreated();
            String[] invoiceParts = invoiceDate.split(" ")[0].split("/");

            int invoiceDay = Integer.parseInt(invoiceParts[0]);
            int invoiceMonth = Integer.parseInt(invoiceParts[1]);
            int invoiceYear = Integer.parseInt(invoiceParts[2]);

            invoiceCalendar.set(Calendar.DAY_OF_MONTH, invoiceDay);
            invoiceCalendar.set(Calendar.MONTH, invoiceMonth - 1);
            invoiceCalendar.set(Calendar.YEAR, invoiceYear);

            boolean isInRange = false;

            switch (dateRangeType) {
                case "today":
                    isInRange = isSameDay(todayCalendar, invoiceCalendar);
                    break;
                case "yesterday":
                    isInRange = isYesterday(todayCalendar, invoiceCalendar);
                    break;
                case "this_week":
                    isInRange = isThisWeek(todayCalendar, invoiceCalendar);
                    break;
                case "this_month":
                    isInRange = isThisMonth(todayCalendar, invoiceCalendar);
                    break;
                default:
                    break;
            }

            if (isInRange) {
                filteredList.add(invoice);
            }
        }

        invoices.clear();
        invoices.addAll(filteredList);
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    private boolean isYesterday(Calendar today, Calendar invoice) {
        Calendar yesterday = (Calendar) today.clone();
        yesterday.add(Calendar.DATE, -1);
        return isSameDay(yesterday, invoice);
    }

    private boolean isThisWeek(Calendar today, Calendar invoice) {
        int currentWeek = today.get(Calendar.WEEK_OF_YEAR);
        int invoiceWeek = invoice.get(Calendar.WEEK_OF_YEAR);
        int currentYear = today.get(Calendar.YEAR);
        int invoiceYear = invoice.get(Calendar.YEAR);

        return currentYear == invoiceYear && currentWeek == invoiceWeek;
    }

    private boolean isThisMonth(Calendar today, Calendar invoice) {
        return today.get(Calendar.YEAR) == invoice.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == invoice.get(Calendar.MONTH);
    }

    private List<MyBill> filterByTotalCash(List<MyBill> invoices, double min, double max) {
        List<MyBill> filteredList = new ArrayList<>();
        for (MyBill invoice : invoices) {
            if (invoice.getTotalCash() >= min && invoice.getTotalCash() <= max) {
                filteredList.add(invoice);
            }
        }
        return filteredList;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reloadSound != null) {
            reloadSound.release();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        getAllInvoice();
    }

    private void getAllInvoice() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Invoices/" + user.getMobileNumber());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                invoiceList.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot customerSnapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot invoiceSnapshot : customerSnapshot.getChildren()) {
                            MyBill invoice = new MyBill();
                            invoice.setCustomerMobileNum(invoiceSnapshot.child("customerMobileNum").getValue(String.class));
                            invoice.setCustomerName(invoiceSnapshot.child("customerName").getValue(String.class));
                            invoice.setInvoiceID(invoiceSnapshot.child("invoiceID").getValue(String.class));
                            invoice.setDateCreated(invoiceSnapshot.child("dateCreated").getValue(String.class));
                            invoice.setNote(invoiceSnapshot.child("note").getValue(String.class));
                            invoice.setTotalCash(invoiceSnapshot.child("totalCash").getValue(Integer.class));
                            invoice.setCustomerPaid(invoiceSnapshot.child("customerPaid").getValue(Integer.class));
                            invoice.setChangeOfCustomer(invoiceSnapshot.child("changeOfCustomer").getValue(Integer.class));
                            invoice.setTotalQty(invoiceSnapshot.child("totalQty").getValue(Integer.class));

                            List<MyBill.Item> items = new ArrayList<>();
                            for (DataSnapshot itemSnapshot : invoiceSnapshot.child("items").getChildren()) {
                                MyBill.Item item = new MyBill.Item();
                                item.setIdDrug(itemSnapshot.child("idDrug").getValue(String.class));
                                item.setDrugName(itemSnapshot.child("drugName").getValue(String.class));
                                item.setQtyDrug(itemSnapshot.child("qtyDrug").getValue(Integer.class));
                                item.setPrice(itemSnapshot.child("price").getValue(Integer.class));
                                items.add(item);
                            }
                            invoice.setItems(items);

                            invoiceList.add(invoice);
                        }
                    }
                    //sắp xếp
                    Collections.sort(invoiceList, (invoice1, invoice2) -> {
                        //
                        return invoice2.getDateCreated().compareTo(invoice1.getDateCreated());
                    });
                    noDataAvailable.setVisibility(View.GONE);
                    originalInvoiceList.clear();
                    originalInvoiceList.addAll(invoiceList);

                    adapterItemInvoice.notifyDataSetChanged();
                } else {
                    noDataAvailable.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi
            }
        });
    }

    private void deleteAInvoice() {
        SwipeToDelete swipeToDeleteCallback = new SwipeToDelete(adapterItemInvoice, this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recycleview_historySales);
    }

    private void filterInvoices(String query) {
        ArrayList<MyBill> filteredList = new ArrayList<>();
        for (MyBill invoice : originalInvoiceList) {
            if (invoice.getCustomerMobileNum().toLowerCase().contains(query.toLowerCase()) ||
                    invoice.getCustomerName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(invoice);
            }
        }
        invoiceList.clear();
        invoiceList.addAll(filteredList);
        adapterItemInvoice.notifyDataSetChanged();
    }

    private void setControl() {
        noDataAvailable = findViewById(R.id.noDataAvailable);
        recycleview_historySales = findViewById(R.id.recycleview_historySales);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        btn_filter_history_salesList = findViewById(R.id.btn_filter_history_salesList);
        search_history_salesList = findViewById(R.id.search_history_salesList);
        toolbar_historySales = findViewById(R.id.toolbar_historySales);
    }

    @Override
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

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
