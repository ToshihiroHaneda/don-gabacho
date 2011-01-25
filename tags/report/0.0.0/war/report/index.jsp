<%@page pageEncoding="UTF-8" isELIgnored="false" session="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="f" uri="http://www.slim3.org/functions"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>帳票定義一覧</title>
</head>
<body>

<pre>
ここでは定義した帳票を一覧で表示します。
テンプレートの数だけ一覧に表示されるはずです。

現在定義は変更できないので、ご注意ください。
※削除もできない

</pre>

<a href="${f:url('define')}">帳票を定義</a>

<!-- 一覧部の作成 -->
<table border=1>
<tr>
<th>-</th>
<th>名称</th>
<th>詳細</th>
</tr>

<c:forEach var="e" items="${templateList}">
<tr>
<td><a href="${f:url('view?templateId=')}${e.key.id}">帳票一覧</a></td>
<td>${e.name}</td>
<td>${e.detail}</td>
</tr>
</c:forEach>

</table>


</body>
</html>
