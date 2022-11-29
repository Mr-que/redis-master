package com.aqwx.feeds.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.aqwx.common.constant.ApiConstant;
import com.aqwx.common.constant.RedisKeyConstant;
import com.aqwx.common.exception.ParameterException;
import com.aqwx.common.model.domain.ResultInfo;
import com.aqwx.common.model.entity.Feeds;
import com.aqwx.common.model.vo.FeedsVO;
import com.aqwx.common.model.vo.ShortDinerInfo;
import com.aqwx.common.model.vo.SingInDinerInfo;
import com.aqwx.common.utils.AssertUtil;
import com.aqwx.feeds.dao.FeedsDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeedsService {

    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private FeedsDao feedsDao;
    @Value("${service.name.ms-oauth-service}")
    private String oauthServiceName;
    @Value("${service.name.ms-follow-service}")
    private String followServiceName;
    @Value("${service.name.ms-diner-service}")
    private String dinerServiceName;

    //添加帖子，将消息推送给粉丝
    public void create(Feeds feeds,String access_token){
        AssertUtil.isNotEmpty(feeds.getContent(), "请输入内容！");
        AssertUtil.isTrue(feeds.getContent().length() > 255, "内容长度超出！");

        //将feed存Mysql
        SingInDinerInfo dinerInfo = loadSignInDinerInfo(access_token);
        feeds.setFkDinerId(dinerInfo.getId());
        int save = feedsDao.save(feeds);
        AssertUtil.isTrue(save == 0, "添加失败");

        //获取用户的所有粉丝
        List<Integer> followers = findFollowers(dinerInfo.getId());
        long now = System.currentTimeMillis();
        followers.forEach(u -> {
            //每个用户关注的feeds集合
            String key = RedisKeyConstant.following_feeds.getKey() + u;
            redisTemplate.opsForZSet().add(key, feeds.getId() ,now);
        });
    }

    //远程获取粉丝集合
    private List<Integer> findFollowers(Integer id) {
        String url = followServiceName + "follow/followers/" + id;
        ResultInfo resultInfo = restTemplate.getForObject(url, ResultInfo.class);
        if (resultInfo.getCode()!=ApiConstant.SUCCESS_CODE){
            throw new ParameterException(resultInfo.getCode(),resultInfo.getMessage());
        }
        List<Integer> fanData = (List<Integer>) resultInfo.getData();
        return fanData;
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

    public void deleteFeeds(Integer id, String access_token) {
        AssertUtil.isTrue(id==null || id<1,"请选择需要删除的Feed");

        //通过Token获取用户信息后判断是否已被删除&是否是自己的Feed
        SingInDinerInfo dinerInfo = loadSignInDinerInfo(access_token);
        Feeds feeds = feedsDao.findById(id,dinerInfo.getId());
        AssertUtil.isTrue(feeds==null,"该Feed已被删除");
        AssertUtil.isTrue(!feeds.getFkDinerId().equals(dinerInfo.getId()),"只能删除自己的Feed");

        /*删除Feed*/
        int delete = feedsDao.delete(id);
        if(delete!=0){
            //这里将粉丝（关注）用户的推送Feed数据删除,将redis中数据清除
            List<Integer> followers =  findFollowers(dinerInfo.getId());
            followers.forEach( a ->{
                String key =  RedisKeyConstant.following_feeds.getKey() + a;
                redisTemplate.opsForZSet().remove(key,feeds.getId());
            } );
        }
    }

    /**
     * 取关 & 关注时的Feed操作
     * @param followingDinerId 目标对象
     * @param accessToken      用户信息
     * @param type  0取关 1关注
     */
    public void addFollowingFeed(Integer followingDinerId,String accessToken,Integer type){
        AssertUtil.isTrue(followingDinerId == null || followingDinerId < 1, "请选择关注对象");
        SingInDinerInfo dinerInfo = loadSignInDinerInfo(accessToken);
        //获取关注对象的所有feed
        List<Feeds> feedsList = feedsDao.findByDinerId(followingDinerId);
        String key = RedisKeyConstant.following_feeds.getKey() + dinerInfo.getId();
        if (type == 0){
            if (CollectionUtil.isNotEmpty(feedsList)){
                feedsList.forEach(feed -> {
                    //删除我关注的某个用户的所有feed
                    redisTemplate.opsForZSet().remove(key, feed.getId());
                });
            }
        }else {
            if (CollectionUtil.isNotEmpty(feedsList)){
                Set<ZSetOperations.TypedTuple> feedsSet = feedsList.stream()
                        .map(feed -> new DefaultTypedTuple(feed.getId(), (double) feed.getUpdateTime().getTime()))
                        .collect(Collectors.toSet());
                feedsList.forEach( feed -> redisTemplate.opsForZSet().add(key, feedsSet));
            }
        }
    }

    /*根据时间排序，每次显示20条Feed*/
    public List<FeedsVO> selectForPage(Integer page, String accessToken){
        if (page == null) page = 1;
        SingInDinerInfo dinerInfo = loadSignInDinerInfo(accessToken);
        String key = RedisKeyConstant.following_feeds.getKey() + dinerInfo.getId();
        long start = (page - 1) * ApiConstant.PAGE_SIZE;
        long end = page * ApiConstant.PAGE_SIZE;
        //拿到用户关注的所有用户的feed前二十
        Set<Integer> feedSets = redisTemplate.opsForZSet().reverseRange(key, start, end);
        if (CollectionUtil.isEmpty(feedSets)){
            return new ArrayList<FeedsVO>(1);
        }
        //根据feed_id拿取所有feed
        List<Feeds> feedsList = feedsDao.findFeedsByIds(feedSets);
        List<String> dinerIdList = new ArrayList<>(8);

        //将feed转为feedVO
        List<FeedsVO> feedsVOList = feedsList.stream().map(feed -> {
            FeedsVO feedsVO = new FeedsVO();
            BeanUtil.copyProperties(feed, feedsVO);
            dinerIdList.add(feed.getFkDinerId().toString());
            return feedsVO;
        }).collect(Collectors.toList());
        //根据dinerId获取用户详情
        String idsArr = dinerIdList.stream().collect(Collectors.joining(","));
        ResultInfo resultInfo = restTemplate.getForObject(dinerServiceName + "findByIds?ids={id}", ResultInfo.class, idsArr);
        if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE){
            throw new ParameterException(resultInfo.getCode(),ApiConstant.ERROR_MESSAGE);
        }
        List<LinkedHashMap> dinerMap = (List<LinkedHashMap>) resultInfo.getData();
        Map<Integer, ShortDinerInfo> dinerInfos = dinerMap.stream().collect(Collectors.toMap(
                //key 为dinerId
                diner -> (Integer) diner.get("id"),
                //value 为ShortdinerInfo
                diner -> BeanUtil.fillBeanWithMap(diner, new ShortDinerInfo(), true)
        ));
        //将用户详情存入feedsVOList中
        feedsVOList.forEach( feedsVO -> {
            feedsVO.setDinerInfo(dinerInfos.get(feedsVO.getFkDinerId()));
        });
        return feedsVOList;
    }
}
