package vn.edu.tdc.selling_medicine_app.feature;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GetCurrentDate {
    public static String getCurrentDate() {
        LocalDate currentDate = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String formattedDate = currentDate.format(formatter);
        return formattedDate;
    }
}
