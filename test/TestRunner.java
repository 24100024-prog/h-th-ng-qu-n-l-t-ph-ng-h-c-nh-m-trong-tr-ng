package test;

import model.Booking;
import model.NormalRoom;
import model.ProjectorRoom;
import model.Room;
import model.SeminarRoom;
import model.Student;
import model.TimeSlot;
import repository.BookingRepository;
import repository.RoomRepository;
import repository.StudentRepository;
import service.BookingService;
import service.DataService;
import service.RoomService;
import service.policy.FreeFeePolicy;
import service.policy.HourlyFeePolicy;
import service.policy.RoomTypeFeePolicy;
import service.validator.BookingValidator;
import service.validator.CapacityValidator;
import service.validator.MaxHoursValidator;
import service.validator.OverlapValidator;
import service.validator.StudentExistsValidator;
import utils.exceptions.BookingException;
import utils.exceptions.InvalidTimeException;
import utils.exceptions.OverlapBookingException;
import utils.exceptions.RoomMaintenanceException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Chay: java -cp out test.TestRunner  (khong can JUnit). Thoat voi ma 1 neu co kich ban that bai. */
public class TestRunner {
    private static final LocalDateTime NOW = LocalDateTime.of(2030, 1, 1, 8, 0);
    private static int passed = 0;
    private static int failed = 0;

    private StudentRepository students;
    private RoomRepository rooms;
    private BookingRepository bookings;
    private BookingService service;
    private RoomService roomService;

    interface Scenario { void run() throws Exception; }

    public static void main(String[] args) {
        TestRunner t = new TestRunner();
        t.scenario("1. Dat phong hop le", t::validBooking);
        t.scenario("2. Thoi gian khong hop le", t::invalidTime);
        t.scenario("3. Xung dot lich cung phong", t::overlap);
        t.scenario("4. Phong bao tri", t::maintenance);
        t.scenario("5. Huy booking cap nhat lich", t::cancel);
        t.scenario("6. File I/O text va binary", t::fileIo);
        t.scenario("7. Tinh phi theo chinh sach", t::fees);
        t.scenario("8. Du lieu sai / file loi", t::badInputAndFiles);
        System.out.println("\nKet qua: " + passed + " dat, " + failed + " that bai");
        System.exit(failed == 0 ? 0 : 1);
    }

    private void setUp() {
        students = new StudentRepository();
        rooms = new RoomRepository();
        bookings = new BookingRepository();
        students.add(new Student("SV001", "An", "0900000001", "CNTT1", "an@x.vn"));
        students.add(new Student("SV002", "Binh", "0900000002", "CNTT2", "binh@x.vn"));
        rooms.add(new NormalRoom("P101", "Phong 101", 8, Room.ACTIVE));
        rooms.add(new ProjectorRoom("P102", "Phong 102", 10, Room.ACTIVE, true));
        rooms.add(new SeminarRoom("P201", "Hoi thao 201", 50, Room.MAINTENANCE, 10));
        List<BookingValidator> v = new ArrayList<>();
        v.add(new StudentExistsValidator(students));
        v.add(new CapacityValidator(rooms));
        v.add(new OverlapValidator(bookings));
        v.add(new MaxHoursValidator(bookings));
        service = new BookingService(bookings, rooms, new FreeFeePolicy(), v);
        roomService = new RoomService(rooms, bookings);
    }

    private static TimeSlot slot(int startHour, int endHour) {
        return new TimeSlot(LocalDateTime.of(2030, 1, 2, startHour, 0), LocalDateTime.of(2030, 1, 2, endHour, 0));
    }

    // ---- kich ban
    private void validBooking() throws Exception {
        Booking b = service.createBooking("SV001", "P101", slot(9, 11), 5, NOW);
        check(b.isActive(), "booking phai con hieu luc");
        check(bookings.findAll().size() == 1, "booking phai duoc luu vao repository");
        check("BK001".equals(b.getBookingId()), "ma booking dau tien la BK001");
        check(!roomService.isAvailable("P101", slot(10, 12)), "phong khong con trong o khung da dat");
    }

    private void invalidTime() throws Exception {
        expect(InvalidTimeException.class, () -> service.createBooking("SV001", "P101", slot(11, 9), 5, NOW));
        expect(InvalidTimeException.class, () -> service.createBooking("SV001", "P101", slot(9, 9), 5, NOW));
        expect(InvalidTimeException.class, () -> service.createBooking("SV001", "P101",
                new TimeSlot(NOW.minusHours(3), NOW.minusHours(1)), 5, NOW));
        check(bookings.findAll().isEmpty(), "khong duoc tao booking khi thoi gian sai");
    }

    private void overlap() throws Exception {
        service.createBooking("SV001", "P101", slot(9, 11), 5, NOW);
        expect(OverlapBookingException.class, () -> service.createBooking("SV002", "P101", slot(10, 12), 5, NOW));
        expect(OverlapBookingException.class, () -> service.createBooking("SV002", "P101", slot(8, 10), 5, NOW));
        service.createBooking("SV002", "P101", slot(11, 12), 5, NOW);   // lien ke: hop le
        service.createBooking("SV002", "P102", slot(9, 11), 5, NOW);    // phong khac: hop le
        check(bookings.findAll().size() == 3, "chi 3 booking hop le duoc luu");
    }

    private void maintenance() throws Exception {
        expect(RoomMaintenanceException.class, () -> service.createBooking("SV001", "P201", slot(9, 11), 5, NOW));
        check(roomService.findFreeSlots("P201", NOW.toLocalDate().plusDays(1)).isEmpty(), "phong bao tri khong co gio trong");
        roomService.setMaintenance("P201", false);
        service.createBooking("SV001", "P201", slot(9, 11), 5, NOW);    // het bao tri thi dat duoc
    }

    private void cancel() throws Exception {
        Booking b = service.createBooking("SV001", "P101", slot(9, 11), 5, NOW);
        check(!roomService.isAvailable("P101", slot(9, 11)), "dang bi chiem");
        expect(BookingException.class, () -> service.cancelBooking(b.getBookingId(), "SV002", NOW));
        service.cancelBooking(b.getBookingId(), "SV001", NOW);
        check(!b.isActive(), "booking chuyen sang da huy");
        check(roomService.isAvailable("P101", slot(9, 11)), "sau khi huy phong phai trong");
        check(roomService.findFreeSlots("P101", slot(9, 11).getStartTime().toLocalDate()).size() == 1,
                "toan bo ngay trong thanh 1 khoang lien");
        expect(BookingException.class, () -> service.cancelBooking(b.getBookingId(), "SV001", NOW));
        service.createBooking("SV002", "P101", slot(9, 11), 5, NOW);    // dat lai duoc
    }

    private void fileIo() throws Exception {
        service.createBooking("SV001", "P101", slot(9, 11), 5, NOW);
        Booking cancelled = service.createBooking("SV002", "P102", slot(13, 15), 4, NOW);
        service.cancelBooking(cancelled.getBookingId(), "SV002", NOW);
        students.add(new Student("SV003", "Co|dau\\dac biet", "0900000003", "L", "c@x.vn"));

        Path dir = Files.createTempDirectory("qldp-test");
        DataService data = new DataService(students, rooms, bookings);
        for (boolean binary : new boolean[]{false, true}) {
            data.saveAll(dir, binary);
            StudentRepository s2 = new StudentRepository();
            RoomRepository r2 = new RoomRepository();
            BookingRepository b2 = new BookingRepository();
            new DataService(s2, r2, b2).loadAll(dir, binary);
            String mode = binary ? "binary" : "text";
            check(s2.findAll().size() == 3 && r2.findAll().size() == 3 && b2.findAll().size() == 2, mode + ": so luong ban ghi");
            check("Co|dau\\dac biet".equals(s2.findById("SV003").get().getName()), mode + ": ky tu dac biet giu nguyen");
            check(r2.findById("P201").get().isUnderMaintenance(), mode + ": trang thai phong");
            check(r2.findById("P102") .get() instanceof ProjectorRoom, mode + ": loai phong");
            Booking x = b2.findById("BK002").get();
            check(!x.isActive() && x.getNumberOfPeople() == 4 && x.getTimeSlot().equals(slot(13, 15)), mode + ": booking da huy");
            check(b2.findById("BK001").get().getFee() == bookings.findById("BK001").get().getFee(), mode + ": phi");
        }
    }

    private void fees() throws Exception {
        TimeSlot twoHours = slot(9, 11);
        Room normal = rooms.findById("P101").get();
        Room projector = rooms.findById("P102").get();
        check(new FreeFeePolicy().calculateFee(normal, 2) == 0.0, "mien phi");
        check(new HourlyFeePolicy(20000).calculateFee(projector, 3) == 60000.0, "theo gio");
        Map<String, Double> rates = new LinkedHashMap<>();
        rates.put("NORMAL", 10000.0);
        rates.put("PROJECTOR", 30000.0);
        RoomTypeFeePolicy byType = new RoomTypeFeePolicy(rates);
        check(byType.calculateFee(normal, 2) == 20000.0, "loai thuong");
        check(byType.calculateFee(projector, 2) == 60000.0, "loai may chieu");

        service.setFeePolicy(byType);
        Booking b = service.createBooking("SV001", "P102", twoHours, 5, NOW);
        check(b.getFee() == 60000.0, "phi luu trong booking dung chinh sach");
        check(service.getDetail(b.getBookingId(), students).getFee() == b.getFee(), "phi hien thi khop phi luu");
        // 90 phut lam tron len 2 gio
        Booking c = service.createBooking("SV002", "P101", new TimeSlot(
                LocalDateTime.of(2030, 1, 2, 9, 0), LocalDateTime.of(2030, 1, 2, 10, 30)), 5, NOW);
        check(c.getFee() == 20000.0, "90 phut tinh 2 gio");
        service.setFeePolicy(new FreeFeePolicy());
        check(b.getFee() == 60000.0, "doi chinh sach khong lam doi phi booking cu");
    }

    private void badInputAndFiles() throws Exception {
        expect(BookingException.class, () -> service.createBooking("", "P101", slot(9, 10), 5, NOW));
        expect(BookingException.class, () -> service.createBooking("SV999", "P101", slot(9, 10), 5, NOW));
        expect(BookingException.class, () -> service.createBooking("SV001", "XXX", slot(9, 10), 5, NOW));
        expect(BookingException.class, () -> service.createBooking("SV001", "P101", slot(9, 10), 0, NOW));
        expect(BookingException.class, () -> service.createBooking("SV001", "P101", slot(9, 10), 99, NOW));
        expect(BookingException.class, () -> service.createBooking("SV001", "P101", slot(8, 14), 5, NOW)); // 6 gio > 4 gio/ngay
        check(bookings.findAll().isEmpty(), "du lieu sai khong tao booking");

        Path dir = Files.createTempDirectory("qldp-bad");
        DataService data = new DataService(students, rooms, bookings);
        expectIo(() -> data.loadAll(dir, false), "thieu file text");
        expectIo(() -> data.loadAll(dir, true), "thieu file binary");

        Files.write(dir.resolve("students.txt"), "SV1|chi|ba truong\n".getBytes("UTF-8"));
        Files.write(dir.resolve("rooms.txt"), new byte[0]);
        Files.write(dir.resolve("bookings.txt"), new byte[0]);
        expectIo(() -> data.loadAll(dir, false), "text sai so truong");
        check(students.findAll().size() == 2, "du lieu cu giu nguyen sau khi tai loi");

        Files.write(dir.resolve("students.bin"), new byte[]{1, 2, 3});
        Files.write(dir.resolve("rooms.bin"), new byte[]{1, 2, 3});
        Files.write(dir.resolve("bookings.bin"), new byte[]{1, 2, 3});
        expectIo(() -> data.loadAll(dir, true), "binary hong");

        Files.write(dir.resolve("students.txt"), "SV1|An|0900000001|L|a@x.vn\n".getBytes("UTF-8"));
        Files.write(dir.resolve("rooms.txt"), "NORMAL|P1|Phong|abc|DANG_HOAT_DONG|\n".getBytes("UTF-8"));
        expectIo(() -> data.loadAll(dir, false), "text sai kieu du lieu");

        Files.write(dir.resolve("rooms.txt"), "NORMAL|P1|Phong|5|DANG_HOAT_DONG|\n".getBytes("UTF-8"));
        Files.write(dir.resolve("bookings.txt"),
                "BK001|SV1|P404|2030-01-02T09:00|2030-01-02T10:00|2|0.0|HIEU_LUC\n".getBytes("UTF-8"));
        expectIo(() -> data.loadAll(dir, false), "booking tham chieu phong khong ton tai");
    }

    // ---- ha tang kiem thu
    private void scenario(String name, Scenario s) {
        try {
            setUp();
            s.run();
            passed++;
            System.out.println("[PASS] " + name);
        } catch (AssertionError | Exception e) {
            failed++;
            System.out.println("[FAIL] " + name + " -> " + e);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void expect(Class<? extends Exception> type, Scenario action) {
        try {
            action.run();
        } catch (Exception e) {
            if (type.isInstance(e)) return;
            throw new AssertionError("can " + type.getSimpleName() + " nhung nhan " + e);
        }
        throw new AssertionError("can nem " + type.getSimpleName() + " nhung khong co loi");
    }

    private static void expectIo(Scenario action, String what) {
        try {
            action.run();
        } catch (IOException e) {
            return;
        } catch (Exception e) {
            throw new AssertionError(what + ": can IOException nhung nhan " + e);
        }
        throw new AssertionError(what + ": can IOException nhung khong co loi");
    }
}
