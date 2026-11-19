<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Form loại đồ uống - PolyCoffee</title>
</head>
<body>
<h2>${empty category.id ? 'Thêm loại đồ uống' : 'Sửa loại đồ uống'}</h2>
<c:if test="${not empty error}"><p style="color:red">${error}</p></c:if>
<form method="post" action="${pageContext.request.contextPath}/manager/category">
    <input type="hidden" name="action" value="save">
    <input type="hidden" name="id" value="${category.id}">
    <label>Tên: <input type="text" name="name" value="${category.name}" required></label><br><br>
    <label>Mô tả: <textarea name="description">${category.description}</textarea></label><br><br>
    <label><input type="checkbox" name="active" ${category.active ? 'checked' : ''}> Đang hoạt động</label><br><br>
    <button type="submit">Lưu</button>
    <a href="${pageContext.request.contextPath}/manager/category">Hủy</a>
</form>
</body>
</html>
