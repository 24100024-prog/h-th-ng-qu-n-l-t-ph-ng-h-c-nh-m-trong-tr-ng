package model;

public abstract class Room {
    private String roomId;
    private String roomName;
    private int capacity;
    private String status; // "DANG_HOAT_DONG", "BAO_TRI"

    public Room() {}

    public Room(String roomId, String roomName, int capacity, String status) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.capacity = capacity;
        this.status = status;
    }

    // Getter & Setter
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}