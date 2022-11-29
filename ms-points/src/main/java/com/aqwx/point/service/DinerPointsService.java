package com.aqwx.point.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.aqwx.common.constant.ApiConstant;
import com.aqwx.common.constant.RedisKeyConstant;
import com.aqwx.common.exception.ParameterException;
import com.aqwx.common.model.domain.ResultInfo;
import com.aqwx.common.model.entity.DinerPoints;
import com.aqwx.common.model.vo.DinerPointsRankVO;
import com.aqwx.common.model.vo.ShortDinerInfo;
import com.aqwx.common.model.vo.SingInDinerInfo;
import com.aqwx.common.utils.AssertUtil;
import com.aqwx.point.dao.DinerPointsDao;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;

@Service
@Transactional(rollbackFor = Exception.class)
public class DinerPointsService {

    @Resource
    private DinerPointsDao dinerPointsDao;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private RedisTemplate redisTemplate;
    @Value("${service.name.ms-oauth-service}")
    private String oauthServiceName;
    @Value("${service.name.ms-diner-service}")
    private String dinerServiceName;
    //排行榜 TOPN
    private static final int TOPN = 20;

    public void addPoints(Integer dinerId,Integer points,Integer types){
        AssertUtil.isTrue(dinerId == null || dinerId < 1, "食客不能为空");
        AssertUtil.isTrue(points == null || dinerId < 1, "积分不能为空");
        AssertUtil.isTrue(types == null , "积分类型不能为空");

        DinerPoints dinerPoints = new DinerPoints();
        dinerPoints.setFkDinerId(dinerId);
        dinerPoints.setPoints(points);
        dinerPoints.setTypes(types);
        dinerPointsDao.save(dinerPoints);

        //类型 一个key(user_point)对应一个value(3,4,5)
        redisTemplate.opsForZSet().incrementScore(
                RedisKeyConstant.diner_points.getKey(),dinerId,points);
    }

    /*查询排行榜前20与登录用户的排名 Mysql版*/
    public List<DinerPointsRankVO> findDinerPointRank(String accessToken) {
        SingInDinerInfo dinerInfo = loadSignInDinerInfo(accessToken);
        List<DinerPointsRankVO> topnList = dinerPointsDao.findTopN(TOPN);
        if (CollectionUtil.isEmpty(topnList)){
            return new ArrayList<>(1);
        }
        //判断用户是否在排行榜中
        Map<Integer,DinerPointsRankVO> resultMap = new LinkedHashMap<>(20);
        for (DinerPointsRankVO rankVO : topnList) {
            resultMap.put(rankVO.getId(), rankVO);
        }
        if (resultMap.containsKey(dinerInfo.getId())){
            DinerPointsRankVO loginVo = resultMap.get(dinerInfo.getId());
            loginVo.setIsMe(1);
            return Lists.newArrayList(resultMap.values());
        }
        //查询用户
        DinerPointsRankVO myRank = dinerPointsDao.findDinerRank(dinerInfo.getId());
        myRank.setIsMe(1);
        topnList.add(myRank);
        return topnList;
    }

    /*查询排行榜前20与登录用户的排名 Redis版*/
    public List<DinerPointsRankVO> findDinerPointRankRedis(String accessToken) {
        SingInDinerInfo dinerInfo = loadSignInDinerInfo(accessToken);
        //返回倒序前20
        Set<ZSetOperations.TypedTuple<Integer>> set = redisTemplate.opsForZSet().
                reverseRangeWithScores(RedisKeyConstant.diner_points.getKey(), 0, 19);
        if (CollectionUtil.isEmpty( set)){
            return new ArrayList<>(1);
        }

        List<Integer> rankDinerList = Lists.newArrayList();
        Map<Integer,DinerPointsRankVO> ranksMap = new LinkedHashMap<>();

        LongAdder adder = new LongAdder();
        adder.increment();
        for (ZSetOperations.TypedTuple<Integer> temp : set) {
            Integer dinerId = temp.getValue();
            int points = temp.getScore().intValue();
            rankDinerList.add(dinerId);

            DinerPointsRankVO vo = new DinerPointsRankVO();
            vo.setId(dinerId);
            vo.setRanks(adder.intValue());
            vo.setTotal(points);
            ranksMap.put(dinerId, vo);
            adder.increment();
        }
        //封装排行榜用户
        ResultInfo resultInfo = restTemplate.getForObject(dinerServiceName + "findByIds?ids={ids}",
                ResultInfo.class, StrUtil.join(",", rankDinerList));
        List<LinkedHashMap> dataMaps = (List<LinkedHashMap>) resultInfo.getData();
        for (LinkedHashMap dataMap : dataMaps) {
            ShortDinerInfo shutInfo = BeanUtil.fillBeanWithMap(dataMap, new ShortDinerInfo(), false);
            DinerPointsRankVO rankVo = ranksMap.get(shutInfo.getId());
            rankVo.setNickname(shutInfo.getNickname());
            rankVo.setAvatarUrl(shutInfo.getAvatarUrl());
        }

        if (ranksMap.containsKey(dinerInfo.getId())){
            DinerPointsRankVO userVo = ranksMap.get(dinerInfo.getId());
            userVo.setIsMe(1);
            return Lists.newArrayList(ranksMap.values());
        }

        Long myRank = redisTemplate.opsForZSet().reverseRank(
                RedisKeyConstant.diner_points.getKey(),
                dinerInfo.getId());
        if (myRank != null){
            DinerPointsRankVO myVo = new DinerPointsRankVO();
            BeanUtil.copyProperties(dinerInfo, myVo);
            myVo.setIsMe(1);
            myVo.setRanks(myRank.intValue() + 1);
            Double score = redisTemplate.opsForZSet().score(RedisKeyConstant.diner_points.getKey(), dinerInfo.getId());
            myVo.setTotal(score.intValue());
            ranksMap.put(dinerInfo.getId(),myVo);
        }
        return Lists.newArrayList(ranksMap.values());
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
