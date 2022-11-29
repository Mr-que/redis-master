package com.aqwx.gateway.fliter;

import com.aqwx.gateway.component.HandleException;
import com.aqwx.gateway.config.IgnoreUrlsConfig;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Resource
    private IgnoreUrlsConfig ignoreUrlsConfig;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private HandleException handleException;
    @Value("${service.name.ms-oauth-service}")
    private String oauth2Url;

    //身份校验
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        /*当前请求是否在白名单中*/
        AntPathMatcher pathMatcher = new AntPathMatcher();
        boolean flag = false;
        String path = exchange.getRequest().getURI().getPath();//当前的请求路径
        for (String url : ignoreUrlsConfig.getUrls()) {
            if (pathMatcher.match(url, path)) {
                flag = true;
                break;
            }
        }
        /*白名单放行*/
        if (flag){
            return chain.filter(exchange);
        }
        /*获取access_token*/
        String access_token = exchange.getRequest().getQueryParams().getFirst("access_token");
        /*判断token是否为空，为空返回JSON错误信息*/
        if(StringUtils.isBlank(access_token)){
            return handleException.writeError(exchange,"请登录");
        }
        /*验证token是否有效*/
        String checkTokenUrl = oauth2Url+"oauth/check_token?token=".concat(access_token);
        try {
            ResponseEntity<String> getEntity = restTemplate.getForEntity(checkTokenUrl, String.class);
            if(getEntity.getStatusCode()!= HttpStatus.OK){
                return handleException.writeError(exchange,"Token无效".concat(access_token));
            }
            if( StringUtils.isBlank(getEntity.getBody()) ){
                return handleException.writeError(exchange,"数据为空!".concat(access_token));
            }
        } catch (RestClientException e) {
            return handleException.writeError(exchange,"Token校验传输出错!".concat(access_token));
        }
        /*放行*/
        return chain.filter(exchange);

    }

    /*网关过滤器的排序，数字越小优先级越高*/
    public int getOrder() {
        return 0;
    }

}