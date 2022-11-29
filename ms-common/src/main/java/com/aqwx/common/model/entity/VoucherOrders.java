package com.aqwx.common.model.entity;

import com.aqwx.common.model.base.BaseModel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Setter;

@Setter
@ApiModel(description = "订单创建实体类")
public class VoucherOrders extends BaseModel {

    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("代金券")
    private Integer fkVoucherId;

    @ApiModelProperty("下单用户")
    private Integer fkDinerId;

    @ApiModelProperty( "生成qrcode地址")
    private String qrcode ;

    @ApiModelProperty("支付方式0=微信支付1=支付宝")
    private int payment ;

    @ApiModelProperty("订单状态-1=已取消 0=未支付1=已支付2=已消费了=已过期")
    private Integer status;

    @ApiModelProperty("订单类型0=正常订单1=抢购订单")
    private Integer orderType;

    @ApiModelProperty ("抢购代金券的外键ID" )
    private Integer fkSeckillId;


}
