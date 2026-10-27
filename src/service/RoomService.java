package service;

import java.util.ArrayList;
import java.util.List;

public class RoomService {
    private List<Room> rooms;

    public RoomService() {
        this.rooms = new ArrayList<>();
    }

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public Room findRoomById(String roomId) {
        for (Room room : rooms) {
            if (room.getRoomId().equalsIgnoreCase(roomId)) {
                return room;
            }
        }
        return null;
    }

    public void displayAllRooms() {
        System.out.println("=== DANH SÁCH PHÒNG ===");
        for (Room room : rooms) {
            room.displayInfo();
        }
    }

    public double calculateRoomBookingFee(String roomId, int hours) {
        Room room = findRoomById(roomId);
        if (room != null) {
            return room.calculateTotalFee(hours);
        }
        System.out.println("Không tìm thấy phòng có mã: " + roomId);
        return 0.0;
    }
}
