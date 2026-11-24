package repository;

import model.Student;
import utils.TextCodec;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StudentRepository {
    private static final String KIND = "STUDENT";
    private Map<String, Student> students = new LinkedHashMap<>();

    public List<Student> findAll() {
        return new ArrayList<>(students.values());
    }

    public Optional<Student> findById(String id) {
        return Optional.ofNullable(students.get(id));
    }

    public boolean existsById(String id) {
        return students.containsKey(id);
    }

    public void add(Student student) {
        students.put(student.getId(), student);
    }

    public boolean remove(String id) {
        return students.remove(id) != null;
    }

    public void clear() {
        students.clear();
    }

    public void saveText(Path path) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Student s : students.values()) {
            lines.add(TextCodec.join(s.getId(), s.getName(), s.getPhone(), s.getLop(), s.getEmail()));
        }
        FileSupport.writeTextLines(path, lines);
    }

    /** Doc toan bo file truoc; chi khi hop le hoan toan moi thay du lieu hien tai. */
    public void loadText(Path path) throws IOException {
        List<String[]> records = FileSupport.readTextRecords(path, 5);
        Map<String, Student> loaded = new LinkedHashMap<>();
        int n = 0;
        for (String[] f : records) {
            n++;
            try {
                putUnique(loaded, new Student(f[0], f[1], f[2], f[3], f[4]));
            } catch (RuntimeException e) {
                throw FileSupport.invalid(path, n, e);
            }
        }
        students = loaded;
    }

    public void saveBinary(Path path) throws IOException {
        FileSupport.atomicWrite(path, out -> {
            DataOutputStream dos = FileSupport.binaryHeader(out, KIND, students.size());
            for (Student s : students.values()) {
                dos.writeUTF(s.getId());
                dos.writeUTF(s.getName());
                dos.writeUTF(s.getPhone());
                dos.writeUTF(s.getLop());
                dos.writeUTF(s.getEmail());
            }
            dos.flush();
        });
    }

    public void loadBinary(Path path) throws IOException {
        Map<String, Student> loaded = new LinkedHashMap<>();
        try (DataInputStream in = FileSupport.openBinary(path)) {
            int count = FileSupport.readBinaryHeader(in, path, KIND);
            for (int i = 1; i <= count; i++) {
                try {
                    putUnique(loaded, new Student(in.readUTF(), in.readUTF(), in.readUTF(), in.readUTF(), in.readUTF()));
                } catch (IllegalArgumentException e) {
                    throw FileSupport.invalid(path, i, e);
                }
            }
        } catch (EOFException e) {
            throw new IOException("File " + path.getFileName() + " bi cat cut hoac hong", e);
        }
        students = loaded;
    }

    private static void putUnique(Map<String, Student> map, Student s) {
        if (s.getId().trim().isEmpty()) throw new IllegalArgumentException("ma sinh vien trong");
        if (map.put(s.getId(), s) != null) throw new IllegalArgumentException("trung ma sinh vien " + s.getId());
    }
}
