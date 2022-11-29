package com.aqwx.diners.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.aqwx.common.constant.ApiConstant;
import com.aqwx.common.constant.PointTypesConstant;
import com.aqwx.common.exception.ParameterException;
import com.aqwx.common.model.domain.ResultInfo;
import com.aqwx.common.model.vo.SingInDinerInfo;
import com.aqwx.common.utils.AssertUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SignService {

    @Value("${service.name.ms-oauth-service}")
    private String oauthServiceName;
    @Value("${service.name.ms-points-service}")
    private String pointsServiceName;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RestTemplate restTemplate;

    public int doSign(String accessToken,String dateStr){
        SingInDinerInfo dinerInfo = loadSignInDinerInfo(accessToken);
        Date date = getDate(dateStr);
        int offSet = DateUtil.dayOfMonth(date) - 1;//位置索引
        String key = buildSignKey(dinerInfo.getId(),date);
        Boolean isSign = redisTemplate.opsForValue().getBit(key, offSet);//获取指定位置的值
        AssertUtil.isTrue(isSign, "请不要重复签到！");
        redisTemplate.opsForValue().setBit(key, offSet, true);
        int count = getSignCount(dinerInfo.getId(),date);
        return addPoints(count,dinerInfo.getId());
    }

    /**
     * 添加用户积分
     * @param count 连续签到次数
     * @param id    登录用户id
     * @return      获取的积分
     */
    private int addPoints(int count, Integer id) {
        int points = 10;//默认积分 1->10 2->20 3->30 >=4 ->50
        if (count == 2){
            points = 20;
        }else if (count == 3){
            points = 30;
        }else if (count >= 4){
            points = 50;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String,Object> body = new LinkedMultiValueMap<>();
        body.add("dinerId", id);
        body.add("points",points);
        body.add("types", PointTypesConstant.sign.getType());
        HttpEntity<MultiValueMap<String,Object>> entity = new HttpEntity<>(body,headers);

        ResponseEntity<ResultInfo> result = restTemplate.postForEntity(pointsServiceName + "/points/save", entity, ResultInfo.class);
        AssertUtil.isTrue(result.getStatusCode() != HttpStatus.OK,"签到失败");
        ResultInfo resultInfo = result.getBody();
        if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE){
            throw new ParameterException(resultInfo.getCode(),resultInfo.getMessage());
        }
        return points;
    }

    /*统计连续签到的次数*/
    private int getSignCount(Integer dinerId,Date date){
        /*获取当前日期的天数*/
        int dayOfMonth = DateUtil.dayOfMonth(date);
        String signKey = buildSignKey(dinerId, date);
        /*相当于 bitfield key get u当前天数 0 */
        BitFieldSubCommands bitFieldSubCommands = BitFieldSubCommands.create()
                .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth))
                .valueAt(0);
        List<Long> list = redisTemplate.opsForValue().bitField(signKey, bitFieldSubCommands);
        if (CollectionUtil.isEmpty(list)){
            return 0;
        }
        int signCount = 0;
        /*将二进制数转为10101010的源数据*/
        long v = list.get(0) == null ? 0 : list.get(0);
        for (int i = dayOfMonth; i>0; i--){
            //数据右移再左移与当前数据一致，说明最后一位为0，未签到
            if (v >> 1 << 1 == v){
                if (i != dayOfMonth)
                    break;
            }else {
                signCount++;
            }
            v >>= 1;//右移一位，相当于缩短一天
        }
        return signCount;
    }

    //根据字符串获取时间
    private Date getDate(String dateStr){
        if (StrUtil.isBlank(dateStr)){
            return new Date();
        }
        try {
            return DateUtil.parseDate(dateStr);
        }catch (Exception e){
            throw new RuntimeException("请传入yyyy-MM-dd格式的日期");
        }
    }
    //根据用户id和时间转换为key
    private String buildSignKey(Integer dinerId,Date date){
        return String.format("user:sign:%d:%s", dinerId,DateUtil.format(date,"yyyyMM"));
    }

    /*统计用户签到次数*/
    public Long getUserSignCount(String accessToken,String dateStr){
        SingInDinerInfo dinerInfo = loadSignInDinerInfo(accessToken);
        Date date = getDate(dateStr);
        String key = buildSignKey(dinerInfo.getId(), date);
        //获取指定范围值为1的个数
        Long sums = (Long) redisTemplate.execute((RedisCallback<Long>) d -> d.bitCount(key.getBytes()));
        return sums;
    }

    /*统计用户签到详情*/
    public Map<String,Boolean> getSignInfo(String accessToken,String dateStr){
        SingInDinerInfo dinerInfo = loadSignInDinerInfo(accessToken);
        Date date = getDate(dateStr);
        String key = buildSignKey(dinerInfo.getId(), date);
        Map<String, Boolean> signMap = new TreeMap<>();

        //获取月的天数，索引从0开始，需要+1
        int dayOfMouth = DateUtil.lengthOfMonth(
                DateUtil.month(date) + 1,
                DateUtil.isLeapYear(DateUtil.dayOfYear(date)));

        BitFieldSubCommands bitFieldSubCommands = BitFieldSubCommands.create()
                .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMouth))
                .valueAt(0);
        List<Long> list = redisTemplate.opsForValue().bitField(key, bitFieldSubCommands);
        if (CollectionUtil.isEmpty(list)){
            return signMap;
        }

        long v = list.get(0) == null ? 0 : list.get(0);
        for (int i = dayOfMouth;i>0;i--){
            //当天日期
            LocalDateTime localDateTime = LocalDateTimeUtil.of(date).withDayOfMonth(dayOfMouth);
            boolean flag = v >> 1 << 1 != v;
            signMap.put(DateUtil.format(localDateTime, "yyyy-MM-dd"), flag);
            v >>= 1;
        }
        return signMap;
    }


    /*获取登录信息*/
    private SingInDinerInfo loadSignInDinerInfo(String accessToken) {

        /*判断Token是否为空*/
        AssertUtil.mustLogin(accessToken);
        /*获取登录信息*/
        String url = oauthServiceName+"user/me?access_token={accessToken}";
        ResultInfo resultInfo = restTemplate.getForObject(url, ResultInfo.class, accessToken);
        if(resultInfo.getCode()!= ApiConstant.SUCCESS_CODE){
            throw new ParameterException(resultInfo.getMessage());
        }
        SingInDinerInfo dinerInfo = BeanUtil.fillBeanWithMap((LinkedHashMap)resultInfo.getData(),new SingInDinerInfo(),false);
        return dinerInfo;

    }

}
