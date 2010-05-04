<%@page pageEncoding="UTF-8" isELIgnored="false" session="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="f" uri="http://www.slim3.org/functions"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>帳票入力</title>

<script type="text/javascript">
function addLine(tableElm,argArray){

	var lastIdx = argArray.length-1;
	argArray[lastIdx]++;
	var cntName = argArray[lastIdx];

	var row = tableElm.insertRow(cntName);
	for ( var cnt = 0; cnt < lastIdx; ++cnt ) {
		var cell = row.insertCell(cnt);

		var formName = argArray[cnt];
		var textInput = '<input type="text" name="' +formName+ '_' + cntName + '" value="" />';
		cell.innerHTML = textInput;
	}

	var cell = row.insertCell(lastIdx);
	cell.innerHTML = "&nbsp;";
}

</script>
</head>
<body>
<pre>
帳票を入力します。 チェック等はまだやってません。
</pre>


<form action="${f:url('inputExec')}" method="post">
${template.name}<br>
${template.detail}<br>
<br>

名称:<input type="text" name="reportName"><br>
詳細: <input type="text" name="reportDetail"></br>

<table>
<c:forEach var="e" items="${paramList}">
<tr>
<td>${e.name}</td>
<td><input type="text" name="${e.formName}"></td>
</tr>
</c:forEach>
</table>

<c:forEach var="e" items="${repeatParamList}">
<hr>
${e.name}
  <table id="${e.formName}">
    <tr>
  <c:forEach var="r" items="${e.paramList}">
      <td>${r.name}</td>
  </c:forEach>

<script type="text/javascript">
var ${e.formName}Var =  Array( 
		<c:forEach var="r" items="${e.paramList}"> 
		"${r.formName}",  
		</c:forEach> 
		0
		);
</script>

  <td><input type="button" onclick="addLine(${e.formName},${e.formName}Var)" value="データ追加"></td>
    </tr>
  </table>

 <br> 
 <br> 
 
</c:forEach>

<hr>
<input type="hidden" name="templateId" value="${templateId}">
<input type="submit" value="入力">
</form>
</body>
</html>
