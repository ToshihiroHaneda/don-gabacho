package jp.co.ziro.report.service;

import java.util.List;

import org.slim3.datastore.Datastore;

import jp.co.ziro.report.meta.TemplateMeta;
import jp.co.ziro.report.model.Template;

public class TemplateService {
    
    private static TemplateMeta meta = new TemplateMeta();
    
    public static List<Template> findTemplateList() {
        return Datastore.query(meta).asList();
    }
}
