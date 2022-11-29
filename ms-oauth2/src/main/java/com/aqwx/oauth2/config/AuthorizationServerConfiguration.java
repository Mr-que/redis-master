package com.aqwx.oauth2.config;

import com.aqwx.common.model.domain.SignInidentity;
import com.aqwx.oauth2.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.annotation.Resource;
import java.util.LinkedHashMap;

/*授权服务*/
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    @Resource
    private RedisTokenStore redisTokenStore;
    @Resource
    private AuthenticationManager authenticationManager;
    @Resource
    private PasswordEncoder passwordEncoder;//密码加密器
    @Resource
    private ClientOAuth2DataConfiguration configuration;//自定义配置类
    @Resource
    private UserService userService;//登陆校验


    /*配置令牌安全管理约束*/
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
       security.tokenKeyAccess("permitAll()") //允许访问Token的公钥，默认 /oauth/token_key 是受保护的
               .checkTokenAccess("permitAll()");//允许检查token的状态，默认 /oauth/check_key 是受保护的

    }

    /*客户端配置，配置授权模型
        client:
            oauth2:
                client-id: appId          #客户端标识ID
                secret: 123456            #客户端安全码
                grant_type: password      #授权类型,下划线不是驼峰，因此实体类要有下划线
                scopes: api               #客户端访问范围
    **/
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
       clients.inMemory().withClient(configuration.getClientId())
               .secret(passwordEncoder.encode(configuration.getSecret()))//使用密码编译器进行加密
               .refreshTokenValiditySeconds(configuration.getRefreshTokenValiditySeconds())
               .accessTokenValiditySeconds(configuration.getAccessTokenValiditySeconds())
               .scopes(configuration.getScope())
               .authorizedGrantTypes(configuration.getGrantType());//授权类型
    }

    /*配置授权以及令牌的访问端点和令牌服务*/
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        //认证器
        endpoints.authenticationManager(authenticationManager)//认知管理对象
                .userDetailsService(userService)     //具体登录方法,实现UserDetailsService接口
                .tokenStore(redisTokenStore)       //存放Token的方式
                /*令牌增强对象，增强返回的结果*/
                .tokenEnhancer((accessToken,authentication)->{
                    //获取登录后用户的信息并设置，封装到LoginDinerInfo类中
                    SignInidentity principal = (SignInidentity) authentication.getPrincipal();
                    LinkedHashMap hashMap = new LinkedHashMap();
                    hashMap.put("nickname",principal.getNickname());
                    hashMap.put("avatarUrl",principal.getAvatarUrl());
                    DefaultOAuth2AccessToken auth2AccessToken = (DefaultOAuth2AccessToken) accessToken;
                    auth2AccessToken.setAdditionalInformation(hashMap);
                    return auth2AccessToken;
                });
    }


}
