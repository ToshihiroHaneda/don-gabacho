/*************************************************************************
 *
 * Copyright 2009 by bBreak Systems.
 *
 * ExCella Core - ExcelファイルをJavaから利用するための共通基盤
 *
 * $Id: BookExporter.java 2 2009-05-08 07:39:20Z yuta-takahashi $
 * $Revision: 2 $
 *
 * This file is part of ExCella Core.
 *
 * ExCella Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * only, as published by the Free Software Foundation.
 *
 * ExCella Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License version 3 for more details
 * (a copy is included in the COPYING.LESSER file that accompanied this code).
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with ExCella Core.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>
 * for a copy of the LGPLv3 License.
 *
 ************************************************************************/
package org.bbreak.excella.core.exporter.book;

import org.apache.poi.ss.usermodel.Workbook;
import org.bbreak.excella.core.BookData;
import org.bbreak.excella.core.exception.ExportException;

/**
 * ブック解析結果の出力処理用インターフェイス
 * 
 * 下記の順でメソッドが呼び出されます。
 * 
 * 1.setup
 * 2.export
 * 3.tearDown
 * 
 * @since 1.0
 */
public interface BookExporter {
	/**
     * 初期化処理
     */
    public void setup();

    /**
     * 出力処理の実行
     * 
     * @param book 処理後のブック
     * @param bookdata 処理結果のデータ
     */
    public void export( Workbook book, BookData bookdata) throws ExportException;

    /**
     * 終了処理
     */
    public void tearDown();
}
