package service.policy;

import model.Room;

/** Strategy interface: moi chinh sach tu dinh nghia cach tinh phi. */
public interface RoomFeePolicy {
    double calculateFee(Room room, int hours);
    String getName();
}

