package com.aqwx.common.model.entity;

import com.aqwx.common.model.base.BaseModel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "用户积分服务")
public class DinerPoints extends BaseModel {

    @ApiModelProperty("关联DinerId")
    private Integer fkDinerId;
    @ApiModelProperty("积分")
    private Integer points;
    @ApiModelProperty(name = "类型",example = "0=签到，1=关注好友，2=添加Feed，3=添加商户评论")
    private Integer types;

}
