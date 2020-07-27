package com.changgou.oauth.service.impl;

import com.changgou.oauth.service.UserLoginService;
import com.changgou.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Map;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 17:05 2019/8/23
 */
@Service
public class UserLoginServiceImpl implements UserLoginService {



    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private LoadBalancerClient loadBalancerClient;


    /**
     * 授权操作 模拟发送请求获得token
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @param grant_type
     */
    @Override
    public AuthToken login(String username, String password, String clientId, String clientSecret, String grant_type)  {
        // discoveryClient.getInstances()
        // String url = "http://localhost:9001/oauth/token ";
        // 请求的url
        ServiceInstance serviceInstance = loadBalancerClient.choose("user-auth");
        String uri = serviceInstance.getUri().toString();
        String url = uri + "/oauth/token";
        // 请求体(授权方式)
        MultiValueMap body = new LinkedMultiValueMap();
        body.add("grant_type",grant_type);
        body.add("username",username);
        body.add("password",password);

        MultiValueMap headers = new LinkedMultiValueMap();
        byte[] encode = Base64.getEncoder().encode((clientId + ":" + clientSecret).getBytes());
        String encodeMsg = null;
        try {
            encodeMsg = new String(encode,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        headers.add("Authorization", "Basic " + encodeMsg);

        HttpEntity httpEntity = new HttpEntity(body,headers);
        // 发请求arg01: 请求的url arg02:请求的方式 arg03：请求的体(数据) arg04:响应的结果
        // 将请求信息装换成map集合然后封装到token对象中
        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);
        Map map = responseEntity.getBody();
        // 将令牌信息封装到AuthToken中
        AuthToken authToken = new AuthToken();
        // 生成的令牌
        authToken.setAccessToken(map.get("access_token").toString());
        // 刷新的令牌
        authToken.setRefreshToken(map.get("refresh_token").toString());
        // 短令牌
        authToken.setJti(map.get("jti").toString());
        return authToken;
    }
}
