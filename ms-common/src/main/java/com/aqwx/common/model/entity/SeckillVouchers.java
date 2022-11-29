package com.aqwx.common.model.entity;

import com.aqwx.common.model.base.BaseModel;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
@ApiModel(description = "抢购代金券信息")
public class SeckillVouchers extends BaseModel {

    @ApiModelProperty("代金券ID")
    private Integer fkVoucherId;

    @ApiModelProperty("数量")
    private int amount;

    @ApiModelProperty ("代金券抢购开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date startTime;

    @ApiModelProperty("代金券抢购结束时间")
    @JsonFormat (pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date endTime;


}
