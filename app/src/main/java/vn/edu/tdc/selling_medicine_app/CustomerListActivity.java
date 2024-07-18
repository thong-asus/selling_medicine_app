package vn.edu.tdc.selling_medicine_app;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Date;
import java.util.List;

import vn.edu.tdc.selling_medicine_app.feature.CustomToast;
import vn.edu.tdc.selling_medicine_app.feature.ReceiveUserInfo;
import vn.edu.tdc.selling_medicine_app.feature.SwipeToDelete;
import vn.edu.tdc.selling_medicine_app.model.Customer;
import vn.edu.tdc.selling_medicine_app.feature.GetCurrentDate;
import vn.edu.tdc.selling_medicine_app.feature.ReloadSound;
import vn.edu.tdc.selling_medicine_app.model.User;
import vn.edu.tdc.selling_medicine_app.recycleview.Adapter_ItemCustomer;

public class CustomerListActivity extends AppCompatActivity {
    private AlertDialog addCustomerDialog;
    private Toolbar toolbar_customerList;
    private TextInputEditText search_customerList;
    private RecyclerView recyclerView_customerList;
    private LinearLayout noDataAvailable;
    private Adapter_ItemCustomer itemCustomerAdapter;
    private List<Customer> customerList = new ArrayList<>();
    private List<Customer> originalCustomerList = new ArrayList<>();
    private Context context;
    private SwipeRefreshLayout swipeRefresh;
    private ReloadSound reloadSound;
    private  User user = new User();
    private ImageView btn_filter_customerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list);
        ////////////////////////////////////////////////////////
        context = this;
        user = ReceiveUserInfo.getUserInfo(context);



        /////////////////////////////
        reloadSound = new ReloadSound(this);
        setInitialization();
        //////////////////////////////////////////////////////////
        getAllCustomer();
        deleteACustomer();
        setEvent();
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog_filter_customers, null);
        builder.setView(dialogView);

        Spinner spinnerSortByName = dialogView.findViewById(R.id.spinnerSortByName);
        Spinner spinnerSortByDate = dialogView.findViewById(R.id.spinnerSortByDate);
        Spinner spinnerSortByPurchaseCount = dialogView.findViewById(R.id.spinnerSortByPurchaseCount);
        Spinner spinnerSortByTotalSpent = dialogView.findViewById(R.id.spinnerSortByTotalSpent);
        Button btnApplyFilter = dialogView.findViewById(R.id.btnApplyFilter);

        ArrayAdapter<CharSequence> adapterName = ArrayAdapter.createFromResource(this,
                R.array.sort_by_name_customer, android.R.layout.simple_spinner_item);
        adapterName.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortByName.setAdapter(adapterName);

        ArrayAdapter<CharSequence> adapterDate = ArrayAdapter.createFromResource(this,
                R.array.sort_customer_by_date, android.R.layout.simple_spinner_item);
        adapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortByDate.setAdapter(adapterDate);

        ArrayAdapter<CharSequence> adapterPurchaseCount = ArrayAdapter.createFromResource(this,
                R.array.sort_customer_by_qty_bought, android.R.layout.simple_spinner_item);
        adapterPurchaseCount.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortByPurchaseCount.setAdapter(adapterPurchaseCount);

        ArrayAdapter<CharSequence> adapterTotalSpent = ArrayAdapter.createFromResource(this,
                R.array.sort_customer_by_total_cash, android.R.layout.simple_spinner_item);
        adapterTotalSpent.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortByTotalSpent.setAdapter(adapterTotalSpent);

        final AlertDialog dialog = builder.create();

        btnApplyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedNameOption = spinnerSortByName.getSelectedItem().toString();
                String selectedDateOption = spinnerSortByDate.getSelectedItem().toString();
                String selectedPurchaseCountOption = spinnerSortByPurchaseCount.getSelectedItem().toString();
                String selectedTotalSpentOption = spinnerSortByTotalSpent.getSelectedItem().toString();
                handleFilterSelection(selectedNameOption, selectedDateOption, selectedPurchaseCountOption, selectedTotalSpentOption);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void handleFilterSelection(String nameOption, String dateOption, String purchaseCountOption, String totalSpentOption) {
        List<Customer> filteredCustomers = new ArrayList<>(originalCustomerList);

        //Lọc theo tên KH
        if (!nameOption.equals("Không chọn")) {
            if (nameOption.equals("Từ A-Z")) {
                Collections.sort(filteredCustomers, new Comparator<Customer>() {
                    @Override
                    public int compare(Customer c1, Customer c2) {
                        return c1.getCustomerName().compareTo(c2.getCustomerName());
                    }
                });
            } else if (nameOption.equals("Từ Z-A")) {
                Collections.sort(filteredCustomers, new Comparator<Customer>() {
                    @Override
                    public int compare(Customer c1, Customer c2) {
                        return c2.getCustomerName().compareTo(c1.getCustomerName());
                    }
                });
            }
        }

        //Lọc theo ngày thêm KH
        if (!dateOption.equals("Không chọn")) {
            Date startDate = null;
            Date endDate = null;

            switch (dateOption) {
                case "Mới nhất":
                    Collections.sort(filteredCustomers, new Comparator<Customer>() {
                        @Override
                        public int compare(Customer c1, Customer c2) {
                            return c2.getDateCreated().compareTo(c1.getDateCreated());
                        }
                    });
                    break;
                case "Cũ nhất":
                    Collections.sort(filteredCustomers, new Comparator<Customer>() {
                        @Override
                        public int compare(Customer c1, Customer c2) {
                            return c1.getDateCreated().compareTo(c2.getDateCreated());
                        }
                    });
                    break;
                case "Hôm nay":
                    filterByDateRange(filteredCustomers, "today");
                    break;
                case "Hôm qua":
                    filterByDateRange(filteredCustomers, "yesterday");
                    break;
                case "Tuần này":
                    filterByDateRange(filteredCustomers, "this_week");
                    break;
                case "Tháng này":
                    filterByDateRange(filteredCustomers, "this_month");
                    break;
            }
        }

        //Lọc theo số lần mua
        if (!purchaseCountOption.equals("Không chọn")) {
            if (purchaseCountOption.equals("Cao nhất")) {
                Collections.sort(filteredCustomers, new Comparator<Customer>() {
                    @Override
                    public int compare(Customer c1, Customer c2) {
                        return Integer.compare(c2.getQtyBought(), c1.getQtyBought());
                    }
                });
            } else if (purchaseCountOption.equals("Thấp nhất")) {
                Collections.sort(filteredCustomers, new Comparator<Customer>() {
                    @Override
                    public int compare(Customer c1, Customer c2) {
                        return Integer.compare(c1.getQtyBought(), c2.getQtyBought());
                    }
                });
            } else if (purchaseCountOption.equals("<5")) {
                filteredCustomers = filterByQtyBought(filteredCustomers, 0, 5);
            } else if (purchaseCountOption.equals("5-10")) {
                filteredCustomers = filterByQtyBought(filteredCustomers, 5, 10);
            } else if (purchaseCountOption.equals(">10")) {
                filteredCustomers = filterByQtyBought(filteredCustomers, 10, Integer.MAX_VALUE);
            }
        }
        //Lọc theo tổng tiền
        if (!totalSpentOption.equals("Không chọn")) {
            if (totalSpentOption.equals("Cao nhất")) {
                Collections.sort(filteredCustomers, new Comparator<Customer>() {
                    @Override
                    public int compare(Customer c1, Customer c2) {
                        return Double.compare(c2.getTotalCash(), c1.getTotalCash());
                    }
                });
            } else if (totalSpentOption.equals("Thấp nhất")) {
                Collections.sort(filteredCustomers, new Comparator<Customer>() {
                    @Override
                    public int compare(Customer c1, Customer c2) {
                        return Double.compare(c1.getTotalCash(), c2.getTotalCash());
                    }
                });
            } else if (totalSpentOption.equals("<5000")) {
                filteredCustomers = filterByTotalCash(filteredCustomers, 0, 5000);
            } else if (totalSpentOption.equals("5000-10000")) {
                filteredCustomers = filterByTotalCash(filteredCustomers, 5000, 10000);
            } else if (totalSpentOption.equals("10000-20000")) {
                filteredCustomers = filterByTotalCash(filteredCustomers, 10000, 20000);
            } else if (totalSpentOption.equals("20000-50000")) {
                filteredCustomers = filterByTotalCash(filteredCustomers, 20000, 50000);
            } else if (totalSpentOption.equals(">50000")) {
                filteredCustomers = filterByTotalCash(filteredCustomers, 50000, Double.MAX_VALUE);
            }
        }
        if(!filteredCustomers.isEmpty()) {
            itemCustomerAdapter.updateData(filteredCustomers);
        } else {
            CustomToast.showToastFailed(context,"Không tìm thấy khách hàng nào");
        }
    }

    private void filterByDateRange(List<Customer> customers, String dateRangeType) {
        List<Customer> filteredList = new ArrayList<>();
        String currentDate = GetCurrentDate.getCurrentDate();
        String[] parts = currentDate.split(" ")[0].split("/");

        int currentDay = Integer.parseInt(parts[0]);
        int currentMonth = Integer.parseInt(parts[1]);
        int currentYear = Integer.parseInt(parts[2]);

        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.DAY_OF_MONTH, currentDay);
        todayCalendar.set(Calendar.MONTH, currentMonth - 1);
        todayCalendar.set(Calendar.YEAR, currentYear);

        Calendar customerCalendar = Calendar.getInstance();

        for (Customer customer : customers) {
            String customerDate = customer.getDateCreated();
            String[] customerParts = customerDate.split(" ")[0].split("/");

            int customerDay = Integer.parseInt(customerParts[0]);
            int customerMonth = Integer.parseInt(customerParts[1]);
            int customerYear = Integer.parseInt(customerParts[2]);

            customerCalendar.set(Calendar.DAY_OF_MONTH, customerDay);
            customerCalendar.set(Calendar.MONTH, customerMonth - 1);
            customerCalendar.set(Calendar.YEAR, customerYear);

            boolean isInRange = false;

            switch (dateRangeType) {
                case "today":
                    isInRange = isSameDay(todayCalendar, customerCalendar);
                    break;
                case "yesterday":
                    isInRange = isYesterday(todayCalendar, customerCalendar);
                    break;
                case "this_week":
                    isInRange = isThisWeek(todayCalendar, customerCalendar);
                    break;
                case "this_month":
                    isInRange = isThisMonth(todayCalendar, customerCalendar);
                    break;
                default:
                    break;
            }

            if (isInRange) {
                filteredList.add(customer);
            }
        }

        customers.clear();
        customers.addAll(filteredList);
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    private boolean isYesterday(Calendar today, Calendar customer) {
        Calendar yesterday = (Calendar) today.clone();
        yesterday.add(Calendar.DATE, -1);
        return isSameDay(yesterday, customer);
    }

    private boolean isThisWeek(Calendar today, Calendar customer) {
        int currentWeek = today.get(Calendar.WEEK_OF_YEAR);
        int customerWeek = customer.get(Calendar.WEEK_OF_YEAR);
        int currentYear = today.get(Calendar.YEAR);
        int customerYear = customer.get(Calendar.YEAR);

        return currentYear == customerYear && currentWeek == customerWeek;
    }

    private boolean isThisMonth(Calendar today, Calendar customer) {
        return today.get(Calendar.YEAR) == customer.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == customer.get(Calendar.MONTH);
    }


    private List<Customer> filterByQtyBought(List<Customer> customers, int min, int max) {
        List<Customer> filteredList = new ArrayList<>();
        for (Customer customer : customers) {
            if (customer.getQtyBought() >= min && customer.getQtyBought() <= max) {
                filteredList.add(customer);
            }
        }
        return filteredList;
    }
    private List<Customer> filterByTotalCash(List<Customer> customers, double min, double max) {
        List<Customer> filteredList = new ArrayList<>();
        for (Customer customer : customers) {
            if (customer.getTotalCash() >= min && customer.getTotalCash() <= max) {
                filteredList.add(customer);
            }
        }
        return filteredList;
    }
    private void setEvent() {

        btn_filter_customerList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();
            }
        });
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadSound.playReloadSound();
                getAllCustomer();
                swipeRefresh.setRefreshing(false);
            }
        });
        search_customerList.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchCustomer(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        search_customerList.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();
                }
                return false;
            }
        });
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
        getAllCustomer();
    }
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private void setInitialization() {
        toolbar_customerList = findViewById(R.id.toolbar_customerList);
        search_customerList = findViewById(R.id.search_customerList);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        noDataAvailable = findViewById(R.id.noDataAvailable);
        btn_filter_customerList = findViewById(R.id.btn_filter_customerList);
        setSupportActionBar(toolbar_customerList);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView_customerList = findViewById(R.id.recycleview_customerList);
        recyclerView_customerList.setLayoutManager(new LinearLayoutManager(this));
        itemCustomerAdapter = new Adapter_ItemCustomer(customerList, this);
        recyclerView_customerList.setAdapter(itemCustomerAdapter);
        itemCustomerAdapter.notifyDataSetChanged();
    }

    private void getAllCustomer() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Customers/" + user.getMobileNumber());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                customerList.clear();
                originalCustomerList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Customer customer = snapshot.getValue(Customer.class);
                    if (customer != null) {
                        customerList.add(customer);
                        originalCustomerList.add(customer);
                    }
                }

                if (customerList.isEmpty()) {
                    noDataAvailable.setVisibility(VISIBLE);
                } else {
                    noDataAvailable.setVisibility(GONE);

                    Collections.sort(customerList, new Comparator<Customer>() {
                        @Override
                        public int compare(Customer c1, Customer c2) {
                            return c2.getDateCreated().compareTo(c1.getDateCreated());
                        }
                    });

                    Collections.sort(originalCustomerList, new Comparator<Customer>() {
                        @Override
                        public int compare(Customer c1, Customer c2) {
                            return c2.getDateCreated().compareTo(c1.getDateCreated());
                        }
                    });
                }

                itemCustomerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error loading data from Firebase: " + databaseError.getMessage());
            }
        });
    }


    private void deleteACustomer() {
        SwipeToDelete swipeToDeleteCallback = new SwipeToDelete(itemCustomerAdapter, this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView_customerList);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.menu_add) {
            showAddCustomerDialog();
            //finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddCustomerDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog_add_customer, null);
        dialogBuilder.setView(dialogView);

        EditText customerMobileNum = dialogView.findViewById(R.id.customerMobileNum);
        EditText customerName = dialogView.findViewById(R.id.customerName);

        dialogBuilder.setTitle("Thêm khách hàng");
        dialogBuilder.setPositiveButton("Thêm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String mobileNumber = customerMobileNum.getText().toString().trim();
                String fullName = customerName.getText().toString().trim();

                if (mobileNumber.isEmpty() || fullName.isEmpty()) {
                    CustomToast.showToastFailed(context,"Vui lòng nhập đầy đủ thông tin khách hàng");
                } else {
                    addNewCustomer(mobileNumber, fullName);
                    getAllCustomer();
                    CustomToast.showToastSuccessful(context,"Thêm khách hàng thành công");
                }
            }
        });
        dialogBuilder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        addCustomerDialog = dialogBuilder.create();
        addCustomerDialog.show();
    }

    private void addNewCustomer(String mobileNumber, String fullName) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Customers/"+user.getMobileNumber());
        Customer newCustomer = new Customer(mobileNumber, fullName, GetCurrentDate.getCurrentDateTime(), 0, 0);
        databaseReference.child(newCustomer.getCustomerMobileNum()).setValue(newCustomer);
    }

    private void searchCustomer(String searchText) {
        ArrayList<Customer> filteredCustomerList = new ArrayList<>();
        if (searchText.isEmpty()) {
            filteredCustomerList.addAll(originalCustomerList);
        } else {
            for (Customer customer : originalCustomerList) {
                if (customer.getCustomerMobileNum().toLowerCase().contains(searchText.toLowerCase()) ||
                        customer.getCustomerName().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredCustomerList.add(customer);
                }
            }
        }
        itemCustomerAdapter.updateData(filteredCustomerList);
    }
}
