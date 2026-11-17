package com.fashionweb.controller.auth;

import com.fashionweb.constant.AppConstant;
import com.fashionweb.constant.Role;
import com.fashionweb.exception.ValidationException;
import com.fashionweb.model.User;
import com.fashionweb.service.auth.AuthService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test cho LoginServlet (không cần Tomcat, không cần MySQL).
 * Request/response là mock. Các ca kiểm tra dữ liệu đầu vào dùng AuthService thật (lỗi ném trước khi chạm DB);
 * các ca đăng nhập thành công dùng AuthService giả để không cần database.
 */
class LoginControllerTest {

    private static final String CTX = "/saltair";
    private static final String LOGIN_JSP = "/WEB-INF/views/auth/login.jsp";

    private HttpServletRequest req;
    private HttpServletResponse resp;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        req = mock(HttpServletRequest.class);
        resp = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);
        when(req.getContextPath()).thenReturn(CTX);
        when(req.getServletPath()).thenReturn("/login");
        when(req.getRequestDispatcher(LOGIN_JSP)).thenReturn(dispatcher);
    }

    private static User user(Role role) {
        User u = new User();
        u.setId(1);
        u.setEmail(role == Role.ADMIN ? "admin@saltair.vn" : "khach@saltair.vn");
        u.setRole(role);
        u.setActive(true);
        return u;
    }

    // ---------- GET ----------

    @Test
    @DisplayName("TC_AUTH_01: GET /login khi chưa đăng nhập -> forward login.jsp")
    void getShowsLoginForm() throws Exception {
        new LoginServlet().doGet(req, resp);
        verify(dispatcher).forward(req, resp);
        verify(resp, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("TC_AUTH_02: GET /admin/login (đường dẫn cũ) -> redirect /login kèm tham số redirect đã mã hóa")
    void getOldAdminLoginRedirects() throws Exception {
        when(req.getServletPath()).thenReturn("/admin/login");
        when(req.getParameter("redirect")).thenReturn("/admin/orders?x=1");
        new LoginServlet().doGet(req, resp);
        verify(resp).sendRedirect(CTX + "/login?redirect=%2Fadmin%2Forders%3Fx%3D1");
    }

    @ParameterizedTest(name = "[{index}] đã đăng nhập role {0} -> {1}")
    @DisplayName("TC_AUTH_03: GET /login khi đã đăng nhập -> CUSTOMER về /home, ADMIN về /admin")
    @CsvSource({"CUSTOMER, /home", "ADMIN, /admin"})
    void getWhenLoggedInRedirects(Role role, String expected) throws Exception {
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute(AppConstant.SESSION_USER)).thenReturn(user(role));
        new LoginServlet().doGet(req, resp);
        verify(resp).sendRedirect(CTX + expected);
    }

    // ---------- POST: dữ liệu không hợp lệ (AuthService thật, không chạm DB) ----------

    @ParameterizedTest(name = "[{index}] email=''{0}'', mật khẩu=''{1}'' -> lỗi ô {2}")
    @DisplayName("TC_AUTH_04..09: POST /login dữ liệu sai -> ở lại form, errors đúng ô, không redirect")
    @CsvSource({
            "'',               Khach@123, email,    Vui lòng nhập email.",
            "'   ',            Khach@123, email,    Vui lòng nhập email.",
            "khach,            Khach@123, email,    Email không đúng định dạng.",
            "khach@,           Khach@123, email,    Email không đúng định dạng.",
            "khach@saltair,    Khach@123, email,    Email không đúng định dạng.",
            "khach@saltair.vn, '',        password, Vui lòng nhập mật khẩu."
    })
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @SuppressWarnings("unchecked")
    void postInvalidInputShowsErrors(String email, String password, String field, String message) throws Exception {
        when(req.getParameter("email")).thenReturn(email);
        when(req.getParameter("password")).thenReturn(password);

        new LoginServlet().doPost(req, resp);

        ArgumentCaptor<Map<String, String>> errors = ArgumentCaptor.forClass(Map.class);
        verify(req).setAttribute(eq("errors"), errors.capture());
        assertEquals(message, errors.getValue().get(field));
        verify(dispatcher).forward(req, resp);
        verify(resp, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("TC_AUTH_10: POST /login sai mật khẩu hoặc tài khoản bị khóa -> hiện lỗi từ Service")
    void postServiceRejectsCredentials() throws Exception {
        when(req.getParameter("email")).thenReturn("mai.pham@example.com");
        when(req.getParameter("password")).thenReturn("Khach@123");
        try (MockedConstruction<AuthService> mocked = mockConstruction(AuthService.class,
                (svc, ctx) -> when(svc.login(anyString(), anyString()))
                        .thenThrow(new ValidationException("password", AuthService.MSG_LOCKED)))) {
            new LoginServlet().doPost(req, resp);
        }
        verify(req).setAttribute(eq("errors"), any());
        verify(dispatcher).forward(req, resp);
        verify(resp, never()).sendRedirect(anyString());
    }

    // ---------- POST: đăng nhập thành công (AuthService giả) ----------

    @ParameterizedTest(name = "[{index}] role={0}, redirect=''{1}'' -> {2}")
    @DisplayName("TC_AUTH_11..17: POST /login thành công -> redirect đúng trang theo role")
    @CsvSource(nullValues = "NULL", value = {
            "CUSTOMER, NULL,             /home",
            "CUSTOMER, /cart,            /cart",
            "CUSTOMER, /admin/orders,    /home",
            "CUSTOMER, //evil.com,       /home",
            "ADMIN,    NULL,             /admin",
            "ADMIN,    /admin/vouchers,  /admin/vouchers",
            "ADMIN,    /cart,            /admin"
    })
    void postSuccessRedirects(Role role, String redirect, String expected) throws Exception {
        User u = user(role);
        when(req.getParameter("email")).thenReturn(u.getEmail());
        when(req.getParameter("password")).thenReturn("matkhau123");
        when(req.getParameter("redirect")).thenReturn(redirect);
        when(req.getRemoteAddr()).thenReturn("127.0.0.1");
        when(req.getHeader("User-Agent")).thenReturn("JUnit");

        try (MockedConstruction<AuthService> mocked = mockConstruction(AuthService.class,
                (svc, ctx) -> when(svc.login(u.getEmail(), "matkhau123")).thenReturn(u));
             MockedStatic<LoginSupport> support = mockStatic(LoginSupport.class, CALLS_REAL_METHODS)) {
            support.when(() -> LoginSupport.login(any(), any())).thenReturn(session);

            new LoginServlet().doPost(req, resp);

            AuthService svc = mocked.constructed().get(0);
            if (role == Role.ADMIN) {
                verify(session).setMaxInactiveInterval(AppConstant.ADMIN_SESSION_SECONDS);
                verify(svc).notifyAdminLogin(u, "127.0.0.1", "JUnit");
            } else {
                verify(session, never()).setMaxInactiveInterval(AppConstant.ADMIN_SESSION_SECONDS);
                verify(svc, never()).notifyAdminLogin(any(), any(), any());
            }
        }
        verify(resp).sendRedirect(CTX + expected);
    }
}
