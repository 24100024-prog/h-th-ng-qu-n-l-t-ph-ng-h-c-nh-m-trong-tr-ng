package service.validator;

import model.Booking;
import model.TimeSlot;
import repository.BookingRepository;
import utils.exceptions.BookingException;
import utils.exceptions.MaxHoursExceededException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MaxHoursValidator implements BookingValidator {
    public static final int DEFAULT_MAX_HOURS_PER_DAY = 4;
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BookingRepository bookingRepository;
    private final int maxHoursPerDay;

    public MaxHoursValidator(BookingRepository bookingRepository) {
        this(bookingRepository, DEFAULT_MAX_HOURS_PER_DAY);
    }

    public MaxHoursValidator(BookingRepository bookingRepository, int maxHoursPerDay) {
        this.bookingRepository = bookingRepository;
        this.maxHoursPerDay = maxHoursPerDay;
    }

    @Override
    public void validate(BookingRequest request) throws BookingException {
        TimeSlot slot = request.getTimeSlot();
        LocalDate day = slot.getStartTime().toLocalDate();
        LocalDate lastDay = slot.getEndTime().toLocalTime().equals(LocalTime.MIDNIGHT)
                ? slot.getEndTime().toLocalDate().minusDays(1)
                : slot.getEndTime().toLocalDate();

        for (; !day.isAfter(lastDay); day = day.plusDays(1)) {
            long usedMinutes = minutesOnDay(slot, day);
            for (Booking existing : bookingRepository.findAll()) {
                if (existing.isActive() && existing.getStudentId().equals(request.getStudentId())) {
                    usedMinutes += minutesOnDay(existing.getTimeSlot(), day);
                }
            }
            if (usedMinutes > maxHoursPerDay * 60L) {
                throw new MaxHoursExceededException("Sinh vien " + request.getStudentId()
                        + " se co tong " + (usedMinutes / 60) + "h" + String.format("%02d", usedMinutes % 60)
                        + " trong ngay " + day.format(DAY_FORMAT)
                        + ", vuot gioi han " + maxHoursPerDay + " gio/ngay");
            }
        }
    }

    private long minutesOnDay(TimeSlot slot, LocalDate day) {
        LocalDateTime dayStart = day.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        LocalDateTime from = slot.getStartTime().isAfter(dayStart) ? slot.getStartTime() : dayStart;
        LocalDateTime to = slot.getEndTime().isBefore(dayEnd) ? slot.getEndTime() : dayEnd;
        return to.isAfter(from) ? Duration.between(from, to).toMinutes() : 0;
    }
}