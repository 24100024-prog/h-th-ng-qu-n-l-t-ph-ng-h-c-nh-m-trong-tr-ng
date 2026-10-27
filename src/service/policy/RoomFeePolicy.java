package service.policy;

import model.Room;

public interface RoomFeePolicy {
    double calculateFee(int hours);
}
