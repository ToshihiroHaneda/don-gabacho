package jp.co.ziro.report.dto;

import java.util.ArrayList;
import java.util.List;

public class RepeatParamDto {

    /**
     * リスト名
     */
    private String name;
    /**
     * フォームの名前(リスト用に頭につける奴)
     */
    private String formName;

    /**
     * パラメータリスト
     */
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
