<%@page pageEncoding="UTF-8" isELIgnored="false" session="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="f" uri="http://www.slim3.org/functions"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>report View</title>
</head>
<body>

<pre>
ここでは入力した帳票を一覧で表示します。
入力したデータはExcel出力できるようになります。
現在変更できないので、ご注意ください。
※削除もできない

</pre>

<a href="${f:url('input?templateId=')}${templateId}">帳票を入力する</a>

<!-- 一覧部の作成 -->
<table border=1>
<tr>
<th>-</th>
<th>名称</th>
<th>詳細</th>
<th>-</th>
</tr>

<c:forEach var="e" items="${reportList}">
<tr>
<td><!-- <a href="${f:url('edit?reportId=')}${e.key.id}">表示</a> --></td>
<td>${e.name}</td>
<td>${e.detail}</td>
<td><a href="${f:url('export?reportId=')}${e.key.id}">出力</a></td>
</tr>
</c:forEach>

</table>

</body>
</html>
