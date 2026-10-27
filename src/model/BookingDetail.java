package model;

import utils.DateTimeUtils;

/** Thong tin day du cua mot booking (noi voi sinh vien va phong) de hien thi. Phi luon lay tu chinh booking. */
public class BookingDetail {
    private final Booking booking;
    private final Student student;
    private final Room room;

    public BookingDetail(Booking booking, Student student, Room room) {
        this.booking = booking;
        this.student = student;
        this.room = room;
    }

    public Booking getBooking() { return booking; }
    public Student getStudent() { return student; }
    public Room getRoom() { return room; }

    public int getBillableHours() { return booking.getTimeSlot().getBillableHours(); }
    public double getFee() { return booking.getFee(); }

    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append("Ma booking : ").append(booking.getBookingId()).append('\n');
        sb.append("Trang thai : ").append(booking.isActive() ? "Con hieu luc" : "Da huy").append('\n');
        sb.append("Sinh vien  : ").append(booking.getStudentId());
        if (student != null) sb.append(" - ").append(student.getName()).append(" (").append(student.getLop()).append(")");
        sb.append('\n');
        sb.append("Phong      : ").append(booking.getRoomId());
        if (room != null) sb.append(" - ").append(room.getRoomName()).append(" [").append(room.getType()).append("]");
        sb.append('\n');
        sb.append("Thoi gian  : ").append(booking.getTimeSlot()).append(" (")
                .append(getBillableHours()).append(" gio tinh phi)\n");
        sb.append("So nguoi   : ").append(booking.getNumberOfPeople()).append('\n');
        sb.append("Phi        : ").append(DateTimeUtils.formatMoney(booking.getFee()));
        return sb.toString();
    }
}
