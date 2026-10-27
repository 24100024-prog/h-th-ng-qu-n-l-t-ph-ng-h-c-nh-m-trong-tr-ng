package repository;

import utils.TextCodec;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/** Ham dung chung cho cac repository khi doc/ghi file text va binary. */
final class FileSupport {
    static final String MAGIC = "QLDP1";

    interface IOWriter {
        void write(OutputStream out) throws IOException;
    }

    private FileSupport() {}

    /** Ghi vao file tam roi doi ten, de loi giua chung khong lam hong file cu. */
    static void atomicWrite(Path path, IOWriter writer) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
        try (OutputStream out = Files.newOutputStream(tmp)) {
            writer.write(out);
        } catch (IOException | RuntimeException e) {
            Files.deleteIfExists(tmp);
            throw e;
        }
        Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
    }

    static void writeTextLines(Path path, List<String> lines) throws IOException {
        atomicWrite(path, out -> {
            for (String line : lines) {
                out.write((line + "\n").getBytes(StandardCharsets.UTF_8));
            }
        });
    }

    static List<String[]> readTextRecords(Path path, int fieldCount) throws IOException {
        if (!Files.exists(path)) throw new FileNotFoundException("Khong tim thay file: " + path);
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<String[]> records = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) continue;
            String[] fields = TextCodec.split(line);
            if (fields.length != fieldCount) {
                throw new IOException("File " + path.getFileName() + " dong " + (i + 1)
                        + " khong hop le: can " + fieldCount + " truong nhung co " + fields.length);
            }
            records.add(fields);
        }
        return records;
    }

    static DataOutputStream binaryHeader(OutputStream out, String kind, int count) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeUTF(MAGIC);
        dos.writeUTF(kind);
        dos.writeInt(count);
        return dos;
    }

    /** Doc va kiem tra phan dau file binary; tra ve so ban ghi. */
    static int readBinaryHeader(DataInputStream in, Path path, String kind) throws IOException {
        if (!MAGIC.equals(in.readUTF()) || !kind.equals(in.readUTF())) {
            throw new IOException("File " + path.getFileName() + " khong phai file du lieu " + kind + " hop le");
        }
        int count = in.readInt();
        if (count < 0) throw new IOException("File " + path.getFileName() + " bi hong (so ban ghi am)");
        return count;
    }

    static DataInputStream openBinary(Path path) throws IOException {
        if (!Files.exists(path)) throw new FileNotFoundException("Khong tim thay file: " + path);
        InputStream in = Files.newInputStream(path);
        return new DataInputStream(new java.io.BufferedInputStream(in));
    }

    static IOException invalid(Path path, int record, RuntimeException cause) {
        return new IOException("File " + path.getFileName() + " ban ghi " + record
                + " khong hop le: " + cause.getMessage());
    }
}
