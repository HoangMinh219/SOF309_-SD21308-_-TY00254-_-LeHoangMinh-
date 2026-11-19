<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng nhập - PolyCoffee</title>
</head>
<body>
<h2>Đăng nhập PolyCoffee</h2>
<c:if test="${not empty error}">
    <p style="color:red">${error}</p>
</c:if>
<form method="post" action="${pageContext.request.contextPath}/login">
    <label>Email: <input type="email" name="email" value="${email}" required></label><br><br>
    <label>Mật khẩu: <input type="password" name="password" required></label><br><br>
    <button type="submit">Đăng nhập</button>
</form>
</body>
</html>
