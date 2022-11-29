package com.aqwx.common.model.entity;

import com.aqwx.common.model.base.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/*用户实体类*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Diners extends BaseModel {

    //主键
    private Integer id;
    //用户名
    private String username ;
    //昵称
    private String nickname;
    //密码
    private String password;

    //手机号
    private String phone;
    //邮箱
    private String email;
    //头像
    private String avatarUrl;
    //角色
    private String roles;

}
