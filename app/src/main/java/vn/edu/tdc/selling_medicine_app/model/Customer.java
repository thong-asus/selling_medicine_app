package vn.edu.tdc.selling_medicine_app.model;

import java.io.Serializable;

public class Customer implements Serializable {
    private String customerMobileNum, customerName, dateCreated;
    private int qtyBought, totalCash;

    public Customer(String customerMobileNum, String customerName, String dateCreated, int qtyBought, int totalCash) {
        this.customerMobileNum = customerMobileNum;
        this.customerName = customerName;
        this.qtyBought = qtyBought;
        this.totalCash = totalCash;
        this.dateCreated = dateCreated;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
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

    public int getQtyBought() {
        return qtyBought;
    }

    public void setQtyBought(int qtyBought) {
        this.qtyBought = qtyBought;
    }

    public int getTotalCash() {
        return totalCash;
    }

    public void setTotalCash(int totalCash) {
        this.totalCash = totalCash;
    }
}
