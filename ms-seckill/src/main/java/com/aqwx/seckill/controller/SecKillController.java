package com.aqwx.seckill.controller;

import com.aqwx.common.model.domain.ResultInfo;
import com.aqwx.common.model.entity.SeckillVouchers;
import com.aqwx.common.utils.ResultInfoUtil;
import com.aqwx.seckill.service.SeckillService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("seckill")
public class SecKillController {

    @Resource
    private SeckillService seckillService;
    @Resource
    private HttpServletRequest request;

    @PostMapping("add")
    public ResultInfo<String> addSecKillVouchers(@RequestBody SeckillVouchers seckillVouchers){
        seckillService.addSecKillVouchers(seckillVouchers);
        return ResultInfoUtil.buildSuccess(request.getServletPath(),"代金券活动添加成功！");
    }

    @RequestMapping("buy/{voucherId}")
    public ResultInfo<String> doSecKill(@PathVariable("voucherId") Integer voucherId,String access_token){
        ResultInfo resultInfo = seckillService.doSecKill(voucherId,access_token,request.getServletPath());
        return resultInfo;
    }
}
