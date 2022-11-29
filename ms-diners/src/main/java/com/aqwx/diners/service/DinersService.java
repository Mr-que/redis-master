package com.aqwx.diners.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.aqwx.common.constant.ApiConstant;
import com.aqwx.common.model.domain.ResultInfo;
import com.aqwx.common.model.dto.DinersDTO;
import com.aqwx.common.model.entity.Diners;
import com.aqwx.common.model.vo.ShortDinerInfo;
import com.aqwx.common.utils.AssertUtil;
import com.aqwx.common.utils.ResultInfoUtil;
import com.aqwx.diners.config.OAuth2ClientConfiguration;
import com.aqwx.diners.dao.DinersDao;
import com.aqwx.diners.entity.OAuthDinerInfo;
import com.aqwx.diners.entity.vo.LoginDinerInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class DinersService {

    @Resource
    private RestTemplate restTemplate;
    @Resource
    private OAuth2ClientConfiguration oAuth2ClientConfiguration;
    @Resource
    private DinersDao dinersDao;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private SendVerifyCodeService sendVerifyCodeService;

    @Value("${service.name.ms-oauth-service}")
    private String oauthServiceName;

    /*根据 ids集合查询用户信息*/
    public List<ShortDinerInfo> findByIds(String ids) {
        AssertUtil.isNotEmpty(ids);
        String[] idArr = ids.split(",");
        List<ShortDinerInfo> list = dinersDao.findByIds(idArr);
        return list;
    }

    /* sign in */
    public ResultInfo signIn(String account,String password,String path){
        //参数校验
        AssertUtil.isNotEmpty(account, "请输入登录账号");
        AssertUtil.isNotEmpty(password, "请输入登录密码");
        //构建请求头参数
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //构建请求体参数
        MultiValueMap<String,Object> body = new LinkedMultiValueMap<>();
        body.add("username", account);
        body.add("password", password);
        body.setAll(BeanUtil.beanToMap(oAuth2ClientConfiguration));
        HttpEntity<MultiValueMap<String,Object>> entity = new HttpEntity<>(body,headers);
        //设置auth (id,secret)
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(
                oAuth2ClientConfiguration.getClientId(), oAuth2ClientConfiguration.getSecret()));
        //通过restTemplate调用OAuthController
        ResponseEntity<ResultInfo> result = restTemplate.postForEntity(oauthServiceName+"oauth/token", entity, ResultInfo.class);
        AssertUtil.isTrue(result.getStatusCode()!= HttpStatus.OK,"登录失败");
        ResultInfo resultInfo = result.getBody();
        if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE){
            resultInfo.setData(resultInfo.getMessage());
            return resultInfo;
        }
        //data是一个LinkedHashMap 需要转为OAuthDinerInfo
        OAuthDinerInfo oAuthDinerInfo = BeanUtil.fillBeanWithMap((LinkedHashMap) resultInfo.getData(), new OAuthDinerInfo(), false);
        LoginDinerInfo loginDinerInfo = new LoginDinerInfo();
        loginDinerInfo.setNickname(oAuthDinerInfo.getNickname());
        loginDinerInfo.setAvatarUrl(oAuthDinerInfo.getAvatarUrl());
        loginDinerInfo.setToken(oAuthDinerInfo.getAccessToken());
        return ResultInfoUtil.buildSuccess(path, loginDinerInfo);
    }

    /* 判断手机号是否已经注册 */
    public void checkPhoneIsRegister(String phone) {
        AssertUtil.isNotEmpty(phone, "手机号不能为空!");
        Diners diners = dinersDao.selectByPhone(phone);
        AssertUtil.isTrue(diners == null, "该手机号为注册");
        AssertUtil.isTrue(diners.getIsValid() == 0, "该手机号已经被锁定!");
    }

    /* 注册 */
    public ResultInfo register(DinersDTO dinersDTO,String path) {
        AssertUtil.isNotEmpty(dinersDTO.getUsername(), "请输入用户名");
        AssertUtil.isNotEmpty(dinersDTO.getPassword(), "请输入密码");
        AssertUtil.isNotEmpty(dinersDTO.getPhone(), "请输入手机号");
        AssertUtil.isNotEmpty(dinersDTO.getVerifyCode(), "请输入验证码");
        //验证码判断
        String codeByPhone = sendVerifyCodeService.getCodeByPhone(dinersDTO.getPhone());
        AssertUtil.isNotEmpty(codeByPhone, "验证码已经过期，请重新获取");
        AssertUtil.isTrue(!codeByPhone.equals(dinersDTO.getVerifyCode()), "验证码不一致，请重新输入");
        //验证用户是否存在
        Diners diners = dinersDao.selectByUsername(dinersDTO.getUsername());
        AssertUtil.isTrue(diners != null, "用户名已经存在，请重新输入");
        String password = dinersDTO.getPassword();
        dinersDTO.setPassword(DigestUtil.md5Hex(password));
        dinersDao.saveDiners(dinersDTO);
        //自动登录
        ResultInfo resultInfo = signIn(dinersDTO.getUsername(), password, path);
        return resultInfo;

    }

}
