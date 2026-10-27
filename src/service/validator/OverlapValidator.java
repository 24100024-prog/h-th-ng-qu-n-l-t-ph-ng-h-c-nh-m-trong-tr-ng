package service.validator;

import model.Booking;
import model.TimeSlot;
import repository.BookingRepository;
import utils.DateTimeUtils;
import utils.exceptions.BookingException;
import utils.exceptions.OverlapBookingException;

public class OverlapValidator implements BookingValidator {
    private final BookingRepository bookingRepository;

    public OverlapValidator(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void validate(BookingRequest request) throws BookingException {
        for (Booking existing : bookingRepository.findAll()) {
            if (!existing.getRoomId().equals(request.getRoomId())) continue;
            if (isOverlapping(existing.getTimeSlot(), request.getTimeSlot())) {
                throw new OverlapBookingException("Phòng " + request.getRoomId()
                        + " đã có lịch đặt từ " + DateTimeUtils.format(existing.getTimeSlot().getStartTime())
                        + " đến " + DateTimeUtils.format(existing.getTimeSlot().getEndTime()));
            }
        }
    }

    private boolean isOverlapping(TimeSlot a, TimeSlot b) {
        return a.getStartTime().isBefore(b.getEndTime())
                && b.getStartTime().isBefore(a.getEndTime());
    }
}