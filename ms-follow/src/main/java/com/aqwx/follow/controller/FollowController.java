package com.aqwx.follow.controller;

import com.aqwx.common.constant.ApiConstant;
import com.aqwx.common.model.domain.ResultInfo;
import com.aqwx.common.utils.ResultInfoUtil;
import com.aqwx.follow.service.FollowService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("follow")
public class FollowController {

    @Resource
    private FollowService followService;
    @Resource
    private HttpServletRequest request;

    /**
     * 关注 & 取关
     * @param followDinerId
     * @param isFollowed
     * @param access_token
     * @return
     */
    @PostMapping("/{followDinerId}")
    public ResultInfo follow(@PathVariable Integer followDinerId,
                             @RequestParam Integer isFollowed,
                             String access_token){
        return followService.follow(followDinerId, isFollowed, access_token, request.getServletPath());
    }

    /*获取共同关注*/
    @GetMapping("commons/{dinerId}")
    public ResultInfo findCommonFriends(@PathVariable Integer dinerId,String access_token){
        return followService.findCommonsFriends(dinerId,access_token,request.getServletPath());
    }

    @GetMapping("followers/{dinerId}")
    public ResultInfo findFollowers(@PathVariable("dinerId") Integer dinerId){
        return ResultInfoUtil.build(ApiConstant.SUCCESS_CODE,"粉丝列表获取成功",
                request.getServletPath(),followService.findFollowers(dinerId));
    }
}
