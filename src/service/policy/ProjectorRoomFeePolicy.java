package service.policy
public class ProjectorRoomFeePolicy implements RoomFeePolicy {
    private double feePerHour;

    public ProjectorRoomFeePolicy(double feePerHour) {
        this.feePerHour = feePerHour;
    }

    @Override
    public double calculateFee(int hours) {
        return hours * feePerHour;
    }
}
