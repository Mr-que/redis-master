package com.aqwx.restaurant.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.aqwx.common.constant.ApiConstant;
import com.aqwx.common.constant.RedisKeyConstant;
import com.aqwx.common.exception.ParameterException;
import com.aqwx.common.model.domain.ResultInfo;
import com.aqwx.common.model.entity.Restaurant;
import com.aqwx.common.model.entity.Reviews;
import com.aqwx.common.model.vo.ReviewsVO;
import com.aqwx.common.model.vo.ShortDinerInfo;
import com.aqwx.common.model.vo.SingInDinerInfo;
import com.aqwx.common.utils.AssertUtil;
import com.aqwx.restaurant.dao.RestaurantDao;
import com.aqwx.restaurant.dao.ReviewsDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    @Resource
    private RestaurantDao restaurantDao;
    @Resource
    private ReviewsDao reviewsDao;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RestTemplate restTemplate;
    @Value("${service.name.ms-oauth-service}")
    private String oauthServiceName;
    @Value("${service.name.ms-diner-service}")
    private String dinerServiceName;

    /**
     * 根据餐厅id返回餐厅详情
     */
    public Restaurant findRestaurantById(Integer restaurantId){
        AssertUtil.isTrue(restaurantId == null || restaurantId < 1, "请选择餐厅查看");
        String key = RedisKeyConstant.restaurants.getKey() + restaurantId;
        LinkedHashMap map = (LinkedHashMap) redisTemplate.opsForHash().entries(key);

        //redis中没有餐厅数据，查询MySQL
        Restaurant restaurant = null;
        if (map == null || map.isEmpty()){
            Restaurant currentR = restaurantDao.findById(restaurantId);
            redisTemplate.setEnableTransactionSupport(true);
            if (currentR != null){
                redisTemplate.opsForHash().putAll(key, BeanUtil.beanToMap(currentR));
            }else {
                //mysql中也没有数据，造一个null数据传入redis 防止缓存穿透
                Restaurant tempR= new Restaurant();
                redisTemplate.execute(new SessionCallback<Object>(){
                    @Override
                    public Object execute(RedisOperations redisOperations) throws DataAccessException {
                        redisOperations.multi();
                        redisOperations.opsForHash().putAll(key, BeanUtil.beanToMap(tempR));
                        redisOperations.expire(key, 60, TimeUnit.SECONDS);
                        return redisOperations.exec();
                    }
                });
            }
        }else {
            restaurant = BeanUtil.fillBeanWithMap(map, new Restaurant(), false);
        }
        return restaurant;
    }

    public void addReview(Integer restaurantId,String accessToken,String content,Integer likeIt){
        AssertUtil.isTrue(restaurantId == null || restaurantId < 1, "请选择评论的餐厅");
        AssertUtil.isNotEmpty(content, "请输入评论内容");
        AssertUtil.isTrue(content.length() > 255, "内容过长！");
        Restaurant restaurant = findRestaurantById(restaurantId);
        AssertUtil.isTrue(restaurant == null, "该餐厅不存在");

        SingInDinerInfo dinerInfo = loadSignInDinerInfo(accessToken);
        Reviews reviews = new Reviews();
        reviews.setFkRestaurantId(restaurantId);
        reviews.setFkDinerId(dinerInfo.getId());
        reviews.setContent(content);
        reviews.setLikeIt(likeIt);
        int count = reviewsDao.saveReviews(reviews);
        if (count == 0){
            return;
        }

        String num = RedisKeyConstant.restaurants.getKey() + restaurantId;
        String key = RedisKeyConstant.restaurant_new_reviews.getKey() + restaurantId;

        redisTemplate.execute(new SessionCallback<Object>(){
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                redisOperations.multi();
                if (likeIt == 1){
                    redisOperations.opsForHash().increment(num, "likeVotes", 1);
                }else {
                    redisOperations.opsForHash().increment(num, "dislikeVotes",1);
                }
                redisOperations.opsForList().leftPush(key, reviews);
                return redisOperations.exec();
            }
        });
    }

    public List<ReviewsVO> findNewReviews(Integer restaurantId,String accessToken){
        AssertUtil.isTrue(restaurantId == null || restaurantId < 1,"请选择要查看的餐厅");
        String key = RedisKeyConstant.restaurant_new_reviews.getKey() + restaurantId;
        List<Reviews> rangeList = redisTemplate.opsForList().range(key, 0, 9);

        List<ReviewsVO> reviewsVOS = new ArrayList<>();
        List<Integer> dinerIds = new ArrayList<>();
        //封装上两个集合
        rangeList.forEach(review -> {
            ReviewsVO vo = new ReviewsVO();
            BeanUtil.copyProperties(review, vo);
            reviewsVOS.add(vo);
            dinerIds.add(review.getFkDinerId());
        });

        ResultInfo resultInfo = restTemplate.getForObject(dinerServiceName + "findByIds?ids={ids}", ResultInfo.class,
                StrUtil.join(",", dinerIds));
        if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE){
            throw new ParameterException(resultInfo.getCode(),resultInfo.getMessage());
        }
        List<LinkedHashMap> dinerInfoMaps = (List<LinkedHashMap>) resultInfo.getData();
        Map<Integer, ShortDinerInfo> dinerInfos = dinerInfoMaps.stream().collect(Collectors.toMap(
                diner -> (int) diner.get("id"),
                diner -> BeanUtil.fillBeanWithMap(diner, new ShortDinerInfo(), false)));

        reviewsVOS.forEach(reviewsVO -> {
            ShortDinerInfo dinerInfo = dinerInfos.get(reviewsVO.getFkDinerId());
            if (dinerIds != null){
                reviewsVO.setDinerInfo(dinerInfo);
            }
        });
        return reviewsVOS;
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


