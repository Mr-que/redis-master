package com.aqwx.oauth2.config;

import com.aqwx.common.constant.ApiConstant;
import com.aqwx.common.utils.ResultInfoUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/*认证失败处理*/
@Component
public class MyAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Resource
    private ObjectMapper objectMapper;

    /*认证失败返回JSON信息与401状态码*/
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        response.setContentType ("application/json; charset=utf-8");
        response.setStatus (HttpServletResponse.SC_UNAUTHORIZED);
        PrintWriter out = response.getWriter();
        String errorMessage = authException.getMessage();
        if (StringUtils.isBlank(errorMessage)) {
            errorMessage = "登录失效!";
        }
        ResultInfoUtil.buildError(ApiConstant.ERROR_CODE,errorMessage, request.getRequestURI());
        out.write(objectMapper.writeValueAsString(request));
        out.flush();
        out.close();
    }

}
