package main;

import model.Booking;
import model.BookingDetail;
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
import service.StudentService;
import service.policy.FreeFeePolicy;
import service.policy.HourlyFeePolicy;
import service.policy.RoomTypeFeePolicy;
import service.validator.BookingValidator;
import service.validator.CapacityValidator;
import service.validator.MaxHoursValidator;
import service.validator.OverlapValidator;
import service.validator.StudentExistsValidator;
import utils.DateTimeUtils;
import utils.InputValidator;
import utils.exceptions.BookingException;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private final Scanner scanner = new Scanner(System.in, "UTF-8");

    private final StudentRepository studentRepository = new StudentRepository();
    private final RoomRepository roomRepository = new RoomRepository();
    private final BookingRepository bookingRepository = new BookingRepository();

    private final StudentService studentService = new StudentService(studentRepository, bookingRepository);
    private final RoomService roomService = new RoomService(roomRepository, bookingRepository);
    private final DataService dataService = new DataService(studentRepository, roomRepository, bookingRepository);
    private final BookingService bookingService;

    public static void main(String[] args) throws Exception {
        System.setOut(new PrintStream(System.out, true, "UTF-8"));
        new Main().run();
    }

    public Main() {
        List<BookingValidator> validators = new ArrayList<>();
        validators.add(new StudentExistsValidator(studentRepository));
        validators.add(new CapacityValidator(roomRepository));
        validators.add(new OverlapValidator(bookingRepository));
        validators.add(new MaxHoursValidator(bookingRepository));
        bookingService = new BookingService(bookingRepository, roomRepository, new FreeFeePolicy(), validators);
    }

    private void run() {
        System.out.println("=== HE THONG QUAN LY DAT PHONG HOC NHOM ===");
        autoLoad();
        boolean running = true;
        while (running) {
            printMenu();
            switch (readLine("Chon chuc nang: ")) {
                case "1": studentMenu(); break;
                case "2": roomMenu(); break;
                case "3": listRooms(); break;
                case "4": checkFreeSlots(); break;
                case "5": createBooking(); break;
                case "6": listBookings(); break;
                case "7": showBookingDetail(); break;
                case "8": cancelBooking(); break;
                case "9": feeMenu(); break;
                case "10": saveData(); break;
                case "11": loadData(); break;
                case "0": running = false; break;
                default: System.out.println("Lua chon khong hop le, vui long nhap lai.");
            }
        }
        System.out.println("Tam biet!");
    }

    private void printMenu() {
        System.out.println("\n---------------- MENU ----------------");
        System.out.println(" 1. Quan ly sinh vien");
        System.out.println(" 2. Quan ly phong");
        System.out.println(" 3. Xem danh sach phong va trang thai");
        System.out.println(" 4. Kiem tra khung gio trong");
        System.out.println(" 5. Dat phong");
        System.out.println(" 6. Xem danh sach booking");
        System.out.println(" 7. Xem chi tiet booking");
        System.out.println(" 8. Huy booking");
        System.out.println(" 9. Thong tin / chinh sach phi");
        System.out.println("10. Luu du lieu");
        System.out.println("11. Tai du lieu");
        System.out.println(" 0. Thoat");
    }

    // ------------------------------------------------------------ sinh vien
    private void studentMenu() {
        System.out.println("\n[Sinh vien] 1.Them  2.Danh sach  3.Cap nhat  4.Xoa  0.Quay lai");
        switch (readLine("Chon: ")) {
            case "1": {
                String id = readLine("Ma SV: ");
                String name = readLine("Ho ten: ");
                String phone = readLine("SDT: ");
                String lop = readLine("Lop: ");
                String email = readLine("Email: ");
                runGuarded(() -> {
                    studentService.addStudent(new Student(id.trim(), name.trim(), phone.trim(), lop.trim(), email.trim()));
                    System.out.println("Da them sinh vien " + id.trim());
                });
                break;
            }
            case "2":
                listStudents();
                break;
            case "3": {
                String id = readLine("Ma SV can sua: ").trim();
                if (!studentRepository.existsById(id)) {
                    System.out.println("Khong tim thay sinh vien " + id);
                    break;
                }
                String name = readLine("Ho ten moi: ");
                String phone = readLine("SDT moi: ");
                String lop = readLine("Lop moi: ");
                String email = readLine("Email moi: ");
                runGuarded(() -> {
                    studentService.updateStudent(id, name.trim(), phone.trim(), lop.trim(), email.trim());
                    System.out.println("Da cap nhat sinh vien " + id);
                });
                break;
            }
            case "4": {
                String id = readLine("Ma SV can xoa: ").trim();
                runGuarded(() -> {
                    studentService.deleteStudent(id);
                    System.out.println("Da xoa sinh vien " + id);
                });
                break;
            }
            case "0": break;
            default: System.out.println("Lua chon khong hop le.");
        }
    }

    private void listStudents() {
        List<Student> list = studentService.getAllStudents();
        if (list.isEmpty()) {
            System.out.println("Chua co sinh vien nao.");
            return;
        }
        for (Student s : list) s.hienThiThongTin();
    }

    // ------------------------------------------------------------ phong
    private void roomMenu() {
        System.out.println("\n[Phong] 1.Them  2.Dat trang thai bao tri/hoat dong  3.Xoa  0.Quay lai");
        switch (readLine("Chon: ")) {
            case "1": addRoom(); break;
            case "2": {
                String id = readLine("Ma phong: ").trim();
                String choice = readLine("1.Chuyen sang BAO TRI  2.Chuyen sang HOAT DONG: ");
                if (!choice.equals("1") && !choice.equals("2")) {
                    System.out.println("Lua chon khong hop le.");
                    break;
                }
                runGuarded(() -> {
                    roomService.setMaintenance(id, choice.equals("1"));
                    System.out.println("Phong " + id + " hien o trang thai " + roomService.getRoom(id).getStatus());
                });
                break;
            }
            case "3": {
                String id = readLine("Ma phong can xoa: ").trim();
                runGuarded(() -> {
                    roomService.deleteRoom(id);
                    System.out.println("Da xoa phong " + id);
                });
                break;
            }
            case "0": break;
            default: System.out.println("Lua chon khong hop le.");
        }
    }

    private void addRoom() {
        String type = readLine("Loai phong (1.Thuong  2.May chieu  3.Hoi thao): ");
        if (!type.equals("1") && !type.equals("2") && !type.equals("3")) {
            System.out.println("Loai phong khong hop le.");
            return;
        }
        String id = readLine("Ma phong: ").trim();
        String name = readLine("Ten phong: ").trim();
        Integer capacity = readIntOrNull("Suc chua: ");
        if (capacity == null) return;
        Room room;
        if (type.equals("1")) {
            room = new NormalRoom(id, name, capacity, Room.ACTIVE);
        } else if (type.equals("2")) {
            String has = readLine("Co may chieu? (y/n): ").trim().toLowerCase();
            room = new ProjectorRoom(id, name, capacity, Room.ACTIVE, has.equals("y"));
        } else {
            Integer stage = readIntOrNull("Suc chua san khau: ");
            if (stage == null) return;
            if (stage < 0) {
                System.out.println("Suc chua san khau khong duoc am.");
                return;
            }
            room = new SeminarRoom(id, name, capacity, Room.ACTIVE, stage);
        }
        runGuarded(() -> {
            roomService.addRoom(room);
            System.out.println("Da them phong " + id);
        });
    }

    private void listRooms() {
        List<Room> rooms = roomService.getAllRooms();
        if (rooms.isEmpty()) {
            System.out.println("Chua co phong nao.");
            return;
        }
        for (Room r : rooms) System.out.println(r);
    }

    private void checkFreeSlots() {
        String roomId = readLine("Ma phong: ").trim();
        LocalDate date = readDate("Ngay (dd/MM/yyyy): ");
        if (date == null) return;
        runGuarded(() -> {
            Room room = roomService.getRoom(roomId);
            if (room.isUnderMaintenance()) {
                System.out.println(room.getRoomName() + " dang bao tri, khong co gio trong.");
                return;
            }
            List<TimeSlot> free = roomService.findFreeSlots(roomId, date);
            System.out.println("Gio trong cua " + room.getRoomName() + " ngay " + DateTimeUtils.formatDate(date)
                    + " (tu " + RoomService.OPEN_TIME + " den " + RoomService.CLOSE_TIME + "):");
            if (free.isEmpty()) System.out.println("  (het cho)");
            for (TimeSlot s : free) System.out.println("  " + s);
        });
    }

    // ------------------------------------------------------------ booking
    private void createBooking() {
        String studentId = readLine("Ma SV: ").trim();
        String roomId = readLine("Ma phong: ").trim();
        LocalDateTime start = readDateTime("Gio bat dau (dd/MM/yyyy HH:mm): ");
        if (start == null) return;
        LocalDateTime end = readDateTime("Gio ket thuc (dd/MM/yyyy HH:mm): ");
        if (end == null) return;
        Integer people = readIntOrNull("So nguoi: ");
        if (people == null) return;
        try {
            Booking b = bookingService.createBooking(studentId, roomId, new TimeSlot(start, end), people);
            System.out.println("Dat phong thanh cong! Ma booking: " + b.getBookingId()
                    + " - Phi: " + DateTimeUtils.formatMoney(b.getFee()));
        } catch (BookingException e) {
            System.out.println("Khong the dat phong: " + e.getMessage());
        }
    }

    private void listBookings() {
        List<Booking> list = bookingService.getAllBookings();
        if (list.isEmpty()) {
            System.out.println("Chua co booking nao.");
            return;
        }
        for (Booking b : list) System.out.println(b);
    }

    private void showBookingDetail() {
        String id = readLine("Ma booking: ").trim();
        try {
            BookingDetail d = bookingService.getDetail(id, studentRepository);
            System.out.println(d.describe());
        } catch (BookingException e) {
            System.out.println(e.getMessage());
        }
    }

    private void cancelBooking() {
        String id = readLine("Ma booking: ").trim();
        String studentId = readLine("Ma SV dat phong: ").trim();
        try {
            bookingService.cancelBooking(id, studentId);
            System.out.println("Da huy booking " + id + ". Khung gio nay da trong tro lai.");
        } catch (BookingException e) {
            System.out.println("Khong the huy: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------ phi
    private void feeMenu() {
        System.out.println("\nChinh sach phi hien tai: " + bookingService.getFeePolicy().getName());
        System.out.println("1.Uoc tinh phi  2.Doi chinh sach phi (chi ap dung cho booking moi)  3.Xem phi cac booking  0.Quay lai");
        switch (readLine("Chon: ")) {
            case "1": {
                String roomId = readLine("Ma phong: ").trim();
                LocalDateTime start = readDateTime("Gio bat dau (dd/MM/yyyy HH:mm): ");
                if (start == null) return;
                LocalDateTime end = readDateTime("Gio ket thuc (dd/MM/yyyy HH:mm): ");
                if (end == null) return;
                try {
                    double fee = bookingService.estimateFee(roomId, new TimeSlot(start, end));
                    System.out.println("Phi uoc tinh: " + DateTimeUtils.formatMoney(fee));
                } catch (BookingException e) {
                    System.out.println(e.getMessage());
                }
                break;
            }
            case "2": changeFeePolicy(); break;
            case "3":
                for (Booking b : bookingService.getAllBookings()) {
                    System.out.println(b.getBookingId() + ": " + DateTimeUtils.formatMoney(b.getFee())
                            + (b.isActive() ? "" : " (da huy)"));
                }
                if (bookingService.getAllBookings().isEmpty()) System.out.println("Chua co booking nao.");
                break;
            case "0": break;
            default: System.out.println("Lua chon khong hop le.");
        }
    }

    private void changeFeePolicy() {
        System.out.println("1.Mien phi  2.Theo gio (cung don gia)  3.Theo loai phong");
        switch (readLine("Chon: ")) {
            case "1":
                bookingService.setFeePolicy(new FreeFeePolicy());
                break;
            case "2": {
                Double rate = readRate("Don gia moi gio (VND): ");
                if (rate == null) return;
                bookingService.setFeePolicy(new HourlyFeePolicy(rate));
                break;
            }
            case "3": {
                Map<String, Double> rates = new LinkedHashMap<>();
                String[] types = {"NORMAL", "PROJECTOR", "SEMINAR"};
                for (String t : types) {
                    Double rate = readRate("Don gia moi gio cho phong " + t + " (VND): ");
                    if (rate == null) return;
                    rates.put(t, rate);
                }
                bookingService.setFeePolicy(new RoomTypeFeePolicy(rates));
                break;
            }
            default:
                System.out.println("Lua chon khong hop le.");
                return;
        }
        System.out.println("Da doi chinh sach phi: " + bookingService.getFeePolicy().getName());
    }

    // ------------------------------------------------------------ file
    private void autoLoad() {
        Path dir = DataService.defaultDir();
        if (dataService.exists(dir, false)) {
            try {
                dataService.loadAll(dir, false);
                System.out.println("Da tai du lieu tu thu muc '" + dir + "' (text).");
            } catch (IOException e) {
                System.out.println("Khong tai duoc du lieu cu, bat dau voi du lieu trong: " + e.getMessage());
            }
        } else {
            System.out.println("Chua co du lieu da luu, bat dau voi du lieu trong.");
        }
    }

    private void saveData() {
        Boolean binary = readFormat();
        if (binary == null) return;
        try {
            dataService.saveAll(DataService.defaultDir(), binary);
            System.out.println("Da luu du lieu vao thu muc '" + DataService.defaultDir() + "'.");
        } catch (IOException e) {
            System.out.println("Luu that bai: " + e.getMessage());
        }
    }

    private void loadData() {
        Boolean binary = readFormat();
        if (binary == null) return;
        try {
            dataService.loadAll(DataService.defaultDir(), binary);
            System.out.println("Da tai du lieu: " + studentRepository.findAll().size() + " sinh vien, "
                    + roomRepository.findAll().size() + " phong, " + bookingRepository.findAll().size() + " booking.");
        } catch (IOException e) {
            System.out.println("Tai that bai (du lieu hien tai duoc giu nguyen): " + e.getMessage());
        }
    }

    private Boolean readFormat() {
        String c = readLine("Dinh dang (1.Text  2.Binary): ").trim();
        if (c.equals("1")) return false;
        if (c.equals("2")) return true;
        System.out.println("Lua chon khong hop le.");
        return null;
    }

    // ------------------------------------------------------------ nhap lieu
    private String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            System.out.println("\nKet thuc du lieu nhap, thoat chuong trinh.");
            System.exit(0);
        }
        return scanner.nextLine();
    }

    private Integer readIntOrNull(String prompt) {
        Integer v = InputValidator.parseInt(readLine(prompt));
        if (v == null) System.out.println("Vui long nhap mot so nguyen.");
        return v;
    }

    private Double readRate(String prompt) {
        Double v = InputValidator.parseNonNegativeDouble(readLine(prompt));
        if (v == null) System.out.println("Don gia phai la so khong am.");
        return v;
    }

    private LocalDateTime readDateTime(String prompt) {
        LocalDateTime v = DateTimeUtils.parseDateTime(readLine(prompt));
        if (v == null) System.out.println("Sai dinh dang hoac ngay gio khong ton tai (can dd/MM/yyyy HH:mm).");
        return v;
    }

    private LocalDate readDate(String prompt) {
        LocalDate v = DateTimeUtils.parseDate(readLine(prompt));
        if (v == null) System.out.println("Sai dinh dang hoac ngay khong ton tai (can dd/MM/yyyy).");
        return v;
    }

    private void runGuarded(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Loi: " + e.getMessage());
        }
    }
}
