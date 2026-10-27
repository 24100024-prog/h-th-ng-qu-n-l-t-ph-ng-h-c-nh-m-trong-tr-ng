package service;

import model.Booking;
import model.Room;
import model.TimeSlot;
import repository.BookingRepository;
import repository.RoomRepository;
import service.policy.RoomFeePolicy;
import service.validator.BookingRequest;
import service.validator.BookingValidator;
import utils.exceptions.BookingException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class BookingService {
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final RoomFeePolicy feePolicy;
    private final List<BookingValidator> validators;

    public BookingService(BookingRepository bookingRepository,
                          RoomRepository roomRepository,
                          RoomFeePolicy feePolicy,
                          List<BookingValidator> validators) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.feePolicy = feePolicy;
        this.validators = validators;
    }

    public Booking createBooking(String studentId, String roomId, TimeSlot timeSlot, int numberOfPeople)
            throws BookingException {
        return createBooking(studentId, roomId, timeSlot, numberOfPeople, LocalDateTime.now());
    }

    public Booking createBooking(String studentId, String roomId, TimeSlot timeSlot, int numberOfPeople,
                                 LocalDateTime now) throws BookingException {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new BookingException("Mã sinh viên không được để trống");
        }
        if (timeSlot == null || timeSlot.getStartTime() == null || timeSlot.getEndTime() == null) {
            throw new BookingException("Khung giờ không hợp lệ");
        }
        if (!timeSlot.getEndTime().isAfter(timeSlot.getStartTime())) {
            throw new BookingException("Giờ kết thúc phải sau giờ bắt đầu");
        }
        if (timeSlot.getStartTime().isBefore(now)) {
            throw new BookingException("Không thể đặt phòng trong quá khứ");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BookingException("Không tìm thấy phòng " + roomId));
        if ("BAO_TRI".equals(room.getStatus())) {
            throw new BookingException(room.getRoomName() + " đang bảo trì");
        }

        String bookingId = generateNextId();
        BookingRequest request = new BookingRequest(bookingId, studentId, roomId, timeSlot, numberOfPeople);

        for (BookingValidator validator : validators) {
            validator.validate(request);
        }

        long minutes = Duration.between(timeSlot.getStartTime(), timeSlot.getEndTime()).toMinutes();
        int hours = (int) Math.ceil(minutes / 60.0);
        double fee = feePolicy.calculateFee(room, hours);

        Booking booking = new Booking(bookingId, studentId, roomId, timeSlot, fee);
        bookingRepository.add(booking);
        return booking;
    }

    public void cancelBooking(String bookingId, String studentId) throws BookingException {
        cancelBooking(bookingId, studentId, LocalDateTime.now());
    }

    public void cancelBooking(String bookingId, String studentId, LocalDateTime now) throws BookingException {
        Booking booking = bookingRepository.findAll().stream()
                .filter(b -> b.getBookingId().equals(bookingId))
                .findFirst()
                .orElseThrow(() -> new BookingException("Không tìm thấy lịch đặt " + bookingId));

        if (!booking.getStudentId().equals(studentId)) {
            throw new BookingException("Bạn không phải người đặt lịch " + bookingId + " nên không thể hủy");
        }
        if (!booking.getTimeSlot().getStartTime().isAfter(now)) {
            throw new BookingException("Lịch " + bookingId + " đã bắt đầu hoặc đã kết thúc, không thể hủy");
        }
        bookingRepository.remove(booking);
    }

    public List<Booking> getBookingsByStudent(String studentId) {
        return bookingRepository.findAll().stream()
                .filter(b -> b.getStudentId().equals(studentId))
                .collect(Collectors.toList());
    }

    private String generateNextId() {
        int max = 0;
        for (Booking b : bookingRepository.findAll()) {
            String id = b.getBookingId();
            if (id != null && id.matches("BK\\d+")) {
                max = Math.max(max, Integer.parseInt(id.substring(2)));
            }
        }
        return String.format("BK%03d", max + 1);
    }
}