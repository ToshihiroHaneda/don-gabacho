package jp.co.ziro.report.dto;

import java.util.ArrayList;
import java.util.List;

public class RepeatParamDto {

    private String name;
    private String formName;

    private List<ParamDto> paramList = new ArrayList<ParamDto>();

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void addParam(ParamDto param) {
        this.paramList.add(param);
    }
    public List<ParamDto> getParamList() {
        return paramList;
    }
    public void setFormName(String formName) {
        this.formName = formName;
    }
    public String getFormName() {
        return formName;
    }
    
}
