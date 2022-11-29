package com.aqwx.common.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;


@Data
@ApiModel(description = "关注用户信息")
public class ShortDinerInfo implements Serializable {

    @ApiModelProperty("主键")
    public Integer id;
    @ApiModelProperty("昵称")
    private String nickname;
    @ApiModelProperty("头像")
    private String avatarUrl;


}
