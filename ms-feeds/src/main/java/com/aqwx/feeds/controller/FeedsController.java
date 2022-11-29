package com.aqwx.feeds.controller;

import com.aqwx.common.model.domain.ResultInfo;
import com.aqwx.common.model.entity.Feeds;
import com.aqwx.common.model.vo.FeedsVO;
import com.aqwx.common.utils.ResultInfoUtil;
import com.aqwx.feeds.service.FeedsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Result;
import java.util.List;

@RestController
@RequestMapping("feeds")
public class FeedsController {

    @Resource
    private FeedsService feedsService;
    @Resource
    private HttpServletRequest request;

    @PostMapping("addFeeds")
    public ResultInfo create(@RequestBody Feeds feeds,String access_token){
        feedsService.create(feeds, access_token);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), "添加成功");
    }

    /*删除Feed*/
    @PostMapping("delFeeds")
    public ResultInfo delete(Integer id,String access_token){
        feedsService.deleteFeeds(id,access_token);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), "删除成功");
    }

    /*用户关注取关时feed的变更操作*/
    @PostMapping("updateFollowingFeeds/{followingDinerId}")
    public ResultInfo addFollowingFeeds(@PathVariable("followingDinerId") Integer followingDinerId, String access_token, Integer type){
        feedsService.addFollowingFeed(followingDinerId, access_token, type);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), "操作成功");
    }

    /*分页获取关注的feed数据*/
    @GetMapping("show/{page}")
    public ResultInfo selectForPage(@PathVariable Integer page,String access_token){
        List<FeedsVO> feedsVOS = feedsService.selectForPage(page, access_token);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), feedsVOS);
    }
}
