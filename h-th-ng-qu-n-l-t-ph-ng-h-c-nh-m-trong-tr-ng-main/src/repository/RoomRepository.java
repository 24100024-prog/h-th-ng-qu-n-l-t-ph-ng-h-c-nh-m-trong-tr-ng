package repository;

import model.NormalRoom;
import model.ProjectorRoom;
import model.Room;
import model.SeminarRoom;
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

public class RoomRepository {
    private static final String KIND = "ROOM";
    private Map<String, Room> rooms = new LinkedHashMap<>();

    public List<Room> findAll() {
        return new ArrayList<>(rooms.values());
    }

    public Optional<Room> findById(String id) {
        return Optional.ofNullable(rooms.get(id));
    }

    public boolean existsById(String id) {
        return rooms.containsKey(id);
    }

    public void add(Room room) {
        rooms.put(room.getRoomId(), room);
    }

    public boolean remove(String id) {
        return rooms.remove(id) != null;
    }

    public void clear() {
        rooms.clear();
    }

    /** Tao dung loai phong tu ma loai va thuoc tinh rieng da luu. */
    public static Room createRoom(String type, String id, String name, int capacity, String status, String extra) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("ma phong trong");
        if (capacity <= 0) throw new IllegalArgumentException("suc chua phai > 0");
        switch (type) {
            case "NORMAL":
                return new NormalRoom(id, name, capacity, status);
            case "PROJECTOR":
                return new ProjectorRoom(id, name, capacity, status, Boolean.parseBoolean(extra));
            case "SEMINAR":
                int stage = Integer.parseInt(extra);
                if (stage < 0) throw new IllegalArgumentException("suc chua san khau am");
                return new SeminarRoom(id, name, capacity, status, stage);
            default:
                throw new IllegalArgumentException("loai phong khong hop le: " + type);
        }
    }

    public void saveText(Path path) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Room r : rooms.values()) {
            lines.add(TextCodec.join(r.getType(), r.getRoomId(), r.getRoomName(),
                    String.valueOf(r.getCapacity()), r.getStatus(), r.getExtraInfo()));
        }
        FileSupport.writeTextLines(path, lines);
    }

    public void loadText(Path path) throws IOException {
        List<String[]> records = FileSupport.readTextRecords(path, 6);
        Map<String, Room> loaded = new LinkedHashMap<>();
        int n = 0;
        for (String[] f : records) {
            n++;
            try {
                putUnique(loaded, createRoom(f[0], f[1], f[2], Integer.parseInt(f[3]), f[4], f[5]));
            } catch (RuntimeException e) {
                throw FileSupport.invalid(path, n, e);
            }
        }
        rooms = loaded;
    }

    public void saveBinary(Path path) throws IOException {
        FileSupport.atomicWrite(path, out -> {
            DataOutputStream dos = FileSupport.binaryHeader(out, KIND, rooms.size());
            for (Room r : rooms.values()) {
                dos.writeUTF(r.getType());
                dos.writeUTF(r.getRoomId());
                dos.writeUTF(r.getRoomName());
                dos.writeInt(r.getCapacity());
                dos.writeUTF(r.getStatus());
                dos.writeUTF(r.getExtraInfo());
            }
            dos.flush();
        });
    }

    public void loadBinary(Path path) throws IOException {
        Map<String, Room> loaded = new LinkedHashMap<>();
        try (DataInputStream in = FileSupport.openBinary(path)) {
            int count = FileSupport.readBinaryHeader(in, path, KIND);
            for (int i = 1; i <= count; i++) {
                try {
                    putUnique(loaded, createRoom(in.readUTF(), in.readUTF(), in.readUTF(), in.readInt(),
                            in.readUTF(), in.readUTF()));
                } catch (IllegalArgumentException e) {
                    throw FileSupport.invalid(path, i, e);
                }
            }
        } catch (EOFException e) {
            throw new IOException("File " + path.getFileName() + " bi cat cut hoac hong", e);
        }
        rooms = loaded;
    }

    private static void putUnique(Map<String, Room> map, Room r) {
        if (map.put(r.getRoomId(), r) != null) throw new IllegalArgumentException("trung ma phong " + r.getRoomId());
    }
}
