package model;

public class NormalRoom extends Room {
    public NormalRoom(String roomId, String roomName, int capacity, String status) {
        super(roomId, roomName, capacity, status);
    }

    @Override
    public String getType() { return "NORMAL"; }

    @Override
    public String getExtraInfo() { return ""; }
}
