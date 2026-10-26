package model;

public class ProjectorRoom extends Room {
    private boolean hasProjector;

    public ProjectorRoom(String roomId, String roomName, int capacity, String status, boolean hasProjector) {
        super(roomId, roomName, capacity, status);
        this.hasProjector = hasProjector;
    }

    public boolean isHasProjector() { return hasProjector; }
    public void setHasProjector(boolean hasProjector) { this.hasProjector = hasProjector; }
}
