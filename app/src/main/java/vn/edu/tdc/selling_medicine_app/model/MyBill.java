package vn.edu.tdc.selling_medicine_app.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MyBill implements Serializable {
    private String userMobileNum;
    private String customerMobileNum;
    private String customerName;
    private String invoiceID;
    private String dateCreated;
    private String note;
    private String imageInvoice;
    private List<Item> items;
    private int totalCash;
    private int customerPaid;
    private int changeOfCustomer;
    private int totalQty; // Thêm thuộc tính totalQty


    public MyBill() {
        items = new ArrayList<>();
    }



    public MyBill(String userMobileNum, String customerMobileNum, String customerName, String invoiceID, String dateCreated, String note, String imageInvoice, List<Item> items, int totalCash, int customerPaid, int changeOfCustomer, int totalQty) {
        this.userMobileNum = userMobileNum;
        this.customerMobileNum = customerMobileNum;
        this.customerName = customerName;
        this.invoiceID = invoiceID;
        this.dateCreated = dateCreated;
        this.note = note;
        this.imageInvoice = imageInvoice;
        this.items = items;
        this.totalCash = totalCash;
        this.customerPaid = customerPaid;
        this.changeOfCustomer = changeOfCustomer;
        this.totalQty = totalQty; // Khởi tạo totalQty
    }


    // Getters and setters cho tất cả các trường
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    public String getUserMobileNum() {
        return userMobileNum;
    }

    public void setUserMobileNum(String userMobileNum) {
        this.userMobileNum = userMobileNum;
    }

    public String getCustomerMobileNum() {
        return customerMobileNum;
    }

    public void setCustomerMobileNum(String customerMobileNum) {
        this.customerMobileNum = customerMobileNum;
    }

    public String getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(String invoiceID) {
        this.invoiceID = invoiceID;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getImageInvoice() {
        return imageInvoice;
    }

    public void setImageInvoice(String imageInvoice) {
        this.imageInvoice = imageInvoice;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    public int getTotalCash() {
        return totalCash;
    }

    public void setTotalCash(int totalCash) {
        this.totalCash = totalCash;
    }

    public int getCustomerPaid() {
        return customerPaid;
    }

    public void setCustomerPaid(int customerPaid) {
        this.customerPaid = customerPaid;
    }

    public int getChangeOfCustomer() {
        return changeOfCustomer;
    }

    public void setChangeOfCustomer(int changeOfCustomer) {
        this.changeOfCustomer = changeOfCustomer;
    }

    public int getTotalQty() {
        return totalQty; // Getter cho totalQty
    }

    public void setTotalQty(int totalQty) {
        this.totalQty = totalQty; // Setter cho totalQty
    }

    public int calculateTotalQty() {
        int totalQuantity = 0;
        for (Item item : items) {
            totalQuantity += item.getQtyDrug();
        }
        return totalQuantity;
    }

    public static class Item implements Serializable {
        private String idDrug;
        private String drugName;
        private int qtyDrug;
        private int price;

        public Item() {
        }

        public Item(String idDrug, String drugName, int qtyDrug, int price) {
            this.idDrug = idDrug;
            this.drugName = drugName;
            this.qtyDrug = qtyDrug;
            this.price = price;
        }

        public String getIdDrug() {
            return idDrug;
        }

        public void setIdDrug(String idDrug) {
            this.idDrug = idDrug;
        }

        public String getDrugName() {
            return drugName;
        }

        public void setDrugName(String drugName) {
            this.drugName = drugName;
        }

        public int getQtyDrug() {
            return qtyDrug;
        }

        public void setQtyDrug(int qtyDrug) {
            this.qtyDrug = qtyDrug;
        }

        public int getPrice() {
            return price;
        }

        public void setPrice(int price) {
            this.price = price;
        }
    }
}
