package model;

public class Booking {
    public static final String ACTIVE = "HIEU_LUC";
    public static final String CANCELLED = "DA_HUY";

    private final String bookingId;
    private final String studentId;
    private final String roomId;
    private final TimeSlot timeSlot;
    private final int numberOfPeople;
    private final double fee;
    private String status;

    public Booking(String bookingId, String studentId, String roomId, TimeSlot timeSlot,
                   int numberOfPeople, double fee) {
        this(bookingId, studentId, roomId, timeSlot, numberOfPeople, fee, ACTIVE);
    }

    public Booking(String bookingId, String studentId, String roomId, TimeSlot timeSlot,
                   int numberOfPeople, double fee, String status) {
        if (!ACTIVE.equals(status) && !CANCELLED.equals(status)) {
            throw new IllegalArgumentException("Trang thai booking khong hop le: " + status);
        }
        this.bookingId = bookingId;
        this.studentId = studentId;
        this.roomId = roomId;
        this.timeSlot = timeSlot;
        this.numberOfPeople = numberOfPeople;
        this.fee = fee;
        this.status = status;
    }

    public String getBookingId() { return bookingId; }
    public String getStudentId() { return studentId; }
    public String getRoomId() { return roomId; }
    public TimeSlot getTimeSlot() { return timeSlot; }
    public int getNumberOfPeople() { return numberOfPeople; }
    public double getFee() { return fee; }
    public String getStatus() { return status; }
    public boolean isActive() { return ACTIVE.equals(status); }
    public void cancel() { this.status = CANCELLED; }

    @Override
    public String toString() {
        return bookingId + " | SV " + studentId + " | phong " + roomId + " | " + timeSlot + " | " + status;
    }
}
