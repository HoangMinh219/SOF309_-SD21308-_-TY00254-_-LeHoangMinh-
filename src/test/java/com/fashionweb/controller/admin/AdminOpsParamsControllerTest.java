package com.fashionweb.controller.admin;

import com.fashionweb.constant.OrderStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Kiểm thử hàm đọc tham số dùng chung của các Servlet Admin: không bao giờ ném lỗi, sai thì trả mặc định. */
class AdminOpsParamsControllerTest {

    private static HttpServletRequest requestWith(String name, String value) {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getParameter(name)).thenReturn(value);
        return req;
    }

    @ParameterizedTest(name = "[{index}] page=''{0}'' -> {1}")
    @DisplayName("TC_ADM_PARAM_01: page luôn >= 1, giá trị sai quay về 1")
    @CsvSource(nullValues = "NULL", value = {
            "NULL, 1", "'', 1", "abc, 1", "-5, 1", "0, 1", "1, 1", "2, 2", "' 7 ', 7", "99999, 99999", "1.5, 1"
    })
    void page(String raw, int expected) {
        assertEquals(expected, AdminOpsParams.page(requestWith("page", raw)));
    }

    @ParameterizedTest(name = "[{index}] ''{0}'' -> {1}")
    @DisplayName("TC_ADM_PARAM_02: positiveInt chỉ nhận số nguyên dương")
    @CsvSource(nullValues = "NULL", value = {
            "NULL, NULL", "'', NULL", "'  ', NULL", "abc, NULL", "0, NULL", "-1, NULL",
            "1, 1", "' 42 ', 42", "2147483647, 2147483647", "2147483648, NULL"
    })
    void positiveInt(String raw, Integer expected) {
        assertEquals(expected, AdminOpsParams.positiveInt(raw));
    }

    @ParameterizedTest(name = "[{index}] ''{0}'' -> {1}")
    @DisplayName("TC_ADM_PARAM_03: enumValue không phân biệt hoa thường, sai thì null")
    @CsvSource(nullValues = "NULL", value = {
            "pending, PENDING", "SHIPPING, SHIPPING", " completed , COMPLETED", "Cancelled, CANCELLED",
            "NULL, NULL", "'', NULL", "refunded, NULL", "pend ing, NULL"
    })
    void enumValue(String raw, OrderStatus expected) {
        assertEquals(expected, AdminOpsParams.enumValue(OrderStatus.class, raw));
    }

    @Test
    @DisplayName("TC_ADM_PARAM_04: text trim, null -> chuỗi rỗng, cắt tối đa 100 ký tự")
    void text() {
        assertEquals("", AdminOpsParams.text(requestWith("q", null), "q"));
        assertEquals("áo thun", AdminOpsParams.text(requestWith("q", "  áo thun  "), "q"));
        assertEquals(100, AdminOpsParams.text(requestWith("q", "x".repeat(250)), "q").length());
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    @DisplayName("TC_ADM_PARAM_05: Đọc 10.000 tham số bẩn liên tiếp không treo và không ném lỗi")
    void neverThrowsAndNeverHangs() {
        for (int i = 0; i < 10_000; i++) {
            String dirty = i % 2 == 0 ? "%00'; DROP TABLE users;--" + i : null;
            assertEquals(1, AdminOpsParams.page(requestWith("page", dirty)));
            assertNull(AdminOpsParams.positiveInt(dirty));
        }
    }
}
