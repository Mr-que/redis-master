package com.aqwx.point.controller;

import com.aqwx.common.model.domain.ResultInfo;
import com.aqwx.common.model.vo.DinerPointsRankVO;
import com.aqwx.common.utils.ResultInfoUtil;
import com.aqwx.point.service.DinerPointsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("points")
public class DinerPointsController {

    @Resource
    private DinerPointsService dinerPointsService;
    @Resource
    private HttpServletRequest request;

    /**
     * 添加积分
     *
     * @param dinerId 食客ID
     * @param points  积分
     * @param types   类型 0=签到，1=关注好友，2=添加Feed，3=添加商户评论
     * @return
     */
    @PostMapping("save")
    public ResultInfo<Integer> addPoints(@RequestParam(required = false) Integer dinerId,
                                         @RequestParam(required = false) Integer points,
                                         @RequestParam(required = false) Integer types) {
        dinerPointsService.addPoints(dinerId, points, types);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), points);
    }

    /*查询前20的积分排行榜与登录用户排名*/
    @GetMapping("findPoint")
    public ResultInfo findDinerPointRank(String accessToken){
        List<DinerPointsRankVO> dinerPointRank = dinerPointsService.findDinerPointRank(accessToken);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), dinerPointRank);
    }

    @GetMapping("findPoints")
    public ResultInfo findDinerPointsRankFromRedis(String accessToken){
        List<DinerPointsRankVO> dinerPointRank = dinerPointsService.findDinerPointRankRedis(accessToken);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), dinerPointRank);
    }
}
