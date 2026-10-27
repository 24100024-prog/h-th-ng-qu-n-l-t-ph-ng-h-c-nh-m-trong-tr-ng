package model;

public class NormalRoom extends Room {

    public NormalRoom(String roomId, String roomName, int capacity) {
        super(roomId, roomName, capacity, new FreeRoomFeePolicy());
    }

    @Override
    public void displayInfo() {
        System.out.println("[Phòng Thường] ID: " + getRoomId() 
            + " | Tên: " + getRoomName() 
            + " | Sức chứa: " + getCapacity() + " người");
    }
}
