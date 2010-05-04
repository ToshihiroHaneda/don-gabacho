package jp.co.ziro.report.dto;

public class ParamDto {

    private String name;
    private String formName;
    private String type;
    private String attr;

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void setFormName(String formName) {
        this.formName = formName;
    }
    public String getFormName() {
        return formName;
    }
    public void setAttr(String attr) {
        this.attr = attr;
    }
    public String getAttr() {
        return attr;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }
}
