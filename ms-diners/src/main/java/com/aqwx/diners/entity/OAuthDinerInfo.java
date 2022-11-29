package com.aqwx.diners.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/*登录返回数据实体类*/
@Getter
@Setter
public class OAuthDinerInfo implements Serializable {

    private String nickname;
    private String avatarUrl;
    private String accessToken;
    private String expireIn;
    private List<String> scopes;
    private String refreshToken;

}
