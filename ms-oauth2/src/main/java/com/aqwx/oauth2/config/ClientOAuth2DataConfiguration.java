package com.aqwx.oauth2.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/*客户端配置类*/
@Data
@Component
@ConfigurationProperties(prefix = "oauth2.client")
public class ClientOAuth2DataConfiguration {

    private String clientId;//客户端标识ID
    private String secret;//客户端安全码
    private String[] grantType;//授权类型
    private int accessTokenValiditySeconds;//token有效期
    private int refreshTokenValiditySeconds;//refresh-token有效期
    private String[] scope;//客户端访问范围


}
