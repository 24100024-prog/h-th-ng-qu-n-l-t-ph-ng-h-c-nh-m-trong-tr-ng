package service.policy;

import model.Room;

public interface RoomFeePolicy {
    double calculateFee(Room room, int hours);

    String getName();
}
