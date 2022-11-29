package com.aqwx.diners.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "client.oauth2")
@Setter
@Getter
public class OAuth2ClientConfiguration {

    private String clientId;
    private String secret;
    private String grant_type;
    private String scope;
}
