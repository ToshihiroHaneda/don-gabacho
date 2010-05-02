<%@page pageEncoding="UTF-8" isELIgnored="false" session="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="f" uri="http://www.slim3.org/functions"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>帳票定義</title>

<script type="text/javascript">
var paramCnt = 0;
var listParam = new Array(100);
var listCnt = 0;

function addTableRows(tableName,listNum){

	// カウンタを回す
	var counter = 0;
	if ( tableName == "param" ) {
		paramCnt++;
		counter = paramCnt;
	} else {
		listParam[listNum]++;
		counter = listParam[listNum];
	}

	var table1 = document.getElementById(tableName);
	var row1 = table1.insertRow(counter);
	var cell1 = row1.insertCell(0);
	var cell2 = row1.insertCell(1);
	var cell3 = row1.insertCell(2);

	// class の付与は UserAgent によって
	// 挙動が違うっぽいので念のため両方の方法で
	cell1.setAttribute("class","name");
	cell2.setAttribute("class","type");
	cell2.setAttribute("class","attr");
	cell1.className = 'name';
	cell2.className = 'type';
	cell3.className = 'attr';


	var HTML1 = '<input type="text" name="' +tableName+ 'Name' + counter + '" value="" size="10" />';
	var HTML2 = 
	      '<select name="' + tableName + 'Type' + counter + '">' +
    		'<option value="1">文字列' +
    		'<option value="2">数値' +
    		'<option value="3">通番' +
    		'<option value="4">日付' +
    		'<option value="5">マスタ値' +
    		'<option value="6">画像' +
  			'</select>';
	var HTML3 = '<input type="text" name="' +tableName+ 'Attr' + counter + '" value="" size="10" />';
	cell1.innerHTML = HTML1;
	cell2.innerHTML = HTML2;
	cell3.innerHTML = HTML3;
}

function addList() {
	++listCnt;
	listParam[listCnt] = 0;

	//リスト用のデータを追加する
	var div = document.getElementById("listDiv");

	var tableId    = "repeatParam"  + listCnt;
	var listName   = "repeatName"   + listCnt;
	var listDetail = "repeatDetail" + listCnt;

	var tableHTML =
	'リスト' + listCnt + '<br>' +
	'<input type="text" name="' + listName   + '">' +
	'<input type="text" name="' + listDetail + '">' +
	'<br>' + 
	'<input type="button" onclick="addTableRows(\'' + tableId + '\',' + listCnt + ')" value="リスト内定義追加">' + 
	'<table cellspacing="0" id="' + tableId + '">' + 
    '<tr>' +
      '<th>名前</th>' +
      '<th>タイプ</th>' +
      '<th>属性</th>' +
    '</tr>' +
	'</table>';
	div.innerHTML = div.innerHTML + tableHTML;
}

</script>
</head>
<body>

<form action="${f:url('defineExec')}" method="post" enctype="multipart/form-data" >
<!-- 名称 -->
<input type="text" name="name">
<!-- 詳細 -->
<input type="text" name="detail">
<!-- 定義ファイル -->
<input type="file" name="templateFile">
<br>
<!-- 定義 -->
<input type="button" onclick="addTableRows('param')" value="定義追加">
<table cellspacing="0" id="param">
  <tr>
    <th>名前</th>
    <th>タイプ</th>
    <th>属性</th>
  </tr>

</table>


<!-- リスト表示 -->

<div id="listDiv">
</div>

<br>
<input type="button" onclick="addList()" value="リスト追加">


<br>
<br>
<!-- 定義 -->
<input type="submit" value="定義">

</form>

</body>
</html>
