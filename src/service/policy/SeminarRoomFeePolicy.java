package service.policy  
public class SeminarRoomFeePolicy implements RoomFeePolicy {
    private double baseFee;
    private double feePerHour;

    public SeminarRoomFeePolicy(double baseFee, double feePerHour) {
        this.baseFee = baseFee;
        this.feePerHour = feePerHour;
    }

    @Override
    public double calculateFee(int hours) {
        return baseFee + (hours * feePerHour);
    }
}
