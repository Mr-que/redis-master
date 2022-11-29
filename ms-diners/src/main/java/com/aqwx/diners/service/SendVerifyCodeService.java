package com.aqwx.diners.service;

import cn.hutool.core.util.RandomUtil;
import com.aqwx.common.constant.RedisKeyConstant;
import com.aqwx.common.utils.AssertUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class SendVerifyCodeService {

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    //发送验证码
    public String send(String phone) {
        AssertUtil.isNotEmpty(phone, "手机号不能为空!");
        String key = RedisKeyConstant.verify_code.getKey() + phone;
        //判断验证码是否过期
        if (checkCode(key,phone)){
            String code = RandomUtil.randomNumbers(6);
            redisTemplate.opsForValue().set(key, code,60, TimeUnit.SECONDS);
            return code;
        }
        return redisTemplate.opsForValue().get(key);
    }

    private boolean checkCode(String key, String phone) {
        Long expire = redisTemplate.getExpire(key); //获取key的过期时间
        return expire.equals(-2l) ? true : false;
    }

    public String getCodeByPhone(String phone) {
        String key = RedisKeyConstant.verify_code.getKey() + phone;
        return redisTemplate.opsForValue().get(key);
    }
}
