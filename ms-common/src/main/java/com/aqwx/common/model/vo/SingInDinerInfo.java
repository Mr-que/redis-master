package com.aqwx.common.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@ApiModel(description = "登录用户信息",value = "SingInDinerInfo")
public class SingInDinerInfo implements Serializable {

    @ApiModelProperty("主键")
    private Integer id;
    @ApiModelProperty("户名")
    private String username ;
    @ApiModelProperty ("昵称")
    private String nickname;
    @ApiModelProperty("手机号")
    private String phone ;
    @ApiModelProperty("邮箱")
    private String email;
    @ApiModelProperty("头像")
    private String avatarUrl;
    @ApiModelProperty("角色")
    private String roLes;


}
