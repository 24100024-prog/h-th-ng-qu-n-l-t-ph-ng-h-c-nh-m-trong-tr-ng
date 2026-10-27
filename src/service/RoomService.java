package service;

import model.Booking;
import model.Room;
import model.TimeSlot;
import repository.BookingRepository;
import repository.RoomRepository;
import utils.InputValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class RoomService {
    /** Khung gio hoat dong trong ngay dung khi tim gio trong (de bai chua xac dinh, day la gia tri mac dinh). */
    public static final LocalTime OPEN_TIME = LocalTime.of(7, 0);
    public static final LocalTime CLOSE_TIME = LocalTime.of(21, 0);

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public RoomService(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    public void addRoom(Room room) {
        if (!InputValidator.isValidId(room.getRoomId())) {
            throw new IllegalArgumentException("Ma phong khong hop le (2-20 ky tu chu, so, '-' hoac '_')");
        }
        if (!InputValidator.isNotBlank(room.getRoomName())) {
            throw new IllegalArgumentException("Ten phong khong duoc de trong");
        }
        if (room.getCapacity() <= 0) {
            throw new IllegalArgumentException("Suc chua phai lon hon 0");
        }
        if (roomRepository.existsById(room.getRoomId())) {
            throw new IllegalArgumentException("Ma phong " + room.getRoomId() + " da ton tai");
        }
        roomRepository.add(room);
    }

    public void setMaintenance(String roomId, boolean maintenance) {
        Room room = getRoom(roomId);
        room.setStatus(maintenance ? Room.MAINTENANCE : Room.ACTIVE);
    }

    public void deleteRoom(String roomId) {
        getRoom(roomId);
        for (Booking b : bookingRepository.findAll()) {
            if (b.isActive() && b.getRoomId().equals(roomId)) {
                throw new IllegalStateException("Phong " + roomId + " con booking " + b.getBookingId()
                        + " dang hieu luc, hay huy truoc khi xoa");
            }
        }
        roomRepository.remove(roomId);
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Optional<Room> findRoom(String roomId) {
        return roomRepository.findById(roomId);
    }

    public Room getRoom(String roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay phong " + roomId));
    }

    /** Cac khoang trong lien tuc cua phong trong mot ngay (giua OPEN_TIME va CLOSE_TIME); phong bao tri khong co khoang trong. */
    public List<TimeSlot> findFreeSlots(String roomId, LocalDate date) {
        Room room = getRoom(roomId);
        List<TimeSlot> free = new ArrayList<>();
        if (room.isUnderMaintenance()) return free;

        LocalDateTime dayStart = date.atTime(OPEN_TIME);
        LocalDateTime dayEnd = date.atTime(CLOSE_TIME);

        List<TimeSlot> busy = new ArrayList<>();
        TimeSlot window = new TimeSlot(dayStart, dayEnd);
        for (Booking b : bookingRepository.findAll()) {
            if (b.isActive() && b.getRoomId().equals(roomId) && b.getTimeSlot().overlaps(window)) {
                busy.add(b.getTimeSlot());
            }
        }
        busy.sort(Comparator.comparing(TimeSlot::getStartTime));

        LocalDateTime cursor = dayStart;
        for (TimeSlot slot : busy) {
            if (slot.getStartTime().isAfter(cursor)) {
                free.add(new TimeSlot(cursor, slot.getStartTime()));
            }
            if (slot.getEndTime().isAfter(cursor)) cursor = slot.getEndTime();
        }
        if (cursor.isBefore(dayEnd)) free.add(new TimeSlot(cursor, dayEnd));
        return free;
    }

    /** Phong co trong tron khoang gio yeu cau hay khong (chua ke bao tri: phong bao tri luon tra false). */
    public boolean isAvailable(String roomId, TimeSlot slot) {
        Room room = getRoom(roomId);
        if (room.isUnderMaintenance()) return false;
        for (Booking b : bookingRepository.findAll()) {
            if (b.isActive() && b.getRoomId().equals(roomId) && b.getTimeSlot().overlaps(slot)) return false;
        }
        return true;
    }
}
