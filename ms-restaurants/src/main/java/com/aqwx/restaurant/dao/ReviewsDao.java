package com.aqwx.restaurant.dao;

import com.aqwx.common.model.entity.Reviews;
import org.apache.ibatis.annotations.Insert;

public interface ReviewsDao {

    // 插入餐厅评论
    @Insert("insert into t_reviews (fk_restaurant_id, fk_diner_id, content, like_it, is_valid, create_time, update_time)" +
            " values (#{fkRestaurantId}, #{fkDinerId}, #{content}, #{likeIt}, 1, now(), now())")
    int saveReviews(Reviews reviews);

}
