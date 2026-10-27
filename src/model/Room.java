package model;

public abstract class Room {
    public static final String ACTIVE = "DANG_HOAT_DONG";
    public static final String MAINTENANCE = "BAO_TRI";

    private String roomId;
    private String roomName;
    private int capacity;
    private String status;

    public Room() {}

    public Room(String roomId, String roomName, int capacity, String status) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.capacity = capacity;
        setStatus(status);
    }

    /** Ma loai phong dung khi luu file va chon chinh sach phi: NORMAL, PROJECTOR, SEMINAR. */
    public abstract String getType();

    /** Thuoc tinh rieng cua tung loai phong (dang text) de luu file. */
    public abstract String getExtraInfo();

    public boolean isUnderMaintenance() { return MAINTENANCE.equals(status); }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public String getStatus() { return status; }

    public void setStatus(String status) {
        if (!ACTIVE.equals(status) && !MAINTENANCE.equals(status)) {
            throw new IllegalArgumentException("Trang thai phong khong hop le: " + status);
        }
        this.status = status;
    }

    @Override
    public String toString() {
        return roomId + " | " + roomName + " | " + getType() + " | suc chua " + capacity + " | " + status;
    }
}
