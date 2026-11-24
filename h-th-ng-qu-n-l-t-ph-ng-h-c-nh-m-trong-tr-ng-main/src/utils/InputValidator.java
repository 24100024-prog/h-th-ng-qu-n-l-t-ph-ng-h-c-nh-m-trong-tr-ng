package utils;

import java.util.regex.Pattern;

public class InputValidator {
    private static final Pattern ID = Pattern.compile("^[A-Za-z0-9_-]{2,20}$");
    private static final Pattern PHONE = Pattern.compile("^0\\d{9,10}$");
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    public static boolean isNotBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    public static boolean isValidId(String s) {
        return s != null && ID.matcher(s.trim()).matches();
    }

    public static boolean isValidPhone(String s) {
        return s != null && PHONE.matcher(s.trim()).matches();
    }

    public static boolean isValidEmail(String s) {
        return s != null && EMAIL.matcher(s.trim()).matches();
    }

    /** Tra ve so nguyen hoac null neu chuoi khong phai so nguyen. */
    public static Integer parseInt(String s) {
        if (s == null) return null;
        try {
            return Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Tra ve so thuc >= 0 hoac null neu khong hop le. */
    public static Double parseNonNegativeDouble(String s) {
        if (s == null) return null;
        try {
            double d = Double.parseDouble(s.trim());
            return (d >= 0 && !Double.isNaN(d) && !Double.isInfinite(d)) ? d : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
