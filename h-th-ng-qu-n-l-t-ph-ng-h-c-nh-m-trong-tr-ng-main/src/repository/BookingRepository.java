package repository;

import model.Booking;
import model.TimeSlot;
import utils.TextCodec;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BookingRepository {
    private static final String KIND = "BOOKING";
    private List<Booking> bookings = new ArrayList<>();

    /** Danh sach chi doc (gom ca booking da huy). */
    public List<Booking> findAll() {
        return Collections.unmodifiableList(bookings);
    }

    public Optional<Booking> findById(String id) {
        for (Booking b : bookings) {
            if (b.getBookingId().equals(id)) return Optional.of(b);
        }
        return Optional.empty();
    }

    public void add(Booking booking) {
        bookings.add(booking);
    }

    public boolean remove(Booking booking) {
        return bookings.remove(booking);
    }

    public void clear() {
        bookings.clear();
    }

    public void saveText(Path path) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Booking b : bookings) {
            lines.add(TextCodec.join(b.getBookingId(), b.getStudentId(), b.getRoomId(),
                    b.getTimeSlot().getStartTime().toString(), b.getTimeSlot().getEndTime().toString(),
                    String.valueOf(b.getNumberOfPeople()), Double.toString(b.getFee()), b.getStatus()));
        }
        FileSupport.writeTextLines(path, lines);
    }

    public void loadText(Path path) throws IOException {
        List<String[]> records = FileSupport.readTextRecords(path, 8);
        List<Booking> loaded = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        int n = 0;
        for (String[] f : records) {
            n++;
            try {
                addChecked(loaded, ids, create(f[0], f[1], f[2], LocalDateTime.parse(f[3]),
                        LocalDateTime.parse(f[4]), Integer.parseInt(f[5]), Double.parseDouble(f[6]), f[7]));
            } catch (RuntimeException e) {
                throw FileSupport.invalid(path, n, e);
            }
        }
        bookings = loaded;
    }

    public void saveBinary(Path path) throws IOException {
        FileSupport.atomicWrite(path, out -> {
            DataOutputStream dos = FileSupport.binaryHeader(out, KIND, bookings.size());
            for (Booking b : bookings) {
                dos.writeUTF(b.getBookingId());
                dos.writeUTF(b.getStudentId());
                dos.writeUTF(b.getRoomId());
                dos.writeUTF(b.getTimeSlot().getStartTime().toString());
                dos.writeUTF(b.getTimeSlot().getEndTime().toString());
                dos.writeInt(b.getNumberOfPeople());
                dos.writeDouble(b.getFee());
                dos.writeUTF(b.getStatus());
            }
            dos.flush();
        });
    }

    public void loadBinary(Path path) throws IOException {
        List<Booking> loaded = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        try (DataInputStream in = FileSupport.openBinary(path)) {
            int count = FileSupport.readBinaryHeader(in, path, KIND);
            for (int i = 1; i <= count; i++) {
                try {
                    addChecked(loaded, ids, create(in.readUTF(), in.readUTF(), in.readUTF(),
                            LocalDateTime.parse(in.readUTF()), LocalDateTime.parse(in.readUTF()),
                            in.readInt(), in.readDouble(), in.readUTF()));
                } catch (IllegalArgumentException | java.time.format.DateTimeParseException e) {
                    throw FileSupport.invalid(path, i, e);
                }
            }
        } catch (EOFException e) {
            throw new IOException("File " + path.getFileName() + " bi cat cut hoac hong", e);
        }
        bookings = loaded;
    }

    private static Booking create(String id, String studentId, String roomId, LocalDateTime start,
                                  LocalDateTime end, int people, double fee, String status) {
        TimeSlot slot = new TimeSlot(start, end);
        if (!slot.isValid()) throw new IllegalArgumentException("khung gio khong hop le");
        if (fee < 0 || Double.isNaN(fee)) throw new IllegalArgumentException("phi khong hop le");
        if (people <= 0) throw new IllegalArgumentException("so nguoi phai > 0");
        return new Booking(id, studentId, roomId, slot, people, fee, status);
    }

    private static void addChecked(List<Booking> list, Set<String> ids, Booking b) {
        if (!ids.add(b.getBookingId())) throw new IllegalArgumentException("trung ma booking " + b.getBookingId());
        list.add(b);
    }
}
