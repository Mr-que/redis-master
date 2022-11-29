package com.aqwx.point.handler;


import com.aqwx.common.exception.ParameterException;
import com.aqwx.common.model.domain.ResultInfo;
import com.aqwx.common.utils.ResultInfoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestControllerAdvice//将输出的内容写入 ResponseBody中
@Slf4j
public class GlobalExceptionHandler {

    @Autowired
    private HttpServletRequest request;

    @ExceptionHandler(ParameterException.class)
    public ResultInfo<Map<String,String>> handlerParameterException(ParameterException ex){

        String path = request.getRequestURI();
        ResultInfo<Map<String,String>> resultInfo =
                ResultInfoUtil.buildError(ex.getErrorCode(),ex.getMessage(),path);
        return resultInfo;

    }

    @ExceptionHandler(Exception.class)
    public ResultInfo<Map<String,String>> handLerException(Exception ex) {
        log.info("未知异常: {}",ex);
        String path = request.getRequestURI() ;
        ResultInfo<Map<String,String>> resultInfo = ResultInfoUtil.buildError(path) ;
        return resultInfo;
    }




}
