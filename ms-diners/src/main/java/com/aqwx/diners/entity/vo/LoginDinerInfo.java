package com.aqwx.diners.entity.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/*前台展示视图实体类*/
@Getter
@Setter
public class LoginDinerInfo implements Serializable {

    private String nickname;
    private String token;
    private String avatarUrl;

}
