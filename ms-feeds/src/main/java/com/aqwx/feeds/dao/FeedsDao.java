package com.aqwx.feeds.dao;

import com.aqwx.common.model.entity.Feeds;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Set;

public interface FeedsDao {

    // 添加 Feed
    @Insert("insert into t_feeds (content, fk_diner_id, praise_amount, " +
            " comment_amount, fk_restaurant_id, create_time, update_time, is_valid) " +
            " values (#{content}, #{fkDinerId}, #{praiseAmount}, #{commentAmount}, #{fkRestaurantId}, " +
            " now(), now(), 1)")
    int save(Feeds feeds);


    // 查询 Feed
    @Select("select id, content, fk_diner_id, praise_amount, " +
            " comment_amount, fk_restaurant_id, create_time, update_time, is_valid " +
            " from t_feeds where id = #{id} and is_valid = 1 and fk_diner_id = #{userId} ")
    Feeds findById(@Param("id") Integer id, @Param("userId") Integer userId);

    // 逻辑删除 Feed
    @Update("update t_feeds set is_valid = 0 where id = #{id} and is_valid = 1")
    int delete(@Param("id") Integer id);

    // 根据用户 ID 查询 Feed
    @Select("select id, content, update_time from t_feeds " +
            " where fk_diner_id = #{dinerId} and is_valid = 1")
    List<Feeds> findByDinerId(@Param("dinerId") Integer dinerId);

    // 根据多主键查询 Feed
    @Select("<script> " +
            " select id, content, fk_diner_id, praise_amount, " +
            " comment_amount, fk_restaurant_id, create_time, update_time, is_valid " +
            " from t_feeds where is_valid = 1 and id in " +
            " <foreach item=\"id\" collection=\"feedIds\" open=\"(\" separator=\",\" close=\")\">" +
            "   #{id}" +
            " </foreach> order by id desc" +
            " </script>")
    List<Feeds> findFeedsByIds(@Param("feedIds") Set<Integer> feedIds);
}
