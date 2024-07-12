package vn.edu.tdc.selling_medicine_app.model;

public class BillSingleton {
    private static BillSingleton instance;
    private MyBill bill;


    private BillSingleton() {}

    public static synchronized BillSingleton getInstance() {
        if (instance == null) {
            instance = new BillSingleton();
        }
        return instance;
    }

    public MyBill getBill() {
        return bill;
    }

    public void setBill(MyBill bill) {
        this.bill = bill;
    }
}
