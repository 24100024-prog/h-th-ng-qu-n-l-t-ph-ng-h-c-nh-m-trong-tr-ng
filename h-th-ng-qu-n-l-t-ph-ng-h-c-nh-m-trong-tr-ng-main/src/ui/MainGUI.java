package ui;

import model.Booking;
import model.NormalRoom;
import model.ProjectorRoom;
import model.Room;
import model.SeminarRoom;
import model.Student;
import model.TimeSlot;
import service.BookingService;
import service.DataService;
import service.RoomService;
import service.StudentService;
import utils.DateTimeUtils;
import utils.InputValidator;
import utils.exceptions.BookingException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class MainGUI extends JFrame {
    private final RoomService roomService;
    private final BookingService bookingService;
    private final StudentService studentService;
    private final DataService dataService;

    // Bảng dữ liệu
    private DefaultTableModel roomTableModel;
    private DefaultTableModel bookingTableModel;
    private DefaultTableModel studentTableModel;

    // Tab Đặt phòng
    private JComboBox<String> cbRoomSelect;
    private JTextField txtStudentId;
    private JTextField txtPeople;
    private JTextField txtStart;
    private JTextField txtEnd;
    private JTextField txtCancelBookingId;
    private JTextField txtCancelStudentId;

    // Tab Quản lý Phòng
    private JTextField txtNewRoomId;
    private JTextField txtNewRoomName;
    private JComboBox<String> cbNewRoomType;
    private JTextField txtNewRoomCap;
    private JTextField txtNewRoomExtra;
    private JTextField txtManageRoomId;

    // Tab Quản lý Sinh viên
    private JTextField txtNewStuId;
    private JTextField txtNewStuName;
    private JTextField txtNewStuEmail;

    public MainGUI(RoomService roomService, BookingService bookingService,
                   StudentService studentService, DataService dataService) {
        this.roomService = roomService;
        this.bookingService = bookingService;
        this.studentService = studentService;
        this.dataService = dataService;

        setTitle("Hệ Thống Quản Lý Đặt Phòng Học Nhóm");
        setSize(1060, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initUI();
        refreshAllData();
    }

    private void initUI() {
        // 1. HEADER + LOGO
        JPanel headerPanel = new JPanel(new BorderLayout(15, 10));
        headerPanel.setBackground(new Color(21, 101, 192));
        headerPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel lblLogo = new JLabel();
        String[] candidatePaths = {
                "assets/logo.png",
                "./assets/logo.png",
                "h-th-ng-qu-n-l-t-ph-ng-h-c-nh-m-trong-tr-ng-main/assets/logo.png"
        };
        ImageIcon loadedIcon = null;
        for (String path : candidatePaths) {
            File f = new File(path);
            if (f.exists() && f.isFile()) {
                ImageIcon rawIcon = new ImageIcon(f.getAbsolutePath());
                if (rawIcon.getIconWidth() > 0) {
                    Image scaledImg = rawIcon.getImage().getScaledInstance(56, 56, Image.SCALE_SMOOTH);
                    loadedIcon = new ImageIcon(scaledImg);
                    break;
                }
            }
        }
        if (loadedIcon != null) {
            lblLogo.setIcon(loadedIcon);
        } else {
            lblLogo.setText("[LOGO]");
            lblLogo.setForeground(Color.WHITE);
            lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        }

        JLabel lblTitle = new JLabel("QUẢN LÝ ĐẶT PHÒNG HỌC NHÓM TRONG TRƯỜNG");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JButton btnSaveNow = new JButton("Lưu Dữ Liệu");
        btnSaveNow.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSaveNow.addActionListener(e -> saveDataToDisk(true));

        headerPanel.add(lblLogo, BorderLayout.WEST);
        headerPanel.add(lblTitle, BorderLayout.CENTER);
        headerPanel.add(btnSaveNow, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. CÁC TAB CHỨC NĂNG
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        tabbedPane.addTab("Quản Lý Phòng", createRoomPanel());
        tabbedPane.addTab("Đặt Phòng & Booking", createBookingPanel());
        tabbedPane.addTab("Quản Lý Sinh Viên", createStudentPanel());
        add(tabbedPane, BorderLayout.CENTER);
    }

    // ==================== TAB 1: QUẢN LÝ PHÒNG ====================
    private JPanel createRoomPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Form thêm phòng mới
        JPanel addForm = new JPanel(new GridLayout(3, 4, 10, 8));
        addForm.setBorder(BorderFactory.createTitledBorder("Thêm Phòng Học Mới"));

        txtNewRoomId = new JTextField();
        txtNewRoomName = new JTextField();
        cbNewRoomType = new JComboBox<>(new String[]{"NORMAL", "PROJECTOR", "SEMINAR"});
        txtNewRoomCap = new JTextField("10");
        txtNewRoomExtra = new JTextField();

        JButton btnAddRoom = new JButton("Thêm Phòng");
        btnAddRoom.setBackground(new Color(21, 101, 192));
        btnAddRoom.setForeground(Color.WHITE);
        btnAddRoom.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddRoom.addActionListener(e -> handleAddRoom());

        addForm.add(new JLabel("Mã Phòng (VD: P101):"));
        addForm.add(txtNewRoomId);
        addForm.add(new JLabel("Tên Phòng:"));
        addForm.add(txtNewRoomName);
        addForm.add(new JLabel("Loại Phòng:"));
        addForm.add(cbNewRoomType);
        addForm.add(new JLabel("Sức Chứa:"));
        addForm.add(txtNewRoomCap);
        addForm.add(new JLabel("Thiết bị thêm (Máy chiếu / Âm thanh):"));
        addForm.add(txtNewRoomExtra);
        addForm.add(new JLabel(""));
        addForm.add(btnAddRoom);

        // Bảng danh sách phòng
        String[] cols = {"Mã Phòng", "Tên Phòng", "Loại Phòng", "Sức Chứa", "Trạng Thái", "Thông Tin Thêm"};
        roomTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(roomTableModel);
        table.setRowHeight(26);

        // Thanh thao tác nhanh trên phòng (Bảo trì / Kích hoạt / Xóa)
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        actionBar.setBorder(BorderFactory.createTitledBorder("Cập Nhật Trạng Thái / Xóa Phòng"));
        txtManageRoomId = new JTextField(10);

        JButton btnMaint = new JButton("Đặt Bảo Trì");
        btnMaint.addActionListener(e -> handleToggleMaintenance(true));

        JButton btnActive = new JButton("Mở Hoạt Động");
        btnActive.addActionListener(e -> handleToggleMaintenance(false));

        JButton btnDeleteRoom = new JButton("Xóa Phòng");
        btnDeleteRoom.setBackground(new Color(198, 40, 40));
        btnDeleteRoom.setForeground(Color.WHITE);
        btnDeleteRoom.addActionListener(e -> handleDeleteRoom());

        actionBar.add(new JLabel("Nhập Mã Phòng:"));
        actionBar.add(txtManageRoomId);
        actionBar.add(btnMaint);
        actionBar.add(btnActive);
        actionBar.add(btnDeleteRoom);

        panel.add(addForm, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(actionBar, BorderLayout.SOUTH);
        return panel;
    }

    // ==================== TAB 2: ĐẶT PHÒNG & BOOKING ====================
    private JPanel createBookingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Tạo Phiếu Đặt Phòng (Giờ mở cửa: 07:00 - 21:00)"));

        txtStudentId = new JTextField();
        cbRoomSelect = new JComboBox<>();
        txtPeople = new JTextField("4");
        txtStart = new JTextField("25/11/2026 08:00");
        txtEnd = new JTextField("25/11/2026 10:00");

        JButton btnBook = new JButton("Xác Nhận Đặt Phòng");
        btnBook.setBackground(new Color(46, 125, 50));
        btnBook.setForeground(Color.WHITE);
        btnBook.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnBook.addActionListener(e -> handleCreateBooking());

        formPanel.add(new JLabel("Mã Sinh Viên:"));
        formPanel.add(txtStudentId);
        formPanel.add(new JLabel("Chọn Phòng:"));
        formPanel.add(cbRoomSelect);
        formPanel.add(new JLabel("Bắt Đầu (dd/MM/yyyy HH:mm):"));
        formPanel.add(txtStart);
        formPanel.add(new JLabel("Kết Thúc (dd/MM/yyyy HH:mm):"));
        formPanel.add(txtEnd);
        formPanel.add(new JLabel("Số Người Tham Gia:"));
        formPanel.add(txtPeople);
        formPanel.add(new JLabel(""));
        formPanel.add(btnBook);

        String[] bookingCols = {"Mã BK", "Mã SV", "Mã Phòng", "Khung Giờ", "Số Người", "Phí (VND)", "Trạng Thái"};
        bookingTableModel = new DefaultTableModel(bookingCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable bookingTable = new JTable(bookingTableModel);
        bookingTable.setRowHeight(26);

        JPanel cancelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        cancelPanel.setBorder(BorderFactory.createTitledBorder("Hủy Booking"));
        txtCancelBookingId = new JTextField(10);
        txtCancelStudentId = new JTextField(10);
        JButton btnCancel = new JButton("Hủy Lịch Đặt");
        btnCancel.setBackground(new Color(198, 40, 40));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.addActionListener(e -> handleCancelBooking());

        cancelPanel.add(new JLabel("Mã Booking (VD: BK001):"));
        cancelPanel.add(txtCancelBookingId);
        cancelPanel.add(new JLabel("Mã SV Người Đặt:"));
        cancelPanel.add(txtCancelStudentId);
        cancelPanel.add(btnCancel);

        JPanel centerBox = new JPanel(new BorderLayout(8, 8));
        centerBox.add(new JScrollPane(bookingTable), BorderLayout.CENTER);
        centerBox.add(cancelPanel, BorderLayout.SOUTH);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(centerBox, BorderLayout.CENTER);
        return panel;
    }

    // ==================== TAB 3: QUẢN LÝ SINH VIÊN ====================
    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel stuForm = new JPanel(new GridLayout(2, 4, 10, 8));
        stuForm.setBorder(BorderFactory.createTitledBorder("Đăng Ký Sinh Viên Mới"));

        txtNewStuId = new JTextField();
        txtNewStuName = new JTextField();
        txtNewStuEmail = new JTextField();

        JButton btnAddStu = new JButton("Thêm Sinh Viên");
        btnAddStu.setBackground(new Color(21, 101, 192));
        btnAddStu.setForeground(Color.WHITE);
        btnAddStu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddStu.addActionListener(e -> handleAddStudent());

        stuForm.add(new JLabel("Mã Sinh Viên (VD: 24100024):"));
        stuForm.add(txtNewStuId);
        stuForm.add(new JLabel("Họ và Tên:"));
        stuForm.add(txtNewStuName);
        stuForm.add(new JLabel("Email / Lớp:"));
        stuForm.add(txtNewStuEmail);
        stuForm.add(new JLabel(""));
        stuForm.add(btnAddStu);

        String[] stuCols = {"Mã Sinh Viên", "Họ và Tên", "Thông Tin Liên Hệ / Lớp"};
        studentTableModel = new DefaultTableModel(stuCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable stuTable = new JTable(studentTableModel);
        stuTable.setRowHeight(26);

        panel.add(stuForm, BorderLayout.NORTH);
        panel.add(new JScrollPane(stuTable), BorderLayout.CENTER);
        return panel;
    }

    // ==================== XỬ LÝ SỰ KIỆN ====================
    private void handleAddRoom() {
        try {
            String id = txtNewRoomId.getText().trim();
            String name = txtNewRoomName.getText().trim();
            String type = (String) cbNewRoomType.getSelectedItem();
            Integer cap = InputValidator.parseInt(txtNewRoomCap.getText().trim());
            String extra = txtNewRoomExtra.getText().trim();

            if (cap == null || cap <= 0) {
                JOptionPane.showMessageDialog(this, "Sức chứa phải là số nguyên dương!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Room room;
            if ("PROJECTOR".equals(type)) {
                room = new ProjectorRoom(id, name, cap, Room.ACTIVE, true);
            } else if ("SEMINAR".equals(type)) {
                int stageCap = InputValidator.parseInt(extra) != null ? InputValidator.parseInt(extra) : 5;
                room = new SeminarRoom(id, name, cap, Room.ACTIVE, stageCap);
            } else {
                room = new NormalRoom(id, name, cap, Room.ACTIVE);
            }

            roomService.addRoom(room);
            saveDataToDisk(false);
            refreshAllData();
            txtNewRoomId.setText("");
            txtNewRoomName.setText("");
            txtNewRoomExtra.setText("");
            JOptionPane.showMessageDialog(this, "Đã thêm phòng " + id + " thành công!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể thêm phòng", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleToggleMaintenance(boolean maintenance) {
        try {
            String roomId = txtManageRoomId.getText().trim();
            roomService.setMaintenance(roomId, maintenance);
            saveDataToDisk(false);
            refreshAllData();
            JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái phòng " + roomId + "!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi cập nhật", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteRoom() {
        try {
            String roomId = txtManageRoomId.getText().trim();
            roomService.deleteRoom(roomId);
            saveDataToDisk(false);
            refreshAllData();
            txtManageRoomId.setText("");
            JOptionPane.showMessageDialog(this, "Đã xóa phòng " + roomId + "!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể xóa phòng", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAddStudent() {
        try {
            String id = txtNewStuId.getText().trim();
            String name = txtNewStuName.getText().trim();
            String email = txtNewStuEmail.getText().trim();

            Student student = new Student(id, name, email, email.isEmpty() ? "CNTT" : email);
            studentService.addStudent(student);
            saveDataToDisk(false);
            refreshAllData();

            txtNewStuId.setText("");
            txtNewStuName.setText("");
            txtNewStuEmail.setText("");
            JOptionPane.showMessageDialog(this, "Đã thêm sinh viên " + name + " (" + id + ")!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể thêm sinh viên", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCreateBooking() {
        try {
            String studentId = txtStudentId.getText().trim();
            String selected = (String) cbRoomSelect.getSelectedItem();
            if (selected == null || selected.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Chưa có phòng hoạt động nào! Hãy sang tab 'Quản Lý Phòng' để thêm phòng trước.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String roomId = selected.split(" - ")[0].trim();

            Integer people = InputValidator.parseInt(txtPeople.getText().trim());
            if (people == null || people <= 0) {
                JOptionPane.showMessageDialog(this, "Số người phải là số nguyên dương!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalDateTime start = DateTimeUtils.parseDateTime(txtStart.getText().trim());
            LocalDateTime end = DateTimeUtils.parseDateTime(txtEnd.getText().trim());
            if (start == null || end == null) {
                JOptionPane.showMessageDialog(this, "Sai định dạng ngày giờ! Dùng dạng: dd/MM/yyyy HH:mm", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }

            TimeSlot slot = new TimeSlot(start, end);
            Booking created = bookingService.createBooking(studentId, roomId, slot, people);
            saveDataToDisk(false);
            refreshAllData();

            JOptionPane.showMessageDialog(
                    this,
                    "Đặt phòng thành công!\nMã booking: " + created.getBookingId() + "\nPhí: " + created.getFee(),
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (BookingException | IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể đặt phòng", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCancelBooking() {
        try {
            String bookingId = txtCancelBookingId.getText().trim();
            String studentId = txtCancelStudentId.getText().trim();
            bookingService.cancelBooking(bookingId, studentId);
            saveDataToDisk(false);
            refreshAllData();
            JOptionPane.showMessageDialog(this, "Đã hủy booking " + bookingId + " thành công!");
        } catch (BookingException | IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể hủy booking", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshAllData() {
        loadRooms();
        loadBookings();
        loadStudents();
    }

    private void loadRooms() {
        roomTableModel.setRowCount(0);
        cbRoomSelect.removeAllItems();

        List<Room> rooms = roomService.getAllRooms();
        for (Room r : rooms) {
            roomTableModel.addRow(new Object[]{
                    r.getRoomId(),
                    r.getRoomName(),
                    r.getType(),
                    r.getCapacity(),
                    r.getStatus(),
                    r.getExtraInfo()
            });
            if (!r.isUnderMaintenance()) {
                cbRoomSelect.addItem(r.getRoomId() + " - " + r.getRoomName());
            }
        }
    }

    private void loadBookings() {
        bookingTableModel.setRowCount(0);
        List<Booking> bookings = bookingService.getAllBookings();
        for (Booking b : bookings) {
            String slotStr = b.getTimeSlot() != null
                    ? b.getTimeSlot().getStartTime() + " -> " + b.getTimeSlot().getEndTime()
                    : "";
            bookingTableModel.addRow(new Object[]{
                    b.getBookingId(),
                    b.getStudentId(),
                    b.getRoomId(),
                    slotStr,
                    b.getNumberOfPeople(),
                    b.getFee(),
                    b.getStatus()
            });
        }
    }

    private void loadStudents() {
        studentTableModel.setRowCount(0);
        List<Student> students = studentService.getAllStudents();
        for (Student s : students) {
            studentTableModel.addRow(new Object[]{
                    s.getId(),
                    s.getName(),
                    s.getEmail()
            });
        }
    }

    private void saveDataToDisk(boolean showPopup) {
        try {
            Path dir = DataService.defaultDir();
            dataService.saveAll(dir, false);
            if (showPopup) {
                JOptionPane.showMessageDialog(this, "Đã lưu dữ liệu vào: " + dir.toAbsolutePath());
            }
        } catch (IOException ex) {
            if (showPopup) {
                JOptionPane.showMessageDialog(this, "Lỗi lưu dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}