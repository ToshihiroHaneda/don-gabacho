package jp.co.ziro.report.model;

import java.io.Serializable;

import com.google.appengine.api.datastore.Key;

import org.slim3.datastore.Attribute;
import org.slim3.datastore.InverseModelListRef;
import org.slim3.datastore.Model;
import org.slim3.datastore.ModelRef;

@Model(schemaVersion = 1)
public class Report implements Serializable {

    private static final long serialVersionUID = 1L;

    @Attribute(primaryKey = true)
    private Key key;

    @Attribute(version = true)
    private Long version;

    @Attribute
    private String name;
 
    @Attribute
    private String detail;

    @Attribute
    private ModelRef<Template> templateRef = 
                    new ModelRef<Template>(Template.class);
    
    @Attribute(persistent = false)
    private InverseModelListRef<ReportParam, Report> reportParamListRef = 
        new InverseModelListRef<ReportParam, Report>(ReportParam.class, "reportRef", this);

    @Attribute(persistent = false)
    private InverseModelListRef<ReportRepeatParam, Report> reportRepeatParamListRef =
        new InverseModelListRef<ReportRepeatParam, Report>(ReportRepeatParam.class, "reportRef", this);
    /**
     * Returns the key.
     *
     * @return the key
     */
    public Key getKey() {
        return key;
    }

    /**
     * Sets the key.
     *
     * @param key
     *            the key
     */
    public void setKey(Key key) {
        this.key = key;
    }

    /**
     * Returns the version.
     *
     * @return the version
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Sets the version.
     *
     * @param version
     *            the version
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Report other = (Report) obj;
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        return true;
    }

    public InverseModelListRef<ReportParam, Report> getReportParamListRef() {
        return reportParamListRef;
    }

    public InverseModelListRef<ReportRepeatParam, Report> getReportRepeatParamListRef() {
        return reportRepeatParamListRef;
    }

    public ModelRef<Template> getTemplateRef() {
        return templateRef;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getDetail() {
        return detail;
    }


}
