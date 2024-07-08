package vn.edu.tdc.selling_medicine_app.model;

import java.io.Serializable;

public class Customer implements Serializable {
    private String customerMobileNum, customerName;
    private long qtyBought, totalCash;

    public Customer(String customerMobileNum, String customerName, long qtyBought, long totalCash) {
        this.customerMobileNum = customerMobileNum;
        this.customerName = customerName;
        this.qtyBought = qtyBought;
        this.totalCash = totalCash;
    }

    public Customer() {
    }

    public String getCustomerMobileNum() {
        return customerMobileNum;
    }

    public void setCustomerMobileNum(String customerMobileNum) {
        this.customerMobileNum = customerMobileNum;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public long getQtyBought() {
        return qtyBought;
    }

    public void setQtyBought(long qtyBought) {
        this.qtyBought = qtyBought;
    }

    public long getTotalCash() {
        return totalCash;
    }

    public void setTotalCash(long totalCash) {
        this.totalCash = totalCash;
    }
}
