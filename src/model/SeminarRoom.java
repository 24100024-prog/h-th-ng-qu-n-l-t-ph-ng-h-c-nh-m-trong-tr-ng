package model;

public class SeminarRoom extends Room {
    private boolean hasAudioSystem;

    public SeminarRoom(String roomId, String roomName, int capacity, boolean hasAudioSystem, double baseFee, double feePerHour) {
        super(roomId, roomName, capacity, new SeminarRoomFeePolicy(baseFee, feePerHour));
        this.hasAudioSystem = hasAudioSystem;
    }

    @Override
    public void displayInfo() {
        System.out.println("[Phòng Hội Thảo] ID: " + getRoomId() 
            + " | Tên: " + getRoomName() 
            + " | Sức chứa: " + getCapacity() 
            + " | Âm thanh: " + (hasAudioSystem ? "Có" : "Không"));
    }
}
