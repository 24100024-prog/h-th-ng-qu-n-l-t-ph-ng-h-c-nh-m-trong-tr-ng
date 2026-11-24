package ui;

import model.Student;
import service.StudentService;
import utils.InputValidator;

import java.util.List;
import java.util.function.Function;

public class StudentUI {
    private final ConsoleIO io;
    private final StudentService studentService;

    public StudentUI(ConsoleIO io, StudentService studentService) {
        this.io = io;
        this.studentService = studentService;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            System.out.println("\n[Sinh vien] 1.Them  2.Danh sach  3.Cap nhat  4.Xoa  0.Quay lai");
            switch (io.readLine("Chon: ").trim()) {
                case "1": addStudent(); break;
                case "2": listStudents(); break;
                case "3": updateStudent(); break;
                case "4": deleteStudent(); break;
                case "0": back = true; break;
                default: System.out.println("Lua chon khong hop le.");
            }
        }
    }

    private static final Function<String, String> CHECK_NAME =
            s -> InputValidator.isNotBlank(s) ? null : "Ho ten khong duoc de trong";
    private static final Function<String, String> CHECK_PHONE =
            s -> InputValidator.isValidPhone(s) ? null : "So dien thoai khong hop le (bat dau bang 0, 10-11 chu so)";
    private static final Function<String, String> CHECK_LOP =
            s -> InputValidator.isNotBlank(s) ? null : "Lop khong duoc de trong";
    private static final Function<String, String> CHECK_EMAIL =
            s -> InputValidator.isValidEmail(s) ? null : "Email khong hop le (vi du: ten@truong.edu.vn)";

    private boolean exists(String id) {
        for (Student s : studentService.getAllStudents()) {
            if (s.getId().equals(id)) return true;
        }
        return false;
    }
    private void addStudent() {
        System.out.println("--- Them sinh vien (go '" + ConsoleIO.CANCEL_WORD + "' bat ky luc nao de quay lai) ---");
        String id = io.ask("Ma SV: ", s -> {
            if (!InputValidator.isValidId(s)) return "Ma SV khong hop le (2-20 ky tu chu, so, '-' hoac '_')";
            if (exists(s)) return "Ma SV " + s + " da ton tai";
            return null;
        });
        if (id == null) return;
        String name = io.ask("Ho ten: ", CHECK_NAME);
        if (name == null) return;
        String phone = io.ask("SDT: ", CHECK_PHONE);
        if (phone == null) return;
        String lop = io.ask("Lop: ", CHECK_LOP);
        if (lop == null) return;
        String email = io.ask("Email: ", CHECK_EMAIL);
        if (email == null) return;

        io.guarded(() -> {
            studentService.addStudent(new Student(id, name, phone, lop, email));
            System.out.println("Da them sinh vien " + id);
        });
    }

    private void listStudents() {
        List<Student> list = studentService.getAllStudents();
        if (list.isEmpty()) {
            System.out.println("Chua co sinh vien nao.");
            return;
        }
        System.out.println(String.format("%-12s %-25s %-12s %-10s %s", "Ma SV", "Ho ten", "SDT", "Lop", "Email"));
        System.out.println("---------------------------------------------------------------------------");
        for (Student s : list) {
            System.out.println(String.format("%-12s %-25s %-12s %-10s %s",
                    s.getId(), s.getName(), s.getPhone(), s.getLop(), s.getEmail()));
        }
        System.out.println("Tong: " + list.size() + " sinh vien.");
    }

    private void updateStudent() {
        String id = io.ask("Ma SV can sua: ", s -> exists(s) ? null : "Khong tim thay sinh vien " + s);
        if (id == null) return;
        Student cur = studentService.getStudent(id);
        System.out.println("--- Cap nhat " + id + " ---");
        String name = io.askKeep("Ho ten", cur.getName(), CHECK_NAME);
        if (name == null) return;
        String phone = io.askKeep("SDT", cur.getPhone(), CHECK_PHONE);
        if (phone == null) return;
        String lop = io.askKeep("Lop", cur.getLop(), CHECK_LOP);
        if (lop == null) return;
        String email = io.askKeep("Email", cur.getEmail(), CHECK_EMAIL);
        if (email == null) return;

        io.guarded(() -> {
            studentService.updateStudent(id, name, phone, lop, email);
            System.out.println("Da cap nhat sinh vien " + id);
        });
    }

    private void deleteStudent() {
        String id = io.ask("Ma SV can xoa: ", s -> exists(s) ? null : "Khong tim thay sinh vien " + s);
        if (id == null) return;
        if (!io.askYesNo("Xac nhan xoa sinh vien " + id + "?")) {
            System.out.println("Da huy xoa.");
            return;
        }
        io.guarded(() -> {
            studentService.deleteStudent(id);
            System.out.println("Da xoa sinh vien " + id);
        });
    }
}