package service.validator;

import repository.StudentRepository;
import utils.exceptions.BookingException;

public class StudentExistsValidator implements BookingValidator {
    private final StudentRepository studentRepository;

    public StudentExistsValidator(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public void validate(BookingRequest request) throws BookingException {
        if (!studentRepository.existsById(request.getStudentId())) {
            throw new BookingException("Khong tim thay sinh vien " + request.getStudentId());
        }
    }
}
