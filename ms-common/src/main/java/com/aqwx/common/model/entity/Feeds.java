package com.aqwx.common.model.entity;

import com.aqwx.common.model.base.BaseModel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Feed信息类")
public class Feeds extends BaseModel {

    @ApiModelProperty("内容")
    private String content;
    @ApiModelProperty("发布人ID")
    private Integer fkDinerId;
    @ApiModelProperty("点赞")
    private Integer praiseAmount;
    @ApiModelProperty("评论")
    private Integer commentAmount;
    @ApiModelProperty("关联的板块")
    private Integer fkRestaurantId;

}
