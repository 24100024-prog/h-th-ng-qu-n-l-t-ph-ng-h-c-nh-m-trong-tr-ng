package model;

public class SeminarRoom extends Room {
    private int stageCapacity;

    public SeminarRoom(String roomId, String roomName, int capacity, String status, int stageCapacity) {
        super(roomId, roomName, capacity, status);
        this.stageCapacity = stageCapacity;
    }

    public int getStageCapacity() { return stageCapacity; }
    public void setStageCapacity(int stageCapacity) { this.stageCapacity = stageCapacity; }
}
