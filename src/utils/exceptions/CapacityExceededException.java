package utils.exceptions;

public class CapacityExceededException extends BookingException {
    public CapacityExceededException(String message) {
        super(message);
    }
}