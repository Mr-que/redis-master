package com.aqwx.follow.dao;

import com.aqwx.common.model.entity.Follow;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/*关注功能*/
public interface FollowDao {

    // 查询关注信息
    @Select("select id, diner_id, follow_diner_id, is_valid from t_follow " +
            "where diner_id = #{dinerId} and follow_diner_id = #{followDinerId}")
    Follow selectFollow(@Param("dinerId") Integer dinerId, @Param("followDinerId") Integer followDinerId);

    // 添加关注信息
    @Insert("insert into t_follow (diner_id, follow_diner_id, is_valid, create_time, update_time)" +
            " values(#{dinerId}, #{followDinerId}, 1, now(), now())")
    int save(@Param("dinerId") Integer dinerId, @Param("followDinerId") Integer followDinerId);

    // 修改关注信息
    @Update("update t_follow set is_valid = #{isFollowed}, update_time = now() where id = #{id}")
    int update(@Param("id") Integer id, @Param("isFollowed") int isFollowed);



}
