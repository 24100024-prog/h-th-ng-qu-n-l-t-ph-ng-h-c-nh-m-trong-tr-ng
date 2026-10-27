package service;

import model.Booking;
import model.BookingDetail;
import model.Room;
import model.TimeSlot;
import repository.BookingRepository;
import repository.RoomRepository;
import repository.StudentRepository;
import service.policy.RoomFeePolicy;
import service.validator.BookingRequest;
import service.validator.BookingValidator;
import utils.exceptions.BookingException;
import utils.exceptions.InvalidTimeException;
import utils.exceptions.RoomMaintenanceException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class BookingService {
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final List<BookingValidator> validators;
    private RoomFeePolicy feePolicy;

    public BookingService(BookingRepository bookingRepository,
                          RoomRepository roomRepository,
                          RoomFeePolicy feePolicy,
                          List<BookingValidator> validators) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.feePolicy = feePolicy;
        this.validators = validators;
    }

    public RoomFeePolicy getFeePolicy() { return feePolicy; }

    /** Chinh sach moi chi ap dung cho booking tao sau do; phi cua booking cu giu nguyen nhu da luu. */
    public void setFeePolicy(RoomFeePolicy feePolicy) {
        if (feePolicy == null) throw new IllegalArgumentException("Chinh sach phi khong duoc null");
        this.feePolicy = feePolicy;
    }

    public Booking createBooking(String studentId, String roomId, TimeSlot timeSlot, int numberOfPeople)
            throws BookingException {
        return createBooking(studentId, roomId, timeSlot, numberOfPeople, LocalDateTime.now());
    }

    public Booking createBooking(String studentId, String roomId, TimeSlot timeSlot, int numberOfPeople,
                                 LocalDateTime now) throws BookingException {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new BookingException("Ma sinh vien khong duoc de trong");
        }
        if (timeSlot == null || timeSlot.getStartTime() == null || timeSlot.getEndTime() == null) {
            throw new InvalidTimeException("Khung gio khong hop le");
        }
        if (!timeSlot.isValid()) {
            throw new InvalidTimeException("Gio ket thuc phai sau gio bat dau");
        }
        if (timeSlot.getStartTime().isBefore(now)) {
            throw new InvalidTimeException("Khong the dat phong trong qua khu");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BookingException("Khong tim thay phong " + roomId));
        if (room.isUnderMaintenance()) {
            throw new RoomMaintenanceException(room.getRoomName() + " dang bao tri, khong the dat");
        }

        String bookingId = generateNextId();
        BookingRequest request = new BookingRequest(bookingId, studentId, roomId, timeSlot, numberOfPeople);
        for (BookingValidator validator : validators) {
            validator.validate(request);
        }

        double fee = feePolicy.calculateFee(room, timeSlot.getBillableHours());
        Booking booking = new Booking(bookingId, studentId, roomId, timeSlot, numberOfPeople, fee);
        bookingRepository.add(booking);
        return booking;
    }

    public void cancelBooking(String bookingId, String studentId) throws BookingException {
        cancelBooking(bookingId, studentId, LocalDateTime.now());
    }

    /** Huy = doi trang thai sang DA_HUY (giu lai lich su); booking da huy khong con chiem lich phong. */
    public void cancelBooking(String bookingId, String studentId, LocalDateTime now) throws BookingException {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException("Khong tim thay lich dat " + bookingId));

        if (!booking.isActive()) {
            throw new BookingException("Lich " + bookingId + " da duoc huy truoc do");
        }
        if (!booking.getStudentId().equals(studentId)) {
            throw new BookingException("Ban khong phai nguoi dat lich " + bookingId + " nen khong the huy");
        }
        if (!booking.getTimeSlot().getStartTime().isAfter(now)) {
            throw new BookingException("Lich " + bookingId + " da bat dau hoac da ket thuc, khong the huy");
        }
        booking.cancel();
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByStudent(String studentId) {
        return bookingRepository.findAll().stream()
                .filter(b -> b.getStudentId().equals(studentId))
                .collect(Collectors.toList());
    }

    public BookingDetail getDetail(String bookingId, StudentRepository studentRepository) throws BookingException {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException("Khong tim thay lich dat " + bookingId));
        return new BookingDetail(booking,
                studentRepository.findById(booking.getStudentId()).orElse(null),
                roomRepository.findById(booking.getRoomId()).orElse(null));
    }

    /** Phi uoc tinh cho mot phong va khoang gio theo chinh sach hien hanh (chua tao booking). */
    public double estimateFee(String roomId, TimeSlot slot) throws BookingException {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BookingException("Khong tim thay phong " + roomId));
        if (slot == null || !slot.isValid()) throw new InvalidTimeException("Khung gio khong hop le");
        return feePolicy.calculateFee(room, slot.getBillableHours());
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
