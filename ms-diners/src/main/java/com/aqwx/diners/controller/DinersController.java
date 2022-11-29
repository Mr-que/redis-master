package com.aqwx.diners.controller;

import com.aqwx.common.model.domain.ResultInfo;
import com.aqwx.common.model.dto.DinersDTO;
import com.aqwx.common.model.vo.ShortDinerInfo;
import com.aqwx.common.utils.ResultInfoUtil;
import com.aqwx.diners.service.DinersService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@Api(tags = "用户相关接口")
public class DinersController {

    @Resource
    private DinersService dinersService;
    @Resource
    private HttpServletRequest request;

    @PostMapping("login")
    public ResultInfo signIn(String account,String password){
        return dinersService.signIn(account, password, request.getServletPath());
    }

    @PostMapping("checkPhone")
    public ResultInfo checkPhone(String phone){
        dinersService.checkPhoneIsRegister(phone);
        return ResultInfoUtil.buildSuccess(request.getServletPath());
    }

    @RequestMapping("register")
    public ResultInfo register(@RequestBody DinersDTO dinersDTO){
        return dinersService.register(dinersDTO,request.getServletPath());
    }

    /*根据用户ID集合查询用户信息*/
    @GetMapping("findByIds")
    public ResultInfo<List<ShortDinerInfo>> findByIds(String ids) {
        List<ShortDinerInfo> dinerInfos = dinersService.findByIds(ids);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), dinerInfos);
    }

}
