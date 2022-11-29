package com.aqwx.follow.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.aqwx.common.constant.ApiConstant;
import com.aqwx.common.constant.RedisKeyConstant;
import com.aqwx.common.exception.ParameterException;
import com.aqwx.common.model.domain.ResultInfo;
import com.aqwx.common.model.entity.Follow;
import com.aqwx.common.model.vo.ShortDinerInfo;
import com.aqwx.common.model.vo.SingInDinerInfo;
import com.aqwx.common.utils.AssertUtil;
import com.aqwx.common.utils.ResultInfoUtil;
import com.aqwx.follow.dao.FollowDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/*关注功能Service*/
@Service
public class FollowService {

    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private FollowDao followDao;
    @Value("${service.name.ms-oauth-service}")
    private String oauthServiceName;
    @Value("${service.name.ms-diner-service}")
    private String dinerServiceName;
    @Value("${service.name.ms-feed-service}")
    private String feedServiceName;

    /**
     * 关注&取关
     * @param followDinerId 用户id
     * @param isFollowed 1关注 0取关
     * @param accessToken 用户token
     * @param path 访问地址
     * @return
     */
    public ResultInfo follow(Integer followDinerId,Integer isFollowed,String accessToken,String path){
        //判断关注对象状态
        AssertUtil.isTrue(followDinerId == null && followDinerId < 1, "请选择需要关注的人");
        //获取用户登录信息
        SingInDinerInfo dinerInfo = loadSignInDinerInfo(accessToken);
        //获取登录用户和需要操作用户的关系
        Follow follow = followDao.selectFollow(dinerInfo.getId(), followDinerId);
        //从未关注过，现在第一次关注
        if (follow == null && isFollowed == 1){
            int save = followDao.save(dinerInfo.getId(), followDinerId);
            if (save == 1){
                addToRedisSet(dinerInfo.getId(),followDinerId);
                sendSaveOrRemoveFeed(followDinerId, accessToken, 1);
            }
            return ResultInfoUtil.build(ApiConstant.SUCCESS_CODE,"关注成功！",path,"关注成功！");
        }

        //关注过，现在重新关注
        if (follow != null && follow.getIsValid() == 0 && isFollowed == 1){
            int update = followDao.update(follow.getId(), isFollowed);
            if (update == 1){
                addToRedisSet(dinerInfo.getId(), followDinerId);
                sendSaveOrRemoveFeed(followDinerId, accessToken, 1);
            }
            return ResultInfoUtil.build(ApiConstant.SUCCESS_CODE,"关注成功！",path,"关注成功！");
        }

        //取关
        if (follow != null && follow.getIsValid() == 1 && isFollowed == 0){
            int update = followDao.update(follow.getId(), isFollowed);
            if (update == 1){
                removeFromRedisSet(dinerInfo.getId(),followDinerId);
                sendSaveOrRemoveFeed(followDinerId, accessToken, 0);
            }
            return ResultInfoUtil.build(ApiConstant.SUCCESS_CODE,"取关成功！",path,"取关成功！");
        }

        return ResultInfoUtil.buildSuccess(path,"关注ERROR!");
    }

    /*移除关注*/
    private void removeFromRedisSet(Integer id, Integer followDinerId) {

        String userkey =  RedisKeyConstant.following.getKey()+ id;
        String followKey =  RedisKeyConstant.followers.getKey()+ followDinerId;
        redisTemplate.opsForSet().remove(userkey,followDinerId);//用户关注集合
        redisTemplate.opsForSet().remove(followKey,id);//被关注者的粉丝集合

    }

    /*添加关注*/
    private void addToRedisSet(Integer id, Integer followDinerId) {

        String userKey =  RedisKeyConstant.following.getKey()+ id;
        String followKey =  RedisKeyConstant.followers.getKey()+ followDinerId;
        redisTemplate.opsForSet().add(userKey,followDinerId);//用户关注集合
        redisTemplate.opsForSet().add(followKey,id);//被关注者的粉丝集合

    }

    /*共同关注列表*/
    public ResultInfo findCommonsFriends(Integer dinersId,String accessToken,String path){

        /*是否选择了关注对象*/
        AssertUtil.isTrue(dinersId==null || dinersId<1,"请选择需要查看的人");

        /*获取登录用户信息*/
        SingInDinerInfo dinerInfo = loadSignInDinerInfo(accessToken);
        /*获取登录用户关注信息*/
        String loginDinerKey = RedisKeyConstant.following.getKey()+dinerInfo.getId();
        /*获取目标用户的关注信息*/
        String selectDinerKey = RedisKeyConstant.following.getKey()+dinersId;
        /*计算交集*/
        Set<Integer> interIds = redisTemplate.opsForSet().intersect(loginDinerKey, selectDinerKey);

        /*判断交集是否存在,不存在返回空List，存在返回用户信息*/
        if(CollectionUtil.isEmpty(interIds)){
            return ResultInfoUtil.buildSuccess(path,new ArrayList<ShortDinerInfo>(1));
        }
        ResultInfo resultInfo = restTemplate.getForObject(dinerServiceName + "findByIds?&ids={ids}",
                ResultInfo.class,
                StrUtil.join(",", interIds));

        if(resultInfo.getCode()!=ApiConstant.SUCCESS_CODE){
            resultInfo.setPath(path);
            return  resultInfo;
        }
        List<ShortDinerInfo> dinerList = (List<ShortDinerInfo>) resultInfo.getData();//转为List
        return  ResultInfoUtil.buildSuccess(path,dinerList);

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

    /*获取用户的粉丝列表*/
    public Set<Integer> findFollowers(Integer dinerId) {
        AssertUtil.isNotNull(dinerId, "选择要查看的用户");
        Set<Integer> ids = redisTemplate.opsForSet().members(RedisKeyConstant.followers.getKey() + dinerId);
        return ids;
    }

    /*用户关注or取关更新feed*/
    private void sendSaveOrRemoveFeed(Integer followDinerId,String accessToken,Integer type){
        String feedsUpdateUrl = feedServiceName + "feeds/updateFollowingFeeds/"
                + followDinerId + "?access_token=" + accessToken;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String,Object> body = new LinkedMultiValueMap<>();
        body.add("type", type);
        HttpEntity<MultiValueMap<String,Object>> entity = new HttpEntity<>(body,headers);
        restTemplate.postForEntity(feedsUpdateUrl, entity, ResultInfo.class);
    }


}
