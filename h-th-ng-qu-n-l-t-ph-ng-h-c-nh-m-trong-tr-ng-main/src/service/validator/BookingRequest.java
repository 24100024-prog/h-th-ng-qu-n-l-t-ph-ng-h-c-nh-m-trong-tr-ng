package service.validator;

import model.TimeSlot;

public class BookingRequest {
    private final String bookingId;
    private final String studentId;
    private final String roomId;
    private final TimeSlot timeSlot;
    private final int numberOfPeople;

    public BookingRequest(String bookingId, String studentId, String roomId, TimeSlot timeSlot, int numberOfPeople) {
        this.bookingId = bookingId;
        this.studentId = studentId;
        this.roomId = roomId;
        this.timeSlot = timeSlot;
        this.numberOfPeople = numberOfPeople;
    }

    public String getBookingId() { 
        return bookingId; 
        }
    public String getStudentId() { 
        return studentId; 
        }
    public String getRoomId() { 
        return roomId; 
        }
    public TimeSlot getTimeSlot() { 
        return timeSlot; 
        }
    public int getNumberOfPeople() { 
        return numberOfPeople; 
        }
}