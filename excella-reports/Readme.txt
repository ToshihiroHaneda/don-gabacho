****************************
  ExCella Reports Version 1.3   
****************************

1. ExCella Reportsとは  
---------------------

  ExCella ReportsはExcelファイルをテンプレートとし、Excel形式またはPDF形式の帳票を作成する帳票ツールです。

  最新版、各種ドキュメントは以下のサイトで公開しています。
  http://sourceforge.jp/projects/excella-reports/

  ExCella Reportsは株式会社ビーブレイクシステムズが
  作成したオープンソースソフトウェアです。


2. 配布条件  
-------------

  本ソフトウェアはLGPL v3にて公開しています。
  詳細は「COPYING.LESSER」ファイルまたは
  以下のページを参照してください。
  http://www.gnu.org/licenses/lgpl-3.0-standalone.html


3. 免責  
---------

  このソフトウェアを使用したことによって生じたいかなる
  障害・損害・不具合等に関しても、株式会社ビーブレイクシステムズは
  一切の責任を負いません。各自の責任においてご使用ください。


4. バグ報告・サポート  
-----------------------

  バグ報告は以下のフォーラムにお願いします。
  http://sourceforge.jp/projects/excella-reports/forums/


5. ディレクトリ構成  
---------------------

  zipファイルのディレクトリ構成は以下の通りです。

  ■ excella-reports-bin.zip

     excella-reports-bin
     ├─apidocs   … Javadocフォルダ
     ├─lib       … ライブラリフォルダ
     └─samples   … サンプルソースフォルダ

  ■ excella-reports-all.zip

     excella-reports-all
     ├─.settings … Eclipse設定ファイル用フォルダ
     ├─apidocs   … Javadocフォルダ
     ├─build     … Antビルド用フォルダ
     ├─conf      … コンフィグファイル用フォルダ
     ├─dev       … コーディングテンプレートフォルダ
     ├─lib       … ライブラリフォルダ
     ├─samples   … サンプルソースフォルダ
     ├─src       … ソースフォルダ
     └─tests     … テストケースソースフォルダ


6. 動作環境構築  
-----------------

  ・下記のjarファイルをクラスパスに追加

    - 解凍フォルダ直下の「excella-reports-≪バージョン番号≫.jar」ファイル
    - libフォルダ下の全jarファイル


7. 依存ライブラリ一覧  
-----------------------

  ExCella reportsでは以下の外部ライブラリを使用しています。
  
○ExCella Core
    ■ExcelファイルをJavaから利用するための共通基盤
    ■http://sourceforge.jp/projects/excella-core/
    
  ・excella-core-1.5.jar
  
  ・commons-beanutils-1.6.1.jar
  ・commons-collections-3.2.jar
  ・commons-logging-1.1.1.jar
  ・log4j-1.2.14.jar
  

○Apache POI
    ■Microsoft OLE 2複合ドキュメント形式に基づいた様々なファイル形式を100%Javaで取り扱うためのAPI
    ■http://poi.apache.org/
    
   ・poi-3.6-20091214.jar
   ・poi-ooxml-3.6-20091214.jar

   ・dom4j-1.6.1.jar
   ・jsr173_1.0_api.jar
   ・log4j-1.2.14.jar
   ・poi-ooxml-schemas-3.6-20091214.jar
   ・xmlbeans-2.3.0.jar

○JODConverter
    ■OpenOffice.orgの機能を使ってオフィス(Microsoft Office)ファイルのデータ変換を実施するアプリケーション  
    ■http://code.google.com/p/jodconverter/

  ・jodconverter-3.0-beta-3.jar
  
  ・commons-io-1.4.jar
  ・json-20080701.jar
  ・juh-3.0.1.jar
  ・jurt-3.0.1.jar
  ・ridl-3.0.1.jar
  ・unoil-3.0.1.jar
  


8. 更新履歴  
-------------
  ・2010/01/20 Version 1.3 リリース
  ・2009/11/20 Version 1.2 リリース
  ・2009/10/30 Version 1.1 リリース
  ・2009/06/25 Version 1.0 リリース


Copyright 2009 by bBreak Systems.
