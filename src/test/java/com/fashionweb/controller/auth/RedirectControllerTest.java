package com.fashionweb.controller.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Kiểm thử LoginSupport.safeRedirect: chặn open-redirect sau khi đăng nhập (TC_AUTH_REDIR_01..03). */
class RedirectControllerTest {

    @ParameterizedTest(name = "[{index}] redirect=''{0}'' -> {1}")
    @DisplayName("safeRedirect: chỉ nhận đường dẫn nội bộ hợp lệ")
    @CsvSource(nullValues = "NULL", value = {
            "/cart,                 /cart",
            "/orders?page=2,        /orders?page=2",
            "/admin/products,       /admin/products",
            "'  /wishlist  ',       /wishlist",
            "NULL,                  NULL",
            "'',                    NULL",
            "'   ',                 NULL",
            "//evil.com,            NULL",
            "https://evil.com,      NULL",
            "javascript:alert(1),   NULL",
            "cart,                  NULL",
            "/login,                NULL",
            "/login?redirect=/cart, NULL",
            "/logout,               NULL",
            "/register,             NULL",
            "/admin/login,          NULL"
    })
    void safeRedirect(String input, String expected) {
        assertEquals(expected, LoginSupport.safeRedirect(input));
    }

    @Test
    @DisplayName("safeRedirect: chặn dấu gạch chéo ngược và ký tự xuống dòng (header injection)")
    void rejectsBackslashAndLineBreaks() {
        assertNull(LoginSupport.safeRedirect("/\\evil.com"));
        assertNull(LoginSupport.safeRedirect("/cart\r\nSet-Cookie: a=b"));
        assertNull(LoginSupport.safeRedirect("/cart\nX: y"));
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    @DisplayName("safeRedirect: chuỗi rất dài vẫn xử lý xong trong 2 giây (không treo)")
    void handlesVeryLongInputQuickly() {
        String longPath = "/" + "a".repeat(100_000);
        assertEquals(longPath, LoginSupport.safeRedirect(longPath));
    }
}
