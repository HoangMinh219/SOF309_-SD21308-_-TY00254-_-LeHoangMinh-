# BỘ KỊCH BẢN KIỂM THỬ (TEST CASES)
> *Kế hoạch: [TEST_PLAN.md](TEST_PLAN.md). Dữ liệu mẫu (tài khoản, voucher): [SPECIFICATIONS.md](SPECIFICATIONS.md) mục 6.*
> *Cột **Tự động hóa** cho biết test JUnit tương ứng (đã chạy bằng `mvn test`). Các ca `TC_UI_*` sẽ viết bằng Selenium ở tuần 3 đến 4.*

**Tổng kết:** 53 test case tự động (tương ứng 104 lần chạy JUnit 5, toàn bộ Pass) · 8 test case giao diện dự kiến (Selenium) chưa thực hiện.
**Ký hiệu:** Pos = Positive (hợp lệ), Neg = Negative (không hợp lệ). Chuỗi rỗng ghi là `""`.

---

## A. ĐĂNG NHẬP - `LoginServlet` (`LoginControllerTest`)
| Test Case ID | Chức năng | Mô tả ca kiểm thử | Loại | Các bước thực hiện | Dữ liệu kiểm thử | Kết quả mong đợi | Tự động hóa | Trạng thái |
| :---: | :---: | :--- | :---: | :--- | :--- | :--- | :---: | :---: |
| **TC_AUTH_01** | Login | Mở form đăng nhập khi chưa đăng nhập | Pos | 1. GET `/login` | Không có session | Hiện `login.jsp`, không redirect | `getShowsLoginForm` | Pass |
| **TC_AUTH_02** | Login | Đường dẫn cũ `/admin/login` chuyển về `/login` | Pos | 1. GET `/admin/login?redirect=/admin/orders?x=1` | `redirect=/admin/orders?x=1` | Redirect `/login?redirect=%2Fadmin%2Forders%3Fx%3D1` | `getOldAdminLoginRedirects` | Pass |
| **TC_AUTH_03** | Login | Đã đăng nhập mà mở lại `/login` | Pos | 1. GET `/login` với session đã có user | CUSTOMER / ADMIN | CUSTOMER về `/home`, ADMIN về `/admin` | `getWhenLoggedInRedirects` (2 bộ) | Pass |
| **TC_AUTH_04** | Login | Email để trống | Neg | 1. POST `/login` | email `""`, pw `Khach@123` | Lỗi ô email: "Vui lòng nhập email." | `postInvalidInputShowsErrors` | Pass |
| **TC_AUTH_05** | Login | Email chỉ có khoảng trắng | Neg | 1. POST `/login` | email `"   "` | Lỗi ô email: "Vui lòng nhập email." | như trên | Pass |
| **TC_AUTH_06** | Login | Email thiếu `@` | Neg | 1. POST `/login` | `khach` | Lỗi ô email: "Email không đúng định dạng." | như trên | Pass |
| **TC_AUTH_07** | Login | Email thiếu phần sau `@` | Neg | 1. POST `/login` | `khach@` | Lỗi ô email: "Email không đúng định dạng." | như trên | Pass |
| **TC_AUTH_08** | Login | Email thiếu `.tên miền` | Neg | 1. POST `/login` | `khach@saltair` | Lỗi ô email: "Email không đúng định dạng." | như trên | Pass |
| **TC_AUTH_09** | Login | Mật khẩu để trống | Neg | 1. POST `/login` | `khach@saltair.vn` / `""` | Lỗi ô password: "Vui lòng nhập mật khẩu."; ở lại form, không redirect | như trên | Pass |
| **TC_AUTH_10** | Login | Tài khoản bị khóa / sai mật khẩu (Service ném lỗi) | Neg | 1. POST `/login` | `mai.pham@example.com` (bị khóa) | Hiện lại form kèm `errors`, không redirect | `postServiceRejectsCredentials` | Pass |
| **TC_AUTH_11** | Login | CUSTOMER đăng nhập, không có `redirect` | Pos | 1. POST `/login` hợp lệ | CUSTOMER, redirect trống | Redirect `/home` | `postSuccessRedirects` | Pass |
| **TC_AUTH_12** | Login | CUSTOMER quay lại trang đang xem | Pos | 1. POST `/login` | CUSTOMER, `redirect=/cart` | Redirect `/cart` | như trên | Pass |
| **TC_AUTH_13** | Login | CUSTOMER bị chặn chuyển vào trang admin | Neg | 1. POST `/login` | CUSTOMER, `redirect=/admin/orders` | Redirect `/home` | như trên | Pass |
| **TC_AUTH_14** | Login | Chặn open-redirect sang web khác | Neg | 1. POST `/login` | CUSTOMER, `redirect=//evil.com` | Redirect `/home` | như trên | Pass |
| **TC_AUTH_15** | Login | ADMIN đăng nhập, không có `redirect` | Pos | 1. POST `/login` | ADMIN, redirect trống | Redirect `/admin`; session 1800 giây; gửi mail báo đăng nhập | như trên | Pass |
| **TC_AUTH_16** | Login | ADMIN quay lại trang admin đang xem | Pos | 1. POST `/login` | ADMIN, `redirect=/admin/vouchers` | Redirect `/admin/vouchers` | như trên | Pass |
| **TC_AUTH_17** | Login | ADMIN không được chuyển ra ngoài khu admin | Neg | 1. POST `/login` | ADMIN, `redirect=/cart` | Redirect `/admin` | như trên | Pass |

## B. CHỐNG OPEN-REDIRECT - `LoginSupport.safeRedirect` (`RedirectControllerTest`)
| Test Case ID | Chức năng | Mô tả ca kiểm thử | Loại | Các bước thực hiện | Dữ liệu kiểm thử | Kết quả mong đợi | Tự động hóa | Trạng thái |
| :---: | :---: | :--- | :---: | :--- | :--- | :--- | :---: | :---: |
| **TC_AUTH_REDIR_01** | Redirect | Đường dẫn nội bộ hợp lệ được giữ nguyên (có trim) | Pos | Gọi `safeRedirect(x)` | `/cart`, `/orders?page=2`, `/admin/products`, `"  /wishlist  "` | Trả về đúng đường dẫn đã trim | `safeRedirect` (4 bộ) | Pass |
| **TC_AUTH_REDIR_02** | Redirect | Giá trị rỗng, URL ngoài, giao thức nguy hiểm, trang đăng nhập bị từ chối | Neg | Gọi `safeRedirect(x)` | `null`, `""`, `"   "`, `//evil.com`, `https://evil.com`, `javascript:alert(1)`, `cart`, `/login`, `/login?redirect=/cart`, `/logout`, `/register`, `/admin/login` | Trả về `null` | `safeRedirect` (12 bộ) | Pass |
| **TC_AUTH_REDIR_03** | Redirect | Chặn `\` và xuống dòng (header injection); chuỗi 100.000 ký tự xử lý dưới 2 giây | Neg | Gọi `safeRedirect(x)` | `/\evil.com`, `/cart\r\nSet-Cookie: a=b`, `/` + 100.000 chữ `a` | Hai ca đầu trả `null`; ca dài trả lại nguyên chuỗi, không vượt `@Timeout` 2 giây | `rejectsBackslashAndLineBreaks`, `handlesVeryLongInputQuickly` | Pass |

## C. ĐĂNG KÝ - `RegisterServlet` (`RegisterControllerTest`)
| Test Case ID | Chức năng | Mô tả ca kiểm thử | Loại | Các bước thực hiện | Dữ liệu kiểm thử | Kết quả mong đợi | Tự động hóa | Trạng thái |
| :---: | :---: | :--- | :---: | :--- | :--- | :--- | :---: | :---: |
| **TC_REG_01** | Register | Mở form đăng ký | Pos | GET `/register` | Chưa đăng nhập | Hiện `register.jsp` | `getShowsForm` | Pass |
| **TC_REG_02** | Register | Email để trống | Neg | POST `/register` | email `""`, tên `Minh`, mk `Pass@1234` | `errors` chỉ có ô `email` | `postBoundaryValues` | Pass |
| **TC_REG_03** | Register | Email thiếu `@` | Neg | POST `/register` | `abc` | `errors` chỉ có ô `email` | như trên | Pass |
| **TC_REG_04** | Register | Email thiếu `.tên miền` | Neg | POST `/register` | `a@b` | `errors` chỉ có ô `email` | như trên | Pass |
| **TC_REG_05** | Register | Họ tên để trống (kèm email sai) | Neg | POST `/register` | tên `""` | `errors` có `email`, `fullName` | như trên | Pass |
| **TC_REG_06** | Register | Mật khẩu 7 ký tự (biên dưới - 1) | Neg | POST `/register` | mk `1234567` | `errors` có `password` | như trên | Pass |
| **TC_REG_07** | Register | Nhập lại mật khẩu không khớp | Neg | POST `/register` | mk `Pass@1234`, nhập lại `Pass@9999` | `errors` có `confirm` | như trên | Pass |
| **TC_REG_08** | Register | Tất cả để trống | Neg | POST `/register` | tất cả `""` | `errors` có `email`, `fullName`, `password` | như trên | Pass |
| **TC_REG_09** | Register | Họ tên 101 ký tự, mật khẩu 65 ký tự (biên trên + 1) | Neg | POST `/register` | tên `N`×101, mk `p`×65 | "Họ tên tối đa 100 ký tự." và "Mật khẩu tối đa 64 ký tự." | `postTooLongValues` | Pass |
| **TC_REG_10** | Register | Email đã có tài khoản | Neg | POST `/register` | `khach@saltair.vn` | Hiện lại form, lỗi ô email "Email này đã có tài khoản." | `postDuplicateEmailShowsError` | Pass |
| **TC_REG_11** | Register | Đăng ký thành công, không có `redirect` | Pos | POST `/register` hợp lệ | redirect trống | Tự đăng nhập, flash `success`, redirect `/home` | `postSuccessRedirects` | Pass |
| **TC_REG_12** | Register | Đăng ký xong quay lại trang đang xem | Pos | POST `/register` hợp lệ | `redirect=/cart` | Redirect `/cart` | như trên | Pass |
| **TC_REG_13** | Register | Không cho chuyển vào `/admin` | Neg | POST `/register` hợp lệ | `redirect=/admin/users` | Redirect `/home` | như trên | Pass |
| **TC_REG_14** | Register | Không cho chuyển sang web ngoài | Neg | POST `/register` hợp lệ | `redirect=https://x.com` | Redirect `/home` | như trên | Pass |

## D. SERVICE ĐĂNG NHẬP - `AuthService` (`AuthServiceTest`)
| Test Case ID | Chức năng | Mô tả ca kiểm thử | Loại | Các bước thực hiện | Dữ liệu kiểm thử | Kết quả mong đợi | Tự động hóa | Trạng thái |
| :---: | :---: | :--- | :---: | :--- | :--- | :--- | :---: | :---: |
| **TC_SVC_AUTH_01** | AuthService | Chuẩn hóa email: trim + chữ thường | Pos | `normalizeEmail(x)` | `"  Khach@Saltair.VN  "`, `ADMIN@SALTAIR.VN`, `a@b.co` | `khach@saltair.vn`, `admin@saltair.vn`, `a@b.co` | `normalizeEmailOk` | Pass |
| **TC_SVC_AUTH_02** | AuthService | Email sai ném `ValidationException` đúng ô và đúng nội dung | Neg | `assertThrows(ValidationException.class, ...)` | `null`, `""`, `"   "`, `abc`, `a@b`, `@b.com`, `a b@c.com` | Ô `email`; "Vui lòng nhập email." hoặc "Email không đúng định dạng." | `normalizeEmailInvalid` (7 bộ) | Pass |
| **TC_SVC_AUTH_03** | AuthService | Biên độ dài email: 150 ký tự hợp lệ, 151 ký tự bị từ chối | Pos/Neg | `normalizeEmail(x)` | chuỗi 150 và 151 ký tự | 150 chấp nhận; 151 ném `ValidationException` | `normalizeEmailLengthBoundary` | Pass |
| **TC_SVC_AUTH_04** | AuthService | Đăng nhập thiếu mật khẩu | Neg | `login(email, "")` và `login(email, null)` | `khach@saltair.vn` | Ném `ValidationException`, ô `password`: "Vui lòng nhập mật khẩu." | `loginRequiresPassword` | Pass |
| **TC_SVC_AUTH_05** | AuthService | Đăng ký rỗng gom đủ lỗi từng ô, xong trong 5 giây | Neg | `register("", "", "", "")` | tất cả rỗng | Ném `ValidationException` có `email`, `fullName`, `password` | `registerCollectsAllErrors` (`@Timeout` 5 giây) | Pass |
| **TC_SVC_AUTH_06** | AuthService | Hằng số biên mật khẩu | Pos | Đọc hằng số | - | `PASSWORD_MIN = 8`, `PASSWORD_MAX = 64` | `passwordBounds` | Pass |

## E. PHÂN QUYỀN - Filter (`AccessFilterControllerTest`)
| Test Case ID | Chức năng | Mô tả ca kiểm thử | Loại | Các bước thực hiện | Dữ liệu kiểm thử | Kết quả mong đợi | Tự động hóa | Trạng thái |
| :---: | :---: | :--- | :---: | :--- | :--- | :--- | :---: | :---: |
| **TC_FILTER_01** | AuthFilter | Khách chưa đăng nhập vào trang cần đăng nhập | Neg | GET `/orders`, `/profile?tab=info`, `/wishlist` | Không có session | Redirect `/login?redirect=<trang đang mở, đã mã hóa>`; không gọi `chain` | `authFilterRedirectsGuest` (3 bộ) | Pass |
| **TC_FILTER_02** | AuthFilter | Đã đăng nhập | Pos | GET `/orders` | Session có user | Cho đi tiếp (`chain.doFilter`) | `authFilterPassesLoggedIn` | Pass |
| **TC_FILTER_03** | AuthFilter | POST (ví dụ thêm yêu thích) dùng `Referer` cùng host để quay lại đúng trang | Pos | POST `/wishlist` | `Referer: http://localhost:8080/saltair/product?slug=ao-polo` | Redirect `/login?redirect=%2Fproduct%3Fslug%3Dao-polo` | `authFilterUsesRefererForPost` | Pass |
| **TC_FILTER_04** | AuthFilter | Bỏ qua `Referer` của web khác | Neg | POST `/wishlist` | `Referer: http://evil.com/saltair/x` | Redirect `/login?redirect=%2Fwishlist` | `authFilterIgnoresForeignReferer` | Pass |
| **TC_FILTER_05** | AdminFilter | Chưa đăng nhập vào `/admin/*` | Neg | GET `/admin/products` | Không có session | Redirect `/login?redirect=%2Fadmin%2Fproducts` | `adminFilterRedirectsGuest` | Pass |
| **TC_FILTER_06** | AdminFilter | CUSTOMER vào `/admin` | Neg | GET `/admin` | Role CUSTOMER | Lỗi 403; không gọi `chain` | `adminFilterForbidsCustomer` | Pass |
| **TC_FILTER_07** | AdminFilter | ADMIN vào `/admin/orders` | Pos | GET `/admin/orders` | Role ADMIN | Cho đi tiếp | `adminFilterPassesAdmin` | Pass |
| **TC_FILTER_08** | AdminFilter | `/admin/login` luôn được cho qua | Pos | GET `/admin/login` | Không có session | Cho đi tiếp | `adminFilterAllowsLegacyLoginPath` | Pass |

## F. ĐỌC THAM SỐ ADMIN - `AdminOpsParams` (`AdminOpsParamsControllerTest`)
| Test Case ID | Chức năng | Mô tả ca kiểm thử | Loại | Các bước thực hiện | Dữ liệu kiểm thử | Kết quả mong đợi | Tự động hóa | Trạng thái |
| :---: | :---: | :--- | :---: | :--- | :--- | :--- | :---: | :---: |
| **TC_ADM_PARAM_01** | Admin | `page` luôn >= 1, sai thì về 1 | Pos/Neg | `page(req)` | `null`, `""`, `abc`, `-5`, `0`, `1`, `2`, `" 7 "`, `99999`, `1.5` | 1, 1, 1, 1, 1, 1, 2, 7, 99999, 1 | `page` (10 bộ) | Pass |
| **TC_ADM_PARAM_02** | Admin | `positiveInt` chỉ nhận số nguyên dương | Pos/Neg | `positiveInt(s)` | `null`, `""`, `abc`, `0`, `-1`, `1`, `" 42 "`, `2147483647`, `2147483648` | `null` với giá trị sai/<=0/tràn; còn lại đúng số | `positiveInt` (10 bộ) | Pass |
| **TC_ADM_PARAM_03** | Admin | `enumValue(OrderStatus)` không phân biệt hoa thường | Pos/Neg | `enumValue(OrderStatus.class, s)` | `pending`, `SHIPPING`, `" completed "`, `Cancelled`, `null`, `""`, `refunded`, `pend ing` | 4 giá trị đầu đúng enum; còn lại `null` | `enumValue` (8 bộ) | Pass |
| **TC_ADM_PARAM_04** | Admin | `text` trim, `null` thành rỗng, cắt tối đa 100 ký tự | Pos | `text(req, "q")` | `null`, `"  áo thun  "`, chuỗi 250 ký tự | `""`, `áo thun`, độ dài 100 | `text` | Pass |
| **TC_ADM_PARAM_05** | Admin | 10.000 tham số bẩn (có `'; DROP TABLE users;--`) không ném lỗi, không treo | Neg | Lặp `page`, `positiveInt` | chuỗi SQL injection hoặc `null` | `page` luôn 1, `positiveInt` luôn `null`, xong trong 2 giây | `neverThrowsAndNeverHangs` (`@Timeout` 2 giây) | Pass |

## G. GIAO DIỆN - Selenium (kế hoạch, làm ở tuần 3 đến 4)
| Test Case ID | Chức năng | Mô tả ca kiểm thử | Loại | Các bước thực hiện | Dữ liệu kiểm thử | Kết quả mong đợi | Tự động hóa | Trạng thái |
| :---: | :---: | :--- | :---: | :--- | :--- | :--- | :---: | :---: |
| **TC_UI_01** | Login | Khách đăng nhập thành công | Pos | 1. Mở `/login`<br>2. Nhập email, mật khẩu<br>3. Bấm Đăng nhập | `khach@saltair.vn` / `Khach@123` | Về `/home`, menu hiện tài khoản | `LoginTest` (dự kiến) | Chưa thực hiện |
| **TC_UI_02** | Login | Sai mật khẩu | Neg | Như trên | `khach@saltair.vn` / `saimatkhau` | Ở lại `/login`, báo "Email hoặc mật khẩu không đúng." | `LoginTest` (`@DataProvider`) | Chưa thực hiện |
| **TC_UI_03** | Login | Tài khoản bị khóa | Neg | Như trên | `mai.pham@example.com` / `Khach@123` | Báo "Tài khoản đã bị khóa..." | `LoginTest` (`@DataProvider`) | Chưa thực hiện |
| **TC_UI_04** | Login | Admin đăng nhập vào trang quản trị | Pos | Như trên | `admin@saltair.vn` / `Admin@123` | Chuyển tới `/admin` | `LoginTest` | Chưa thực hiện |
| **TC_UI_05** | Register | Đăng ký sai định dạng email | Neg | 1. Mở `/register`<br>2. Nhập `abc`<br>3. Gửi form | email `abc` | Hiện lỗi dưới ô email | `RegisterTest` | Chưa thực hiện |
| **TC_UI_06** | Product | Tìm kiếm sản phẩm | Pos | 1. Mở `/products`<br>2. Nhập từ khóa | `polo` | Danh sách chỉ có sản phẩm liên quan | `ProductTest` | Chưa thực hiện |
| **TC_UI_07** | Cart | Thêm vào giỏ khi chưa đăng nhập | Neg | 1. Mở chi tiết sản phẩm<br>2. Chọn màu, size<br>3. Bấm Thêm vào giỏ | - | Chuyển sang `/login?redirect=...` | `CartTest` | Chưa thực hiện |
| **TC_UI_08** | Admin | Khách thường vào `/admin` | Neg | Đăng nhập `khach@saltair.vn`, mở `/admin` | - | Trang 403 | `AdminAccessTest` | Chưa thực hiện |
