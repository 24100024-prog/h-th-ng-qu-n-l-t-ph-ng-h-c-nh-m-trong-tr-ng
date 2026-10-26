package model;

public class Booking {
    private String bookingId;
    private String studentId;
    private String roomId;
    private TimeSlot timeSlot;
    private double fee;

    public Booking(String bookingId, String studentId, String roomId, TimeSlot timeSlot, double fee) {
        this.bookingId = bookingId;
        this.studentId = studentId;
        this.roomId = roomId;
        this.timeSlot = timeSlot;
        this.fee = fee;
    }

    public String getBookingId() { return bookingId; }
    public String getStudentId() { return studentId; }
    public String getRoomId() { return roomId; }
    public TimeSlot getTimeSlot() { return timeSlot; }
    public double getFee() { return fee; }
}
