package com.aqwx.common.utils;

import com.aqwx.common.constant.ApiConstant;
import com.aqwx.common.model.domain.ResultInfo;

/*公共返回对象工具类*/
public class ResultInfoUtil {

    /*请求出错返回*/
    public static <T> ResultInfo<T> buildError(String path){
        ResultInfo<T> resultInfo = build(ApiConstant.ERROR_CODE,ApiConstant.ERROR_MESSAGE,path,null) ;
        return resultInfo;
    }

    /*请求出错返回，指定错误码*/
    public static <T> ResultInfo<T> buildError(int errorCode,String message,String path){
        ResultInfo<T> resultInfo = build(errorCode,message,path,null) ;
        return resultInfo;
    }

    /*请求成功返回*/
    public static <T> ResultInfo<T> buildSuccess(String path){
        ResultInfo<T> resultInfo = build(ApiConstant.SUCCESS_CODE,ApiConstant.SUCCESS_MESSAGE,path,null) ;
        return resultInfo;
    }

    /*请求成功返回，指定成功码*/
    public static <T> ResultInfo<T> buildSuccess(String path,T data){
        ResultInfo<T> resultInfo = build(ApiConstant.SUCCESS_CODE,ApiConstant.SUCCESS_MESSAGE,path,data) ;
        return resultInfo;
    }


    /*构建返回对象方法*/
    public static <T> ResultInfo<T> build(Integer code,String message,String path,T data){

        if (code == null) {
            code = ApiConstant.SUCCESS_CODE;
        }
        if (message == null) {
            message = ApiConstant.SUCCESS_MESSAGE;
        }
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setCode(code);
        resultInfo.setMessage(message);
        resultInfo.setPath(path);
        resultInfo.setData(data);
        return resultInfo;
    }


}
