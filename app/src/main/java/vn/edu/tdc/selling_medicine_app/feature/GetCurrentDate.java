package vn.edu.tdc.selling_medicine_app.feature;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GetCurrentDate {
    public static String getCurrentDateTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        String formattedDateTime = currentDateTime.format(formatter);
        return formattedDateTime;
    }
    public static String getCurrentDate() {
        LocalDateTime currentDateTime = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String formattedDateTime = currentDateTime.format(formatter);
        return formattedDateTime;
    }
}

