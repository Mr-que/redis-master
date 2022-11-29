package com.aqwx.gateway.component;

import com.aqwx.common.constant.ApiConstant;
import com.aqwx.common.model.domain.ResultInfo;
import com.aqwx.common.utils.ResultInfoUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;

@Component
public class HandleException {

    @Autowired
    private ObjectMapper objectMapper;

    public Mono<Void> writeError(ServerWebExchange exchange, String error){

        ServerHttpResponse response = exchange.getResponse();
        ServerHttpRequest request = exchange.getRequest() ;
        response.setStatusCode (HttpStatus.OK);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        ResultInfo resultInfo = ResultInfoUtil.buildError(ApiConstant.NO_LOGIN_CODE,ApiConstant.NO_LOGIN_MESSAGE,request.getURI().getPath());
        String resultInfoJson = null;
        DataBuffer buffer = null;
        try {
            resultInfoJson = objectMapper.writeValueAsString(resultInfo);
            buffer = response.bufferFactory().wrap(resultInfoJson.getBytes(Charset.forName("utf-8")));
        }catch(JsonProcessingException ex) {
            ex. printStackTrace();
        }
        return response.writeWith(Mono.just(buffer));
    }

}
