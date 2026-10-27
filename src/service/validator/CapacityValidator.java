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
                .orElseThrow(() -> new BookingException("Không tìm thấy phòng " + request.getRoomId()));

        if (request.getNumberOfPeople() <= 0) {
            throw new BookingException("Số người phải lớn hơn 0");
        }
        if (request.getNumberOfPeople() > room.getCapacity()) {
            throw new CapacityExceededException(room.getRoomName() + " chỉ chứa tối đa "
                    + room.getCapacity() + " người, nhưng đăng ký " + request.getNumberOfPeople());
        }
    }
}