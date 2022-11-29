package com.aqwx.diners.controller;

import com.aqwx.common.model.domain.ResultInfo;
import com.aqwx.common.model.vo.NearMeDinerVO;
import com.aqwx.common.utils.ResultInfoUtil;
import com.aqwx.diners.service.NearMeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("nearname")
public class NearMeController {

    @Resource
    private NearMeService nearMeService;
    @Resource
    private HttpServletRequest request;

    /*保存用户地理位置*/
    @RequestMapping("save")
    public ResultInfo updateDinerLocation(String accessToken, Float lon, Float lat){
        nearMeService.updateDinerLocation(accessToken, lon, lat);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), "地理位置保存成功");
    }

    @GetMapping("search")
    public ResultInfo nearMe(String accessToken,Integer radius,Float lon,Float lat){
        List<NearMeDinerVO> nearMe = nearMeService.findNearMe(accessToken, radius, lon, lat);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), nearMe);
    }
}
