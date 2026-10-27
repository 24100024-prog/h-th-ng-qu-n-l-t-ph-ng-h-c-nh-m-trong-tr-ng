package model;

public abstract class Room {
    private String roomId;
    private String roomName;
    private int capacity;
    protected RoomFeePolicy feePolicy;

    public Room(String roomId, String roomName, int capacity, RoomFeePolicy feePolicy) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.capacity = capacity;
        this.feePolicy = feePolicy;
    }

    public double calculateTotalFee(int hours) {
        if (feePolicy == null) {
            return 0.0;
        }
        return feePolicy.calculateFee(hours);
    }

    public void setFeePolicy(RoomFeePolicy feePolicy) {
        this.feePolicy = feePolicy;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public int getCapacity() {
        return capacity;
    }

    public abstract void displayInfo();
}
