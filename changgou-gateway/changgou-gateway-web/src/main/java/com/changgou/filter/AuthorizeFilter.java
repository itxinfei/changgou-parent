package com.changgou.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 12:21 2019/8/23
 */
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {

    //定义常量
    private static final String AUTHORIZE_TOKEN = "Authorization";

    // 登录页面
    private static final String LOGIN_URL = "http://localhost:9001/oauth/login";


    /**
     * 指定过滤器的优先级
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取request, response
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 判断用户是否是登录操作，如果是，则直接放行
        String url = request.getURI().getPath();
        if (URLFilter.hasAuthorization(url)){
        // 如果是登陆 或者注册 放行
        return chain.filter(exchange);
        }

        // 其他请求判断用户是否登录(判断用户是否携带token信息，从请求参数获得token)
        String token = request.getQueryParams().getFirst(AUTHORIZE_TOKEN);
        // 从请求头获得token
        if (StringUtils.isEmpty(token)){
            token = request.getHeaders().getFirst(AUTHORIZE_TOKEN);
        }
        // 如果请求头中不包含token就从cooKie中获取
        if (StringUtils.isEmpty(token)){
            HttpCookie cookie = request.getCookies().getFirst(AUTHORIZE_TOKEN);
            if (cookie != null){
               token = cookie.getValue();
            }
        }
        // 如果还是没有获取到token说明用户处于未登录状态，不放行(无效认证)
        if (StringUtils.isEmpty(token)){
            // 如果用户未登录,踢回登录页面
            response.setStatusCode(HttpStatus.SEE_OTHER);
            String path = LOGIN_URL + "?ReturnUrl=" + request.getURI().toString();
            response.getHeaders().add("Location",path);
            return response.setComplete();
            // 响应未登录状态
//            response.setStatusCode(HttpStatus.UNAUTHORIZED);
//            return response.setComplete();
        }

        try {
            // token不为空，解析token(只能解析jwt加密的token)
//            Claims claims = JwtUtil.parseJWT(token);
//            request.mutate().header("Authorization_Token",token);
            //  由oauth生成令牌
            // 将token添加到请求头信息,有对应的微服务去解析
            request.mutate().header(AUTHORIZE_TOKEN,"bearer "+ token);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("token非法！！！！！！！！！");
            // 解析失败，不放行 响应未登录状态
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        // 解析成功
        return chain.filter(exchange);
    }


    /**
     * 处理业务
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
