package com.aqwx.common.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "用户积分总排行榜视图")
public class DinerPointsRankVO extends ShortDinerInfo{


    @ApiModelProperty("总积分")
    private int total;

    @ApiModelProperty("排名")
    private int ranks;

    @ApiModelProperty(value = "是否是自己", example = "0=否，1=是")
    private int isMe;

}
