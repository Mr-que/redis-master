package com.aqwx.restaurant.controller;

import com.aqwx.common.model.domain.ResultInfo;
import com.aqwx.common.model.entity.Restaurant;
import com.aqwx.common.model.vo.ReviewsVO;
import com.aqwx.common.utils.ResultInfoUtil;
import com.aqwx.restaurant.service.RestaurantService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("restaurant")
public class RestaurantController {

    @Resource
    private RestaurantService restaurantService;
    @Resource
    private HttpServletRequest request;

    @GetMapping("search/{restaurantId}")
    public ResultInfo findRestaurantById(@PathVariable Integer restaurantId){
        Restaurant restaurant = restaurantService.findRestaurantById(restaurantId);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), restaurant);
    }

    /**
     * 添加餐厅评论
     * @param restaurantId
     * @param content
     * @param likeIt
     * @param access_token
     * @return
     */
    @PostMapping("addReview/{restaurantId}")
    public ResultInfo addReview(@PathVariable Integer restaurantId,
                                @RequestParam("content") String content,
                                @RequestParam("likeIt") Integer likeIt,
                                String access_token){
        restaurantService.addReview(restaurantId, access_token, content, likeIt);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), "评论成功！");
    }

    /**
     * 查看最新的十条评论
     * @param restaurantId
     * @param access_token
     * @return
     */
    @GetMapping("{restaurantId}/news")
    public ResultInfo findNewReviews(@PathVariable Integer restaurantId,String access_token){
        List<ReviewsVO> reviewsVOList = restaurantService.findNewReviews(restaurantId, access_token);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), reviewsVOList);
    }
}
