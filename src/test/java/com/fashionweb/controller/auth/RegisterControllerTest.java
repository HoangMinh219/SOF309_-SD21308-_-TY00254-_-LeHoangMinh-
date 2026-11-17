package com.fashionweb.controller.auth;

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

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

/** Unit test cho RegisterServlet: kiểm tra biên form đăng ký và luồng chuyển hướng (không cần DB). */
class RegisterControllerTest {

    private static final String CTX = "/saltair";
    private static final String REGISTER_JSP = "/WEB-INF/views/auth/register.jsp";

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
        when(req.getSession()).thenReturn(session);
        when(req.getRequestDispatcher(REGISTER_JSP)).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("TC_REG_01: GET /register khi chưa đăng nhập -> forward register.jsp")
    void getShowsForm() throws Exception {
        new RegisterServlet().doGet(req, resp);
        verify(dispatcher).forward(req, resp);
    }

    /**
     * Mọi ca dùng email sai định dạng để AuthService dừng trước khi truy vấn DB, nhưng vẫn kiểm được
     * các ô còn lại (họ tên, mật khẩu, nhập lại) vì Service gom lỗi theo từng ô.
     */
    @ParameterizedTest(name = "[{index}] {4}")
    @DisplayName("TC_REG_02..08: POST /register giá trị biên -> errors đúng tập ô lỗi")
    @CsvSource(delimiter = ';', value = {
            "''     ; Minh ; Pass@1234 ; Pass@1234 ; email trống                    ; email",
            "abc    ; Minh ; Pass@1234 ; Pass@1234 ; email thiếu @                  ; email",
            "a@b    ; Minh ; Pass@1234 ; Pass@1234 ; email thiếu phần .domain       ; email",
            "abc    ; ''   ; Pass@1234 ; Pass@1234 ; email sai + họ tên trống       ; email,fullName",
            "abc    ; Minh ; 1234567   ; 1234567   ; mật khẩu 7 ký tự (biên dưới-1) ; email,password",
            "abc    ; Minh ; Pass@1234 ; Pass@9999 ; nhập lại mật khẩu không khớp   ; email,confirm",
            "abc    ; ''   ; ''        ; ''        ; tất cả trống                   ; email,fullName,password"
    })
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @SuppressWarnings("unchecked")
    void postBoundaryValues(String email, String name, String password, String confirm,
                            String description, String expectedFields) throws Exception {
        when(req.getParameter("email")).thenReturn(email);
        when(req.getParameter("fullName")).thenReturn(name);
        when(req.getParameter("password")).thenReturn(password);
        when(req.getParameter("confirm")).thenReturn(confirm);

        new RegisterServlet().doPost(req, resp);

        ArgumentCaptor<Map<String, String>> errors = ArgumentCaptor.forClass(Map.class);
        verify(req).setAttribute(eq("errors"), errors.capture());
        Set<String> expected = Arrays.stream(expectedFields.split(",")).map(String::trim).collect(Collectors.toSet());
        assertEquals(expected, errors.getValue().keySet(), description);
        verify(dispatcher).forward(req, resp);
        verify(resp, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("TC_REG_11: Biên trên mật khẩu 65 ký tự và họ tên 101 ký tự bị từ chối")
    @SuppressWarnings("unchecked")
    void postTooLongValues() throws Exception {
        when(req.getParameter("email")).thenReturn("abc");
        when(req.getParameter("fullName")).thenReturn("N".repeat(101));
        when(req.getParameter("password")).thenReturn("p".repeat(65));
        when(req.getParameter("confirm")).thenReturn("p".repeat(65));

        new RegisterServlet().doPost(req, resp);

        ArgumentCaptor<Map<String, String>> errors = ArgumentCaptor.forClass(Map.class);
        verify(req).setAttribute(eq("errors"), errors.capture());
        assertEquals("Họ tên tối đa 100 ký tự.", errors.getValue().get("fullName"));
        assertEquals("Mật khẩu tối đa 64 ký tự.", errors.getValue().get("password"));
    }

    @Test
    @DisplayName("TC_REG_12: Email đã có tài khoản -> Service ném ValidationException, form hiện lỗi ở ô email")
    @SuppressWarnings("unchecked")
    void postDuplicateEmailShowsError() throws Exception {
        when(req.getParameter("email")).thenReturn("khach@saltair.vn");
        try (MockedConstruction<AuthService> mocked = mockConstruction(AuthService.class,
                (svc, ctx) -> when(svc.register(anyString(), any(), any(), any()))
                        .thenThrow(new ValidationException("email", "Email này đã có tài khoản.")))) {
            new RegisterServlet().doPost(req, resp);
        }
        ArgumentCaptor<Map<String, String>> errors = ArgumentCaptor.forClass(Map.class);
        verify(req).setAttribute(eq("errors"), errors.capture());
        assertEquals("Email này đã có tài khoản.", errors.getValue().get("email"));
        verify(dispatcher).forward(req, resp);
    }

    @ParameterizedTest(name = "[{index}] redirect=''{0}'' -> {1}")
    @DisplayName("TC_REG_13..16: Đăng ký thành công -> tự đăng nhập, redirect an toàn")
    @CsvSource(nullValues = "NULL", value = {
            "NULL,          /home",
            "/cart,         /cart",
            "/admin/users,  /home",
            "https://x.com, /home"
    })
    void postSuccessRedirects(String redirect, String expected) throws Exception {
        User created = new User();
        created.setId(10);
        created.setEmail("moi@saltair.vn");
        created.setRole(Role.CUSTOMER);
        when(req.getParameter("email")).thenReturn("moi@saltair.vn");
        when(req.getParameter("redirect")).thenReturn(redirect);

        try (MockedConstruction<AuthService> mocked = mockConstruction(AuthService.class,
                (svc, ctx) -> when(svc.register(any(), any(), any(), any())).thenReturn(created));
             MockedStatic<LoginSupport> support = mockStatic(LoginSupport.class, CALLS_REAL_METHODS)) {
            support.when(() -> LoginSupport.login(any(), any())).thenReturn(session);
            new RegisterServlet().doPost(req, resp);
            support.verify(() -> LoginSupport.login(req, created));
        }
        verify(session).setAttribute("flashType", "success");
        verify(resp).sendRedirect(CTX + expected);
    }
}
