package vn.edu.tdc.selling_medicine_app.model;

import java.io.Serializable;

public class User implements Serializable {
    private String mobileNumber, password, fullname, fcmToken;

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public User(String mobileNumber, String password, String fullname, String fcmToken) {
        this.mobileNumber = mobileNumber;
        this.password = password;
        this.fullname = fullname;
        this.fcmToken = fcmToken;
    }

    public User() {
    }
}
