package service.validator;

import model.Room;
import repository.RoomRepository;
import utils.exceptions.BookingException;
import utils.exceptions.CapacityExceededException;

public class CapacityValidator implements BookingValidator {
    private final RoomRepository roomRepository;

    public CapacityValidator(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public void validate(BookingRequest request) throws BookingException {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new BookingException("Khong tim thay phong " + request.getRoomId()));

        if (request.getNumberOfPeople() <= 0) {
            throw new BookingException("So nguoi phai lon hon 0");
        }
        if (request.getNumberOfPeople() > room.getCapacity()) {
            throw new CapacityExceededException(room.getRoomName() + " chi chua toi da "
                    + room.getCapacity() + " nguoi, nhung dang ky " + request.getNumberOfPeople());
        }
    }
}