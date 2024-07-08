package vn.edu.tdc.selling_medicine_app.model;

import java.io.Serializable;
import java.util.Date;

public class Product implements Serializable {
    private String idDrug;
    private String imageDrug;
    private String drugName;
    private String form;
    private String strength;
    private String indications;
    private String dosage;
    private String sideEffects;
    private String expiryDate;
    private int qtyInventory;
    private int price;

    public String getIdDrug() {
        return idDrug;
    }

    public void setIdDrug(String idDrug) {
        this.idDrug = idDrug;
    }

    public String getImageDrug() {
        return imageDrug;
    }

    public void setImageDrug(String imageDrug) {
        this.imageDrug = imageDrug;
    }

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getIndications() {
        return indications;
    }

    public void setIndications(String indications) {
        this.indications = indications;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getSideEffects() {
        return sideEffects;
    }

    public void setSideEffects(String sideEffects) {
        this.sideEffects = sideEffects;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getQtyInventory() {
        return qtyInventory;
    }

    public void setQtyInventory(int qtyInventory) {
        this.qtyInventory = qtyInventory;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Product(String idDrug, String imageDrug, String drugName, String form, String strength, String indications, String dosage, String sideEffects, String expiryDate, int qtyInventory, int price) {
        this.idDrug = idDrug;
        this.imageDrug = imageDrug;
        this.drugName = drugName;
        this.form = form;
        this.strength = strength;
        this.indications = indications;
        this.dosage = dosage;
        this.sideEffects = sideEffects;
        this.expiryDate = expiryDate;
        this.qtyInventory = qtyInventory;
        this.price = price;
    }

    public Product() {
    }
}
