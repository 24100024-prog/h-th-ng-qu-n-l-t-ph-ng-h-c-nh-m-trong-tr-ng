package service;

import model.Booking;
import repository.BookingRepository;
import repository.RoomRepository;
import repository.StudentRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/** Luu/tai dong thoi ca ba repository va kiem tra tinh nhat quan du lieu giua chung. */
public class DataService {
    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public DataService(StudentRepository studentRepository, RoomRepository roomRepository,
                       BookingRepository bookingRepository) {
        this.studentRepository = studentRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    public void saveAll(Path dir, boolean binary) throws IOException {
        List<String> problems = checkConsistency();
        if (!problems.isEmpty()) {
            throw new IOException("Du lieu khong nhat quan, khong luu: " + problems.get(0));
        }
        if (binary) {
            studentRepository.saveBinary(dir.resolve("students.bin"));
            roomRepository.saveBinary(dir.resolve("rooms.bin"));
            bookingRepository.saveBinary(dir.resolve("bookings.bin"));
        } else {
            studentRepository.saveText(dir.resolve("students.txt"));
            roomRepository.saveText(dir.resolve("rooms.txt"));
            bookingRepository.saveText(dir.resolve("bookings.txt"));
        }
    }

    /**
     * Tai ca ba file; neu bat ky file nao loi hoac du lieu tham chieu cheo sai thi giu nguyen du lieu dang co.
     */
    public void loadAll(Path dir, boolean binary) throws IOException {
        StudentRepository s = new StudentRepository();
        RoomRepository r = new RoomRepository();
        BookingRepository b = new BookingRepository();
        if (binary) {
            s.loadBinary(dir.resolve("students.bin"));
            r.loadBinary(dir.resolve("rooms.bin"));
            b.loadBinary(dir.resolve("bookings.bin"));
        } else {
            s.loadText(dir.resolve("students.txt"));
            r.loadText(dir.resolve("rooms.txt"));
            b.loadText(dir.resolve("bookings.txt"));
        }
        List<String> problems = checkConsistency(s, r, b);
        if (!problems.isEmpty()) {
            throw new IOException("Du lieu tai len khong nhat quan: " + problems.get(0));
        }
        // chi hoan doi khi moi thu deu hop le
        studentRepository.clear();
        for (model.Student x : s.findAll()) studentRepository.add(x);
        roomRepository.clear();
        for (model.Room x : r.findAll()) roomRepository.add(x);
        bookingRepository.clear();
        for (Booking x : b.findAll()) bookingRepository.add(x);
    }

    public boolean exists(Path dir, boolean binary) {
        String ext = binary ? ".bin" : ".txt";
        return Files.exists(dir.resolve("students" + ext)) && Files.exists(dir.resolve("rooms" + ext))
                && Files.exists(dir.resolve("bookings" + ext));
    }

    public List<String> checkConsistency() {
        return checkConsistency(studentRepository, roomRepository, bookingRepository);
    }

    /** Booking phai tham chieu sinh vien va phong co that; cac booking con hieu luc khong duoc trung gio cung phong. */
    static List<String> checkConsistency(StudentRepository s, RoomRepository r, BookingRepository b) {
        List<String> problems = new ArrayList<>();
        List<Booking> all = b.findAll();
        for (int i = 0; i < all.size(); i++) {
            Booking x = all.get(i);
            if (!s.existsById(x.getStudentId())) {
                problems.add("Booking " + x.getBookingId() + " tham chieu sinh vien khong ton tai: " + x.getStudentId());
            }
            if (!r.existsById(x.getRoomId())) {
                problems.add("Booking " + x.getBookingId() + " tham chieu phong khong ton tai: " + x.getRoomId());
            }
            if (!x.isActive()) continue;
            for (int j = i + 1; j < all.size(); j++) {
                Booking y = all.get(j);
                if (y.isActive() && x.getRoomId().equals(y.getRoomId()) && x.getTimeSlot().overlaps(y.getTimeSlot())) {
                    problems.add("Booking " + x.getBookingId() + " va " + y.getBookingId()
                            + " trung gio tai phong " + x.getRoomId());
                }
            }
        }
        return problems;
    }

    public static Path defaultDir() {
        return Paths.get("data");
    }
}
