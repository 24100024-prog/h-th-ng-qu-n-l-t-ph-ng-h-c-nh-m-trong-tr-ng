package model;

public class SeminarRoom extends Room {
    private int stageCapacity;

    public SeminarRoom(String roomId, String roomName, int capacity, String status, int stageCapacity) {
        super(roomId, roomName, capacity, status);
        this.stageCapacity = stageCapacity;
    }

    public int getStageCapacity() { return stageCapacity; }
    public void setStageCapacity(int stageCapacity) { this.stageCapacity = stageCapacity; }

    @Override
    public String getType() { return "SEMINAR"; }

    @Override
    public String getExtraInfo() { return String.valueOf(stageCapacity); }

    @Override
    public String toString() { return super.toString() + " | suc chua san khau: " + stageCapacity; }
}
