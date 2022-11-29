package com.aqwx.seckill.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.IdUtil;
import com.aqwx.common.constant.ApiConstant;
import com.aqwx.common.constant.RedisKeyConstant;
import com.aqwx.common.exception.ParameterException;
import com.aqwx.common.model.domain.ResultInfo;
import com.aqwx.common.model.entity.SeckillVouchers;
import com.aqwx.common.model.entity.VoucherOrders;
import com.aqwx.common.model.vo.SingInDinerInfo;
import com.aqwx.common.utils.AssertUtil;
import com.aqwx.common.utils.ResultInfoUtil;
import com.aqwx.seckill.dao.SeckillVouchersDao;
import com.aqwx.seckill.dao.VoucherOrdersDao;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillService {

    @Resource
    private VoucherOrdersDao voucherOrdersDao;
    @Resource
    private SeckillVouchersDao seckillVouchersDao;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private DefaultRedisScript defaultRedisScript;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RedissonClient redissonClient;

    @Value("${service.name.ms-oauth-service}")
    String oauth2ServerName;

    public void addSecKillVouchers(SeckillVouchers seckillVouchers) {
        /*非空校验*/
        AssertUtil.isTrue(seckillVouchers.getFkVoucherId()==null,"代金券ID不存在!");
        AssertUtil.isTrue(seckillVouchers.getAmount()<0,"代金券数量不能为小于0!");
        AssertUtil.isTrue(seckillVouchers.getStartTime()==null,"代金券抢购开始时间不能为空!");
        AssertUtil.isTrue(seckillVouchers.getEndTime()==null,"代金券抢购结束时间不能为空!");
        AssertUtil.isTrue(seckillVouchers.getStartTime().after(seckillVouchers.getEndTime()),"抢购开始时间不能晚于结束时间!");

        //使用sql保存代金券活动流程
        //根据代金券id查询是否重复添加
        //SeckillVouchers getHot = seckillVouchersDao.selectVouchers(seckillVouchers.getFkVoucherId());
        //AssertUtil.isTrue(getHot != null, "已经添加过了该代金券活动");
        //seckillVouchers.setId( (int)( UUID.randomUUID().hashCode()+(Math.random()*1000)+new Random().nextInt(100000) ));//生成随机ID
        //seckillVouchersDao.save(seckillVouchers);

        /*采用redis实现 将代金券存入redis中*/
        String key = RedisKeyConstant.seckill_vouchers.getKey() + seckillVouchers.getFkVoucherId();
        Map<String,Object> map = redisTemplate.opsForHash().entries(key);
        AssertUtil.isTrue(!map.isEmpty() && (int)map.get("amount") > 0 ,"已经添加过了该代金券活动");

        seckillVouchers.setIsValid(1);
        seckillVouchers.setId( (int)( UUID.randomUUID().hashCode()+(Math.random()*1000)+new Random().nextInt(100000) ));//生成随机ID
        seckillVouchers.setCreateTime(new Date());
        seckillVouchers.setUpdateTime(new Date());
        Map<String, Object> changeMap = BeanUtil.beanToMap(seckillVouchers);
        redisTemplate.opsForHash().putAll(key, changeMap);
    }

    public ResultInfo doSecKill(Integer voucherId, String access_token, String servletPath) {
        /*基本参数校验*/
        AssertUtil.isTrue(voucherId==null || voucherId<0,"请选择需要抢购的代金券");
        AssertUtil.isNotEmpty(access_token,"请登录");

        //从MySql中实现
        ///*判断代金券是否有抢购活动*/
        //SeckillVouchers seckillVouchers = seckillVouchersDao.selectVouchers(voucherId);
        //AssertUtil.isTrue(seckillVouchers==null,"该代金券没有抢购活动!");
        ///*判断活动是否有效*/
        //AssertUtil.isTrue(seckillVouchers.getIsValid()==0,"该活动已经过期!");

        /*使用redis实现*/
        String key = RedisKeyConstant.seckill_vouchers.getKey()+voucherId;
        Map<String,Object> map = redisTemplate.opsForHash().entries(key);
        AssertUtil.isTrue(map.isEmpty(),"该代金券没有抢购活动!");
        SeckillVouchers seckillVouchers = BeanUtil.mapToBean(map, SeckillVouchers.class, true, null);

        /*判断是否开始、结束*/
        Date now = new Date();
        AssertUtil.isTrue(seckillVouchers.getStartTime().after(now),"抢购活动还未开始!");
        AssertUtil.isTrue(seckillVouchers.getEndTime().before(now),"抢购活动已结束!");
        /*判断是否售空*/
        AssertUtil.isTrue(seckillVouchers.getAmount()<1,"该代金券已经卖完了!");

        /*获取用户信息*/
        String url = oauth2ServerName+"user/me?access_token={access_token}";
        ResultInfo resultInfo = restTemplate.getForObject(url, ResultInfo.class, access_token);
        if (resultInfo.getCode()!= ApiConstant.SUCCESS_CODE){
            resultInfo.setPath(servletPath);
            return resultInfo;
        }
        SingInDinerInfo dinerInfo = BeanUtil.fillBeanWithMap((LinkedHashMap) resultInfo.getData(), new SingInDinerInfo(), false);
        //根据用户id和代金券id判断是否抢到了代金券
        VoucherOrders orders = voucherOrdersDao.findDinerOrder(dinerInfo.getId(),seckillVouchers.getId());
        AssertUtil.isTrue(orders!=null, "该用户已拥有代金券！");

        //使用MySql减少库存
        //int count = seckillVouchersDao.stockDecrease(seckillVouchers.getId());
        //AssertUtil.isTrue(count==0, "代金券修改失败！");


        /*创建Redis重入锁，锁一个账号，解决一人多单问题*/
        String lockName = RedisKeyConstant.lock_key.getKey() + dinerInfo.getId() + ":" + voucherId;//登录用户的主键+优惠券ID组合为Key
        long expireTime = seckillVouchers.getEndTime().getTime() - now.getTime();

        RLock lock = redissonClient.getLock(lockName);

        try {
            boolean isLocked = lock.tryLock(expireTime, TimeUnit.MICROSECONDS);
            if (isLocked){
                //采用redis实现
                List<String> keys = new ArrayList<>();
                keys.add(key);
                keys.add("amount");
                //通过lua脚本进行代金券数量查询和代金券数量减少
                Long redisAccount = (Long) redisTemplate.execute(defaultRedisScript, keys);
                AssertUtil.isTrue(redisAccount==null || redisAccount < 1, "代金券数量为0！");

                /*下单 将订单保存到订单实体类中*/
                VoucherOrders voucherOrders = new VoucherOrders();
                voucherOrders.setFkDinerId(dinerInfo.getId());
                voucherOrders.setFkSeckillId(seckillVouchers.getId());
                voucherOrders.setFkVoucherId(seckillVouchers.getFkVoucherId());
                String orderNo = IdUtil.getSnowflake(1, 1).nextIdStr();
                voucherOrders.setOrderNo(orderNo);
                voucherOrders.setOrderType(1);
                voucherOrders.setStatus(0);
                int save = voucherOrdersDao.save(voucherOrders);
                AssertUtil.isTrue(save==0, "抢购失败，请重试！");
                return ResultInfoUtil.buildSuccess(servletPath, "抢购成功！");
            }
        }catch (Exception e) {
            /*手动回滚事务*/
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            /*自定义Redis解锁*/
            //  redisLock.unlock(lockName,lockKey);

            lock.unlock();
            if(e instanceof ParameterException){
                return ResultInfoUtil.buildError(0,"卷已经卖完了!",servletPath);
            }
        }

        return ResultInfoUtil.buildSuccess(servletPath,"抢购成功!");
    }
}
