package service.policy;

import model.Room;

public class FreeFeePolicy implements RoomFeePolicy {
    @Override
    public double calculateFee(int hours) {
        return 0.0;
    }
}
}
