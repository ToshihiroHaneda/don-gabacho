<%@page pageEncoding="UTF-8" isELIgnored="false" session="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="f" uri="http://www.slim3.org/functions"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>GoogleAppEngine 帳票[don-gabacho]</title>
</head>
<body>

<h1>メニュー</h1>
<ul>
<li><a href="/report/">帳票検索</a></li>
<li>マスタメンテナンス</li>
</ul>

<pre>
don-gabachoは<a href="http://code.google.com/p/don-gabacho/">オープンソース</a>で開発された帳票ツールです。

1.帳票を定義する
  - 値とテンプレートをアップロード
2.帳票を作成する

3.帳票を表示する

現在、定義したりデータを入力したりする事は可能です。
なお[don-gabacho]はGoogleAppEngineで作成されています。

由来は「ドンと入力して、ガバっと出力できる帳票」っていうひょこりな感じです。
<a href="http://bit.ly/cOmUvt">ムマモメム</a>にするか悩みました。

出力はExcel形式で、
オープンソースの<a href="http://excella-core.sourceforge.jp/">ExCella</a>を使用しています。
ExCella開発者に感謝します。

フレームワークは<a href="http://sites.google.com/site/slim3appengine/">slim3</a>です。
<a href="http://sites.google.com/site/slim3appengine/">slim3</a>開発者に感謝。(Mavenはまだ出来ません)


</pre>
</body>
</html>
