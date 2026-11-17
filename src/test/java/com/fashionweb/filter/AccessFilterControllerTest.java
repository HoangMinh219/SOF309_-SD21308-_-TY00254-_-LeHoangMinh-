package com.fashionweb.filter;

import com.fashionweb.constant.AppConstant;
import com.fashionweb.constant.Role;
import com.fashionweb.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Kiểm thử phân quyền: AuthFilter (cần đăng nhập) và AdminFilter (chỉ ADMIN vào /admin). */
class AccessFilterControllerTest {

    private static final String CTX = "/saltair";

    private HttpServletRequest req;
    private HttpServletResponse resp;
    private HttpSession session;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        req = mock(HttpServletRequest.class);
        resp = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        chain = mock(FilterChain.class);
        when(req.getContextPath()).thenReturn(CTX);
        when(req.getMethod()).thenReturn("GET");
    }

    private void loginAs(Role role) {
        User u = new User();
        u.setRole(role);
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute(AppConstant.SESSION_USER)).thenReturn(u);
    }

    @ParameterizedTest(name = "[{index}] {0}?{1} -> {2}")
    @DisplayName("TC_FILTER_01: AuthFilter chưa đăng nhập -> redirect /login?redirect=<trang đang mở>")
    @CsvSource(nullValues = "NULL", value = {
            "/saltair/orders,  NULL,     /saltair/login?redirect=%2Forders",
            "/saltair/profile, tab=info, /saltair/login?redirect=%2Fprofile%3Ftab%3Dinfo",
            "/saltair/wishlist, NULL,    /saltair/login?redirect=%2Fwishlist"
    })
    void authFilterRedirectsGuest(String uri, String query, String expected) throws Exception {
        when(req.getRequestURI()).thenReturn(uri);
        when(req.getQueryString()).thenReturn(query);
        new AuthFilter().doFilter(req, resp, chain);
        verify(resp).sendRedirect(expected);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("TC_FILTER_02: AuthFilter đã đăng nhập -> cho đi tiếp")
    void authFilterPassesLoggedIn() throws Exception {
        loginAs(Role.CUSTOMER);
        new AuthFilter().doFilter(req, resp, chain);
        verify(chain).doFilter(req, resp);
        verify(resp, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("TC_FILTER_03: AuthFilter POST dùng Referer cùng host để quay lại đúng trang")
    void authFilterUsesRefererForPost() throws Exception {
        when(req.getMethod()).thenReturn("POST");
        when(req.getRequestURI()).thenReturn(CTX + "/wishlist");
        when(req.getServerName()).thenReturn("localhost");
        when(req.getHeader("Referer")).thenReturn("http://localhost:8080/saltair/product?slug=ao-polo");
        new AuthFilter().doFilter(req, resp, chain);
        verify(resp).sendRedirect(CTX + "/login?redirect=%2Fproduct%3Fslug%3Dao-polo");
    }

    @Test
    @DisplayName("TC_FILTER_04: AuthFilter POST bỏ qua Referer của trang web khác (chống open-redirect)")
    void authFilterIgnoresForeignReferer() throws Exception {
        when(req.getMethod()).thenReturn("POST");
        when(req.getRequestURI()).thenReturn(CTX + "/wishlist");
        when(req.getServerName()).thenReturn("localhost");
        when(req.getHeader("Referer")).thenReturn("http://evil.com/saltair/x");
        new AuthFilter().doFilter(req, resp, chain);
        verify(resp).sendRedirect(CTX + "/login?redirect=%2Fwishlist");
    }

    @Test
    @DisplayName("TC_FILTER_05: AdminFilter chưa đăng nhập -> redirect /login")
    void adminFilterRedirectsGuest() throws Exception {
        when(req.getRequestURI()).thenReturn(CTX + "/admin/products");
        when(req.getQueryString()).thenReturn(null);
        new AdminFilter().doFilter(req, resp, chain);
        verify(resp).sendRedirect(CTX + "/login?redirect=%2Fadmin%2Fproducts");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("TC_FILTER_06: AdminFilter khách CUSTOMER vào /admin -> 403")
    void adminFilterForbidsCustomer() throws Exception {
        loginAs(Role.CUSTOMER);
        when(req.getRequestURI()).thenReturn(CTX + "/admin");
        new AdminFilter().doFilter(req, resp, chain);
        verify(resp).sendError(HttpServletResponse.SC_FORBIDDEN);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("TC_FILTER_07: AdminFilter ADMIN -> cho đi tiếp")
    void adminFilterPassesAdmin() throws Exception {
        loginAs(Role.ADMIN);
        when(req.getRequestURI()).thenReturn(CTX + "/admin/orders");
        new AdminFilter().doFilter(req, resp, chain);
        verify(chain).doFilter(req, resp);
    }

    @Test
    @DisplayName("TC_FILTER_08: AdminFilter cho /admin/login đi qua dù chưa đăng nhập")
    void adminFilterAllowsLegacyLoginPath() throws Exception {
        when(req.getRequestURI()).thenReturn(CTX + "/admin/login");
        new AdminFilter().doFilter(req, resp, chain);
        verify(chain).doFilter(req, resp);
    }
}
