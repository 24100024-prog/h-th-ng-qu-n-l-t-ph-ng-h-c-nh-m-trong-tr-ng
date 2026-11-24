package utils.exceptions;

public class MaxHoursExceededException extends BookingException {
    public MaxHoursExceededException(String message) {
        super(message);
    }
}