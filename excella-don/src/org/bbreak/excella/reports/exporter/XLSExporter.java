/*************************************************************************
 *
 * Copyright 2009 by bBreak Systems.
 *
 * ExCella Reports - Excelファイルを利用した帳票ツール
 *
 * $Id: XLSExporter.java 5 2009-06-22 07:55:44Z tomo-shibata $
 * $Revision: 5 $
 *
 * This file is part of ExCella Reports.
 *
 * ExCella Reports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * only, as published by the Free Software Foundation.
 *
 * ExCella Reports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License version 3 for more details
 * (a copy is included in the COPYING.LESSER file that accompanied this code).
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with ExCella Reports .  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>
 * for a copy of the LGPLv3 License.
 *
 ************************************************************************/
package org.bbreak.excella.reports.exporter;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.bbreak.excella.core.BookData;
import org.bbreak.excella.core.util.PoiUtil;
import org.bbreak.excella.reports.model.ConvertConfiguration;

/**
 * Excel出力エクスポーター
 * 
 * @since 1.0
 */
public class XLSExporter extends ReportBookExporter {

    /**
     * 変換タイプ：エクセル
     */
    public static final String FORMAT_TYPE = "XLS";

    /**
     * 拡張子：2007
     */
    public static final String EXTENTION = ".xls";

    /*
     * (non-Javadoc)
     * 
     * @see org.poireports.exporter.ReportBookExporter#output(org.apache.poi.ss.usermodel.Workbook, org.excelparser.BookData, org.poireports.model.ConvertConfiguration)
     */
    @Override
    public void output( Workbook book, BookData bookdata, ConvertConfiguration configuration) {
        if ( !(book instanceof HSSFWorkbook)) {
            throw new IllegalArgumentException( "Workbook is not HSSFWorkbook.");
        }
        PoiUtil.writeBook(book,getOutputStream());
    }

    @Override
    public String getFormatType() {
        return FORMAT_TYPE;
    }

    @Override
    public String getExtention() {
        return EXTENTION;
    }

}
