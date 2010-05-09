package jp.co.ziro.report.service;

import java.util.List;

import org.slim3.datastore.Datastore;
import org.slim3.datastore.InverseModelListRef;

import com.google.appengine.api.datastore.Key;

import jp.co.ziro.report.meta.TemplateMeta;
import jp.co.ziro.report.model.Report;
import jp.co.ziro.report.model.Template;

public class TemplateService {
    
    private static TemplateMeta meta = new TemplateMeta();
    
    public static List<Template> findTemplateList() {
        return Datastore.query(meta).asList();
    }

    public static Template findById(int keyId) {
        Key key = Datastore.createKey(meta, keyId);
        Template template = Datastore.query(meta).filter(meta.key.equal(key)).asSingle();
        return template;
    }

    public static List<Report> findReportList(int keyId) {
        Template template = findById(keyId);
        InverseModelListRef<Report, Template> reportList = template.getReportListRef();
        if ( reportList == null ) return null;
        return reportList.getModelList();
    }
}