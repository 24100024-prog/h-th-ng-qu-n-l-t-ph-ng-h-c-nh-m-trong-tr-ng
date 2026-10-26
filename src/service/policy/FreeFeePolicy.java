package service.policy;

import model.Room;

public class FreeFeePolicy implements RoomFeePolicy {
    @Override
    public double calculateFee(Room room, int hours) {
        return 0.0;
    }
}
