package ui;

import model.Booking;
import model.Room;
import model.TimeSlot;
import service.BookingService;
import service.DataService;
import service.RoomService;
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
    private final DataService dataService;

    private DefaultTableModel roomTableModel;
    private DefaultTableModel bookingTableModel;
    private JComboBox<String> cbRoomSelect;
    private JTextField txtStudentId;
    private JTextField txtPeople;
    private JTextField txtStart;
    private JTextField txtEnd;
    private JTextField txtCancelBookingId;
    private JTextField txtCancelStudentId;

    public MainGUI(RoomService roomService, BookingService bookingService, DataService dataService) {
        this.roomService = roomService;
        this.bookingService = bookingService;
        this.dataService = dataService;

        setTitle("Hệ Thống Quản Lý Đặt Phòng Học Nhóm");
        setSize(1020, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initUI();
        refreshAllData();
    }

    private void initUI() {
        // 1. HEADER + LOGO từ thư mục assets/logo.png
        JPanel headerPanel = new JPanel(new BorderLayout(15, 10));
        headerPanel.setBackground(new Color(21, 101, 192));
        headerPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel lblLogo = new JLabel();
        String[] candidatePaths = {
            "assets/logo.png",
            "./assets/logo.png",
            "D:/h th ng qu n l t ph ng h c nh m trong tr ng-main/assets/logo.png"
        };

        ImageIcon loadedIcon = null;
        for (String path : candidatePaths) {
            File f = new File(path);
            if (f.exists() && f.isFile()) {
                ImageIcon rawIcon = new ImageIcon(f.getAbsolutePath());
                if (rawIcon.getIconWidth() > 0) {
                    Image scaledImg = rawIcon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
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

        tabbedPane.addTab("Danh Sách Phòng", createRoomPanel());
        tabbedPane.addTab("Đặt Phòng & Quản Lý Booking", createBookingPanel());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createRoomPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"Mã Phòng", "Tên Phòng", "Loại Phòng", "Sức Chứa", "Trạng Thái", "Thông Tin Thêm"};
        roomTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(roomTableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton btnRefresh = new JButton("Làm Mới Danh Sách");
        btnRefresh.addActionListener(e -> loadRooms());

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(btnRefresh, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createBookingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Form đặt phòng
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

        // Bảng danh sách booking
        String[] bookingCols = {"Mã BK", "Mã SV", "Mã Phòng", "Khung Giờ", "Số Người", "Phí (VND)", "Trạng Thái"};
        bookingTableModel = new DefaultTableModel(bookingCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable bookingTable = new JTable(bookingTableModel);
        bookingTable.setRowHeight(26);

        // Thanh hủy booking ở dưới cùng
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

    private void handleCreateBooking() {
        try {
            String studentId = txtStudentId.getText().trim();
            String selected = (String) cbRoomSelect.getSelectedItem();
            if (selected == null || selected.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Chưa có phòng nào trong danh sách!", "Thông báo", JOptionPane.WARNING_MESSAGE);
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