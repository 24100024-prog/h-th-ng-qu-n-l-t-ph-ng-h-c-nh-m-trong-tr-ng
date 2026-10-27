package utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

public class DateTimeUtils {
    public static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    public static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(FORMATTER) : "";
    }

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    /** Phan tich "dd/MM/yyyy HH:mm"; tra ve null neu sai dinh dang hoac ngay khong ton tai. */
    public static LocalDateTime parseDateTime(String text) {
        if (text == null) return null;
        try {
            return LocalDateTime.parse(text.trim(), FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Phan tich "dd/MM/yyyy"; tra ve null neu sai dinh dang hoac ngay khong ton tai. */
    public static LocalDate parseDate(String text) {
        if (text == null) return null;
        try {
            return LocalDate.parse(text.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static String formatMoney(double amount) {
        return String.format(Locale.US, "%,.0f VND", amount);
    }
}
