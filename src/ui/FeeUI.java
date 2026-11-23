package ui;

import model.Booking;
import model.TimeSlot;
import service.BookingService;
import service.policy.FreeFeePolicy;
import service.policy.HourlyFeePolicy;
import service.policy.RoomTypeFeePolicy;
import utils.DateTimeUtils;
import utils.InputValidator;
import utils.exceptions.BookingException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Giao dien console quan ly chinh sach va xem chi phi booking. */
public class FeeUI {
    private final ConsoleIO io;
    private final BookingService bookingService;

    public FeeUI(ConsoleIO io, BookingService bookingService) {
        this.io = io;
        this.bookingService = bookingService;
    }

    /** Chuc nang 9: xem phi, uoc tinh phi va thay doi chinh sach tinh phi. */
    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("\n========== QUAN LY CHI PHI ==========");
            System.out.println("Chinh sach hien tai: " + bookingService.getFeePolicy().getName());
            System.out.println("1. Uoc tinh phi dat phong");
            System.out.println("2. Thay doi chinh sach tinh phi");
            System.out.println("3. Xem phi cac booking");
            System.out.println("0. Quay lai");
            switch (io.readLine("Chon chuc nang: ").trim()) {
                case "1": estimateFee(); break;
                case "2": changeFeePolicy(); break;
                case "3": showBookingFees(); break;
                case "0": running = false; break;
                default: System.out.println("Lua chon khong hop le.");
            }
        }
    }

    private void estimateFee() {
        String roomId = io.readLine("Ma phong: ").trim();
        if (roomId.equalsIgnoreCase(ConsoleIO.CANCEL_WORD)) return;
        LocalDateTime start = readDateTime("Gio bat dau (dd/MM/yyyy HH:mm): ");
        if (start == null) return;
        LocalDateTime end = readDateTime("Gio ket thuc (dd/MM/yyyy HH:mm): ");
        if (end == null) return;
        try {
            double fee = bookingService.estimateFee(roomId, new TimeSlot(start, end));
            System.out.println("So gio tinh phi: " + new TimeSlot(start, end).getBillableHours());
            System.out.println("Phi uoc tinh: " + DateTimeUtils.formatMoney(fee));
        } catch (BookingException | IllegalArgumentException e) {
            System.out.println("Khong the tinh phi: " + e.getMessage());
        }
    }

    private void changeFeePolicy() {
        System.out.println("\nChon chinh sach tinh phi:");
        System.out.println("1. Mien phi");
        System.out.println("2. Tinh theo gio (mot don gia cho moi loai phong)");
        System.out.println("3. Tinh theo loai phong");
        String choice = io.readLine("Lua chon: ").trim();
        try {
            switch (choice) {
                case "1":
                    bookingService.setFeePolicy(new FreeFeePolicy());
                    break;
                case "2": {
                    Double rate = readRate("Don gia moi gio (VND): ");
                    if (rate == null) return;
                    bookingService.setFeePolicy(new HourlyFeePolicy(rate));
                    break;
                }
                case "3": {
                    Map<String, Double> rates = new LinkedHashMap<>();
                    String[] types = {"NORMAL", "PROJECTOR", "SEMINAR"};
                    for (String type : types) {
                        Double rate = readRate("Don gia/gio cho phong " + type + " (VND): ");
                        if (rate == null) return;
                        rates.put(type, rate);
                    }
                    bookingService.setFeePolicy(new RoomTypeFeePolicy(rates));
                    break;
                }
                default:
                    System.out.println("Lua chon khong hop le.");
                    return;
            }
            System.out.println("Da cap nhat chinh sach: " + bookingService.getFeePolicy().getName());
            System.out.println("Chinh sach moi chi ap dung cho booking duoc tao tu bay gio.");
        } catch (IllegalArgumentException e) {
            System.out.println("Khong the doi chinh sach: " + e.getMessage());
        }
    }

    private void showBookingFees() {
        System.out.println("\n========== CHI PHI CAC BOOKING ==========");
        if (bookingService.getAllBookings().isEmpty()) {
            System.out.println("Chua co booking nao.");
            return;
        }
        double total = 0;
        for (Booking booking : bookingService.getAllBookings()) {
            System.out.println(booking.getBookingId() + " | Phong: " + booking.getRoomId()
                    + " | Sinh vien: " + booking.getStudentId()
                    + " | Phi: " + DateTimeUtils.formatMoney(booking.getFee())
                    + (booking.isActive() ? " | Con hieu luc" : " | Da huy"));
            total += booking.getFee();
        }
        System.out.println("Tong phi da ghi nhan: " + DateTimeUtils.formatMoney(total));
    }

    private Double readRate(String prompt) {
        while (true) {
            String value = io.readLine(prompt).trim();
            if (value.equalsIgnoreCase(ConsoleIO.CANCEL_WORD)) return null;
            Double rate = InputValidator.parseNonNegativeDouble(value);
            if (rate != null) return rate;
            System.out.println("Don gia phai la so khong am. Hay nhap lai hoac go 'huy'.");
        }
    }

    private LocalDateTime readDateTime(String prompt) {
        while (true) {
            String value = io.readLine(prompt).trim();
            if (value.equalsIgnoreCase(ConsoleIO.CANCEL_WORD)) return null;
            LocalDateTime dateTime = DateTimeUtils.parseDateTime(value);
            if (dateTime != null) return dateTime;
            System.out.println("Ngay gio khong hop le. Hay nhap dd/MM/yyyy HH:mm hoac go 'huy'.");
        }
    }
}

