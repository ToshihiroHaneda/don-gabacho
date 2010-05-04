package jp.co.ziro.report.service;

import org.slim3.datastore.Datastore;

import com.google.appengine.api.datastore.Key;

import jp.co.ziro.report.meta.ReportMeta;
import jp.co.ziro.report.model.Report;

public class ReportService {
    
    private static ReportMeta meta = new ReportMeta();
    public static Report findById(Integer id) {
        Key key = Datastore.createKey(Report.class, id);
        return Datastore.query(meta).filter(meta.key.equal(key)).asSingle();
    }
}
