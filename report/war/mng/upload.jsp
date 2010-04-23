<%@page pageEncoding="UTF-8" isELIgnored="false" session="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="f" uri="http://www.slim3.org/functions"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>TemplateUpload</title>
</head>
<body>

<form action="${f:url('uploadExec')}" method="post" enctype="multipart/form-data">

<input type="file" name="template">
<input type="submit">

</form>

</body>
</html>
