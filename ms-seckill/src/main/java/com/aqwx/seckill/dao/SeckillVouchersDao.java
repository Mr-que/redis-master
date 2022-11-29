package com.aqwx.seckill.dao;

import com.aqwx.common.model.entity.SeckillVouchers;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface SeckillVouchersDao {

    /*新增秒杀活动*/
    @Insert("insert into t_seckill_vouchers (id,fk_voucher_id,amount,start_time,end_time,is_valid,create_time,update_time)"+
            "values ( #{id},#{fkVoucherId},#{amount},#{startTime},#{endTime},'1',now(),now() )")
    int save(SeckillVouchers seckillVouchers);

    @Select("select id,fk_voucher_id,amount,start_time,end_time,is_valid " +
            "from t_seckill_vouchers where fk_voucher_id = #{fkVoucherId}")
    SeckillVouchers selectVouchers(Integer fkVoucherId);

    /*减少库存*/
    @Update("update t_seckill_vouchers set amount = amount -1 where id = #{seckillId}")
    int stockDecrease(@Param("seckillId") int seckillId);

}
