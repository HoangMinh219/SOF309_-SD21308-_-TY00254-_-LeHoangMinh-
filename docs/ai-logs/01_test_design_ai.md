# Nhật ký AI: Thiết kế Test Plan và Test Case (Y2 - Tuần 1)

## 1. Ngữ cảnh & Mục tiêu
- Dự án: Saltair (Java Servlet/JSP, MySQL, Maven WAR). Cần có `docs/SPECIFICATIONS.md`, `docs/TEST_PLAN.md`, `docs/TEST_CASES.md`.
- Công cụ AI: Claude Code (chạy trong VS Code, đọc trực tiếp mã nguồn trong repo).
- Kỹ thuật yêu cầu: Phân vùng tương đương và Phân tích giá trị biên (RAG: AI đọc đặc tả và mã nguồn thật, không chỉ đọc mô tả).

## 2. Câu lệnh đã gửi cho AI (Prompt)
> **Prompt 1:** "điền lại specifications này theo đúng với tech stack dự án hiện tại giúp tôi"
>
> **Prompt 2:** "Thực hiện đúng yêu cầu trong file readme.md tuần 1 và tuần 2 giúp tôi."
>
> Với prompt 2, AI đọc `README.md` và `docs/HUONG_DAN_CHI_TIET_6_TUAN.md` để lấy yêu cầu. Phần Test Plan/Test Case tương ứng với prompt mẫu của giảng viên: *"Bạn là QA Lead. Dưới đây là đặc tả nghiệp vụ dự án Web của tôi: [SPECIFICATIONS.md]. Hãy áp dụng kỹ thuật Phân vùng tương đương và Phân tích giá trị biên để thiết kế Test Plan và Bộ Test Case chi tiết cho Controller và UI."*

## 3. Kết quả do AI sinh ra (Raw Output)
- Đọc `pom.xml`, `web.xml`, Flyway migration, `LoginServlet`, `RegisterServlet`, `AuthService`, `AuthFilter`, `AdminFilter`, `AdminOpsParams`, `V2__du_lieu_mau.sql` rồi viết lại `SPECIFICATIONS.md` (công nghệ, route, tài khoản mẫu, quy ước test).
- Sinh `TEST_PLAN.md`: mục tiêu, phạm vi, cấp độ test, kỹ thuật (phân vùng tương đương, giá trị biên, bảng quyết định), môi trường, rủi ro, lịch 6 tuần.
- Sinh `TEST_CASES.md`: 53 test case tự động (nhóm A đến F) và 8 test case giao diện cho tuần 3 đến 4 (nhóm G).
- Phát hiện kèm theo: `src/main/resources/payment.properties` chứa khóa payOS thật đã bị commit vào repo.

## 4. Chỉnh sửa của Sinh viên & Lý do sửa
- Những điểm AI làm sai hoặc thiếu (đã đối chiếu với code):
  - Bản `SPECIFICATIONS.md` cũ ghi "đăng nhập bằng mã 6 số qua email", trong khi `AuthService` hiện dùng email + mật khẩu PBKDF2. AI sửa theo code.
  - Bản Test Plan cũ của nhóm nhắc `develop`, `docs/requirements/`, phân công 3 người; AI viết lại thành kế hoạch cá nhân cho SOF309.
- Việc sinh viên cần tự kiểm tra: đọc lại các test case, bổ sung ca nghiệp vụ riêng (giỏ hàng, voucher, đặt hàng) khi làm Selenium; tạo lại khóa payOS đã lộ.
