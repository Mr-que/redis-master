package com.aqwx.common.constant;

import lombok.Getter;

/**
 *  redis keys
 */
@Getter
public enum RedisKeyConstant {

    verify_code("verify_code:","验证码"),
    seckill_vouchers("seckill_vouchers:","秒杀卷的Key"),
    lock_key("lockby:","分布式锁的key"),
    following("following:","关注集合Key"),
    followers("followers:","粉丝集合Key"),
    following_feeds("following_feeds:","我关注的用户的Feeds的Key"),
    diner_points("diner:points", "diner用户的积分Key"),
    diner_location("diner:location", "diner地理位置Key"),
    restaurants("restaurants:","餐厅的主键"),
    restaurant_new_reviews("restaurant:new:reviews:", "餐厅评论Key");

    private String key;
    private String desc;

    RedisKeyConstant(String key, String desc) {
        this.key = key;
        this.desc = desc;
    }

}
