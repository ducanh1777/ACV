# Hướng dẫn chạy dự án ACV Asset Management

Dự án này được xây dựng bằng Java Spring Boot và MySQL. Dưới đây là các bước để khởi động hệ thống.

## 1. Chuẩn bị Cơ sở dữ liệu (MySQL Workbench)
- Mở **MySQL Workbench** và kết nối vào Server của bạn.
- Nhấn vào biểu tượng **Create a new schema** (hình chiếc thùng có dấu cộng) trên thanh công cụ.
- Nhập tên schema là: `acv_asset_management`.
- Nhấn **Apply** rồi **Apply** một lần nữa để xác nhận tạo database.
- (Hệ thống Spring Boot sẽ tự động tạo các bảng khi bạn chạy ứng dụng lần đầu).

## 2. Cấu hình kết nối
Nếu bạn sử dụng mật khẩu cho tài khoản `root` của MySQL, hãy mở file `src/main/resources/application.properties` và cập nhật dòng:
```properties
spring.datasource.password=Mật_khẩu_của_bạn
```

## 3. Chạy dự án
Bạn có thể chạy dự án theo 2 cách:

### Cách 1: Sử dụng Terminal (Dòng lệnh)
Mở terminal tại thư mục gốc của dự án (`d:\ACV_QuanLyTaiSan`) và chạy lệnh:
```bash
mvn spring-boot:run
```

### Cách 2: Sử dụng IDE (NetBeans/IntelliJ/Eclipse)
- Mở dự án trong IDE của bạn.
- Tìm file `src/main/java/com/acv/assetmanagement/AssetManagementApplication.java`.
- Chuột phải vào file và chọn **Run File** (hoặc nhấn nút Play).

## 4. Truy cập hệ thống
Sau khi ứng dụng khởi động thành công (thông báo *Started AssetManagementApplication* trong console):
- Truy cập địa chỉ: [http://localhost:8080/](http://localhost:8080/)
- **Tài khoản Admin mặc định:**
    - **Username:** `admin`
    - **Password:** `admin123`

## 5. Chức năng chính
- **Đăng nhập/Đăng xuất:** Sử dụng tài khoản admin trên.
- **Đăng ký nhân viên:** Sau khi đăng nhập admin, vào mục **Quản trị** -> **Đăng ký nhân viên** trên sidebar để tạo tài khoản cho nhân viên bằng Gmail họ cung cấp.
- **Quên mật khẩu:** Trang yêu cầu khôi phục mật khẩu.
