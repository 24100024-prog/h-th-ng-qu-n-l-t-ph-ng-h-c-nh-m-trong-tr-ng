package service.policy;

import model.Room;
import utils.DateTimeUtils;

/** Phi = don gia moi gio x so gio, ap dung nhu nhau cho moi loai phong. */
public class HourlyFeePolicy implements RoomFeePolicy {
    private final double ratePerHour;

    public HourlyFeePolicy(double ratePerHour) {
        if (ratePerHour < 0) throw new IllegalArgumentException("Don gia khong duoc am");
        this.ratePerHour = ratePerHour;
    }

    @Override
    public double calculateFee(Room room, int hours) {
        return ratePerHour * Math.max(0, hours);
    }

    @Override
    public String getName() {
        return "Theo gio (" + DateTimeUtils.formatMoney(ratePerHour) + "/gio)";
    }
}
