package main;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.SwingUtilities;
import repository.BookingRepository;
import repository.RoomRepository;
import repository.StudentRepository;
import service.BookingService;
import service.DataService;
import service.RoomService;
import service.StudentService;
import service.policy.FreeFeePolicy;
import service.validator.BookingValidator;
import service.validator.CapacityValidator;
import service.validator.MaxHoursValidator;
import service.validator.OverlapValidator;
import service.validator.StudentExistsValidator;
import ui.BookingUI;
import ui.ConsoleIO;
import ui.DataUI;
import ui.FeeUI;
import ui.MainGUI;
import ui.RoomUI;
import ui.StudentUI;
import utils.DateTimeUtils;
import utils.InputValidator;

public class Main {
    private final Scanner scanner = new Scanner(System.in, "UTF-8");

    private final StudentRepository studentRepository = new StudentRepository();
    private final RoomRepository roomRepository = new RoomRepository();
    private final BookingRepository bookingRepository = new BookingRepository();

    private final StudentService studentService = new StudentService(studentRepository, bookingRepository);
    private final RoomService roomService = new RoomService(roomRepository, bookingRepository);
    private final DataService dataService = new DataService(studentRepository, roomRepository, bookingRepository);
    private final ConsoleIO io = new ConsoleIO(scanner); // dung chung Scanner voi Main
    private final StudentUI studentUI = new StudentUI(io, studentService);
    private final RoomUI roomUI = new RoomUI(io, roomService);
    private final DataUI dataUI = new DataUI(io, dataService, studentRepository, roomRepository, bookingRepository);
    private final BookingService bookingService;
    private final BookingUI bookingUI;
    private final FeeUI feeUI;

    public static void main(String[] args) throws Exception {
        System.setOut(new PrintStream(System.out, true, "UTF-8"));
        SwingUtilities.invokeLater(() -> {
            Main app = new Main();
            app.autoLoad();
            new MainGUI(app.roomService, app.bookingService, app.studentService, app.dataService).setVisible(true);
        });
    }

    public Main() {
        List<BookingValidator> validators = new ArrayList<>();
        validators.add(new StudentExistsValidator(studentRepository));
        validators.add(new CapacityValidator(roomRepository));
        validators.add(new OverlapValidator(bookingRepository));
        validators.add(new MaxHoursValidator(bookingRepository));
        bookingService = new BookingService(bookingRepository, roomRepository, new FreeFeePolicy(), validators);
        bookingUI = new BookingUI(io, bookingService, roomService, studentRepository);
        feeUI = new FeeUI(io, bookingService);
    }

    private void run() {
        System.out.println("=== HE THONG QUAN LY DAT PHONG HOC NHOM ===");
        autoLoad();
        boolean running = true;
        while (running) {
            printMenu();
            switch (readLine("Chon chuc nang: ")) {
                case "1": studentUI.run(); break;
                case "2": roomUI.run(); break;
                case "3": roomUI.showRoomList(); break;
                case "4": bookingUI.checkFreeSlots(); break;
                case "5": bookingUI.createBooking(); break;
                case "6": bookingUI.listBookings(); break;
                case "7": bookingUI.showBookingDetail(); break;
                case "8": bookingUI.cancelBooking(); break;
                case "9": feeUI.run(); break;
                case "10": dataUI.save(); break;
                case "11": dataUI.load(); break;
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

