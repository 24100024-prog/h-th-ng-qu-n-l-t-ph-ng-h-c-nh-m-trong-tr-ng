package service;

import model.Booking;
import model.Student;
import repository.BookingRepository;
import repository.StudentRepository;
import utils.InputValidator;

import java.util.List;

public class StudentService {
    private final StudentRepository studentRepository;
    private final BookingRepository bookingRepository;

    public StudentService(StudentRepository studentRepository, BookingRepository bookingRepository) {
        this.studentRepository = studentRepository;
        this.bookingRepository = bookingRepository;
    }

    public void addStudent(Student s) {
        validate(s);
        if (studentRepository.existsById(s.getId())) {
            throw new IllegalArgumentException("Ma sinh vien " + s.getId() + " da ton tai");
        }
        studentRepository.add(s);
    }

    public void updateStudent(String id, String name, String phone, String lop, String email) {
        Student current = getStudent(id);
        Student candidate = new Student(id, name, phone, lop, email);
        validate(candidate);
        current.setName(name);
        current.setPhone(phone);
        current.setLop(lop);
        current.setEmail(email);
    }

    public void deleteStudent(String id) {
        getStudent(id);
        for (Booking b : bookingRepository.findAll()) {
            if (b.isActive() && b.getStudentId().equals(id)) {
                throw new IllegalStateException("Sinh vien " + id + " con booking " + b.getBookingId()
                        + " dang hieu luc, hay huy truoc khi xoa");
            }
        }
        studentRepository.remove(id);
    }

    public Student getStudent(String id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay sinh vien " + id));
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    private void validate(Student s) {
        if (!InputValidator.isValidId(s.getId())) {
            throw new IllegalArgumentException("Ma sinh vien khong hop le (2-20 ky tu chu, so, '-' hoac '_')");
        }
        if (!InputValidator.isNotBlank(s.getName())) {
            throw new IllegalArgumentException("Ho ten khong duoc de trong");
        }
        if (!InputValidator.isValidPhone(s.getPhone())) {
            throw new IllegalArgumentException("So dien thoai khong hop le (bat dau bang 0, 10-11 chu so)");
        }
        if (!InputValidator.isNotBlank(s.getLop())) {
            throw new IllegalArgumentException("Lop khong duoc de trong");
        }
        if (!InputValidator.isValidEmail(s.getEmail())) {
            throw new IllegalArgumentException("Email khong hop le");
        }
    }
}
