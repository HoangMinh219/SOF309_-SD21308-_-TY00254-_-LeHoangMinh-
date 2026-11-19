<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Loại đồ uống - PolyCoffee</title>
</head>
<body>
<h2>Quản lý loại đồ uống</h2>
<p><a href="${pageContext.request.contextPath}/manager/category?action=new">+ Thêm loại mới</a> |
   <a href="${pageContext.request.contextPath}/logout">Đăng xuất</a></p>

<c:if test="${param.msg == 'saved'}"><p style="color:green">Đã lưu loại đồ uống.</p></c:if>
<c:if test="${param.msg == 'deleted'}"><p style="color:green">Đã xóa loại đồ uống.</p></c:if>
<c:if test="${param.error == 'delete'}"><p style="color:red">Không thể xóa: loại này đang có đồ uống.</p></c:if>
<c:if test="${param.error == 'notfound'}"><p style="color:red">Không tìm thấy loại đồ uống.</p></c:if>

<table border="1" cellpadding="6">
    <tr><th>Mã</th><th>Tên</th><th>Mô tả</th><th>Hoạt động</th><th>Thao tác</th></tr>
    <c:forEach var="c" items="${categories}">
        <tr>
            <td>${c.id}</td>
            <td>${c.name}</td>
            <td>${c.description}</td>
            <td>${c.active ? 'Có' : 'Không'}</td>
            <td>
                <a href="${pageContext.request.contextPath}/manager/category?action=edit&id=${c.id}">Sửa</a> |
                <a href="${pageContext.request.contextPath}/manager/category?action=delete&id=${c.id}"
                   onclick="return confirm('Xóa loại này?')">Xóa</a>
            </td>
        </tr>
    </c:forEach>
</table>
</body>
</html>
