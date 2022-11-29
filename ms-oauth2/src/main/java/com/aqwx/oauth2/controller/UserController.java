package com.aqwx.oauth2.controller;

import cn.hutool.core.bean.BeanUtil;
import com.aqwx.common.model.domain.ResultInfo;
import com.aqwx.common.model.domain.SignInidentity;
import com.aqwx.common.model.vo.SingInDinerInfo;
import com.aqwx.common.utils.ResultInfoUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
public class UserController {

    @Resource
    private HttpServletRequest request;
    @Resource
    private RedisTokenStore redisTokenStore;

    @GetMapping("user/me")
    public ResultInfo getCurrentUser(Authentication authentication){
        SignInidentity principal = (SignInidentity) authentication.getPrincipal();
        SingInDinerInfo dinerInfo = new SingInDinerInfo();
        BeanUtil.copyProperties(principal, dinerInfo);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), dinerInfo);
    }

    @GetMapping("user/logout")
    public ResultInfo logout(String access_token,String authorization){
        if (StringUtils.isBlank(access_token)){
            access_token = authorization;
        }
        if (StringUtils.isBlank(access_token)){
            return ResultInfoUtil.buildSuccess(request.getServletPath(), "退出成功！");
        }
        //清除token中的bearer
        if (access_token.toLowerCase().contains("bearer".toLowerCase())){
            access_token = access_token.toLowerCase().replace("bearer", "");
        }
        //清空redis
        OAuth2AccessToken oAuth2AccessToken = redisTokenStore.readAccessToken(access_token);
        if (oAuth2AccessToken != null){
            redisTokenStore.removeAccessToken(oAuth2AccessToken);
            OAuth2RefreshToken refreshToken = oAuth2AccessToken.getRefreshToken();
            redisTokenStore.removeRefreshToken(refreshToken);
        }

        return ResultInfoUtil.buildSuccess(request.getServletPath(), "退出成功!");
    }
}
