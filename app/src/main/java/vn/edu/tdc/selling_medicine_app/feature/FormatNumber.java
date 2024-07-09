package vn.edu.tdc.selling_medicine_app.feature;

import java.text.DecimalFormat;
import java.util.Locale;

public class FormatNumber {
    public static String formatNumber(int money) {
        String formattedCurrency = "";
        Locale locale = new Locale("vi", "VN");
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(locale);
        decimalFormat.applyPattern("#,###");
        formattedCurrency = decimalFormat.format(money);
        return formattedCurrency;
    }
}
