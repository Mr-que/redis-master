package com.aqwx.common.model.vo;

import com.aqwx.common.model.entity.Reviews;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(description = "餐厅评论视图实体类")
public class ReviewsVO extends Reviews {

    @ApiModelProperty("食客信息")
    private ShortDinerInfo dinerInfo;
    @ApiModelProperty(value = "创建日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date createDate;


}
