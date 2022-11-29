package com.aqwx.diners.controller;

import com.aqwx.common.model.domain.ResultInfo;
import com.aqwx.common.utils.ResultInfoUtil;
import com.aqwx.diners.service.SendVerifyCodeService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
public class SendVerifyCodeController {

    @Resource
    private SendVerifyCodeService sendVerifyCodeService;
    @Resource
    private HttpServletRequest request;

    @RequestMapping("send")
    public ResultInfo send(String phone){
        String sendCode = sendVerifyCodeService.send(phone);
        return ResultInfoUtil.buildSuccess(request.getServletPath(),"验证码:"+sendCode);
    }
}
