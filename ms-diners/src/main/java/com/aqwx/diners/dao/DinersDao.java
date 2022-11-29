package com.aqwx.diners.dao;

import com.aqwx.common.model.dto.DinersDTO;
import com.aqwx.common.model.entity.Diners;
import com.aqwx.common.model.vo.ShortDinerInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DinersDao {

    /* 查询手机号是否存在 */
    @Select("select id,username, phone,email,is_valid from t_diners where phone = #{phone}")
    Diners selectByPhone(@Param("phone") String phone);

    /* 查询用户注册名是否存在 */
    @Select("select id,username, phone,email,is_valid from t_diners where username = #{username}")
    Diners selectByUsername(@Param("username") String username);

    /*保存用户信息*/
    @Insert("insert into t_diners(username,phone,email,password,roles,is_valid,create_time,update_time) "
            + "values (#{username},#{phone},null,#{password},\"ROLE_USER\",'1',now(),now())")
    void saveDiners(DinersDTO diners);

    // 根据 ID 集合查询多个用户信息
    @Select("<script> " +
            " select id, nickname, avatar_url from t_diners " +
            " where is_valid = 1 and id in " +
            " <foreach item=\"id\" collection=\"ids\" open=\"(\" separator=\",\" close=\")\"> " +
            "   #{id} " +
            " </foreach> " +
            " </script>")
    List<ShortDinerInfo> findByIds(@Param("ids") String[] ids);
}
