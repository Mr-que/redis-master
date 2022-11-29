package com.aqwx.diners.controller;

import com.aqwx.common.model.domain.ResultInfo;
import com.aqwx.common.utils.ResultInfoUtil;
import com.aqwx.diners.service.SignService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("sign")
public class SignController {

    @Resource
    private SignService signService;
    @Resource
    private HttpServletRequest request;

    @PostMapping("signing")
    public ResultInfo signInfo(@RequestParam(required = false) String dateStr, String access_token){
        int signCount = signService.doSign(access_token, dateStr);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), signCount);
    }

    /*返回用户签到次数*/
    @PostMapping("signCount")
    public ResultInfo getSignCount(String access_token,@RequestParam(required = false) String dateStr){
        Long signCount = signService.getUserSignCount(access_token, dateStr);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), signCount);
    }

    /*返回用户签到详情*/
    @PostMapping("signMap")
    public ResultInfo getSignMap(String access_token,@RequestParam(required = false) String dateStr){
        Map<String, Boolean> signInfo = signService.getSignInfo(access_token, dateStr);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), signInfo);
    }
}
