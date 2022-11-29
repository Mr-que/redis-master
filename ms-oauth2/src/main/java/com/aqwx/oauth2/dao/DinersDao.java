package com.aqwx.oauth2.dao;

import com.aqwx.common.model.entity.Diners;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/*用户Dao*/
public interface DinersDao {

    @Select("select id,username,nickname, phone,email,password,avatar_url,roles,is_valid from t_diners where (username = #{account} or phone = #{account} or email = #{account}) ")
    Diners selectByAccountInfo(@Param("account") String account);

}
