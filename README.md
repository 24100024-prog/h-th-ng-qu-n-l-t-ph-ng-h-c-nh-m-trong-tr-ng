# HỆ THỐNG QUẢN LÝ ĐẶT PHÒNG HỌC NHÓM TRONG TRƯỜNG

Ứng dụng Java Console (Java 8+) quản lý sinh viên, phòng, khung giờ và đặt phòng.

## Biên dịch, chạy, kiểm thử (từ thư mục gốc dự án)

```bash
javac -encoding UTF-8 -d out $(find src test -name '*.java')
java -Dfile.encoding=UTF-8 -cp out main.Main        # chạy ứng dụng
java -Dfile.encoding=UTF-8 -cp out test.TestRunner  # chạy 8 kịch bản kiểm thử
```

Dữ liệu lưu trong thư mục `data/` (text: `*.txt`, binary: `*.bin`); lúc khởi động chương trình tự tải bản text nếu có.

## Kiến trúc
- `model`: User/Student, Room (Normal/Projector/Seminar), TimeSlot, Booking, BookingDetail.
- `service/policy`: Strategy Pattern tính phí (`RoomFeePolicy`: Free, Hourly, RoomType).
- `service/validator`: `BookingValidator` (sinh viên tồn tại, sức chứa, trùng lịch, giới hạn giờ/ngày).
- `service`: BookingService, RoomService, StudentService, DataService (lưu/tải + kiểm tra nhất quán).
- `repository`: lưu `Map`/`List` trong bộ nhớ, đọc/ghi text và binary.
- `utils`: DateTimeUtils, InputValidator, TextCodec, exceptions.

## Quy ước và thông số đề bài CHƯA xác định
Đề bài không quy định các mục sau; đây là giá trị mặc định của nhóm, có thể đổi:
- Giờ hoạt động khi tìm giờ trống: 07:00–21:00 (`RoomService.OPEN_TIME/CLOSE_TIME`).
- Giới hạn 4 giờ đặt/ngày/sinh viên (`MaxHoursValidator`).
- Đơn giá của chính sách Hourly / RoomType do người dùng nhập; không có bảng giá chính thức.
- Phí tính theo số giờ làm tròn lên. Chính sách phí mặc định là miễn phí.
- Phòng "bảo trì" được mô hình hóa bằng trạng thái phòng (không có khoảng thời gian bảo trì riêng).
- Hủy booking đổi trạng thái sang `DA_HUY` (giữ lịch sử), chỉ người đặt mới hủy được và chỉ trước giờ bắt đầu.
