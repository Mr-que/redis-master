package com.aqwx.common.model.entity;

import com.aqwx.common.model.base.BaseModel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "用户关注实体类")
public class Follow extends BaseModel {

    @ApiModelProperty("用户ID")
    private int dinerId;

    @ApiModelProperty("关注用户ID")
    private Integer followDinerId;

}
