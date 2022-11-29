package com.aqwx.common.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "附近的人视图")
public class NearMeDinerVO extends ShortDinerInfo{

    @ApiModelProperty(value = "距离",example = "98m")
    private String distance;


}
