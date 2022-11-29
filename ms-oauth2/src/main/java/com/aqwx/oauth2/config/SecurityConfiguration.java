package com.aqwx.oauth2.config;

import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;

/*SpringSecurity配置*/
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    /*注入Redis连接工厂*/
    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    /*初始化RedisTokenStore*/
    @Bean
    public RedisTokenStore redisTokenStore(){
        RedisTokenStore redisTokenStore = new RedisTokenStore(redisConnectionFactory);
        redisTokenStore.setPrefix("TOKEN:");//设置key的层级前缀，方便查询
        return redisTokenStore;
    }

    /*初始化密码编码器*/
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new PasswordEncoder() {

            /*加密原始密码*/
            @Override
            public String encode(CharSequence charSequence) {
                return DigestUtil.md5Hex(charSequence.toString());
            }

            /*校验密码*/
            @Override
            public boolean matches(CharSequence inputPassword, String encodePassword) {
                return DigestUtil.md5Hex(inputPassword.toString()).equals(encodePassword);
            }

        };
    }

    /*初始化认证管理对象*/
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


    /*初始化认证放行对象*/
    @Override
    protected void configure(HttpSecurity http) throws Exception {
       http.csrf().disable()
               .authorizeRequests()
               //放行的请求
               .antMatchers("/oauth/**","/actuator/**").permitAll()
               .and()
               .authorizeRequests()
               //其他请求必须认证才能访问
               .anyRequest().authenticated();
    }


}
