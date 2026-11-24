package service.policy;

import model.Room;
import utils.DateTimeUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/** Phi = don gia theo loai phong (NORMAL/PROJECTOR/SEMINAR) x so gio. Loai khong khai bao don gia thi mien phi. */
public class RoomTypeFeePolicy implements RoomFeePolicy {
    private final Map<String, Double> ratePerHourByType = new LinkedHashMap<>();

    public RoomTypeFeePolicy(Map<String, Double> ratePerHourByType) {
        for (Map.Entry<String, Double> e : ratePerHourByType.entrySet()) {
            if (e.getValue() == null || e.getValue() < 0) {
                throw new IllegalArgumentException("Don gia khong hop le cho loai " + e.getKey());
            }
        }
        this.ratePerHourByType.putAll(ratePerHourByType);
    }

    @Override
    public double calculateFee(Room room, int hours) {
        Double rate = ratePerHourByType.get(room.getType());
        return rate == null ? 0.0 : rate * Math.max(0, hours);
    }

    @Override
    public String getName() {
        StringBuilder sb = new StringBuilder("Theo loai phong (");
        boolean first = true;
        for (Map.Entry<String, Double> e : ratePerHourByType.entrySet()) {
            if (!first) sb.append(", ");
            sb.append(e.getKey()).append(": ").append(DateTimeUtils.formatMoney(e.getValue())).append("/gio");
            first = false;
        }
        return sb.append(")").toString();
    }
}
