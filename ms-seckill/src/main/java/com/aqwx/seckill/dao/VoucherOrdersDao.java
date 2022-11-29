package com.aqwx.seckill.dao;

import com.aqwx.common.model.entity.VoucherOrders;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface VoucherOrdersDao {

    /*根据用户ID和秒杀ID查询代金券订单*/
    @Select("select id,order_no,fk_voucher_id,fk_diner_id,qrcode,payment,status,fk_seckill_id,order_type,create_time,udpate_time,is_valid "
            +"from t_voucher_orders "
            +"where fk_diner_id = #{userId} and fk_seckill_id = #{seckillId} and is_valid = '1' and status between 0 and 1")
    VoucherOrders findDinerOrder(@Param("userId") Integer userId, @Param("seckillId") Integer seckillId);

    /*新增代金券订单*/
    @Insert("insert into t_voucher_orders (order_no,fk_voucher_id,fk_diner_id,qrcode,payment,status,fk_seckill_id,order_type,create_time,udpate_time,is_valid)"
            +"values ( #{orderNo},#{fkVoucherId},#{fkDinerId},#{qrcode},#{payment},#{status},#{fkSeckillId},#{orderType},now(),now(),'1' )")
    int save(VoucherOrders voucherOrders);
}
