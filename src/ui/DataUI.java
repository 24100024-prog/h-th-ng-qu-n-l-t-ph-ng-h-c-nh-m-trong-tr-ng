package ui;

import repository.BookingRepository;
import repository.RoomRepository;
import repository.StudentRepository;
import service.DataService;

import java.io.IOException;

public class DataUI {
    private final ConsoleIO io;
    private final DataService dataService;
    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public DataUI(ConsoleIO io, DataService dataService, StudentRepository studentRepository,
                  RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.io = io;
        this.dataService = dataService;
        this.studentRepository = studentRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    public void save() {
        Boolean binary = askFormat();
        if (binary == null) return;
        try {
            dataService.saveAll(DataService.defaultDir(), binary);
            System.out.println("Da luu du lieu vao thu muc '" + DataService.defaultDir() + "'.");
        } catch (IOException e) {
            System.out.println("Luu that bai: " + e.getMessage());
        }
    }

    public void load() {
        Boolean binary = askFormat();
        if (binary == null) return;
        if (!io.askYesNo("Tai du lieu se thay the du lieu hien tai. Tiep tuc?")) {
            System.out.println("Da huy tai du lieu.");
            return;
        }
        try {
            dataService.loadAll(DataService.defaultDir(), binary);
            System.out.println("Da tai du lieu: " + studentRepository.findAll().size() + " sinh vien, "
                    + roomRepository.findAll().size() + " phong, " + bookingRepository.findAll().size() + " booking.");
        } catch (IOException e) {
            System.out.println("Tai that bai (du lieu hien tai duoc giu nguyen): " + e.getMessage());
        }
    }

    private Boolean askFormat() {
        String c = io.ask("Dinh dang (1.Text  2.Binary): ", s ->
                (s.equals("1") || s.equals("2")) ? null : "Chi chon 1 hoac 2");
        if (c == null) return null;
        return c.equals("2");
    }
}