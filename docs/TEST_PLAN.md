# KẾ HOẠCH KIỂM THỬ (TEST PLAN)
> *Bối cảnh dự án, công nghệ và dữ liệu mẫu: [SPECIFICATIONS.md](SPECIFICATIONS.md). Kịch bản chi tiết: [TEST_CASES.md](TEST_CASES.md).*

* **Dự án:** Saltair - website bán quần áo basic (Java Servlet/JSP, MySQL)
* **Môn học:** SOF309 - Kiểm thử nâng cao cùng AI · **Lớp:** SD21308
* **Người phụ trách:** Lê Hoàng Minh (TY00254)
* **Phiên bản:** 1.0 (tuần 1 lập kế hoạch, tuần 2 hoàn thành phần Unit test Controller)

---

## 1. MỤC TIÊU KIỂM THỬ
* Phát hiện sớm lỗi ở lớp **Controller** (Servlet, Filter) và **Service**: kiểm tra dữ liệu đầu vào, chuyển hướng, phân quyền, báo lỗi tiếng Việt.
* Áp dụng đúng kỹ thuật thiết kế test: **phân vùng tương đương** và **phân tích giá trị biên**.
* Xây dựng bộ test **tự động** chạy bằng `mvn test` (JUnit 5), rồi bằng Selenium trên giao diện (tuần 3 đến 4), và chạy tự động trên GitHub Actions (tuần 5).

## 2. PHẠM VI KIỂM THỬ
| Phạm vi | Nội dung | Cấp độ |
| :--- | :--- | :--- |
| **Trong phạm vi (ưu tiên)** | Đăng nhập, đăng ký, chuyển hướng sau đăng nhập (chống open-redirect) | Unit (Controller + Service) |
| | Phân quyền: `AuthFilter` (cần đăng nhập), `AdminFilter` (chỉ ADMIN) | Unit (Filter) |
| | Hàm đọc tham số của Servlet Admin (`AdminOpsParams`) | Unit |
| | Luồng giao diện: đăng nhập, tìm sản phẩm, giỏ hàng, voucher, đặt hàng, trang Admin | UI (Selenium, tuần 3 đến 4) |
| **Đã có sẵn từ nhóm** | Chữ ký payOS, mã QR, cấu hình thanh toán, `PaymentService` | Unit (5 class trong `util/`, `service/order/`) |
| **Ngoài phạm vi** | Hiệu năng, bảo mật chuyên sâu, thanh toán payOS thật, gửi mail thật, kiểm thử DAO với MySQL thật | - |

## 3. CHIẾN LƯỢC & KỸ THUẬT
### 3.1. Cấp độ kiểm thử
| Cấp độ | Công cụ | Thư mục | Lệnh chạy | Tuần |
| :--- | :--- | :--- | :--- | :---: |
| Unit test Controller/Filter | JUnit 5 + Mockito | `src/test/java/com/fashionweb/controller/`, `filter/` | `mvn test -Dtest=*ControllerTest` | 2 |
| Unit test Service | JUnit 5 | `src/test/java/com/fashionweb/service/` | `mvn test` | 2 |
| UI test | Selenium 4 + Page Object Model (TestNG từ tuần 4) | `src/test/java/com/fashionweb/ui/{pages,tests}/` | `mvn test` (hoặc `testng.xml`) | 3 đến 4 |
| CI | GitHub Actions | `.github/workflows/test.yml` | tự chạy khi push/PR | 5 |

### 3.2. Cách kiểm thử Controller mà không cần Tomcat và MySQL
* `HttpServletRequest`, `HttpServletResponse`, `HttpSession`, `RequestDispatcher` được **giả lập bằng Mockito**; gọi trực tiếp `doGet` / `doPost` của Servlet rồi `verify` kết quả (`sendRedirect`, `forward`, `setAttribute("errors", ...)`).
* **Ca dữ liệu sai:** dùng `AuthService` thật. Service kiểm tra dữ liệu và ném `ValidationException` trước khi gọi DAO nên không chạm database.
* **Ca thành công:** dùng `Mockito.mockConstruction(AuthService.class)` để thay Service bằng bản giả trả về `User` mẫu; `mockStatic(LoginSupport.class, CALLS_REAL_METHODS)` chỉ thay hàm `login` (cần session và giỏ hàng trong DB), vẫn chạy thật `safeRedirect`.
* Nhờ vậy `mvn test` chạy được trên GitHub Actions (không có MySQL).

### 3.3. Kỹ thuật thiết kế test case
* **Phân vùng tương đương:** nhóm dữ liệu hợp lệ / không hợp lệ (email đúng, email trống, email sai định dạng; role CUSTOMER / ADMIN / chưa đăng nhập).
* **Giá trị biên:** mật khẩu 7 / 8 / 64 / 65 ký tự; họ tên 100 / 101 ký tự; email 150 / 151 ký tự; `page` = 0, 1, -5; số nguyên 2147483647 / 2147483648.
* **Bảng quyết định:** role × `redirect` → trang đến sau đăng nhập.
* **Kiểm thử ngoại lệ:** `assertThrows(ValidationException.class, ...)` kèm kiểm tra ô lỗi và nội dung thông báo.
* **Timeout:** `@Timeout` để chắc chắn hàm không treo với dữ liệu rất dài hoặc lặp nhiều lần.
* **Tham số hóa:** `@ParameterizedTest` + `@CsvSource` thay cho nhiều hàm test lặp.

## 4. MÔI TRƯỜNG & DỮ LIỆU
* Java 17, Maven 3, Tomcat 11 (chỉ cần cho UI test), MySQL 8 (chỉ cần cho UI test và chạy web).
* Unit test **không cần** `db.properties`, MySQL hay Tomcat.
* Tài khoản và voucher mẫu: [SPECIFICATIONS.md](SPECIFICATIONS.md) mục 6.
* Trình duyệt UI test: Chrome (headless trên CI).

## 5. QUY ƯỚC ĐẶT TÊN
* Lớp test Controller/Filter kết thúc bằng `ControllerTest`; lớp test Service kết thúc bằng `Test`.
* `@DisplayName` bắt đầu bằng mã test case trong [TEST_CASES.md](TEST_CASES.md) (ví dụ `TC_AUTH_04: ...`) để tra ngược.
* Mã test case: `TC_<MẢNG>_<số>`. Mảng: `AUTH` (đăng nhập), `AUTH_REDIR`, `REG` (đăng ký), `FILTER`, `ADM_PARAM`, `UI`.

## 6. TIÊU CHÍ HOÀN THÀNH
* `mvn test`: **100% Pass**, không Failure/Error (hiện tại: 140 test).
* Mỗi mảng trong phạm vi có test cho trường hợp hợp lệ, không hợp lệ, biên và ngoại lệ.
* Mọi test case tự động trong TEST_CASES.md có trạng thái Pass.
* Tuần 5: GitHub Actions báo xanh.

## 7. RỦI RO & GIẢI PHÁP
| Rủi ro | Giải pháp |
| :--- | :--- |
| DAO cần MySQL nên test Service/Controller phụ thuộc DB | Giả lập Service bằng Mockito; chỉ test nhánh kiểm tra dữ liệu của Service thật |
| Mockito/Byte Buddy chưa hỗ trợ JDK mới trên máy cá nhân | Surefire đặt `-Dnet.bytebuddy.experimental=true` trong `pom.xml` |
| Hash mật khẩu PBKDF2 làm class `AuthService` nạp chậm (khoảng 0,3 giây) | Đặt `@Timeout` 5 giây thay vì 2 giây cho test dùng `AuthService` |
| Dữ liệu mẫu bị đổi sau khi test UI | Dùng tài khoản mẫu cố định; Flyway nạp lại khi tạo DB mới |

## 8. LỊCH THỰC HIỆN
| Tuần | Công việc | Trạng thái |
| :---: | :--- | :---: |
| 1 | Đặc tả, Test Plan, Test Case | Xong |
| 2 | Unit test Controller/Filter/Service (JUnit 5) | Xong |
| 3 | Selenium POM: `LoginPage`, `RegisterPage`, `ProductPage` | Chưa làm |
| 4 | TestNG: groups, DataProvider, chụp ảnh lỗi | Chưa làm |
| 5 | GitHub Actions, Chrome headless, AI debug | Chưa làm |
| 6 | AI code review, hoàn thiện `docs/ai-logs/`, slide | Chưa làm |
