#ExCellaの変更点

# ExCellaの変更点 #

2010/04/25　現状の状況を残すようにリファクタリング

・PoiUtil::writeBook()の引数をOutputStreamに変更

・TextFileExporter(book,sheet)、WorkbookExporterを削除

・DebugErrorHandlerのwrite呼び出しをコメント化(需要があれば何らかの形で復活)

・BookController::parseSheet() におけるerrorHandler.notifyException()呼び出しを削除

・ReportBookExporterの出力ファイルパスをOutputStreamに変更

・OoPdfExporterを削除。jodconverter依存(FileのIFだから)の為※ライブラリも削除

・ReportCreateHelperのデフォルトからOoPdfExporterを削除

・ReportProcessorへのファイルパス設定時にByteArrayOutputStreamを設定

・ExcelOutputStreamExporterを削除(ExcelExporterで対応)

・ReportProssor::getTemplateWorkbook()をInputStreamに変更

・ReportBookをInputStreamとOutputStreamに変更


・ExportException、ParseExceptionをRuntimeException派生に変更

・ConsoleExporterを削除

・throws句をすべてにおいて削除


以上の変更を行いました。（ヌケはないはず）

ExCellaの仕様をなるべく継承するように変更しました。
一番の変更点は

・ファイル系の書き込みを排除したこと

・出力パスの指定はできず、ByteArrayOutputStreamに変更したこと

ですかね？
Excel2007には対応できてないはずですが、<br>
おそらくそこを触るにはPOIも触る必要があるかと思います。<br>
PDFに関してもIFがFileなのでちょっと難しいかな？