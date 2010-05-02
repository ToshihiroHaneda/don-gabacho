<%@page pageEncoding="UTF-8" isELIgnored="false" session="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="f" uri="http://www.slim3.org/functions"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>report Index</title>
</head>
<body>

<a href="${f:url('define')}">帳票定義</a>

<!-- 一覧部の作成 -->
<table>
<tr>
<th>-</th>
<th>名称</th>
<th>詳細</th>
</tr>

<c:forEach var="e" items="${templateList}">
<tr>
<td></td>
<td>${e.name}</td>
<td>${e.detail}</td>
</tr>
</c:forEach>

</table>


</body>
</html>
