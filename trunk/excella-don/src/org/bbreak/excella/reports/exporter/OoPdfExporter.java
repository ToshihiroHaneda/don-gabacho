/*************************************************************************
 *
 * Copyright 2009 by bBreak Systems.
 *
 * ExCella Reports 
 *
 * $Id: OoPdfExporter.java 97 2010-01-13 02:11:36Z tomo-shibata $
 * $Revision: 97 $
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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.document.DefaultDocumentFormatRegistry;
import org.artofsolving.jodconverter.document.DocumentFamily;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.document.DocumentFormatRegistry;
import org.artofsolving.jodconverter.document.SimpleDocumentFormatRegistry;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.bbreak.excella.core.BookData;
import org.bbreak.excella.reports.model.ConvertConfiguration;

public abstract class OoPdfExporter extends ReportBookExporter {
    private static Log log = LogFactory.getLog(OoPdfExporter.class);
    public static final String FORMAT_TYPE = "PDF";
    public static final String EXTENTION = ".pdf";
    public static final int OPENOFFICE_DEFAULT_PORT = 8100;
    private int port = OPENOFFICE_DEFAULT_PORT;
    private OfficeManager officeManager = null;
    private boolean controlOfficeManager = false;
    public OoPdfExporter() {
    }
    public OoPdfExporter(int port) {
        this.port = port;
    }
    public OoPdfExporter( OfficeManager officeManager) {
        this.officeManager = officeManager;
        controlOfficeManager = true;
    }
    @Override
    public void output( Workbook book, BookData bookdata, ConvertConfiguration configuration) {
        if ( !controlOfficeManager) {
            officeManager = new DefaultOfficeManagerConfiguration().setPortNumber(port).buildOfficeManager();
            officeManager.start();
        }
        OfficeDocumentConverter converter = null;
        if ( configuration.getOptionsProperties().isEmpty()) {
            converter = new OfficeDocumentConverter( officeManager);
        } else {
            DocumentFormatRegistry registry = createDocumentFormatRegistry( configuration);
            converter = new OfficeDocumentConverter( officeManager, registry);
        }
        ExcelExporter excelExporter = new ExcelExporter();
        excelExporter.output( book, bookdata, null);
        converter.convert(null,null);
        if ( !controlOfficeManager) {
            officeManager.stop();
        }
    }
    private DocumentFormatRegistry createDocumentFormatRegistry( ConvertConfiguration configuration) {
        SimpleDocumentFormatRegistry registry = new DefaultDocumentFormatRegistry();
        if ( configuration == null || configuration.getOptionsProperties().isEmpty()) {
            return registry;
        }
        DocumentFormat documentFormat = registry.getFormatByExtension( "pdf");
        Map<String, Object> optionMap = new HashMap<String, Object>( documentFormat.getStoreProperties( DocumentFamily.SPREADSHEET));
        documentFormat.setStoreProperties( DocumentFamily.SPREADSHEET, optionMap);
        return registry;
    }
    @Override
    public String getFormatType() {
        return FORMAT_TYPE;
    }
}
