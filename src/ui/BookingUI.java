package ui;

import model.Booking;
import model.BookingDetail;
import model.Room;
import model.TimeSlot;
import repository.StudentRepository;
import service.BookingService;
import service.RoomService;
import utils.DateTimeUtils;
import utils.exceptions.BookingException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Giao dien console cho cac chuc nang gio trong va dat/huy booking. */
public class BookingUI {
    private final ConsoleIO io;
    private final BookingService bookingService;
    private final RoomService roomService;
    private final StudentRepository studentRepository;

    public BookingUI(ConsoleIO io, BookingService bookingService,
                     RoomService roomService, StudentRepository studentRepository) {
        this.io = io;
        this.bookingService = bookingService;
        this.roomService = roomService;
        this.studentRepository = studentRepository;
    }

    /** Chuc nang 4: tim cac khoang gio trong cua mot phong trong ngay. */
    public void checkFreeSlots() {
        String roomId = io.readLine("Ma phong (go 'huy' de quay lai): ").trim();
        if (roomId.equalsIgnoreCase(ConsoleIO.CANCEL_WORD)) return;
        LocalDate date = readDate("Ngay (dd/MM/yyyy): ");
        if (date == null) return;

        io.guarded(() -> {
            Room room = roomService.getRoom(roomId);
            if (room.isUnderMaintenance()) {
                System.out.println(room.getRoomName() + " dang bao tri, khong co gio trong.");
                return;
            }
            List<TimeSlot> freeSlots = roomService.findFreeSlots(roomId, date);
            System.out.println("Gio trong cua " + room.getRoomName() + " ngay "
                    + DateTimeUtils.formatDate(date) + " (" + RoomService.OPEN_TIME
                    + " - " + RoomService.CLOSE_TIME + "): ");
            if (freeSlots.isEmpty()) System.out.println("  Khong con khung gio trong.");
            for (TimeSlot slot : freeSlots) System.out.println("  " + slot);
        });
    }

    /** Chuc nang 5: tao booking moi. */
    public void createBooking() {
        String studentId = io.readLine("Ma sinh vien: ").trim();
        if (studentId.equalsIgnoreCase(ConsoleIO.CANCEL_WORD)) return;
        String roomId = io.readLine("Ma phong: ").trim();
        if (roomId.equalsIgnoreCase(ConsoleIO.CANCEL_WORD)) return;
        LocalDateTime start = readDateTime("Gio bat dau (dd/MM/yyyy HH:mm): ");
        if (start == null) return;
        LocalDateTime end = readDateTime("Gio ket thuc (dd/MM/yyyy HH:mm): ");
        if (end == null) return;
        Integer people = io.askInt("So nguoi: ", 1, "So nguoi phai lon hon 0");
        if (people == null) return;

        try {
            Booking booking = bookingService.createBooking(studentId, roomId,
                    new TimeSlot(start, end), people);
            System.out.println("Dat phong thanh cong!");
            System.out.println("Ma booking: " + booking.getBookingId());
            System.out.println("Chi phi: " + DateTimeUtils.formatMoney(booking.getFee()));
        } catch (BookingException | IllegalArgumentException e) {
            System.out.println("Khong the dat phong: " + e.getMessage());
        }
    }

    /** Chuc nang 6: hien thi toan bo booking. */
    public void listBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        if (bookings.isEmpty()) {
            System.out.println("Chua co booking nao.");
            return;
        }
        System.out.println("\n========== DANH SACH BOOKING ==========");
        for (Booking booking : bookings) {
            System.out.println(booking + " | Phi: " + DateTimeUtils.formatMoney(booking.getFee()));
        }
    }

    /** Chuc nang 7: hien thi thong tin chi tiet mot booking. */
    public void showBookingDetail() {
        String bookingId = io.readLine("Ma booking: ").trim();
        if (bookingId.equalsIgnoreCase(ConsoleIO.CANCEL_WORD)) return;
        try {
            BookingDetail detail = bookingService.getDetail(bookingId, studentRepository);
            System.out.println("\n========== CHI TIET BOOKING ==========");
            System.out.println(detail.describe());
        } catch (BookingException e) {
            System.out.println("Khong tim thay booking: " + e.getMessage());
        }
    }

    /** Chuc nang 8: huy booking theo ma booking va ma sinh vien dat. */
    public void cancelBooking() {
        String bookingId = io.readLine("Ma booking can huy: ").trim();
        if (bookingId.equalsIgnoreCase(ConsoleIO.CANCEL_WORD)) return;
        String studentId = io.readLine("Ma sinh vien dat phong: ").trim();
        if (studentId.equalsIgnoreCase(ConsoleIO.CANCEL_WORD)) return;
        try {
            bookingService.cancelBooking(bookingId, studentId);
            System.out.println("Da huy booking " + bookingId + ".");
        } catch (BookingException e) {
            System.out.println("Khong the huy booking: " + e.getMessage());
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            String value = io.readLine(prompt).trim();
            if (value.equalsIgnoreCase(ConsoleIO.CANCEL_WORD)) return null;
            LocalDate date = DateTimeUtils.parseDate(value);
            if (date != null) return date;
            System.out.println("Ngay khong hop le. Hay nhap theo dd/MM/yyyy hoac go 'huy'.");
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

