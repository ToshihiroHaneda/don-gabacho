package jp.co.ziro.report.controller.report;

import java.util.ArrayList;
import java.util.List;

import jp.co.ziro.report.dto.ParamDto;
import jp.co.ziro.report.dto.RepeatParamDto;
import jp.co.ziro.report.model.Param;
import jp.co.ziro.report.model.RepeatParam;
import jp.co.ziro.report.model.Template;
import jp.co.ziro.report.service.TemplateService;

import org.slim3.controller.Controller;
import org.slim3.controller.Navigation;
import org.slim3.datastore.InverseModelListRef;
import org.slim3.util.BeanUtil;

public class InputController extends Controller {

    @Override
    public Navigation run() throws Exception {

        String idBuf = requestScope("templateId");
        //指定された帳票を検索
        Template template = TemplateService.findById(Integer.valueOf(idBuf));
        
        InverseModelListRef<Param, Template> param = template.getParamListRef();
        InverseModelListRef<RepeatParam, Template> repeatParam = template.getRepeatParamListRef();

        //パラメータにより箱を作成する
        List<ParamDto> paramList = createParam(param);
        List<RepeatParamDto> repeatParamList = createRepeatParam(repeatParam);

        requestScope("template",template);
        requestScope("paramList",paramList);
        requestScope("repeatParamList",repeatParamList);

        return forward("input.jsp");
    }

    private List<RepeatParamDto> createRepeatParam(
        InverseModelListRef<RepeatParam, Template> repeatParam) {
        
        List<RepeatParamDto> repeatDtoList = new ArrayList<RepeatParamDto>();
        if ( repeatParam == null ) {
            return repeatDtoList;
        }

        List<RepeatParam> repeatList = repeatParam.getModelList();
        for (RepeatParam repeatObj : repeatList) {
            InverseModelListRef<Param, RepeatParam> repeatParamObj =
                        repeatObj.getParamListRef();
            RepeatParamDto repeatDto = new RepeatParamDto();
            repeatDto.setName(repeatObj.getName());
            repeatDto.setFormName("list" + repeatObj.getSeq());

            List<Param> paramList = repeatParamObj.getModelList();

            for (Param paramObj : paramList) {
                ParamDto dto = createDto(paramObj);
                String formName = 
                    String.valueOf(repeatObj.getSeq()) + "_" +
                    String.valueOf(paramObj.getSeq());
                dto.setFormName(formName);
                repeatDto.addParam(dto);
            }
            repeatDtoList.add(repeatDto);
        }
        return repeatDtoList;
    }

    private List<ParamDto> createParam(
            InverseModelListRef<Param, Template> param) {
        
        List<ParamDto> dtoList = new ArrayList<ParamDto>();
        if ( param == null ) {
            return dtoList;
        }
       
        List<Param> paramList = param.getModelList();
        for (Param paramObj : paramList) {
            ParamDto dto = createDto(paramObj);
            dto.setFormName(String.valueOf(paramObj.getSeq()));
            dtoList.add(dto);
        }
        return dtoList;
    }

    private ParamDto createDto(Param param) {
        ParamDto dto = new ParamDto();
        BeanUtil.copy(param, dto);
        return dto;
    }
}
