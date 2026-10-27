package model;

import utils.DateTimeUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class TimeSlot {
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public TimeSlot(LocalDateTime startTime, LocalDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }

    /** Hop le khi co du hai moc va gio ket thuc sau gio bat dau. */
    public boolean isValid() {
        return startTime != null && endTime != null && endTime.isAfter(startTime);
    }

    /** Hai khoang gio trung nhau khi phan giao co do dai > 0 (cham dau mut khong tinh la trung). */
    public boolean overlaps(TimeSlot other) {
        return startTime.isBefore(other.endTime) && other.startTime.isBefore(endTime);
    }

    public long getDurationMinutes() {
        return Duration.between(startTime, endTime).toMinutes();
    }

    /** So gio tinh phi: lam tron len theo gio. */
    public int getBillableHours() {
        return (int) Math.ceil(getDurationMinutes() / 60.0);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TimeSlot)) return false;
        TimeSlot t = (TimeSlot) o;
        return Objects.equals(startTime, t.startTime) && Objects.equals(endTime, t.endTime);
    }

    @Override
    public int hashCode() { return Objects.hash(startTime, endTime); }

    @Override
    public String toString() {
        return DateTimeUtils.format(startTime) + " - " + DateTimeUtils.format(endTime);
    }
}
