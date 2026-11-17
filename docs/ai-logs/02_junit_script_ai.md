# Nhật ký AI: Viết Unit Test cho Controller bằng JUnit 5 (Y3 - Tuần 2)

## 1. Ngữ cảnh & Mục tiêu
- Viết Unit Test cho Controller/Filter của Saltair, yêu cầu: `@ParameterizedTest` + `@CsvSource` kiểm tra biên form, `assertThrows()` cho lỗi nghiệp vụ, `@Timeout`, và `mvn test -Dtest=*ControllerTest` phải Pass 100%.
- Khó khăn: Controller gọi Service rồi DAO, DAO cần MySQL; máy chấm và GitHub Actions không có MySQL.
- Công cụ AI: Claude Code.

## 2. Câu lệnh đã gửi cho AI (Prompt)
> "Thực hiện đúng yêu cầu trong file readme.md tuần 1 và tuần 2 giúp tôi."
>
> (AI tự đọc yêu cầu tuần 2 trong `docs/HUONG_DAN_CHI_TIET_6_TUAN.md`, đọc `LoginServlet`, `RegisterServlet`, `LoginSupport`, `AuthService`, `AuthFilter`, `AdminFilter`, `AdminOpsParams` rồi viết test.)

## 3. Kết quả do AI sinh ra (Raw Output)
Các file test được tạo (thư mục `src/test/java/com/fashionweb/`):

| File | Nội dung chính | Số lần chạy |
| :--- | :--- | :---: |
| `controller/auth/LoginControllerTest.java` | `doGet`/`doPost` của `LoginServlet`; `@CsvSource` cho email/mật khẩu sai và bảng role × `redirect` | 18 |
| `controller/auth/RegisterControllerTest.java` | Biên mật khẩu 7/65 ký tự, họ tên 101 ký tự, nhập lại không khớp, email trùng, redirect an toàn | 14 |
| `controller/auth/RedirectControllerTest.java` | `LoginSupport.safeRedirect` chống open-redirect, `@Timeout` 2 giây | 18 |
| `controller/admin/AdminOpsParamsControllerTest.java` | `page`, `positiveInt`, `enumValue`, `text`; 10.000 tham số bẩn | 30 |
| `filter/AccessFilterControllerTest.java` | `AuthFilter`, `AdminFilter`: redirect, 403, Referer | 10 |
| `service/auth/AuthServiceTest.java` | `assertThrows(ValidationException.class, ...)`, biên email 150/151 ký tự | 14 |

- `pom.xml`: thêm `mockito-core` 5.17.0 (test) và `argLine` `-Dnet.bytebuddy.experimental=true` cho surefire.
- Kết quả: `mvn test` → **140 test, 0 lỗi, BUILD SUCCESS** (gồm 36 test có sẵn của nhóm về payOS, QR, cấu hình thanh toán).

Ví dụ mã AI sinh (rút gọn):
```java
@ParameterizedTest
@CsvSource({
    "'',               Khach@123, email,    Vui lòng nhập email.",
    "khach@saltair,    Khach@123, email,    Email không đúng định dạng.",
    "khach@saltair.vn, '',        password, Vui lòng nhập mật khẩu."
})
void postInvalidInputShowsErrors(String email, String password, String field, String message) { ... }
```

## 4. Chỉnh sửa của Sinh viên & Lý do sửa
- **Lần chạy đầu 13 test lỗi:** `Could not modify all classes ... Java 27 (71) is not supported by Byte Buddy`. Nguyên nhân: JDK trên máy mới hơn bản Byte Buddy đi kèm Mockito 5.17. Cách sửa: thêm `-Dnet.bytebuddy.experimental=true` cho surefire. Không ảnh hưởng JDK 17 trên GitHub Actions.
- **Chạy offline thất bại** do thiếu `byte-buddy` 1.15.11 trong `~/.m2`; chạy online để Maven tải về.
- **Sửa mã TC trong `@DisplayName`** cho khớp bảng `TEST_CASES.md` (`TC_AUTH_11..17`, `TC_REG_02..08`).
- **Hạn chế đã chấp nhận:** test không chạm DAO/MySQL. Các nhánh cần DB (đăng nhập đúng mật khẩu, email trùng thật) được giả lập bằng `mockConstruction(AuthService.class)`; sẽ kiểm bằng Selenium ở tuần 3 đến 4.
- **Sửa thêm:** xóa dòng `assertTrue(true)` thừa AI để lại trong `LoginControllerTest.postSuccessRedirects`. Nếu máy chậm có thể tăng `@Timeout`.
