package jp.co.ziro.report.model;

import java.io.Serializable;

import com.google.appengine.api.datastore.Key;

import org.slim3.datastore.Attribute;
import org.slim3.datastore.Model;
import org.slim3.datastore.ModelRef;

@Model(schemaVersion = 1)
public class ReportParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @Attribute(primaryKey = true)
    private Key key;

    @Attribute(version = true)
    private Long version;

    @Attribute
    private String value;
    
    @Attribute
    private Integer rowNum;

    @Attribute
    private ModelRef<Param> paramRef = 
                    new ModelRef<Param>(Param.class);
    
    @Attribute
    private ModelRef<Report> reportRef = 
                    new ModelRef<Report>(Report.class);

    @Attribute
    private ModelRef<ReportRepeatParam> reportRepeatParamRef = 
                    new ModelRef<ReportRepeatParam>(ReportRepeatParam.class);
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
        ReportParam other = (ReportParam) obj;
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        return true;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public ModelRef<Report> getReportRef() {
        return reportRef;
    }

    public ModelRef<ReportRepeatParam> getReportRepeatParamRef() {
        return reportRepeatParamRef;
    }

    public ModelRef<Param> getParamRef() {
        return paramRef;
    }

    public void setRowNum(Integer rowNum) {
        this.rowNum = rowNum;
    }

    public Integer getRowNum() {
        return rowNum;
    }
}
