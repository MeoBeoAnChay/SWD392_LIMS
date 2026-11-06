# Laptop Inventory Management System (LIMS)

Dự án Web Quản lý Kho Laptop (LIMS) xây dựng trên nền tảng **Java Jakarta EE**, theo mô hình **MVC 3-Tier**.

---

## Cấu trúc Khung 7 Packages (Base Code Structure)

```text
SWD392_LIMS/
├── pom.xml                                     # Cấu hình Maven & Dependencies (Jakarta EE, MSSQL, HikariCP, Gson, BCrypt)
├── .gitignore                                  # Cấu hình bỏ qua git (/target, IDE configs)
├── README.md                                   # Tài liệu kiến trúc dự án
└── src/
    └── main/
        ├── java/com/lims/
        │   ├── [1] controller/                 # TẦNG ĐIỀU PHỐI HTTP
        │   │   └── BaseServlet.java            # Lớp Servlet cơ sở (forward, redirect, sendJson, getCurrentUser)
        │   │
        │   ├── [2] service/                    # TẦNG LOGIC NGHIỆP VỤ & TRANSACTIONS
        │   │   └── package-info.java           # Sẵn sàng tạo các lớp Service khi làm chức năng
        │   │
        │   ├── [3] dao/                        # TẦNG TRUY CẬP DỮ LIỆU (PreparedStatement & SQL)
        │   │   └── package-info.java           # Sẵn sàng tạo các lớp DAO khi làm chức năng
        │   │
        │   ├── [4] model/                      # TẦNG THỰC THỂ DOMAIN
        │   │   └── package-info.java           # Sẵn sàng tạo các lớp POJO khi làm chức năng
        │   │
        │   ├── [5] filter/                     # BỘ LỌC HTTP & BẢO MẬT
        │   │   └── EncodingFilter.java         # Bộ lọc UTF-8 toàn bộ ứng dụng
        │   │
        │   └── [6] util/                       # TIỆN ÍCH DÙNG CHUNG
        │       ├── DBContext.java              # Kết nối SQL Server qua HikariCP Connection Pool
        │       └── Constants.java              # Hằng số chuẩn (Roles, Trạng thái, Session keys)
        │
        ├── resources/                          # CẤU HÌNH HỆ THỐNG
        │   ├── db.properties                   # Tham số kết nối CSDL SQL Server
        │   └── sql/
        │       ├── schema.sql                  # Script tạo cấu trúc 21 bảng CSDL
        │       └── seed.sql                    # Script nạp dữ liệu mẫu
        │
        └── webapp/                             # [7] TẦNG HIỂN THỊ (VIEW)
            ├── WEB-INF/
            │   ├── web.xml                     # Cấu hình ứng dụng Web
            │   └── views/                      # Thư mục chứa các trang JSP (view)
            │       └── home.jsp                # Trang chủ khởi động
            └── index.jsp                       # Điều hướng mặc định
```

---

## Nguyên tắc triển khai khi làm từng tính năng:
Mỗi khi phát triển một chức năng mới, bạn chỉ cần tạo các file tương ứng trong các package theo thứ tự:
1. **`model`**: Tạo Entity POJO đại diện cho bảng dữ liệu.
2. **`dao`**: Tạo DAO thực thi truy vấn SQL (CRUD) sử dụng kết nối từ `DBContext`.
3. **`service`**: Tạo Service xử lý logic, kiểm tra dữ liệu và bọc Transaction (nếu cập nhật nhiều bảng).
4. **`controller`**: Tạo Servlet kế thừa `BaseServlet`, nhận request, gọi Service và gán dữ liệu.
5. **`view`**: Tạo trang JSP tương ứng trong thư mục `webapp/WEB-INF/views/`.
