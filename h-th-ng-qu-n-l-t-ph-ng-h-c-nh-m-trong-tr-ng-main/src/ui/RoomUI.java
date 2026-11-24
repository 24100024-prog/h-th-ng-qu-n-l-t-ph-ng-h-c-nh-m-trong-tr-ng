package ui;

import model.NormalRoom;
import model.ProjectorRoom;
import model.Room;
import model.SeminarRoom;
import service.RoomService;
import utils.InputValidator;

import java.util.ArrayList;
import java.util.List;

public class RoomUI {
    public static final String TYPE_NORMAL = "NORMAL";
    public static final String TYPE_PROJECTOR = "PROJECTOR";
    public static final String TYPE_SEMINAR = "SEMINAR";

    private final ConsoleIO io;
    private final RoomService roomService;

    public RoomUI(ConsoleIO io, RoomService roomService) {
        this.io = io;
        this.roomService = roomService;
    }
    public static List<Room> filterByType(List<Room> rooms, String type) {
        if (type == null) return rooms;
        List<Room> result = new ArrayList<>();
        for (Room r : rooms) {
            if (type.equals(r.getType())) result.add(r);
        }
        return result;
    }

    public static String typeLabel(String type) {
        switch (type) {
            case TYPE_NORMAL: return "Thuong";
            case TYPE_PROJECTOR: return "May chieu";
            case TYPE_SEMINAR: return "Hoi thao";
            default: return type;
        }
    }

    public static String statusLabel(Room r) {
        return r.isUnderMaintenance() ? "BAO TRI" : "Hoat dong";
    }

    private static String extraLabel(Room r) {
        if (r instanceof ProjectorRoom) return "may chieu: " + (((ProjectorRoom) r).isHasProjector() ? "co" : "khong");
        if (r instanceof SeminarRoom) return "san khau: " + ((SeminarRoom) r).getStageCapacity() + " cho";
        return "";
    }

    public void run() {
        boolean back = false;
        while (!back) {
            System.out.println("\n[Phong] 1.Them  2.Dat trang thai bao tri/hoat dong  3.Xoa  4.Xem danh sach  0.Quay lai");
            switch (io.readLine("Chon: ").trim()) {
                case "1": addRoom(); break;
                case "2": changeStatus(); break;
                case "3": deleteRoom(); break;
                case "4": showRoomList(); break;
                case "0": back = true; break;
                default: System.out.println("Lua chon khong hop le.");
            }
        }
    }

    private boolean exists(String id) {
        return roomService.findRoom(id).isPresent();
    }

    private void addRoom() {
        System.out.println("--- Them phong (go '" + ConsoleIO.CANCEL_WORD + "' bat ky luc nao de quay lai) ---");
        System.out.println("Loai phong: 1.Thuong (NormalRoom)  2.May chieu (ProjectorRoom)  3.Hoi thao (SeminarRoom)");
        String type = io.ask("Chon loai: ", s ->
                (s.equals("1") || s.equals("2") || s.equals("3")) ? null : "Chi chon 1, 2 hoac 3");
        if (type == null) return;

        String id = io.ask("Ma phong: ", s -> {
            if (!InputValidator.isValidId(s)) return "Ma phong khong hop le (2-20 ky tu chu, so, '-' hoac '_')";
            if (exists(s)) return "Ma phong " + s + " da ton tai";
            return null;
        });
        if (id == null) return;
        String name = io.ask("Ten phong: ", s -> InputValidator.isNotBlank(s) ? null : "Ten phong khong duoc de trong");
        if (name == null) return;
        Integer capacity = io.askInt("Suc chua: ", 1, "Suc chua phai la so nguyen >= 1");
        if (capacity == null) return;

        Room room;
        if (type.equals("1")) {
            room = new NormalRoom(id, name, capacity, Room.ACTIVE);
        } else if (type.equals("2")) {
            boolean hasProjector = io.askYesNo("Phong co may chieu?");
            room = new ProjectorRoom(id, name, capacity, Room.ACTIVE, hasProjector);
        } else {
            Integer stage = io.askInt("Suc chua san khau: ", 0, "Suc chua san khau phai la so nguyen >= 0");
            if (stage == null) return;
            room = new SeminarRoom(id, name, capacity, Room.ACTIVE, stage);
        }
        io.guarded(() -> {
            roomService.addRoom(room);
            System.out.println("Da them phong " + id + " (" + typeLabel(room.getType()) + ")");
        });
    }

    private void changeStatus() {
        if (roomService.getAllRooms().isEmpty()) {
            System.out.println("Chua co phong nao.");
            return;
        }
        printRooms(roomService.getAllRooms());
        String id = io.ask("Ma phong: ", s -> exists(s) ? null : "Khong tim thay phong " + s);
        if (id == null) return;
        String choice = io.ask("1.Chuyen sang BAO TRI  2.Chuyen sang HOAT DONG: ", s ->
                (s.equals("1") || s.equals("2")) ? null : "Chi chon 1 hoac 2");
        if (choice == null) return;
        io.guarded(() -> {
            roomService.setMaintenance(id, choice.equals("1"));
            System.out.println("Phong " + id + " hien o trang thai " + statusLabel(roomService.getRoom(id)));
        });
    }

    private void deleteRoom() {
        String id = io.ask("Ma phong can xoa: ", s -> exists(s) ? null : "Khong tim thay phong " + s);
        if (id == null) return;
        if (!io.askYesNo("Xac nhan xoa phong " + id + "?")) {
            System.out.println("Da huy xoa.");
            return;
        }
        io.guarded(() -> {
            roomService.deleteRoom(id);
            System.out.println("Da xoa phong " + id);
        });
    }

    public void showRoomList() {
        List<Room> all = roomService.getAllRooms();
        if (all.isEmpty()) {
            System.out.println("Chua co phong nao.");
            return;
        }
        System.out.println("Xem: 1.Tat ca  2.Phong thuong  3.Phong may chieu  4.Phong hoi thao");
        String c = io.ask("Chon: ", s ->
                (s.equals("1") || s.equals("2") || s.equals("3") || s.equals("4")) ? null : "Chi chon tu 1 den 4");
        if (c == null) return;
        String type = c.equals("2") ? TYPE_NORMAL : c.equals("3") ? TYPE_PROJECTOR : c.equals("4") ? TYPE_SEMINAR : null;
        List<Room> list = filterByType(all, type);
        if (list.isEmpty()) {
            System.out.println("Khong co phong thuoc loai nay.");
            return;
        }
        printRooms(list);
    }

    private void printRooms(List<Room> rooms) {
        System.out.println(String.format("%-10s %-22s %-10s %-8s %-10s %s",
                "Ma phong", "Ten phong", "Loai", "Suc chua", "Trang thai", "Thong tin them"));
        System.out.println("-----------------------------------------------------------------------------------");
        for (Room r : rooms) {
            System.out.println(String.format("%-10s %-22s %-10s %-8d %-10s %s",
                    r.getRoomId(), r.getRoomName(), typeLabel(r.getType()), r.getCapacity(),
                    statusLabel(r), extraLabel(r)));
        }
        System.out.println("Tong: " + rooms.size() + " phong.");
    }
}