package com.aqwx.diners.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.aqwx.common.constant.ApiConstant;
import com.aqwx.common.constant.RedisKeyConstant;
import com.aqwx.common.exception.ParameterException;
import com.aqwx.common.model.domain.ResultInfo;
import com.aqwx.common.model.vo.NearMeDinerVO;
import com.aqwx.common.model.vo.ShortDinerInfo;
import com.aqwx.common.model.vo.SingInDinerInfo;
import com.aqwx.common.utils.AssertUtil;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NearMeService {

    @Value("${service.name.ms-oauth-service}")
    private String oauthServiceName;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private DinersService dinersService;

    //更新用户坐标
    public void updateDinerLocation(String accessToken,Float lon,Float lat){
        AssertUtil.isTrue(lon == null, "获取经度失败！");
        AssertUtil.isTrue(lat == null, "获取纬度失败！");

        SingInDinerInfo dinerInfo = loadSignInDinerInfo(accessToken);
        if (dinerInfo == null){
            throw new ParameterException(ApiConstant.NO_LOGIN_CODE,ApiConstant.NO_LOGIN_MESSAGE);
        }
        Point point = new Point(lon.doubleValue(),lat.doubleValue());
        // redis GEO类型
        redisTemplate.opsForGeo().add(RedisKeyConstant.diner_location.getKey(), point,dinerInfo.getId());
    }

    public List<NearMeDinerVO> findNearMe(String accessToken,Integer radius,Float lon,Float lat){
        SingInDinerInfo dinerInfo = loadSignInDinerInfo(accessToken);
        if (dinerInfo == null){
            throw new ParameterException(ApiConstant.NO_LOGIN_CODE,ApiConstant.NO_LOGIN_MESSAGE);
        }
        Integer id = dinerInfo.getId();
        String key = RedisKeyConstant.diner_location.getKey();

        if (radius == null){
            radius = 1000;
        }
        Point point = null;
        if (lon == null || lat == null){
            List<Point> position = redisTemplate.opsForGeo().position(key, id);
            AssertUtil.isTrue(CollectionUtil.isEmpty(position), "暂无位置信息");
            point = position.get(0);
        }else {
            point = new Point(lon,lat);
        }
        Distance distance = new Distance(radius, RedisGeoCommands.DistanceUnit.METERS);
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs();
        //限制返回20人 由远到近排序
        args.limit(20).includeDistance().sortAscending();
        Circle circle = new Circle(point, distance);
        GeoResults<RedisGeoCommands.GeoLocation> geoLocation = redisTemplate.opsForGeo().radius(key, circle, args);

        Map<Integer,NearMeDinerVO> nearMap = new LinkedHashMap<>();
        geoLocation.forEach(geo -> {
            RedisGeoCommands.GeoLocation<Integer> content = geo.getContent();
            NearMeDinerVO vo = new NearMeDinerVO();
            vo.setId(content.getName());
            double value = geo.getDistance().getValue();//经纬度
            String distanceStr = NumberUtil.round(value, 1).toString()+"m";
            vo.setDistance(distanceStr);
            nearMap.put(content.getName(), vo);
        });

        Integer[] dinerIds = nearMap.keySet().toArray(new Integer[]{});
        List<ShortDinerInfo> shortDinerInfos = dinersService.findByIds(StrUtil.join(",", dinerIds));
        shortDinerInfos.forEach( shortDinerInfo -> {
            NearMeDinerVO nearMeDinerVO = nearMap.get(shortDinerInfo.getId());
            nearMeDinerVO.setNickname(shortDinerInfo.getNickname());
            nearMeDinerVO.setAvatarUrl(shortDinerInfo.getAvatarUrl());
        });
        return Lists.newArrayList(nearMap.values());
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
