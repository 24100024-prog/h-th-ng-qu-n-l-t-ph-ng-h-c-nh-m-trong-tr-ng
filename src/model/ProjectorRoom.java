package model;

public class ProjectorRoom extends Room {
    private String projectorModel;

    public ProjectorRoom(String roomId, String roomName, int capacity, String projectorModel, double feePerHour) {
        super(roomId, roomName, capacity, new ProjectorRoomFeePolicy(feePerHour));
        this.projectorModel = projectorModel;
    }

    public String getProjectorModel() {
        return projectorModel;
    }

    @Override
    public void displayInfo() {
        System.out.println("[Phòng Máy Chiếu] ID: " + getRoomId() 
            + " | Tên: " + getRoomName() 
            + " | Sức chứa: " + getCapacity() 
            + " | Loại máy chiếu: " + projectorModel);
    }
}
