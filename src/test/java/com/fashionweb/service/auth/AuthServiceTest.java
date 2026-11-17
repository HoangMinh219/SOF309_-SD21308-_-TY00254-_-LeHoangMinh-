package com.fashionweb.service.auth;

import com.fashionweb.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Kiểm thử lớp Service mà Controller đăng nhập/đăng ký gọi. Chỉ chạy các nhánh không chạm database. */
class AuthServiceTest {

    private final AuthService service = new AuthService();

    @ParameterizedTest(name = "[{index}] ''{0}'' -> ''{1}''")
    @DisplayName("normalizeEmail: trim và chuyển chữ thường")
    @CsvSource({"'  Khach@Saltair.VN  ', khach@saltair.vn", "ADMIN@SALTAIR.VN, admin@saltair.vn", "a@b.co, a@b.co"})
    void normalizeEmailOk(String raw, String expected) {
        assertEquals(expected, service.normalizeEmail(raw));
    }

    @ParameterizedTest(name = "[{index}] ''{0}'' -> {1}")
    @DisplayName("normalizeEmail: ném ValidationException gắn ô email khi sai")
    @CsvSource(nullValues = "NULL", value = {
            "NULL, Vui lòng nhập email.", "'', Vui lòng nhập email.", "'   ', Vui lòng nhập email.",
            "abc, Email không đúng định dạng.", "a@b, Email không đúng định dạng.",
            "@b.com, Email không đúng định dạng.", "'a b@c.com', Email không đúng định dạng."
    })
    void normalizeEmailInvalid(String raw, String message) {
        ValidationException ex = assertThrows(ValidationException.class, () -> service.normalizeEmail(raw));
        assertEquals(message, ex.getErrors().get("email"));
        assertEquals(message, ex.getMessage());
    }

    @Test
    @DisplayName("normalizeEmail: email dài 151 ký tự bị từ chối, 150 ký tự hợp lệ")
    void normalizeEmailLengthBoundary() {
        String local150 = "a".repeat(150 - "@x.vn".length()) + "@x.vn";
        assertEquals(150, local150.length());
        assertEquals(local150, service.normalizeEmail(local150));
        assertThrows(ValidationException.class, () -> service.normalizeEmail("a" + local150));
    }

    @Test
    @DisplayName("login: thiếu mật khẩu -> ValidationException ô password")
    void loginRequiresPassword() {
        ValidationException ex = assertThrows(ValidationException.class, () -> service.login("khach@saltair.vn", ""));
        assertEquals("Vui lòng nhập mật khẩu.", ex.getErrors().get("password"));
        assertThrows(ValidationException.class, () -> service.login("khach@saltair.vn", null));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisplayName("register: dữ liệu rỗng gom đủ lỗi từng ô và không treo")
    void registerCollectsAllErrors() {
        ValidationException ex = assertThrows(ValidationException.class, () -> service.register("", "", "", ""));
        assertTrue(ex.getErrors().keySet().containsAll(java.util.List.of("email", "fullName", "password")));
    }

    @Test
    @DisplayName("Hằng số biên mật khẩu: 8 đến 64 ký tự")
    void passwordBounds() {
        assertEquals(8, AuthService.PASSWORD_MIN);
        assertEquals(64, AuthService.PASSWORD_MAX);
    }
}
