# ĐẶC TẢ BỐI CẢNH DỰ ÁN (SPECIFICATIONS)
> *File RIÊNG của dự án. Dùng làm ngữ cảnh (Context) gửi cho AI ở bước Phân tích và Test (Test Plan, Test Case, JUnit, Selenium), và là nơi duy nhất [`HUONG_DAN_QUY_TRINH_PHAT_TRIEN.md`](HUONG_DAN_QUY_TRINH_PHAT_TRIEN.md) tham chiếu khi cần thông tin cụ thể.*

---

## 1. THÔNG TIN DỰ ÁN
* **Môn học:** SOF309 - Kiểm thử nâng cao cùng AI · **Lớp:** SD21308
* **Sinh viên:** Lê Hoàng Minh · **MSSV:** TY00254
* **Repo:** `HoangMinh219/SOF309_-SD21308-_-TY00254-_-LeHoangMinh-`
* **Web được kiểm thử:** Saltair - website bán quần áo basic unisex (đồ án JAV102, kế thừa từ repo nhóm `nhan0x/saltair-fashion-web`).
* **Nghiệp vụ chi tiết** (role, chức năng, trạng thái đơn, voucher, phí ship): [`project.md`](../project.md)

## 2. CÔNG NGHỆ & CÁCH CHẠY
* **Ngôn ngữ / nền tảng:** Java 17, Jakarta EE (`jakarta.*`): Servlet 6.1, JSP 4, JSTL 3 + EL. Không dùng Spring.
* **Máy chủ:** Apache Tomcat 11. Đóng gói Maven WAR (`target/saltair.war`).
* **Cơ sở dữ liệu:** MySQL 8, truy cập bằng JDBC thuần (`PreparedStatement`, `mysql-connector-j 9.1.0`).
* **Migration:** Flyway 10 (`flyway-core`, `flyway-mysql`). `AppStartupListener` gọi `DBMigration.migrate()` khi Tomcat khởi động, tự chạy các file `src/main/resources/db/migration/V<số>__<tên>.sql`:
  * `V1__tao_bang.sql` (tạo bảng), `V2__du_lieu_mau.sql` (dữ liệu mẫu), `V3__chuyen_khoan.sql` (bảng `payment_transactions`, phương thức `BANK_TRANSFER`), `V4__payos.sql` (cột `payment_attempts`).
* **Thư viện khác:**
  * `angus-mail` 2.0.3: gửi mail SMTP Gmail (`util/MailUtil`) báo có lượt đăng nhập Admin.
  * `jackson-databind` 2.15: đọc/ghi JSON khi gọi API payOS và nhận webhook.
  * `zxing core` 3.5: sinh mã QR chuyển khoản dạng SVG (`util/QrSvg`).
  * Cloudinary (gọi qua `util/CloudinaryUtil`): lưu ảnh Admin tải lên; không cấu hình thì lưu vào `assets/images/`.
* **Giao diện:** HTML5, CSS3, JavaScript thuần (không framework, không Bootstrap), font Manrope, icon SVG sprite.
* **Kiểm thử:** JUnit 5.10 (Jupiter, `@ParameterizedTest`, `assertThrows`, `@Timeout`), Mockito 5.17 (giả lập request/response/Service để test Controller không cần Tomcat và MySQL), Selenium WebDriver 4.20 (Chrome). TestNG sẽ bổ sung ở tuần 4.
* **CI/CD:** GitHub Actions `.github/workflows/test.yml`: Ubuntu, JDK 17 Temurin, chạy `mvn clean test` khi push/PR vào `main`, upload `target/surefire-reports/` lên Artifacts.

**Cách chạy local**
* **URL:** `http://localhost:8080/saltair/home` (Admin: `http://localhost:8080/saltair/admin`)
* **Build:** `mvn clean package` → deploy `target/saltair.war` lên Tomcat 11.
* **Test:** `mvn test` (thư mục `src/test/java/com/fashionweb/`).
* **Database:** chỉ cần MySQL đang chạy. `db.url` có `createDatabaseIfNotExist=true` nên database `saltair` tự tạo, Flyway tự tạo bảng và nạp dữ liệu mẫu. (Script `database/schema.sql`, `data.sql` chỉ dùng để chạy tay khi cần.)
* **Cấu hình không commit** (copy từ file `.example` cùng thư mục `src/main/resources/`):
  * `db.properties`: `db.url`, `db.username`, `db.password` (bắt buộc).
  * `mail.properties`: SMTP Gmail; `auth.devCode=true` thì không gửi mail, chỉ ghi log.
  * `payment.properties`: `payos.clientId`, `payos.apiKey`, `payos.checksumKey`; để trống thì chỉ có COD.
  * `cloudinary.properties`: tùy chọn.

## 3. KIẾN TRÚC & QUY TẮC CODE
* **Mô hình:** MVC + DAO + Service Layer + Intercepting Filter. Một Servlet cho mỗi chức năng (không dùng Front Controller).
* **Luồng bắt buộc:** `Servlet -> Service -> DAO -> MySQL`.
  * Servlet (`controller`): đọc tham số, gọi Service, forward JSP hoặc redirect. Không viết SQL.
  * Service (`service`): nghiệp vụ, kiểm tra dữ liệu, ném `ValidationException` / `VoucherException` với câu tiếng Việt.
  * DAO (`dao`): chỉ SQL, luôn `PreparedStatement`; lỗi SQL bọc thành `DataAccessException`.
  * JSP (`WEB-INF/views`): chỉ hiển thị bằng JSTL/EL, đầu file ghi comment route và thuộc tính request.
* **Package** (`com.fashionweb`): chia theo tầng rồi theo chức năng `controller|service|dao/<auth|account|shop|cart|order|admin>/`; dùng chung: `model` (16 lớp), `dto`, `constant` (`Role`, `OrderStatus`, `PaymentMethod`, `PaymentStatus`, `DiscountType`, `AppConstant`), `exception`, `filter`, `listener`, `util`.
* **Filter** (thứ tự khai báo trong `web.xml`): `EncodingFilter` (UTF-8) → `StaticCacheFilter` (`/assets/*`) → `AuthFilter` (`/order`, `/orders`, `/profile`, `/addresses`, `/wishlist`) → `AdminFilter` (`/admin`, `/admin/*`).
* **Xác thực:** đăng nhập bằng email + mật khẩu (8 đến 64 ký tự), băm PBKDF2-SHA256 (`util/PasswordUtil`). Một form `/login` cho cả khách và Admin (`/admin/login` chuyển về `/login`). Thông tin đăng nhập lưu trong `HttpSession` (timeout 60 phút, cookie HttpOnly).
* **Thanh toán:** COD hoặc chuyển khoản qua payOS (`/order-pay`, webhook `/api/payment/payos` kiểm chữ ký bằng `PayOSSignature`).
* **Route chính:** `/home`, `/products`, `/product?slug=`, `/search/quick`, `/blog`, `/about`, `/login`, `/register`, `/logout`, `/cart`, `/checkout`, `/orders`, `/order?code=`, `/order-pay`, `/order-success`, `/profile`, `/addresses`, `/wishlist`, `/admin/*` (products, categories, attributes, trash, orders, vouchers, reviews, users, banners, blogs).
* **Đổi database:** thêm file mới `src/main/resources/db/migration/V<số tiếp theo>__<ten>.sql`. Không sửa file migration đã chạy (Flyway báo lỗi checksum).
* **Giao diện:** chỉ animate `transform`/`opacity`, hỗ trợ `prefers-reduced-motion`, điểm ngắt 990px và 750px.

## 4. QUY ƯỚC KIỂM THỬ & GIT
* **Thư mục test:**
  * Unit test (JUnit 5): `src/test/java/com/fashionweb/<tầng>/<chức năng>/` (đã có `util/PayOSSignatureTest`, `util/QrSvgTest`, `util/PaymentConfigTest`, `service/order/PayOSClientTest`, `service/order/PaymentServiceTest`).
  * Selenium (Page Object Model): `src/test/java/com/fashionweb/ui/pages/` và `ui/tests/`; ảnh lỗi lưu ở `screenshots/`.
  * Tên lớp test: `<LớpĐượcTest>Test`. Unit test không phụ thuộc MySQL thật (tách logic thuần hoặc giả lập dữ liệu) để chạy được trên GitHub Actions.
* **Minh chứng AI:** ghi prompt, kết quả AI và phần tự sửa vào `docs/ai-logs/`.
* **Nhánh:** `main` là nhánh chính (CI chạy trên `main`). Làm việc trên nhánh `feature/<ten-ngan>`, `test/<ten-ngan>`, `fix/<ten-ngan>`, `docs/<ten-ngan>` rồi merge vào `main`.
* **Commit:** `<loại>(<phạm vi>): <việc đã làm>`, viết tiếng Việt. Loại: `feat`, `fix`, `test`, `docs`, `ci`, `chore`. Phạm vi: `auth`, `account`, `shop`, `cart`, `order`, `admin`, `ui`, `db`, `y1`…`y6`.

## 5. NHÃN ISSUE
| Nhóm | Nhãn |
| :--- | :--- |
| Loại | `logic`, `giao-dien`, `feature`, `bug`, `test`, `chore`, `documentation` |
| Mảng | `auth`, `account`, `shop`, `cart`, `order`, `admin` |
| Yêu cầu SOF309 | `Y1` … `Y6` |

## 6. DỮ LIỆU MẪU ĐỂ TEST
*Nạp tự động bởi `V2__du_lieu_mau.sql`: 24 sản phẩm, 4 danh mục, 276 biến thể (6 màu × 4 size S/M/L/XL), tài khoản, voucher, đơn demo.*

* **Tài khoản** (đăng nhập đều ở `/login`):

| Email | Mật khẩu | Role | Ghi chú |
| :--- | :--- | :--- | :--- |
| `admin@saltair.vn` | `Admin@123` | ADMIN | Vào được `/admin` |
| `khach@saltair.vn` | `Khach@123` | CUSTOMER | Có giỏ hàng, yêu thích, đơn ở đủ trạng thái |
| `lan.tran@example.com`, `hoang.le@example.com`, `duc.vo@example.com` | `Khach@123` | CUSTOMER | Tài khoản thường |
| `mai.pham@example.com` | `Khach@123` | CUSTOMER | **Bị khóa** (thử đăng nhập tài khoản bị khóa) |

* **Voucher:** `WELCOME10` (giảm 10%, tối đa 50.000đ, đơn từ 200.000đ), `GIAM50K` (giảm 50.000đ, đơn áo khoác từ 500.000đ) dùng được; `HETHAN20` (hết hạn), `HETLUOT` (hết lượt), `TAMDUNG` (vô hiệu hóa).
* **Quy tắc tính tiền:** phí ship cố định 30.000đ, bằng 0 khi dùng voucher hợp lệ; `tổng = tiền hàng - giảm giá + phí ship`.
* **Trạng thái đơn:** `PENDING` (Chờ xác nhận) → `SHIPPING` (Đang giao) → `COMPLETED` (Hoàn thành), nhánh `CANCELLED`. Khách chỉ hủy khi `PENDING`; chỉ đơn `COMPLETED` mới được đánh giá.
* **Gợi ý:** bật `auth.devCode=true` trong `mail.properties` để không gửi mail thật khi test đăng nhập Admin.

## 7. CHECKLIST RIÊNG CỦA DỰ ÁN (DÙNG KHI REVIEW PR)
- [ ] Đúng luồng `Servlet -> Service -> DAO`; Servlet không viết SQL, JSP không gọi DAO/Service.
- [ ] SQL dùng `PreparedStatement`, không nối chuỗi từ dữ liệu người dùng.
- [ ] Không commit `db.properties`, `mail.properties`, `payment.properties`, `cloudinary.properties`, `target/`, mật khẩu, khóa API.
- [ ] Đổi bảng thì có file Flyway mới `V<số>__<tên>.sql`, không sửa file migration cũ.
- [ ] `mvn test` pass ở local và GitHub Actions báo xanh.
- [ ] Test mới có tên rõ ràng, có cả trường hợp hợp lệ, biên và ngoại lệ (`assertThrows`).
- [ ] Giao diện đúng ở 1440px và 375px, Console không có lỗi đỏ.
