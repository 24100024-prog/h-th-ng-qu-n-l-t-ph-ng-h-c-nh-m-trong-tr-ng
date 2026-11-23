package service.policy;

import model.Room;

/** Chinh sach tinh phi phong may chieu theo don gia moi gio. */
public class ProjectorRoomFeePolicy implements RoomFeePolicy {
    private final double feePerHour;

    public ProjectorRoomFeePolicy(double feePerHour) {
        if (feePerHour < 0) throw new IllegalArgumentException("Don gia khong duoc am");
        this.feePerHour = feePerHour;
    }

    @Override
    public double calculateFee(Room room, int hours) {
        return "PROJECTOR".equals(room.getType()) ? feePerHour * Math.max(0, hours) : 0.0;
    }

    @Override
    public String getName() {
        return "Phong may chieu (" + feePerHour + " VND/gio)";
    }
}

