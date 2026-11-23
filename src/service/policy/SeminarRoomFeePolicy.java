package service.policy;

import model.Room;

/** Chinh sach tinh phi phong hoi thao gom phi co dinh va phi theo gio. */
public class SeminarRoomFeePolicy implements RoomFeePolicy {
    private final double baseFee;
    private final double feePerHour;

    public SeminarRoomFeePolicy(double baseFee, double feePerHour) {
        if (baseFee < 0 || feePerHour < 0) {
            throw new IllegalArgumentException("Don gia khong duoc am");
        }
        this.baseFee = baseFee;
        this.feePerHour = feePerHour;
    }

    @Override
    public double calculateFee(Room room, int hours) {
        if (!"SEMINAR".equals(room.getType())) return 0.0;
        return baseFee + feePerHour * Math.max(0, hours);
    }

    @Override
    public String getName() {
        return "Phong hoi thao (phi co dinh " + baseFee + " VND + " + feePerHour + " VND/gio)";
    }
}

